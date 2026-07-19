package com.example

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class CouponResponse(
    val success: Boolean,
    val discountPercent: Float,
    val code: String,
    val message: String
)

interface BookingApiService {
    @POST("api/bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body booking: Booking
    ): Booking

    @GET("api/bookings")
    suspend fun getBookings(
        @Header("Authorization") token: String
    ): List<Booking>

    @PATCH("api/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Booking

    @PATCH("api/bookings/{id}/reschedule")
    suspend fun rescheduleBooking(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Booking

    @POST("api/coupons/apply")
    suspend fun applyCoupon(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): CouponResponse

    @PATCH("api/bookings/{id}/technician")
    suspend fun assignTechnician(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Booking
}
