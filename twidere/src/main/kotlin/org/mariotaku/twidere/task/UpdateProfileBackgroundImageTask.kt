package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.ProfileUpdatedEvent
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.DebugLog
import java.io.IOException

/**
 * Created by mariotaku on 16/3/11.
 */
open class UpdateProfileBackgroundImageTask<ResultHandler>(
        context: Context,
        accountKey: UserKey,
        private val imageUri: Uri,
        private val tile: Boolean,
        private val deleteImage: Boolean
) : AbsAccountRequestTask<Any?, ParcelableUser, ResultHandler>(context, accountKey) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableUser {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        try {
            UpdateStatusTask.getBodyFromMedia(context, imageUri, ParcelableMedia.Type.IMAGE,
                isDeleteAlways = true,
                isDeleteOnSuccess = true,
                sizeLimit = null,
                chucked = false,
                readListener = null
            ).use {
                microBlog.updateProfileBackgroundImage(it.body, tile)
            }
        } catch (e: IOException) {
            throw MicroBlogException(e)
        }
        // Wait for 5 seconds, see
        // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
        try {
            Thread.sleep(5000L)
        } catch (e: InterruptedException) {
            DebugLog.w(LOGTAG, tr = e)
        }
        val user = microBlog.verifyCredentials()
        return user.toParcelable(account, profileImageSize = profileImageSize)
    }

    override fun onSucceed(callback: ResultHandler?, result: ParcelableUser) {
        Toast.makeText(context, R.string.message_toast_profile_background_image_updated,
                Toast.LENGTH_SHORT).show()
        bus.post(ProfileUpdatedEvent(result))
    }


}
