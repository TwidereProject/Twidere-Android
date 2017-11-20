package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.AccountUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.ProfileUpdatedEvent
import org.mariotaku.twidere.promise.UpdateStatusPromise
import org.mariotaku.twidere.util.DebugLog
import java.io.IOException

/**
 * Created by mariotaku on 2016/12/9.
 */
open class UpdateProfileImageTask<ResultHandler>(
        context: Context,
        accountKey: UserKey,
        private val imageUri: Uri,
        private val deleteImage: Boolean
) : AbsAccountRequestTask<Any?, ParcelableUser, ResultHandler>(context, accountKey) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableUser {
        try {
            return UpdateStatusPromise.getBodyFromMedia(context, imageUri, ParcelableMedia.Type.IMAGE,
                    deleteImage, false, null, false, null).use {
                when (account.type) {
                    AccountType.MASTODON -> {
                        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                        return@use mastodon.updateCredentials(AccountUpdate().avatar(it.body))
                                .toParcelable(account)
                    }
                    else -> {
                        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                        microBlog.updateProfileImage(it.body)
                        // Wait for 5 seconds, see
                        // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                        Thread.sleep(5000L)
                        return@use microBlog.verifyCredentials().toParcelable(account,
                                profileImageSize = profileImageSize)
                    }
                }
            }
        } catch (e: IOException) {
            throw MicroBlogException(e)
        } catch (e: InterruptedException) {
            DebugLog.w(tr = e)
            throw MicroBlogException(e)
        }
    }

    override fun onSucceed(callback: ResultHandler?, result: ParcelableUser) {
        Toast.makeText(context, R.string.message_toast_profile_image_updated, Toast.LENGTH_SHORT)
                .show()
        bus.post(ProfileUpdatedEvent(result))
    }

}
