package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.TwitterWrapper
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.IOException
import javax.inject.Inject

/**
 * Created by mariotaku on 16/3/11.
 */
open class UpdateProfileBackgroundImageTask<ResultHandler>(
        private val context: Context,
        private val accountKey: UserKey,
        private val imageUri: Uri,
        private val tile: Boolean,
        private val deleteImage: Boolean
) : AbstractTask<Any?, SingleResponse<ParcelableUser>, ResultHandler>() {

    @Inject
    lateinit var bus: Bus

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as UpdateProfileBackgroundImageTask<Any>)
    }

    override fun afterExecute(handler: ResultHandler?, result: SingleResponse<ParcelableUser>) {
        super.afterExecute(handler, result)
        if (result.hasData()) {
            Utils.showOkMessage(context, R.string.profile_banner_image_updated, false)
            bus.post(ProfileUpdatedEvent(result.data!!))
        } else {
            Utils.showErrorMessage(context, R.string.action_updating_profile_background_image,
                    result.exception, true)
        }
    }

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableUser> {
        try {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey)!!
            TwitterWrapper.updateProfileBackgroundImage(context, twitter, imageUri, tile,
                    deleteImage)
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
