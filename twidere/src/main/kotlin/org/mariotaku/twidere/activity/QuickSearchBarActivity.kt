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

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff.Mode
import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.cursoradapter.widget.CursorAdapter
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_quick_search_bar.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_QUERY
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.ACTION_NAVIGATION_BACK
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.appendQueryParameterIgnoreNull
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.SuggestionItem
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory
import org.mariotaku.twidere.provider.TwidereDataStore.Suggestions
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 15/1/6.
 */
class QuickSearchBarActivity : BaseActivity(), OnClickListener, LoaderCallbacks<Cursor?>,
        OnItemSelectedListener, OnItemClickListener, SwipeDismissListViewTouchListener.DismissCallbacks {

    private var textChanged: Boolean = false
    private var hasQrScanner: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasQrScanner = run {
            val scanIntent = Intent(ACTION_ZXING_SCAN)
            scanIntent.putExtra(EXTRA_ZXING_SCAN_MODE, ZXING_SCAN_MODE_QR_CODE)
            return@run scanIntent.resolveActivity(packageManager) != null
        }

        setContentView(R.layout.activity_quick_search_bar)

        promotionService.setupBanner(adContainer, PromotionService.BannerType.QUICK_SEARCH,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))

        val am = AccountManager.get(this)
        val accounts = AccountUtils.getAllAccountDetails(am, AccountUtils.getAccounts(am), true).toList()
        val accountsSpinnerAdapter = AccountsSpinnerAdapter(this, R.layout.spinner_item_account_icon,
                requestManager = requestManager)
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_simple_user)
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
        ViewCompat.setOnApplyWindowInsetsListener(mainContent, this)
        mainContent.setOnClickListener {
            finish()
        }
        suggestionsList.adapter = SuggestionsAdapter(this)
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
                updateSubmitButton()
            }

            override fun afterTextChanged(s: Editable) {
                supportLoaderManager.restartLoader(0, null, this@QuickSearchBarActivity)
            }
        })

        if (savedInstanceState == null) {
            searchQuery.setText(intent.getStringExtra(EXTRA_QUERY))
            searchQuery.setSelection(searchQuery.length())
        }

        LoaderManager.getInstance(this).initLoader(0, null, this)

        updateSubmitButton()
        promotionService.loadBanner(adContainer)
    }

    override fun canDismiss(position: Int): Boolean {
        val adapter = suggestionsList.adapter as SuggestionsAdapter
        return adapter.getItemViewType(position) == SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY
    }

    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
        val adapter = suggestionsList.adapter as SuggestionsAdapter
        val ids = LongArray(reverseSortedPositions.size)
        for (i in reverseSortedPositions.indices) {
            val position = reverseSortedPositions[i]
            val item = adapter.getSuggestionItem(position) ?: return
            ids[i] = item._id
        }
        adapter.addRemovedPositions(reverseSortedPositions)
        ContentResolverUtils.bulkDelete(contentResolver, SearchHistory.CONTENT_URI, SearchHistory._ID,
                false, ids, null, null)
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    override fun onClick(v: View) {
        when (v) {
            searchSubmit -> {
                if (searchQuery.empty && hasQrScanner) {
                    val currentFocus = currentFocus
                    if (currentFocus === searchQuery) {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                        currentFocus.clearFocus()
                    }

                    val scanIntent = Intent(ACTION_ZXING_SCAN)
                    scanIntent.putExtra(EXTRA_ZXING_SCAN_MODE, ZXING_SCAN_MODE_QR_CODE)
                    try {
                        startActivityForResult(scanIntent, REQUEST_SCAN_QR)
                    } catch (e: ActivityNotFoundException) {
                        // Ignore
                        Toast.makeText(this, R.string.message_toast_qr_scanner_not_supported,
                                Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        // Goddamned SAMSUNG again!!!
                        Toast.makeText(this, R.string.message_toast_qr_scanner_not_supported,
                                Toast.LENGTH_SHORT).show()
                    }
                } else {
                    doSearch()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val scanResult = data.getStringExtra(EXTRA_ZXING_SCAN_RESULT) ?: run {
                        Toast.makeText(this, R.string.message_toast_qr_scanner_not_supported,
                                Toast.LENGTH_SHORT).show()
                        return
                    }
                    val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(scanResult)).apply {
                        `package` = BuildConfig.APPLICATION_ID
                        putExtra(EXTRA_ACCOUNT_KEY, selectedAccountDetails?.key)
                    }
                    val componentName = viewIntent.resolveActivity(packageManager) ?: run {
                        Toast.makeText(this, R.string.message_toast_qr_scan_link_not_supported,
                                Toast.LENGTH_SHORT).show()
                        return
                    }
                    viewIntent.component = componentName
                    startActivity(viewIntent)
                    finish()
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val account = selectedAccountDetails
        val builder = Suggestions.Search.CONTENT_URI.buildUpon()
        builder.appendQueryParameter(QUERY_PARAM_QUERY, ParseUtils.parseString(searchQuery.text))
        if (account != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, account.key.toString())
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_TYPE, account.type)
            if (account.type != AccountType.MASTODON) {
                builder.appendQueryParameterIgnoreNull(QUERY_PARAM_ACCOUNT_HOST, account.key.host)
            }
        }
        return CursorLoader(this, builder.build(), Suggestions.Search.COLUMNS, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        val adapter = suggestionsList.adapter as SuggestionsAdapter
        adapter.changeCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        val adapter = suggestionsList.adapter as SuggestionsAdapter
        adapter.changeCursor(null)
    }


    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        super.onApplyWindowInsets(v, insets)
        updateWindowAttributes()
        return insets
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val adapter = suggestionsList.adapter as SuggestionsAdapter
        val item = adapter.getSuggestionItem(position) ?: return
        val details = selectedAccountDetails ?: return
        when (adapter.getItemViewType(position)) {
            SuggestionsAdapter.VIEW_TYPE_USER_SUGGESTION_ITEM -> {
                IntentUtils.openUserProfile(this, details.key,
                        UserKey.valueOf(item.extra_id!!), item.summary, null,
                        preferences[newDocumentApiKey], null)
                finish()
            }
            SuggestionsAdapter.VIEW_TYPE_USER_SCREEN_NAME -> {
                IntentUtils.openUserProfile(this, details.key, null, item.title,
                        null, preferences[newDocumentApiKey], null)
                finish()
            }
            SuggestionsAdapter.VIEW_TYPE_SAVED_SEARCH, SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY -> {
                val query = item.title ?: return
                IntentUtils.openSearch(this, details.key, query)
                setResult(RESULT_SEARCH_PERFORMED)
                finish()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        LoaderManager.getInstance(this).restartLoader(0, null, this)
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

    override fun onResume() {
        super.onResume()
        updateWindowAttributes()
    }

    private fun doSearch() {
        if (isFinishing) return
        val query = ParseUtils.parseString(searchQuery.text)
        if (TextUtils.isEmpty(query)) return
        val details = selectedAccountDetails ?: return
        IntentUtils.openSearch(this, details.key, query)
        setResult(RESULT_SEARCH_PERFORMED)
        finish()
    }

    private val selectedAccountDetails: AccountDetails?
        get() {
            return accountSpinner.selectedItem as? AccountDetails
        }

    private fun updateWindowAttributes() {
        val window = window ?: return
        val attributes = window.attributes
        attributes.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        attributes.y = systemWindowsInsets?.top ?: 0
        window.attributes = attributes
    }

    private fun setSearchQueryText(query: String?) {
        searchQuery.setText(query)
        if (query == null) return
        searchQuery.setSelection(query.length)
    }

    private fun updateSubmitButton() {
        if (searchQuery.empty && hasQrScanner) {
            searchSubmit.setImageResource(R.drawable.ic_action_qr_scan)
        } else {
            searchSubmit.setImageResource(R.drawable.ic_action_search)
        }
    }

    class SuggestionsAdapter internal constructor(
            private val activity: QuickSearchBarActivity
    ) : CursorAdapter(activity, null, 0), OnClickListener {

        private val profileImageStyle = activity.preferences[profileImageStyleKey]
        private val profileImageSize = activity.getString(R.string.profile_image_size)
        private val requestManager = activity.requestManager
        private val inflater = LayoutInflater.from(activity)
        private val userColorNameManager = activity.userColorNameManager
        private val removedPositions = ArrayList<Int>()

        private var indices: SuggestionItem.Indices? = null

        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            when (getActualItemViewType(cursor.position)) {
                VIEW_TYPE_SEARCH_HISTORY, VIEW_TYPE_SAVED_SEARCH -> {
                    val view = inflater.inflate(R.layout.list_item_suggestion_search, parent, false)
                    val holder = SearchViewHolder(view)
                    holder.edit_query.setOnClickListener(this)
                    view.tag = holder
                    return view
                }
                VIEW_TYPE_USER_SUGGESTION_ITEM, VIEW_TYPE_USER_SCREEN_NAME -> {
                    val view = inflater.inflate(R.layout.list_item_suggestion_user, parent, false)
                    view.tag = UserViewHolder(view).apply {
                        icon.style = profileImageStyle
                    }
                    return view
                }
            }
            throw UnsupportedOperationException("Unknown viewType")
        }

        internal fun getSuggestionItem(position: Int): SuggestionItem? {
            val cursor = (getItem(position) ?: return null) as Cursor
            val indices = indices ?: return null
            return SuggestionItem(cursor, indices)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val indices = indices!!
            when (getActualItemViewType(cursor.position)) {
                VIEW_TYPE_SEARCH_HISTORY -> {
                    val holder = view.tag as SearchViewHolder
                    val title = cursor.getString(indices.title)
                    holder.edit_query.tag = title
                    holder.text1.spannable = title
                    holder.icon.setImageResource(R.drawable.ic_action_history)
                }
                VIEW_TYPE_SAVED_SEARCH -> {
                    val holder = view.tag as SearchViewHolder
                    val title = cursor.getString(indices.title)
                    holder.edit_query.tag = title
                    holder.text1.spannable = title
                    holder.icon.setImageResource(R.drawable.ic_action_save)
                }
                VIEW_TYPE_USER_SUGGESTION_ITEM -> {
                    val holder = view.tag as UserViewHolder
                    val userKey = UserKey.valueOf(cursor.getString(indices.extra_id))
                    holder.text1.spannable = userColorNameManager.getUserNickname(userKey,
                            cursor.getString(indices.title))
                    holder.text2.visibility = View.VISIBLE
                    holder.text2.spannable = "@${cursor.getString(indices.summary)}"
                    holder.icon.clearColorFilter()
                    requestManager.loadProfileImage(context, cursor.getString(indices.icon),
                            profileImageStyle, cornerRadius = holder.icon.cornerRadius,
                            cornerRadiusRatio = holder.icon.cornerRadiusRatio,
                            size = profileImageSize).into(holder.icon)
                }
                VIEW_TYPE_USER_SCREEN_NAME -> {
                    val holder = view.tag as UserViewHolder
                    holder.text1.spannable = "@${cursor.getString(indices.title)}"
                    holder.text2.visibility = View.GONE
                    holder.icon.setColorFilter(holder.text1.currentTextColor, Mode.SRC_ATOP)
                    //TODO cancel image load
                    holder.icon.setImageResource(R.drawable.ic_action_user)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return getActualItemViewType(getActualPosition(position))
        }

        fun getActualItemViewType(position: Int): Int {
            val cursor = super.getItem(position) as Cursor
            if (indices == null) throw NullPointerException()
            when (cursor.getString(indices!!.type)) {
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
                    activity.setSearchQueryText(v.tag as String)
                }
            }
        }

        override fun swapCursor(newCursor: Cursor?): Cursor? {
            indices = newCursor?.let(SuggestionItem::Indices)
            removedPositions.clear()
            return super.swapCursor(newCursor)
        }

        override fun getCount(): Int {
            return super.getCount() - removedPositions.size
        }

        override fun getItem(position: Int): Any? {
            return super.getItem(getActualPosition(position))
        }

        override fun getItemId(position: Int): Long {
            return super.getItemId(getActualPosition(position))
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return super.getView(getActualPosition(position), convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return super.getDropDownView(getActualPosition(position), convertView, parent)
        }

        private fun getActualPosition(position: Int): Int {
            var skipped = 0
            for (i in 0 until removedPositions.size) {
                if (position + skipped >= removedPositions[i]) {
                    skipped++
                }
            }
            return position + skipped
        }

        fun addRemovedPositions(positions: IntArray) {
            for (position in positions) {
                removedPositions.add(getActualPosition(position))
            }
            removedPositions.sort()
            notifyDataSetChanged()
        }

        internal class SearchViewHolder(view: View) {

            internal val icon: ImageView = view.findViewById(android.R.id.icon)
            internal val text1: TextView = view.findViewById(android.R.id.text1)
            internal val edit_query: View = view.findViewById(R.id.edit_query)

        }

        internal class UserViewHolder(view: View) {

            internal val icon: ProfileImageView = view.findViewById(android.R.id.icon)
            internal val text1: TextView = view.findViewById(android.R.id.text1)
            internal val text2: TextView = view.findViewById(android.R.id.text2)

        }

        companion object {

            internal const val VIEW_TYPE_SEARCH_HISTORY = 0
            internal const val VIEW_TYPE_SAVED_SEARCH = 1
            internal const val VIEW_TYPE_USER_SUGGESTION_ITEM = 2
            internal const val VIEW_TYPE_USER_SCREEN_NAME = 3
        }
    }

    companion object {
        const val ACTION_ZXING_SCAN = "com.google.zxing.client.android.SCAN"
        const val EXTRA_ZXING_SCAN_MODE = "SCAN_MODE"
        const val EXTRA_ZXING_SCAN_RESULT = "SCAN_RESULT"
        const val ZXING_SCAN_MODE_QR_CODE = "QR_CODE_MODE"
        const val REQUEST_SCAN_QR = 101

        const val RESULT_SEARCH_PERFORMED = 2
    }
}
