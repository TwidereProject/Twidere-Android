package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.FixedAsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bluelinelabs.logansquare.LoganSquare
import com.bumptech.glide.Glide
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.adapter.BaseArrayAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.setSelectedItem
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator
import java.io.IOException
import javax.inject.Inject

class APIEditorDialogFragment : BaseDialogFragment() {

    private val loadDefaults by lazy { dialog.findViewById(R.id.loadDefaults) }
    private val editAPIUrlFormat by lazy { dialog.findViewById(R.id.editApiUrlFormat) as EditText }
    private val editSameOAuthSigningUrl by lazy { dialog.findViewById(R.id.editSameOAuthSigningUrl) as CheckBox }
    private val editNoVersionSuffix by lazy { dialog.findViewById(R.id.editNoVersionSuffix) as CheckBox }
    private val editConsumerKey by lazy { dialog.findViewById(R.id.editConsumerKey) as MaterialEditText }
    private val editConsumerSecret by lazy { dialog.findViewById(R.id.editConsumerSecret) as MaterialEditText }
    private val editAuthType by lazy { dialog.findViewById(R.id.editAuthType) as RadioGroup }
    private val apiFormatHelpButton by lazy { dialog.findViewById(R.id.apiUrlFormatHelp) }
    private val accountTypeSpinner by lazy { dialog.findViewById(R.id.accountTypeSpinner) as Spinner }

    private var editNoVersionSuffixChanged: Boolean = false
    private lateinit var apiConfig: CustomAPIConfig

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_api_editor)
        builder.setPositiveButton(R.string.action_save) { dialog, which ->
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
                kPreferences[defaultAPIConfigKey] = applyCustomAPIConfig()
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            it as AlertDialog
            it.applyTheme()
            if (arguments?.getBoolean(EXTRA_SHOW_LOAD_DEFAULTS) ?: false) {
                loadDefaults.visibility = View.VISIBLE
            } else {
                loadDefaults.visibility = View.GONE
            }
            loadDefaults.setOnClickListener {
                val df = LoadDefaultsChooserDialogFragment()
                df.show(childFragmentManager, "load_defaults")
            }

            accountTypeSpinner.adapter = AccountTypeSpinnerAdapter(this)

            editConsumerKey.addValidator(ConsumerKeySecretValidator(context.getString(R.string.invalid_consumer_key)))
            editConsumerSecret.addValidator(ConsumerKeySecretValidator(context.getString(R.string.invalid_consumer_secret)))

            editNoVersionSuffix.setOnCheckedChangeListener { buttonView, isChecked -> editNoVersionSuffixChanged = true }
            editAuthType.setOnCheckedChangeListener { group, checkedId ->
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

            if (savedInstanceState != null) {
                apiConfig = savedInstanceState.getParcelable(EXTRA_API_CONFIG)
            } else {
                apiConfig = arguments?.getParcelable(EXTRA_API_CONFIG) ?: kPreferences[defaultAPIConfigKey]
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
            LoaderManager.LoaderCallbacks<List<CustomAPIConfig>?> {
        private lateinit var adapter: ArrayAdapter<CustomAPIConfig>

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val configs = CustomAPIConfig.listDefault(context)
            adapter = CustomAPIConfigArrayAdapter(context, configs)
            val builder = AlertDialog.Builder(context)
            builder.setAdapter(adapter, this)
            loaderManager.initLoader(0, null, this)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val fragment = parentFragment as APIEditorDialogFragment
            fragment.apiConfig = adapter.getItem(which)
            fragment.displayCustomApiConfig()
            dismiss()
        }

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<CustomAPIConfig>?> {
            return DefaultAPIConfigLoader(context)
        }

        override fun onLoadFinished(loader: Loader<List<CustomAPIConfig>?>, data: List<CustomAPIConfig>?) {
            if (data != null) {
                adapter.clear()
                adapter.addAll(data)
            }
        }

        override fun onLoaderReset(loader: Loader<List<CustomAPIConfig>?>) {

        }

        class DefaultAPIConfigLoader(context: Context) : FixedAsyncTaskLoader<List<CustomAPIConfig>?>(context) {
            @Inject
            lateinit var client: RestHttpClient

            init {
                GeneralComponentHelper.build(context).inject(this)
            }

            override fun loadInBackground(): List<CustomAPIConfig>? {
                val request = HttpRequest(GET.METHOD, DEFAULT_API_CONFIGS_URL,
                        null, null, null)
                try {
                    client.newCall(request).execute().use { response ->
                        // Save to cache
                        if (!response.isSuccessful) {
                            return null
                        }
                        // Save to cache
                        return LoganSquare.parseList(response.body.stream(), CustomAPIConfig::class.java)
                    }
                } catch (e: IOException) {
                    // Ignore
                }
                return null
            }

            override fun onStartLoading() {
                forceLoad()
            }

            companion object {
                const val DEFAULT_API_CONFIGS_URL = "https://twidere.mariotaku.org/assets/data/default_api_configs.json"
            }
        }

        private inner class CustomAPIConfigArrayAdapter(
                context: Context,
                defaultItems: List<CustomAPIConfig>
        ) : ArrayAdapter<CustomAPIConfig>(context, android.R.layout.simple_list_item_1, defaultItems) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val type = getItem(position)
                (view.findViewById(android.R.id.text1) as TextView).text = type.getLocalizedName(context)
                return view
            }

        }
    }

    private class AccountTypeSpinnerAdapter(
            fragment: APIEditorDialogFragment
    ) : BaseArrayAdapter<String>(fragment.context, R.layout.support_simple_spinner_dropdown_item,
            getRequestManager = { Glide.with(fragment) }) {
        init {
            add(AccountType.TWITTER)
            add(AccountType.FANFOU)
            add(AccountType.STATUSNET)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById(android.R.id.text1) as TextView
            textView.text = getTypeTitle(getItem(position))
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view.findViewById(android.R.id.text1) as TextView
            textView.text = getTypeTitle(getItem(position))
            return view
        }

        private fun getTypeTitle(item: String): CharSequence {
            return when (item) {
                AccountType.TWITTER -> context.getString(R.string.account_type_twitter)
                AccountType.FANFOU -> context.getString(R.string.account_type_fanfou)
                AccountType.STATUSNET -> context.getString(R.string.account_type_statusnet)
                else -> throw UnsupportedOperationException(item)
            }
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
                else -> {
                    return R.id.oauth
                }
            }
        }
    }

}