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

import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayoutAccessor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageButton;

import com.emojidex.emojidexandroid.Emojidex;
import com.squareup.otto.Subscribe;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.fragment.AccountsDashboardFragment;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.MessagesEntriesFragment;
import org.mariotaku.twidere.fragment.TrendsSuggestionsFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.TaskStateChangedEvent;
import org.mariotaku.twidere.model.message.UnreadCountUpdatedEvent;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.service.StreamingService;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.mariotaku.twidere.util.TwidereViewUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ExtendedRelativeLayout;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.HomeDrawerLayout;
import org.mariotaku.twidere.view.TabPagerIndicator;

import java.util.Collections;
import java.util.List;

public class HomeActivity extends BaseActivity implements OnClickListener, OnPageChangeListener,
        SupportFragmentCallback, OnLongClickListener, DrawerLayout.DrawerListener {
    private static final int[] HOME_AS_UP_ATTRS = {android.support.v7.appcompat.R.attr.homeAsUpIndicator};

    private final Handler mHandler = new Handler();

    private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

    private ParcelableAccount mSelectedAccountToSearch;
    private int mTabColumns;


    private MultiSelectEventHandler mMultiSelectHandler;

    private SupportTabsAdapter mPagerAdapter;
    private ExtendedViewPager mViewPager;
    private Toolbar mToolbar;
    private View mWindowOverlay;
    private TabPagerIndicator mTabIndicator;
    private HomeDrawerLayout mDrawerLayout;
    private View mEmptyTabHint;
    private FloatingActionButton mActionsButton;
    private ExtendedRelativeLayout mHomeContent;
    private ImageButton mDrawerToggleButton;


    private UpdateUnreadCountTask mUpdateUnreadCountTask;
    private OnSharedPreferenceChangeListener mReadStateChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateUnreadCount();
        }
    };
    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBarDrawerToggle.Delegate mHomeDrawerToggleDelegate = new ActionBarDrawerToggle.Delegate() {
        @Override
        public void setActionBarUpIndicator(Drawable upDrawable, @StringRes int contentDescRes) {
            mDrawerToggleButton.setImageDrawable(upDrawable);
            mDrawerToggleButton.setContentDescription(getString(contentDescRes));
        }

        @Override
        public void setActionBarDescription(@StringRes int contentDescRes) {
            mDrawerToggleButton.setContentDescription(getString(contentDescRes));
        }

        @Override
        public Drawable getThemeUpIndicator() {
            final TintTypedArray a = TintTypedArray.obtainStyledAttributes(
                    getActionBarThemedContext(), null, HOME_AS_UP_ATTRS);
            final Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        @Override
        public Context getActionBarThemedContext() {
            return mToolbar.getContext();
        }

        @Override
        public boolean isNavigationVisible() {
            return true;
        }
    };

    public void closeAccountsDrawer() {
        if (mDrawerLayout == null) return;
        mDrawerLayout.closeDrawers();
    }

    @NonNull
    public UserKey[] getActivatedAccountKeys() {
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            return ((AccountsDashboardFragment) fragment).getActivatedAccountIds();
        }
        return DataStoreUtils.getActivatedAccountKeys(this);
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

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mTabIndicator == null || mHomeContent == null) return false;
        final int height = mTabIndicator.getHeight();
        if (height != 0) {
            insets.top = height;
        } else {
            insets.top = ThemeUtils.getActionBarHeight(this);
        }
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
                    if (previous < 0 && DrawerLayoutAccessor.findDrawerWithGravity(mDrawerLayout, Gravity.START) != null) {
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
                    if (next >= mPagerAdapter.getCount() && DrawerLayoutAccessor.findDrawerWithGravity(mDrawerLayout, Gravity.END) != null) {
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
                    drawer.openDrawer(GravityCompat.START);
                }
                return true;
            }
            case KeyEvent.KEYCODE_BACK: {
                final DrawerLayout drawer = mDrawerLayout;
                if (isDrawerOpen()) {
                    drawer.closeDrawers();
                    return true;
                }
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
        super.onCreate(savedInstanceState);
        mMultiSelectHandler = new MultiSelectEventHandler(this);
        mMultiSelectHandler.dispatchOnCreate();
        if (!DataStoreUtils.hasAccount(this)) {
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
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_ACTION_MODE_OVERLAY);
        setContentView(R.layout.activity_home);

        setSupportActionBar(mToolbar);

        ThemeUtils.setCompatContentViewOverlay(getWindow(), new EmptyDrawable());

        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        int tabDisplayOptionInt = Utils.getTabDisplayOptionInt(this);

        mTabColumns = getResources().getInteger(R.integer.default_tab_columns);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_accounts_dashboard,
                R.string.close_accounts_dashboard);
        mHomeContent.setOnFitSystemWindowsListener(this);
        mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mTabIndicator, mTabColumns);
        mViewPager.setAdapter(mPagerAdapter);
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

        if (mPreferences.getBoolean(KEY_DRAWER_TOGGLE)) {
            mDrawerToggleButton.setVisibility(View.VISIBLE);
        } else {
            mDrawerToggleButton.setVisibility(View.GONE);
        }

        mHomeContent.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (top != oldTop) {
                    final Fragment fragment = getLeftDrawerFragment();
                    if (fragment instanceof AccountsDashboardFragment) {
                        ((AccountsDashboardFragment) fragment).setStatusBarHeight(top);
                    }
                }
            }
        });

        mActionsButton.setOnClickListener(this);
        mActionsButton.setOnLongClickListener(this);
        mDrawerToggleButton.setOnClickListener(this);
        mEmptyTabHint.setOnClickListener(this);

        setupSlidingMenu();
        setupBars();
        initUnreadCount();
        setupHomeTabs();
        updateActionsButton();

        if (savedInstanceState == null) {
            if (refreshOnStart) {
                mTwitterWrapper.refreshAll(getActivatedAccountKeys());
            }
            if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
                openAccountsDrawer();
            }
        }

        final int initialTabPosition = handleIntent(intent, savedInstanceState == null,
                savedInstanceState != null);
        setTabPosition(initialTabPosition);

        if (Utils.isStreamingEnabled()) {
            startService(new Intent(this, StreamingService.class));
        }

        // Initialize emojidex.
        final Emojidex emojidex = Emojidex.getInstance();
        emojidex.initialize(this);
        if(emojidex.getAllEmojiList().isEmpty())
            emojidex.reload();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMultiSelectHandler.dispatchOnStart();
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
        mBus.register(this);

        mReadStateManager.registerOnSharedPreferenceChangeListener(mReadStateChangeListener);
        updateUnreadCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        updateActionsButton();
    }

    @Override
    protected void onPause() {
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

        super.onStop();
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
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, CustomTabsFragment.class.getName());
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.tabs);
                startActivityForResult(intent, REQUEST_SETTINGS);
                break;
            }
            case R.id.drawer_toggle: {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.actions_button: {
                Utils.showMenuItemToast(v, v.getContentDescription(), true);
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
    }

    public void updateUnreadCount() {
        if (mTabIndicator == null || mUpdateUnreadCountTask != null
                && mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mUpdateUnreadCountTask = new UpdateUnreadCountTask(this, mReadStateManager, mTabIndicator,
                mPagerAdapter.getTabs());
        AsyncTaskUtils.executeTask(mUpdateUnreadCountTask);
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
    }

    public List<SupportTabSpec> getTabs() {
        return mPagerAdapter.getTabs();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final int tabPosition = handleIntent(intent, false, false);
        if (tabPosition >= 0) {
            mViewPager.setCurrentItem(TwidereMathUtils.clamp(tabPosition, mPagerAdapter.getCount(), 0));
        }
    }

    @Override
    protected void onDestroy() {

        stopService(new Intent(this, StreamingService.class));

        // Delete unused items in databases.

        final Context context = getApplicationContext();
        TaskStarter.execute(new AbstractTask() {
            @Override
            public Object doLongOperation(Object o) {
                DataStoreUtils.cleanDatabasesByItemLimit(context);
                return null;
            }
        });
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        return 1 + mToolbar.getTranslationY() / totalHeight;
    }

    @Override
    public void setControlBarOffset(float offset) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        final int translationY = mTabColumns > 1 ? 0 : (int) (getControlBarHeight() * (offset - 1));
        mToolbar.setTranslationY(translationY);
        mWindowOverlay.setTranslationY(translationY);
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
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        mDrawerLayout = (HomeDrawerLayout) findViewById(R.id.home_menu);
        mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
        mEmptyTabHint = findViewById(R.id.empty_tab_hint);
        mActionsButton = (FloatingActionButton) findViewById(R.id.actions_button);
        mHomeContent = (ExtendedRelativeLayout) findViewById(R.id.home_content);
        mWindowOverlay = findViewById(R.id.window_overlay);
        mDrawerToggleButton = (ImageButton) findViewById(R.id.drawer_toggle);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            ((AccountsDashboardFragment) fragment).loadAccounts();
        }
    }

    @Nullable
    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return mHomeDrawerToggleDelegate;
    }

    @Override
    public int getControlBarHeight() {
        return mTabIndicator.getHeight() - mTabIndicator.getStripHeight();
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

    private int handleIntent(final Intent intent, final boolean handleExtraIntent,
                             final boolean restoreInstanceState) {
        // use packge's class loader to prevent BadParcelException
        intent.setExtrasClassLoader(getClassLoader());
        // reset intent
        setIntent(new Intent(this, HomeActivity.class));
        final String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            final Bundle appSearchData = intent.getBundleExtra(SearchManager.APP_DATA);
            final UserKey accountKey;
            if (appSearchData != null && appSearchData.containsKey(EXTRA_ACCOUNT_KEY)) {
                accountKey = appSearchData.getParcelable(EXTRA_ACCOUNT_KEY);
            } else {
                accountKey = Utils.getDefaultAccountKey(this);
            }
            IntentUtils.openSearch(this, accountKey, query);
            return -1;
        }
        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        if (handleExtraIntent && refreshOnStart) {
            mTwitterWrapper.refreshAll();
        }
        final Intent extraIntent = intent.getParcelableExtra(EXTRA_EXTRA_INTENT);

        final Uri uri = intent.getData();
        @CustomTabType
        final String tabType = uri != null ? Utils.matchTabType(uri) : null;
        int initialTab = -1;
        if (tabType != null) {
            final UserKey accountKey = UserKey.valueOf(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY));
            for (int i = 0, j = mPagerAdapter.getCount(); i < j; i++) {
                final SupportTabSpec tab = mPagerAdapter.getTab(i);
                if (tabType.equals(CustomTabUtils.getTabTypeAlias(tab.type))) {
                    if (tab.args != null && CustomTabUtils.hasAccountId(this, tab.args,
                            getActivatedAccountKeys(), accountKey)) {
                        initialTab = i;
                        break;
                    }
                }
            }
            if (initialTab == -1 && (extraIntent == null || !handleExtraIntent)) {
                // Tab not found, open account specific page
                switch (tabType) {
                    case CustomTabType.NOTIFICATIONS_TIMELINE: {
                        IntentUtils.openInteractions(this, accountKey);
                        return -1;
                    }
                    case CustomTabType.DIRECT_MESSAGES: {
                        IntentUtils.openDirectMessages(this, accountKey);
                        return -1;
                    }
                }
            }
        }
        if (extraIntent != null && handleExtraIntent) {
            extraIntent.setExtrasClassLoader(getClassLoader());
            startActivity(extraIntent);
        }
        return initialTab;
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
            mViewPager.setCurrentItem(TwidereMathUtils.clamp(initialTab, mPagerAdapter.getCount(), 0));
        } else if (rememberPosition) {
            final int position = mPreferences.getInt(KEY_SAVED_TAB_POSITION, 0);
            mViewPager.setCurrentItem(TwidereMathUtils.clamp(position, mPagerAdapter.getCount(), 0));
        }
    }

    private void setupBars() {
        final String backgroundOption = getCurrentThemeBackgroundOption();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(this)) : 0xFF;
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
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.addDrawerListener(this);
        mDrawerLayout.setShouldDisableDecider(new HomeDrawerLayout.ShouldDisableDecider() {
            @Override
            public boolean shouldDisableTouch(MotionEvent e) {
                final Fragment fragment = getLeftDrawerFragment();
                if (fragment instanceof AccountsDashboardFragment) {
                    RecyclerView accountsSelector = ((AccountsDashboardFragment) fragment)
                            .getAccountsSelector();
                    if (accountsSelector != null) {
                        return TwidereViewUtils.hitView(e.getRawX(), e.getRawY(), accountsSelector);
                    }
                }
                return false;
            }
        });
    }

    private void triggerActionsClick() {
        if (mViewPager == null || mPagerAdapter == null) return;
        final int position = mViewPager.getCurrentItem();
        if (mPagerAdapter.getCount() == 0) return;
        final SupportTabSpec tab = mPagerAdapter.getTab(position);
        if (DirectMessagesFragment.class == tab.cls) {
            IntentUtils.openMessageConversation(this, null, null);
        } else if (MessagesEntriesFragment.class == tab.cls) {
            IntentUtils.openMessageConversation(this, null, null);
        } else if (TrendsSuggestionsFragment.class == tab.cls) {
            openSearchView(null);
        } else {
            startActivity(new Intent(INTENT_ACTION_COMPOSE));
        }
    }

    private void updateActionsButton() {
        if (mViewPager == null || mPagerAdapter == null) return;
        final int icon, title;
        final int position = mViewPager.getCurrentItem();
        if (mPagerAdapter.getCount() == 0) return;
        final SupportTabSpec tab = mPagerAdapter.getTab(position);
        if (DirectMessagesFragment.class == tab.cls) {
            icon = R.drawable.ic_action_add;
            title = R.string.new_direct_message;
        } else if (MessagesEntriesFragment.class == tab.cls) {
            icon = R.drawable.ic_action_add;
            title = R.string.new_direct_message;
        } else if (TrendsSuggestionsFragment.class == tab.cls) {
            icon = R.drawable.ic_action_search;
            title = android.R.string.search_go;
        } else {
            icon = R.drawable.ic_action_status_compose;
            title = R.string.compose;
        }
        mActionsButton.setImageResource(icon);
        mActionsButton.setContentDescription(getString(title));
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

    private static class UpdateUnreadCountTask extends AsyncTask<Object, UpdateUnreadCountTask.TabBadge,
            SparseIntArray> {
        private final Context mContext;
        private final ReadStateManager mReadStateManager;
        private final TabPagerIndicator mIndicator;
        private final List<SupportTabSpec> mTabs;

        UpdateUnreadCountTask(final Context context, final ReadStateManager manager,
                              final TabPagerIndicator indicator, final List<SupportTabSpec> tabs) {
            mContext = context;
            mReadStateManager = manager;
            mIndicator = indicator;
            mTabs = Collections.unmodifiableList(tabs);
        }

        @Override
        protected SparseIntArray doInBackground(final Object... params) {
            final SparseIntArray result = new SparseIntArray();
            for (int i = 0, j = mTabs.size(); i < j; i++) {
                SupportTabSpec spec = mTabs.get(i);
                if (spec.type == null) {
                    publishProgress(new TabBadge(i, -1));
                    continue;
                }
                switch (spec.type) {
                    case CustomTabType.HOME_TIMELINE: {
                        final UserKey[] accountKeys = Utils.getAccountKeys(mContext, spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext,
                                true, ReadPositionTag.HOME_TIMELINE, accountKeys);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        final int count = DataStoreUtils.getStatusesCount(mContext, Statuses.CONTENT_URI,
                                spec.args, position, Statuses.STATUS_TIMESTAMP, true, accountKeys);
                        result.put(i, count);
                        publishProgress(new TabBadge(i, count));
                        break;
                    }
                    case CustomTabType.NOTIFICATIONS_TIMELINE: {
                        final UserKey[] accountIds = Utils.getAccountKeys(mContext, spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext,
                                true, ReadPositionTag.ACTIVITIES_ABOUT_ME, accountIds);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        final int count = DataStoreUtils.getInteractionsCount(mContext, spec.args,
                                accountIds, position, Activities.TIMESTAMP);
                        publishProgress(new TabBadge(i, count));
                        result.put(i, count);
                        break;
                    }
                    default: {
                        publishProgress(new TabBadge(i, -1));
                        break;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(final SparseIntArray result) {
            mIndicator.clearBadge();
            for (int i = 0, j = result.size(); i < j; i++) {
                mIndicator.setBadge(result.keyAt(i), result.valueAt(i));
            }
        }

        @Override
        protected void onProgressUpdate(TabBadge... values) {
            for (TabBadge value : values) {
                mIndicator.setBadge(value.index, value.count);
            }
        }

        static class TabBadge {
            int index;
            int count;

            public TabBadge(int index, int count) {
                this.index = index;
                this.count = count;
            }
        }
    }


}
