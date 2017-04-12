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

package org.mariotaku.twidere.fragment.content

import android.accounts.AccountManager
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.support.annotation.CheckResult
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.twitter.Validator
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.content.RetweetQuoteDialogActivity
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_QUICK_SEND
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.textLimit
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.QuoteStatusActionExtras
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.EditTextEnterHandler
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.Utils.isMyRetweet
import org.mariotaku.twidere.view.ColorLabelRelativeLayout
import org.mariotaku.twidere.view.ComposeEditText
import org.mariotaku.twidere.view.StatusTextCountView
import org.mariotaku.twidere.view.holder.StatusViewHolder
import java.util.*

class RetweetQuoteDialogFragment : BaseDialogFragment() {
    private lateinit var popupMenu: PopupMenu

    private val PopupMenu.quoteOriginalStatus get() = menu.isItemChecked(R.id.quote_original_status)
    private val Dialog.itemContent get() = findViewById(R.id.itemContent) as ColorLabelRelativeLayout
    private val Dialog.textCountView get() = findViewById(R.id.commentTextCount) as StatusTextCountView
    private val Dialog.itemMenu get() = findViewById(R.id.itemMenu) as ImageButton
    private val Dialog.actionButtons get() = findViewById(R.id.actionButtons) as LinearLayout
    private val Dialog.commentContainer get() = findViewById(R.id.commentContainer) as RelativeLayout
    private val Dialog.editComment get() = findViewById(R.id.editComment) as ComposeEditText
    private val Dialog.commentMenu get() = findViewById(R.id.commentMenu) as ImageButton


    private val status: ParcelableStatus
        get() = arguments.getParcelable<ParcelableStatus>(EXTRA_STATUS)

