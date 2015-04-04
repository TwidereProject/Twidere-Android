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

package org.mariotaku.twidere.activity.support;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.fragment.support.SearchFragment;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ActivityAccessor;
import org.mariotaku.twidere.util.accessor.ActivityAccessor.TaskDescriptionCompat;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;

import static org.mariotaku.twidere.util.Utils.createFragmentForIntent;
import static org.mariotaku.twidere.util.Utils.matchLinkId;

public class LinkHandlerActivity extends BaseActionBarActivity implements OnClickListener,
        OnLongClickListener, SystemWindowsInsetsCallback, IControlBarActivity, SupportFragmentCallback {

    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);

    private MultiSelectEventHandler mMultiSelectHandler;
    private TintedStatusFrameLayout mMainContent;
    private boolean mFinishOnly;

    @Override
    public Fragment getCurrentVisibleFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.main_content);
    }

    @Override
    public void onDetachFragment(Fragment fragment) {

    }

    @Override
    public void onSetUserVisibleHint(Fragment fragment, boolean isVisibleToUser) {

    }

    @Override
    public boolean triggerRefresh(int position) {
        return false;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.go_top: {
                final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
                if (fragment instanceof RefreshScrollTopInterface) {
                    ((RefreshScrollTopInterface) fragment).scrollToStart();
                } else if (fragment instanceof ListFragment) {
                    ((ListFragment) fragment).setSelection(0);
                }
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.go_top: {
                final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
                if (fragment instanceof RefreshScrollTopInterface) {
                    ((RefreshScrollTopInterface) fragment).triggerRefresh();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                if (mFinishOnly) {
                    finish();
                } else {
                    navigateUpFromSameTask();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof IBasePullToRefreshFragment)
            return (IBasePullToRefreshFragment) fragment;
        else if (fragment instanceof SupportFragmentCallback) {
            final Fragment curr = ((SupportFragmentCallback) fragment).getCurrentVisibleFragment();
            if (curr instanceof IBasePullToRefreshFragment)
                return (IBasePullToRefreshFragment) curr;
        }
        return null;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        mMultiSelectHandler = new MultiSelectEventHandler(this);
        mMultiSelectHandler.dispatchOnCreate();
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final int linkId = matchLinkId(data);
        requestWindowFeatures(getWindow(), linkId, data);
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setActionBarBackground(actionBar, linkId, data);
        }
        setContentView(R.layout.activity_content_fragment);
        mMainContent.setOnFitSystemWindowsListener(this);
        setStatusBarColor(linkId, data);
        setTaskInfo(linkId, data);
        setSupportProgressBarIndeterminateVisibility(false);
        if (data == null || !showFragment(linkId, data)) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMultiSelectHandler.dispatchOnStart();
    }

    @Override
    protected void onStop() {
        mMultiSelectHandler.dispatchOnStop();
        super.onStop();
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        super.onFitSystemWindows(insets);
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (fragment instanceof IBaseFragment) {
            ((IBaseFragment) fragment).requestFitSystemWindows();
        }
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mMainContent = (TintedStatusFrameLayout) findViewById(R.id.main_content);
    }

    public final void setSubtitle(CharSequence subtitle) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setSubtitle(subtitle);
    }

    private void requestWindowFeatures(Window window, int linkId, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY);
        final int transitionRes;
        switch (linkId) {
            case LINK_ID_USER: {
                transitionRes = R.transition.transition_user;
                break;
            }
//            case LINK_ID_STATUS: {
//                transitionRes = R.transition.transition_status;
//                break;
//            }
            default: {
                transitionRes = 0;
                break;
            }
        }
        if (transitionRes != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !ThemeUtils.isTransparentBackground(this)) {
            Utils.setSharedElementTransition(this, window, transitionRes);
        }
    }

    @SuppressLint("AppCompatMethod")
    private void setActionBarBackground(ActionBar actionBar, int linkId, Uri data) {
        switch (linkId) {
            case LINK_ID_USER: {
                break;
            }
            case LINK_ID_SEARCH:
            case LINK_ID_USER_LISTS: {
                ThemeUtils.applyActionBarBackground(actionBar, this, getCurrentThemeResourceId(),
                        getCurrentThemeColor(), false);
                ThemeUtils.applyActionBarBackground(getActionBar(), this, getCurrentThemeResourceId(),
                        getCurrentThemeColor(), true);
                break;
            }
            default: {
                ThemeUtils.applyActionBarBackground(actionBar, this, getCurrentThemeResourceId(),
                        getCurrentThemeColor(), true);
                ThemeUtils.applyActionBarBackground(getActionBar(), this, getCurrentThemeResourceId(),
                        getCurrentThemeColor(), true);
                break;
            }
        }

    }

    private void setStatusBarColor(int linkId, Uri uri) {
        switch (linkId) {
            case LINK_ID_USER: {
                mMainContent.setShadowColor(0xA0000000);
                mMainContent.setDrawShadow(false);
                mMainContent.setDrawColor(true);
                break;
            }
            default: {
                mMainContent.setDrawShadow(false);
                mMainContent.setDrawColor(true);
                mMainContent.setFactor(1);
                final int color = getCurrentThemeColor();
                final int alpha = getCurrentThemeBackgroundAlpha();
                if (ThemeUtils.isDarkTheme(getCurrentThemeResourceId())) {
                    mMainContent.setColor(getResources().getColor(R.color.background_color_action_bar_dark), alpha);
                } else {
                    mMainContent.setColor(color, alpha);
                }
                break;
            }
        }
    }

    private void setTaskInfo(int linkId, Uri uri) {
        switch (linkId) {
            case LINK_ID_USER: {
                break;
            }
            default: {
                if (ThemeUtils.isColoredActionBar(getCurrentThemeResourceId())) {
                    ActivityAccessor.setTaskDescription(this, new TaskDescriptionCompat(null, null,
                            getCurrentThemeColor()));
                }
                break;
            }
        }
    }

    private boolean showFragment(final int linkId, final Uri uri) {
        final Intent intent = getIntent();
        intent.setExtrasClassLoader(getClassLoader());
        final Fragment fragment = createFragmentForIntent(this, linkId, intent);
        if (uri == null || fragment == null) return false;
        switch (linkId) {
            case LINK_ID_STATUS: {
                setTitle(R.string.status);
                break;
            }
            case LINK_ID_USER: {
                setTitle(R.string.user);
                break;
            }
            case LINK_ID_USER_TIMELINE: {
                setTitle(R.string.statuses);
                break;
            }
            case LINK_ID_USER_FAVORITES: {
                setTitle(R.string.favorites);
                break;
            }
            case LINK_ID_USER_FOLLOWERS: {
                setTitle(R.string.followers);
                break;
            }
            case LINK_ID_USER_FRIENDS: {
                setTitle(R.string.following);
                break;
            }
            case LINK_ID_USER_BLOCKS: {
                setTitle(R.string.blocked_users);
                break;
            }
            case LINK_ID_MUTES_USERS: {
                setTitle(R.string.twitter_muted_users);
                break;
            }
            case LINK_ID_DIRECT_MESSAGES_CONVERSATION: {
                setTitle(R.string.direct_messages);
                break;
            }
            case LINK_ID_USER_LIST: {
                setTitle(R.string.user_list);
                break;
            }
            case LINK_ID_USER_LISTS: {
                setTitle(R.string.user_lists);
                break;
            }
            case LINK_ID_USER_LIST_TIMELINE: {
                setTitle(R.string.list_timeline);
                break;
            }
            case LINK_ID_USER_LIST_MEMBERS: {
                setTitle(R.string.list_members);
                break;
            }
            case LINK_ID_USER_LIST_SUBSCRIBERS: {
                setTitle(R.string.list_subscribers);
                break;
            }
            case LINK_ID_USER_LIST_MEMBERSHIPS: {
                setTitle(R.string.lists_following_user);
                break;
            }
            case LINK_ID_SAVED_SEARCHES: {
                setTitle(R.string.saved_searches);
                break;
            }
            case LINK_ID_USER_MENTIONS: {
                setTitle(R.string.user_mentions);
                break;
            }
            case LINK_ID_INCOMING_FRIENDSHIPS: {
                setTitle(R.string.incoming_friendships);
                break;
            }
            case LINK_ID_USERS: {
                setTitle(R.string.users);
                break;
            }
            case LINK_ID_STATUSES: {
                setTitle(R.string.statuses);
                break;
            }
            case LINK_ID_USER_MEDIA_TIMELINE: {
                setTitle(R.string.media);
                break;
            }
            case LINK_ID_STATUS_RETWEETERS: {
                setTitle(R.string.users_retweeted_this);
                break;
            }
            case LINK_ID_STATUS_FAVORITERS: {
                setTitle(R.string.users_retweeted_this);
                break;
            }
            case LINK_ID_STATUS_REPLIES: {
                setTitle(R.string.view_replies);
                break;
            }
            case LINK_ID_SEARCH: {
                setTitle(android.R.string.search_go);
                setSubtitle(uri.getQueryParameter(QUERY_PARAM_QUERY));
                break;
            }
        }
        mFinishOnly = Boolean.parseBoolean(uri.getQueryParameter(QUERY_PARAM_FINISH_ONLY));
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_content, fragment);
        ft.commit();
        return true;
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (!(getCurrentVisibleFragment() instanceof SearchFragment)) return;
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible);
    }


    @Override
    public void setControlBarOffset(float offset) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setHideOffset(Math.round((1 - offset) * getControlBarHeight()));
        notifyControlBarOffsetChanged();
    }

    @Override
    public float getControlBarOffset() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return 0;
        return 1f - actionBar.getHideOffset() / (float) getControlBarHeight();
    }

    @Override
    public int getControlBarHeight() {
        final ActionBar actionBar = getSupportActionBar();
        return actionBar != null ? actionBar.getHeight() : 0;
    }

}
