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

package org.mariotaku.twidere.extension.model

import android.accounts.Account
import android.accounts.AccountManager
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.util.JsonSerializer

/**
 * Created by mariotaku on 2017/4/16.
 */
fun Account.updateDetails(am: AccountManager, details: AccountDetails) {
    setAccountKey(am, details.key)
    
    am.setUserData(this, ACCOUNT_USER_DATA_TYPE, details.type)
    am.setUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE, details.credentials_type)

    setActivated(am, details.activated)
    setTest(am, details.test)
    setColor(am, details.color)
    setAccountUser(am, details.user)

    am.setUserData(this, ACCOUNT_USER_DATA_EXTRAS, details.extras?.let { JsonSerializer.serialize(it) })
    am.setAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE, JsonSerializer.serialize(details.credentials))
}
