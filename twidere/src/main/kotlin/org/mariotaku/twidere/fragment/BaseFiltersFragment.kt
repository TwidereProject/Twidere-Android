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

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.RawItemArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter
import org.mariotaku.twidere.adapter.SourceAutoCompleteAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.Utils.getDefaultAccountKey
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

abstract class BaseFiltersFragment : AbsContentListViewFragment<SimpleCursorAdapter>(),
        LoaderManager.LoaderCallbacks<Cursor?>, MultiChoiceModeListener {
    private var actionMode: ActionMode? = null

    abstract val contentUri: Uri

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        loaderManager.initLoader(0, null, this)
        setRefreshEnabled(false)
        showProgress()
    }

    //    @Override
    //    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    //        final View view = super.onCreateView(inflater, container, savedInstanceState);
    //        assert view != null;
    //        final ListView listView = (ListView) view.findViewById(R.id.list_view);
    //        final Resources res = getResources();
    //        final float density = res.getDisplayMetrics().density;
    //        final int padding = (int) density * 16;
    //        listView.setPadding(padding, 0, padding, 0);
    //        return view;
    //    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && actionMode != null) {
            actionMode!!.finish()
        }
    }

    override fun setControlVisible(visible: Boolean) {
        super.setControlVisible(visible || !isQuickReturnEnabled)
    }

    private val isQuickReturnEnabled: Boolean
        get() = actionMode == null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        setControlVisible(true)
        mode.menuInflater.inflate(R.menu.action_multi_select_items, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateTitle(mode)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                val where = Expression.`in`(Column(Filters._ID),
                        RawItemArray(listView.checkedItemIds))
                contentResolver.delete(contentUri, where.sql, null)
            }
            R.id.inverse_selection -> {
                val positions = listView.checkedItemPositions
                for (i in 0 until listView.count) {
                    listView.setItemChecked(i, !positions.get(i))
                }
                return true
            }
            else -> {
                return false
            }
        }
        mode.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long,
                                           checked: Boolean) {
        updateTitle(mode)
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
        return CursorLoader(activity, contentUri, contentColumns, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        val adapter = adapter
        adapter!!.swapCursor(data)
        if (data != null && data.count > 0) {
            showContent()
        } else {
            showEmpty(R.drawable.ic_info_volume_off, getString(R.string.no_rule))
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        val adapter = adapter
        adapter!!.swapCursor(null)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_filters, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val args = Bundle()
                args.putParcelable(EXTRA_URI, contentUri)
                val dialog = AddItemFragment()
                dialog.arguments = args
                dialog.show(fragmentManager, "add_rule")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override var refreshing: Boolean
        get() = false
        set(value) {
            super.refreshing = value
        }

    override fun onCreateAdapter(context: Context): SimpleCursorAdapter {
        return FilterListAdapter(context)
    }

    protected abstract val contentColumns: Array<String>

    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null || activity == null) return
        val count = listView!!.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    class AddItemFragment : BaseDialogFragment(), OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val text = text
                    if (TextUtils.isEmpty(text)) return
                    val values = ContentValues()
                    values.put(Filters.VALUE, text)
                    val args = arguments
                    val uri = args.getParcelable<Uri>(EXTRA_URI)!!
                    contentResolver.insert(uri, values)
                }
            }

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_auto_complete_textview)

            builder.setTitle(R.string.add_rule)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, this)
            val dialog = builder.create()
            dialog.setOnShowListener { dialog ->
                val alertDialog = dialog as AlertDialog
                val editText = (alertDialog.findViewById(R.id.edit_text) as AutoCompleteTextView?)!!
                val args = arguments
                val autoCompleteType: Int
                autoCompleteType = args.getInt(EXTRA_AUTO_COMPLETE_TYPE, 0)
                if (autoCompleteType != 0) {
                    val mUserAutoCompleteAdapter: SimpleCursorAdapter
                    if (autoCompleteType == AUTO_COMPLETE_TYPE_SOURCES) {
                        mUserAutoCompleteAdapter = SourceAutoCompleteAdapter(activity)
                    } else {
                        val adapter = ComposeAutoCompleteAdapter(activity)
                        adapter.setAccountKey(Utils.getDefaultAccountKey(activity))
                        mUserAutoCompleteAdapter = adapter
                    }
                    editText.setAdapter(mUserAutoCompleteAdapter)
                    editText.threshold = 1
                }
            }
            return dialog
        }

        protected val text: String
            get() {
                val alertDialog = dialog as AlertDialog
                val editText = (alertDialog.findViewById(R.id.edit_text) as AutoCompleteTextView?)!!
                return ParseUtils.parseString(editText.text)
            }

    }

    private class FilterListAdapter(context: Context) : SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_1, null, BaseFiltersFragment.FilterListAdapter.from, BaseFiltersFragment.FilterListAdapter.to, 0) {
        companion object {

            private val from = arrayOf(Filters.VALUE)

            private val to = intArrayOf(android.R.id.text1)
        }

    }

    class FilteredKeywordsFragment : BaseFiltersFragment() {

        override val contentUri: Uri
            get() = Filters.Keywords.CONTENT_URI

        public override val contentColumns: Array<String>
            get() = Filters.Keywords.COLUMNS


    }

    class FilteredLinksFragment : BaseFiltersFragment() {

        public override val contentColumns: Array<String>
            get() = Filters.Links.COLUMNS

        override val contentUri: Uri
            get() = Filters.Links.CONTENT_URI

    }

    class FilteredSourcesFragment : BaseFiltersFragment() {

        public override val contentColumns: Array<String>
            get() = Filters.Sources.COLUMNS

        override val contentUri: Uri
            get() = Filters.Sources.CONTENT_URI

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.add -> {
                    val args = Bundle()
                    args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_SOURCES)
                    args.putParcelable(EXTRA_URI, contentUri)
                    val dialog = AddItemFragment()
                    dialog.arguments = args
                    dialog.show(fragmentManager, "add_rule")
                    return true
                }
            }
            return super.onOptionsItemSelected(item)
        }

    }

    class FilteredUsersFragment : BaseFiltersFragment() {


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_SELECT_USER -> {
                    if (resultCode != FragmentActivity.RESULT_OK || !data!!.hasExtra(EXTRA_USER))
                        return
                    val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                    val values = ContentValuesCreator.createFilteredUser(user)
                    val resolver = contentResolver
                    val where = Expression.equalsArgs(Filters.Users.USER_KEY).sql
                    val whereArgs = arrayOf(user.key.toString())
                    resolver.delete(Filters.Users.CONTENT_URI, where, whereArgs)
                    resolver.insert(Filters.Users.CONTENT_URI, values)
                }
            }
        }

        public override val contentColumns: Array<String>
            get() = Filters.Users.COLUMNS

        override val contentUri: Uri
            get() = Filters.Users.CONTENT_URI

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.add -> {
                    val intent = Intent(INTENT_ACTION_SELECT_USER)
                    intent.setClass(context, UserListSelectorActivity::class.java)
                    intent.putExtra(EXTRA_ACCOUNT_KEY, getDefaultAccountKey(activity))
                    startActivityForResult(intent, REQUEST_SELECT_USER)
                    return true
                }
            }
            return super.onOptionsItemSelected(item)
        }

        override fun onCreateAdapter(context: Context): SimpleCursorAdapter {
            return FilterUsersListAdapter(context)
        }

        class FilterUsersListAdapter internal constructor(context: Context) : SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_2, null, arrayOfNulls<String>(0), IntArray(0), 0) {

            private val nameFirst: Boolean
            @Inject
            lateinit var userColorNameManager: UserColorNameManager
            @Inject
            lateinit var preferences: SharedPreferencesWrapper
            private var userIdIdx: Int = 0
            private var nameIdx: Int = 0
            private var screenNameIdx: Int = 0

            init {
                GeneralComponentHelper.build(context).inject(this)
                nameFirst = preferences.getBoolean(KEY_NAME_FIRST, true)
            }

            override fun bindView(view: View, context: Context?, cursor: Cursor) {
                super.bindView(view, context, cursor)
                val text1 = view.findViewById(android.R.id.text1) as TextView
                val text2 = view.findViewById(android.R.id.text2) as TextView
                val userId = UserKey.valueOf(cursor.getString(userIdIdx))!!
                val name = cursor.getString(nameIdx)
                val screenName = cursor.getString(screenNameIdx)
                val displayName = userColorNameManager.getDisplayName(userId, name, screenName,
                        nameFirst)
                text1.text = displayName
                text2.text = userId.host
            }

            override fun swapCursor(c: Cursor?): Cursor? {
                val old = super.swapCursor(c)
                if (c != null) {
                    userIdIdx = c.getColumnIndex(Filters.Users.USER_KEY)
                    nameIdx = c.getColumnIndex(Filters.Users.NAME)
                    screenNameIdx = c.getColumnIndex(Filters.Users.SCREEN_NAME)
                }
                return old
            }

        }

    }

    companion object {

        private val EXTRA_AUTO_COMPLETE_TYPE = "auto_complete_type"
        private val AUTO_COMPLETE_TYPE_SOURCES = 2
    }
}
