package com.example

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.security.MessageDigest

// --- AUTHENTICATION DATA STRUCTURES ---
data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val addresses: List<String>,
    val primary_address: String,
    val role: String,
    val created_at: String
)

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val user: AuthUser?
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val pin: String,
    val role: String,
    val address: String
)

data class LoginRequest(
    val email: String,
    val password: String? = null,
    val pin: String? = null
)

data class OtpSendRequest(val phone: String)
data class OtpVerifyRequest(val phone: String, val otp: String, val role: String? = null)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val otp: String, val newPassword: String, val newPin: String)
data class AddressRequest(val address: String?, val action: String, val index: Int? = null)
data class RefreshRequest(val refreshToken: String)
data class GenericMessageResponse(val success: Boolean, val message: String)

// --- RETROFIT SERVICE INTERFACE ---
interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/otp/send")
    suspend fun sendOtp(@Body request: OtpSendRequest): GenericMessageResponse

    @POST("api/auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): AuthResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GenericMessageResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericMessageResponse

    @POST("api/auth/google")
    suspend fun loginGoogle(@Body body: Map<String, String>): AuthResponse

    @POST("api/auth/apple")
    suspend fun loginApple(@Body body: Map<String, String>): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): AuthResponse

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Map<String, AuthUser>

    @POST("api/auth/address")
    suspend fun manageAddress(
        @Header("Authorization") token: String,
        @Body request: AddressRequest
    ): Map<String, AuthUser>
}

// --- SECURE SESSION AND AUTHENTICATION MANAGER ---
class AuthManager(private val context: Context) {
    private val PREFS_NAME = "onecall_auth_prefs"
    private val KEY_ACCESS_TOKEN = "access_token"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    private val KEY_USER_ID = "user_id"
    private val KEY_USER_NAME = "user_name"
    private val KEY_USER_EMAIL = "user_email"
    private val KEY_USER_PHONE = "user_phone"
    private val KEY_USER_ROLE = "user_role"
    private val KEY_USER_PRIMARY_ADDRESS = "user_primary_address"
    private val KEY_USER_ADDRESSES = "user_addresses"
    private val KEY_USER_PIN_HASH = "user_pin_hash"
    private val KEY_USER_PASS_HASH = "user_pass_hash"

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Moshi setup
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Base URL is configurable; uses 10.0.2.2 to connect to local development server on host machine from emulator
    private val BASE_URL = "http://10.0.2.2:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: AuthApiService = retrofit.create(AuthApiService::class.java)

    // Reactive states
    var currentUserState by mutableStateOf<UserState?>(null)
    var isApiLoading by mutableStateOf(false)
    var apiErrorMessage by mutableStateOf<String?>(null)

    init {
        loadSessionFromPrefs()
    }

    private fun loadSessionFromPrefs() {
        val accessToken = sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
        val email = sharedPrefs.getString(KEY_USER_EMAIL, null)
        if (accessToken != null && email != null) {
            val name = sharedPrefs.getString(KEY_USER_NAME, "User") ?: "User"
            val phone = sharedPrefs.getString(KEY_USER_PHONE, "") ?: ""
            val address = sharedPrefs.getString(KEY_USER_PRIMARY_ADDRESS, "") ?: ""
            currentUserState = UserState(
                fullName = name,
                email = email,
                phone = phone,
                address = address,
                isAuthenticated = true
            )
        }
    }

    private fun saveSession(token: String?, refresh: String?, user: AuthUser, pin: String? = null, rawPassword: String? = null) {
        val editor = sharedPrefs.edit()
        if (token != null) editor.putString(KEY_ACCESS_TOKEN, token)
        if (refresh != null) editor.putString(KEY_REFRESH_TOKEN, refresh)
        
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_PHONE, user.phone)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.putString(KEY_USER_PRIMARY_ADDRESS, user.primary_address)
        
        val addressesJson = moshi.adapter(List::class.java).toJson(user.addresses)
        editor.putString(KEY_USER_ADDRESSES, addressesJson)

        if (pin != null) {
            editor.putString(KEY_USER_PIN_HASH, sha256(pin))
        }
        if (rawPassword != null) {
            editor.putString(KEY_USER_PASS_HASH, sha256(rawPassword))
        }
        editor.apply()

