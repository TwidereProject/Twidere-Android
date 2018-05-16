package org.mariotaku.twidere.content.database.converter

import android.arch.persistence.room.TypeConverter
import org.mariotaku.twidere.extension.lang.threadLocal
import java.text.SimpleDateFormat
import java.util.*

class LocalDateConverter {

    @TypeConverter
    fun parse(time: String): Date {
        return simpleDateFormat.parse(time)
    }

    @TypeConverter
    fun serialize(date: Date): String {
        return simpleDateFormat.format(date)
    }

    companion object {
        val simpleDateFormat by threadLocal {
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        }
    }
}