    private val accountKey: UserKey
        get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY) ?: status.account_key

    private val text: String?
        get() = arguments.getString(EXTRA_TEXT)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val accountKey = this.accountKey
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)!!
        val status = this.status.apply {
            account_key = details.key
            account_color = details.color
        }

        builder.setView(R.layout.dialog_status_quote_retweet)
        builder.setTitle(R.string.retweet_quote_confirm_title)
        builder.setPositiveButton(R.string.action_retweet, null)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setNeutralButton(R.string.action_quote) { _, _ ->
            val intent = Intent(INTENT_ACTION_QUOTE)
            val menu = popupMenu.menu
            val quoteOriginalStatus = menu.findItem(R.id.quote_original_status)
            intent.putExtra(EXTRA_STATUS, status)
            intent.putExtra(EXTRA_QUOTE_ORIGINAL_STATUS, quoteOriginalStatus.isChecked)
            startActivity(intent)
        }

        val dialog = builder.create()
        dialog.setOnShowListener { dialog ->
            dialog as AlertDialog
            dialog.applyTheme()

            val adapter = DummyItemAdapter(context, requestManager = Glide.with(this))
            adapter.setShouldShowAccountsColor(true)
            val holder = StatusViewHolder(adapter, dialog.itemContent)
            holder.displayStatus(status = status, displayInReplyTo = false)

            dialog.textCountView.maxLength = details.textLimit

            dialog.itemMenu.visibility = View.GONE
            dialog.actionButtons.visibility = View.GONE
            dialog.itemContent.isFocusable = false
            val useQuote = useQuote(!status.user_is_protected, details)

            dialog.commentContainer.visibility = if (useQuote) View.VISIBLE else View.GONE
            dialog.editComment.accountKey = details.key

            val sendByEnter = preferences.getBoolean(KEY_QUICK_SEND)
            val enterHandler = EditTextEnterHandler.attach(dialog.editComment, object : EditTextEnterHandler.EnterListener {
                override fun shouldCallListener(): Boolean {
                    return true
                }

                override fun onHitEnter(): Boolean {
                    if (retweetOrQuote(details, status, SHOW_PROTECTED_CONFIRM)) {
                        dismiss()
                        return true
                    }
                    return false
                }
            }, sendByEnter)
            enterHandler.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    updateTextCount(getDialog(), s, status, details)
                }

                override fun afterTextChanged(s: Editable) {

                }
            })

            popupMenu = PopupMenu(context, dialog.commentMenu, Gravity.NO_GRAVITY,
                    R.attr.actionOverflowMenuStyle, 0).apply {
                inflate(R.menu.menu_dialog_comment)
                menu.setItemAvailability(R.id.quote_original_status, status.retweet_id != null || status.quoted_id != null)
                setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    if (item.isCheckable) {
                        item.isChecked = !item.isChecked
                        return@OnMenuItemClickListener true
                    }
                    false
                })
            }
            dialog.commentMenu.setOnClickListener { popupMenu.show() }
            dialog.commentMenu.setOnTouchListener(popupMenu.dragToOpenListener)
            dialog.commentMenu.visibility = if (popupMenu.menu.hasVisibleItems()) View.VISIBLE else View.GONE

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                var dismissDialog = false
                if (dialog.editComment.length() > 0) {
                    dismissDialog = retweetOrQuote(details, status, SHOW_PROTECTED_CONFIRM)
                } else if (isMyRetweet(status)) {
                    twitterWrapper.cancelRetweetAsync(details.key, status.id, status.my_retweet_id)
                    dismissDialog = true
                } else if (useQuote(!status.user_is_protected, details)) {
                    dismissDialog = retweetOrQuote(details, status, SHOW_PROTECTED_CONFIRM)
                } else {
                    Analyzer.logException(IllegalStateException(status.toString()))
                }
                if (dismissDialog) {
                    dismiss()
                }
            }

            if (savedInstanceState == null) {
                dialog.editComment.setText(text)
            }
            dialog.editComment.setSelection(dialog.editComment.length())

            updateTextCount(dialog, dialog.editComment.text, status, details)
        }
        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        if (dialog !is Dialog) return
        if (dialog.editComment.empty) return
        dialog.saveToDrafts()
        Toast.makeText(context, R.string.message_toast_status_saved_to_draft, Toast.LENGTH_SHORT).show()
        finishRetweetQuoteActivity()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        finishRetweetQuoteActivity()
    }

    private fun finishRetweetQuoteActivity() {
        val activity = this.activity
        if (activity is RetweetQuoteDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    private fun updateTextCount(dialog: DialogInterface, s: CharSequence, status: ParcelableStatus,
            credentials: AccountDetails) {
        if (dialog !is AlertDialog) return
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE) ?: return
        if (s.isNotEmpty()) {
            positiveButton.setText(R.string.comment)
            positiveButton.isEnabled = true
        } else if (isMyRetweet(status)) {
            positiveButton.setText(R.string.action_cancel_retweet)
            positiveButton.isEnabled = true
        } else if (useQuote(false, credentials)) {
            positiveButton.setText(R.string.action_retweet)
            positiveButton.isEnabled = true
        } else {
            positiveButton.setText(R.string.action_retweet)
            positiveButton.isEnabled = !status.user_is_protected
        }
        val textCountView = dialog.findViewById(R.id.commentTextCount) as StatusTextCountView
        textCountView.textCount = validator.getTweetLength(s.toString())
    }

    @CheckResult
    private fun retweetOrQuote(account: AccountDetails, status: ParcelableStatus,
            showProtectedConfirmation: Boolean): Boolean {
        val twitter = twitterWrapper
        val dialog = dialog ?: return false
        val editComment = dialog.findViewById(R.id.editComment) as EditText
        if (useQuote(editComment.length() > 0, account)) {
            val quoteOriginalStatus = popupMenu.quoteOriginalStatus

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
                    if (commentText.length > Validator.MAX_TWEET_LENGTH) {
                        commentText = commentText.substring(0, Math.max(Validator.MAX_TWEET_LENGTH,
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

    private fun useQuote(preCondition: Boolean, account: AccountDetails): Boolean {
        return preCondition || AccountType.FANFOU == account.type
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
            this.isQuoteOriginalStatus = popupMenu.quoteOriginalStatus
        }
        val values = ObjectCursor.valuesCreatorFrom(Draft::class.java).create(draft)
        val contentResolver = context.contentResolver
        val draftUri = contentResolver.insert(Drafts.CONTENT_URI, values)
        displayNewDraftNotification(draftUri)
    }


    private fun displayNewDraftNotification(draftUri: Uri) {
        val contentResolver = context.contentResolver
        val values = ContentValues {
            this[BaseColumns._ID] = draftUri.lastPathSegment
        }
        contentResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, values)
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
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
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

        val FRAGMENT_TAG = "retweet_quote"
        private val SHOW_PROTECTED_CONFIRM = java.lang.Boolean.parseBoolean("false")

        fun show(fm: FragmentManager, status: ParcelableStatus, accountKey: UserKey? = null,
                text: String? = null): RetweetQuoteDialogFragment {
            val f = RetweetQuoteDialogFragment()
            f.arguments = Bundle {
                this[EXTRA_STATUS] = status
                this[EXTRA_ACCOUNT_KEY] = accountKey
                this[EXTRA_TEXT] = text
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
