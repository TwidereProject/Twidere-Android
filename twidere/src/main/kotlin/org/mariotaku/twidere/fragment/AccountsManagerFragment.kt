package org.mariotaku.twidere.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.util.SimpleArrayMap
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import com.mobeta.android.dslv.DragSortListView.DropListener
import kotlinx.android.synthetic.main.layout_draggable_list_with_empty_view.*
import org.mariotaku.sqliteqb.library.ArgsArray
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.SignInActivity
import org.mariotaku.twidere.adapter.AccountsAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Outbox
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.TwidereCollectionUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.collection.CompactHashSet

/**
 * Created by mariotaku on 14/10/26.
 */
class AccountsManagerFragment : BaseSupportFragment(), LoaderCallbacks<Cursor?>, DropListener, OnSharedPreferenceChangeListener, AdapterView.OnItemClickListener, AccountsAdapter.OnAccountToggleListener {

    private var adapter: AccountsAdapter? = null
    private var selectedAccount: ParcelableAccount? = null
    private val activatedState = SimpleArrayMap<UserKey, Boolean>()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val activity = activity
        preferences.registerOnSharedPreferenceChangeListener(this)
        adapter = AccountsAdapter(activity)
        Utils.configBaseAdapter(activity, adapter)
        adapter!!.setSortEnabled(true)
        adapter!!.setSwitchEnabled(true)
        adapter!!.setOnAccountToggleListener(this)
        listView.adapter = adapter
        listView.isDragEnabled = true
        listView.setDropListener(this)
        listView.onItemClickListener = this
        listView.setOnCreateContextMenuListener(this)
        listView.emptyView = emptyView
        emptyText.setText(R.string.no_account)
        emptyIcon.setImageResource(R.drawable.ic_info_error_generic)
        loaderManager.initLoader(0, null, this)
        setListShown(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                if (resultCode != Activity.RESULT_OK || data == null || selectedAccount == null)
                    return
                val values = ContentValues()
                values.put(Accounts.COLOR, data.getIntExtra(EXTRA_COLOR, Color.WHITE))
                val where = Expression.equalsArgs(Accounts.ACCOUNT_KEY)
                val whereArgs = arrayOf(selectedAccount!!.account_key.toString())
                val cr = contentResolver
                cr.update(Accounts.CONTENT_URI, values, where.sql, whereArgs)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_account -> {
                val intent = Intent(INTENT_ACTION_TWITTER_LOGIN)
                intent.setClass(activity, SignInActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_accounts_manager, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val menuInfo = item!!.menuInfo
        if (menuInfo !is AdapterContextMenuInfo) return false
        val account = adapter!!.getAccount(menuInfo.position)
        selectedAccount = account
        if (account == null) return false
        when (item.itemId) {
            R.id.set_color -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, account.color)
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                startActivityForResult(intent, REQUEST_SET_COLOR)
            }
            R.id.delete -> {
                val f = AccountDeletionDialogFragment()
                val args = Bundle()
                args.putLong(EXTRA_ID, account.id)
                f.arguments = args
                f.show(childFragmentManager, FRAGMENT_TAG_ACCOUNT_DELETION)
            }
        }
        return false
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val context = context ?: return
        val account = adapter!!.getAccount(position)
        if (account!!.account_user != null) {
            IntentUtils.openUserProfile(context, account.account_user!!, null,
                    preferences.getBoolean(SharedPreferenceConstants.KEY_NEW_DOCUMENT_API),
                    Referral.SELF_PROFILE)
        } else {
            IntentUtils.openUserProfile(context, account.account_key, account.account_key,
                    account.screen_name, null, preferences.getBoolean(SharedPreferenceConstants.KEY_NEW_DOCUMENT_API),
                    Referral.SELF_PROFILE)
        }
    }

    override fun onStop() {
        super.onStop()
        saveActivatedState()
    }

