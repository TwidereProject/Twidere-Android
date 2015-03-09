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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.util.Property;
import android.util.SparseArray;
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

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.DataProfilingSettingsActivity;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.activity.SettingsWizardActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.fragment.support.AccountsDashboardFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ColorUtils;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.FlymeUtils;
import org.mariotaku.twidere.util.HotKeyHandler;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UnreadCountUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ActivityAccessor;
import org.mariotaku.twidere.util.accessor.ActivityAccessor.TaskDescriptionCompat;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.util.message.UnreadCountUpdatedEvent;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.HomeSlidingMenu;
import org.mariotaku.twidere.view.LeftDrawerFrameLayout;
import org.mariotaku.twidere.view.RightDrawerFrameLayout;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
import org.mariotaku.twidere.view.iface.IHomeActionButton;

import edu.tsinghua.spice.Utilies.NetworkStateUtil;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;
import edu.ucdavis.earlybird.ProfilingUtil;

import static org.mariotaku.twidere.util.CompareUtils.classEquals;
import static org.mariotaku.twidere.util.CustomTabUtils.getAddedTabPosition;
import static org.mariotaku.twidere.util.Utils.cleanDatabasesByItemLimit;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTabDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.isDatabaseReady;
import static org.mariotaku.twidere.util.Utils.openMessageConversation;
import static org.mariotaku.twidere.util.Utils.openSearch;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

