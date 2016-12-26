package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IContentCardAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.fragment.AbsContentListRecyclerViewFragment
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder

/**
 * Created by mariotaku on 2016/12/26.
 */

abstract class BaseFiltersImportFragment : AbsContentListRecyclerViewFragment<BaseFiltersImportFragment.SelectableUsersAdapter>(),
        LoaderManager.LoaderCallbacks<List<ParcelableUser>?> {

    protected var nextCursor: Long = -1
        private set
    protected var prevCursor: Long = -1
        private set
    protected var nextPage = 1
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableUser>?> {
        val fromUser = args.getBoolean(IntentConstants.EXTRA_FROM_USER)
        args.remove(IntentConstants.EXTRA_FROM_USER)
        return onCreateUsersLoader(context, args, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        adapter.data = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        adapter.data = data
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(data)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        showContent()
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        val cursorLoader = loader as CursorSupportUsersLoader
        nextCursor = cursorLoader.nextCursor
        prevCursor = cursorLoader.prevCursor
        nextPage = cursorLoader.nextPage
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderArgs.putLong(IntentConstants.EXTRA_NEXT_CURSOR, nextCursor)
        loaderArgs.putInt(IntentConstants.EXTRA_PAGE, nextPage)
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filters_import, container, false)
    }

    override fun onCreateAdapter(context: Context): SelectableUsersAdapter {
        return SelectableUsersAdapter(context)
    }

    protected abstract fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableUser>?>

    protected open fun hasMoreData(data: List<ParcelableUser>?): Boolean {
        return data == null || !data.isEmpty()
    }

    class SelectableUsersAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), IContentCardAdapter {
        override val isShowAbsoluteTime: Boolean
        override val profileImageEnabled: Boolean
        override val profileImageStyle: Int
        override val textSize: Float

        val ITEM_VIEW_TYPE_USER = 2

        private val inflater: LayoutInflater

        var data: List<ParcelableUser>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }


        init {
            inflater = LayoutInflater.from(context)
            isShowAbsoluteTime = preferences[showAbsoluteTimeKey]
            profileImageEnabled = preferences[displayProfileImageKey]
            profileImageStyle = preferences[profileImageStyleKey]
            textSize = preferences[textSizeKey].toFloat()
        }

        private fun bindUser(holder: SelectableUserViewHolder, position: Int) {
            holder.displayUser(getUser(position)!!)
        }

        override fun getItemCount(): Int {
            val position = loadMoreIndicatorPosition
            var count = userCount
            if (position and ILoadMoreSupportAdapter.START !== 0L) {
                count++
            }
            if (position and ILoadMoreSupportAdapter.END !== 0L) {
                count++
            }
            return count
        }

        fun getUser(position: Int): ParcelableUser? {
            val dataPosition = position - userStartIndex
            if (dataPosition < 0 || dataPosition >= userCount) return null
            return data!![dataPosition]
        }

        val userStartIndex: Int
            get() {
                val position = loadMoreIndicatorPosition
                var start = 0
                if (position and ILoadMoreSupportAdapter.START !== 0L) {
                    start += 1
                }
                return start
            }

        fun getUserId(position: Int): String? {
            if (position == userCount) return null
            return data!![position].key.id
        }

        val userCount: Int
            get() {
                if (data == null) return 0
                return data!!.size
            }

        fun removeUserAt(position: Int): Boolean {
            val data = this.data as? MutableList ?: return false
            val dataPosition = position - userStartIndex
            if (dataPosition < 0 || dataPosition >= userCount) return false
            data.removeAt(dataPosition)
            notifyItemRemoved(position)
            return true
        }

        fun setUserAt(position: Int, user: ParcelableUser): Boolean {
            val data = this.data as? MutableList ?: return false
            val dataPosition = position - userStartIndex
            if (dataPosition < 0 || dataPosition >= userCount) return false
            data[dataPosition] = user
            notifyItemChanged(position)
            return true
        }

        fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
            if (data == null) return RecyclerView.NO_POSITION
            for (i in userStartIndex until userStartIndex + userCount) {
                val user = data!![i]
                if (accountKey == user.account_key && userKey == user.key) {
                    return i
                }
            }
            return RecyclerView.NO_POSITION
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                ITEM_VIEW_TYPE_USER -> {
                    val view = inflater.inflate(R.layout.list_item_simple_user, parent, false)
                    val holder = SelectableUserViewHolder(view, this)
                    return holder
                }
                ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                    val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
                    return LoadIndicatorViewHolder(view)
                }
            }
            throw IllegalStateException("Unknown view type " + viewType)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ITEM_VIEW_TYPE_USER -> {
                    bindUser(holder as SelectableUserViewHolder, position)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
                return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            if (position == userCount) {
                return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            return ITEM_VIEW_TYPE_USER
        }
    }


    class SelectableUserViewHolder(
            itemView: View,
            adapter: IContentCardAdapter
    ) : SimpleUserViewHolder(itemView, adapter) {
        override fun displayUser(user: ParcelableUser) {
            super.displayUser(user)
        }

    }
}
