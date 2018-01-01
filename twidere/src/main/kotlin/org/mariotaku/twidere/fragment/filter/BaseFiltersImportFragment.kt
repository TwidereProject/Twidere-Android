package org.mariotaku.twidere.fragment.filter

import android.app.Activity
import android.app.Dialog
import android.arch.paging.PagedList
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.dialog_block_mute_filter_user_confirm.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.ktextension.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.adapter.SelectableUsersAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_COUNT
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.util.isAdvancedFiltersEnabled
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import java.lang.ref.WeakReference

abstract class BaseFiltersImportFragment : AbsContentListRecyclerViewFragment<SelectableUsersAdapter>() {

    protected var nextPagination: Pagination? = null
        private set
    protected var prevPagination: Pagination? = null
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_import, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val checkedCount = adapter.checkedCount
        val userCount = adapter.userCount
        menu.setItemAvailability(R.id.select_none, checkedCount > 0)
        menu.setItemAvailability(R.id.select_all, checkedCount < userCount)
        menu.setItemAvailability(R.id.invert_selection, checkedCount in 1 until userCount)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_none -> {
                adapter.clearSelection()
                adapter.notifyDataSetChanged()
            }
            R.id.select_all -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount)) {
                    adapter.setItemChecked(idx, true)
                }
                adapter.notifyDataSetChanged()
            }
            R.id.invert_selection -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount)) {
                    adapter.setItemChecked(idx, !adapter.isItemChecked(idx))
                }
                adapter.notifyDataSetChanged()
            }
            R.id.perform_import -> {
                if (adapter.checkedCount == 0) {
                    Toast.makeText(context, R.string.message_toast_no_user_selected, Toast.LENGTH_SHORT).show()
                    return true
                }
                if (!extraFeaturesService.isAdvancedFiltersEnabled) {
                    ExtraFeaturesIntroductionDialogFragment.show(fragmentManager!!,
                            feature = ExtraFeaturesService.FEATURE_ADVANCED_FILTERS,
                            requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
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

    override fun onLoadMoreContents(@LoadMorePosition position: Int) {
        // No-op
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): SelectableUsersAdapter {
        val adapter = SelectableUsersAdapter(context, this.requestManager)
        adapter.itemCheckedListener = listener@ { _, _ ->
            if (!extraFeaturesService.isAdvancedFiltersEnabled) {
                ExtraFeaturesIntroductionDialogFragment.show(fragmentManager!!,
                        feature = ExtraFeaturesService.FEATURE_ADVANCED_FILTERS,
                        requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
                return@listener false
            }
            val count = adapter.checkedCount
            val actionBar = (activity as BaseActivity).supportActionBar
            actionBar?.subtitle = if (count > 0) {
                resources.getQuantityString(R.plurals.Nitems_selected, count, count)
            } else {
                null
            }
            activity!!.invalidateOptionsMenu()
            return@listener true
        }
        return adapter
    }

    protected abstract fun onCreateUsersFetcher(): UsersFetcher

    fun onDataLoaded(data: PagedList<ParcelableUser>?) {
        val hasMoreData = run {
            val previousCount = adapter.data?.size
            if (previousCount != data?.size) return@run true
            val previousFirst = adapter.data?.firstOrNull()
            val previousLast = adapter.data?.lastOrNull()
            // If first and last data not changed, assume no more data
            return@run previousFirst != data?.firstOrNull() && previousLast != data?.lastOrNull()
        }
        adapter.clearLockedState()
        data?.forEach { user ->
            if (user.is_filtered) {
                adapter.setLockedState(user.key, true)
            }
        }
        adapter.data = data
        showContent()
        refreshEnabled = data.isNullOrEmpty()
        refreshing = false
        setLoadMoreIndicatorPosition(LoadMorePosition.NONE)
        activity!!.invalidateOptionsMenu()
    }

    private fun performImport(filterEverywhere: Boolean) {
        val selectedUsers = rangeOfSize(adapter.userStartIndex, adapter.userCount)
                .filter { adapter.isItemChecked(it) }
                .mapNotNull {
                    val user = adapter.getUser(it)
                    // Skip if already filtered
                    if (user.is_filtered) return@mapNotNull null
                    return@mapNotNull user
                }
        selectedUsers.forEach { it.is_filtered = true }
        val weakThis = WeakReference(this)
        executeAfterFragmentResumed {
            ProgressDialogFragment.show(it.childFragmentManager, "import_progress")
        } and task {
            val context = weakThis.get()?.context ?: return@task
            DataStoreUtils.addToFilter(context, selectedUsers, filterEverywhere)
        }.alwaysUi {
            executeAfterFragmentResumed(true) { fragment ->
                fragment.childFragmentManager.dismissDialogFragment("import_progress")
            }
            weakThis.get()?.adapter?.notifyDataSetChanged()
        }
    }

    class ImportConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val filterEverywhere = (dialog as Dialog).findViewById<CheckBox>(R.id.filterEverywhereToggle).isChecked
                    (parentFragment as BaseFiltersImportFragment).performImport(filterEverywhere)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.action_add_to_filter)
            builder.setView(R.layout.dialog_block_mute_filter_user_confirm)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow {
                it.applyTheme()
                val confirmMessageView = it.confirmMessage
                val filterEverywhereHelp = it.filterEverywhereHelp
                filterEverywhereHelp.setOnClickListener {
                    MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                            message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
                }
                val usersCount = arguments!!.getInt(EXTRA_COUNT)
                val nUsers = resources.getQuantityString(R.plurals.N_users, usersCount, usersCount)
                confirmMessageView.text = getString(R.string.filter_user_confirm_message, nUsers)
            }
            return dialog
        }
    }

}
