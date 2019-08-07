package com.amitesh.guestapp.domainobject

data class SmartKeyUnlockDoorRequest(
    val reservationNo: String,
    val roomNo: String,
    val reqId: String,
    val reqTime: Long
)