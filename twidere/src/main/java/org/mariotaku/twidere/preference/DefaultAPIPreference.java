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

package org.mariotaku.twidere.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.APIEditorActivity;
import org.mariotaku.twidere.fragment.ThemedPreferenceDialogFragmentCompat;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.preference.iface.IDialogPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.ParseUtils;

import static org.mariotaku.twidere.util.Utils.trim;

public class DefaultAPIPreference extends DialogPreference implements Constants, IDialogPreference {

    public DefaultAPIPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public DefaultAPIPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setDialogLayoutResource(R.layout.layout_api_editor);
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        DefaultAPIPreferenceDialogFragment df = DefaultAPIPreferenceDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }

    public static final class DefaultAPIPreferenceDialogFragment extends ThemedPreferenceDialogFragmentCompat {

        public static DefaultAPIPreferenceDialogFragment newInstance(String key) {
            final DefaultAPIPreferenceDialogFragment df = new DefaultAPIPreferenceDialogFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        private EditText mEditAPIUrlFormat;
        private CheckBox mEditSameOAuthSigningUrl, mEditNoVersionSuffix;
        private EditText mEditConsumerKey, mEditConsumerSecret;
        private RadioGroup mEditAuthType;
        private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
        private View mAPIFormatHelpButton;
        private boolean mEditNoVersionSuffixChanged;

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final DialogPreference preference = getPreference();
            final Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Dialog editDialog = (Dialog) dialog;
                    mEditAPIUrlFormat = (EditText) editDialog.findViewById(R.id.editApiUrlFormat);
                    mEditAuthType = (RadioGroup) editDialog.findViewById(R.id.editAuthType);
                    mButtonOAuth = (RadioButton) editDialog.findViewById(R.id.oauth);
                    mButtonxAuth = (RadioButton) editDialog.findViewById(R.id.xauth);
                    mButtonBasic = (RadioButton) editDialog.findViewById(R.id.basic);
                    mButtonTwipOMode = (RadioButton) editDialog.findViewById(R.id.twipO);
                    mEditSameOAuthSigningUrl = (CheckBox) editDialog.findViewById(R.id.editSameOAuthSigningUrl);
                    mEditNoVersionSuffix = (CheckBox) editDialog.findViewById(R.id.editNoVersionSuffix);
                    mEditConsumerKey = (EditText) editDialog.findViewById(R.id.editConsumerKey);
                    mEditConsumerSecret = (EditText) editDialog.findViewById(R.id.editConsumerSecret);
                    mAPIFormatHelpButton = editDialog.findViewById(R.id.apiUrlFormatHelp);

                    mEditNoVersionSuffix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            mEditNoVersionSuffixChanged = true;
                        }
                    });
                    mEditAuthType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            final int authType = APIEditorActivity.Companion.getCheckedAuthType(checkedId);
                            final boolean isOAuth = authType == ParcelableCredentials.AuthType.OAUTH || authType == ParcelableCredentials.AuthType.XAUTH;
                            mEditSameOAuthSigningUrl.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
                            mEditConsumerKey.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
                            mEditConsumerSecret.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
                            if (!mEditNoVersionSuffixChanged) {
                                mEditNoVersionSuffix.setChecked(authType == ParcelableCredentials.AuthType.TWIP_O_MODE);
                            }
                        }
                    });
                    mAPIFormatHelpButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), R.string.api_url_format_help, Toast.LENGTH_LONG).show();
                        }
                    });

                    if (savedInstanceState != null) {
                        final String apiUrlFormat = savedInstanceState.getString(Accounts.API_URL_FORMAT);
                        final int authType = savedInstanceState.getInt(Accounts.AUTH_TYPE);
                        final boolean sameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL);
                        final boolean noVersionSuffix = savedInstanceState.getBoolean(Accounts.NO_VERSION_SUFFIX);
                        final String consumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY));
                        final String consumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET));
                        setValues(apiUrlFormat, authType, sameOAuthSigningUrl, noVersionSuffix, consumerKey, consumerSecret);
                    } else {
                        final SharedPreferences preferences = preference.getSharedPreferences();
                        final String apiUrlFormat = preferences.getString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT);
                        final int authType = preferences.getInt(KEY_AUTH_TYPE, ParcelableCredentials.AuthType.OAUTH);
                        final boolean sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, true);
                        final boolean noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false);
                        final String consumerKey = trim(preferences.getString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY));
                        final String consumerSecret = trim(preferences.getString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET));
                        setValues(apiUrlFormat, authType, sameOAuthSigningUrl, noVersionSuffix, consumerKey, consumerSecret);
                    }
                }
            });
            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (!positiveResult) return;
            DefaultAPIPreference preference = (DefaultAPIPreference) getPreference();
            final SharedPreferences preferences = preference.getSharedPreferences();

            final String apiUrlFormat = ParseUtils.parseString(mEditAPIUrlFormat.getText());
            final int authType = APIEditorActivity.Companion.getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
            final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
            final boolean noVersionSuffix = mEditNoVersionSuffix.isChecked();
            final String consumerKey = ParseUtils.parseString(mEditConsumerKey.getText());
            final String consumerSecret = ParseUtils.parseString(mEditConsumerSecret.getText());
            final SharedPreferences.Editor editor = preferences.edit();
            if (!TextUtils.isEmpty(consumerKey) && !TextUtils.isEmpty(consumerSecret)) {
                editor.putString(KEY_CONSUMER_KEY, consumerKey);
                editor.putString(KEY_CONSUMER_SECRET, consumerSecret);
            } else {
                editor.remove(KEY_CONSUMER_KEY);
                editor.remove(KEY_CONSUMER_SECRET);
            }
            editor.putString(KEY_API_URL_FORMAT, apiUrlFormat);
            editor.putInt(KEY_AUTH_TYPE, authType);
            editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
            editor.putBoolean(KEY_NO_VERSION_SUFFIX, noVersionSuffix);
            editor.apply();
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(Accounts.API_URL_FORMAT, ParseUtils.parseString(mEditAPIUrlFormat.getText()));
            outState.putInt(Accounts.AUTH_TYPE, APIEditorActivity.Companion.getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId()));
            outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, mEditSameOAuthSigningUrl.isChecked());
            outState.putString(Accounts.CONSUMER_KEY, ParseUtils.parseString(mEditConsumerKey.getText()));
            outState.putString(Accounts.CONSUMER_SECRET, ParseUtils.parseString(mEditConsumerSecret.getText()));
        }

        private void setValues(final String apiUrlFormat, final int authType, final boolean sameOAuthSigningUrl,
                               final boolean noVersionSuffix, final String consumerKey, final String consumerSecret) {
            mEditAPIUrlFormat.setText(apiUrlFormat);
            mEditSameOAuthSigningUrl.setChecked(sameOAuthSigningUrl);
            mEditNoVersionSuffix.setChecked(noVersionSuffix);
            mEditConsumerKey.setText(consumerKey);
            mEditConsumerSecret.setText(consumerSecret);

            mButtonOAuth.setChecked(authType == ParcelableCredentials.AuthType.OAUTH);
            mButtonxAuth.setChecked(authType == ParcelableCredentials.AuthType.XAUTH);
            mButtonBasic.setChecked(authType == ParcelableCredentials.AuthType.BASIC);
            mButtonTwipOMode.setChecked(authType == ParcelableCredentials.AuthType.TWIP_O_MODE);
            if (mEditAuthType.getCheckedRadioButtonId() == -1) {
                mButtonOAuth.setChecked(true);
            }
        }
    }

}
