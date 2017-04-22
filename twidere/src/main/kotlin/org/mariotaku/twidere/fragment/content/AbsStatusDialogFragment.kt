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
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.view.holder.StatusViewHolder
import java.lang.ref.WeakReference

abstract class AbsStatusDialogFragment : BaseDialogFragment() {

    protected abstract val Dialog.loadProgress: View
    protected abstract val Dialog.itemContent: View

    protected val status: ParcelableStatus?
        get() = arguments.getParcelable<ParcelableStatus>(EXTRA_STATUS)

    protected val statusId: String
        get() = arguments.getString(EXTRA_STATUS_ID)

    protected val accountKey: UserKey
        get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)

    private lateinit var adapter: DummyItemAdapter

    override final fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val accountKey = this.accountKey

        builder.setupAlertDialog()

        adapter = DummyItemAdapter(context, requestManager = Glide.with(this))
        adapter.showCardActions = false
        adapter.showAccountsColor = true

        val dialog = builder.create()
        dialog.setOnShowListener { dialog ->
            dialog as AlertDialog
            dialog.applyTheme()

            val am = AccountManager.get(context)
            val details = AccountUtils.getAccountDetails(am, accountKey, true) ?: run {
                dismiss()
                return@setOnShowListener
            }
            val weakThis = WeakReference(this)
            val weakHolder = WeakReference(StatusViewHolder(adapter, dialog.itemContent).apply {
                setupViewOptions()
            })
            promiseOnUi {
                val currentDialog = weakThis.get()?.dialog as? AlertDialog ?: return@promiseOnUi
                currentDialog.loadProgress.visibility = View.VISIBLE
                currentDialog.itemContent.visibility = View.GONE
                currentDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = false
                currentDialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.isEnabled = false
            } and showStatus(context, details, statusId, status).successUi { status ->
                val fragment = weakThis.get() ?: return@successUi
                val currentDialog = fragment.dialog as? AlertDialog ?: return@successUi
                val holder = weakHolder.get() ?: return@successUi
                currentDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = true
                currentDialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.isEnabled = true
                currentDialog.itemContent.visibility = View.VISIBLE
                currentDialog.loadProgress.visibility = View.GONE
                currentDialog.itemContent.isFocusable = false
                holder.displayStatus(status = status, displayInReplyTo = false)
                currentDialog.onStatusLoaded(details, status, savedInstanceState)
            }.failUi {
                val fragment = weakThis.get()?.takeIf { it.dialog != null } ?: return@failUi
                Toast.makeText(fragment.context, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
                fragment.dismiss()
            }
        }
        return dialog
    }

    protected abstract fun AlertDialog.Builder.setupAlertDialog()

    protected abstract fun AlertDialog.onStatusLoaded(details: AccountDetails, status: ParcelableStatus,
            savedInstanceState: Bundle?)

    companion object {

        fun showStatus(context: Context, details: AccountDetails, statusId: String,
                status: ParcelableStatus?): Promise<ParcelableStatus, Exception> {
            if (status != null) {
                status.apply {
                    if (account_key != details.key) {
                        my_retweet_id = null
                        is_favorite = false
                    }
                    account_key = details.key
                    account_color = details.color
                }
                return Promise.ofSuccess(status)
            }
            val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
            val profileImageSize = context.getString(R.string.profile_image_size)
            return task { microBlog.showStatus(statusId).toParcelable(details, profileImageSize) }
        }

    }
}
