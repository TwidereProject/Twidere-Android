/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment.status

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.CheckResult
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.Toast
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.content.RetweetQuoteDialogActivity
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.quickSendKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.can_retweet
import org.mariotaku.twidere.extension.model.is_my_retweet
import org.mariotaku.twidere.extension.model.textLimit
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.QuoteStatusActionExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.util.EditTextEnterHandler
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.text.FanfouValidator
import org.mariotaku.twidere.util.text.StatusTextValidator
import org.mariotaku.twidere.util.view.SimpleTextWatcher
import org.mariotaku.twidere.view.ComposeEditText
import org.mariotaku.twidere.view.StatusTextCountView
import java.util.*

/**
 * Asks user to retweet/quote a status.
 */
class RetweetQuoteDialogFragment : AbsStatusDialogFragment() {

    override val Dialog.loadProgress: View get() = findViewById(R.id.loadProgress)
    override val Dialog.itemContent: View get() = findViewById(R.id.itemContent)

    private val Dialog.textCountView: StatusTextCountView get() = findViewById(R.id.commentTextCount)

    private val Dialog.commentContainer: RelativeLayout get() = findViewById(R.id.commentContainer)
    private val Dialog.editComment: ComposeEditText get() = findViewById(R.id.editComment)
    private val Dialog.quoteOriginal: CheckBox get() = findViewById(R.id.quoteOriginal)

    private val text: String?
        get() = arguments.getString(EXTRA_TEXT)

    override fun AlertDialog.Builder.setupAlertDialog() {
        setTitle(R.string.title_retweet_quote_confirm)
        setView(R.layout.dialog_status_quote_retweet)
        setPositiveButton(R.string.action_retweet, null)
        setNegativeButton(android.R.string.cancel, null)
        setNeutralButton(R.string.action_quote, null)
    }

