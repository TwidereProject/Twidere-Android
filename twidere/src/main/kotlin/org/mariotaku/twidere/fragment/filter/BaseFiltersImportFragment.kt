package org.mariotaku.twidere.fragment.filter

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.util.ArrayMap
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_COUNT
import org.mariotaku.twidere.fragment.AbsContentListRecyclerViewFragment
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.MessageDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.support.ViewSupport
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder
import java.lang.ref.WeakReference

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
        setHasOptionsMenu(true)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_import, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val checkedCount = adapter.checkedCount
        val userCount = adapter.userCount
        menu.setItemAvailability(R.id.select_none, checkedCount > 0)
        menu.setItemAvailability(R.id.select_all, checkedCount < userCount)
        menu.setItemAvailability(R.id.invert_selection, checkedCount > 0 && checkedCount < userCount)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_none -> {
                adapter.clearSelection()
                adapter.notifyDataSetChanged()
            }
            R.id.select_all -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount - 1)) {
                    adapter.setItemChecked(idx, true)
                }
                adapter.notifyDataSetChanged()
            }
            R.id.invert_selection -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount - 1)) {
                    adapter.setItemChecked(idx, !adapter.isItemChecked(idx))
                }
                adapter.notifyDataSetChanged()
            }
            R.id.perform_import -> {
                if (adapter.checkedCount == 0) {
                    Toast.makeText(context, R.string.message_toast_no_user_selected, Toast.LENGTH_SHORT).show()
                    return true
                }
                val df = ImportConfirmDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_COUNT] = adapter.checkedCount
                }
                df.show(childFragmentManager, "import_confirm")
            }
            else -> {
                return false
            }
        }
        return true
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
        val hasMoreData = run {
            val previousCount = adapter.data?.size
            if (previousCount != data?.size) return@run true
            val previousFirst = adapter.data?.firstOrNull()
            val previousLast = adapter.data?.lastOrNull()
            // If first and last data not changed, assume no more data
            return@run previousFirst != data?.firstOrNull() && previousLast != data?.lastOrNull()
        }
        adapter.data = data
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData) {
                ILoadMoreSupportAdapter.END
            } else {
                ILoadMoreSupportAdapter.NONE
            }
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        showContent()
        refreshEnabled = data.isNullOrEmpty()
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        val cursorLoader = loader as CursorSupportUsersLoader
        nextCursor = cursorLoader.nextCursor
        prevCursor = cursorLoader.prevCursor
        nextPage = cursorLoader.nextPage
        activity.supportInvalidateOptionsMenu()
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

    override fun onCreateAdapter(context: Context): SelectableUsersAdapter {
        val adapter = SelectableUsersAdapter(context)
        adapter.itemCheckedListener = { position, checked ->
            val count = adapter.checkedCount
            val actionBar = (activity as BaseActivity).supportActionBar
            actionBar?.subtitle = if (count > 0) {
                resources.getQuantityString(R.plurals.Nitems_selected, count, count)
            } else {
                null
            }
            activity.supportInvalidateOptionsMenu()
        }
        return adapter
    }

    protected abstract fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableUser>?>

    private fun performImport(filterEverywhere: Boolean) {
        val selectedUsers = rangeOfSize(adapter.userStartIndex, adapter.userCount - 1)
                .filter { adapter.isItemChecked(it) }
                .map { adapter.getUser(it)!! }
        selectedUsers.forEach { it.is_filtered = true }
        val weakDf = WeakReference(ProgressDialogFragment.show(childFragmentManager, "import_progress"))
        val weakThis = WeakReference(this)
        task {
            val context = weakThis.get()?.context ?: return@task
            DataStoreUtils.addToFilter(context, selectedUsers, filterEverywhere)
        }.alwaysUi {
            executeAfterFragmentResumed(true) {
                val fm = weakThis.get()?.fragmentManager ?: return@executeAfterFragmentResumed
                val df = weakDf.get() ?: fm.findFragmentByTag("import_progress") as? DialogFragment
                df?.dismiss()
            }
            weakThis.get()?.adapter?.notifyDataSetChanged()
        }
    }

    class ImportConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val filterEverywhere = ((dialog as Dialog).findViewById(R.id.filterEverywhereToggle) as CheckBox).isChecked
                    (parentFragment as BaseFiltersImportFragment).performImport(filterEverywhere)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.add_to_filter)
            builder.setView(R.layout.dialog_block_mute_filter_user_confirm)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                val confirmMessageView = dialog.findViewById(R.id.confirmMessage) as TextView
                val filterEverywhereHelp = dialog.findViewById(R.id.filterEverywhereHelp)!!
                filterEverywhereHelp.setOnClickListener {
                    MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                            message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
                }
                val usersCount = arguments.getInt(EXTRA_COUNT)
                val nUsers = resources.getQuantityString(R.plurals.N_users, usersCount, usersCount)
                confirmMessageView.text = getString(R.string.filter_user_confirm_message, nUsers)
            }
            return dialog
        }
    }


    class SelectableUsersAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), IContentAdapter {

        val ITEM_VIEW_TYPE_USER = 2

        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val itemStates: MutableMap<UserKey, Boolean> = ArrayMap()
        var itemCheckedListener: ((Int, Boolean) -> Unit)? = null

        var data: List<ParcelableUser>? = null
            set(value) {
                field = value
                value?.forEach { item ->
                    if (item.key !in itemStates && item.is_filtered) {
                        itemStates[item.key] = true
                    }
                }
                notifyDataSetChanged()
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

        fun getUserKey(position: Int): UserKey {
            return data!![position].key
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
                    val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
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

        val checkedCount: Int get() {
            return data?.count { !it.is_filtered && itemStates[it.key] ?: false } ?: 0
        }

        fun setItemChecked(position: Int, value: Boolean) {
            val userKey = getUserKey(position)
            itemStates[userKey] = value
            itemCheckedListener?.invoke(position, value)
        }

        fun isItemChecked(position: Int): Boolean {
            return itemStates[getUserKey(position)] ?: false
        }

        fun clearSelection() {
            itemStates.clear()
        }
    }

    internal class SelectableUserViewHolder(
            itemView: View,
            adapter: SelectableUsersAdapter
    ) : SimpleUserViewHolder(itemView, adapter) {
        val checkChangedListener: CompoundButton.OnCheckedChangeListener

        init {
            ViewSupport.setBackground(itemView, ThemeUtils.getSelectableItemBackgroundDrawable(itemView.context))
            checkBox.visibility = View.VISIBLE
            checkChangedListener = CompoundButton.OnCheckedChangeListener { view, value ->
                adapter.setItemChecked(layoutPosition, value)
            }
            itemView.setOnClickListener {
                checkBox.toggle()
            }
        }

        override fun displayUser(user: ParcelableUser) {
            super.displayUser(user)
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = (adapter as SelectableUsersAdapter).isItemChecked(layoutPosition)
            checkBox.setOnCheckedChangeListener(checkChangedListener)
            itemView.isEnabled = !user.is_filtered
            checkBox.isEnabled = !user.is_filtered
        }

    }
}
