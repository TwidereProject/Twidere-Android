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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayoutTrojan;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.meizu.flyme.reflect.StatusBarProxy;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.activity.SettingsWizardActivity;
import org.mariotaku.twidere.activity.UsageStatisticsActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.fragment.support.AccountsDashboardFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggestionsFragment;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Mentions;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.service.StreamingService;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereColorUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.util.message.UnreadCountUpdatedEvent;
import org.mariotaku.twidere.util.support.ActivitySupport;
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
import org.mariotaku.twidere.view.iface.IHomeActionButton;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.mariotaku.twidere.util.CompareUtils.classEquals;
import static org.mariotaku.twidere.util.Utils.cleanDatabasesByItemLimit;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTabDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.isDatabaseReady;
import static org.mariotaku.twidere.util.Utils.openMessageConversation;
import static org.mariotaku.twidere.util.Utils.openSearch;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

public class HomeActivity extends BaseAppCompatActivity implements OnClickListener, OnPageChangeListener,
        SupportFragmentCallback, OnLongClickListener {
    private final Handler mHandler = new Handler();

    private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

    private ParcelableAccount mSelectedAccountToSearch;

    private SharedPreferences mPreferences;


    private NotificationManager mNotificationManager;

    private MultiSelectEventHandler mMultiSelectHandler;

    private SupportTabsAdapter mPagerAdapter;

    private ExtendedViewPager mViewPager;
    private TabPagerIndicator mTabIndicator;
    private DrawerLayout mDrawerLayout;
    private View mEmptyTabHint;
    private View mActionsButton;
    private View mActionBarWithOverlay;
    private TintedStatusFrameLayout mHomeContent;

    private UpdateUnreadCountTask mUpdateUnreadCountTask;

    private Toolbar mActionBar;

    private OnSharedPreferenceChangeListener mReadStateChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateUnreadCount();
        }
    };
    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);
    private int mTabColumns;
    private View mActionBarContainer;

    public void closeAccountsDrawer() {
        if (mDrawerLayout == null) return;
        mDrawerLayout.closeDrawers();
    }

    public long[] getActivatedAccountIds() {
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            return ((AccountsDashboardFragment) fragment).getActivatedAccountIds();
        }
        return Utils.getActivatedAccountIds(this);
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        final int currentItem = mViewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= mPagerAdapter.getCount()) return null;
        return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentItem);
    }

    @Override
    public boolean triggerRefresh(final int position) {
        final Fragment f = (Fragment) mPagerAdapter.instantiateItem(mViewPager, position);
        if (!(f instanceof RefreshScrollTopInterface)) return false;
        if (f.getActivity() == null || f.isDetached()) return false;
        return ((RefreshScrollTopInterface) f).triggerRefresh();
    }

    public Fragment getLeftDrawerFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.left_drawer);
    }

    public boolean getDefaultSystemWindowsInsets(Rect insets) {
        return super.getSystemWindowsInsets(insets);
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        final int height = mTabIndicator != null ? mTabIndicator.getHeight() : 0;
        insets.top = (height != 0 ? height : ThemeUtils.getActionBarHeight(this));
        return true;
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible);
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener) {
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible, listener);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                final FragmentManager fm = getSupportFragmentManager();
                final int count = fm.getBackStackEntryCount();
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawers();
                    return true;
                } else if (count == 0) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
                return true;
            }
            case R.id.search: {
                openSearchView(mSelectedAccountToSearch);
                return true;
            }
            case R.id.actions: {
                triggerActionsClick();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true;
        String action = handler.getKeyAction(CONTEXT_TAG_HOME, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_HOME_ACCOUNTS_DASHBOARD: {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawers();
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        setControlBarVisibleAnimate(true);
                    }
                    return true;
                }
            }
        }
        action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_NAVIGATION_PREVIOUS_TAB: {
                    final int previous = mViewPager.getCurrentItem() - 1;
                    if (previous < 0 && DrawerLayoutTrojan.findDrawerWithGravity(mDrawerLayout, Gravity.START) != null) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        setControlBarVisibleAnimate(true);
                    } else if (previous < mPagerAdapter.getCount()) {
                        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                            mDrawerLayout.closeDrawers();
                        } else {
                            mViewPager.setCurrentItem(previous, true);
                        }
                    }
                    return true;
                }
                case ACTION_NAVIGATION_NEXT_TAB: {
                    final int next = mViewPager.getCurrentItem() + 1;
                    if (next >= mPagerAdapter.getCount() && DrawerLayoutTrojan.findDrawerWithGravity(mDrawerLayout, Gravity.END) != null) {
                        mDrawerLayout.openDrawer(GravityCompat.END);
                        setControlBarVisibleAnimate(true);
                    } else if (next >= 0) {
                        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                            mDrawerLayout.closeDrawers();
                        } else {
                            mViewPager.setCurrentItem(next, true);
                        }
                    }
                    return true;
                }
            }
        }
        return handler.handleKey(this, null, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true;
        return super.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        if (handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState))
            return true;
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU: {
                final DrawerLayout drawer = mDrawerLayout;
                if (isDrawerOpen()) {
                    drawer.closeDrawers();
                } else {
                    drawer.openDrawer(Gravity.LEFT);
                }
                return true;
            }
            case KeyEvent.KEYCODE_BACK: {
                final DrawerLayout drawer = mDrawerLayout;
                if (isDrawerOpen()) {
                    drawer.closeDrawers();
                } else {
                    onBackPressed();
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean isDrawerOpen() {
        final DrawerLayout drawer = mDrawerLayout;
        if (drawer == null) return false;
        return drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END);
    }

    /**
     * Called when the context is first created.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        if (!isDatabaseReady(this)) {
            Toast.makeText(this, R.string.preparing_database_toast, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mMultiSelectHandler = new MultiSelectEventHandler(this);
        mMultiSelectHandler.dispatchOnCreate();
        if (!Utils.hasAccount(this)) {
            final Intent signInIntent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
            signInIntent.setClass(this, SignInActivity.class);
            startActivity(signInIntent);
            finish();
            return;
        } else {
            notifyAccountsChanged();
        }
        final Intent intent = getIntent();
        if (openSettingsWizard()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        setSupportActionBar(mActionBar);
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        int tabDisplayOptionInt = getTabDisplayOptionInt(this);

        mTabColumns = getResources().getInteger(R.integer.default_tab_columns);

        mHomeContent.setOnFitSystemWindowsListener(this);
        mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mTabIndicator, mTabColumns);
        mViewPager.setAdapter(mPagerAdapter);
//        mViewPager.setOffscreenPageLimit(3);
        mTabIndicator.setViewPager(mViewPager);
        mTabIndicator.setOnPageChangeListener(this);
        mTabIndicator.setColumns(mTabColumns);
        if (tabDisplayOptionInt == 0) {
            tabDisplayOptionInt = TabPagerIndicator.ICON;
        }
        mTabIndicator.setTabDisplayOption(tabDisplayOptionInt);
        mTabIndicator.setTabExpandEnabled((tabDisplayOptionInt & TabPagerIndicator.LABEL) == 0);
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
        mTabIndicator.updateAppearance();

        mActionsButton.setOnClickListener(this);
        mActionsButton.setOnLongClickListener(this);
        mEmptyTabHint.setOnClickListener(this);

        ThemeUtils.setCompatContentViewOverlay(this, new EmptyDrawable());
        ViewCompat.setElevation(mActionBarContainer, ThemeUtils.getSupportActionBarElevation(this));
        ViewSupport.setOutlineProvider(mActionBarContainer, ViewOutlineProviderCompat.BACKGROUND);
        final View windowOverlay = findViewById(R.id.window_overlay);
        ViewSupport.setBackground(windowOverlay, ThemeUtils.getNormalWindowContentOverlay(this, getCurrentThemeResourceId()));

        setupSlidingMenu();
        setupBars();
        showDataProfilingRequest();
        initUnreadCount();
        updateActionsButton();
        updateSlidingMenuTouchMode();

        if (savedInstanceState == null) {
            if (refreshOnStart) {
                mTwitterWrapper.refreshAll(getActivatedAccountIds());
            }
            if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
                openAccountsDrawer();
            }
        }
        setupHomeTabs();

        final int initialTabPosition = handleIntent(intent, savedInstanceState == null);
        setTabPosition(initialTabPosition);

        if (Utils.isStreamingEnabled()) {
            startService(new Intent(this, StreamingService.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMultiSelectHandler.dispatchOnStart();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
        mBus.register(this);

        mReadStateManager.registerOnSharedPreferenceChangeListener(mReadStateChangeListener);
        updateUnreadCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONRESUME));
        invalidateOptionsMenu();
        updateActionsButtonStyle();
        updateActionsButton();
        updateSlidingMenuTouchMode();
    }

    @Override
    protected void onPause() {
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONPAUSE));
        super.onPause();
    }

    @Override
    protected void onStop() {
        mMultiSelectHandler.dispatchOnStop();
        mReadStateManager.unregisterOnSharedPreferenceChangeListener(mReadStateChangeListener);
        mBus.unregister(this);
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mAccountChangeObserver);
        mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).apply();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

        super.onStop();
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void notifyAccountsChanged() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateActionsButton();
    }

    @Subscribe
    public void notifyUnreadCountUpdated(UnreadCountUpdatedEvent event) {
        updateUnreadCount();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.actions_button: {
                triggerActionsClick();
                break;
            }
            case R.id.empty_tab_hint: {
                final Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, CustomTabsFragment.class.getName());
                intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.tabs);
                startActivityForResult(intent, REQUEST_SETTINGS);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.actions_button: {
                showMenuItemToast(v, v.getContentDescription(), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(final int position) {
        //TODO handle secondary drawer
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        }
        updateSlidingMenuTouchMode();
        updateActionsButton();
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        setControlBarVisibleAnimate(true);
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, QuickSearchBarActivity.class));
        return true;
    }

    public void openSearchView(final ParcelableAccount account) {
        mSelectedAccountToSearch = account;
        onSearchRequested();
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        super.onFitSystemWindows(insets);
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            ((AccountsDashboardFragment) fragment).requestFitSystemWindows();
        }
        mHomeContent.setStatusBarHeight(insets.top);
    }

    public void updateUnreadCount() {
        if (mTabIndicator == null || mUpdateUnreadCountTask != null
                && mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mUpdateUnreadCountTask = new UpdateUnreadCountTask(this, mReadStateManager, mTabIndicator,
                mPagerAdapter.getTabs());
        AsyncTaskUtils.executeTask(mUpdateUnreadCountTask);
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final int tabPosition = handleIntent(intent, false);
        if (tabPosition >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(tabPosition, mPagerAdapter.getCount(), 0));
        }
    }

    @Override
    protected void onDestroy() {

        stopService(new Intent(this, StreamingService.class));

        // Delete unused items in databases.
        cleanDatabasesByItemLimit(this);
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONDESTROY));
        super.onDestroy();
    }

    @Override
    public float getControlBarOffset() {
        if (mTabColumns > 1) {
            final ViewGroup.LayoutParams lp = mActionsButton.getLayoutParams();
            float total;
            if (lp instanceof MarginLayoutParams) {
                total = ((MarginLayoutParams) lp).bottomMargin + mActionsButton.getHeight();
            } else {
                total = mActionsButton.getHeight();
            }
            return 1 - mActionsButton.getTranslationY() / total;
        }
        final float totalHeight = getControlBarHeight();
        return 1 + mActionBarWithOverlay.getTranslationY() / totalHeight;
    }

    @Override
    public void setControlBarOffset(float offset) {
        mActionBarWithOverlay.setTranslationY(mTabColumns > 1 ? 0 : getControlBarHeight() * (offset - 1));
        final ViewGroup.LayoutParams lp = mActionsButton.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            mActionsButton.setTranslationY((((MarginLayoutParams) lp).bottomMargin + mActionsButton.getHeight()) * (1 - offset));
        } else {
            mActionsButton.setTranslationY(mActionsButton.getHeight() * (1 - offset));
        }
        notifyControlBarOffsetChanged();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mActionBarContainer = findViewById(R.id.twidere_action_bar_container);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_menu);
        mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
        mEmptyTabHint = findViewById(R.id.empty_tab_hint);
        mActionsButton = findViewById(R.id.actions_button);
        mActionBarWithOverlay = findViewById(R.id.twidere_action_bar_with_overlay);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mHomeContent = (TintedStatusFrameLayout) findViewById(R.id.home_content);
    }

    private Fragment getKeyboardShortcutRecipient() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            return getLeftDrawerFragment();
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            return null;
        } else {
            return getCurrentVisibleFragment();
        }
    }

    @Override
    public int getControlBarHeight() {
        return mTabIndicator.getHeight() - mTabIndicator.getStripHeight();
    }

    private boolean handleFragmentKeyboardShortcutRepeat(final KeyboardShortcutsHandler handler,
                                                         final int keyCode, final int repeatCount,
                                                         @NonNull final KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(final KeyboardShortcutsHandler handler,
                                                         final int keyCode, @NonNull final KeyEvent event,
                                                         int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState);
        }
        return false;
    }

    private boolean isFragmentKeyboardShortcutHandled(final KeyboardShortcutsHandler handler,
                                                      final int keyCode, @NonNull final KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).isKeyboardShortcutHandled(handler, keyCode,
                    event, metaState);
        }
        return false;
    }

    private int handleIntent(final Intent intent, final boolean firstCreate) {
        // use packge's class loader to prevent BadParcelException
        intent.setExtrasClassLoader(getClassLoader());
        // reset intent
        setIntent(new Intent(this, HomeActivity.class));
        final String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            final Bundle appSearchData = intent.getBundleExtra(SearchManager.APP_DATA);
            final long accountId;
            if (appSearchData != null && appSearchData.containsKey(EXTRA_ACCOUNT_ID)) {
                accountId = appSearchData.getLong(EXTRA_ACCOUNT_ID, -1);
            } else {
                accountId = getDefaultAccountId(this);
            }
            openSearch(this, accountId, query);
            return -1;
        }
        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        final long[] refreshedIds = intent.getLongArrayExtra(EXTRA_REFRESH_IDS);
        if (refreshedIds != null) {
            mTwitterWrapper.refreshAll(refreshedIds);
        } else if (firstCreate && refreshOnStart) {
            mTwitterWrapper.refreshAll();
        }

        final Uri uri = intent.getData();
        final String tabType = uri != null ? Utils.matchTabType(uri) : null;
        int initialTab = -1;
        if (tabType != null) {
            final long accountId = ParseUtils.parseLong(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID));
            for (int i = mPagerAdapter.getCount() - 1; i > -1; i--) {
                final SupportTabSpec tab = mPagerAdapter.getTab(i);
                if (tabType.equals(tab.type)) {
                    initialTab = i;
                    if (hasAccountId(tab.args, accountId)) {
                        break;
                    }
                }
            }
        }
        if (initialTab != -1 && mViewPager != null) {
            // clearNotification(initial_tab);
        }
        final Intent extraIntent = intent.getParcelableExtra(EXTRA_EXTRA_INTENT);
        if (extraIntent != null && firstCreate) {
            extraIntent.setExtrasClassLoader(getClassLoader());
            startActivity(extraIntent);
        }
        return initialTab;
    }

    private boolean hasAccountId(Bundle args, long accountId) {
        if (args == null) return false;
        if (args.containsKey(EXTRA_ACCOUNT_ID)) {
            return args.getLong(EXTRA_ACCOUNT_ID) == accountId;
        } else if (args.containsKey(EXTRA_ACCOUNT_IDS)) {
            return ArrayUtils.contains(args.getLongArray(EXTRA_ACCOUNT_IDS), accountId);
        }
        return false;
    }

    private void initUnreadCount() {
        for (int i = 0, j = mTabIndicator.getCount(); i < j; i++) {
            mTabIndicator.setBadge(i, 0);
        }
    }

    private void openAccountsDrawer() {
        if (mDrawerLayout == null) return;
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private boolean openSettingsWizard() {
        if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false))
            return false;
        startActivity(new Intent(this, SettingsWizardActivity.class));
        return true;
    }

    private void setTabPosition(final int initialTab) {
        final boolean rememberPosition = mPreferences.getBoolean(KEY_REMEMBER_POSITION, true);
        if (initialTab >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(initialTab, mPagerAdapter.getCount(), 0));
        } else if (rememberPosition) {
            final int position = mPreferences.getInt(KEY_SAVED_TAB_POSITION, 0);
            mViewPager.setCurrentItem(MathUtils.clamp(position, mPagerAdapter.getCount(), 0));
        }
    }

    private void setupBars() {
        final int themeColor = getThemeColor();
        final int themeResId = getCurrentThemeResourceId();
        final String backgroundOption = getCurrentThemeBackgroundOption();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(this)) : 0xFF;
        final IHomeActionButton homeActionButton = (IHomeActionButton) mActionsButton;
        mTabIndicator.setItemContext(ThemeUtils.getActionBarThemedContext(this, themeResId, themeColor));
        ViewSupport.setBackground(mActionBarContainer, ThemeUtils.getActionBarBackground(this, themeResId, themeColor,
                backgroundOption, true));
        final int actionBarColor = ThemeUtils.getActionBarColor(this, themeColor, themeResId, backgroundOption);
        final int actionItemColor = ThemeUtils.getContrastForegroundColor(this, getCurrentThemeResourceId(), themeColor);
        final int[] foregroundColors = new int[2];
        ThemeUtils.getColorForegroundAndInverse(this, foregroundColors);
        if (ThemeUtils.isDarkTheme(themeResId)) {
            homeActionButton.setButtonColor(actionBarColor);
            homeActionButton.setIconColor(actionItemColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(themeColor);
            mTabIndicator.setIconColor(foregroundColors[0]);
            mTabIndicator.setLabelColor(foregroundColors[0]);
        } else {
            final int contrastColor = TwidereColorUtils.getContrastYIQ(themeColor,
                    ThemeUtils.ACCENT_COLOR_THRESHOLD, foregroundColors[0], foregroundColors[1]);
            homeActionButton.setButtonColor(themeColor);
            homeActionButton.setIconColor(actionItemColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(contrastColor);
            mTabIndicator.setIconColor(contrastColor);
            mTabIndicator.setLabelColor(contrastColor);
            ActivitySupport.setTaskDescription(this, new TaskDescriptionCompat(null, null, themeColor));
        }
        mHomeContent.setDrawColor(true);
        mHomeContent.setDrawShadow(false);
        mHomeContent.setColor(actionBarColor, actionBarAlpha);
        StatusBarProxy.setStatusBarDarkIcon(getWindow(), TwidereColorUtils.getYIQLuminance(actionBarColor) > ThemeUtils.ACCENT_COLOR_THRESHOLD);
        mHomeContent.setFactor(1);
        mActionBarWithOverlay.setAlpha(actionBarAlpha / 255f);
        mActionsButton.setAlpha(actionBarAlpha / 255f);
    }

    private void setupHomeTabs() {
        mPagerAdapter.clear();
        mPagerAdapter.addTabs(CustomTabUtils.getHomeTabs(this));
        final boolean hasNoTab = mPagerAdapter.getCount() == 0;
        mEmptyTabHint.setVisibility(hasNoTab ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(hasNoTab ? View.GONE : View.VISIBLE);
//        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() / 2);
    }

    private void setupSlidingMenu() {
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START);
        final Window window = getWindow();
        ThemeUtils.applyWindowBackground(this, mHomeContent, getCurrentThemeResourceId(),
                getThemeBackgroundOption(), getCurrentThemeBackgroundAlpha());
        window.setBackgroundDrawable(new EmptyDrawable());
    }

    private void showDataProfilingRequest() {
        //spice
        if (mPreferences.contains(KEY_USAGE_STATISTICS)) {
            return;
        }
        final Intent intent = new Intent(this, UsageStatisticsActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_stat_info);
        builder.setTicker(getString(R.string.usage_statistics));
        builder.setContentTitle(getString(R.string.usage_statistics));
        builder.setContentText(getString(R.string.usage_statistics_notification_summary));
        builder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID_DATA_PROFILING, builder.build());
    }

    private void triggerActionsClick() {
        if (mViewPager == null || mPagerAdapter == null) return;
        final int position = mViewPager.getCurrentItem();
        final SupportTabSpec tab = mPagerAdapter.getTab(position);
        if (tab == null) {
            startActivity(new Intent(INTENT_ACTION_COMPOSE));
        } else {
            if (classEquals(DirectMessagesFragment.class, tab.cls)) {
                openMessageConversation(this, -1, -1);
            } else if (classEquals(TrendsSuggestionsFragment.class, tab.cls)) {
                openSearchView(null);
            } else {
                startActivity(new Intent(INTENT_ACTION_COMPOSE));
            }
        }
    }

    private void updateActionsButton() {
        if (mViewPager == null || mPagerAdapter == null) return;
        final int icon, title;
        final int position = mViewPager.getCurrentItem();
        final SupportTabSpec tab = mPagerAdapter.getTab(position);
        if (tab == null) {
            title = R.string.compose;
            icon = R.drawable.ic_action_status_compose;
        } else {
            if (classEquals(DirectMessagesFragment.class, tab.cls)) {
                icon = R.drawable.ic_action_add;
                title = R.string.new_direct_message;
            } else if (classEquals(TrendsSuggestionsFragment.class, tab.cls)) {
                icon = R.drawable.ic_action_search;
                title = android.R.string.search_go;
            } else {
                icon = R.drawable.ic_action_status_compose;
                title = R.string.compose;
            }
        }
        if (mActionsButton instanceof IHomeActionButton) {
            final IHomeActionButton hab = (IHomeActionButton) mActionsButton;
            hab.setIcon(icon);
            hab.setTitle(title);
        }
    }

    private void updateActionsButtonStyle() {
        final boolean leftsideComposeButton = mPreferences.getBoolean(KEY_LEFTSIDE_COMPOSE_BUTTON, false);
        final FrameLayout.LayoutParams lp = (LayoutParams) mActionsButton.getLayoutParams();
        lp.gravity = Gravity.BOTTOM | (leftsideComposeButton ? Gravity.LEFT : Gravity.RIGHT);
        mActionsButton.setLayoutParams(lp);
    }

    private void updateSlidingMenuTouchMode() {
//        if (mViewPager == null || mSlidingMenu == null) return;
//        final int position = mViewPager.getCurrentItem();
//        final boolean pagingEnabled = mViewPager.isEnabled();
//        final boolean atFirstOrLast = position == 0 || position == mPagerAdapter.getCount() - 1;
//        final int mode = !pagingEnabled || atFirstOrLast ? SlidingMenu.TOUCHMODE_FULLSCREEN
//                : SlidingMenu.TOUCHMODE_MARGIN;
//        mSlidingMenu.setTouchModeAbove(mode);
    }

    private static final class AccountChangeObserver extends ContentObserver {
        private final HomeActivity mActivity;

        public AccountChangeObserver(final HomeActivity activity, final Handler handler) {
            super(handler);
            mActivity = activity;
        }

        @Override
        public void onChange(final boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            mActivity.notifyAccountsChanged();
            mActivity.updateUnreadCount();
        }
    }

    private static class UpdateUnreadCountTask extends AsyncTask<Object, Object, Map<SupportTabSpec, Integer>> {
        private final Context mContext;
        private final ReadStateManager mReadStateManager;
        private final TabPagerIndicator mIndicator;
        private final List<SupportTabSpec> mTabs;

        UpdateUnreadCountTask(final Context context, final ReadStateManager manager, final TabPagerIndicator indicator, final List<SupportTabSpec> tabs) {
            mContext = context;
            mReadStateManager = manager;
            mIndicator = indicator;
            mTabs = Collections.unmodifiableList(tabs);
        }

        @Override
        protected Map<SupportTabSpec, Integer> doInBackground(final Object... params) {
            final Map<SupportTabSpec, Integer> result = new HashMap<>();
            for (SupportTabSpec spec : mTabs) {
                switch (spec.type) {
                    case TAB_TYPE_HOME_TIMELINE: {
                        final long[] accountIds = Utils.getAccountIds(spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext, true, spec.tag, accountIds);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        result.put(spec, Utils.getStatusesCount(mContext, Statuses.CONTENT_URI, position, accountIds));
                        break;
                    }
                    case TAB_TYPE_MENTIONS_TIMELINE: {
                        final long[] accountIds = Utils.getAccountIds(spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext, true, spec.tag, accountIds);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        result.put(spec, Utils.getStatusesCount(mContext, Mentions.CONTENT_URI, position, accountIds));
                        break;
                    }
                    case TAB_TYPE_DIRECT_MESSAGES: {
                        break;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Map<SupportTabSpec, Integer> result) {
            mIndicator.clearBadge();
            for (Entry<SupportTabSpec, Integer> entry : result.entrySet()) {
                final SupportTabSpec key = entry.getKey();
                mIndicator.setBadge(key.position, entry.getValue());
            }
        }

    }


}
