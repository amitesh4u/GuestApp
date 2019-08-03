package com.amitesh.guestapp.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

class DateJsonAdapter : JsonAdapter<Date>() {

    private val dateFormat = "yyyy/MM/dd HH:mm:ss"
    private val sdFormat = SimpleDateFormat(dateFormat, Locale.JAPAN)

    @Synchronized
    @Throws(Exception::class)
    override fun fromJson(reader: JsonReader): Date {
        val string = reader.nextString()
        return sdFormat.parse(string)
    }

    @Synchronized
    @Throws(Exception::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        writer.value(sdFormat.format(value))
    }

}