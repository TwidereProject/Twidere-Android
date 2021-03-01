package org.mariotaku.twidere.fragment

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.appcompat.app.AlertDialog
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import kotlinx.android.synthetic.main.layout_draggable_list_with_empty_view.*
import nl.komponents.kovenant.task
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_AUTH_TOKEN_TYPE
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.adapter.AccountDetailsAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.getAccountKey
import org.mariotaku.twidere.extension.model.setActivated
import org.mariotaku.twidere.extension.model.setColor
import org.mariotaku.twidere.extension.model.setPosition
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.loader.AccountDetailsLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.deleteAccountData
import org.mariotaku.twidere.util.support.removeAccountSupport

/**
 * Sort and toggle account availability
 * Created by mariotaku on 14/10/26.
 */
class AccountsManagerFragment : BaseFragment(), LoaderManager.LoaderCallbacks<List<AccountDetails>>,
        AdapterView.OnItemClickListener {

    private lateinit var adapter: AccountDetailsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val am = AccountManager.get(context)
        adapter = AccountDetailsAdapter(requireContext(), requestManager).apply {
            sortEnabled = true
            switchEnabled = true
            accountToggleListener = { pos, checked ->
                val item = getItem(pos)
                item.activated = checked
                item.account.setActivated(am, checked)
            }
        }
        listView.adapter = adapter
        listView.isDragEnabled = true
        listView.onItemClickListener = this
        listView.setDropListener { from, to ->
            adapter.drop(from, to)
            for (i in 0 until adapter.count) {
                val item = adapter.getItem(i)
                item.account.setActivated(am, item.activated)
                item.account.setPosition(am, i)
            }
        }
        listView.setOnCreateContextMenuListener(this)
        listView.emptyView = emptyView
        emptyText.setText(R.string.message_toast_no_account)
        emptyIcon.setImageResource(R.drawable.ic_info_error_generic)
        setListShown(false)

        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return
                val am = AccountManager.get(context)
                val accountKey: UserKey = data.getBundleExtra(EXTRA_EXTRAS)?.getParcelable(EXTRA_ACCOUNT_KEY) ?: return
                val color = data.getIntExtra(EXTRA_COLOR, Color.WHITE)
                val details = adapter.findItem(accountKey) ?: return
                details.color = color
                details.account.setColor(am, color)
                val resolver = context?.contentResolver
                task {
                    resolver?.let { updateContentsColor(it, details) }
                }
                adapter.notifyDataSetChanged()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_account -> {
                AccountManager.get(context).addAccount(ACCOUNT_TYPE, ACCOUNT_AUTH_TOKEN_TYPE,
                        null, null, activity, null, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_accounts_manager, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as? AdapterContextMenuInfo ?: return false
        val details = adapter.getItem(menuInfo.position) ?: return false
        when (item.itemId) {
            R.id.set_color -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, details.color)
                intent.putExtra(EXTRA_EXTRAS, Bundle {
                    this[EXTRA_ACCOUNT_KEY] = details.key
                })
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                startActivityForResult(intent, REQUEST_SET_COLOR)
            }
            R.id.delete -> {
                val f = AccountDeletionDialogFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT, details.account)
                f.arguments = args
                f.show(childFragmentManager, FRAGMENT_TAG_ACCOUNT_DELETION)
            }
        }
        return false
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val account = adapter.getItem(position)
        context?.let {
            IntentUtils.openUserProfile(it, account.user, preferences[newDocumentApiKey],
                null)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<AccountDetails>> {
        return AccountDetailsLoader(requireContext())
    }

    override fun onLoaderReset(loader: Loader<List<AccountDetails>>) {

    }

    override fun onLoadFinished(loader: Loader<List<AccountDetails>>, data: List<AccountDetails>) {
        adapter.apply {
            clear()
            addAll(data)
        }
        setListShown(true)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (menuInfo !is AdapterContextMenuInfo) return
        val account = adapter.getItem(menuInfo.position)!!
        menu.setHeaderTitle(account.user.name)
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.action_manager_account, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false)
    }

    private fun setListShown(shown: Boolean) {
        listContainer.visibility = if (shown) View.VISIBLE else View.GONE
        progressContainer.visibility = if (shown) View.GONE else View.VISIBLE
    }

    private fun updateContentsColor(resolver: ContentResolver, details: AccountDetails) {
        val statusValues = ContentValues().apply {
            put(Statuses.ACCOUNT_COLOR, details.color)
        }
        val statusesWhere = Expression.equalsArgs(Statuses.ACCOUNT_KEY)
        val statusesWhereArgs = arrayOf(details.key.toString())

        DataStoreUtils.STATUSES_ACTIVITIES_URIS.forEach { uri ->
            resolver.update(uri, statusValues, statusesWhere.sql, statusesWhereArgs)
        }
    }

    /**
     * DELETE YOUR ACCOUNT
     */
    class AccountDeletionDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val account: Account = arguments?.getParcelable(EXTRA_ACCOUNT)!!
            val resolver = context?.contentResolver
            val am = AccountManager.get(context)
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val accountKey = account.getAccountKey(am)
                    resolver?.deleteAccountData(accountKey)
                    context?.let {
                        AccountPreferences.getSharedPreferencesForAccount(it, accountKey).edit()
                                .clear().apply()
                    }
                    am.removeAccountSupport(account)
                }
            }
        }


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context
            val builder = AlertDialog.Builder(requireContext())
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setTitle(R.string.title_account_delete_confirm)
            builder.setMessage(R.string.message_account_delete_confirm)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    companion object {

        private const val FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion"
    }
}
