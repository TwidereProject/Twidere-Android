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
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ListScrollDistanceCalculator;
import org.mariotaku.twidere.util.ListScrollDistanceCalculator.ScrollDistanceListener;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.iface.IExtendedView.TouchInterceptor;

import static android.support.v4.app.ListFragmentTrojan.INTERNAL_EMPTY_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_LIST_CONTAINER_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_PROGRESS_CONTAINER_ID;

public class BaseSupportListFragment extends ListFragment implements IBaseFragment, Constants, OnScrollListener,
        RefreshScrollTopInterface, ScrollDistanceListener {

    private ListScrollDistanceCalculator mListScrollCounter = new ListScrollDistanceCalculator();

    private boolean mIsInstanceStateSaved;

    private boolean mReachedBottom, mNotReachedBottomBefore;

    private boolean mReachedTop, mNotReachedTopBefore;

    private boolean mStoppedPreviously;


    public final TwidereApplication getApplication() {
        return TwidereApplication.getInstance(getActivity());
    }

    public final ContentResolver getContentResolver() {
        final Activity activity = getActivity();
        if (activity != null) return activity.getContentResolver();
        return null;
    }

    @Override
    public Bundle getExtraConfiguration() {
        final Bundle args = getArguments();
        final Bundle extras = new Bundle();
        if (args != null && args.containsKey(EXTRA_EXTRAS)) {
            extras.putAll(args.getBundle(EXTRA_EXTRAS));
        }
        return extras;
    }

    public final MultiSelectManager getMultiSelectManager() {
        return getApplication() != null ? getApplication().getMultiSelectManager() : null;
    }

    public final SharedPreferences getSharedPreferences(final String name, final int mode) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSharedPreferences(name, mode);
        return null;
    }

    public final Object getSystemService(final String name) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSystemService(name);
        return null;
    }

    @Override
    public final int getTabPosition() {
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

    protected void fitSystemWindows(Rect insets) {
        Utils.makeListFragmentFitsSystemWindows(this, insets);
    }

    public AsyncTwitterWrapper getTwitterWrapper() {
        return getApplication() != null ? getApplication().getTwitterWrapper() : null;
    }

    public void invalidateOptionsMenu() {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.invalidateOptionsMenu();
    }

    public boolean isInstanceStateSaved() {
        return mIsInstanceStateSaved;
    }

    public boolean isReachedBottom() {
        return mReachedBottom;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNotReachedBottomBefore = true;
        mStoppedPreviously = false;
        mIsInstanceStateSaved = savedInstanceState != null;
        mListScrollCounter.setScrollDistanceListener(this);
        final ListView lv = getListView();
        lv.setOnScrollListener(this);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }


    private final TouchInterceptor mInternalOnTouchListener = new TouchInterceptor() {

        @Override
        public boolean dispatchTouchEvent(View view, MotionEvent event) {
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    onListTouched();
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(View view, MotionEvent event) {
            return false;
        }
    };

    protected void onListTouched() {

    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBaseViewCreated(view, savedInstanceState);
        requestFitSystemWindows();
    }

    /**
     * Provide default implementation to return a simple list view. Subclasses
     * can override to replace with their own layout. If doing so, the returned
     * view hierarchy <em>must</em> have a ListView whose id is
     * {@link android.R.id#list android.R.id.list} and can optionally have a
     * sibling view id {@link android.R.id#empty android.R.id.empty} that is to
     * be shown when the list is empty.
     * <p/>
     * <p/>
     * If you are overriding this method with your own custom content, consider
     * including the standard layout {@link android.R.layout#list_content} in
     * your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment. In particular, this is currently the only way
     * to have the built-in indeterminant progress state be shown.
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final Context context = getActivity();

        final FrameLayout root = new FrameLayout(context);

        // ------------------------------------------------------------------

        final LinearLayout pframe = new LinearLayout(context);
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        pframe.setOrientation(LinearLayout.VERTICAL);
        pframe.setVisibility(View.GONE);
        pframe.setGravity(Gravity.CENTER);

        final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        pframe.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(pframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // ------------------------------------------------------------------

        final ExtendedFrameLayout lframe = new ExtendedFrameLayout(context);
        lframe.setId(INTERNAL_LIST_CONTAINER_ID);
        lframe.setTouchInterceptor(mInternalOnTouchListener);

        final TextView tv = new TextView(getActivity());
        tv.setTextAppearance(context, ThemeUtils.getTextAppearanceLarge(context));
        tv.setId(INTERNAL_EMPTY_ID);
        tv.setGravity(Gravity.CENTER);
        lframe.addView(tv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        final ListView lv = new ListView(getActivity());
        lv.setId(android.R.id.list);
        lv.setDrawSelectorOnTop(false);
        lv.setOnScrollListener(this);
        lframe.addView(lv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // ------------------------------------------------------------------

        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return root;
    }

    @Override
    public void onDestroy() {
        mStoppedPreviously = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        final Fragment fragment = getParentFragment();
        if (fragment instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) fragment).onDetachFragment(this);
        }
        final Activity activity = getActivity();
        if (activity instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) activity).onDetachFragment(this);
        }
    }

    public void onPostStart() {
    }

    public void onRestart() {

    }


    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                         final int totalItemCount) {
        mListScrollCounter.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        final ListAdapter adapter = getListAdapter();
        if (adapter == null) return;
        final boolean reachedTop = firstVisibleItem == 0;
        final boolean reachedBottom = firstVisibleItem + visibleItemCount >= totalItemCount
                && totalItemCount >= visibleItemCount;

        if (mReachedBottom != reachedBottom) {
            mReachedBottom = reachedBottom;
            if (mReachedBottom && mNotReachedBottomBefore) {
                mNotReachedBottomBefore = false;
                return;
            }
            if (mReachedBottom && adapter.getCount() > visibleItemCount) {
                onReachedBottom();
            }
        }
        if (mReachedTop != reachedTop) {
            mReachedTop = reachedTop;
            if (mReachedTop && mNotReachedTopBefore) {
                mNotReachedTopBefore = false;
                return;
            }
            if (mReachedTop && adapter.getCount() > visibleItemCount) {
                onReachedTop();
            }
        }
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        mListScrollCounter.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onStart() {
        super.onStart();
        onPostStart();
        if (mStoppedPreviously) {
            onRestart();
        }
        mStoppedPreviously = false;
    }

    @Override
    public void onStop() {
        mStoppedPreviously = true;
        super.onStop();
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.registerReceiver(receiver, filter);
    }

    @Override
    public boolean scrollToStart() {
        final Activity activity = getActivity();
        if (!isAdded() || activity == null) return false;
        Utils.scrollListToTop(getListView());
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).setControlBarOffset(1);
        }
        return true;
    }

    public void setProgressBarIndeterminateVisibility(final boolean visible) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.setProgressBarIndeterminateVisibility(visible);
    }

    @Override
    public void setSelection(final int position) {
        if (getView() == null) return;
        Utils.scrollListToPosition(getListView(), position);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        final Fragment fragment = getParentFragment();
        if (fragment instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) fragment).onSetUserVisibleHint(this, isVisibleToUser);
        }
        final Activity activity = getActivity();
        if (activity instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) activity).onSetUserVisibleHint(this, isVisibleToUser);
        }
    }

    @Override
    public boolean triggerRefresh() {
        return false;
    }

    public void unregisterReceiver(final BroadcastReceiver receiver) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.unregisterReceiver(receiver);
    }

    protected void onReachedBottom() {

    }

    protected void onReachedTop() {

    }

    @Override
    public void onScrollDistanceChanged(int delta, int total) {
//        final FragmentActivity a = getActivity();
//        if (a instanceof IControlBarActivity && getTabPosition() >= 0 && getUserVisibleHint()) {
//            ((IControlBarActivity) a).moveControlBarBy(delta);
//        }
    }
}
