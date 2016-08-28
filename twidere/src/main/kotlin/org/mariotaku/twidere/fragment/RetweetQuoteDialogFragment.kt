/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.CheckResult
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.twitter.Validator
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_QUICK_SEND
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.service.BackgroundOperationService
import org.mariotaku.twidere.util.BugReporter
import org.mariotaku.twidere.util.EditTextEnterHandler
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.TwidereValidator
import org.mariotaku.twidere.util.Utils.isMyRetweet
import org.mariotaku.twidere.view.ComposeEditText
import org.mariotaku.twidere.view.StatusTextCountView
import org.mariotaku.twidere.view.holder.StatusViewHolder

class RetweetQuoteDialogFragment : BaseDialogFragment() {
    private var popupMenu: PopupMenu? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val context = builder.context
        val status = status!!
        val credentials = ParcelableCredentialsUtils.getCredentials(getContext(),
                status.account_key)!!

        builder.setView(R.layout.dialog_status_quote_retweet)
        builder.setTitle(R.string.retweet_quote_confirm_title)
        builder.setPositiveButton(R.string.retweet, null)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setNeutralButton(R.string.quote) { dialog, which ->
            val intent = Intent(INTENT_ACTION_QUOTE)
            val menu = popupMenu!!.menu
            val quoteOriginalStatus = menu.findItem(R.id.quote_original_status)
            intent.putExtra(EXTRA_STATUS, status)
            intent.putExtra(EXTRA_QUOTE_ORIGINAL_STATUS, quoteOriginalStatus.isChecked)
            startActivity(intent)
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val alertDialog = it as AlertDialog

            val itemContent = alertDialog.findViewById(R.id.itemContent)!!
            val textCountView = alertDialog.findViewById(R.id.comment_text_count) as StatusTextCountView
            val itemMenu = alertDialog.findViewById(R.id.itemMenu)!!
            val actionButtons = alertDialog.findViewById(R.id.actionButtons)!!
            val commentContainer = alertDialog.findViewById(R.id.comment_container)!!
            val editComment = alertDialog.findViewById(R.id.edit_comment) as ComposeEditText
            val commentMenu = alertDialog.findViewById(R.id.comment_menu)!!

            val adapter = DummyItemAdapter(context)
            adapter.setShouldShowAccountsColor(true)
            val holder = StatusViewHolder(adapter, itemContent)
            holder.displayStatus(status, false, true)

            textCountView.maxLength = TwidereValidator.getTextLimit(credentials)

            itemMenu.visibility = View.GONE
            actionButtons.visibility = View.GONE
            itemContent.isFocusable = false
            val useQuote = useQuote(!status.user_is_protected, credentials)

            commentContainer.visibility = if (useQuote) View.VISIBLE else View.GONE
            editComment.accountKey = (status.account_key)

            val sendByEnter = preferences.getBoolean(KEY_QUICK_SEND)
            val enterHandler = EditTextEnterHandler.attach(editComment, object : EditTextEnterHandler.EnterListener {
                override fun shouldCallListener(): Boolean {
                    return true
                }

                override fun onHitEnter(): Boolean {
                    if (retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM)) {
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
                    updateTextCount(getDialog(), s, status, credentials)
                }

                override fun afterTextChanged(s: Editable) {

                }
            })

            popupMenu = PopupMenu(context, commentMenu, Gravity.NO_GRAVITY,
                    R.attr.actionOverflowMenuStyle, 0)
            commentMenu.setOnClickListener { popupMenu!!.show() }
            commentMenu.setOnTouchListener(popupMenu!!.dragToOpenListener)
            popupMenu!!.inflate(R.menu.menu_dialog_comment)
            val menu = popupMenu!!.menu
            menu.setItemAvailability(R.id.quote_original_status,
                    status.retweet_id != null || status.quoted_id != null)
            popupMenu!!.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                if (item.isCheckable) {
                    item.isChecked = !item.isChecked
                    return@OnMenuItemClickListener true
                }
                false
            })

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                var dismissDialog = false
                if (editComment.length() > 0) {
                    dismissDialog = retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM)
                } else if (isMyRetweet(status)) {
                    twitterWrapper.cancelRetweetAsync(status.account_key, status.id, status.my_retweet_id)
                    dismissDialog = true
                } else if (useQuote(!status.user_is_protected, credentials)) {
                    dismissDialog = retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM)
                } else {
                    BugReporter.logException(IllegalStateException(status.toString()))
                }
                if (dismissDialog) {
                    dismiss()
                }
            }

            updateTextCount(alertDialog, editComment.text, status, credentials)
        }
        return dialog
    }

    private fun updateTextCount(dialog: DialogInterface, s: CharSequence, status: ParcelableStatus, credentials: ParcelableCredentials) {
        if (dialog !is AlertDialog) return
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE) ?: return
        if (s.length > 0) {
            positiveButton.setText(R.string.comment)
            positiveButton.isEnabled = true
        } else if (isMyRetweet(status)) {
            positiveButton.setText(R.string.cancel_retweet)
            positiveButton.isEnabled = true
        } else if (useQuote(false, credentials)) {
            positiveButton.setText(R.string.retweet)
            positiveButton.isEnabled = true
        } else {
            positiveButton.setText(R.string.retweet)
            positiveButton.isEnabled = !status.user_is_protected
        }
        val textCountView = (dialog.findViewById(R.id.comment_text_count) as StatusTextCountView?)!!
        textCountView.textCount = validator.getTweetLength(s.toString())
    }

    private val status: ParcelableStatus?
        get() {
            val args = arguments
            if (!args.containsKey(EXTRA_STATUS)) return null
            return args.getParcelable<ParcelableStatus>(EXTRA_STATUS)
        }

    @CheckResult
    private fun retweetOrQuote(account: ParcelableAccount, status: ParcelableStatus,
                               showProtectedConfirmation: Boolean): Boolean {
        val twitter = twitterWrapper
        val dialog = dialog ?: return false
        val editComment = dialog.findViewById(R.id.edit_comment) as EditText
        if (useQuote(editComment.length() > 0, account)) {
            val menu = popupMenu!!.menu
            val itemQuoteOriginalStatus = menu.findItem(R.id.quote_original_status)
            val statusLink: Uri
            val quoteOriginalStatus = itemQuoteOriginalStatus.isChecked

            var commentText: String
            val update = ParcelableStatusUpdate()
            update.accounts = arrayOf(account)
            val editingComment = editComment.text.toString()
            when (ParcelableAccountUtils.getAccountType(account)) {
                ParcelableAccount.Type.FANFOU -> {
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
                    if (!status.is_quote || !quoteOriginalStatus) {
                        statusLink = LinkCreator.getStatusWebLink(status)
                    } else {
                        statusLink = LinkCreator.getQuotedStatusWebLink(status)
                    }
                    update.attachment_url = statusLink.toString()
                    commentText = editingComment
                }
            }
            update.text = commentText
            update.is_possibly_sensitive = status.is_possibly_sensitive
            BackgroundOperationService.updateStatusesAsync(context, Draft.Action.QUOTE, update)
        } else {
            twitter.retweetStatusAsync(status.account_key, status.id)
        }
        return true
    }

    private fun useQuote(preCondition: Boolean, account: ParcelableAccount): Boolean {
        return preCondition || ParcelableAccount.Type.FANFOU == account.account_type
    }


    class QuoteProtectedStatusWarnFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val fragment = parentFragment as RetweetQuoteDialogFragment
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val args = arguments
                    val account = args.getParcelable<ParcelableAccount>(EXTRA_ACCOUNT)
                    val status = args.getParcelable<ParcelableStatus>(EXTRA_STATUS)
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
            return builder.create()
        }

        companion object {

            fun show(pf: RetweetQuoteDialogFragment,
                     account: ParcelableAccount,
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

        fun show(fm: FragmentManager, status: ParcelableStatus): RetweetQuoteDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_STATUS, status)
            val f = RetweetQuoteDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
