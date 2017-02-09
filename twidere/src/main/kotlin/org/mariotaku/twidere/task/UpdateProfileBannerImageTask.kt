package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import android.util.Log
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.ProfileUpdatedEvent
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.TwitterWrapper
import org.mariotaku.twidere.util.Utils
import java.io.IOException

/**
 * Created by mariotaku on 16/3/11.
 */
open class UpdateProfileBannerImageTask<ResultHandler>(
        context: Context,
        private val accountKey: UserKey,
        private val imageUri: Uri,
        private val deleteImage: Boolean
) : BaseAbstractTask<Any?, SingleResponse<ParcelableUser>, ResultHandler>(context) {

    override fun afterExecute(handler: ResultHandler?, result: SingleResponse<ParcelableUser>?) {
        super.afterExecute(handler, result)
        if (result!!.hasData()) {
            Utils.showOkMessage(context, R.string.message_toast_profile_banner_image_updated, false)
            bus.post(ProfileUpdatedEvent(result.data!!))
        } else {
            Utils.showErrorMessage(context, R.string.action_updating_profile_banner_image, result.exception,
                    true)
        }
    }

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableUser> {
        try {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey)!!
            TwitterWrapper.updateProfileBannerImage(context, twitter, imageUri, deleteImage)
            // Wait for 5 seconds, see
            // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
            try {
                Thread.sleep(5000L)
            } catch (e: InterruptedException) {
                Log.w(LOGTAG, e)
            }

            val user = twitter.verifyCredentials()
            return SingleResponse(ParcelableUserUtils.fromUser(user, accountKey))
        } catch (e: MicroBlogException) {
            return SingleResponse(exception = e)
        } catch (e: IOException) {
            return SingleResponse(exception = e)
        }
    }


}
