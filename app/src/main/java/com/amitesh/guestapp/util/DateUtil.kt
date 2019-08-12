package com.amitesh.guestapp.util

import java.text.SimpleDateFormat
import java.util.*

/*
* EEEE - Display the long letter version of the weekday
* MMM - Display the letter abbreviation of the nmotny
* dd  - day in month and full year numerically
* hh:mm a- Hours and minutes in 12hr format
* HH:mm - Hour in 24 hour format
*/
private const val dateFormat = "EEEE MMM dd, hh:mm a"
private val sdFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)

fun convertLongToDateString(timeInMilliSec: Long): String {
    if (timeInMilliSec == 0L) return ""
    return sdFormat.format(timeInMilliSec).toString()
}