package org.mariotaku.twidere.content.database.converter

import android.arch.persistence.room.TypeConverter
import java.util.*

class TimestampDateTimeConverter {

    @TypeConverter
    fun parse(time: Long): Date {
        return Date(time)
    }

    @TypeConverter
    fun serialize(date: Date): Long {
        return date.time
    }
}