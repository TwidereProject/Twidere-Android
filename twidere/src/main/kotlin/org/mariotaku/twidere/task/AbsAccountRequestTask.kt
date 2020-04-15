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

package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.insert
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.task.twitter.UpdateStatusTask

/**
 * Created by mariotaku on 2017/4/20.
 */

abstract class AbsAccountRequestTask<Params, Result, Callback>(context: Context, val accountKey: UserKey?) :
        ExceptionHandlingAbstractTask<Params, Result, MicroBlogException, Callback>(context) {
    final override val exceptionClass = MicroBlogException::class.java

    final override fun onExecute(params: Params): Result {
        val am = AccountManager.get(context)
        val account = accountKey?.let { AccountUtils.getAccountDetails(am, it, true) } ?:
                throw AccountNotFoundException()
        val draft = createDraft()
        var draftId = -1L
        if (draft != null) {
            val uri = context.contentResolver.insert(Drafts.CONTENT_URI, draft)
            draftId = uri?.lastPathSegment.toLongOr(-1)
        }
        if (draftId != -1L) {
            microBlogWrapper.addSendingDraftId(draftId)
        }
        try {
            val result = onExecute(account, params)
            onCleanup(account, params, result, null)
            if (draftId != -1L) {
                UpdateStatusTask.deleteDraft(context, draftId)
            }
            return result
        } catch (e: MicroBlogException) {
            onCleanup(account, params, null, e)
            if (draftId != 1L && deleteDraftOnException(account, params, e)) {
                UpdateStatusTask.deleteDraft(context, draftId)
            }
            throw e
        } finally {
            if (draftId != -1L) {
                microBlogWrapper.removeSendingDraftId(draftId)
            }
        }
    }

    protected abstract fun onExecute(account: AccountDetails, params: Params): Result

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result?, exception: MicroBlogException?) {
        if (result != null) {
            onCleanup(account, params, result)
        } else if (exception != null) {
            onCleanup(account, params, exception)
        }
    }

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result) {}
    protected open fun onCleanup(account: AccountDetails, params: Params, exception: MicroBlogException) {}

    protected open fun createDraft(): Draft? = null

    protected open fun deleteDraftOnException(account: AccountDetails, params: Params, exception: MicroBlogException): Boolean = false

    override fun onException(callback: Callback?, exception: MicroBlogException) {
        Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
    }
}
