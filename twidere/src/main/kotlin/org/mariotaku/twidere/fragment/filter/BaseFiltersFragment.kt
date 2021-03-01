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

package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.core.view.ViewCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.*
import android.widget.AbsListView
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.setGroupAvailability
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.invertSelection
import org.mariotaku.twidere.extension.selectAll
import org.mariotaku.twidere.extension.selectNone
import org.mariotaku.twidere.extension.updateSelectionItems
import org.mariotaku.twidere.fragment.AbsContentListViewFragment
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.ThemeUtils


abstract class BaseFiltersFragment : AbsContentListViewFragment<SimpleCursorAdapter>(),
        LoaderManager.LoaderCallbacks<Cursor?>, MultiChoiceModeListener {

    override var refreshing: Boolean
        get() = false
        set(value) {
            super.refreshing = value
        }

    private var actionMode: ActionMode? = null

    protected abstract val contentUri: Uri
    protected abstract val contentColumns: Array<String>
    protected open val sortOrder: String? = "${Filters.SOURCE} >= 0"
    protected open val supportsEdit: Boolean = true

    private val isQuickReturnEnabled: Boolean
        get() = actionMode == null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
            onItemClick(pos)
        }
        listView.setMultiChoiceModeListener(this)
        LoaderManager.getInstance(this).initLoader(0, null, this)
        refreshEnabled = false
        showProgress()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            actionMode?.finish()
        }
    }

    override fun setControlVisible(visible: Boolean) {
        super.setControlVisible(visible || !isQuickReturnEnabled)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        setControlVisible(true)
        mode.menuInflater.inflate(R.menu.action_multi_select_items, menu)
        menu.setGroupAvailability(R.id.selection_group, true)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateTitle(mode)
        listView.updateSelectionItems(menu)
        val checkedPos = listView.checkedItemPositions
        val adapter = this.adapter
        val hasUneditableItem = (0 until checkedPos.size()).any { i ->
            if (checkedPos.valueAt(i) && adapter is IFilterAdapter) {
                return@any adapter.isReadOnly(checkedPos.keyAt(i))
            }
            return@any false
        }
        menu.setGroupAvailability(R.id.edit_group, !hasUneditableItem)
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
            else -> {
                return false
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
        updateTitle(mode)
        mode.invalidate()
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)
        if (ViewCompat.isLaidOut(view)) {
            val childCount = view.childCount
            if (childCount > 0) {
                val firstChild = view.getChildAt(0)
                val activity = activity
                var controlBarHeight = 0
                if (activity is IControlBarActivity) {
                    controlBarHeight = activity.controlBarHeight
                }
                val visible = firstChild.top > controlBarHeight
                setControlVisible(visible)
            } else {
                setControlVisible(true)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val selection = Expression.isNull(Columns.Column(Filters.USER_KEY))
        return CursorLoader(requireActivity(), contentUri, contentColumns, selection.sql, null, sortOrder)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        adapter.swapCursor(data)
        if (data != null && data.count > 0) {
            showContent()
        } else {
            showEmpty(R.drawable.ic_info_volume_off, getString(R.string.no_rule))
        }
        actionMode?.invalidate()
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter.swapCursor(null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                addOrEditItem()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): SimpleCursorAdapter {
        return FilterListAdapter(context)
    }

    protected open fun onItemClick(position: Int) {
        val adapter = this.adapter as FilterListAdapter
        val item = adapter.getFilterItem(position) ?: return
        if (item.source >= 0) return
        addOrEditItem(item.id, item.value, item.scope)
    }

    protected open fun performDeletion() {
        val ids = listView.checkedItemIds
        val where = Expression.inArgs(Columns.Column(Filters._ID), ids.size)
        context?.contentResolver?.delete(contentUri, where.sql, Array(ids.size) { ids[it].toString() })
    }

    protected open fun addOrEditItem(id: Long = -1, value: String? = null, scope: Int = FilterScope.DEFAULT) {
        val dialog = AddEditItemFragment()
        dialog.arguments = Bundle {
            this[EXTRA_URI] = contentUri
            this[EXTRA_ID] = id
            this[EXTRA_VALUE] = value
            this[EXTRA_SCOPE] = scope
        }
        parentFragmentManager.let { dialog.show(it, "add_rule") }
    }


    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null || activity == null) return
        val count = listView!!.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    interface IFilterAdapter {
        fun isReadOnly(position: Int): Boolean
    }


    private class FilterListAdapter(
            context: Context
    ) : SimpleCursorAdapter(context, R.layout.simple_list_item_activated_1, null,
            emptyArray(), intArrayOf(), 0), IFilterAdapter {

        private var indices: ObjectCursor.CursorIndices<FiltersData.BaseItem>? = null
        private val secondaryTextColor = ThemeUtils.getTextColorSecondary(context)

        override fun swapCursor(c: Cursor?): Cursor? {
            indices = c?.let { ObjectCursor.indicesFrom(it, FiltersData.BaseItem::class.java) }
            return super.swapCursor(c)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val text1 = view.findViewById<TextView>(android.R.id.text1)

            val ssb = SpannableStringBuilder(cursor.getString(indices[Filters.VALUE]))
            if (cursor.getLong(indices[Filters.SOURCE]) >= 0) {
                val start = ssb.length
                ssb.append("*")
                val end = start + 1
                ContextCompat.getDrawable(context, R.drawable.ic_action_sync)?.let { drawable ->
                    drawable.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_ATOP)
                    ssb.setSpan(EmojiSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            }
            text1.text = ssb
        }


        override fun isReadOnly(position: Int): Boolean {
            val cursor = this.cursor ?: return false
            val indices = this.indices ?: return false
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(indices[Filters.SOURCE]) >= 0
            }
            return false
        }

        fun getFilterItem(position: Int): FiltersData.BaseItem? {
            val cursor = this.cursor ?: return null
            val indices = this.indices ?: return null
            if (cursor.moveToPosition(position)) {
                return indices.newObject(cursor)
            }
            return null
        }
    }

    companion object {

        internal const val REQUEST_ADD_USER_SELECT_ACCOUNT = 201
        internal const val REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT = 202
        internal const val REQUEST_IMPORT_MUTES_SELECT_ACCOUNT = 203
        internal const val REQUEST_EXPORT_MUTES_SELECT_ACCOUNT = 204

    }
}
