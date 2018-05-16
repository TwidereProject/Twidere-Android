package org.mariotaku.twidere.content.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.mariotaku.twidere.content.database.converter.LocalDateConverter
import org.mariotaku.twidere.model.UserKey
import java.util.*

@Entity(tableName = "account_daily_stats",
        indices = [Index("createdAt", "accountKey", unique = true)])
@TypeConverters(LocalDateConverter::class)
data class AccountStats(
        @PrimaryKey(autoGenerate = true)
        val rowId: Long = 0,
        val createdAt: Date,
        val accountKey: UserKey,
        val statusesCount: Long,
        val followersCount: Long
) {


    data class Summaries(
            val statuses: DisplaySummary,
            val followers: DisplaySummary
    )

    data class DisplaySummary(
            val number: Long,
            val growthSign: Int,
            val growth: String?,
            val valuesCount: Int,
            val values: FloatArray
    ) {
        val numberDisplay: String
            get() = number.toString()

        val growthDisplay: String?
            get() = when {
                growthSign < 0 -> "-$growth"
                growthSign > 0 -> "+$growth"
                else -> growth
            }
    }

}
