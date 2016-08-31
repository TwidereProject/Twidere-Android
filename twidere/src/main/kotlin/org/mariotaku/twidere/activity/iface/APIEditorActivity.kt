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

package org.mariotaku.twidere.activity.iface

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_api_editor.*
import kotlinx.android.synthetic.main.layout_api_editor.*
import kotlinx.android.synthetic.main.layout_api_editor_advanced_fields.*
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.HttpResponse
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.IOException
import javax.inject.Inject

class APIEditorActivity : BaseActivity(), OnCheckedChangeListener, OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var editNoVersionSuffixChanged: Boolean = false

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val authType = getCheckedAuthType(checkedId)
        val isOAuth = authType == ParcelableCredentials.AuthType.OAUTH || authType == ParcelableCredentials.AuthType.XAUTH
        editSameOAuthSigningUrl.visibility = if (isOAuth) View.VISIBLE else View.GONE
        editConsumerKey.visibility = if (isOAuth) View.VISIBLE else View.GONE
        editConsumerSecret.visibility = if (isOAuth) View.VISIBLE else View.GONE
        if (!editNoVersionSuffixChanged) {
            editNoVersionSuffix.isChecked = authType == ParcelableCredentials.AuthType.TWIP_O_MODE
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        editNoVersionSuffixChanged = true
    }

    override fun onClick(v: View) {
        when (v) {
            save -> {
                if (checkApiUrl()) {
                    saveAndFinish()
                } else {
                    editApiUrlFormat.error = getString(R.string.wrong_url_format)
                }
            }
            apiUrlFormatHelp -> {
                Toast.makeText(this, R.string.api_url_format_help, Toast.LENGTH_LONG).show()
            }
            loadDefaults -> {
                val df = LoadDefaultsChooserDialogFragment()
                df.show(supportFragmentManager, "load_defaults")
            }
        }
    }

    private fun checkApiUrl(): Boolean {
        return MicroBlogAPIFactory.verifyApiFormat(editApiUrlFormat.text.toString())
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        val apiUrlFormat = ParseUtils.parseString(this.editApiUrlFormat.text)
        val authType = getCheckedAuthType(this.editAuthType.checkedRadioButtonId)
        val sameOAuthSigningUrl = this.editSameOAuthSigningUrl.isChecked
        val noVersionSuffix = this.editNoVersionSuffix.isChecked
        val consumerKey = ParseUtils.parseString(this.editConsumerKey.text)
        val consumerSecret = ParseUtils.parseString(this.editConsumerSecret.text)
        outState.putString(Accounts.API_URL_FORMAT, apiUrlFormat)
        outState.putInt(Accounts.AUTH_TYPE, authType)
        outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
        outState.putBoolean(Accounts.NO_VERSION_SUFFIX, noVersionSuffix)
        outState.putString(Accounts.CONSUMER_KEY, consumerKey)
        outState.putString(Accounts.CONSUMER_SECRET, consumerSecret)
        super.onSaveInstanceState(outState)
    }

    fun saveAndFinish() {
        val apiUrlFormat = ParseUtils.parseString(this.editApiUrlFormat.text)
        val authType = getCheckedAuthType(this.editAuthType.checkedRadioButtonId)
        val sameOAuthSigningUrl = this.editSameOAuthSigningUrl.isChecked
        val noVersionSuffix = this.editNoVersionSuffix.isChecked
        val consumerKey = ParseUtils.parseString(this.editConsumerKey.text)
        val consumerSecret = ParseUtils.parseString(this.editConsumerSecret.text)
        val intent = Intent()
        intent.putExtra(Accounts.API_URL_FORMAT, apiUrlFormat)
        intent.putExtra(Accounts.AUTH_TYPE, authType)
        intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
        intent.putExtra(Accounts.NO_VERSION_SUFFIX, noVersionSuffix)
        intent.putExtra(Accounts.CONSUMER_KEY, consumerKey)
        intent.putExtra(Accounts.CONSUMER_SECRET, consumerSecret)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val extras = intent.extras

        setContentView(R.layout.activity_api_editor)

        val apiUrlFormat: String
        val authType: Int
        val sameOAuthSigningUrl: Boolean
        val noVersionSuffix: Boolean
        val consumerKey: String
        val consumerSecret: String

        val pref = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val prefApiUrlFormat = Utils.getNonEmptyString(pref, KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val prefAuthType = pref.getInt(KEY_AUTH_TYPE, ParcelableCredentials.AuthType.OAUTH)
        val prefSameOAuthSigningUrl = pref.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val prefNoVersionSuffix = pref.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val prefConsumerKey = Utils.getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY)
        val prefConsumerSecret = Utils.getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET)
        val bundle: Bundle
        if (savedInstanceState != null) {
            bundle = savedInstanceState
        } else if (extras != null) {
            bundle = extras
        } else {
            bundle = Bundle()
        }
        apiUrlFormat = Utils.trim(bundle.getString(Accounts.API_URL_FORMAT, prefApiUrlFormat))
        authType = bundle.getInt(Accounts.AUTH_TYPE, prefAuthType)
        sameOAuthSigningUrl = bundle.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL, prefSameOAuthSigningUrl)
        noVersionSuffix = bundle.getBoolean(Accounts.NO_VERSION_SUFFIX, prefNoVersionSuffix)
        consumerKey = Utils.trim(bundle.getString(Accounts.CONSUMER_KEY, prefConsumerKey))
        consumerSecret = Utils.trim(bundle.getString(Accounts.CONSUMER_SECRET, prefConsumerSecret))

        editAuthType.setOnCheckedChangeListener(this)
        editNoVersionSuffix.setOnCheckedChangeListener(this)
        save.setOnClickListener(this)
        apiUrlFormatHelp.setOnClickListener(this)

        loadDefaults.visibility = View.VISIBLE
        loadDefaults.setOnClickListener(this)

        editApiUrlFormat.setText(apiUrlFormat)
        editSameOAuthSigningUrl.isChecked = sameOAuthSigningUrl
        editNoVersionSuffix.isChecked = noVersionSuffix
        editConsumerKey.setText(consumerKey)
        editConsumerSecret.setText(consumerSecret)

        oauth.isChecked = authType == ParcelableCredentials.AuthType.OAUTH
        xauth.isChecked = authType == ParcelableCredentials.AuthType.XAUTH
        basic.isChecked = authType == ParcelableCredentials.AuthType.BASIC
        twipO.isChecked = authType == ParcelableCredentials.AuthType.TWIP_O_MODE
        if (editAuthType.checkedRadioButtonId == -1) {
            oauth.isChecked = true
        }
    }


    private fun getAuthTypeId(authType: Int): Int {
        when (authType) {
            ParcelableCredentials.AuthType.XAUTH -> {
                return R.id.xauth
            }
            ParcelableCredentials.AuthType.BASIC -> {
                return R.id.basic
            }
            ParcelableCredentials.AuthType.TWIP_O_MODE -> {
                return R.id.twipO
            }
            else -> {
                return R.id.oauth
            }
        }
    }

    private fun setAPIConfig(apiConfig: CustomAPIConfig) {
        editApiUrlFormat.setText(apiConfig.apiUrlFormat)
        editAuthType.check(getAuthTypeId(apiConfig.authType))
        editSameOAuthSigningUrl.isChecked = apiConfig.isSameOAuthUrl
        editNoVersionSuffix.isChecked = apiConfig.isNoVersionSuffix
        editConsumerKey.setText(apiConfig.consumerKey)
        editConsumerSecret.setText(apiConfig.consumerSecret)
    }

    class LoadDefaultsChooserDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener,
            LoaderManager.LoaderCallbacks<List<CustomAPIConfig>?> {
        private lateinit var adapter: ArrayAdapter<CustomAPIConfig>

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context
            val configs = CustomAPIConfig.listDefault(context)
            adapter = CustomAPIConfigArrayAdapter(context, configs)
            val builder = AlertDialog.Builder(context)
            builder.setAdapter(adapter, this)
            if (!BuildConfig.DEBUG) {
                loaderManager.initLoader(0, null, this)
            }
            return builder.create()
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            (activity as APIEditorActivity).setAPIConfig(adapter.getItem(which))
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

        class DefaultAPIConfigLoader(context: Context) : AsyncTaskLoader<List<CustomAPIConfig>?>(context) {
            @Inject
            lateinit var client: RestHttpClient

            init {
                GeneralComponentHelper.build(context).inject(this)
            }

            override fun loadInBackground(): List<CustomAPIConfig>? {
                val request = HttpRequest(GET.METHOD, DEFAULT_API_CONFIGS_URL,
                        null, null, null)
                var response: HttpResponse? = null
                try {
                    response = client.newCall(request).execute()
                    if (response!!.isSuccessful) {
                        val `is` = response.body.stream()
                        return JsonSerializer.parseList(`is`, CustomAPIConfig::class.java)
                    }
                } catch (e: IOException) {
                    // Ignore
                } finally {
                    Utils.closeSilently(response)
                }
                return null
            }

            override fun onStartLoading() {
                forceLoad()
            }

            companion object {
                val DEFAULT_API_CONFIGS_URL = "https://raw.githubusercontent.com/TwidereProject/Twidere-Android/master/twidere/src/main/assets/data/default_api_configs.json"
            }
        }

        private inner class CustomAPIConfigArrayAdapter(context: Context, defaultItems: List<CustomAPIConfig>) : ArrayAdapter<CustomAPIConfig>(context, android.R.layout.simple_list_item_1, defaultItems) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val config = getItem(position)
                (view.findViewById(android.R.id.text1) as TextView).text = config.getLocalizedName(context)
                return view
            }
        }
    }

    companion object {

        fun getCheckedAuthType(checkedId: Int): Int {
            when (checkedId) {
                R.id.xauth -> {
                    return ParcelableCredentials.AuthType.XAUTH
                }
                R.id.basic -> {
                    return ParcelableCredentials.AuthType.BASIC
                }
                R.id.twipO -> {
                    return ParcelableCredentials.AuthType.TWIP_O_MODE
                }
                else -> {
                    return ParcelableCredentials.AuthType.OAUTH
                }
            }
        }
    }
}
