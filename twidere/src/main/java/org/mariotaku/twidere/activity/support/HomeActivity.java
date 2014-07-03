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

import static org.mariotaku.twidere.util.CompareUtils.classEquals;
import static org.mariotaku.twidere.util.CustomTabUtils.getAddedTabPosition;
import static org.mariotaku.twidere.util.CustomTabUtils.getHomeTabs;
import static org.mariotaku.twidere.util.Utils.cleanDatabasesByItemLimit;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTabDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.isDatabaseReady;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;
import static org.mariotaku.twidere.util.Utils.openSearch;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.CustomViewAbove;
import com.jeremyfeinstein.slidingmenu.lib.CustomViewBehind;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.readystatesoftware.viewbadger.BadgeView;

import edu.ucdavis.earlybird.ProfilingUtil;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.DataProfilingSettingsActivity;
import org.mariotaku.twidere.activity.SettingsWizardActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.FlymeUtils;
import org.mariotaku.twidere.util.HotKeyHandler;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.SwipebackActivityUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UnreadCountUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.HomeActionsActionView;
import org.mariotaku.twidere.view.LeftDrawerFrameLayout;
import org.mariotaku.twidere.view.RightDrawerFrameLayout;
import org.mariotaku.twidere.view.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseSupportActivity implements OnClickListener, OnPageChangeListener,
		SupportFragmentCallback, SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener, OnLongClickListener {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateActionsButton();
				updateSmartBar();
			} else if (BROADCAST_UNREAD_COUNT_UPDATED.equals(action)) {
				updateUnreadCount();
			}
		}

	};

	private final Handler mHandler = new Handler();

	private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

	private final ArrayList<SupportTabSpec> mCustomTabs = new ArrayList<SupportTabSpec>();

	private final SparseArray<Fragment> mAttachedFragments = new SparseArray<Fragment>();
	private Account mSelectedAccountToSearch;

	private SharedPreferences mPreferences;

	private AsyncTwitterWrapper mTwitterWrapper;

	private NotificationManager mNotificationManager;

	private MultiSelectEventHandler mMultiSelectHandler;
	private HotKeyHandler mHotKeyHandler;

	private ActionBar mActionBar;
	private SupportTabsAdapter mPagerAdapter;

	private ExtendedViewPager mViewPager;
	private TabPageIndicator mIndicator;
	private HomeSlidingMenu mSlidingMenu;
	private View mEmptyTabHint;
	private ProgressBar mSmartBarProgress;
	private HomeActionsActionView mActionsButton, mBottomActionsButton;
	private LeftDrawerFrameLayout mLeftDrawerContainer;
	private RightDrawerFrameLayout mRightDrawerContainer;

	private Fragment mCurrentVisibleFragment;
	private UpdateUnreadCountTask mUpdateUnreadCountTask;

	private int mTabDisplayOption;

	private boolean mBottomComposeButton;

	public void closeAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showContent();
	}

	@Override
	public Fragment getCurrentVisibleFragment() {
		return mCurrentVisibleFragment;
	}

	public SlidingMenu getSlidingMenu() {
		return mSlidingMenu;
	}

	public ViewPager getViewPager() {
		return mViewPager;
	}

	public void hideControls() {
		// TODO Auto-generated method stub

	}

	public void notifyAccountsChanged() {
		if (mPreferences == null) return;
		final long[] account_ids = getAccountIds(this);
		final long default_id = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
		if (account_ids == null || account_ids.length == 0) {
			finish();
		} else if (account_ids.length > 0 && !ArrayUtils.contains(account_ids, default_id)) {
			mPreferences.edit().putLong(KEY_DEFAULT_ACCOUNT_ID, account_ids[0]).commit();
		}
	}

	@Override
	public void onAttachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.put(((IBaseFragment) fragment).getTabPosition(), fragment);
		}
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
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.actions:
			case R.id.actions_button:
			case R.id.actions_button_bottom: {
				triggerActionsClick();
				break;
			}
		}
	}

	@Override
	public void onClosed() {
		updateDrawerPercentOpen(0, true);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
		mEmptyTabHint = findViewById(R.id.empty_tab_hint);
		mBottomActionsButton = (HomeActionsActionView) findViewById(R.id.actions_button_bottom);
		if (mSlidingMenu == null) {
			mSlidingMenu = new HomeSlidingMenu(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);
		final MenuItem itemProgress = menu.findItem(MENU_PROGRESS);
		mSmartBarProgress = (ProgressBar) itemProgress.getActionView().findViewById(android.R.id.progress);
		updateActionsButton();
		return true;
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.remove(((IBaseFragment) fragment).getTabPosition());
		}
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
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
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.actions:
			case R.id.actions_button: {
				showMenuItemToast(v, v.getContentDescription());
				return true;
			}
			case R.id.actions_button_bottom: {
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				final int count = fm.getBackStackEntryCount();

				if (mSlidingMenu.isMenuShowing()) {
					mSlidingMenu.showContent();
					return true;
				} else if (count == 0 && !mSlidingMenu.isMenuShowing()) {
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
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(final int state) {

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
	public boolean onPrepareOptionsMenu(final Menu menu) {
		if (mViewPager == null || mPagerAdapter == null) return false;
		final boolean useBottomActionItems = FlymeUtils.hasSmartBar() && isBottomComposeButton();
		setMenuItemAvailability(menu, MENU_ACTIONS, useBottomActionItems);
		setMenuItemAvailability(menu, MENU_PROGRESS, useBottomActionItems);
		if (useBottomActionItems) {
			final int icon, title;
			final int position = mViewPager.getCurrentItem();
			final SupportTabSpec tab = mPagerAdapter.getTab(position);
			if (tab == null) {
				title = R.string.compose;
				icon = R.drawable.ic_iconic_action_compose;
			} else {
				if (classEquals(DirectMessagesFragment.class, tab.cls)) {
					icon = R.drawable.ic_iconic_action_new_message;
					title = R.string.new_direct_message;
				} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
					icon = R.drawable.ic_iconic_action_search;
					title = android.R.string.search_go;
				} else {
					icon = R.drawable.ic_iconic_action_compose;
					title = R.string.compose;
				}
			}
			final MenuItem actionsItem = menu.findItem(MENU_ACTIONS);
			actionsItem.setIcon(icon);
			actionsItem.setTitle(title);
		}
		return true;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appSearchData = new Bundle();
		if (mSelectedAccountToSearch != null) {
			appSearchData.putLong(EXTRA_ACCOUNT_ID, mSelectedAccountToSearch.account_id);
		}
		startSearch(null, false, appSearchData, false);
		return true;
	}

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
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

	public void openSearchView(final Account account) {
		mSelectedAccountToSearch = account;
		onSearchRequested();
	}

	public void setHomeProgressBarIndeterminateVisibility(final boolean visible) {
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return false;
	}

	public void showControls() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean triggerRefresh(final int position) {
		final Fragment f = mAttachedFragments.get(position);
		return f instanceof RefreshScrollTopInterface && !f.isDetached()
				&& ((RefreshScrollTopInterface) f).triggerRefresh();
	}

	public void updateUnreadCount() {
		if (mIndicator == null || mUpdateUnreadCountTask != null
				&& mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mUpdateUnreadCountTask = new UpdateUnreadCountTask(mIndicator, mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
		mUpdateUnreadCountTask.execute();
	}

	@Override
	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		if (mCurrentVisibleFragment instanceof IBasePullToRefreshFragment)
			return (IBasePullToRefreshFragment) mCurrentVisibleFragment;
		else if (mCurrentVisibleFragment instanceof SupportFragmentCallback) {
			final Fragment curr = ((SupportFragmentCallback) mCurrentVisibleFragment).getCurrentVisibleFragment();
			if (curr instanceof IBasePullToRefreshFragment) return (IBasePullToRefreshFragment) curr;
		}
		return null;
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

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mBottomComposeButton = isBottomComposeButton();
		setUiOptions(getWindow());
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
		final Resources res = getResources();
		final boolean displayIcon = res.getBoolean(R.bool.home_display_icon);
		final long[] accountIds = getAccountIds(this);
		if (accountIds.length == 0) {
			final Intent sign_in_intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			sign_in_intent.setClass(this, SignInActivity.class);
			startActivity(sign_in_intent);
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
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
		final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
		mTabDisplayOption = getTabDisplayOptionInt(this);
		final int initialTabPosition = handleIntent(intent, savedInstanceState == null);
		mActionBar = getActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);

		final View view = mActionBar.getCustomView();
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mActionsButton = (HomeActionsActionView) view.findViewById(R.id.actions_button);
		ThemeUtils.applyBackground(mIndicator);
		mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mIndicator, 1);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
		if (mTabDisplayOption != 0) {
			mIndicator.setDisplayLabel((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_LABEL) != 0);
			mIndicator.setDisplayIcon((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_ICON) != 0);
		} else {
			mIndicator.setDisplayLabel(false);
			mIndicator.setDisplayIcon(true);
		}
		mActionsButton.setOnClickListener(this);
		mActionsButton.setOnLongClickListener(this);
		mBottomActionsButton.setOnClickListener(this);
		mBottomActionsButton.setOnLongClickListener(this);
		initTabs();
		final boolean tabsNotEmpty = mPagerAdapter.getCount() > 0;
		mEmptyTabHint.setVisibility(tabsNotEmpty ? View.GONE : View.VISIBLE);
		mViewPager.setVisibility(tabsNotEmpty ? View.VISIBLE : View.GONE);
		mActionBar.setDisplayShowHomeEnabled(displayIcon || !tabsNotEmpty);
		mActionBar.setHomeButtonEnabled(displayIcon || !tabsNotEmpty);
		mActionBar.setDisplayShowTitleEnabled(!tabsNotEmpty);
		mActionBar.setDisplayShowCustomEnabled(tabsNotEmpty);
		setTabPosition(initialTabPosition);
		setupSlidingMenu();
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
	}

	@Override
	protected void onDestroy() {
		// Delete unused items in databases.
		cleanDatabasesByItemLimit(this);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONDESTROY));
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final int tab_position = handleIntent(intent, false);
		if (tab_position >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(tab_position, mPagerAdapter.getCount(), 0));
		}
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
		mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
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
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		filter.addAction(BROADCAST_UNREAD_COUNT_UPDATED);
		registerReceiver(mStateReceiver, filter);
		if (isTabsChanged(getHomeTabs(this)) || mBottomComposeButton != isBottomComposeButton()
				|| getTabDisplayOptionInt(this) != mTabDisplayOption) {
			restart();
		}
		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStart");
		updateUnreadCount();
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
		unregisterReceiver(mStateReceiver);
		final ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(mAccountChangeObserver);
		mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).commit();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStop");
		super.onStop();
	}

	protected void setPagingEnabled(final boolean enabled) {
		if (mIndicator != null && mViewPager != null) {
			mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
			mIndicator.setSwitchingEnabled(enabled);
			mIndicator.setEnabled(enabled);
		}
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
			SwipebackActivityUtils.startSwipebackActivity(this, extraIntent);
		}
		return initialTab;
	}

	private boolean hasActivatedTask() {
		if (mTwitterWrapper == null) return false;
		return mTwitterWrapper.hasActivatedTask();
	}

	private void initTabs() {
		final List<SupportTabSpec> tabs = getHomeTabs(this);
		mCustomTabs.clear();
		mCustomTabs.addAll(tabs);
		mPagerAdapter.clear();
		mPagerAdapter.addTabs(tabs);
	}

	private void initUnreadCount() {
		for (int i = 0, j = mIndicator.getTabCount(); i < j; i++) {
			final BadgeView badge = new BadgeView(this, mIndicator.getTabItem(i).findViewById(R.id.tab_item_content));
			badge.setId(R.id.unread_count);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setTextSize(getResources().getInteger(R.integer.unread_count_text_size));
			badge.hide();
		}
	}

	private boolean isBottomComposeButton() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		return preferences != null && preferences.getBoolean(KEY_BOTTOM_COMPOSE_BUTTON, false);
	}

	private boolean isTabsChanged(final List<SupportTabSpec> tabs) {
		if (mCustomTabs.size() == 0 && tabs == null) return false;
		if (mCustomTabs.size() != tabs.size()) return true;
		for (int i = 0, size = mCustomTabs.size(); i < size; i++) {
			if (!mCustomTabs.get(i).equals(tabs.get(i))) return true;
		}
		return false;
	}

	private void openAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showMenu();
	}

	private boolean openSettingsWizard() {
		if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false)) return false;
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

	private void setUiOptions(final Window window) {
		if (FlymeUtils.hasSmartBar()) {
			if (mBottomComposeButton) {
				window.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
			} else {
				window.setUiOptions(0);
			}
		}
	}

	private void setupSlidingMenu() {
		if (mSlidingMenu == null) return;
		final int marginThreshold = getResources().getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow_left);
		mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);
		mSlidingMenu.setBehindWidthRes(R.dimen.drawer_width_home);
		mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
		mSlidingMenu.setFadeDegree(0.5f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
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
		if (isTransparentBackground) {
			final Drawable windowBackground = ThemeUtils.getWindowBackground(this, getCurrentThemeResourceId());
			ViewAccessor.setBackground(mSlidingMenu.getContent(), windowBackground);
			window.setBackgroundDrawable(new EmptyDrawable());
		} else {
			window.setBackgroundDrawable(null);
		}
	}

	private void showDataProfilingRequest() {
		if (mPreferences.getBoolean(KEY_SHOW_UCD_DATA_PROFILING_REQUEST, true)) {
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
				openDirectMessagesConversation(this, -1, -1);
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
			icon = R.drawable.ic_iconic_action_compose;
		} else {
			if (classEquals(DirectMessagesFragment.class, tab.cls)) {
				icon = R.drawable.ic_iconic_action_new_message;
				title = R.string.new_direct_message;
			} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
				icon = R.drawable.ic_iconic_action_search;
				title = android.R.string.search_go;
			} else {
				icon = R.drawable.ic_iconic_action_compose;
				title = R.string.compose;
			}
		}
		final boolean hasActivatedTask = hasActivatedTask();
		if (mActionsButton != null) {
			mActionsButton.setIcon(icon);
			mActionsButton.setTitle(title);
			mActionsButton.setShowProgress(hasActivatedTask);
		}
		if (mBottomActionsButton != null) {
			mBottomActionsButton.setIcon(icon);
			mBottomActionsButton.setTitle(title);
			mBottomActionsButton.setShowProgress(hasActivatedTask);
		}
		if (mSmartBarProgress != null) {
			mSmartBarProgress.setVisibility(hasActivatedTask ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private void updateActionsButtonStyle() {
		if (mActionsButton == null || mBottomActionsButton == null) return;
		final boolean isBottomActionsButton = isBottomComposeButton();
		final boolean showBottomActionsButton = !FlymeUtils.hasSmartBar() && isBottomActionsButton;
		final boolean leftsideComposeButton = mPreferences.getBoolean(KEY_LEFTSIDE_COMPOSE_BUTTON, false);
		mActionsButton.setVisibility(isBottomActionsButton ? View.GONE : View.VISIBLE);
		mBottomActionsButton.setVisibility(showBottomActionsButton ? View.VISIBLE : View.GONE);
		final FrameLayout.LayoutParams compose_lp = (LayoutParams) mBottomActionsButton.getLayoutParams();
		compose_lp.gravity = Gravity.BOTTOM | (leftsideComposeButton ? Gravity.LEFT : Gravity.RIGHT);
		mBottomActionsButton.setLayoutParams(compose_lp);
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
		final boolean useBottomActionItems = FlymeUtils.hasSmartBar() && isBottomComposeButton();
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

	private static class HomeSlidingMenu extends SlidingMenu {

		private final HomeActivity mActivity;

		public HomeSlidingMenu(final HomeActivity activity) {
			super(activity);
			mActivity = activity;
		}

		@Override
		public boolean dispatchTouchEvent(final MotionEvent ev) {
			switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_DOWN: {
					final boolean isTouchingMargin = isTouchingMargin(ev);
					setTouchModeAbove(isTouchingMargin ? TOUCHMODE_MARGIN : TOUCHMODE_FULLSCREEN);
					break;
				}
			}
			return super.dispatchTouchEvent(ev);
		}

		@Override
		protected CustomViewAbove newCustomViewAbove(final Context context) {
			return new MyCustomViewAbove(context, this);
		}

		@Override
		protected CustomViewBehind newCustomViewBehind(final Context context) {
			return new MyCustomViewBehind(context, this);
		}

		private ViewPager getViewPager() {
			return mActivity.getViewPager();
		}

		private boolean isTouchingMargin(final MotionEvent e) {
			final float x = e.getX(), marginThreshold = getTouchmodeMarginThreshold();
			final View content = getContent();
			final int mode = getMode(), left = content.getLeft(), right = content.getRight();
			if (mode == SlidingMenu.LEFT)
				return x >= left && x <= marginThreshold + left;
			else if (mode == SlidingMenu.RIGHT)
				return x <= right && x >= right - marginThreshold;
			else if (mode == SlidingMenu.LEFT_RIGHT)
				return x >= left && x <= marginThreshold + left || x <= right && x >= right - marginThreshold;
			return false;
		}

		private static class MyCustomViewAbove extends CustomViewAbove {

			private final HomeSlidingMenu mSlidingMenu;

			public MyCustomViewAbove(final Context context, final HomeSlidingMenu slidingMenu) {
				super(context);
				mSlidingMenu = slidingMenu;

			}

		}

		private static class MyCustomViewBehind extends CustomViewBehind {

			private final HomeSlidingMenu mSlidingMenu;

			public MyCustomViewBehind(final Context context, final HomeSlidingMenu slidingMenu) {
				super(context);
				mSlidingMenu = slidingMenu;
			}

			@Override
			public boolean menuClosedSlideAllowed(final float dx) {
				if (mSlidingMenu.getTouchModeAbove() != SlidingMenu.TOUCHMODE_FULLSCREEN)
					return super.menuClosedSlideAllowed(dx);
				final ViewPager viewPager = mSlidingMenu.getViewPager();
				if (viewPager == null) return false;
				final boolean canScrollHorizontally = viewPager.canScrollHorizontally(Math.round(-dx));
				final int mode = getMode();
				if (mode == SlidingMenu.LEFT)
					return dx > 0 && !canScrollHorizontally;
				else if (mode == SlidingMenu.RIGHT)
					return dx < 0 && !canScrollHorizontally;
				else if (mode == SlidingMenu.LEFT_RIGHT) return !canScrollHorizontally;
				return false;
			}
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

	private static class UpdateUnreadCountTask extends AsyncTask<Void, Void, int[]> {
		private final Context mContext;
		private final TabPageIndicator mIndicator;
		private final boolean mEnabled;

		UpdateUnreadCountTask(final TabPageIndicator indicator, final boolean enabled) {
			mIndicator = indicator;
			mContext = indicator.getContext();
			mEnabled = enabled;
		}

		@Override
		protected int[] doInBackground(final Void... params) {
			final int tab_count = mIndicator.getTabCount();
			final int[] result = new int[tab_count];
			for (int i = 0, j = tab_count; i < j; i++) {
				result[i] = UnreadCountUtils.getUnreadCount(mContext, i);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final int[] result) {
			final int tab_count = mIndicator.getTabCount();
			if (result == null || result.length != tab_count) return;
			for (int i = 0, j = tab_count; i < j; i++) {
				final BadgeView badge = (BadgeView) mIndicator.getTabItem(i).findViewById(R.id.unread_count);
				if (!mEnabled) {
					badge.setCount(0);
					badge.hide();
					continue;
				}
				final int count = result[i];
				if (count > 0) {
					badge.setCount(count);
					badge.show();
				} else if (count == 0) {
					badge.setCount(0);
					badge.hide();
				} else {
					badge.setText("\u0387");
					badge.show();
				}
			}
		}

	}

}
