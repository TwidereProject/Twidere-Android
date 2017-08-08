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
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.util.JsonSerializer

/**
 * Created by mariotaku on 2017/4/16.
 */
fun Account.updateDetails(am: AccountManager, details: AccountDetails) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, details.key.toString())
    am.setUserData(this, ACCOUNT_USER_DATA_TYPE, details.type)
    am.setUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE, details.credentials_type)

    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, details.activated.toString())
    am.setUserData(this, ACCOUNT_USER_DATA_TEST, details.test.toString())
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(details.color, format = HexColorFormat.RGB))

    am.setUserData(this, ACCOUNT_USER_DATA_USER, JsonSerializer.serialize(details.user))
    am.setUserData(this, ACCOUNT_USER_DATA_EXTRAS, details.extras?.let { JsonSerializer.serialize(it) })
    am.setAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE, JsonSerializer.serialize(details.credentials))
}
