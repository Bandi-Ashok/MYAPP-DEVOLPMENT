package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthServiceTest {

    private lateinit var context: Context
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear preferences before each test
        context.getSharedPreferences("onecall_auth_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        authManager = AuthManager(context)
    }

    @Test
    fun testInitialSessionIsEmpty() {
        assertNull(authManager.currentUserState)
        assertNull(authManager.getAccessToken())
        assertNull(authManager.getRefreshToken())
        assertEquals("customer", authManager.getUserRole())
        assertTrue(authManager.getUserAddresses().isEmpty())
    }

    @Test
    fun testOfflineAuthenticationSuccess() {
        // Mock a registered user in DB cache
        val user = AuthUser(
            id = "usr_999",
            name = "Test User",
            email = "test@onecall.co.in",
            phone = "+91 99999 99999",
            addresses = listOf("123 Test Lane"),
            primary_address = "123 Test Lane",
            role = "technician",
            created_at = "2026-07-19T00:00:00Z"
        )
        
        // Simulates saving a login session
        // Using private helper simulation in tests by accessing sharedPrefs directly or triggering login mock
        val prefs = context.getSharedPreferences("onecall_auth_prefs", Context.MODE_PRIVATE)
        
        // Hash for pin "2819" is SHA-256 of "2819"
        val sha256Pin = "c05562111bb2b94ae2eebdbb85e408884622fffd762a7e132198b960d2ad4d17" // sha256("2819")
        val sha256Pass = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8" // sha256("password")

        prefs.edit()
            .putString("access_token", "jwt_access_mock")
            .putString("refresh_token", "jwt_refresh_mock")
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_phone", user.phone)
            .putString("user_role", user.role)
            .putString("user_primary_address", user.primary_address)
            .putString("user_pin_hash", sha256Pin)
            .putString("user_pass_hash", sha256Pass)
            .apply()

        // Re-initialize manager to load from prefs
        val freshManager = AuthManager(context)
        assertNotNull(freshManager.currentUserState)
        assertEquals("Test User", freshManager.currentUserState?.fullName)
        assertEquals("technician", freshManager.getUserRole())

        // Test offline credentials check
        assertTrue(freshManager.tryOfflineLogin("test@onecall.co.in", "2819", null))
        assertTrue(freshManager.tryOfflineLogin("test@onecall.co.in", null, "password"))
        
        // Fail checks
        assertFalse(freshManager.tryOfflineLogin("test@onecall.co.in", "wrong_pin", null))
        assertFalse(freshManager.tryOfflineLogin("other@onecall.co.in", "2819", null))
    }

    @Test
    fun testLogoutClearsSession() {
        val prefs = context.getSharedPreferences("onecall_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", "token")
            .putString("user_email", "test@email.com")
            .apply()

        val freshManager = AuthManager(context)
        assertNotNull(freshManager.currentUserState)

        freshManager.logout()
        assertNull(freshManager.currentUserState)
        assertNull(freshManager.getAccessToken())
        assertNull(freshManager.getRefreshToken())
    }
}
