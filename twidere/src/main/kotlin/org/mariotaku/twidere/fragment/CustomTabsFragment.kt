/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.SparseArray
import android.view.*
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView.OnItemClickListener
import com.afollestad.appthemeengine.ATEActivity
import com.afollestad.appthemeengine.Config
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter
import kotlinx.android.synthetic.main.layout_draggable_list_with_empty_view.*
import kotlinx.android.synthetic.main.list_item_section_header.view.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.RawItemArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.TabCursorIndices
import org.mariotaku.twidere.model.TabValuesCreator
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.model.tab.iface.AccountCallback
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs
import org.mariotaku.twidere.util.CustomTabUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder

class CustomTabsFragment : BaseSupportFragment(), LoaderCallbacks<Cursor?>, MultiChoiceModeListener {

    private var adapter: CustomTabsAdapter? = null

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                val itemIds = listView.checkedItemIds
                val where = Expression.`in`(Column(Tabs._ID), RawItemArray(itemIds))
                contentResolver.delete(Tabs.CONTENT_URI, where.sql, null)
                SettingsActivity.setShouldRestart(activity)
            }
        }
        mode.finish()
        return true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = CustomTabsAdapter(context)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val tab = adapter!!.getTab(position)
            val df = TabEditorDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_OBJECT] = tab
            }
            df.show(fragmentManager, TabEditorDialogFragment.TAG_EDIT_TAB)
        }
        listView.adapter = adapter
        listView.emptyView = emptyView
        listView.setDropListener { from, to ->
            adapter!!.drop(from, to)
            if (listView.choiceMode != AbsListView.CHOICE_MODE_NONE) {
                listView.moveCheckState(from, to)
            }
            saveTabPositions()
        }
        emptyText.setText(R.string.no_tab)
        emptyIcon.setImageResource(R.drawable.ic_info_tab)
        loaderManager.initLoader(0, null, this)
        setListShown(false)
    }

    private fun setListShown(shown: Boolean) {
        listContainer.visibility = if (shown) View.VISIBLE else View.GONE
        progressContainer.visibility = if (shown) View.GONE else View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_TAB -> {
                if (resultCode == Activity.RESULT_OK) {
                    val values = ContentValues()
                    values.put(Tabs.NAME, data!!.getStringExtra(EXTRA_NAME))
                    values.put(Tabs.ICON, data.getStringExtra(EXTRA_ICON))
                    values.put(Tabs.TYPE, data.getStringExtra(EXTRA_TYPE))
                    values.put(Tabs.ARGUMENTS, data.getStringExtra(EXTRA_ARGUMENTS))
                    values.put(Tabs.EXTRAS, data.getStringExtra(EXTRA_EXTRAS))
                    values.put(Tabs.POSITION, adapter!!.count)
                    contentResolver.insert(Tabs.CONTENT_URI, values)
                    SettingsActivity.setShouldRestart(activity)
                }
            }
            REQUEST_EDIT_TAB -> {
                if (resultCode == Activity.RESULT_OK && data!!.hasExtra(EXTRA_ID)) {
                    val values = ContentValues()
                    values.put(Tabs.NAME, data.getStringExtra(EXTRA_NAME))
                    values.put(Tabs.ICON, data.getStringExtra(EXTRA_ICON))
                    values.put(Tabs.EXTRAS, data.getStringExtra(EXTRA_EXTRAS))
                    val where = Expression.equals(Tabs._ID, data.getLongExtra(EXTRA_ID, -1)).sql
                    contentResolver.update(Tabs.CONTENT_URI, values, where, null)
                    SettingsActivity.setShouldRestart(activity)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_multi_select_items, menu)
        return true
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        return CursorLoader(activity, Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_custom_tabs, menu)
        val context = this.context
        val accountIds = DataStoreUtils.getAccountKeys(context)
        val itemAdd = menu.findItem(R.id.add_submenu)
        if (itemAdd != null && itemAdd.hasSubMenu()) {
            val subMenu = itemAdd.subMenu
            subMenu.clear()
            for ((type, conf) in TabConfiguration.all()) {
                val accountIdRequired = (conf.accountFlags and TabConfiguration.FLAG_ACCOUNT_REQUIRED) != 0
                val subItem = subMenu.add(0, 0, conf.sortPosition, conf.name.createString(context))
                val disabledByNoAccount = accountIdRequired && accountIds.isEmpty()
                val disabledByDuplicateTab = conf.isSingleTab && CustomTabUtils.isTabAdded(context, type)
                val shouldDisable = disabledByDuplicateTab || disabledByNoAccount
                subItem.isVisible = !shouldDisable
                subItem.isEnabled = !shouldDisable
                val icon = conf.icon.createDrawable(context)
                if (context is ATEActivity) {
                    icon.mutate().setColorFilter(Config.textColorPrimary(context, context.ateKey),
                            Mode.SRC_ATOP)
                }
                subItem.icon = icon
                subItem.setOnMenuItemClickListener { item ->
                    val df = TabEditorDialogFragment()
                    df.arguments = Bundle {
                        this[EXTRA_TAB_TYPE] = type
                        val adapter = adapter!!
                        if (!adapter.isEmpty) {
                            this[EXTRA_TAB_POSITION] = adapter.getTab(adapter.count - 1).position + 1
                        }
                    }
                    df.show(fragmentManager, TabEditorDialogFragment.TAG_ADD_TAB)
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false)
    }

    override fun onDestroyActionMode(mode: ActionMode) {

    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long,
                                           checked: Boolean) {
        updateTitle(mode)
    }


    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter!!.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        adapter!!.changeCursor(cursor)
        setListShown(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            else -> {
                val intent = item.intent ?: return false
                startActivityForResult(intent, REQUEST_ADD_TAB)
                return true
            }
        }
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateTitle(mode)
        return true
    }

    override fun onStop() {
        super.onStop()
    }

    private fun saveTabPositions() {
        val positions = adapter!!.cursorPositions
        val c = adapter!!.cursor
        if (positions != null && c != null && !c.isClosed) {
            val idIdx = c.getColumnIndex(Tabs._ID)
            for (i in 0 until positions.size) {
                c.moveToPosition(positions[i])
                val id = c.getLong(idIdx)
                val values = ContentValues()
                values.put(Tabs.POSITION, i)
                val where = Expression.equals(Tabs._ID, id).sql
                contentResolver.update(Tabs.CONTENT_URI, values, where, null)
            }
        }
        SettingsActivity.setShouldRestart(activity)
    }

    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null || activity == null) return
        val count = listView.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    class TabEditorDialogFragment : BaseDialogFragment(), DialogInterface.OnShowListener, AccountCallback {

        private val activityResultMap: SparseArray<TabConfiguration.ExtraConfiguration> = SparseArray()

        override fun onShow(dialog: DialogInterface) {
            dialog as AlertDialog
            @CustomTabType
            val tabType: String
            val tab: Tab
            val conf: TabConfiguration
            when (tag) {
                TAG_ADD_TAB -> {
                    tabType = arguments.getString(EXTRA_TAB_TYPE)
                    tab = Tab()
                    conf = TabConfiguration.ofType(tabType)!!
                    tab.type = tabType
                    tab.icon = conf.icon.persistentKey
                    tab.position = arguments.getInt(EXTRA_TAB_POSITION)
                }
                TAG_EDIT_TAB -> {
                    tab = arguments.getParcelable(EXTRA_OBJECT)
                    tabType = tab.type
                    conf = TabConfiguration.ofType(tabType) ?: run {
                        dismiss()
                        return
                    }
                }
                else -> {
                    throw AssertionError()
                }
            }

            val tabName = dialog.findViewById(R.id.tabName) as EditText
            val iconSpinner = dialog.findViewById(R.id.tab_icon_spinner) as Spinner
            val accountSpinner = dialog.findViewById(R.id.account_spinner) as Spinner
            val accountContainer = dialog.findViewById(R.id.account_container)!!
            val accountSectionHeader = accountContainer.sectionHeader
            val extraConfigContainer = dialog.findViewById(R.id.extra_config_container) as LinearLayout

            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            val iconsAdapter = TabIconsAdapter(context)
            val accountsAdapter = AccountsSpinnerAdapter(context)
            iconSpinner.adapter = iconsAdapter
            accountSpinner.adapter = accountsAdapter

            iconsAdapter.setData(DrawableHolder.builtins())

            tabName.hint = conf.name.createString(context)
            tabName.setText(tab.name)
            iconSpinner.setSelection(iconsAdapter.findPositionByKey(tab.icon))
            accountSectionHeader.setText(R.string.account)

            val editMode = tag == TAG_EDIT_TAB

            val hasAccount = conf.accountFlags and TabConfiguration.FLAG_HAS_ACCOUNT != 0
            val accountMutable = conf.accountFlags and TabConfiguration.FLAG_ACCOUNT_MUTABLE != 0
            if (hasAccount && (accountMutable || !editMode)) {
                accountContainer.visibility = View.VISIBLE
                val accountIdRequired = conf.accountFlags and TabConfiguration.FLAG_ACCOUNT_REQUIRED != 0
                accountsAdapter.clear()
                if (!accountIdRequired) {
                    accountsAdapter.add(AccountDetails.dummy())
                }
                val officialKeyOnly = arguments.getBoolean(EXTRA_OFFICIAL_KEY_ONLY, false)
                accountsAdapter.addAll(AccountUtils.getAllAccountDetails(AccountManager.get(context)).filter {
                    if (officialKeyOnly && !it.isOfficial(context)) {
                        return@filter false
                    }
                    return@filter true
                })
                accountsAdapter.setDummyItemText(R.string.activated_accounts)

                tab.arguments?.accountKeys?.firstOrNull()?.let { key ->
                    accountSpinner.setSelection(accountsAdapter.findPositionByKey(key))
                }
            } else {
                accountContainer.visibility = View.GONE
            }

            val extraConfigurations = conf.getExtraConfigurations(context).orEmpty()

            fun inflateHeader(title: String): View {
                val headerView = LayoutInflater.from(context).inflate(R.layout.list_item_section_header,
                        extraConfigContainer, false)
                headerView.sectionHeader.text = title
                return headerView
            }

            extraConfigurations.forEachIndexed { idx, extraConf ->
                extraConf.onCreate(context)
                extraConf.position = idx + 1
                // Hide immutable settings in edit mode
                if (editMode && !extraConf.isMutable) return@forEachIndexed
                extraConf.headerTitle?.let {
                    // Inflate header with headerTitle
                    extraConfigContainer.addView(inflateHeader(it.createString(context)))
                }
                val view = extraConf.onCreateView(context, extraConfigContainer)
                extraConf.onViewCreated(context, view, this)
                conf.readExtraConfigurationFrom(tab, extraConf)
                extraConfigContainer.addView(view)
            }

            positiveButton.setOnClickListener {
                tab.name = tabName.text.toString()
                tab.icon = (iconSpinner.selectedItem as DrawableHolder).persistentKey
                tab.arguments = CustomTabUtils.newTabArguments(tabType)
                if (hasAccount) {
                    val account = accountSpinner.selectedItem as? AccountDetails ?: return@setOnClickListener
                    if (!account.dummy) {
                        tab.arguments?.accountKeys = arrayOf(account.key)
                    } else {
                        tab.arguments?.accountKeys = null
                    }
                }
                tab.extras = CustomTabUtils.newTabExtras(tabType)
                extraConfigurations.forEach {
                    // Make sure immutable configuration skipped in edit mode
                    if (editMode && !it.isMutable) return@forEach
                    if (!conf.applyExtraConfigurationTo(tab, it)) {
                        return@setOnClickListener
                    }
                }
                when (tag) {
                    TAG_EDIT_TAB -> {
                        val where = Expression.equalsArgs(Tabs._ID).sql
                        val whereArgs = arrayOf(tab.id.toString())
                        context.contentResolver.update(Tabs.CONTENT_URI, TabValuesCreator.create(tab), where, whereArgs)
                    }
                    TAG_ADD_TAB -> {
                        context.contentResolver.insert(Tabs.CONTENT_URI, TabValuesCreator.create(tab))
                    }
                }
                dismiss()
            }
        }

        override fun getAccount(): AccountDetails? {
            return (dialog.findViewById(R.id.account_spinner) as Spinner).selectedItem as? AccountDetails
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_custom_tab_editor)
            builder.setPositiveButton(R.string.save, null)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener(this)
            return dialog
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            val extraConf = activityResultMap.get(requestCode)
            activityResultMap.remove(requestCode)
            extraConf?.onActivityResult(requestCode and 0xFF, resultCode, data)
        }

        fun startExtraConfigurationActivityForResult(extraConf: TabConfiguration.ExtraConfiguration, intent: Intent, requestCode: Int) {
            val requestCodeInternal = extraConf.position shl 8 and 0xFF00 or (requestCode and 0xFF)
            activityResultMap.put(requestCodeInternal, extraConf)
            startActivityForResult(intent, requestCodeInternal)
        }

        companion object {

            const val TAG_EDIT_TAB = "edit_tab"
            const val TAG_ADD_TAB = "add_tab"
        }
    }

    internal class TabIconsAdapter(context: Context) : ArrayAdapter<DrawableHolder>(context, R.layout.spinner_item_custom_tab_icon) {

        private val iconColor: Int

        init {
            setDropDownViewResource(R.layout.list_item_two_line_small)
            iconColor = ThemeUtils.getThemeForegroundColor(context)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            view.findViewById(android.R.id.text2).visibility = View.GONE
            val text1 = view.findViewById(android.R.id.text1) as TextView
            val item = getItem(position)
            text1.text = item.name
            bindIconView(item, view)
            return view
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            bindIconView(getItem(position), view)
            return view
        }

        fun setData(list: List<DrawableHolder>?) {
            clear()
            if (list == null) return
            addAll(list)
        }

        private fun bindIconView(item: DrawableHolder, view: View) {
            val icon = view.findViewById(android.R.id.icon) as ImageView
            icon.setColorFilter(iconColor, Mode.SRC_ATOP)
            icon.setImageDrawable(item.createDrawable(icon.context))
        }

        fun findPositionByKey(key: String): Int {
            return (0 until count).indexOfFirst { getItem(it).persistentKey == key }
        }

    }

    class CustomTabsAdapter(context: Context) : SimpleDragSortCursorAdapter(context,
            R.layout.list_item_custom_tab, null, emptyArray(), intArrayOf(), 0) {

        private val iconColor: Int
        private var indices: TabCursorIndices? = null

        init {
            iconColor = ThemeUtils.getThemeForegroundColor(context)
        }

        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val holder = view.tag as TwoLineWithIconViewHolder
            val indices = indices!!
            val type = cursor.getString(indices.type)
            val name = cursor.getString(indices.name)
            val iconKey = cursor.getString(indices.icon)
            if (CustomTabUtils.isTabTypeValid(type)) {
                val typeName = CustomTabUtils.getTabTypeName(context, type)
                holder.text1.text = if (TextUtils.isEmpty(name)) typeName else name
                holder.text1.paintFlags = holder.text1.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.text2.visibility = View.VISIBLE
                holder.text2.text = typeName
            } else {
                holder.text1.text = name
                holder.text1.paintFlags = holder.text1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.text2.setText(R.string.invalid_tab)
            }
            val icon = CustomTabUtils.getTabIconDrawable(context, DrawableHolder.parse(iconKey))
            holder.icon.visibility = View.VISIBLE
            if (icon != null) {
                holder.icon.setImageDrawable(icon)
            } else {
                holder.icon.setImageResource(R.drawable.ic_action_list)
            }
            holder.icon.setColorFilter(iconColor, Mode.SRC_ATOP)
        }

        override fun changeCursor(cursor: Cursor?) {
            if (cursor != null) {
                indices = TabCursorIndices(cursor)
            }
            super.changeCursor(cursor)
        }

        override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
            val view = super.newView(context, cursor, parent)
            val tag = view.tag
            if (tag !is TwoLineWithIconViewHolder) {
                val holder = TwoLineWithIconViewHolder(view)
                view.tag = holder
            }
            return view
        }


        fun getTab(position: Int): Tab {
            cursor.moveToPosition(position)
            return indices!!.newObject(cursor)
        }

    }

}
