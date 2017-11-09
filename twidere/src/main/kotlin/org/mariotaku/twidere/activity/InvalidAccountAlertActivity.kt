package org.mariotaku.twidere.activity

import android.accounts.AccountManager
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.ktextension.toWeak
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_INTENT
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.util.AccountUtils

class InvalidAccountAlertActivity : FragmentActivity(), IBaseActivity<InvalidAccountAlertActivity> {

    private val actionHelper = IBaseActivity.ActionHelper<InvalidAccountAlertActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val df = InvalidAccountAlertDialogFragment()
        df.show(supportFragmentManager, "invalid_account_alert")
    }

    override fun onPause() {
        actionHelper.dispatchOnPause(this)
        super.onPause()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        actionHelper.dispatchOnResumeFragments(this)
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (InvalidAccountAlertActivity) -> Unit): Promise<Unit, Exception> {
        return actionHelper.executeAfterFragmentResumed(this, useHandler, action)
    }

    override fun executeBeforeFragmentPaused(useHandler: Boolean, action: (InvalidAccountAlertActivity) -> Unit): Promise<Unit, Exception> {
        return actionHelper.executeBeforeFragmentPaused(this, useHandler, action)
    }

    fun removeInvalidAccounts() {
        val am = AccountManager.get(this)
        val weakThis = toWeak()
        val invalidAccounts = AccountUtils.getAccounts(am).filter { !am.isAccountValid(it) }
        (showProgressDialog("remove_invalid_accounts") and all(invalidAccounts.map { am.removeAccount(it) })).successUi {
            val activity = weakThis.get() ?: return@successUi
            val intent = activity.intent.getParcelableExtra<Intent>(EXTRA_INTENT)
            if (intent != null) {
                activity.startActivity(intent)
            }
            activity.finish()
        }.failUi {
            val activity = weakThis.get() ?: return@failUi
            activity.finish()
        }

    }

    class InvalidAccountAlertDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.title_error_invalid_account)
            builder.setMessage(R.string.message_error_invalid_account)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                (activity as InvalidAccountAlertActivity).removeInvalidAccounts()
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        override fun onCancel(dialog: DialogInterface?) {
            super.onCancel(dialog)
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

}
