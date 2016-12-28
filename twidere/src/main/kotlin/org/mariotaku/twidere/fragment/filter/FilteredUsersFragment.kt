package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.SimpleCursorAdapter
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_HOST
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.`FiltersData$UserItemCursorIndices`
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.premium.ExtraFeaturesChecker
import javax.inject.Inject

class FilteredUsersFragment : BaseFiltersFragment() {

    private lateinit var extraFeaturesChecker: ExtraFeaturesChecker

    public override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Users.COLUMNS

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Users.CONTENT_URI

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        extraFeaturesChecker = ExtraFeaturesChecker.newInstance(context)
    }

    override fun onDestroy() {
        extraFeaturesChecker.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                if (resultCode != FragmentActivity.RESULT_OK) return
                val user = data!!.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                val values = ContentValuesCreator.createFilteredUser(user)
                val resolver = context.contentResolver
                val where = Expression.equalsArgs(TwidereDataStore.Filters.Users.USER_KEY).sql
                val whereArgs = arrayOf(user.key.toString())
                resolver.delete(TwidereDataStore.Filters.Users.CONTENT_URI, where, whereArgs)
                resolver.insert(TwidereDataStore.Filters.Users.CONTENT_URI, values)
            }
            REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = Uri.Builder().scheme(SCHEME_TWIDERE).authority(AUTHORITY_FILTERS_IMPORT_BLOCKS).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data!!.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_IMPORT_MUTES_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = Uri.Builder().scheme(SCHEME_TWIDERE).authority(AUTHORITY_FILTERS_IMPORT_MUTES).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data!!.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_ADD_USER_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK) return
                val intent = Intent(INTENT_ACTION_SELECT_USER)
                intent.setClass(context, UserListSelectorActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, data!!.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivityForResult(intent, REQUEST_ADD_USER_SELECT_ACCOUNT)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_users, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isFeaturesSupported = extraFeaturesChecker.isSupported()
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

        if (!isExtraFeatures || extraFeaturesChecker.isEnabled()) {
            startActivityForResult(intent, requestCode)
        } else {
            val df = ExtraFeaturesIntroductionDialogFragment()
            df.show(childFragmentManager, "extra_features_introduction")
        }
        return true
    }

    override fun onCreateAdapter(context: Context): SimpleCursorAdapter {
        return FilterUsersListAdapter(context)
    }

    class FilterUsersListAdapter(
            context: Context
    ) : SimpleCursorAdapter(context, R.layout.simple_list_item_activated_2, null,
            emptyArray(), IntArray(0), 0) {

        @Inject
        lateinit var userColorNameManager: UserColorNameManager
        @Inject
        lateinit var preferences: KPreferences

        private val nameFirst: Boolean

        private var indices: `FiltersData$UserItemCursorIndices`? = null

        init {
            GeneralComponentHelper.build(context).inject(this)
            nameFirst = preferences[nameFirstKey]
        }

        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val text1 = view.findViewById(android.R.id.text1) as TextView
            val text2 = view.findViewById(android.R.id.text2) as TextView
            val userId = UserKey.valueOf(cursor.getString(indices.userKey))
            val name = cursor.getString(indices.name)
            val screenName = cursor.getString(indices.screenName)
            val displayName = userColorNameManager.getDisplayName(userId, name, screenName,
                    nameFirst)
            text1.text = displayName
            text2.text = userId.host
        }

        override fun swapCursor(c: Cursor?): Cursor? {
            val old = super.swapCursor(c)
            if (c != null) {
                indices = `FiltersData$UserItemCursorIndices`(c)
            }
            return old
        }

    }

}