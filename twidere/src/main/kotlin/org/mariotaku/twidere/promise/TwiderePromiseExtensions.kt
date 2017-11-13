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

package org.mariotaku.twidere.promise

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey

fun <T> accountTask(context: Context, accountKey: UserKey, body: (account: AccountDetails) -> T): Promise<T, Exception> = task {
    return@task body(AccountManager.get(context).getDetailsOrThrow(accountKey, true))
}

fun <T> Promise<T, Exception>.toastOnFail(context: Context): Promise<T, Exception> = failUi {
    Toast.makeText(context, it.getErrorMessage(context), Toast.LENGTH_SHORT).show()
}