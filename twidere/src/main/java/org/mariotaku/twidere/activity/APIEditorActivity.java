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

package org.mariotaku.twidere.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.model.CustomAPIConfig;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

public class APIEditorActivity extends BaseActivity implements OnCheckedChangeListener,
        OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText mEditAPIUrlFormat;
    private CheckBox mEditSameOAuthSigningUrl, mEditNoVersionSuffix;
    private EditText mEditConsumerKey, mEditConsumerSecret;
    private RadioGroup mEditAuthType;
    private RadioButton mButtonOAuth, mButtonXAuth, mButtonBasic, mButtonTWIPOMode;
    private Button mSaveButton;
    private Button mLoadDefaultsButton;
    private View mAPIFormatHelpButton;
    private boolean mEditNoVersionSuffixChanged;

    public static int getCheckedAuthType(final int checkedId) {
        switch (checkedId) {
            case R.id.xauth: {
                return ParcelableCredentials.AuthType.XAUTH;
            }
            case R.id.basic: {
                return ParcelableCredentials.AuthType.BASIC;
            }
            case R.id.twip_o: {
                return ParcelableCredentials.AuthType.TWIP_O_MODE;
            }
            default: {
                return ParcelableCredentials.AuthType.OAUTH;
            }
        }
    }

    @Override
    public void onCheckedChanged(final RadioGroup group, final int checkedId) {
        final int authType = getCheckedAuthType(checkedId);
        final boolean isOAuth = authType == ParcelableCredentials.AuthType.OAUTH || authType == ParcelableCredentials.AuthType.XAUTH;
        mEditSameOAuthSigningUrl.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
        mEditConsumerKey.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
        mEditConsumerSecret.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
        if (!mEditNoVersionSuffixChanged) {
            mEditNoVersionSuffix.setChecked(authType == ParcelableCredentials.AuthType.TWIP_O_MODE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mEditNoVersionSuffixChanged = true;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.save: {
                if (checkApiUrl()) {
                    saveAndFinish();
                } else {
                    mEditAPIUrlFormat.setError(getString(R.string.wrong_url_format));
                }
                break;
            }
            case R.id.api_url_format_help: {
                Toast.makeText(this, R.string.api_url_format_help, Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.load_defaults: {
                final LoadDefaultsChooserDialogFragment df = new LoadDefaultsChooserDialogFragment();
                df.show(getSupportFragmentManager(), "load_defaults");
                break;
            }
        }
    }

    private boolean checkApiUrl() {
        return MicroBlogAPIFactory.verifyApiFormat(String.valueOf(mEditAPIUrlFormat.getText()));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mLoadDefaultsButton = (Button) findViewById(R.id.load_defaults);
        mEditAPIUrlFormat = (EditText) findViewById(R.id.api_url_format);
        mEditAuthType = (RadioGroup) findViewById(R.id.auth_type);
        mButtonOAuth = (RadioButton) findViewById(R.id.oauth);
        mButtonXAuth = (RadioButton) findViewById(R.id.xauth);
        mButtonBasic = (RadioButton) findViewById(R.id.basic);
        mButtonTWIPOMode = (RadioButton) findViewById(R.id.twip_o);
        mEditSameOAuthSigningUrl = (CheckBox) findViewById(R.id.same_oauth_signing_url);
        mEditNoVersionSuffix = (CheckBox) findViewById(R.id.no_version_suffix);
        mEditConsumerKey = (EditText) findViewById(R.id.consumer_key);
        mEditConsumerSecret = (EditText) findViewById(R.id.consumer_secret);
        mSaveButton = (Button) findViewById(R.id.save);
        mAPIFormatHelpButton = findViewById(R.id.api_url_format_help);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        final String apiUrlFormat = ParseUtils.parseString(mEditAPIUrlFormat.getText());
        final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
        final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
        final boolean noVersionSuffix = mEditNoVersionSuffix.isChecked();
        final String consumerKey = ParseUtils.parseString(mEditConsumerKey.getText());
        final String consumerSecret = ParseUtils.parseString(mEditConsumerSecret.getText());
        outState.putString(Accounts.API_URL_FORMAT, apiUrlFormat);
        outState.putInt(Accounts.AUTH_TYPE, authType);
        outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
        outState.putBoolean(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        outState.putString(Accounts.CONSUMER_KEY, consumerKey);
        outState.putString(Accounts.CONSUMER_SECRET, consumerSecret);
        super.onSaveInstanceState(outState);
    }

    public void saveAndFinish() {
        final String apiUrlFormat = ParseUtils.parseString(mEditAPIUrlFormat.getText());
        final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
        final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
        final boolean noVersionSuffix = mEditNoVersionSuffix.isChecked();
        final String consumerKey = ParseUtils.parseString(mEditConsumerKey.getText());
        final String consumerSecret = ParseUtils.parseString(mEditConsumerSecret.getText());
        final Intent intent = new Intent();
        intent.putExtra(Accounts.API_URL_FORMAT, apiUrlFormat);
        intent.putExtra(Accounts.AUTH_TYPE, authType);
        intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
        intent.putExtra(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        intent.putExtra(Accounts.CONSUMER_KEY, consumerKey);
        intent.putExtra(Accounts.CONSUMER_SECRET, consumerSecret);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        setContentView(R.layout.activity_api_editor);

        String apiUrlFormat;
        int authType;
        boolean sameOAuthSigningUrl, noVersionSuffix;
        String consumerKey, consumerSecret;

        final SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String prefApiUrlFormat = Utils.getNonEmptyString(pref, KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT);
        final int prefAuthType = pref.getInt(KEY_AUTH_TYPE, ParcelableCredentials.AuthType.OAUTH);
        final boolean prefSameOAuthSigningUrl = pref.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false);
        final boolean prefNoVersionSuffix = pref.getBoolean(KEY_NO_VERSION_SUFFIX, false);
        final String prefConsumerKey = Utils.getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
        final String prefConsumerSecret = Utils.getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
        final Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else if (extras != null) {
            bundle = extras;
        } else {
            bundle = new Bundle();
        }
        apiUrlFormat = Utils.trim(bundle.getString(Accounts.API_URL_FORMAT, prefApiUrlFormat));
        authType = bundle.getInt(Accounts.AUTH_TYPE, prefAuthType);
        sameOAuthSigningUrl = bundle.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL, prefSameOAuthSigningUrl);
        noVersionSuffix = bundle.getBoolean(Accounts.NO_VERSION_SUFFIX, prefNoVersionSuffix);
        consumerKey = Utils.trim(bundle.getString(Accounts.CONSUMER_KEY, prefConsumerKey));
        consumerSecret = Utils.trim(bundle.getString(Accounts.CONSUMER_SECRET, prefConsumerSecret));

        mEditAuthType.setOnCheckedChangeListener(this);
        mEditNoVersionSuffix.setOnCheckedChangeListener(this);
        mSaveButton.setOnClickListener(this);
        mAPIFormatHelpButton.setOnClickListener(this);

        mLoadDefaultsButton.setVisibility(View.VISIBLE);
        mLoadDefaultsButton.setOnClickListener(this);

        mEditAPIUrlFormat.setText(apiUrlFormat);
        mEditSameOAuthSigningUrl.setChecked(sameOAuthSigningUrl);
        mEditNoVersionSuffix.setChecked(noVersionSuffix);
        mEditConsumerKey.setText(consumerKey);
        mEditConsumerSecret.setText(consumerSecret);

        mButtonOAuth.setChecked(authType == ParcelableCredentials.AuthType.OAUTH);
        mButtonXAuth.setChecked(authType == ParcelableCredentials.AuthType.XAUTH);
        mButtonBasic.setChecked(authType == ParcelableCredentials.AuthType.BASIC);
        mButtonTWIPOMode.setChecked(authType == ParcelableCredentials.AuthType.TWIP_O_MODE);
        if (mEditAuthType.getCheckedRadioButtonId() == -1) {
            mButtonOAuth.setChecked(true);
        }
    }


    private int getAuthTypeId(final int authType) {
        switch (authType) {
            case ParcelableCredentials.AuthType.XAUTH: {
                return R.id.xauth;
            }
            case ParcelableCredentials.AuthType.BASIC: {
                return R.id.basic;
            }
            case ParcelableCredentials.AuthType.TWIP_O_MODE: {
                return R.id.twip_o;
            }
            default: {
                return R.id.oauth;
            }
        }
    }

    private void setAPIConfig(CustomAPIConfig apiConfig) {
        mEditAPIUrlFormat.setText(apiConfig.getApiUrlFormat());
        mEditAuthType.check(getAuthTypeId(apiConfig.getAuthType()));
        mEditSameOAuthSigningUrl.setChecked(apiConfig.isSameOAuthUrl());
        mEditNoVersionSuffix.setChecked(apiConfig.isNoVersionSuffix());
        mEditConsumerKey.setText(apiConfig.getConsumerKey());
        mEditConsumerSecret.setText(apiConfig.getConsumerSecret());
    }

    public static class LoadDefaultsChooserDialogFragment extends BaseDialogFragment
            implements DialogInterface.OnClickListener, LoaderManager.LoaderCallbacks<List<CustomAPIConfig>> {
        private ArrayAdapter<CustomAPIConfig> mAdapter;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getContext();
            List<CustomAPIConfig> configs = CustomAPIConfig.listDefault(context);
            mAdapter = new CustomAPIConfigArrayAdapter(context, configs);
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
            builder.setAdapter(mAdapter, this);
            if (!BuildConfig.DEBUG) {
                getLoaderManager().initLoader(0, null, this);
            }
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ((APIEditorActivity) getActivity()).setAPIConfig(mAdapter.getItem(which));
            dismiss();
        }

        @Override
        public Loader<List<CustomAPIConfig>> onCreateLoader(int id, Bundle args) {
            return new DefaultAPIConfigLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<CustomAPIConfig>> loader, List<CustomAPIConfig> data) {
            if (data != null) {
                mAdapter.clear();
                mAdapter.addAll(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<CustomAPIConfig>> loader) {

        }

        public static class DefaultAPIConfigLoader extends AsyncTaskLoader<List<CustomAPIConfig>> {
            public static final String DEFAULT_API_CONFIGS_URL = "https://raw.githubusercontent.com/TwidereProject/Twidere-Android/master/twidere/src/main/assets/data/default_api_configs.json";
            @Inject
            RestHttpClient mClient;

            public DefaultAPIConfigLoader(Context context) {
                super(context);
                GeneralComponentHelper.build(context).inject(this);
            }

            @Override
            public List<CustomAPIConfig> loadInBackground() {
                HttpRequest request = new HttpRequest(GET.METHOD, DEFAULT_API_CONFIGS_URL,
                        null, null, null);
                HttpResponse response = null;
                try {
                    response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        final InputStream is = response.getBody().stream();
                        return JsonSerializer.parseList(is, CustomAPIConfig.class);
                    }
                } catch (IOException e) {
                    // Ignore
                } finally {
                    Utils.closeSilently(response);
                }
                return null;
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        }

        private class CustomAPIConfigArrayAdapter extends ArrayAdapter<CustomAPIConfig> {
            public CustomAPIConfigArrayAdapter(Context context, List<CustomAPIConfig> defaultItems) {
                super(context, R.layout.md_listitem, defaultItems);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                CustomAPIConfig config = getItem(position);
                ((TextView) view.findViewById(com.afollestad.materialdialogs.R.id.title)).setText(config.getLocalizedName(getContext()));
                return view;
            }
        }
    }
}