    private fun saveActivatedState() {
        val trueIds = CompactHashSet<UserKey>()
        val falseIds = CompactHashSet<UserKey>()
        for (i in 0 until activatedState.size()) {
            if (activatedState.valueAt(i)) {
                trueIds.add(activatedState.keyAt(i))
            } else {
                falseIds.add(activatedState.keyAt(i))
            }
        }
        val cr = contentResolver
        val values = ContentValues()
        values.put(Accounts.IS_ACTIVATED, true)
        var where = Expression.`in`(Columns.Column(Accounts.ACCOUNT_KEY), ArgsArray(trueIds.size))
        var whereArgs = TwidereCollectionUtils.toStringArray(trueIds)
        cr.update(Accounts.CONTENT_URI, values, where.sql, whereArgs)
        values.put(Accounts.IS_ACTIVATED, false)
        where = Expression.`in`(Columns.Column(Accounts.ACCOUNT_KEY), ArgsArray(falseIds.size))
        whereArgs = TwidereCollectionUtils.toStringArray(falseIds)
        cr.update(Accounts.CONTENT_URI, values, where.sql, whereArgs)
    }

    override fun onAccountToggle(accountId: UserKey, state: Boolean) {
        activatedState.put(accountId, state)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        if (menuInfo !is AdapterContextMenuInfo) return
        val account = adapter!!.getAccount(menuInfo.position)
        menu.setHeaderTitle(account!!.name)
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.action_manager_account, menu)
    }

    override fun onDestroyView() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false)
    }

    private fun setListShown(shown: Boolean) {
        listContainer.visibility = if (shown) View.VISIBLE else View.GONE
        progressContainer.visibility = if (shown) View.GONE else View.VISIBLE
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val uri = Accounts.CONTENT_URI
        return CursorLoader(activity, uri, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        setListShown(true)
        adapter!!.changeCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter!!.changeCursor(null)
    }

    override fun drop(from: Int, to: Int) {
        adapter!!.drop(from, to)
        if (listView.choiceMode != AbsListView.CHOICE_MODE_NONE) {
            listView.moveCheckState(from, to)
        }
        saveAccountPositions()
    }

    private fun saveAccountPositions() {
        val cr = contentResolver
        val positions = adapter!!.cursorPositions
        val c = adapter!!.cursor
        if (positions != null && c != null && !c.isClosed) {
            val idIdx = c.getColumnIndex(Accounts._ID)
            for (i in 0 until positions.size) {
                c.moveToPosition(positions[i])
                val id = c.getLong(idIdx)
                val values = ContentValues()
                values.put(Accounts.SORT_POSITION, i)
                val where = Expression.equals(Accounts._ID, id)
                cr.update(Accounts.CONTENT_URI, values, where.sql, null)
            }
        }
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (SharedPreferenceConstants.KEY_DEFAULT_ACCOUNT_KEY == key) {
            updateDefaultAccount()
        }
    }

    private fun updateDefaultAccount() {
        adapter!!.notifyDataSetChanged()
    }

    class AccountDeletionDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val id = arguments.getLong(EXTRA_ID)
            val resolver = contentResolver
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val where = Expression.equalsArgs(Accounts._ID).sql
                    val whereArgs = arrayOf(id.toString())
                    resolver.delete(Accounts.CONTENT_URI, where, whereArgs)
                    // Also delete tweets related to the account we previously
                    // deleted.
                    resolver.delete(Statuses.CONTENT_URI, where, whereArgs)
                    resolver.delete(Mentions.CONTENT_URI, where, whereArgs)
                    resolver.delete(Inbox.CONTENT_URI, where, whereArgs)
                    resolver.delete(Outbox.CONTENT_URI, where, whereArgs)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context
            val builder = AlertDialog.Builder(context)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setTitle(R.string.account_delete_confirm_title)
            builder.setMessage(R.string.account_delete_confirm_message)
            return builder.create()
        }

    }

    companion object {

        private val FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion"
    }
}
