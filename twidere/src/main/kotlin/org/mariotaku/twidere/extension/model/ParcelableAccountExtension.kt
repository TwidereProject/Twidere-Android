package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2016/12/2.
 */

val ParcelableAccount.account_name: String
    get() = UserKey(screen_name, account_key.host).toString()

