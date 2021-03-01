package org.mariotaku.twidere.fragment.filter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.appcompat.app.AlertDialog
import android.view.*
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.layout_list_with_empty_view.*
import okhttp3.HttpUrl
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACTION
import org.mariotaku.twidere.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.getComponentLabel
import org.mariotaku.twidere.extension.model.instantiateComponent
import org.mariotaku.twidere.extension.model.setupUrl
import org.mariotaku.twidere.extension.util.isAdvancedFiltersEnabled
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.FiltersSubscription
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.task.filter.RefreshFiltersSubscriptionsTask
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.view.SimpleTextWatcher
import java.lang.ref.WeakReference


/**
 * Created by mariotaku on 2016/12/31.
 */
class FiltersSubscriptionsFragment : BaseFragment(), LoaderManager.LoaderCallbacks<Cursor>,
        AbsListView.MultiChoiceModeListener {

    private lateinit var adapter: FilterSubscriptionsAdapter
    private var actionMode: ActionMode? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        adapter = FilterSubscriptionsAdapter(requireContext())
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)

        listContainer.visibility = View.GONE
        progressContainer.visibility = View.VISIBLE
        LoaderManager.getInstance(this).initLoader(0, null, this)


        if (!extraFeaturesService.isSupported()) {
//            activity?.finish()
//            return
        }

        if (savedInstanceState == null) {
            parentFragmentManager.let { fragmentManager ->
                when (arguments?.getString(EXTRA_ACTION)) {
                    ACTION_ADD_URL_SUBSCRIPTION -> {
                        if (!extraFeaturesService.isAdvancedFiltersEnabled) {
                            val df = ExtraFeaturesIntroductionDialogFragment.create(
                                    ExtraFeaturesService.FEATURE_ADVANCED_FILTERS)
                            df.setTargetFragment(this, REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE)
                            df.show(fragmentManager, ExtraFeaturesIntroductionDialogFragment.FRAGMENT_TAG)
                        } else {
                            showAddUrlSubscription()
                        }
                    }
                    else -> {
                        if (!extraFeaturesService.isAdvancedFiltersEnabled) {
                            val df = ExtraFeaturesIntroductionDialogFragment.create(
                                    ExtraFeaturesService.FEATURE_ADVANCED_FILTERS)
                            df.setTargetFragment(this, REQUEST_PURCHASE_EXTRA_FEATURES)
                            df.show(fragmentManager, ExtraFeaturesIntroductionDialogFragment.FRAGMENT_TAG)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                    executeAfterFragmentResumed { fragment ->
                        (fragment as FiltersSubscriptionsFragment).showAddUrlSubscription()
                    }
                } else {
                    activity?.finish()
                }
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                } else {
                    activity?.finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_subscriptions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val df = AddUrlSubscriptionDialogFragment()
                parentFragmentManager.let { df.show(it, "add_url_subscription") }
                return true
            }
            R.id.refresh -> {
                executeAfterFragmentResumed { fragment ->
                    ProgressDialogFragment.show(fragment.childFragmentManager, FRAGMENT_TAG_REFRESH_FILTERS)
                    val task = RefreshFiltersSubscriptionsTask(fragment.requireContext())
                    val fragmentRef = WeakReference(fragment)
                    task.callback = {
                        fragmentRef.get()?.executeAfterFragmentResumed { fragment ->
                            fragment.parentFragmentManager.dismissDialogFragment(FRAGMENT_TAG_REFRESH_FILTERS)
                        }
                    }
                    TaskStarter.execute(task)
                }
                return true
            }
            else -> return false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_list_with_empty_view, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val loader = CursorLoader(requireContext())
        loader.uri = Filters.Subscriptions.CONTENT_URI
        loader.projection = Filters.Subscriptions.COLUMNS
        return loader
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        adapter.changeCursor(cursor)
        if (cursor.isEmpty) {
            listView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyIcon.setImageResource(R.drawable.ic_info_info_generic)
            emptyText.setText(R.string.hint_empty_filters_subscriptions)
        } else {
            listView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
        listContainer.visibility = View.VISIBLE
        progressContainer.visibility = View.GONE
    }

    override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {

    }


    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        mode.menuInflater.inflate(R.menu.action_multi_select_items, menu)
        menu.setGroupAvailability(R.id.selection_group, true)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        listView.updateSelectionItems(menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                performDeletion()
                mode.finish()
            }
            R.id.select_all -> {
                listView.selectAll()
            }
            R.id.select_none -> {
                listView.selectNone()
            }
            R.id.invert_selection -> {
                listView.invertSelection()
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
    }

    private fun performDeletion() {
        val ids = listView.checkedItemIds
        val resolver = context?.contentResolver ?: return
        val where = Expression.inArgs(Filters.Subscriptions._ID, ids.size).sql
        val whereArgs = ids.toStringArray()
        resolver.queryReference(Filters.Subscriptions.CONTENT_URI, Filters.Subscriptions.COLUMNS, where,
                whereArgs, null)?.use { (cursor) ->
            val indices = ObjectCursor.indicesFrom(cursor, FiltersSubscription::class.java)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val subscription = indices.newObject(cursor)
                subscription.instantiateComponent(requireContext())?.deleteLocalData()
                cursor.moveToNext()
            }
        }
        ContentResolverUtils.bulkDelete(resolver, Filters.Subscriptions.CONTENT_URI, Filters._ID,
                false, ids, null, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.SOURCE,
                false, ids, null, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI, Filters.Keywords.SOURCE,
                false, ids, null, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI, Filters.Sources.SOURCE,
                false, ids, null, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Links.CONTENT_URI, Filters.Links.SOURCE,
                false, ids, null, null)
    }

    private fun showAddUrlSubscription() {
        val df = AddUrlSubscriptionDialogFragment()
        df.arguments = Bundle {
            this[EXTRA_ADD_SUBSCRIPTION_URL] = arguments?.getString(EXTRA_ADD_SUBSCRIPTION_URL)
            this[EXTRA_ADD_SUBSCRIPTION_NAME] = arguments?.getString(EXTRA_ADD_SUBSCRIPTION_NAME)
        }
        parentFragmentManager.let { df.show(it, "add_url_subscription") }
    }

    class FilterSubscriptionsAdapter(context: Context) : SimpleCursorAdapter(context,
            R.layout.list_item_two_line, null, arrayOf(Filters.Subscriptions.NAME),
            intArrayOf(android.R.id.text1), 0) {
        private var indices: ObjectCursor.CursorIndices<FiltersSubscription>? = null
        private var tempObject: FiltersSubscription = FiltersSubscription()

        override fun swapCursor(c: Cursor?): Cursor? {
            indices = c?.let { ObjectCursor.indicesFrom(it, FiltersSubscription::class.java) }
            return super.swapCursor(c)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val iconView = view.findViewById<View>(android.R.id.icon)
            val summaryView = view.findViewById<TextView>(android.R.id.text2)

            indices.parseFields(tempObject, cursor)

            iconView.visibility = View.GONE
            summaryView.text = tempObject.getComponentLabel(context)
        }
    }

    class AddUrlSubscriptionDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.dialog_add_filters_subscription)
            builder.setPositiveButton(R.string.action_add_filters_subscription) { dialog, _ ->
                dialog as AlertDialog
                val editName = dialog.findViewById<MaterialEditText>(R.id.name)!!
                val editUrl = dialog.findViewById<MaterialEditText>(R.id.url)!!
                val subscription = FiltersSubscription()
                subscription.name = editName.text.toString()
                subscription.setupUrl(editUrl.text.toString())
                val component = subscription.instantiateComponent(requireContext()) ?: return@setPositiveButton
                component.firstAdded()
                val vc = ObjectCursor.valuesCreatorFrom(FiltersSubscription::class.java)
                requireContext().contentResolver.insert(Filters.Subscriptions.CONTENT_URI, vc.create(subscription))
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow {
                it.applyTheme()
                val editName = it.findViewById<MaterialEditText>(R.id.name)!!
                val editUrl = it.findViewById<MaterialEditText>(R.id.url)!!
                val positiveButton = it.getButton(DialogInterface.BUTTON_POSITIVE)

                fun updateEnableState() {
                    val nameValid = !editName.empty
                    val urlValid = HttpUrl.parse(editUrl.text.toString()) != null
                    positiveButton.isEnabled = nameValid && urlValid
                }

                val watcher = object : SimpleTextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        updateEnableState()
                    }
                }
                editName.addTextChangedListener(watcher)
                editUrl.addTextChangedListener(watcher)

                val args = arguments
                if (savedInstanceState == null && args != null) {
                    editName.setText(args.getString(EXTRA_ADD_SUBSCRIPTION_NAME))
                    editUrl.setText(args.getString(EXTRA_ADD_SUBSCRIPTION_URL))
                }

                updateEnableState()
            }
            return dialog
        }
    }

    companion object {
        const val ACTION_ADD_URL_SUBSCRIPTION = "${INTENT_PACKAGE_PREFIX}ADD_URL_FILTERS_SUBSCRIPTION"
        const val EXTRA_ADD_SUBSCRIPTION_URL = "add_subscription.url"
        const val EXTRA_ADD_SUBSCRIPTION_NAME = "add_subscription.name"
        private const val REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE = 101
        private const val FRAGMENT_TAG_REFRESH_FILTERS = "refresh_filters"
    }
}


