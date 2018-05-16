package org.mariotaku.twidere.task.worker

import android.accounts.AccountManager
import androidx.work.Worker
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.content.database.TwidereDatabase
import org.mariotaku.twidere.content.database.dao.AccountDailyStats
import org.mariotaku.twidere.content.model.AccountStats
import org.mariotaku.twidere.extension.getAllDetails
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import java.text.SimpleDateFormat
import java.util.*

class AccountDailyStatWorker : Worker() {

    override fun doWork(): WorkerResult {
        val am = AccountManager.get(applicationContext)
        val dao = TwidereDatabase.get(applicationContext).accountDailyStats()
        val date = Date()
        val existingStats = dao.list(date)
        val accounts = am.getAllDetails(true).filterNot { account ->
            existingStats.any { it.accountKey == account.key }
        }
        val stats = accounts.mapNotNull {
            val user = try {
                when (it.type) {
                    AccountType.MASTODON -> it.newMicroBlogInstance(applicationContext, Mastodon::class.java)
                            .verifyCredentials().toParcelable(it.key)
                    else -> it.newMicroBlogInstance(applicationContext, MicroBlog::class.java)
                            .verifyCredentials().toParcelable(it.key, it.type)
                }
            } catch (e: MicroBlogException) {
                return@mapNotNull null
            }
            return@mapNotNull AccountStats(createdAt = date, accountKey = it.key,
                    statusesCount = user.statuses_count, followersCount = user.followers_count)
        }
        dao.insert(stats)
        return WorkerResult.SUCCESS
    }

    private fun AccountDailyStats.insertTestData(stats: List<AccountStats>) {
        val cal = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2018-05-14")
        var i = 1
        var s = stats
        while (i < 90) {
            cal.time = date
            cal.add(Calendar.DATE, -i)
            s = s.map {
                it.copy(
                        rowId = 0,
                        createdAt = cal.time,
                        statusesCount = (it.statusesCount - Math.random() * 30).toLong().coerceAtLeast(0),
                        followersCount = ((it.followersCount + Math.random() * 20).toLong() - 10).coerceAtLeast(0)
                )
            }
            insert(s)
            i++
        }
    }

}
