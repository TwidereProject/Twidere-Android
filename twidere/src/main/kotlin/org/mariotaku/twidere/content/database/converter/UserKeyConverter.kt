package org.mariotaku.twidere.content.database.converter

import android.arch.persistence.room.TypeConverter
import org.mariotaku.twidere.model.UserKey

class UserKeyConverter {

    @TypeConverter
    fun parse(str: String): UserKey {
        return UserKey.valueOf(str)
    }

    @TypeConverter
    fun serialize(key: UserKey): String {
        return key.toString()
    }

}
