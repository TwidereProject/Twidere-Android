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

import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.constant.IntentConstants.*

/**
 * Asks user to favorite a status.
 *
 * Created by mariotaku on 2017/4/12.
 */
class FavoriteConfirmDialogFragment : AbsStatusDialogFragment() {

    override val android.app.Dialog.loadProgress: android.view.View get() = findViewById(org.mariotaku.twidere.R.id.loadProgress)

    override val android.app.Dialog.itemContent: android.view.View get() = findViewById(org.mariotaku.twidere.R.id.itemContent)

    override fun android.support.v7.app.AlertDialog.Builder.setupAlertDialog() {
        if (preferences[org.mariotaku.twidere.constant.iWantMyStarsBackKey]) {
            setTitle(org.mariotaku.twidere.R.string.title_favorite_confirm)
        } else {
            setTitle(org.mariotaku.twidere.R.string.title_like_confirm)
        }
        setView(org.mariotaku.twidere.R.layout.dialog_status_favorite_confirm)
        setPositiveButton(org.mariotaku.twidere.R.string.action_favorite, null)
        setNegativeButton(android.R.string.cancel, null)
    }

    override fun android.support.v7.app.AlertDialog.onStatusLoaded(details: org.mariotaku.twidere.model.AccountDetails, status: org.mariotaku.twidere.model.ParcelableStatus,
            savedInstanceState: android.os.Bundle?) {
        val positiveButton = getButton(android.content.DialogInterface.BUTTON_POSITIVE)
        if (preferences[org.mariotaku.twidere.constant.iWantMyStarsBackKey]) {
            if (status.is_favorite) {
                positiveButton.setText(org.mariotaku.twidere.R.string.action_unfavorite)
            } else {
                positiveButton.setText(org.mariotaku.twidere.R.string.action_favorite)
            }
        } else {
            if (status.is_favorite) {
                positiveButton.setText(org.mariotaku.twidere.R.string.action_undo_like)
            } else {
                positiveButton.setText(org.mariotaku.twidere.R.string.action_like)
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

    override fun onCancel(dialog: android.content.DialogInterface) {
        finishFavoriteConfirmActivity()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        finishFavoriteConfirmActivity()
    }

    private fun finishFavoriteConfirmActivity() {
        val activity = this.activity
        if (activity is org.mariotaku.twidere.activity.content.FavoriteConfirmDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    companion object {

        val FRAGMENT_TAG = "favorite_confirm"

        fun show(fm: android.support.v4.app.FragmentManager, accountKey: org.mariotaku.twidere.model.UserKey, statusId: String,
                status: org.mariotaku.twidere.model.ParcelableStatus? = null): org.mariotaku.twidere.fragment.status.FavoriteConfirmDialogFragment {
            val f = org.mariotaku.twidere.fragment.status.FavoriteConfirmDialogFragment()
            f.arguments = org.mariotaku.ktextension.Bundle {
                this[EXTRA_ACCOUNT_KEY] = accountKey
                this[EXTRA_STATUS_ID] = statusId
                this[EXTRA_STATUS] = status
            }
            f.show(fm, org.mariotaku.twidere.fragment.status.FavoriteConfirmDialogFragment.Companion.FRAGMENT_TAG)
            return f
        }
    }
}
