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
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.content.RetweetQuoteDialogActivity
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.view.ColorLabelRelativeLayout
import org.mariotaku.twidere.view.holder.StatusViewHolder

/**
 * Asks user to favorite a status.
 *
 * Created by mariotaku on 2017/4/12.
 */
class FavoriteConfirmDialogFragment : BaseDialogFragment() {

    private val Dialog.loadProgress get() = findViewById(R.id.loadProgress)
    private val Dialog.itemContent get() = findViewById(R.id.itemContent) as ColorLabelRelativeLayout
    private val Dialog.itemMenu get() = findViewById(R.id.itemMenu) as ImageButton
    private val Dialog.actionButtons get() = findViewById(R.id.actionButtons) as LinearLayout

    private val status: ParcelableStatus
        get() = arguments.getParcelable<ParcelableStatus>(EXTRA_STATUS)

    private val accountKey: UserKey
        get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY) ?: status.account_key

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val accountKey = this.accountKey
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)!!
        val status = this.status.apply {
            if (account_key != accountKey) {
                is_favorite = false
            }
            account_key = details.key
            account_color = details.color
        }


        builder.setView(R.layout.dialog_status_favorite_confirm)
        builder.setTitle(R.string.title_favorite_confirm)
        if (preferences[iWantMyStarsBackKey]) {
            builder.setPositiveButton(R.string.action_favorite, null)
        } else {
            builder.setPositiveButton(R.string.action_like, null)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.setOnShowListener { dialog ->
            dialog as AlertDialog
            dialog.applyTheme()

            val adapter = DummyItemAdapter(context, requestManager = Glide.with(this))
            adapter.setShouldShowAccountsColor(true)
            val holder = StatusViewHolder(adapter, dialog.itemContent)
            holder.displayStatus(status = status, displayInReplyTo = false)

            dialog.loadProgress.visibility = View.GONE
            dialog.itemMenu.visibility = View.GONE
            dialog.actionButtons.visibility = View.GONE
            dialog.itemContent.isFocusable = false

            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            if (preferences[iWantMyStarsBackKey]) {
                if (status.is_favorite) {
                    positiveButton.setText(R.string.action_unfavorite)
                } else {
                    positiveButton.setText(R.string.action_favorite)
                }
            } else {
                if (status.is_favorite) {
                    positiveButton.setText(R.string.action_undo_like)
                } else {
                    positiveButton.setText(R.string.action_like)
                }
            }
            positiveButton.setOnClickListener {
                if (status.is_favorite) {
                    twitterWrapper.destroyFavoriteAsync(accountKey, status.id)
                } else {
                    twitterWrapper.createFavoriteAsync(accountKey, status)
                }
                dismiss()
            }

        }
        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        finishFavoriteConfirmActivity()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        finishFavoriteConfirmActivity()
    }

    private fun finishFavoriteConfirmActivity() {
        val activity = this.activity
        if (activity is RetweetQuoteDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    companion object {

        val FRAGMENT_TAG = "favorite_confirm"

        fun show(fm: FragmentManager, accountKey: UserKey? = null, statusId: String,
                status: ParcelableStatus?): FavoriteConfirmDialogFragment {
            val f = FavoriteConfirmDialogFragment()
            f.arguments = Bundle {
                this[EXTRA_ACCOUNT_KEY] = accountKey
                this[EXTRA_STATUS_ID] = statusId
                this[EXTRA_STATUS] = status
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