    override fun AlertDialog.onStatusLoaded(account: AccountDetails, status: ParcelableStatus,
            savedInstanceState: Bundle?) {
        textCountView.maxLength = account.textLimit

        val canQuoteRetweet = canQuoteRetweet(account)

        commentContainer.visibility = if (canQuoteRetweet) View.VISIBLE else View.GONE
        editComment.account = account

        val enterHandler = EditTextEnterHandler.attach(editComment, object : EditTextEnterHandler.EnterListener {
            override fun shouldCallListener(): Boolean {
                return true
            }

            override fun onHitEnter(): Boolean {
                if (retweetOrQuote(account, status, showProtectedConfirm)) {
                    dismiss()
                    return true
                }
                return false
            }
        }, preferences[quickSendKey])
        enterHandler.addTextChangedListener(object : SimpleTextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                dialog.updateTextCount(s, status, account)
            }
        })

        quoteOriginal.visibility = if (status.retweet_id != null || status.quoted_id != null) {
            View.VISIBLE
        } else {
            View.GONE
        }

        getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (!shouldQuoteRetweet(account) && status.is_my_retweet) {
                twitterWrapper.cancelRetweetAsync(account.key, status.id, status.my_retweet_id)
                dismiss()
            } else if (retweetOrQuote(account, status, showProtectedConfirm)) {
                dismiss()
            }
        }
        getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            val intent = Intent(INTENT_ACTION_QUOTE)
            intent.putExtra(EXTRA_STATUS, status)
            intent.putExtra(EXTRA_QUOTE_ORIGINAL_STATUS, quoteOriginal.isChecked)
            startActivity(intent)
            dismiss()
        }

        if (savedInstanceState == null) {
            editComment.setText(text)
        }
        editComment.setSelection(editComment.length())

        dialog.updateTextCount(editComment.text, status, account)
    }

    override fun onCancel(dialog: DialogInterface?) {
        if (dialog !is Dialog) return
        if (dialog.editComment.empty) return
        dialog.saveToDrafts()
        Toast.makeText(context, R.string.message_toast_status_saved_to_draft, Toast.LENGTH_SHORT).show()
        finishRetweetQuoteActivity()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        finishRetweetQuoteActivity()
    }

    private fun finishRetweetQuoteActivity() {
        val activity = this.activity
        if (activity is RetweetQuoteDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    private fun DialogInterface.updateTextCount(s: CharSequence, status: ParcelableStatus, account: AccountDetails) {
        if (this !is AlertDialog) return
        val positiveButton = getButton(AlertDialog.BUTTON_POSITIVE) ?: return

        if (canQuoteRetweet(account)) {
            if (!editComment.empty) {
                positiveButton.setText(R.string.action_comment)
                positiveButton.isEnabled = true
            } else if (status.is_my_retweet) {
                positiveButton.setText(R.string.action_cancel_retweet)
                positiveButton.isEnabled = true
            } else {
                positiveButton.setText(R.string.action_retweet)
                positiveButton.isEnabled = status.can_retweet
            }
        } else if (status.is_my_retweet) {
            positiveButton.setText(R.string.action_cancel_retweet)
            positiveButton.isEnabled = true
        } else {
            positiveButton.setText(R.string.action_retweet)
            positiveButton.isEnabled = status.can_retweet
        }
        textCountView.textCount = StatusTextValidator.calculateLength(account.type, account.key,
                null, s.toString())
    }

    private fun DialogInterface.shouldQuoteRetweet(account: AccountDetails): Boolean {
        if (this !is AlertDialog) return false
        if (!canQuoteRetweet(account)) return false
        return !editComment.empty
    }

    @CheckResult
    private fun retweetOrQuote(account: AccountDetails, status: ParcelableStatus,
            showProtectedConfirmation: Boolean): Boolean {
        val twitter = twitterWrapper
        val dialog = dialog ?: return false
        val editComment = dialog.editComment
        if (dialog.isQuoteRetweet(account)) {
            val quoteOriginalStatus = dialog.quoteOriginal.isChecked

            var commentText: String
            val update = ParcelableStatusUpdate()
            update.accounts = arrayOf(account)
            val editingComment = editComment.text.toString()
            when (account.type) {
                AccountType.FANFOU -> {
                    if (!status.is_quote || !quoteOriginalStatus) {
                        if (status.user_is_protected && showProtectedConfirmation) {
                            QuoteProtectedStatusWarnFragment.show(this, account, status)
                            return false
                        }
                        update.repost_status_id = status.id
                        commentText = getString(R.string.fanfou_repost_format, editingComment,
                                status.user_screen_name, status.text_plain)
                    } else {
                        if (status.quoted_user_is_protected && showProtectedConfirmation) {
                            return false
                        }
                        commentText = getString(R.string.fanfou_repost_format, editingComment,
                                status.quoted_user_screen_name, status.quoted_text_plain)
                        update.repost_status_id = status.quoted_id
                    }
                    if (FanfouValidator.calculateLength(commentText) > FanfouValidator.textLimit) {
                        commentText = commentText.substring(0, Math.max(FanfouValidator.textLimit,
                                editingComment.length))
                    }
                }
                else -> {
                    val statusLink = if (!status.is_quote || !quoteOriginalStatus) {
                        LinkCreator.getStatusWebLink(status)
                    } else {
                        LinkCreator.getQuotedStatusWebLink(status)
                    }
                    update.attachment_url = statusLink.toString()
                    commentText = editingComment
                }
            }
            update.text = commentText
            update.is_possibly_sensitive = status.is_possibly_sensitive
            update.draft_action = Draft.Action.QUOTE
            update.draft_extras = QuoteStatusActionExtras().apply {
                this.status = status
                this.isQuoteOriginalStatus = quoteOriginalStatus
            }
            LengthyOperationsService.updateStatusesAsync(context, Draft.Action.QUOTE, update)
        } else {
            twitter.retweetStatusAsync(account.key, status)
        }
        return true
    }

    private fun canQuoteRetweet(account: AccountDetails): Boolean {
        return when (account.type) {
            AccountType.FANFOU, AccountType.TWITTER -> true
            else -> false
        }
    }

    private fun Dialog.isQuoteRetweet(account: AccountDetails): Boolean {
        return when (account.type) {
            AccountType.FANFOU -> true
            AccountType.TWITTER -> !editComment.empty
            else -> false
        }
    }

    private fun Dialog.saveToDrafts() {
        val text = dialog.editComment.text.toString()
        val draft = Draft()
        draft.unique_id = UUID.randomUUID().toString()
        draft.action_type = Draft.Action.QUOTE
        draft.account_keys = arrayOf(accountKey)
        draft.text = text
        draft.timestamp = System.currentTimeMillis()
        draft.action_extras = QuoteStatusActionExtras().apply {
            this.status = this@RetweetQuoteDialogFragment.status
            this.isQuoteOriginalStatus = quoteOriginal.isChecked
        }
        val values = ObjectCursor.valuesCreatorFrom(Draft::class.java).create(draft)
        val contentResolver = context.contentResolver
        val draftUri = contentResolver.insert(Drafts.CONTENT_URI, values)
        displayNewDraftNotification(draftUri)
    }


    private fun displayNewDraftNotification(draftUri: Uri) {
        val contentResolver = context.contentResolver
        val notificationUri = Drafts.CONTENT_URI_NOTIFICATIONS.withAppendedPath(draftUri.lastPathSegment)
        contentResolver.insert(notificationUri, null)
    }

    class QuoteProtectedStatusWarnFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val fragment = parentFragment as RetweetQuoteDialogFragment
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val args = arguments
                    val account: AccountDetails = args.getParcelable(EXTRA_ACCOUNT)
                    val status: ParcelableStatus = args.getParcelable(EXTRA_STATUS)
                    if (fragment.retweetOrQuote(account, status, false)) {
                        fragment.dismiss()
                    }
                }
            }

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.quote_protected_status_warning_message)
            builder.setPositiveButton(R.string.send_anyway, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        companion object {

            fun show(pf: RetweetQuoteDialogFragment,
                    account: AccountDetails,
                    status: ParcelableStatus): QuoteProtectedStatusWarnFragment {
                val f = QuoteProtectedStatusWarnFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT, account)
                args.putParcelable(EXTRA_STATUS, status)
                f.arguments = args
                f.show(pf.childFragmentManager, "quote_protected_status_warning")
                return f
            }
        }
    }

    companion object {

        private const val FRAGMENT_TAG = "retweet_quote"
        private val showProtectedConfirm = false

        fun show(fm: FragmentManager, accountKey: UserKey, statusId: String,
                status: ParcelableStatus? = null, text: String? = null):
                RetweetQuoteDialogFragment {
            val f = RetweetQuoteDialogFragment()
            f.arguments = Bundle {
                this[EXTRA_ACCOUNT_KEY] = accountKey
                this[EXTRA_STATUS_ID] = statusId
                this[EXTRA_STATUS] = status
                this[EXTRA_TEXT] = text
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }

    }
}
