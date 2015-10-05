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
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.BitmapUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.dagger.ApplicationModule;

import java.util.List;

public abstract class AccountsListPreference extends PreferenceCategory implements Constants {

    private static final int[] ATTRS = {R.attr.switchKey, R.attr.switchDefault};
    @Nullable
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

    public void setAccountsData(final List<ParcelableAccount> accounts) {
        removeAll();
        for (final ParcelableAccount account : accounts) {
            final AccountItemPreference preference = new AccountItemPreference(getContext(), account,
                    mSwitchKey, mSwitchDefault);
            setupPreference(preference, account);
            addPreference(preference);
        }
        final Preference preference = new Preference(getContext());
        preference.setLayoutResource(R.layout.settings_layout_click_to_config);
        addPreference(preference);
    }

    @Override
    protected void onAttachedToHierarchy(@NonNull final PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        AsyncTaskUtils.executeTask(new LoadAccountsTask(this));
    }

    protected abstract void setupPreference(AccountItemPreference preference, ParcelableAccount account);

    public static final class AccountItemPreference extends Preference implements ImageLoadingListener,
            OnSharedPreferenceChangeListener {

        private final ParcelableAccount mAccount;
        private final SharedPreferences mSwitchPreference;
        private final MediaLoaderWrapper mImageLoader;

        public AccountItemPreference(final Context context, final ParcelableAccount account, final String switchKey,
                                     final boolean switchDefault) {
            super(context);
            final String switchPreferenceName = ACCOUNT_PREFERENCES_NAME_PREFIX + account.account_id;
            mAccount = account;
            mSwitchPreference = context.getSharedPreferences(switchPreferenceName, Context.MODE_PRIVATE);
            mImageLoader = ApplicationModule.get(context).getMediaLoaderWrapper();
            mSwitchPreference.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onLoadingCancelled(final String imageUri, final View view) {
//            setIcon(R.drawable.ic_profile_image_default);
        }

        @Override
        public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
            final Bitmap roundedBitmap = BitmapUtils.getCircleBitmap(loadedImage);
            setIcon(new BitmapDrawable(getContext().getResources(), roundedBitmap));
        }

        @Override
        public void onLoadingFailed(final String imageUri, final View view, final FailReason failReason) {
//            setIcon(R.drawable.ic_profile_image_default);
        }

        @Override
        public void onLoadingStarted(final String imageUri, final View view) {
//            setIcon(R.drawable.ic_profile_image_default);
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            notifyChanged();
        }

        @Override
        protected void onAttachedToHierarchy(@NonNull final PreferenceManager preferenceManager) {
            super.onAttachedToHierarchy(preferenceManager);
            setTitle(mAccount.name);
            setSummary(String.format("@%s", mAccount.screen_name));
//            setIcon(R.drawable.ic_profile_image_default);
            mImageLoader.loadProfileImage(mAccount.profile_image_url, this);
        }

        @Override
        protected void onBindView(@NonNull final View view) {
            super.onBindView(view);
//            final View iconView = view.findViewById(android.R.id.icon);
//            if (iconView instanceof ImageView) {
//                final ImageView imageView = (ImageView) iconView;
//                imageView.setScaleType(ScaleType.CENTER_CROP);
//            }
            final View titleView = view.findViewById(android.R.id.title);
            if (titleView instanceof TextView) {
                ((TextView) titleView).setSingleLine(true);
            }
            final View summaryView = view.findViewById(android.R.id.summary);
            if (summaryView instanceof TextView) {
                ((TextView) summaryView).setSingleLine(true);
            }
        }
    }

    private static class LoadAccountsTask extends AsyncTask<Object, Object, List<ParcelableAccount>> {

        private final AccountsListPreference mPreference;

        public LoadAccountsTask(final AccountsListPreference preference) {
            mPreference = preference;
        }

        @Override
        protected List<ParcelableAccount> doInBackground(final Object... params) {
            return ParcelableAccount.getAccountsList(mPreference.getContext(), false);
        }

        @Override
        protected void onPostExecute(final List<ParcelableAccount> result) {
            mPreference.setAccountsData(result);
        }

    }

}
