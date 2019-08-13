package com.amitesh.guestapp.network

import com.amitesh.guestapp.domainobject.GuestDetails
import com.amitesh.guestapp.domainobject.SmartKeyUnlockDoor
import com.amitesh.guestapp.util.moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


enum class ApiStatus { LOADING, ERROR, DONE }
enum class ReservationStatus { NONE, ARRIVING, INHOUSE }

const val PROFILE_ID = "sg0300747"

//private const val BASE_URL = "http://10.135.194.224:8080/pms/service/v1/"
//private const val BASE_URL = "http://192.168.0.103:8080/pms/service/v1/"
private const val BASE_URL = "http://amitesh4u.com/pms/service/v1/"

private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
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

    @GET("reservation/{reservationNo}/smartkeyusage")
    suspend fun getSmartKeyUsage(@Path("reservationNo") reservationNo: String): List<SmartKeyUnlockDoor>

}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object GuestAppApi {
    val retrofitService: GuestAppApiService by lazy { retrofit.create(GuestAppApiService::class.java) }
}