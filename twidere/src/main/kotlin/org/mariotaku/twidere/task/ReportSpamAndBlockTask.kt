package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser

/**
 * Created by mariotaku on 16/3/11.
 */
class ReportSpamAndBlockTask(context: Context) : CreateUserBlockTask(context) {

    @Throws(MicroBlogException::class)
    override fun perform(details: AccountDetails, args: Arguments): ParcelableUser {
        when (details.type) {
            AccountType.MASTODON -> {
                throw APINotSupportedException("API", details.type)
            }
            else -> {
                val twitter = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return twitter.reportSpam(args.userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
        }
    }
}
