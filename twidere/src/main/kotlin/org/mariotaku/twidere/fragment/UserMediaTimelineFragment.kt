package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.app.hasRunningLoadersSafe
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import org.mariotaku.twidere.adapter.StaggeredGridParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.MediaTimelineLoader
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/5.
 */
class UserMediaTimelineFragment : AbsContentRecyclerViewFragment<StaggeredGridParcelableStatusesAdapter, StaggeredGridLayoutManager>(), LoaderCallbacks<List<ParcelableStatus>>, DrawerCallback, IStatusViewHolder.StatusClickListener {


    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return loaderManager.hasRunningLoadersSafe()
        }
        set(value) {
            super.refreshing = value
        }


    override val reachingEnd: Boolean
        get() {
            val lm = layoutManager
            var visiblePos = lm.findLastCompletelyVisibleItemPositions(null)
            if (visiblePos.all { it == RecyclerView.NO_POSITION }) {
                visiblePos = lm.findLastVisibleItemPositions(null)
            }
            return visiblePos.contains(lm.itemCount - 1)
        }

    override val reachingStart: Boolean
        get() {
            val lm = layoutManager
            var visiblePos = lm.findFirstCompletelyVisibleItemPositions(null)
            if (visiblePos.all { it == RecyclerView.NO_POSITION }) {
                visiblePos = lm.findFirstVisibleItemPositions(null)
            }
            return visiblePos.contains(0)
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.statusClickListener = this
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
        showProgress()
    }

    override fun setupRecyclerView(context: Context, recyclerView: RecyclerView) {

    }

    override fun onCreateLayoutManager(context: Context): StaggeredGridLayoutManager {
        return StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    override fun scrollToPositionWithOffset(position: Int, offset: Int) {
        layoutManager.scrollToPositionWithOffset(position, offset)
    }

    override fun onCreateAdapter(context: Context): StaggeredGridParcelableStatusesAdapter {
        return StaggeredGridParcelableStatusesAdapter(context)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableStatus>> {
        val context = activity
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return MediaTimelineLoader(context, accountKey, userKey, screenName, sinceId, maxId,
                adapter.getData(), null, tabPosition, fromUser, loadingMore)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableStatus>>, data: List<ParcelableStatus>?) {
        val changed = adapter.setData(data)
        if ((loader as IExtendedLoader).fromUser && loader is MediaTimelineLoader) {
            val maxId = loader.maxId
            val sinceId = loader.sinceId
            if (TextUtils.isEmpty(sinceId) && !TextUtils.isEmpty(maxId)) {
                if (data != null && !data.isEmpty()) {
                    adapter.loadMoreSupportedPosition = if (changed) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
                }
            } else {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
            }
        }
        loader.fromUser = false
        showContent()
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableStatus>>) {
        adapter.setData(null)
    }

    override fun onLoadMoreContents(position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START != 0L) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val maxId = adapter.getStatusId(adapter.statusCount - 1)
        getStatuses(maxId, null)
    }


    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position) ?: return
        IntentUtils.openStatus(context, status, null)
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position) ?: return
        IntentUtils.openStatus(context, status.account_key, status.quoted_id)
    }

    fun getStatuses(maxId: String?, sinceId: String?): Int {
        if (context == null) return -1
        val args = Bundle(arguments)
        args.putBoolean(EXTRA_MAKE_GAP, false)
        args.putString(EXTRA_MAX_ID, maxId)
        args.putString(EXTRA_SINCE_ID, sinceId)
        args.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.restartLoader(0, args, this)
        return 0
    }

}
