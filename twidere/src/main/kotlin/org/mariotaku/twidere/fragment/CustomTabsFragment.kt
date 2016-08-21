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

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.content.res.ResourcesCompat
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.afollestad.appthemeengine.ATEActivity
import com.afollestad.appthemeengine.Config
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter
import kotlinx.android.synthetic.main.layout_draggable_list_with_empty_view.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.RawItemArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.CustomTabEditorActivity
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.model.CustomTabConfiguration
import org.mariotaku.twidere.model.CustomTabConfiguration.CustomTabConfigurationComparator
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs
import org.mariotaku.twidere.util.CustomTabUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder
import java.util.*

class CustomTabsFragment : BaseSupportFragment(), LoaderCallbacks<Cursor?>, MultiChoiceModeListener, OnItemClickListener {

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
        listView.onItemClickListener = this
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

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val c = adapter!!.cursor
        c.moveToPosition(adapter!!.getCursorPosition(position))
        val intent = Intent(INTENT_ACTION_EDIT_TAB)
        intent.setClass(activity, CustomTabEditorActivity::class.java)
        intent.putExtra(EXTRA_ID, c.getLong(c.getColumnIndex(Tabs._ID)))
        intent.putExtra(EXTRA_TYPE, c.getString(c.getColumnIndex(Tabs.TYPE)))
        intent.putExtra(EXTRA_NAME, c.getString(c.getColumnIndex(Tabs.NAME)))
        intent.putExtra(EXTRA_ICON, c.getString(c.getColumnIndex(Tabs.ICON)))
        intent.putExtra(EXTRA_EXTRAS, c.getString(c.getColumnIndex(Tabs.EXTRAS)))
        startActivityForResult(intent, REQUEST_EDIT_TAB)
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_custom_tabs, menu)
        val res = resources
        val activity = activity
        val accountIds = DataStoreUtils.getAccountKeys(activity)
        val itemAdd = menu!!.findItem(R.id.add_submenu)
        if (itemAdd != null && itemAdd.hasSubMenu()) {
            val subMenu = itemAdd.subMenu
            subMenu.clear()
            val map = CustomTabUtils.getConfigurationMap()
            val tabs = ArrayList(
                    map.entries)
            Collections.sort(tabs, CustomTabConfigurationComparator.SINGLETON)
            for ((type, conf) in tabs) {

                val accountIdRequired = conf.accountRequirement == CustomTabConfiguration.ACCOUNT_REQUIRED

                val intent = Intent(INTENT_ACTION_ADD_TAB)
                intent.setClass(activity, CustomTabEditorActivity::class.java)
                intent.putExtra(EXTRA_TYPE, type)

                val subItem = subMenu.add(conf.defaultTitle)
                val disabledByNoAccount = accountIdRequired && accountIds.size == 0
                val disabledByDuplicateTab = conf.isSingleTab && CustomTabUtils.isTabAdded(activity, type)
                val shouldDisable = disabledByDuplicateTab || disabledByNoAccount
                subItem.isVisible = !shouldDisable
                subItem.isEnabled = !shouldDisable
                val icon = ResourcesCompat.getDrawable(res, conf.defaultIcon, null)
                if (icon != null && activity is ATEActivity) {
                    icon.mutate().setColorFilter(Config.textColorPrimary(activity,
                            activity.ateKey), Mode.SRC_ATOP)
                }
                subItem.icon = icon
                subItem.intent = intent
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

    class CustomTabsAdapter(context: Context) : SimpleDragSortCursorAdapter(context, R.layout.list_item_custom_tab, null, arrayOfNulls<String>(0), IntArray(0), 0) {

        private val mIconColor: Int
        private var indices: CursorIndices? = null

        init {
            mIconColor = ThemeUtils.getThemeForegroundColor(context)
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
            val icon = CustomTabUtils.getTabIconDrawable(context, CustomTabUtils.getTabIconObject(iconKey))
            holder.icon.visibility = View.VISIBLE
            if (icon != null) {
                holder.icon.setImageDrawable(icon)
            } else {
                holder.icon.setImageResource(R.drawable.ic_action_list)
            }
            holder.icon.setColorFilter(mIconColor, Mode.SRC_ATOP)
        }

        override fun changeCursor(cursor: Cursor?) {
            if (cursor != null) {
                indices = CursorIndices(cursor)
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

        internal class CursorIndices(cursor: Cursor) {
            val _id: Int
            val name: Int
            val icon: Int
            val type: Int
            val arguments: Int

            init {
                _id = cursor.getColumnIndex(Tabs._ID)
                icon = cursor.getColumnIndex(Tabs.ICON)
                name = cursor.getColumnIndex(Tabs.NAME)
                type = cursor.getColumnIndex(Tabs.TYPE)
                arguments = cursor.getColumnIndex(Tabs.ARGUMENTS)
            }
        }

    }

}
