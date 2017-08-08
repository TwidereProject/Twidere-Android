package org.mariotaku.twidere.preference

import android.accounts.AccountManager
import android.content.Context
import android.content.res.TypedArray
import android.support.v4.util.ArraySet
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceViewHolder
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.getAccountKey
import org.mariotaku.twidere.extension.model.getAccountUser
import org.mariotaku.twidere.extension.model.renameTwidereAccount
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.generateAccountName
import java.util.*

/**
 * Created by mariotaku on 2016/12/16.
 */

class RandomizeAccountNamePreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.switchPreferenceCompatStyle,
        defStyleRes: Int = 0
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes), IDialogPreference {

    init {
        dialogTitle = title
        dialogMessage = context.getString(R.string.preference_randomize_account_rename_accounts_confirm)
        positiveButtonText = context.getString(android.R.string.ok)
        negativeButtonText = context.getString(android.R.string.cancel)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val switchView = holder.findViewById(android.support.v7.preference.R.id.switchWidget) as SwitchCompat
        switchView.isChecked = getPersistedBoolean(false)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getBoolean(index, false)
    }

    override fun onClick() {
        persistBoolean(!getPersistedBoolean(false))
        notifyChanged()
        super.onClick()
    }

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = RenameAccountsConfirmDialogFragment.newInstance(key, getPersistedBoolean(false))
        df.setTargetFragment(fragment, 0)
        df.show(fragment.fragmentManager, key)
    }

    class RenameAccountsConfirmDialogFragment : PreferenceDialogFragmentCompat() {

        override fun onDialogClosed(positiveResult: Boolean) {
            val am = AccountManager.get(context)
            val enabled = arguments.getBoolean(ARG_VALUE)
            if (enabled) {
                val usedNames = ArraySet<String>()
                AccountUtils.getAccounts(am).forEach { oldAccount ->
                    var newName: String
                    do {
                        newName = UUID.randomUUID().toString()
                    } while (usedNames.contains(newName))
                    am.renameTwidereAccount(oldAccount, newName)
                    usedNames.add(newName)
                }
            } else {
                AccountUtils.getAccounts(am).forEach { oldAccount ->
                    val accountKey = oldAccount.getAccountKey(am)
                    val accountUser = oldAccount.getAccountUser(am)
                    val newName = generateAccountName(accountUser.screen_name, accountKey.host)
                    am.renameTwidereAccount(oldAccount, newName)
                }
            }
        }

        companion object {
            const val ARG_VALUE = "value"
            fun newInstance(key: String, value: Boolean): RenameAccountsConfirmDialogFragment {
                val df = RenameAccountsConfirmDialogFragment()
                df.arguments = Bundle {
                    this[ARG_KEY] = key
                    this[ARG_VALUE] = value
                }
                return df
            }
        }
    }
}
