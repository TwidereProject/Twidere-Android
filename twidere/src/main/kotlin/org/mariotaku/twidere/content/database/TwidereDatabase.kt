package org.mariotaku.twidere.content.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.mariotaku.twidere.content.database.converter.UserKeyConverter
import org.mariotaku.twidere.content.database.dao.AccountDailyStats
import org.mariotaku.twidere.content.model.AccountStats
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder

@Database(entities = [AccountStats::class], version = 2)
@TypeConverters(UserKeyConverter::class)
abstract class TwidereDatabase : RoomDatabase() {

    abstract fun accountDailyStats(): AccountDailyStats

    companion object : ApplicationContextSingletonHolder<TwidereDatabase>({
        Room.databaseBuilder(it, TwidereDatabase::class.java, "twidere.db")
                .fallbackToDestructiveMigration()
                .build()
    })

}
