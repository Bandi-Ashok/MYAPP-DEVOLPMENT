package com.example

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BookingRepository(context: Context, private val authManager: AuthManager) {
    private val db = BookingDatabase.getDatabase(context)
    private val bookingDao = db.bookingDao()
    private val transactionDao = db.transactionDao()
    private val notificationDao = db.notificationDao()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

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

    val apiService: BookingApiService = retrofit.create(BookingApiService::class.java)

    val allBookingsFlow: Flow<List<Booking>> = bookingDao.getAllBookingsFlow()
    val allTransactionsFlow: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val allNotificationsFlow: Flow<List<NotificationLog>> = notificationDao.getAllNotificationsFlow()

    suspend fun fetchAndSyncBookings(): Boolean = withContext(Dispatchers.IO) {
        val token = authManager.getAccessToken() ?: return@withContext false
        try {
            val remoteBookings = apiService.getBookings("Bearer $token")
            bookingDao.clearAllBookings()
            bookingDao.insertBookings(remoteBookings)
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to fetch remote bookings, relying on local cache", e)
            false
        }
    }

    suspend fun createBooking(booking: Booking): Boolean = withContext(Dispatchers.IO) {
        bookingDao.insertBooking(booking)
        
        val token = authManager.getAccessToken()
        if (token == null) {
            Log.d("BookingRepository", "Saved booking locally only")
            return@withContext true
        }

        try {
            val remote = apiService.createBooking("Bearer $token", booking)
            bookingDao.insertBooking(remote)
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to upload booking, kept in local DB", e)
            true
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Boolean = withContext(Dispatchers.IO) {
        val booking = bookingDao.getBookingById(bookingId) ?: return@withContext false
        val updated = booking.copy(status = status)
        bookingDao.insertBooking(updated)

        val token = authManager.getAccessToken()
        if (token == null) return@withContext true

        try {
            apiService.updateBookingStatus("Bearer $token", bookingId, mapOf("status" to status))
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to update status on server", e)
            true
        }
    }

    suspend fun rescheduleBooking(bookingId: String, newDate: String, newSlot: String): Boolean = withContext(Dispatchers.IO) {
        val booking = bookingDao.getBookingById(bookingId) ?: return@withContext false
        val updated = booking.copy(date = newDate, timeSlot = newSlot)
        bookingDao.insertBooking(updated)

        val token = authManager.getAccessToken()
        if (token == null) return@withContext true

        try {
            apiService.rescheduleBooking("Bearer $token", bookingId, mapOf("date" to newDate, "timeSlot" to newSlot))
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to reschedule on server", e)
            true
        }
    }

    suspend fun assignTechnician(bookingId: String, technicianName: String): Boolean = withContext(Dispatchers.IO) {
        val booking = bookingDao.getBookingById(bookingId) ?: return@withContext false
        val updated = booking.copy(technician = technicianName)
        bookingDao.insertBooking(updated)

        val token = authManager.getAccessToken()
        if (token == null) return@withContext true

        try {
            apiService.assignTechnician("Bearer $token", bookingId, mapOf("technician" to technicianName))
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to assign technician on server", e)
            true
        }
    }

    suspend fun applyCoupon(couponCode: String): CouponResponse = withContext(Dispatchers.IO) {
        val token = authManager.getAccessToken()
        if (token == null) {
            return@withContext evaluateCouponOffline(couponCode)
        }

        try {
            apiService.applyCoupon("Bearer $token", mapOf("code" to couponCode))
        } catch (e: Exception) {
            Log.e("BookingRepository", "Coupon API error, applying offline rule", e)
            evaluateCouponOffline(couponCode)
        }
    }

    private fun evaluateCouponOffline(code: String): CouponResponse {
        return when (code.uppercase()) {
            "ONECALL50" -> CouponResponse(true, 0.50f, "ONECALL50", "50% Discount applied successfully!")
            "WELCOME10" -> CouponResponse(true, 0.10f, "WELCOME10", "10% Welcome discount applied!")
            "FESTIVE20" -> CouponResponse(true, 0.20f, "FESTIVE20", "20% Festive coupon applied!")
            else -> CouponResponse(false, 0.0f, code, "Invalid or expired coupon code")
        }
    }

    suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertNotification(notification: NotificationLog) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun getLocalBookings(): List<Booking> = withContext(Dispatchers.IO) {
        bookingDao.getAllBookings()
    }

    suspend fun updateBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.insertBooking(booking)
    }

    suspend fun seedInitialData(name: String, phone: String, address: String) = withContext(Dispatchers.IO) {
        if (bookingDao.getAllBookings().isEmpty()) {
            bookingDao.insertBooking(
                Booking(
                    id = "BC-583921",
                    serviceName = "Cleaning Services",
                    subServiceName = "Bathroom Tile Grout Clean",
                    customerName = name,
                    phone = phone,
                    date = "2026-07-18",
                    timeSlot = "10:00 AM - 12:00 PM",
                    address = address,
                    status = "completed",
                    technician = "Rajesh Kumar",
                    price = 699f,
                    paymentStatus = "paid",
                    invoiceId = "INV-482019",
                    reviewStars = 5,
                    reviewComment = "Excellent and neat sanitization work!"
                )
            )
            bookingDao.insertBooking(
                Booking(
                    id = "BC-204918",
                    serviceName = "Electrical Services",
                    subServiceName = "Smart Switch & DB Setup",
                    customerName = name,
                    phone = phone,
                    date = "2026-07-19",
                    timeSlot = "02:00 PM - 04:00 PM",
                    address = address,
                    status = "dispatched",
                    technician = "Rajesh Kumar",
                    price = 1499f,
                    paymentStatus = "paid",
                    invoiceId = "INV-294018"
                )
            )
        }
        if (transactionDao.getAllTransactions().isEmpty()) {
            transactionDao.insertTransaction(
                Transaction("TXN-901823", "deposit", 5000f, "Initial Wallet Balance Linked securely", "2026-07-18")
            )
        }
        if (notificationDao.getAllNotifications().isEmpty()) {
            notificationDao.insertNotification(
                NotificationLog("NL-101", "SMS", phone.ifEmpty { "+91 80193 18625" }, "OTP 2819 Verified. Secure session established.", "Just now")
            )
            notificationDao.insertNotification(
                NotificationLog("NL-102", "WhatsApp", phone.ifEmpty { "+91 80193 18625" }, "Welcome to One Call Home Solutions. Your background checked partner is active.", "5 mins ago")
            )
        }
    }
}
