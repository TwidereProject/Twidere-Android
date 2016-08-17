package org.mariotaku.twidere.menu

import android.content.Context
import android.content.Intent
import android.view.ActionProvider
import android.view.Menu
import android.view.SubMenu
import android.view.View
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils

class AccountActionProvider(
        context: Context,
        var accounts: Array<ParcelableAccount>? = ParcelableAccountUtils.getAccounts(context, false, false)
) : ActionProvider(context), TwidereConstants {

    var selectedAccountIds: Array<UserKey>? = null
    var isExclusive: Boolean = false

    override fun hasSubMenu(): Boolean {
        return true
    }

    override fun onCreateActionView(): View? {
        return null
    }

    override fun onPrepareSubMenu(subMenu: SubMenu) {
        subMenu.removeGroup(MENU_GROUP)
        if (accounts == null) return
        accounts?.forEachIndexed { idx, account ->
            val item = subMenu.add(MENU_GROUP, Menu.NONE, idx, account.name)
            val intent = Intent()
            intent.putExtra(EXTRA_ACCOUNT, account)
            item.intent = intent
        }
        subMenu.setGroupCheckable(MENU_GROUP, true, isExclusive)
        selectedAccountIds?.let {
            for (i in 0 until subMenu.size()) {
                val item = subMenu.getItem(i)
                val intent = item.intent
                val account = intent.getParcelableExtra<ParcelableAccount>(EXTRA_ACCOUNT)
                if (it.contains(account.account_key)) {
                    item.isChecked = true
                }
            }
        }
    }

    companion object {

        val MENU_GROUP = 201
    }

}
