package com.amitesh.guestapp.network

import com.amitesh.guestapp.domainobject.GuestDetails
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


private const val BASE_URL = "http://192.168.0.104:8080/pms/service/v1/"

var okHttpClient = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .build()

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .build()


/**
 * A public interface that exposes the methods
 */
interface GuestAppApiService {

    @GET("profile/{profileId}/reservation/all")
    suspend fun allReservations(@Path("profileId") profileId: String): List<GuestDetails>

    @GET("profile/{profileId}/reservation/create")
    suspend fun createReservation(@Path("profileId") profileId: String): GuestDetails

    @GET("reservation/{reservationNo}/checkin")
    suspend fun checkInReservation(@Path("reservationNo") reservationNo: String): GuestDetails

    @GET("reservation/{reservationNo}/checkout")
    suspend fun checkOutReservation(@Path("reservationNo") reservationNo: String): GuestDetails

    @GET("reservation/{reservationNo}/changeroom")
    suspend fun changeRoomOfReservation(@Path("reservationNo") reservationNo: String): GuestDetails

}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object GuestAppApi {
    val retrofitService: GuestAppApiService by lazy { retrofit.create(GuestAppApiService::class.java) }
}