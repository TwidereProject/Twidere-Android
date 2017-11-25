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

package org.mariotaku.twidere.extension.promise

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey

fun <T> Promise<T, Exception>.thenGetAccount(context: Context, accountKey: UserKey): Promise<AccountDetails, Exception> = then {
    return@then AccountManager.get(context).getDetailsOrThrow(accountKey, true)
}

fun <T> accountTask(context: Context, accountKey: UserKey, body: (account: AccountDetails) -> T): Promise<T, Exception> = task {
    return@task body(AccountManager.get(context).getDetailsOrThrow(accountKey, true))
}

fun <T> twitterTask(context: Context, accountKey: UserKey, action: (AccountDetails, MicroBlog) -> T): Promise<T, Exception> {
    return accountTask(context, accountKey) { account ->
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
                return@accountTask action(account, twitter)
            }
            else -> throw APINotSupportedException("Unsubscribe to user list", account.type)
        }
    }
}

fun <T> Promise<T, Exception>.toastOnResult(context: Context, successMessage: (T) -> String): Promise<T, Exception> = successUi {
    Toast.makeText(context, successMessage(it), Toast.LENGTH_SHORT).show()
}.failUi {
    Toast.makeText(context, it.getErrorMessage(context), Toast.LENGTH_SHORT).show()
}


fun <D> Promise<SingleResponse<D>, Exception>.toData(): Promise<D, Exception> = then { response ->
    if (response.hasException()) throw response.exception!!
    return@then response.data!!
}
