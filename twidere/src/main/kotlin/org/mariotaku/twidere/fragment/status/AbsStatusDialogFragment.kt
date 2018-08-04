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

import android.accounts.AccountManager
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AlertDialog.Builder
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.ktextension.weak
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.databinding.ItemStatusBinding
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.displayInfo
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ModelCreationConfig
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

abstract class AbsStatusDialogFragment : BaseDialogFragment() {

    protected abstract val Dialog.loadProgress: View
    protected abstract val itemBinding: ItemStatusBinding

    protected val status: ParcelableStatus?
        get() = arguments!!.status

    protected val statusId: String
        get() = arguments!!.statusId!!

    protected val accountKey: UserKey
        get() = arguments!!.accountKey!!

    private lateinit var adapter: DummyItemAdapter

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val accountKey = this.accountKey

        onPrepareDialogBuilder(builder)

        adapter = DummyItemAdapter(context!!, requestManager = Glide.with(this))
        adapter.showCardActions = false
        adapter.showAccountsColor = true

        val dialog = builder.create()
        dialog.onShow {
            val context = it.context ?: return@onShow
            it.applyTheme()

            val am = AccountManager.get(context)
            val details = am.getDetails(accountKey, true) ?: run {
                dismiss()
                return@onShow
            }
            val weakThis by weak(this)
            val weakBinding by weak(itemBinding)
            val extraStatus = status
            if (extraStatus != null) {
                showStatus(itemBinding, extraStatus, details, savedInstanceState)
            } else promiseOnUi {
                weakThis?.showProgress()
            } and AbsStatusDialogFragment.showStatus(context, details, statusId).successUi { status ->
                val holder = weakBinding ?: return@successUi
                weakThis?.showStatus(holder, status, details, savedInstanceState)
            }.failUi {
                val fragment = weakThis?.takeIf { it.dialog != null } ?: return@failUi
                Toast.makeText(fragment.context, R.string.message_toast_error_occurred,
                        Toast.LENGTH_SHORT).show()
                fragment.dismiss()
            }
        }
        return dialog
    }

    private fun showProgress() {
        val currentDialog = this.dialog as? AlertDialog ?: return
        currentDialog.loadProgress.visibility = View.VISIBLE
        itemBinding.root.visibility = View.GONE
        currentDialog.getButton(BUTTON_POSITIVE)?.isEnabled = false
        currentDialog.getButton(BUTTON_NEUTRAL)?.isEnabled = false
    }

    private fun showStatus(binding: ItemStatusBinding, status: ParcelableStatus,
            details: AccountDetails, savedInstanceState: Bundle?) {
        status.apply {
            if (account_key != details.key) {
                my_retweet_id = null
                is_favorite = false
            }
            account_key = details.key
            account_color = details.color
            displayInfo(context!!)
        }
        val currentDialog = this.dialog as? AlertDialog ?: return
        currentDialog.getButton(BUTTON_POSITIVE)?.isEnabled = true
        currentDialog.getButton(BUTTON_NEUTRAL)?.isEnabled = true
        currentDialog.loadProgress.visibility = View.GONE
        itemBinding.root.visibility = View.VISIBLE
        itemBinding.root.isFocusable = false
        binding.status = status
        currentDialog.onStatusLoaded(details, status, savedInstanceState)
    }

    protected abstract fun onPrepareDialogBuilder(builder: Builder)

    protected abstract fun AlertDialog.onStatusLoaded(account: AccountDetails, status: ParcelableStatus,
            savedInstanceState: Bundle?)

    companion object {

        fun showStatus(context: Context, details: AccountDetails, statusId: String): Promise<ParcelableStatus, Exception> {
            val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
            val creationConfig = ModelCreationConfig.obtain(context)
            return task { microBlog.showStatus(statusId).toParcelable(details, creationConfig) }
        }

    }
}
