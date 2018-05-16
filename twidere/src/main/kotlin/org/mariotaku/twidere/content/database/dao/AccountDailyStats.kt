package org.mariotaku.twidere.content.database.dao

import android.arch.persistence.room.*
import org.mariotaku.twidere.content.database.converter.LocalDateConverter
import org.mariotaku.twidere.content.model.AccountStats
import org.mariotaku.twidere.extension.julianDay
import org.mariotaku.twidere.extension.time
import org.mariotaku.twidere.model.UserKey
import java.util.*
import kotlin.math.sign

@Dao
abstract class AccountDailyStats {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(stats: AccountStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(stats: Collection<AccountStats>)

    @Query("SELECT * FROM `account_daily_stats` WHERE `createdAt` = :date")
    abstract fun list(@TypeConverters(LocalDateConverter::class) date: Date): List<AccountStats>

    @Query("SELECT * FROM `account_daily_stats` WHERE `accountKey` = :accountKey AND `createdAt` BETWEEN :since AND :until ORDER BY `createdAt`")
    abstract fun list(accountKey: UserKey, @TypeConverters(LocalDateConverter::class) since: Date,
            @TypeConverters(LocalDateConverter::class) until: Date): List<AccountStats>

    @Query("SELECT * FROM `account_daily_stats` WHERE `accountKey` = :accountKey AND `createdAt` >= :since ORDER BY `createdAt` LIMIT 1")
    abstract fun firstSince(accountKey: UserKey, @TypeConverters(LocalDateConverter::class) since: Date): AccountStats?

    fun listSparse(accountKey: UserKey, @TypeConverters(LocalDateConverter::class) since: Date,
            @TypeConverters(LocalDateConverter::class) until: Date): Array<AccountStats?> {
        val sinceJulianDay = Calendar.getInstance().time(since).julianDay()
        val count = Calendar.getInstance().time(until).julianDay() - sinceJulianDay + 1
        val result = arrayOfNulls<AccountStats>(count)
        val tempCal = Calendar.getInstance()
        list(accountKey, since, until).forEach {
            tempCal.time = it.createdAt
            val index = tempCal.julianDay() - sinceJulianDay
            if (index in result.indices) {
                result[index] = it
            }
        }
        return result
    }

    fun monthlySummary(accountKey: UserKey, date: Date): AccountStats.Summaries {
        val since = Calendar.getInstance().apply {
            time = date
            add(Calendar.DATE, -27)
        }.time

        val prevPeriodFirst = firstSince(accountKey, Calendar.getInstance().apply {
            time = date
            add(Calendar.DATE, -55)
        }.time)

        val currentPeriodStats = listSparse(accountKey, since, date)

        val statuses = currentPeriodStats.statusesSummary(prevPeriodFirst)

        val followers = currentPeriodStats.followersSummary()

        return AccountStats.Summaries(statuses, followers)
    }

    private fun Array<AccountStats?>.statusesSummary(prevPeriodFirst: AccountStats?): AccountStats.DisplaySummary {
        val firstNumber = numberAt(0, AccountStats::statusesCount)
        val lastNumber = numberAt(lastIndex, AccountStats::statusesCount)

        var growthText: String? = null
        var growthSign = 0
        if (prevPeriodFirst != null) {
            val prevPeriodCount = firstNumber - prevPeriodFirst.statusesCount
            val currPeriodCount = lastNumber - firstNumber

            val growthPercent = currPeriodCount / prevPeriodCount.toFloat() - 1
            growthText = String.format(Locale.US, "%.1f%%", Math.abs(growthPercent * 100))
            growthSign = growthPercent.sign.toInt()
        }

        val firstNonNullIndex = indexOfFirst { it != null }
        if (firstNonNullIndex < 0) throw DataNotReadyException()

        val diffs = LongArray(size - firstNonNullIndex - 1) item@{ index ->
            val statIndex = index + firstNonNullIndex + 1
            return@item numberAt(statIndex, AccountStats::statusesCount) - numberAt(statIndex - 1,
                    AccountStats::statusesCount)
        }
        var positiveRange = diffs.max()!!
        if (positiveRange <= 0L) {
            positiveRange = Math.abs(diffs.min()!!)
        }
        if (positiveRange <= 0L) {
            positiveRange = 10
        }

        val values = FloatArray(diffs.size) item@{ index ->
            return@item diffs[index] / positiveRange.toFloat()
        }

        val periodSum = lastNumber - firstNumber
        return AccountStats.DisplaySummary(periodSum, growthSign, growthText, size, values)
    }

    private fun Array<AccountStats?>.followersSummary(): AccountStats.DisplaySummary {
        val maxNumber = maxBy { it?.followersCount ?: Long.MIN_VALUE }!!.followersCount

        val firstNonNullIndex = indexOfFirst { it != null }
        if (firstNonNullIndex < 0) throw DataNotReadyException()

        val firstNumber = numberAt(firstNonNullIndex, AccountStats::followersCount)
        val lastNumber = numberAt(lastIndex, AccountStats::followersCount)

        var positiveRange = maxNumber - firstNumber
        if (positiveRange <= 0L) {
            positiveRange = 10
        }

        val values = (firstNonNullIndex..lastIndex).map { index ->
            val number = numberAt(index, AccountStats::followersCount)
            return@map (number - firstNumber) / positiveRange.toFloat()
        }.toFloatArray()

        val growth = lastNumber - firstNumber
        return AccountStats.DisplaySummary(firstNumber, growth.sign, Math.abs(growth).toString(),
                size, values)
    }

    private fun Array<AccountStats?>.numberAt(index: Int, selector: (AccountStats) -> Long): Long {
        val itemAt = this[index]
        if (itemAt != null) return selector(itemAt)
        val firstNonNullIndex = indexOfFirst { it != null }
        val lastNonNullIndex = indexOfLast { it != null }

        if (firstNonNullIndex < 0 || lastNonNullIndex < 0 || firstNonNullIndex == lastNonNullIndex) {
            throw DataNotReadyException()
        }

        val count = lastNonNullIndex - firstNonNullIndex
        val firstNonNullNumber = selector(this[firstNonNullIndex]!!)
        val lastNonNullNumber = selector(this[lastNonNullIndex]!!)
        val delta = (lastNonNullNumber - firstNonNullNumber) / count
        when {
            index < firstNonNullIndex -> return firstNonNullNumber - delta * (firstNonNullIndex - index)
            index > lastNonNullIndex -> return lastNonNullNumber + delta * (index - lastNonNullIndex)
            else -> {
                val startIndex = (0 until index).last { this[it] != null }
                val endIndex = (index + 1..lastIndex).first { this[it] != null }
                val start = selector(this[startIndex]!!)
                val end = selector(this[endIndex]!!)
                val delta2 = (end - start) / (endIndex - startIndex)
                return start + delta2 * (index - startIndex)
            }
        }
    }

    class DataNotReadyException : Exception()

}
