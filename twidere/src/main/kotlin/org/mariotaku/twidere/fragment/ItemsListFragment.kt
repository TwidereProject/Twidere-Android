package org.mariotaku.twidere.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaEvent
import edu.tsinghua.hotmobi.model.TimelineType
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.VariousItemsAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ITEMS
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.MenuUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 16/3/20.
 */
class ItemsListFragment : AbsContentListRecyclerViewFragment<VariousItemsAdapter>(), LoaderCallbacks<List<*>> {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
        loaderManager.initLoader(0, null, this)
        refreshEnabled = false
        showContent()
    }

    override fun onCreateAdapter(context: Context): VariousItemsAdapter {
        val adapter = VariousItemsAdapter(context)
        val dummyItemAdapter = adapter.dummyAdapter
        dummyItemAdapter.statusClickListener = object : IStatusViewHolder.SimpleStatusClickListener() {
            override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
                val status = dummyItemAdapter.getStatus(position) ?: return
                IntentUtils.openStatus(getContext(), status, null)
            }

            override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
                val status = dummyItemAdapter.getStatus(position) ?: return
                AbsStatusesFragment.handleStatusActionClick(context, fragmentManager,
                        twitterWrapper, holder as StatusViewHolder, status, id)
            }

            override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
                if (activity == null) return
                val view = layoutManager!!.findViewByPosition(position) ?: return
                recyclerView.showContextMenuForChild(view)
            }

            override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {
                val status = dummyItemAdapter.getStatus(statusPosition) ?: return
                IntentUtils.openMedia(activity, status, media, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API))
                // BEGIN HotMobi
                val event = MediaEvent.create(activity, status, media,
                        TimelineType.OTHER, dummyItemAdapter.mediaPreviewEnabled)
                HotMobiLogger.getInstance(activity).log(status.account_key, event)
                // END HotMobi
            }

            override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
                val activity = activity
                val status = dummyItemAdapter.getStatus(position) ?: return
                IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                        status.user_screen_name, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.TIMELINE_STATUS)
            }
        }
        dummyItemAdapter.userClickListener = object : IUsersAdapter.SimpleUserClickListener() {
            override fun onUserClick(holder: UserViewHolder, position: Int) {
                val user = dummyItemAdapter.getUser(position) ?: return
                IntentUtils.openUserProfile(getContext(), user, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.TIMELINE_STATUS)
            }
        }
        return adapter
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<*>?> {
        return ItemsLoader(context, arguments)
    }

    override fun onLoadFinished(loader: Loader<List<*>?>, data: List<*>?) {
        adapter!!.setData(data)
    }

    override fun onLoaderReset(loader: Loader<List<*>?>) {
        adapter!!.setData(null)
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
        val adapter = adapter
        when (adapter!!.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(contextMenuInfo.position) ?: return
                inflater.inflate(R.menu.action_status, menu)
                MenuUtils.setupForStatus(context, preferences, menu, status,
                        twitterWrapper)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item!!.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position
        val adapter = adapter
        when (adapter!!.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(position) ?: return false
                if (item.itemId == R.id.share) {
                    val shareIntent = Utils.createStatusShareIntent(activity, status)
                    val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                    Utils.addCopyLinkIntent(context, chooser, LinkCreator.getStatusWebLink(status))
                    startActivity(chooser)
                    return true
                }
                return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                        userColorNameManager, twitterWrapper, status, item)
            }
        }
        return false
    }

    class ItemsLoader(context: Context, private val arguments: Bundle) : AsyncTaskLoader<List<*>>(context) {

        override fun loadInBackground(): List<*> {
            return arguments.getParcelableArrayList<Parcelable>(EXTRA_ITEMS)
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}
