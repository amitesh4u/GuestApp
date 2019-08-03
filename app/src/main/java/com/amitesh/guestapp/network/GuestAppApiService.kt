package com.amitesh.guestapp.network

import com.amitesh.guestapp.domainobject.GuestDetails
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "http://192.168.0.104:8080/pms/service/v1/"

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
    .build()


/**
 * A public interface that exposes the methods
 */
interface GuestAppApiService {

    @GET("profile/{profileId}/reservation/all")
    suspend fun allReservations(@Path("profileId") profileId: String): List<GuestDetails>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object GuestAppApi {
    val retrofitService: GuestAppApiService by lazy { retrofit.create(GuestAppApiService::class.java) }
}