package org.mariotaku.twidere.model

import android.accounts.Account
import android.accounts.AccountManager
import org.mariotaku.twidere.extension.*

/**
 * Created by mariotaku on 2016/12/3.
 */

fun Account.toParcelableAccount(am: AccountManager): ParcelableAccount {
    val account = ParcelableAccount()
    writeParcelableAccount(am, account)
    return account
}

internal fun Account.writeParcelableAccount(am: AccountManager, account: ParcelableAccount) {
    val user = getAccountUser(am)
    val activated = isAccountActivated(am)
    val accountKey = getAccountKey(am)
    val accountType = getAccountType(am)

    account.account_key = accountKey
    account.account_type = accountType
    account.is_activated = activated

    account.screen_name = user.screen_name
    account.name = user.name
    account.profile_banner_url = user.profile_banner_url
    account.profile_image_url = user.profile_image_url

    account.account_user = user
    account.color = getColor(am)
}
