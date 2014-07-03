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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.getNonEmptyString;
import static org.mariotaku.twidere.util.Utils.trim;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import twitter4j.TwitterConstants;

public class DefaultAPIPreference extends DialogPreference implements Constants, TwitterConstants,
		OnCheckedChangeListener, OnClickListener {

	private EditText mEditAPIUrlFormat;
	private CheckBox mEditSameOAuthSigningUrl;
	private EditText mEditConsumerKey, mEditConsumerSecret;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
	private TextView mAdvancedAPIConfigLabel;
	private View mAdvancedAPIConfigContainer;
	private View mAdvancedAPIConfigView;

	public DefaultAPIPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public DefaultAPIPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.api_editor_content);
		setPositiveButtonText(android.R.string.ok);
	}

	@Override
	public void onCheckedChanged(final RadioGroup group, final int checkedId) {
		final int authType = getCheckedAuthType(checkedId);
		final boolean isOAuth = authType == Accounts.AUTH_TYPE_OAUTH || authType == Accounts.AUTH_TYPE_XAUTH;
		mAdvancedAPIConfigContainer.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(final View v) {
		final boolean isVisible = mAdvancedAPIConfigView.isShown();
		final int compoundRes = isVisible ? R.drawable.expander_close_holo : R.drawable.expander_open_holo;
		mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compoundRes, 0, 0, 0);
		mAdvancedAPIConfigView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
	}

	@Override
	protected void onBindDialogView(final View view) {
		final SharedPreferences pref = getSharedPreferences();
		final String apiUrlFormat = getNonEmptyString(pref, KEY_API_URL_FORMAT, DEFAULT_REST_BASE_URL);
		final int authType = pref.getInt(KEY_AUTH_TYPE, Accounts.AUTH_TYPE_OAUTH);
		final boolean sameOAuthSigningUrl = pref.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false);
		final String consumerKey = getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
		final String consumerSecret = getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_2);
		setValues(apiUrlFormat, authType, sameOAuthSigningUrl, consumerKey, consumerSecret);
	}

	@Override
	protected View onCreateDialogView() {
		final View view = super.onCreateDialogView();
		mEditAPIUrlFormat = (EditText) view.findViewById(R.id.api_url_format);
		mEditAuthType = (RadioGroup) view.findViewById(R.id.auth_type);
		mButtonOAuth = (RadioButton) view.findViewById(R.id.oauth);
		mButtonxAuth = (RadioButton) view.findViewById(R.id.xauth);
		mButtonBasic = (RadioButton) view.findViewById(R.id.basic);
		mButtonTwipOMode = (RadioButton) view.findViewById(R.id.twip_o);
		mAdvancedAPIConfigContainer = view.findViewById(R.id.advanced_api_config_container);
		mAdvancedAPIConfigLabel = (TextView) view.findViewById(R.id.advanced_api_config_label);
		mAdvancedAPIConfigView = view.findViewById(R.id.advanced_api_config);
		mEditSameOAuthSigningUrl = (CheckBox) view.findViewById(R.id.same_oauth_signing_url);
		mEditConsumerKey = (EditText) view.findViewById(R.id.consumer_key);
		mEditConsumerSecret = (EditText) view.findViewById(R.id.consumer_secret);

		mEditAuthType.setOnCheckedChangeListener(this);
		mAdvancedAPIConfigLabel.setOnClickListener(this);

		return view;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		if (!positiveResult) return;
		final String apiUrlFormat = parseString(mEditAPIUrlFormat.getText());
		final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
		final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
		final String consumerKey = parseString(mEditConsumerKey.getText());
		final String consumerSecret = parseString(mEditConsumerSecret.getText());
		final SharedPreferences.Editor editor = getSharedPreferences().edit();
		if (!isEmpty(consumerKey) && !isEmpty(consumerSecret)) {
			editor.putString(KEY_CONSUMER_KEY, consumerKey);
			editor.putString(KEY_CONSUMER_SECRET, consumerSecret);
		} else {
			editor.remove(KEY_CONSUMER_KEY);
			editor.remove(KEY_CONSUMER_SECRET);
		}
		editor.putString(KEY_API_URL_FORMAT, apiUrlFormat);
		editor.putInt(KEY_AUTH_TYPE, authType);
		editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
		editor.apply();
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		final Bundle savedInstanceState = (Bundle) state;
		super.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_DATA));

		final SharedPreferences pref = getSharedPreferences();
		final String prefApiUrlFormat = getNonEmptyString(pref, KEY_API_URL_FORMAT, DEFAULT_REST_BASE_URL);
		final int prefAuthType = pref.getInt(KEY_AUTH_TYPE, Accounts.AUTH_TYPE_OAUTH);
		final boolean prefSameOAuthSigningUrl = pref.getBoolean(KEY_API_URL_FORMAT, false);
		final String prefConsumerKey = getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
		final String prefConsumerSecret = getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_2);
		final String apiUrlFormat = trim(savedInstanceState.getString(Accounts.API_URL_FORMAT, prefApiUrlFormat));
		final int authType = savedInstanceState.getInt(Accounts.AUTH_TYPE, prefAuthType);
		final boolean sameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL,
				prefSameOAuthSigningUrl);
		final String consumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY, prefConsumerKey));
		final String consumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET, prefConsumerSecret));
		setValues(apiUrlFormat, authType, sameOAuthSigningUrl, consumerKey, consumerSecret);

	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Bundle outState = new Bundle();
		outState.putParcelable(EXTRA_DATA, super.onSaveInstanceState());
		outState.putString(Accounts.API_URL_FORMAT, parseString(mEditAPIUrlFormat.getText()));
		outState.putInt(Accounts.AUTH_TYPE, getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId()));
		outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, mEditSameOAuthSigningUrl.isChecked());
		outState.putString(Accounts.CONSUMER_KEY, parseString(mEditConsumerKey.getText()));
		outState.putString(Accounts.CONSUMER_SECRET, parseString(mEditConsumerSecret.getText()));
		return outState;
	}

	private int getCheckedAuthType(final int checkedId) {
		switch (checkedId) {
			case R.id.xauth: {
				return Accounts.AUTH_TYPE_XAUTH;
			}
			case R.id.basic: {
				return Accounts.AUTH_TYPE_BASIC;
			}
			case R.id.twip_o: {
				return Accounts.AUTH_TYPE_TWIP_O_MODE;
			}
			default: {
				return Accounts.AUTH_TYPE_OAUTH;
			}
		}
	}

	private void setValues(final String apiUrlFormat, final int authType, final boolean sameOAuthSigningUrl,
			final String consumerKey, final String consumerSecret) {
		mEditAPIUrlFormat.setText(apiUrlFormat);
		mEditSameOAuthSigningUrl.setChecked(sameOAuthSigningUrl);
		mEditConsumerKey.setText(consumerKey);
		mEditConsumerSecret.setText(consumerSecret);

		mButtonOAuth.setChecked(authType == Accounts.AUTH_TYPE_OAUTH);
		mButtonxAuth.setChecked(authType == Accounts.AUTH_TYPE_XAUTH);
		mButtonBasic.setChecked(authType == Accounts.AUTH_TYPE_BASIC);
		mButtonTwipOMode.setChecked(authType == Accounts.AUTH_TYPE_TWIP_O_MODE);
		if (mEditAuthType.getCheckedRadioButtonId() == -1) {
			mButtonOAuth.setChecked(true);
		}
	}

}
