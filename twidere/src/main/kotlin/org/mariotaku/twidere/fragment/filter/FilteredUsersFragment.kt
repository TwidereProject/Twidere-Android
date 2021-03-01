package org.mariotaku.twidere.fragment.filter

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.cursoradapter.widget.SimpleCursorAdapter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_listview.*
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_HOST
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.dismissProgressDialog
import org.mariotaku.twidere.extension.showProgressDialog
import org.mariotaku.twidere.fragment.AddUserFilterDialogFragment
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.task.CreateUserMuteTask
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.IntentUtils
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER) ?: return
                executeAfterFragmentResumed { fragment ->
                    AddUserFilterDialogFragment.show(fragment.childFragmentManager, user)
                }
            }
            REQUEST_IMPORT_BLOCKS_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = IntentUtils.UriBuilder(AUTHORITY_FILTERS).path(PATH_FILTERS_IMPORT_BLOCKS).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_IMPORT_MUTES_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(context, LinkHandlerActivity::class.java)
                intent.data = IntentUtils.UriBuilder(AUTHORITY_FILTERS).path(PATH_FILTERS_IMPORT_MUTES).build()
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivity(intent)
            }
            REQUEST_ADD_USER_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val intent = Intent(INTENT_ACTION_SELECT_USER)
                context?.let { intent.setClass(it, UserSelectorActivity::class.java) }
                intent.putExtra(EXTRA_ACCOUNT_KEY, data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY))
                startActivityForResult(intent, REQUEST_SELECT_USER)
            }
            REQUEST_EXPORT_MUTES_SELECT_ACCOUNT -> {
                if (resultCode != FragmentActivity.RESULT_OK || data == null) return
                val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY) ?: return
                val userKeys = data.getBundleExtra(EXTRA_EXTRAS)?.getNullableTypedArray<UserKey>(EXTRA_ITEMS) ?: return
                exportToMutedUsers(accountKey, userKeys)
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        return CursorLoader(requireActivity(), contentUri, contentColumns, null, null, sortOrder)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_users, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isFeaturesSupported = extraFeaturesService.isSupported()
        menu.setGroupAvailability(R.id.import_export, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(context, AccountSelectorActivity::class.java)
        intent.putExtra(EXTRA_SINGLE_SELECTION, true)
        intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
        val requestCode = when (item.itemId) {
            R.id.add_user -> REQUEST_ADD_USER_SELECT_ACCOUNT
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

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        super.onCreateActionMode(mode, menu)
        mode.menuInflater.inflate(R.menu.action_multi_select_filtered_users, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val result = super.onPrepareActionMode(mode, menu)
        val isFeaturesSupported = extraFeaturesService.isSupported()
        menu.setGroupAvailability(R.id.import_export, true)
        return result && menu.hasVisibleItems()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.export_to_muted_users -> {
                requestExportToMutes()
                return true
            }
        }
        return super.onActionItemClicked(mode, item)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): SimpleCursorAdapter {
        return FilterUsersListAdapter(context)
    }

    override fun onItemClick(position: Int) {
        val adapter = this.adapter as FilterUsersListAdapter
        val item = adapter.getFilterItem(position) ?: return
        if (item.source >= 0) return
        addOrEditItem(item.id, userColorNameManager.getDisplayName(item,
                preferences[nameFirstKey]), item.scope)
    }

    override fun addOrEditItem(id: Long, value: String?, scope: Int) {
        // No-op
        if (id < 0) return
        super.addOrEditItem(id, value, scope)
    }

    override fun performDeletion() {
        val context = context ?: return
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

    private fun requestExportToMutes() {
        val adapter = this.adapter as? FilterUsersListAdapter ?: return
        val checkedPos = listView.checkedItemPositions
        val userKeys = (0 until checkedPos.size()).mapNotNull { i ->
            if (checkedPos.valueAt(i)) {
                return@mapNotNull adapter.getUserKey(checkedPos.keyAt(i))
            }
            return@mapNotNull null
        }.toTypedArray()
        val intent = Intent(context, AccountSelectorActivity::class.java)
        intent.putExtra(EXTRA_SINGLE_SELECTION, true)
        intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
        intent.putExtra(EXTRA_EXTRAS, Bundle {
            this[EXTRA_ITEMS] = userKeys
        })
        startActivityForResult(intent, REQUEST_EXPORT_MUTES_SELECT_ACCOUNT)
    }

    private fun exportToMutedUsers(accountKey: UserKey, items: Array<UserKey>) {
        val weakThis = this.weak()
        showProgressDialog("export_to_muted").then {
            val fragment = weakThis.get() ?: throw InterruptedException()
            val am = AccountManager.get(fragment.context)
            val account = AccountUtils.getAccountDetails(am, accountKey, true) ?:
                    throw AccountNotFoundException()
            CreateUserMuteTask.muteUsers(fragment.requireContext(), account, items)
        }.alwaysUi {
            weakThis.get()?.dismissProgressDialog("export_to_muted")
        }
    }

    class FilterUsersListAdapter(
            context: Context
    ) : SimpleCursorAdapter(context, R.layout.list_item_two_line, null,
            emptyArray(), IntArray(0), 0), IFilterAdapter {

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
                ContextCompat.getDrawable(context, R.drawable.ic_action_sync) ?.let { drawable ->
                    drawable.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_ATOP)
                    ssb.setSpan(EmojiSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            text1.spannable = ssb
            text2.spannable = userKey.host
        }

        override fun swapCursor(c: Cursor?): Cursor? {
            indices = c?.let { ObjectCursor.indicesFrom(it, FiltersData.UserItem::class.java) }
            return super.swapCursor(c)
        }

        override fun isReadOnly(position: Int): Boolean {
            val cursor = this.cursor ?: return false
            val indices = this.indices ?: return false
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(indices[Filters.Users.SOURCE]) >= 0
            }
            return false
        }

        fun getUserKeyString(position: Int): String? {
            val cursor = this.cursor ?: return null
            val indices = this.indices ?: return null
            if (cursor.moveToPosition(position)) {
                return cursor.getString(indices[Filters.Users.USER_KEY])
            }
            return null
        }

        fun getUserKey(position: Int): UserKey? {
            val cursor = this.cursor ?: return null
            val indices = this.indices ?: return null
            if (cursor.moveToPosition(position)) {
                return cursor.getString(indices[Filters.Users.USER_KEY])?.let(UserKey::valueOf)
            }
            return null
        }

        fun getFilterItem(position: Int): FiltersData.UserItem? {
            val cursor = this.cursor ?: return null
            val indices = this.indices ?: return null
            if (cursor.moveToPosition(position)) {
                return indices.newObject(cursor)
            }
            return null
        }
    }

}