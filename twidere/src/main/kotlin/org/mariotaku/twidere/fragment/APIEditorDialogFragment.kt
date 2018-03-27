package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_api_editor.*
import kotlinx.android.synthetic.main.layout_api_editor_advanced_fields.*
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.string
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.setSelectedItem
import org.mariotaku.twidere.loader.DefaultAPIConfigLoader
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator

class APIEditorDialogFragment : BaseDialogFragment() {

    private lateinit var apiConfig: CustomAPIConfig

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setView(R.layout.dialog_api_editor)
        builder.setPositiveButton(R.string.action_save) { _, _ ->
            val targetFragment = this.targetFragment
            val parentFragment = this.parentFragment
            val host = this.host
            if (targetFragment is APIEditorCallback) {
                targetFragment.onSaveAPIConfig(applyCustomAPIConfig())
            } else if (parentFragment is APIEditorCallback) {
                parentFragment.onSaveAPIConfig(applyCustomAPIConfig())
            } else if (host is APIEditorCallback) {
                host.onSaveAPIConfig(applyCustomAPIConfig())
            } else {
                PreferencesSingleton.get(context!!)[defaultAPIConfigKey] = applyCustomAPIConfig()
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)

        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()
            if (arguments?.getBoolean(EXTRA_SHOW_LOAD_DEFAULTS) == true) {
                it.loadDefaults.visibility = View.VISIBLE
            } else {
                it.loadDefaults.visibility = View.GONE
            }
            it.loadDefaults.setOnClickListener {
                val df = LoadDefaultsChooserDialogFragment()
                df.show(childFragmentManager, "load_defaults")
            }

            it.accountTypeSpinner.adapter = AccountTypeSpinnerAdapter(this)

            it.editConsumerKey.addValidator(ConsumerKeySecretValidator(getString(R.string.invalid_consumer_key)))
            it.editConsumerSecret.addValidator(ConsumerKeySecretValidator(getString(R.string.invalid_consumer_secret)))

            it.editAuthType.setOnCheckedChangeListener { _, checkedId ->
                val authType = getCheckedAuthType(checkedId)
                val isOAuth = Credentials.Type.OAUTH == authType || Credentials.Type.XAUTH == authType
                it.editSameOAuthSigningUrl.visibility = if (isOAuth) View.VISIBLE else View.GONE
                it.editConsumerKey.visibility = if (isOAuth) View.VISIBLE else View.GONE
                it.editConsumerSecret.visibility = if (isOAuth) View.VISIBLE else View.GONE
            }
            it.apiUrlFormatHelp.setOnClickListener {
                MessageDialogFragment.show(childFragmentManager, message = getString(R.string.message_api_url_format_help),
                        tag = "api_url_format_help")
            }

            if (savedInstanceState != null) {
                apiConfig = savedInstanceState.getParcelable(EXTRA_API_CONFIG)
            } else {
                apiConfig = arguments?.getParcelable(EXTRA_API_CONFIG) ?: PreferencesSingleton.get(context!!)[defaultAPIConfigKey]
            }
            displayCustomApiConfig()
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_API_CONFIG, applyCustomAPIConfig())
    }

    private fun applyCustomAPIConfig(): CustomAPIConfig {
        val dialog = dialog!!
        return apiConfig.apply {
            apiUrlFormat = dialog.editApiUrlFormat.string
            credentialsType = getCheckedAuthType(dialog.editAuthType.checkedRadioButtonId)
            consumerKey = dialog.editConsumerKey.string
            consumerSecret = dialog.editConsumerSecret.string
            isSameOAuthUrl = dialog.editSameOAuthSigningUrl.isChecked
            isNoVersionSuffix = dialog.editNoVersionSuffix.isChecked
            type = dialog.accountTypeSpinner.selectedItem as String
        }
    }

    private fun displayCustomApiConfig() {
        val dialog = dialog!!
        dialog.editApiUrlFormat.setText(apiConfig.apiUrlFormat)
        dialog.editSameOAuthSigningUrl.isChecked = apiConfig.isSameOAuthUrl
        dialog.editNoVersionSuffix.isChecked = apiConfig.isNoVersionSuffix
        dialog.editConsumerKey.setText(apiConfig.consumerKey)
        dialog.editConsumerSecret.setText(apiConfig.consumerSecret)
        dialog.editAuthType.check(getAuthTypeId(apiConfig.credentialsType))
        if (dialog.editAuthType.checkedRadioButtonId == -1) {
            dialog.editAuthType.check(R.id.oauth)
        }
        dialog.accountTypeSpinner.setSelectedItem(apiConfig.type)
    }

    interface APIEditorCallback {
        fun onSaveAPIConfig(config: CustomAPIConfig)
    }


    class LoadDefaultsChooserDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener,
            LoaderManager.LoaderCallbacks<List<CustomAPIConfig>> {
        private lateinit var adapter: ArrayAdapter<CustomAPIConfig>

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            adapter = CustomAPIConfigArrayAdapter(context!!)
            val builder = AlertDialog.Builder(context!!)
            builder.setAdapter(adapter, this)
            loaderManager.initLoader(0, null, this)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val fragment = parentFragment as APIEditorDialogFragment
            fragment.apiConfig = adapter.getItem(which)
            fragment.displayCustomApiConfig()
            dismiss()
        }

        override fun onCreateLoader(id: Int, args: Bundle?) = DefaultAPIConfigLoader(context!!)

        override fun onLoadFinished(loader: Loader<List<CustomAPIConfig>>, data: List<CustomAPIConfig>) {
            adapter.clear()
            adapter.addAll(data)
        }

        override fun onLoaderReset(loader: Loader<List<CustomAPIConfig>?>) {

        }

        private class CustomAPIConfigArrayAdapter(context: Context) :
                ArrayAdapter<CustomAPIConfig>(context, android.R.layout.simple_list_item_1) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val type = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = type.name
                return view
            }

        }
    }

    private class AccountTypeSpinnerAdapter(
            fragment: APIEditorDialogFragment
    ) : ArrayAdapter<String>(fragment.context!!, R.layout.support_simple_spinner_dropdown_item) {
        init {
            add(AccountType.TWITTER)
            add(AccountType.FANFOU)
            add(AccountType.MASTODON)
            add(AccountType.STATUSNET)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = getTypeTitle(context, getItem(position))
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = getTypeTitle(context, getItem(position))
            return view
        }

    }

    companion object {

        const val EXTRA_SHOW_LOAD_DEFAULTS = "show_load_defaults"

        @Credentials.Type
        fun getCheckedAuthType(checkedId: Int): String {
            when (checkedId) {
                R.id.xauth -> {
                    return Credentials.Type.XAUTH
                }
                R.id.basic -> {
                    return Credentials.Type.BASIC
                }
                R.id.twipO -> {
                    return Credentials.Type.EMPTY
                }
                R.id.oauth2 -> {
                    return Credentials.Type.OAUTH2
                }
                else -> {
                    return Credentials.Type.OAUTH
                }
            }
        }

        fun getAuthTypeId(authType: String): Int {
            when (authType) {
                Credentials.Type.XAUTH -> {
                    return R.id.xauth
                }
                Credentials.Type.BASIC -> {
                    return R.id.basic
                }
                Credentials.Type.EMPTY -> {
                    return R.id.twipO
                }
                Credentials.Type.OAUTH2 -> {
                    return R.id.oauth2
                }
                else -> {
                    return R.id.oauth
                }
            }
        }

        fun getTypeTitle(context: Context, @AccountType type: String): CharSequence {
            return when (type) {
                AccountType.TWITTER -> context.getString(R.string.account_type_twitter)
                AccountType.FANFOU -> context.getString(R.string.account_type_fanfou)
                AccountType.MASTODON -> context.getString(R.string.account_type_mastodon)
                AccountType.STATUSNET -> context.getString(R.string.account_type_statusnet)
                else -> throw UnsupportedOperationException(type)
            }
        }
    }

}