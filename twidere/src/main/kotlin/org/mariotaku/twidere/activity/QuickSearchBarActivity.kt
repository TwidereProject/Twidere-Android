/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.activity

import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff.Mode
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import jopt.csp.util.SortableIntList
import kotlinx.android.synthetic.main.activity_quick_search_bar.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_ACCOUNT_KEY
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_QUERY
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.ACTION_NAVIGATION_BACK
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory
import org.mariotaku.twidere.provider.TwidereDataStore.Suggestions
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.view.iface.IExtendedView.OnFitSystemWindowsListener

/**
 * Created by mariotaku on 15/1/6.
 */
class QuickSearchBarActivity : BaseActivity(), OnClickListener, LoaderCallbacks<Cursor>,
        OnItemSelectedListener, OnItemClickListener, OnFitSystemWindowsListener,
        SwipeDismissListViewTouchListener.DismissCallbacks {

    private var usersSearchAdapter: SuggestionsAdapter? = null
    private val mSystemWindowsInsets = Rect()
    private var textChanged: Boolean = false

    override fun canDismiss(position: Int): Boolean {
        return usersSearchAdapter!!.getItemViewType(position) == SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY
    }

    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
        val ids = LongArray(reverseSortedPositions.size)
        var i = 0
        val j = reverseSortedPositions.size
        while (i < j) {
            val position = reverseSortedPositions[i]
            val item = usersSearchAdapter!!.getSuggestionItem(position) ?: return
            ids[i] = item._id
            i++
        }
        usersSearchAdapter!!.addRemovedPositions(reverseSortedPositions)
        val cr = contentResolver
        ContentResolverUtils.bulkDelete(cr, SearchHistory.CONTENT_URI, SearchHistory._ID, ids,
                null)
        supportLoaderManager.restartLoader(0, null, this)
    }

    override fun onClick(v: View) {
        when (v) {
            searchSubmit -> {
                doSearch()
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        val accountId = selectedAccountKey
        val builder = Suggestions.Search.CONTENT_URI.buildUpon()
        builder.appendQueryParameter(QUERY_PARAM_QUERY, ParseUtils.parseString(searchQuery.text))
        if (accountId != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountId.toString())
        }
        return CursorLoader(this, builder.build(), Suggestions.Search.COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        usersSearchAdapter!!.changeCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        usersSearchAdapter!!.changeCursor(null)
    }

    override fun onFitSystemWindows(insets: Rect) {
        mSystemWindowsInsets.set(insets)
        updateWindowAttributes()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = usersSearchAdapter!!.getSuggestionItem(position)
        when (usersSearchAdapter!!.getItemViewType(position)) {
            SuggestionsAdapter.VIEW_TYPE_USER_SUGGESTION_ITEM -> {
                IntentUtils.openUserProfile(this, selectedAccountKey,
                        UserKey.valueOf(item!!.extra_id), item.summary, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.DIRECT)
                finish()
            }
            SuggestionsAdapter.VIEW_TYPE_USER_SCREEN_NAME -> {
                IntentUtils.openUserProfile(this, selectedAccountKey, null, item!!.title, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API), Referral.DIRECT)
                finish()
            }
            SuggestionsAdapter.VIEW_TYPE_SAVED_SEARCH, SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY -> {
                IntentUtils.openSearch(this, selectedAccountKey, item!!.title)
                finish()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        supportLoaderManager.restartLoader(0, null, this)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_BACK == action && searchQuery.length() == 0) {
            if (!textChanged) {
                onBackPressed()
            } else {
                textChanged = false
            }
            return true
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_search_bar)
        val accounts = DataStoreUtils.getCredentialsList(this, false)
        val accountsSpinnerAdapter = AccountsSpinnerAdapter(this, R.layout.spinner_item_account_icon)
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_user)
        accountsSpinnerAdapter.addAll(accounts)
        accountSpinner.adapter = accountsSpinnerAdapter
        accountSpinner.onItemSelectedListener = this
        if (savedInstanceState == null) {
            val intent = intent
            val accountKey = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
            var index = -1
            if (accountKey != null) {
                index = accountsSpinnerAdapter.findPositionByKey(accountKey)
            }
            if (index != -1) {
                accountSpinner.setSelection(index)
            }
        }
        mainContent.setOnFitSystemWindowsListener(this)
        usersSearchAdapter = SuggestionsAdapter(this)
        suggestionsList.adapter = usersSearchAdapter
        suggestionsList.onItemClickListener = this

        val listener = SwipeDismissListViewTouchListener(suggestionsList, this)
        suggestionsList.setOnTouchListener(listener)
        suggestionsList.setOnScrollListener(listener.makeScrollListener())
        searchSubmit.setOnClickListener(this)

        EditTextEnterHandler.attach(searchQuery, object : EnterListener {
            override fun shouldCallListener(): Boolean {
                return true
            }

            override fun onHitEnter(): Boolean {
                doSearch()
                return true
            }
        }, true)
        searchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textChanged = true
            }

            override fun afterTextChanged(s: Editable) {
                supportLoaderManager.restartLoader(0, null, this@QuickSearchBarActivity)
            }
        })

        supportLoaderManager.initLoader(0, null, this)
    }

    override fun onResume() {
        super.onResume()
        updateWindowAttributes()
    }

    private fun doSearch() {
        if (isFinishing) return
        val query = ParseUtils.parseString(searchQuery.text)
        if (TextUtils.isEmpty(query)) return
        IntentUtils.openSearch(this, selectedAccountKey, query)
        finish()
    }

    private val selectedAccountKey: UserKey?
        get() {
            val account = accountSpinner.selectedItem as ParcelableAccount ?: return null
            return account.account_key
        }

    private fun updateWindowAttributes() {
        val window = window
        val attributes = window.attributes
        attributes.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        attributes.y = mSystemWindowsInsets.top
        window.attributes = attributes
    }

    private fun setSearchQueryText(query: String?) {
        searchQuery.setText(query)
        if (query == null) return
        searchQuery.setSelection(query.length)
    }

    internal class SuggestionItem(cursor: Cursor, indices: SuggestionsAdapter.Indices) {


        val title: String
        val summary: String
        val _id: Long
        val extra_id: String

        init {
            _id = cursor.getLong(indices._id)
            title = cursor.getString(indices.title)
            summary = cursor.getString(indices.summary)
            extra_id = cursor.getString(indices.extra_id)
        }
    }

    class SuggestionsAdapter internal constructor(private val mActivity: QuickSearchBarActivity) : CursorAdapter(mActivity, null, 0), OnClickListener {

        private val mInflater: LayoutInflater
        private val mMediaLoader: MediaLoaderWrapper
        private val mUserColorNameManager: UserColorNameManager
        private val mRemovedPositions: SortableIntList?

        private var mIndices: Indices? = null

        init {
            mRemovedPositions = SortableIntList()
            mMediaLoader = mActivity.mediaLoader
            mUserColorNameManager = mActivity.userColorNameManager
            mInflater = LayoutInflater.from(mActivity)
        }

        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            when (getActualItemViewType(cursor.position)) {
                VIEW_TYPE_SEARCH_HISTORY, VIEW_TYPE_SAVED_SEARCH -> {
                    val view = mInflater.inflate(R.layout.list_item_suggestion_search, parent, false)
                    val holder = SearchViewHolder(view)
                    holder.edit_query.setOnClickListener(this)
                    view.tag = holder
                    return view
                }
                VIEW_TYPE_USER_SUGGESTION_ITEM, VIEW_TYPE_USER_SCREEN_NAME -> {
                    val view = mInflater.inflate(R.layout.list_item_suggestion_user, parent, false)
                    view.tag = UserViewHolder(view)
                    return view
                }
            }
            throw UnsupportedOperationException("Unknown viewType")
        }

        internal fun getSuggestionItem(position: Int): SuggestionItem? {
            val cursor = getItem(position) as Cursor? ?: return null
            val indices = mIndices ?: return null
            return SuggestionItem(cursor, indices)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            if (mIndices == null) throw NullPointerException()
            when (getActualItemViewType(cursor.position)) {
                VIEW_TYPE_SEARCH_HISTORY -> {
                    val holder = view.tag as SearchViewHolder
                    val title = cursor.getString(mIndices!!.title)
                    holder.edit_query.tag = title
                    holder.text1.text = title
                    holder.icon.setImageResource(R.drawable.ic_action_history)
                }
                VIEW_TYPE_SAVED_SEARCH -> {
                    val holder = view.tag as SearchViewHolder
                    val title = cursor.getString(mIndices!!.title)
                    holder.edit_query.tag = title
                    holder.text1.text = title
                    holder.icon.setImageResource(R.drawable.ic_action_save)
                }
                VIEW_TYPE_USER_SUGGESTION_ITEM -> {
                    val holder = view.tag as UserViewHolder
                    val userKey = UserKey.valueOf(cursor.getString(mIndices!!.extra_id))!!
                    holder.text1.text = mUserColorNameManager.getUserNickname(userKey,
                            cursor.getString(mIndices!!.title))
                    holder.text2.visibility = View.VISIBLE
                    holder.text2.text = String.format("@%s", cursor.getString(mIndices!!.summary))
                    holder.icon.clearColorFilter()
                    mMediaLoader.displayProfileImage(holder.icon, cursor.getString(mIndices!!.icon))
                }
                VIEW_TYPE_USER_SCREEN_NAME -> {
                    val holder = view.tag as UserViewHolder
                    holder.text1.text = String.format("@%s", cursor.getString(mIndices!!.title))
                    holder.text2.visibility = View.GONE
                    holder.icon.setColorFilter(holder.text1.currentTextColor, Mode.SRC_ATOP)
                    mMediaLoader.cancelDisplayTask(holder.icon)
                    holder.icon.setImageResource(R.drawable.ic_action_user)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return getActualItemViewType(getActualPosition(position))
        }

        fun getActualItemViewType(position: Int): Int {
            val cursor = super.getItem(position) as Cursor
            if (cursor == null || mIndices == null) throw NullPointerException()
            when (cursor.getString(mIndices!!.type)) {
                Suggestions.Search.TYPE_SAVED_SEARCH -> {
                    return VIEW_TYPE_SAVED_SEARCH
                }
                Suggestions.Search.TYPE_SCREEN_NAME -> {
                    return VIEW_TYPE_USER_SCREEN_NAME
                }
                Suggestions.Search.TYPE_SEARCH_HISTORY -> {
                    return VIEW_TYPE_SEARCH_HISTORY
                }
                Suggestions.Search.TYPE_USER -> {
                    return VIEW_TYPE_USER_SUGGESTION_ITEM
                }
            }
            return Adapter.IGNORE_ITEM_VIEW_TYPE
        }

        override fun getViewTypeCount(): Int {
            return 4
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.edit_query -> {
                    mActivity.setSearchQueryText(v.tag as String)
                }
            }
        }

        override fun swapCursor(newCursor: Cursor?): Cursor {
            if (newCursor != null) {
                mIndices = Indices(newCursor)
            } else {
                mIndices = null
            }
            mRemovedPositions!!.clear()
            return super.swapCursor(newCursor)
        }

        override fun getCount(): Int {
            if (mRemovedPositions == null) return super.getCount()
            return super.getCount() - mRemovedPositions.size()
        }

        override fun getItem(position: Int): Any {
            return super.getItem(getActualPosition(position))
        }

        override fun getItemId(position: Int): Long {
            return super.getItemId(getActualPosition(position))
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            return super.getView(getActualPosition(position), convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
            return super.getDropDownView(getActualPosition(position), convertView, parent)
        }

        private fun getActualPosition(position: Int): Int {
            if (mRemovedPositions == null) return position
            var skipped = 0
            var i = 0
            val j = mRemovedPositions.size()
            while (i < j) {
                if (position + skipped >= mRemovedPositions.get(i)) {
                    skipped++
                }
                i++
            }
            return position + skipped
        }

        fun addRemovedPositions(positions: IntArray) {
            for (position in positions) {
                mRemovedPositions!!.add(getActualPosition(position))
            }
            mRemovedPositions!!.sort()
            notifyDataSetChanged()
        }

        internal class SearchViewHolder(view: View) {

            internal val icon: ImageView
            internal val text1: TextView
            internal val edit_query: View

            init {
                icon = view.findViewById(android.R.id.icon) as ImageView
                text1 = view.findViewById(android.R.id.text1) as TextView
                edit_query = view.findViewById(R.id.edit_query)
            }

        }

        internal class UserViewHolder(view: View) {

            internal val icon: ImageView
            internal val text1: TextView
            internal val text2: TextView

            init {
                icon = view.findViewById(android.R.id.icon) as ImageView
                text1 = view.findViewById(android.R.id.text1) as TextView
                text2 = view.findViewById(android.R.id.text2) as TextView
            }
        }

        internal class Indices(cursor: Cursor) {
            internal val _id: Int
            internal val type: Int
            internal val title: Int
            internal val summary: Int
            internal val icon: Int
            internal val extra_id: Int

            init {
                _id = cursor.getColumnIndex(Suggestions._ID)
                type = cursor.getColumnIndex(Suggestions.TYPE)
                title = cursor.getColumnIndex(Suggestions.TITLE)
                summary = cursor.getColumnIndex(Suggestions.SUMMARY)
                icon = cursor.getColumnIndex(Suggestions.ICON)
                extra_id = cursor.getColumnIndex(Suggestions.EXTRA_ID)
            }
        }

        companion object {

            internal val VIEW_TYPE_SEARCH_HISTORY = 0
            internal val VIEW_TYPE_SAVED_SEARCH = 1
            internal val VIEW_TYPE_USER_SUGGESTION_ITEM = 2
            internal val VIEW_TYPE_USER_SCREEN_NAME = 3
        }
    }

}
