package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.MediaTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 14/11/5.
 */
class UserMediaTimelineFragment : AbsMediaStatusesFragment() {

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return MediaTimelineLoader(context, accountKey, userKey, screenName, sinceId, maxId,
                adapter.getData(), null, tabPosition, fromUser, loadingMore)
    }


    override fun getStatuses(maxId: String?, sinceId: String?): Int {
        if (context == null) return -1
        val args = Bundle(arguments)
        args.putBoolean(EXTRA_MAKE_GAP, false)
        args.putString(EXTRA_MAX_ID, maxId)
        args.putString(EXTRA_SINCE_ID, sinceId)
        args.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.restartLoader(loaderId, args, this)
        return 0
    }

}
