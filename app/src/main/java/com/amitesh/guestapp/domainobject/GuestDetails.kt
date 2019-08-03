package com.amitesh.guestapp.domainobject

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuestDetails(
    val profileId: String,
    val reservationNo: String,
    val rezStatus: String,
    val roomNo: String?,
    val checkedInAt: Long?,
    val checkedOutAt: Long?
) : Parcelable