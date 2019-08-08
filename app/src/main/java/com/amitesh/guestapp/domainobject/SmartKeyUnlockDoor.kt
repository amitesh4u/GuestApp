package com.amitesh.guestapp.domainobject

data class SmartKeyUnlockDoor(
    val reservationNo: String,
    val roomNo: String,
    val reqId: String,
    val reqTime: Long
)