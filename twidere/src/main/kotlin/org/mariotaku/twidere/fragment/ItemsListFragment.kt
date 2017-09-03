package org.mariotaku.twidere.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.FixedAsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.VariousItemsAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ITEMS
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.prefixedHashtag
import org.mariotaku.twidere.fragment.AbsStatusesFragment.Companion.handleActionClick
import org.mariotaku.twidere.model.ParcelableHashtag
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.MenuUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 16/3/20.
 */
open class ItemsListFragment : AbsContentListRecyclerViewFragment<VariousItemsAdapter>(),
        LoaderCallbacks<List<Any>?> {

    protected val accountKey: UserKey?
        get() = arguments.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
        loaderManager.initLoader(0, null, this)
        refreshEnabled = false
        showProgress()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AbsStatusesFragment.REQUEST_FAVORITE_SELECT_ACCOUNT,
            AbsStatusesFragment.REQUEST_RETWEET_SELECT_ACCOUNT -> {
                AbsStatusesFragment.handleActionActivityResult(this, requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): VariousItemsAdapter {
        val adapter = VariousItemsAdapter(context, requestManager)
        val dummyItemAdapter = adapter.dummyAdapter
        dummyItemAdapter.statusClickListener = object : IStatusViewHolder.StatusClickListener {
            override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
                val status = dummyItemAdapter.getStatus(position)
                IntentUtils.openStatus(getContext(), status, null)
            }

            override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
                val status = dummyItemAdapter.getStatus(position)
                IntentUtils.openStatus(getContext(), status.account_key, status.quoted_id)
            }

            override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
                val status = dummyItemAdapter.getStatus(position)
                handleActionClick(this@ItemsListFragment, id, status, holder as StatusViewHolder)
            }

            override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
                val status = dummyItemAdapter.getStatus(position)
                return AbsStatusesFragment.handleActionLongClick(this@ItemsListFragment, status,
                        adapter.getItemId(position), id)
            }

            override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
                if (activity == null) return
                val view = layoutManager.findViewByPosition(position) ?: return
                recyclerView.showContextMenuForChild(view)
            }

            override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
                val status = dummyItemAdapter.getStatus(statusPosition)
                IntentUtils.openMedia(activity, status, current, preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey],
                        null)
            }

            override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
                val activity = activity
                val status = dummyItemAdapter.getStatus(position)
                IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                        status.user_screen_name, status.extras?.user_statusnet_profile_url,
                        preferences[newDocumentApiKey])
            }
        }
        dummyItemAdapter.userClickListener = object : IUsersAdapter.SimpleUserClickListener() {
            override fun onUserClick(holder: UserViewHolder, position: Int) {
                val user = dummyItemAdapter.getUser(position) ?: return
                IntentUtils.openUserProfile(context, user, preferences[newDocumentApiKey],
                        null)
            }
        }
        adapter.hashtagClickListener = { position ->
            val hashtag = adapter.getItem(position) as ParcelableHashtag
            IntentUtils.openTweetSearch(context, accountKey, hashtag.prefixedHashtag)
        }
        return adapter
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Any>?> {
        return ItemsLoader(context, arguments)
    }

    override final fun onLoadFinished(loader: Loader<List<Any>?>, data: List<Any>?) {
        adapter.setData(data)
        showContent()
    }

    override fun onLoaderReset(loader: Loader<List<Any>?>) {
        adapter.setData(null)
    }

    override var refreshing: Boolean
        get() = false
        set(value) {
        }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val position = contextMenuInfo!!.position
        when (adapter.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(contextMenuInfo.position)
                inflater.inflate(R.menu.action_status, menu)
                MenuUtils.setupForStatus(context, menu, preferences, twitterWrapper,
                        userColorNameManager, status)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position
        when (adapter.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(position)
                if (item.itemId == R.id.share) {
                    val shareIntent = Utils.createStatusShareIntent(activity, status)
                    val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                    startActivity(chooser)
                    return true
                }
                return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                        preferences, userColorNameManager, twitterWrapper, status, item)
            }
        }
        return false
    }

    class ItemsLoader(context: Context, private val arguments: Bundle) : FixedAsyncTaskLoader<List<Any>>(context) {

        override fun loadInBackground(): List<Any> {
            return arguments.getParcelableArrayList<Parcelable>(EXTRA_ITEMS)
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}
