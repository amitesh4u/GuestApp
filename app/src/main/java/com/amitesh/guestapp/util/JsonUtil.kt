package com.amitesh.guestapp.util

import com.amitesh.guestapp.domainobject.SmartKeyUnlockDoorRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

fun smartKeyUnlockDoorRequestObjToJson(smartKeyUnlockDoorRequest: SmartKeyUnlockDoorRequest): String {
    val jsonAdapter = moshi.adapter(SmartKeyUnlockDoorRequest::class.java)
    return jsonAdapter.toJson(smartKeyUnlockDoorRequest)
}