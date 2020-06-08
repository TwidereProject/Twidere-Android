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
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.content.FavoriteConfirmDialogActivity
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

/**
 * Asks user to favorite a status.
 *
 * Created by mariotaku on 2017/4/12.
 */
class FavoriteConfirmDialogFragment : AbsStatusDialogFragment() {

    override val Dialog.loadProgress: android.view.View get() = findViewById(R.id.loadProgress)

    override val Dialog.itemContent: android.view.View get() = findViewById(R.id.itemContent)

    override fun AlertDialog.Builder.setupAlertDialog() {
        if (preferences[iWantMyStarsBackKey]) {
            setTitle(R.string.title_favorite_confirm)
        } else {
            setTitle(R.string.title_like_confirm)
        }
        setView(R.layout.dialog_status_favorite_confirm)
        setPositiveButton(R.string.action_favorite, null)
        setNegativeButton(android.R.string.cancel, null)
    }

    override fun AlertDialog.onStatusLoaded(account: AccountDetails, status: ParcelableStatus,
            savedInstanceState: Bundle?) {
        val positiveButton = getButton(BUTTON_POSITIVE)
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

    override fun onCancel(dialog: DialogInterface) {
        finishFavoriteConfirmActivity()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        finishFavoriteConfirmActivity()
    }

    private fun finishFavoriteConfirmActivity() {
        val activity = this.activity
        if (activity is FavoriteConfirmDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    companion object {

        const val FRAGMENT_TAG = "favorite_confirm"

        fun show(fm: FragmentManager, accountKey: UserKey, statusId: String,
                 status: ParcelableStatus? = null): FavoriteConfirmDialogFragment {
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
