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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Switch;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

import java.util.List;

public abstract class AccountsListPreference extends PreferenceCategory implements Constants {

	private static final int[] ATTRS = { R.attr.switchKey, R.attr.switchDefault };
	private final String mSwitchKey;
	private final boolean mSwitchDefault;

	public AccountsListPreference(final Context context) {
		this(context, null);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceCategoryStyle);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
		mSwitchKey = a.getString(0);
		mSwitchDefault = a.getBoolean(1, false);
		a.recycle();
	}

	public void setAccountsData(final List<Account> accounts) {
		removeAll();
		for (final Account account : accounts) {
			final AccountItemPreference preference = new AccountItemPreference(getContext(), account, mSwitchKey,
					mSwitchDefault);
			setupPreference(preference, account);
			addPreference(preference);
		}
		final Preference preference = new Preference(getContext());
		preference.setLayoutResource(R.layout.settings_layout_click_to_config);
		addPreference(preference);
	}

	@Override
	protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		new LoadAccountsTask(this).execute();
	}

	protected abstract void setupPreference(AccountItemPreference preference, Account account);

	public static final class AccountItemPreference extends Preference implements ImageLoadingListener,
			OnCheckedChangeListener, OnSharedPreferenceChangeListener {

		private final Account mAccount;
		private final SharedPreferences mSwitchPreference;
		private final String mSwitchKey;
		private final boolean mSwitchDefault;
		private final float mDensity;

		private View mView;

		public AccountItemPreference(final Context context, final Account account, final String switchKey,
				final boolean switchDefault) {
			super(context);
			final String switchPreferenceName = ACCOUNT_PREFERENCES_NAME_PREFIX + account.account_id;
			mAccount = account;
			mDensity = context.getResources().getDisplayMetrics().density;
			mSwitchPreference = context.getSharedPreferences(switchPreferenceName, Context.MODE_PRIVATE);
			mSwitchKey = switchKey;
			mSwitchDefault = switchDefault;
			mSwitchPreference.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			if (mSwitchKey == null) return;
			final SharedPreferences.Editor editor = mSwitchPreference.edit();
			editor.putBoolean(mSwitchKey, isChecked);
			editor.apply();
		}

		@Override
		public void onLoadingCancelled(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
			setIcon(new BitmapDrawable(getContext().getResources(), loadedImage));
		}

		@Override
		public void onLoadingFailed(final String imageUri, final View view, final FailReason failReason) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingStarted(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			updateSwitchState();
		}

		@Override
		protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
			super.onAttachedToHierarchy(preferenceManager);
			setTitle(mAccount.name);
			setSummary(String.format("@%s", mAccount.screen_name));
			setIcon(R.drawable.ic_profile_image_default);
			final TwidereApplication app = TwidereApplication.getInstance(getContext());
			final ImageLoaderWrapper loader = app.getImageLoaderWrapper();
			loader.loadProfileImage(mAccount.profile_image_url, this);
		}

		@Override
		protected void onBindView(final View view) {
			super.onBindView(view);
			mView = view;
			final View iconView = view.findViewById(android.R.id.icon);
			if (iconView instanceof ImageView) {
				((ImageView) iconView).setScaleType(ScaleType.FIT_CENTER);
			}
			final View titleView = view.findViewById(android.R.id.title);
			if (titleView instanceof TextView) {
				((TextView) titleView).setSingleLine(true);
			}
			final View summaryView = view.findViewById(android.R.id.summary);
			if (summaryView instanceof TextView) {
				((TextView) summaryView).setSingleLine(true);
			}
			updateSwitchState();
		}

		private void updateSwitchState() {
			if (mView == null) return;
			final View widgetFrameView = mView.findViewById(android.R.id.widget_frame);
			if (!(widgetFrameView instanceof ViewGroup)) return;
			final ViewGroup widgetFrame = (ViewGroup) widgetFrameView;
			widgetFrame.setVisibility(mSwitchKey != null ? View.VISIBLE : View.GONE);
			widgetFrame.setPadding(widgetFrame.getPaddingLeft(), widgetFrame.getPaddingTop(), (int) (mDensity * 8),
					widgetFrame.getPaddingBottom());
			widgetFrame.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			// remove preview image that is already created
			widgetFrame.setAlpha(isEnabled() ? 1 : 0.25f);
			final View foundView = widgetFrame.findViewById(android.R.id.toggle);
			final Switch toggle;
			if (foundView instanceof Switch) {
				toggle = (Switch) foundView;
			} else {
				widgetFrame.removeAllViews();
				toggle = new Switch(getContext());
				toggle.setId(android.R.id.toggle);
				toggle.setOnCheckedChangeListener(this);
				widgetFrame.addView(toggle);
			}
			if (mSwitchKey != null) {
				toggle.setChecked(mSwitchPreference.getBoolean(mSwitchKey, mSwitchDefault));
			}
		}

	}

	private static class LoadAccountsTask extends AsyncTask<Void, Void, List<Account>> {

		private final AccountsListPreference mPreference;

		public LoadAccountsTask(final AccountsListPreference preference) {
			mPreference = preference;
		}

		@Override
		protected List<Account> doInBackground(final Void... params) {
			return Account.getAccountsList(mPreference.getContext(), false);
		}

		@Override
		protected void onPostExecute(final List<Account> result) {
			mPreference.setAccountsData(result);
		}

	}

}