public class HomeActivity extends BaseSupportActivity implements OnClickListener, OnPageChangeListener,
        SupportFragmentCallback, OnOpenedListener, OnClosedListener,
        OnLongClickListener, AnimatorListener {

    private final Handler mHandler = new Handler();

    private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

    private final SparseArray<Fragment> mAttachedFragments = new SparseArray<>();
    private ParcelableAccount mSelectedAccountToSearch;

    private SharedPreferences mPreferences;

    private AsyncTwitterWrapper mTwitterWrapper;

    private NotificationManager mNotificationManager;

    private MultiSelectEventHandler mMultiSelectHandler;
    private HotKeyHandler mHotKeyHandler;

    private SupportTabsAdapter mPagerAdapter;

    private ExtendedViewPager mViewPager;
    private TabPagerIndicator mTabIndicator;
    private HomeSlidingMenu mSlidingMenu;
    private View mEmptyTabHint;
    private View mActionsButton;
    private View mTabsContainer;
    private View mActionBarOverlay;
    private LeftDrawerFrameLayout mLeftDrawerContainer;
    private RightDrawerFrameLayout mRightDrawerContainer;
    private TintedStatusFrameLayout mColorStatusFrameLayout;

    private Fragment mCurrentVisibleFragment;
    private UpdateUnreadCountTask mUpdateUnreadCountTask;

    private int mTabDisplayOption;
    private float mPagerPosition;
    private Toolbar mActionBar;
    private int mControlAnimationDirection;

    public void closeAccountsDrawer() {
        if (mSlidingMenu == null) return;
        mSlidingMenu.showContent();
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mControlAnimationDirection = 0;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mControlAnimationDirection = 0;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onDetachFragment(final Fragment fragment) {
        if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
            mAttachedFragments.remove(((IBaseFragment) fragment).getTabPosition());
        }
    }

    @Override
    public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
        if (isVisibleToUser) {
            mCurrentVisibleFragment = fragment;
        }
    }

    @Override
    public boolean triggerRefresh(final int position) {
        final Fragment f = mAttachedFragments.get(position);
        return f instanceof RefreshScrollTopInterface && !f.isDetached()
                && ((RefreshScrollTopInterface) f).triggerRefresh();
    }

    private static final long DURATION = 200l;

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        if (mControlAnimationDirection != 0) return;
        final ObjectAnimator animator;
        final float offset = getControlBarOffset();
        if (visible) {
            if (offset >= 1) return;
            animator = ObjectAnimator.ofFloat(this, ControlBarOffsetProperty.SINGLETON, offset, 1);
        } else {
            if (offset <= 0) return;
            animator = ObjectAnimator.ofFloat(this, ControlBarOffsetProperty.SINGLETON, offset, 0);
        }
        animator.addListener(this);
        animator.setDuration(DURATION);
        animator.start();
        mControlAnimationDirection = visible ? 1 : -1;
    }

    @Override
    public void setControlBarOffset(float offset) {
        mTabsContainer.setTranslationY(getControlBarHeight() * (offset - 1));
        final ViewGroup.LayoutParams lp = mActionsButton.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            mActionsButton.setTranslationY((((MarginLayoutParams) lp).bottomMargin + mActionsButton.getHeight()) * (1 - offset));
        } else {
            mActionsButton.setTranslationY(mActionsButton.getHeight() * (1 - offset));
        }
        notifyControlBarOffsetChanged();
    }

    public SlidingMenu getSlidingMenu() {
        return mSlidingMenu;
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoActionBarThemeResource(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                final FragmentManager fm = getSupportFragmentManager();
                final int count = fm.getBackStackEntryCount();

                if (mSlidingMenu.isMenuShowing()) {
                    mSlidingMenu.showContent();
                    return true;
                } else if (count == 0) {
                    mSlidingMenu.showMenu();
                    return true;
                }
                return true;
            }
            case MENU_SEARCH: {
                openSearchView(mSelectedAccountToSearch);
                return true;
            }
            case MENU_ACTIONS: {
                triggerActionsClick();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
        if (mCurrentVisibleFragment instanceof IBasePullToRefreshFragment)
            return (IBasePullToRefreshFragment) mCurrentVisibleFragment;
        else if (mCurrentVisibleFragment instanceof SupportFragmentCallback) {
            final Fragment curr = ((SupportFragmentCallback) mCurrentVisibleFragment).getCurrentVisibleFragment();
            if (curr instanceof IBasePullToRefreshFragment)
                return (IBasePullToRefreshFragment) curr;
        }
        return null;
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
        mTwitterWrapper = getTwitterWrapper();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mMultiSelectHandler = new MultiSelectEventHandler(this);
        mHotKeyHandler = new HotKeyHandler(this);
        mMultiSelectHandler.dispatchOnCreate();
        final long[] accountIds = getAccountIds(this);
        if (accountIds.length == 0) {
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
        mTabDisplayOption = getTabDisplayOptionInt(this);
        final int initialTabPosition = handleIntent(intent, savedInstanceState == null);

        ThemeUtils.applyBackground(mTabIndicator);
        mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mTabIndicator, 1);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabIndicator.setViewPager(mViewPager);
        mTabIndicator.setOnPageChangeListener(this);
        if (mTabDisplayOption != 0) {
            mTabIndicator.setTabDisplayOption(mTabDisplayOption);
        } else {
            mTabIndicator.setTabDisplayOption(TabPagerIndicator.ICON);
        }
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
        mActionsButton.setOnClickListener(this);
        mActionsButton.setOnLongClickListener(this);
        mEmptyTabHint.setOnClickListener(this);
        setTabPosition(initialTabPosition);
        setupSlidingMenu();
        setupBars();
        showDataProfilingRequest();
        initUnreadCount();
        updateActionsButton();
        updateSmartBar();
        updateSlidingMenuTouchMode();

        if (savedInstanceState == null) {
            if (refreshOnStart) {
                mTwitterWrapper.refreshAll();
            }
            if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
                openAccountsDrawer();
            }
        }
        mPagerPosition = Float.NaN;
        setupHomeTabs();
    }

    @Override
    protected void onPause() {
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONPAUSE));
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONRESUME));
        invalidateOptionsMenu();
        updateActionsButtonStyle();
        updateActionsButton();
        updateSmartBar();
        updateSlidingMenuTouchMode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMultiSelectHandler.dispatchOnStart();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
        final Bus bus = TwidereApplication.getInstance(this).getMessageBus();
        bus.register(this);
        if (getTabDisplayOptionInt(this) != mTabDisplayOption) {
            restart();
        }
        // UCD
        ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStart");
        // spice
        SpiceProfilingUtil.profile(this, SpiceProfilingUtil.FILE_NAME_APP, "App Launch" + "," + Build.MODEL);
        SpiceProfilingUtil.profile(this, SpiceProfilingUtil.FILE_NAME_ONLAUNCH, "App Launch" + "," + NetworkStateUtil.getConnectedType(this) + "," + Build.MODEL);
        //end
        updateUnreadCount();
    }

    @Override
    protected void onStop() {
        mMultiSelectHandler.dispatchOnStop();
        final Bus bus = TwidereApplication.getInstance(this).getMessageBus();
        bus.unregister(this);
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mAccountChangeObserver);
        mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).apply();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

        // UCD
        ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStop");
        // spice
        SpiceProfilingUtil.profile(this, SpiceProfilingUtil.FILE_NAME_APP, "App Stop");
        SpiceProfilingUtil.profile(this, SpiceProfilingUtil.FILE_NAME_ONLAUNCH, "App Stop" + "," + NetworkStateUtil.getConnectedType(this) + "," + Build.MODEL);
        //end
        super.onStop();
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        final int height = mTabIndicator != null ? mTabIndicator.getHeight() : 0;
        insets.top = height != 0 ? height : Utils.getActionBarHeight(this);
        return true;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void notifyAccountsChanged() {
        if (mPreferences == null) return;
        final long[] account_ids = getAccountIds(this);
        final long default_id = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
        if (account_ids == null || account_ids.length == 0) {
            finish();
        } else if (account_ids.length > 0 && !ArrayUtils.contains(account_ids, default_id)) {
            mPreferences.edit().putLong(KEY_DEFAULT_ACCOUNT_ID, account_ids[0]).apply();
        }
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateActionsButton();
        updateSmartBar();
    }

    @Subscribe
    public void notifyUnreadCountUpdated(UnreadCountUpdatedEvent event) {
        updateUnreadCount();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.action_buttons: {
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
    public void onClosed() {
        updateDrawerPercentOpen(0, true);
    }

    @Override
    public boolean onKeyUp(final int keyCode, @NonNull final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU: {
                if (mSlidingMenu != null) {
                    mSlidingMenu.toggle(true);
                    return true;
                }
                break;
            }
            default: {
                if (mHotKeyHandler.handleKey(keyCode, event)) return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
            updateDrawerPercentOpen(1, false);
        } else {
            updateDrawerPercentOpen(0, false);
        }
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, QuickSearchBarActivity.class));
        return true;
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.action_buttons: {
                showMenuItemToast(v, v.getContentDescription(), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onOpened() {
        updateDrawerPercentOpen(1, true);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
    }

    @Override
    public float getControlBarOffset() {
        final float totalHeight = getControlBarHeight();
        return 1 + mTabsContainer.getTranslationY() / totalHeight;
    }

    @Override
    public void onPageSelected(final int position) {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.showContent();
        }
        updateSlidingMenuTouchMode();
        updateActionsButton();
        updateSmartBar();
    }

    @Override
    public int getControlBarHeight() {
        return mTabIndicator.getHeight() - mTabIndicator.getStripHeight();
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        setControlBarVisibleAnimate(true);
    }

    public void openSearchView(final ParcelableAccount account) {
        mSelectedAccountToSearch = account;
        onSearchRequested();
    }

    public void setSystemWindowInsets(Rect insets) {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        if (fragment instanceof AccountsDashboardFragment) {
            ((AccountsDashboardFragment) fragment).setStatusBarHeight(insets.top);
        }
        //TODO
        mColorStatusFrameLayout.setStatusBarHeight(insets.top);
    }

    public void updateUnreadCount() {
        if (mTabIndicator == null || mUpdateUnreadCountTask != null
                && mUpdateUnreadCountTask.getStatus() == TwidereAsyncTask.Status.RUNNING) return;
        mUpdateUnreadCountTask = new UpdateUnreadCountTask(mTabIndicator);
        mUpdateUnreadCountTask.executeTask();
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SWIPEBACK_ACTIVITY: {
                // closeAccountsDrawer();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final int tab_position = handleIntent(intent, false);
        if (tab_position >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(tab_position, mPagerAdapter.getCount(), 0));
        }
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
            mAttachedFragments.put(((IBaseFragment) fragment).getTabPosition(), fragment);
        }
    }

    @Override
    protected void onDestroy() {
        // Delete unused items in databases.
        cleanDatabasesByItemLimit(this);
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONDESTROY));
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.showContent();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mActionBar = (Toolbar) findViewById(R.id.actionbar);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mSlidingMenu = (HomeSlidingMenu) findViewById(R.id.home_menu);
        mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
        mEmptyTabHint = findViewById(R.id.empty_tab_hint);
        mActionsButton = findViewById(R.id.action_buttons);
        mTabsContainer = findViewById(R.id.tabs_container);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mActionBarOverlay = findViewById(R.id.actionbar_overlay);
        mColorStatusFrameLayout = (TintedStatusFrameLayout) findViewById(R.id.home_content);
    }

    @Override
    protected boolean shouldSetWindowBackground() {
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
        final long[] refreshedIds = intent.getLongArrayExtra(EXTRA_IDS);
        if (refreshedIds != null) {
            mTwitterWrapper.refreshAll(refreshedIds);
        } else if (firstCreate && refreshOnStart) {
            mTwitterWrapper.refreshAll();
        }

        final int tab = intent.getIntExtra(EXTRA_INITIAL_TAB, -1);
        final int initialTab = tab != -1 ? tab : getAddedTabPosition(this, intent.getStringExtra(EXTRA_TAB_TYPE));
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

    private boolean hasActivatedTask() {
        if (mTwitterWrapper == null) return false;
        return mTwitterWrapper.hasActivatedTask();
    }

    private void initUnreadCount() {
        for (int i = 0, j = mTabIndicator.getCount(); i < j; i++) {
            mTabIndicator.setBadge(i, 0);
        }
    }

    private void openAccountsDrawer() {
        if (mSlidingMenu == null) return;
        mSlidingMenu.showMenu();
    }

    private boolean openSettingsWizard() {
        if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false))
            return false;
        startActivity(new Intent(this, SettingsWizardActivity.class));
        return true;
    }

    private void setTabPosition(final int initial_tab) {
        final boolean remember_position = mPreferences.getBoolean(KEY_REMEMBER_POSITION, true);
        if (initial_tab >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(initial_tab, mPagerAdapter.getCount(), 0));
        } else if (remember_position) {
            final int position = mPreferences.getInt(KEY_SAVED_TAB_POSITION, 0);
            mViewPager.setCurrentItem(MathUtils.clamp(position, mPagerAdapter.getCount(), 0));
        }
    }

    private void setupBars() {
        final int themeColor = getThemeColor();
        final int themeResId = getCurrentThemeResourceId();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(themeResId);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getUserThemeBackgroundAlpha(this) : 0xFF;
        final IHomeActionButton homeActionButton = (IHomeActionButton) mActionsButton;
        mTabIndicator.setItemContext(ThemeUtils.getActionBarContext(this));
        if (ThemeUtils.isColoredActionBar(themeResId)) {
            final int contrastColor = ColorUtils.getContrastYIQ(themeColor, 192);
            ViewAccessor.setBackground(mActionBar, new ColorDrawable(themeColor));
            homeActionButton.setButtonColor(themeColor);
            homeActionButton.setIconColor(contrastColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(contrastColor);
            mTabIndicator.setIconColor(contrastColor);
            ActivityAccessor.setTaskDescription(this, new TaskDescriptionCompat(null, null, themeColor));
            mColorStatusFrameLayout.setDrawColor(true);
            mColorStatusFrameLayout.setDrawShadow(false);
            mColorStatusFrameLayout.setColor(themeColor, actionBarAlpha);
            mColorStatusFrameLayout.setFactor(1);
        } else {
            final int backgroundColor = ThemeUtils.getThemeBackgroundColor(mTabIndicator.getItemContext());
            final int foregroundColor = ThemeUtils.getThemeForegroundColor(mTabIndicator.getItemContext());
            ViewAccessor.setBackground(mActionBar, ThemeUtils.getActionBarBackground(this, themeResId));
            homeActionButton.setButtonColor(backgroundColor);
            homeActionButton.setIconColor(foregroundColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(themeColor);
            mTabIndicator.setIconColor(foregroundColor);
            mColorStatusFrameLayout.setDrawColor(false);
            mColorStatusFrameLayout.setDrawShadow(false);
        }
        mTabIndicator.setAlpha(actionBarAlpha / 255f);
        mActionsButton.setAlpha(actionBarAlpha / 255f);
        ViewAccessor.setBackground(mActionBarOverlay, ThemeUtils.getWindowContentOverlay(this));
    }

    private void setupHomeTabs() {
        mPagerAdapter.clear();
        mPagerAdapter.addTabs(CustomTabUtils.getHomeTabs(this));
        final boolean hasNoTab = mPagerAdapter.getCount() == 0;
        mEmptyTabHint.setVisibility(hasNoTab ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(hasNoTab ? View.GONE : View.VISIBLE);
    }

    private void setupSlidingMenu() {
        if (mSlidingMenu == null) return;
        final int marginThreshold = getResources().getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
        mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow_left);
        mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);
//        mSlidingMenu.setBehindWidthRes(R.dimen.drawer_width_home);
        mSlidingMenu.setBehindOffsetRes(R.dimen.drawer_offset_home);
        mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
        mSlidingMenu.setFadeDegree(0.5f);
        mSlidingMenu.setMenu(R.layout.drawer_home_accounts);
        mSlidingMenu.setSecondaryMenu(R.layout.drawer_home_quick_menu);
        mSlidingMenu.setOnOpenedListener(this);
        mSlidingMenu.setOnClosedListener(this);
        mLeftDrawerContainer = (LeftDrawerFrameLayout) mSlidingMenu.getMenu().findViewById(R.id.left_drawer_container);
        mRightDrawerContainer = (RightDrawerFrameLayout) mSlidingMenu.getSecondaryMenu().findViewById(
                R.id.right_drawer_container);
        final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(this);
        mLeftDrawerContainer.setClipEnabled(isTransparentBackground);
        mLeftDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
        mRightDrawerContainer.setClipEnabled(isTransparentBackground);
        mRightDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
        mSlidingMenu.setBehindCanvasTransformer(new ListenerCanvasTransformer(this));
        final Window window = getWindow();
        final Drawable windowBackground = ThemeUtils.getWindowBackground(this, getCurrentThemeResourceId());
        ViewAccessor.setBackground(mSlidingMenu.getContent(), windowBackground);
        window.setBackgroundDrawable(new EmptyDrawable(windowBackground));
    }

    private void showDataProfilingRequest() {
        //spice
        if (mPreferences.getBoolean(KEY_SHOW_UCD_DATA_PROFILING_REQUEST, true) || mPreferences.getBoolean(KEY_SHOW_SPICE_DATA_PROFILING_REQUEST, true)) {
            final Intent intent = new Intent(this, DataProfilingSettingsActivity.class);
            final PendingIntent content_intent = PendingIntent.getActivity(this, 0, intent, 0);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_stat_info);
            builder.setTicker(getString(R.string.data_profiling_notification_ticker));
            builder.setContentTitle(getString(R.string.data_profiling_notification_title));
            builder.setContentText(getString(R.string.data_profiling_notification_desc));
            builder.setContentIntent(content_intent);
            mNotificationManager.notify(NOTIFICATION_ID_DATA_PROFILING, builder.build());
        }
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
            } else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
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
            } else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
                icon = R.drawable.ic_action_search;
                title = android.R.string.search_go;
            } else {
                icon = R.drawable.ic_action_status_compose;
                title = R.string.compose;
            }
        }
        final boolean hasActivatedTask = hasActivatedTask();
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

    private void updateDrawerPercentOpen(final float percentOpen, final boolean horizontalScroll) {
        if (mLeftDrawerContainer == null || mRightDrawerContainer == null) return;
        mLeftDrawerContainer.setPercentOpen(percentOpen);
        mRightDrawerContainer.setPercentOpen(percentOpen);
    }

    private void updateSlidingMenuTouchMode() {
        if (mViewPager == null || mSlidingMenu == null) return;
        final int position = mViewPager.getCurrentItem();
        final boolean pagingEnabled = mViewPager.isEnabled();
        final boolean atFirstOrLast = position == 0 || position == mPagerAdapter.getCount() - 1;
        final int mode = !pagingEnabled || atFirstOrLast ? SlidingMenu.TOUCHMODE_FULLSCREEN
                : SlidingMenu.TOUCHMODE_MARGIN;
        mSlidingMenu.setTouchModeAbove(mode);
    }

    private void updateSmartBar() {
        final boolean useBottomActionItems = FlymeUtils.hasSmartBar();
        if (useBottomActionItems) {
            invalidateOptionsMenu();
        }
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

    private static class ListenerCanvasTransformer implements CanvasTransformer {
        private final HomeActivity mHomeActivity;

        public ListenerCanvasTransformer(final HomeActivity homeActivity) {
            mHomeActivity = homeActivity;
        }

        @Override
        public void transformCanvas(final Canvas canvas, final float percentOpen) {
            mHomeActivity.updateDrawerPercentOpen(percentOpen, true);
        }

    }

    private static class UpdateUnreadCountTask extends TwidereAsyncTask<Void, Void, int[]> {
        private final Context mContext;
        private final TabPagerIndicator mIndicator;

        UpdateUnreadCountTask(final TabPagerIndicator indicator) {
            mIndicator = indicator;
            mContext = indicator.getContext();
        }

        @Override
        protected int[] doInBackground(final Void... params) {
            final int tabCount = mIndicator.getCount();
            final int[] result = new int[tabCount];
            for (int i = 0, j = tabCount; i < j; i++) {
                result[i] = UnreadCountUtils.getUnreadCount(mContext, i);
            }
            return result;
        }

        @Override
        protected void onPostExecute(final int[] result) {
            final int tabCount = mIndicator.getCount();
            if (result == null || result.length != tabCount) return;
            for (int i = 0; i < tabCount; i++) {
                mIndicator.setBadge(i, result[i]);
            }
        }

    }

    public void moveControlBarBy(float delta) {
        final int min = -getControlBarHeight(), max = 0;
        mTabsContainer.setTranslationY(MathUtils.clamp(mTabsContainer.getTranslationY() + delta, max, min));
        final ViewGroup.LayoutParams ablp = mActionsButton.getLayoutParams();
        final int totalHeight;
        if (ablp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) ablp;
            totalHeight = mActionsButton.getHeight() + mlp.topMargin + mlp.bottomMargin;
        } else {
            totalHeight = mActionsButton.getHeight();
        }
        mActionsButton.setTranslationY(MathUtils.clamp(mActionsButton.getTranslationY() - delta, totalHeight, 0));
        notifyControlBarOffsetChanged();
    }

    private static class ControlBarOffsetProperty extends Property<IControlBarActivity, Float> {
        public static final ControlBarOffsetProperty SINGLETON = new ControlBarOffsetProperty();

        @Override
        public void set(IControlBarActivity object, Float value) {
            object.setControlBarOffset(value);
        }

        public ControlBarOffsetProperty() {
            super(Float.TYPE, null);
        }

        @Override
        public Float get(IControlBarActivity object) {
            return object.getControlBarOffset();
        }
    }
}
