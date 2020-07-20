package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.adapter.BaseArrayAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.setSelectedItem
import org.mariotaku.twidere.loader.DefaultAPIConfigLoader
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator

class APIEditorDialogFragment : BaseDialogFragment() {

    private val loadDefaults by lazy { dialog!!.findViewById<View>(R.id.loadDefaults) }
    private val editAPIUrlFormat by lazy { dialog!!.findViewById<EditText>(R.id.editApiUrlFormat) }
    private val editSameOAuthSigningUrl by lazy { dialog!!.findViewById<CheckBox>(R.id.editSameOAuthSigningUrl) }
    private val editNoVersionSuffix by lazy { dialog!!.findViewById<CheckBox>(R.id.editNoVersionSuffix) }
    private val editConsumerKey by lazy { dialog!!.findViewById<MaterialEditText>(R.id.editConsumerKey) }
    private val editConsumerSecret by lazy { dialog!!.findViewById<MaterialEditText>(R.id.editConsumerSecret) }
    private val editAuthType by lazy { dialog!!.findViewById<RadioGroup>(R.id.editAuthType) }
    private val apiFormatHelpButton by lazy { dialog!!.findViewById<View>(R.id.apiUrlFormatHelp) }
    private val accountTypeSpinner by lazy { dialog!!.findViewById<Spinner>(R.id.accountTypeSpinner) }

    private var editNoVersionSuffixChanged: Boolean = false
    private lateinit var apiConfig: CustomAPIConfig

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.dialog_api_editor)
        builder.setPositiveButton(R.string.action_save) { _, _ ->
            val targetFragment = this.targetFragment
            val parentFragment = this.parentFragment
            val host = this.host
            when {
                targetFragment is APIEditorCallback -> {
                    targetFragment.onSaveAPIConfig(applyCustomAPIConfig())
                }
                parentFragment is APIEditorCallback -> {
                    parentFragment.onSaveAPIConfig(applyCustomAPIConfig())
                }
                host is APIEditorCallback -> {
                    host.onSaveAPIConfig(applyCustomAPIConfig())
                }
                else -> {
                    kPreferences[defaultAPIConfigKey] = applyCustomAPIConfig()
                }
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)

        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()
            if (arguments?.getBoolean(EXTRA_SHOW_LOAD_DEFAULTS) == true) {
                loadDefaults.visibility = View.VISIBLE
            } else {
                loadDefaults.visibility = View.GONE
            }
            loadDefaults.setOnClickListener {
                val df = LoadDefaultsChooserDialogFragment()
                df.show(childFragmentManager, "load_defaults")
            }

            accountTypeSpinner.adapter = AccountTypeSpinnerAdapter(this)

            editConsumerKey.addValidator(ConsumerKeySecretValidator(requireContext().getString(R.string.invalid_consumer_key)))
            editConsumerSecret.addValidator(ConsumerKeySecretValidator(requireContext().getString(R.string.invalid_consumer_secret)))

            editNoVersionSuffix.setOnCheckedChangeListener { _, _ -> editNoVersionSuffixChanged = true }
            editAuthType.setOnCheckedChangeListener { _, checkedId ->
                val authType = getCheckedAuthType(checkedId)
                val isOAuth = Credentials.Type.OAUTH == authType || Credentials.Type.XAUTH == authType
                editSameOAuthSigningUrl.visibility = if (isOAuth) View.VISIBLE else View.GONE
                editConsumerKey.visibility = if (isOAuth) View.VISIBLE else View.GONE
                editConsumerSecret.visibility = if (isOAuth) View.VISIBLE else View.GONE
                if (!editNoVersionSuffixChanged) {
                    editNoVersionSuffix.isChecked = Credentials.Type.EMPTY == authType
                }
            }
            apiFormatHelpButton.setOnClickListener {
                MessageDialogFragment.show(childFragmentManager, message = getString(R.string.message_api_url_format_help),
                        tag = "api_url_format_help")
            }

            apiConfig = if (savedInstanceState != null) {
                savedInstanceState.getParcelable(EXTRA_API_CONFIG)!!
            } else {
                arguments?.getParcelable(EXTRA_API_CONFIG) ?: kPreferences[defaultAPIConfigKey]
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
        return apiConfig.apply {
            apiUrlFormat = ParseUtils.parseString(editAPIUrlFormat.text)
            credentialsType = getCheckedAuthType(editAuthType.checkedRadioButtonId)
            consumerKey = ParseUtils.parseString(editConsumerKey.text)
            consumerSecret = ParseUtils.parseString(editConsumerSecret.text)
            isSameOAuthUrl = editSameOAuthSigningUrl.isChecked
            isNoVersionSuffix = editNoVersionSuffix.isChecked
            type = accountTypeSpinner.selectedItem as String
        }
    }

    private fun displayCustomApiConfig() {
        editAPIUrlFormat.setText(apiConfig.apiUrlFormat)
        editSameOAuthSigningUrl.isChecked = apiConfig.isSameOAuthUrl
        editNoVersionSuffix.isChecked = apiConfig.isNoVersionSuffix
        editConsumerKey.setText(apiConfig.consumerKey)
        editConsumerSecret.setText(apiConfig.consumerSecret)
        editAuthType.check(getAuthTypeId(apiConfig.credentialsType))
        if (editAuthType.checkedRadioButtonId == -1) {
            editAuthType.check(R.id.oauth)
        }
        accountTypeSpinner.setSelectedItem(apiConfig.type)
    }

    interface APIEditorCallback {
        fun onSaveAPIConfig(config: CustomAPIConfig)
    }


    class LoadDefaultsChooserDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener,
            LoaderManager.LoaderCallbacks<List<CustomAPIConfig>> {
        private lateinit var adapter: ArrayAdapter<CustomAPIConfig>

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            adapter = CustomAPIConfigArrayAdapter(requireContext())
            val builder = AlertDialog.Builder(requireContext())
            builder.setAdapter(adapter, this)
            LoaderManager.getInstance(this).initLoader(0, null, this)
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

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<CustomAPIConfig>> {
            return DefaultAPIConfigLoader(requireContext())
        }

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
                view.findViewById<TextView>(android.R.id.text1).text = type.getLocalizedName(context)
                return view
            }

        }
    }

    private class AccountTypeSpinnerAdapter(
            fragment: APIEditorDialogFragment
    ) : BaseArrayAdapter<String>(fragment.requireContext(), R.layout.support_simple_spinner_dropdown_item,
            requestManager = fragment.requestManager) {
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