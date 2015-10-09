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

package org.mariotaku.twidere.fragment.support;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Bus;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.BaseAppCompatActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemedLayoutInflaterFactory;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.VideoLoader;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;

import javax.inject.Inject;

public class BaseSupportFragment extends Fragment implements IBaseFragment, Constants {

    // Utility classes
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected MediaLoaderWrapper mMediaLoader;
    @Inject
    protected VideoLoader mVideoLoader;
    @Inject
    protected Bus mBus;
    @Inject
    protected AsyncTaskManager mAsyncTaskManager;
    @Inject
    protected MultiSelectManager mMultiSelectManager;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;

    public BaseSupportFragment() {

    }

    @Override
    public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBaseViewCreated(view, savedInstanceState);
        requestFitSystemWindows();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(context)).build().inject(this);
    }


    public TwidereApplication getApplication() {
        final Activity activity = getActivity();
        if (activity != null) return (TwidereApplication) activity.getApplication();
        return null;
    }

    public ContentResolver getContentResolver() {
        final Activity activity = getActivity();
        if (activity != null) return activity.getContentResolver();
        return null;
    }

    public SharedPreferences getSharedPreferences(final String name, final int mode) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSharedPreferences(name, mode);
        return null;
    }

    public Object getSystemService(final String name) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSystemService(name);
        return null;
    }


    public void invalidateOptionsMenu() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.supportInvalidateOptionsMenu();
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.registerReceiver(receiver, filter);
    }

    public void setProgressBarIndeterminateVisibility(final boolean visible) {
        final Activity activity = getActivity();
        if (activity instanceof BaseAppCompatActivity) {
            activity.setProgressBarIndeterminateVisibility(visible);
        }
    }

    public void unregisterReceiver(final BroadcastReceiver receiver) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.unregisterReceiver(receiver);
    }

    @Override
    public Bundle getExtraConfiguration() {
        return null;
    }

    @Override
    public int getTabPosition() {
        final Bundle args = getArguments();
        return args != null ? args.getInt(EXTRA_TAB_POSITION, -1) : -1;
    }

    @Override
    public void requestFitSystemWindows() {
        final Activity activity = getActivity();
        final Fragment parentFragment = getParentFragment();
        final SystemWindowsInsetsCallback callback;
        if (parentFragment instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) parentFragment;
        } else if (activity instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) activity;
        } else {
            return;
        }
        final Rect insets = new Rect();
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets);
        }
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {

    }

    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof IThemedActivity)) {
            return super.getLayoutInflater(savedInstanceState);
        }
        final LayoutInflater inflater = activity.getLayoutInflater().cloneInContext(getThemedContext());
        getChildFragmentManager(); // Init if needed; use raw implementation below.
        final LayoutInflaterFactory delegate = FragmentManagerTrojan.getLayoutInflaterFactory(getChildFragmentManager());
        LayoutInflaterCompat.setFactory(inflater, new ThemedLayoutInflaterFactory((IThemedActivity) activity, delegate));
        return inflater;
    }

    public Context getThemedContext() {
        return getActivity();
    }

    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    }
}
