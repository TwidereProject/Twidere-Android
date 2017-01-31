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
import android.widget.TextView
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_HOST
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.`FiltersData$UserItemCursorIndices`
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
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
                DataStoreUtils.addToFilter(context, listOf(user), false)
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
        var isExtraFeatures: Boolean = false
        val intent = Intent(context, AccountSelectorActivity::class.java)
        intent.putExtra(EXTRA_SINGLE_SELECTION, true)
        intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
        val requestCode = when (item.itemId) {
            R.id.add_user_single, R.id.add_user -> REQUEST_ADD_USER_SELECT_ACCOUNT
            R.id.import_from_blocked_users -> {
                isExtraFeatures = true
                REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT
            }
            R.id.import_from_muted_users -> {
                isExtraFeatures = true
                intent.putExtra(EXTRA_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                REQUEST_IMPORT_MUTES_SELECT_ACCOUNT
            }
            else -> return false
        }

        if (!isExtraFeatures || extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FILTERS_IMPORT)) {
            startActivityForResult(intent, requestCode)
        } else {
            ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                    feature = ExtraFeaturesService.FEATURE_FILTERS_IMPORT,
                    requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
        }
        return true
    }

    override fun onCreateAdapter(context: Context): SimpleCursorAdapter {
        return FilterUsersListAdapter(context)
    }

    override fun addOrEditItem(id: Long, value: String?) {
        // No-op
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

        private var indices: `FiltersData$UserItemCursorIndices`? = null
        private val secondaryTextColor = ThemeUtils.getTextColorSecondary(context)

        init {
            GeneralComponentHelper.build(context).inject(this)
            nameFirst = preferences[nameFirstKey]
        }

        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val icon = view.findViewById(android.R.id.icon)
            val text1 = view.findViewById(android.R.id.text1) as TextView
            val text2 = view.findViewById(android.R.id.text2) as TextView

            icon.visibility = View.GONE

            val userId = UserKey.valueOf(cursor.getString(indices.userKey))
            val name = cursor.getString(indices.name)
            val screenName = cursor.getString(indices.screenName)
            val displayName = userColorNameManager.getDisplayName(userId, name, screenName,
                    nameFirst)
            text1.text = displayName

            val ssb = SpannableStringBuilder(displayName)
            if (cursor.getLong(indices.source) >= 0) {
                val start = ssb.length
                ssb.append("*")
                val end = start + 1
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_action_sync)
                drawable.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_ATOP)
                ssb.setSpan(EmojiSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            text1.text = ssb
            text2.text = userId.host
        }

        override fun swapCursor(c: Cursor?): Cursor? {
            val old = super.swapCursor(c)
            if (c != null) {
                indices = `FiltersData$UserItemCursorIndices`(c)
            }
            return old
        }

        override fun isSelectable(position: Int): Boolean {
            val cursor = this.cursor ?: return false
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(indices!!.source) < 0
            }
            return false
        }
    }

}