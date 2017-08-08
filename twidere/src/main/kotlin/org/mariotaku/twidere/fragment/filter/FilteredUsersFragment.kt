package org.mariotaku.twidere.fragment.filter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SimpleCursorAdapter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.ktextension.spannable
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_HOST
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.fragment.AddUserFilterDialogFragment
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

class FilteredUsersFragment : BaseFiltersFragment() {

    override val contentUri: Uri = Filters.Users.CONTENT_URI
    override val contentColumns: Array<String> = Filters.Users.COLUMNS
    override val sortOrder: String? = "${Filters.Users.SOURCE} >= 0"
    override val supportsEdit: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                executeAfterFragmentResumed { fragment ->
                    AddUserFilterDialogFragment.show(fragment.childFragmentManager, user)
                }
            }
            REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = Uri.Builder().scheme(SCHEME_TWIDERE).authority(AUTHORITY_FILTERS).path(PATH_FILTERS_IMPORT_BLOCKS).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_IMPORT_MUTES_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = Uri.Builder().scheme(SCHEME_TWIDERE).authority(AUTHORITY_FILTERS).path(PATH_FILTERS_IMPORT_MUTES).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_ADD_USER_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(INTENT_ACTION_SELECT_USER)
                intent.setClass(context, UserSelectorActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivityForResult(intent, REQUEST_SELECT_USER)
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_users, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isFeaturesSupported = extraFeaturesService.isSupported()
        menu.setItemAvailability(R.id.add_user_single, !isFeaturesSupported)
        menu.setItemAvailability(R.id.add_user_submenu, isFeaturesSupported)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(context, AccountSelectorActivity::class.java)
        intent.putExtra(EXTRA_SINGLE_SELECTION, true)
        intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
        val requestCode = when (item.itemId) {
            R.id.add_user_single, R.id.add_user -> REQUEST_ADD_USER_SELECT_ACCOUNT
            R.id.import_from_blocked_users -> {
                REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT
            }
            R.id.import_from_muted_users -> {
                intent.putExtra(EXTRA_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                REQUEST_IMPORT_MUTES_SELECT_ACCOUNT
            }
            else -> return false
        }

        startActivityForResult(intent, requestCode)
        return true
    }

    override fun onCreateAdapter(context: Context): SimpleCursorAdapter {
        return FilterUsersListAdapter(context)
    }

    override fun addOrEditItem(id: Long, value: String?) {
        // No-op
    }

    override fun performDeletion() {
        val positions = listView.checkedItemPositions
        val keys = (0 until positions.size()).mapNotNull {
            if (!positions.valueAt(it)) return@mapNotNull null
            return@mapNotNull (adapter as FilterUsersListAdapter).getUserKeyString(positions.keyAt(it))
        }
        super.performDeletion()
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Keywords.CONTENT_URI,
                Filters.USER_KEY, false, keys, null, null)
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Links.CONTENT_URI,
                Filters.USER_KEY, false, keys, null, null)
    }

    class FilterUsersListAdapter(
            context: Context
    ) : SimpleCursorAdapter(context, R.layout.list_item_two_line, null,
            emptyArray(), IntArray(0), 0), SelectableItemAdapter {

        @Inject
        lateinit var userColorNameManager: UserColorNameManager
        @Inject
        lateinit var preferences: KPreferences

        private val nameFirst: Boolean

        private var indices: ObjectCursor.CursorIndices<FiltersData.UserItem>? = null
        private val secondaryTextColor = ThemeUtils.getTextColorSecondary(context)

        init {
            GeneralComponent.get(context).inject(this)
            nameFirst = preferences[nameFirstKey]
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val icon = view.findViewById<ImageView>(android.R.id.icon)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            icon.visibility = View.GONE

            val userKey = UserKey.valueOf(cursor.getString(indices[Filters.Users.USER_KEY]))
            val name = cursor.getString(indices[Filters.Users.NAME])
            val screenName = cursor.getString(indices[Filters.Users.SCREEN_NAME])
            val displayName = userColorNameManager.getDisplayName(userKey, name, screenName,
                    nameFirst)
            text1.spannable = displayName

            val ssb = SpannableStringBuilder(displayName)
            if (cursor.getLong(indices[Filters.Users.SOURCE]) >= 0) {
                val start = ssb.length
                ssb.append("*")
                val end = start + 1
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_action_sync)
                drawable.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_ATOP)
                ssb.setSpan(EmojiSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            text1.spannable = ssb
            text2.spannable = userKey.host
        }

        override fun swapCursor(c: Cursor?): Cursor? {
            indices = c?.let { ObjectCursor.indicesFrom(it, FiltersData.UserItem::class.java) }
            return super.swapCursor(c)
        }

        override fun isSelectable(position: Int): Boolean {
            val cursor = this.cursor ?: return false
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(indices!![Filters.Users.SOURCE]) < 0
            }
            return false
        }

        fun getUserKeyString(position: Int): String? {
            val cursor = this.cursor ?: return null
            if (cursor.moveToPosition(position)) {
                return cursor.getString(indices!![Filters.Users.USER_KEY])
            }
            return null
        }
    }

}