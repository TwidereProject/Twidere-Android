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

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.support.ViewSupport;

import static org.mariotaku.twidere.util.Utils.createFragmentForIntent;
import static org.mariotaku.twidere.util.Utils.matchLinkId;

public class LinkHandlerActivity extends BaseActivity implements SystemWindowsInsetsCallback,
        IControlBarActivity, SupportFragmentCallback {

    private final View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {

        private final Rect tempInsets = new Rect();
        private boolean compatCalled;

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (compatCalled) return;
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom && !ViewSupport.isInLayout(v)) {
                onFitSystemWindows(tempInsets);
                compatCalled = true;
            }
        }
    };
    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);
    private MultiSelectEventHandler mMultiSelectHandler;
    private boolean mFinishOnly;
    private int mActionBarHeight;
    private CharSequence mSubtitle;


    @Override
    public Fragment getCurrentVisibleFragment() {
        return getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    @Override
    public boolean triggerRefresh(int position) {
        return false;
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        super.onFitSystemWindows(insets);
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof IBaseFragment) {
            ((IBaseFragment) fragment).requestFitSystemWindows();
        }
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return super.getSystemWindowsInsets(insets);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (mFinishOnly) {
                    finish();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            return handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState);
        }
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            onBackPressed();
            return true;
        }
        return handler.handleKey(this, null, keyCode, event, metaState);
    }

    private boolean shouldFragmentTakeAllKeyboardShortcuts() {
        final Fragment fragment = getCurrentVisibleFragment();
        return fragment instanceof KeyboardShortcutsHandler.TakeAllKeyboardShortcut;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
        }
        if (handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState))
            return true;
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true;
        return super.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    private boolean isFragmentKeyboardShortcutHandled(final KeyboardShortcutsHandler handler,
                                                      final int keyCode, @NonNull final KeyEvent event, int metaState) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).isKeyboardShortcutHandled(handler, keyCode, event, metaState);
        }
        return false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        mMultiSelectHandler = new MultiSelectEventHandler(this);
        mMultiSelectHandler.dispatchOnCreate();
        final Intent intent = getIntent();
        final Uri uri = intent.getData();
        final int linkId = matchLinkId(uri);
        intent.setExtrasClassLoader(getClassLoader());
        final Fragment fragment;
        try {
            fragment = createFragmentForIntent(this, linkId, intent);
        } catch (Utils.NoAccountException e) {
            super.onCreate(savedInstanceState);
            Intent selectIntent = new Intent(this, AccountSelectorActivity.class);
            String accountHost = intent.getStringExtra(EXTRA_ACCOUNT_HOST);
            if (accountHost == null) {
                accountHost = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_HOST);
            }
            selectIntent.putExtra(EXTRA_SINGLE_SELECTION, true);
            selectIntent.putExtra(EXTRA_SELECT_ONLY_ITEM, true);
            selectIntent.putExtra(EXTRA_ACCOUNT_HOST, accountHost);
            selectIntent.putExtra(EXTRA_START_INTENT, intent);
            startActivity(selectIntent);
            finish();
            return;
        }
        if (fragment instanceof IToolBarSupportFragment) {
            if (!((IToolBarSupportFragment) fragment).setupWindow(this)) {
                supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
                supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY);
            }
        }

        super.onCreate(savedInstanceState);

        if (fragment == null) {
            finish();
            return;
        }

        setupActionBarOption();
        Utils.logOpenNotificationFromUri(this, uri);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment);
        ft.commit();
        setTitle(linkId, uri);
        mFinishOnly = Boolean.parseBoolean(uri.getQueryParameter(QUERY_PARAM_FINISH_ONLY));

        if (fragment instanceof IToolBarSupportFragment) {
            ThemeUtils.setCompatContentViewOverlay(getWindow(), new EmptyDrawable());
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
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        setupActionBarOption();
    }

    public final void setSubtitle(CharSequence subtitle) {
        mSubtitle = subtitle;
        setupActionBarOption();
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (!(getCurrentVisibleFragment() instanceof HideUiOnScroll)) return;
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible);
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (!(getCurrentVisibleFragment() instanceof HideUiOnScroll)) return;
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible, listener);
    }

    @Override
    public float getControlBarOffset() {
        Fragment fragment = getCurrentVisibleFragment();
        ActionBar actionBar = getSupportActionBar();
        if (fragment instanceof IToolBarSupportFragment) {
            return ((IToolBarSupportFragment) fragment).getControlBarOffset();
        } else if (actionBar != null) {
            return actionBar.getHideOffset() / (float) getControlBarHeight();
        }
        return 0;
    }

    @Override
    public void setControlBarOffset(float offset) {
        Fragment fragment = getCurrentVisibleFragment();
        ActionBar actionBar = getSupportActionBar();
        if (fragment instanceof IToolBarSupportFragment) {
            ((IToolBarSupportFragment) fragment).setControlBarOffset(offset);
        } else if (actionBar != null) {
            actionBar.setHideOffset((int) (getControlBarHeight() * offset));
        }
        notifyControlBarOffsetChanged();
    }

    @Override
    public int getControlBarHeight() {
        Fragment fragment = getCurrentVisibleFragment();
        ActionBar actionBar = getSupportActionBar();
        if (fragment instanceof IToolBarSupportFragment) {
            return ((IToolBarSupportFragment) fragment).getControlBarHeight();
        } else if (actionBar != null) {
            return actionBar.getHeight();
        }
        if (mActionBarHeight != 0) return mActionBarHeight;
        return mActionBarHeight = ThemeUtils.getActionBarHeight(this);
    }

    private boolean setTitle(final int linkId, final Uri uri) {
        setSubtitle(null);
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
                if (mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
                    setTitle(R.string.favorites);
                } else {
                    setTitle(R.string.likes);
                }
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
            case LINK_ID_GROUP: {
                setTitle(R.string.group);
                break;
            }
            case LINK_ID_USER_LISTS: {
                setTitle(R.string.user_lists);
                break;
            }
            case LINK_ID_USER_GROUPS: {
                setTitle(R.string.groups);
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
            case LINK_ID_ITEMS: {
                // TODO show title
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
                setTitle(R.string.users_favorited_this);
                break;
            }
            case LINK_ID_SEARCH: {
                setTitle(android.R.string.search_go);
                setSubtitle(uri.getQueryParameter(QUERY_PARAM_QUERY));
                break;
            }
            case LINK_ID_ACCOUNTS: {
                setTitle(R.string.accounts);
                break;
            }
            case LINK_ID_DRAFTS: {
                setTitle(R.string.drafts);
                break;
            }
            case LINK_ID_FILTERS: {
                setTitle(R.string.filters);
                break;
            }
            case LINK_ID_MAP: {
                setTitle(R.string.view_map);
                break;
            }
            case LINK_ID_PROFILE_EDITOR: {
                setTitle(R.string.edit_profile);
                break;
            }
            case LINK_ID_SCHEDULED_STATUSES: {
                setTitle(getString(R.string.scheduled_statuses));
                break;
            }
            case LINK_ID_DIRECT_MESSAGES: {
                setTitle(getString(R.string.direct_messages));
                break;
            }
            case LINK_ID_INTERACTIONS: {
                setTitle(getString(R.string.interactions));
                break;
            }
            case LINK_ID_PUBLIC_TIMELINE: {
                setTitle(getString(R.string.public_timeline));
                break;
            }
            default: {
                setTitle(getString(R.string.app_name));
                break;
            }
        }
        return true;
    }

    private boolean handleFragmentKeyboardShortcutRepeat(KeyboardShortcutsHandler handler, int keyCode,
                                                         int repeatCount, @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(KeyboardShortcutsHandler handler, int keyCode,
                                                         @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof KeyboardShortcutCallback) {
            if (((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState)) {
                return true;
            }
        }
        return false;
    }

    private void setupActionBarOption() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(mSubtitle);
        }
    }

    public interface HideUiOnScroll {

    }
}
