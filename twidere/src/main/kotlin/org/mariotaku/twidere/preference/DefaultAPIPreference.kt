/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.preference

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.iface.APIEditorActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.fragment.ThemedPreferenceDialogFragmentCompat
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder

class DefaultAPIPreference @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet?,
                                                     defStyle: Int = R.attr.dialogPreferenceStyle
) : DialogPreference(context, attrs, defStyle), IDialogPreference {

    private var kPreferences: KPreferences

    init {
        dialogLayoutResource = R.layout.layout_api_editor
        kPreferences = DependencyHolder.get(context).kPreferences
    }

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = DefaultAPIPreferenceDialogFragment.newInstance(key)
        df.setTargetFragment(fragment, 0)
        df.show(fragment.fragmentManager, key)
    }

    class DefaultAPIPreferenceDialogFragment : ThemedPreferenceDialogFragmentCompat() {

        private val editAPIUrlFormat: EditText by lazy { dialog.findViewById(R.id.editApiUrlFormat) as EditText }
        private val editSameOAuthSigningUrl: CheckBox by lazy { dialog.findViewById(R.id.editSameOAuthSigningUrl) as CheckBox }
        private val editNoVersionSuffix: CheckBox by lazy { dialog.findViewById(R.id.editNoVersionSuffix) as CheckBox }
        private val editConsumerKey: EditText by lazy { dialog.findViewById(R.id.editConsumerKey) as EditText }
        private val editConsumerSecret: EditText by lazy { dialog.findViewById(R.id.editConsumerSecret) as EditText }
        private val editAuthType: RadioGroup by lazy { dialog.findViewById(R.id.editAuthType) as RadioGroup }
        private val apiFormatHelpButton: View by lazy { dialog.findViewById(R.id.apiUrlFormatHelp) }

        private var editNoVersionSuffixChanged: Boolean = false

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val preference = preference
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.setOnShowListener { dialog ->

                editNoVersionSuffix.setOnCheckedChangeListener { buttonView, isChecked -> editNoVersionSuffixChanged = true }
                editAuthType.setOnCheckedChangeListener { group, checkedId ->
                    val authType = APIEditorActivity.getCheckedAuthType(checkedId)
                    val isOAuth = Credentials.Type.OAUTH == authType || Credentials.Type.XAUTH == authType
                    editSameOAuthSigningUrl.visibility = if (isOAuth) View.VISIBLE else View.GONE
                    editConsumerKey.visibility = if (isOAuth) View.VISIBLE else View.GONE
                    editConsumerSecret.visibility = if (isOAuth) View.VISIBLE else View.GONE
                    if (!editNoVersionSuffixChanged) {
                        editNoVersionSuffix.isChecked = Credentials.Type.EMPTY == authType
                    }
                }
                apiFormatHelpButton.setOnClickListener { Toast.makeText(context, R.string.api_url_format_help, Toast.LENGTH_LONG).show() }

                if (savedInstanceState != null) {
                    setValues(savedInstanceState.getParcelable(EXTRA_API_CONFIG))
                } else {
                    setValues(kPreferences[defaultAPIConfigKey])
                }
            }
            return dialog
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (!positiveResult) return
            kPreferences[defaultAPIConfigKey] = createCustomAPIConfig()

        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)

            val apiConfig = createCustomAPIConfig()

            outState.putParcelable(EXTRA_API_CONFIG, apiConfig)
        }

        private fun createCustomAPIConfig(): CustomAPIConfig {
            val apiConfig = CustomAPIConfig()
            apiConfig.apiUrlFormat = ParseUtils.parseString(editAPIUrlFormat.text)
            apiConfig.credentialsType = APIEditorActivity.getCheckedAuthType(editAuthType.checkedRadioButtonId)
            apiConfig.consumerKey = ParseUtils.parseString(editConsumerKey.text)
            apiConfig.consumerSecret = ParseUtils.parseString(editConsumerSecret.text)
            apiConfig.isSameOAuthUrl = editSameOAuthSigningUrl.isChecked
            apiConfig.isNoVersionSuffix = editNoVersionSuffix.isChecked
            return apiConfig
        }

        private fun setValues(apiConfig: CustomAPIConfig) {
            editAPIUrlFormat.setText(apiConfig.apiUrlFormat)
            editSameOAuthSigningUrl.isChecked = apiConfig.isSameOAuthUrl
            editNoVersionSuffix.isChecked = apiConfig.isNoVersionSuffix
            editConsumerKey.setText(apiConfig.consumerKey)
            editConsumerSecret.setText(apiConfig.consumerSecret)
            editAuthType.check(APIEditorActivity.getAuthTypeId(apiConfig.credentialsType))
            if (editAuthType.checkedRadioButtonId == -1) {
                editAuthType.check(R.id.oauth)
            }
        }

        companion object {

            fun newInstance(key: String): DefaultAPIPreferenceDialogFragment {
                val df = DefaultAPIPreferenceDialogFragment()
                val args = Bundle()
                args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
                df.arguments = args
                return df
            }
        }
    }

}