        currentUserState = UserState(
            fullName = user.name,
            email = user.email,
            phone = user.phone,
            address = user.primary_address,
            isAuthenticated = true
        )
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
        currentUserState = null
    }

    fun getAccessToken(): String? = sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = sharedPrefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserRole(): String = sharedPrefs.getString(KEY_USER_ROLE, "customer") ?: "customer"

    fun getUserAddresses(): List<String> {
        val json = sharedPrefs.getString(KEY_USER_ADDRESSES, null) ?: return emptyList()
        return try {
            val list = moshi.adapter(List::class.java).fromJson(json) as? List<*>
            list?.filterIsInstance<String>() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Hash helper for secure offline validation
    private fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input
        }
    }

    // Offline Authenticator fallback
    fun tryOfflineLogin(email: String, pin: String?, password: String?): Boolean {
        val storedEmail = sharedPrefs.getString(KEY_USER_EMAIL, null) ?: return false
        if (storedEmail.lowercase() != email.lowercase()) return false

        if (pin != null) {
            val storedPinHash = sharedPrefs.getString(KEY_USER_PIN_HASH, null)
            if (storedPinHash != null && storedPinHash == sha256(pin)) {
                loadSessionFromPrefs()
                return true
            }
        }
        if (password != null) {
            val storedPassHash = sharedPrefs.getString(KEY_USER_PASS_HASH, null)
            if (storedPassHash != null && storedPassHash == sha256(password)) {
                loadSessionFromPrefs()
                return true
            }
        }
        return false
    }

    // --- REST CLIENT ACTIONS WITH LOADING & ERROR HANDLING ---

    suspend fun registerUser(request: RegisterRequest): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.register(request)
            if (response.user != null) {
                saveSession(response.accessToken, response.refreshToken, response.user, request.pin, request.password)
                true
            } else {
                apiErrorMessage = "Invalid registration response"
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Register error", e)
            apiErrorMessage = e.message ?: "Network registration failure"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun loginUser(request: LoginRequest, rawPassword: String? = null, rawPin: String? = null): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.login(request)
            if (response.user != null) {
                saveSession(response.accessToken, response.refreshToken, response.user, rawPin, rawPassword)
                true
            } else {
                apiErrorMessage = "Invalid login response"
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Login error", e)
            // Fallback to offline credentials cache if available
            val success = tryOfflineLogin(request.email, request.pin, request.password)
            if (success) {
                Log.d("AuthManager", "Offline session restored successfully")
                true
            } else {
                apiErrorMessage = e.message ?: "Authentication service unreachable"
                false
            }
        } finally {
            isApiLoading = false
        }
    }

    suspend fun sendOtpCode(phone: String): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.sendOtp(OtpSendRequest(phone))
            response.success
        } catch (e: Exception) {
            Log.e("AuthManager", "OTP send error", e)
            apiErrorMessage = e.message ?: "Unable to dispatch verification OTP"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun verifyOtpCode(phone: String, otp: String, role: String): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.verifyOtp(OtpVerifyRequest(phone, otp, role))
            if (response.user != null) {
                saveSession(response.accessToken, response.refreshToken, response.user, otp, null)
                true
            } else {
                apiErrorMessage = "Invalid verification response"
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "OTP verify error", e)
            apiErrorMessage = e.message ?: "OTP validation failed"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun forgotPassword(email: String): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            response.success
        } catch (e: Exception) {
            Log.e("AuthManager", "Forgot password error", e)
            apiErrorMessage = e.message ?: "Password reset request failed"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.resetPassword(request)
            response.success
        } catch (e: Exception) {
            Log.e("AuthManager", "Reset password error", e)
            apiErrorMessage = e.message ?: "Credentials update failed"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun performGoogleLogin(idToken: String, email: String, name: String, photo: String): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val body = mapOf(
                "idToken" to idToken,
                "email" to email,
                "name" to name,
                "photo" to photo
            )
            val response = apiService.loginGoogle(body)
            if (response.user != null) {
                saveSession(response.accessToken, response.refreshToken, response.user, "2819", null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Google login error", e)
            apiErrorMessage = e.message ?: "Google account authentication failed"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun performAppleLogin(identityToken: String, email: String, name: String): Boolean = withContext(Dispatchers.IO) {
        isApiLoading = true
        apiErrorMessage = null
        try {
            val body = mapOf(
                "identityToken" to identityToken,
                "email" to email,
                "name" to name
            )
            val response = apiService.loginApple(body)
            if (response.user != null) {
                saveSession(response.accessToken, response.refreshToken, response.user, "2819", null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Apple login error", e)
            apiErrorMessage = e.message ?: "Apple identity authentication failed"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun performTokenRefresh(): Boolean = withContext(Dispatchers.IO) {
        val refresh = getRefreshToken() ?: return@withContext false
        try {
            val response = apiService.refreshToken(RefreshRequest(refresh))
            if (response.accessToken != null) {
                val editor = sharedPrefs.edit()
                editor.putString(KEY_ACCESS_TOKEN, response.accessToken)
                if (response.refreshToken != null) {
                    editor.putString(KEY_REFRESH_TOKEN, response.refreshToken)
                }
                editor.apply()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Token refresh error", e)
            false
        }
    }

    suspend fun addNewAddress(address: String): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.manageAddress("Bearer $token", AddressRequest(address, "add"))
            val user = response["user"]
            if (user != null) {
                saveSession(null, null, user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Add address error", e)
            apiErrorMessage = e.message ?: "Failed to add address"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun deleteAddress(index: Int): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.manageAddress("Bearer $token", AddressRequest(null, "delete", index))
            val user = response["user"]
            if (user != null) {
                saveSession(null, null, user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Delete address error", e)
            apiErrorMessage = e.message ?: "Failed to delete address"
            false
        } finally {
            isApiLoading = false
        }
    }

    suspend fun setPrimaryAddress(index: Int): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        isApiLoading = true
        apiErrorMessage = null
        try {
            val response = apiService.manageAddress("Bearer $token", AddressRequest(null, "set_primary", index))
            val user = response["user"]
            if (user != null) {
                saveSession(null, null, user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Set primary address error", e)
            apiErrorMessage = e.message ?: "Failed to set primary address"
            false
        } finally {
            isApiLoading = false
        }
    }
}
