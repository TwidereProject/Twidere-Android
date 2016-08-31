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

package org.mariotaku.twidere.activity

import android.app.PendingIntent
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceActivity
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.DrawerLayoutAccessor
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.TintTypedArray
import android.util.SparseIntArray
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup.MarginLayoutParams
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home_content.*
import kotlinx.android.synthetic.main.layout_empty_tab_hint.*
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.KeyboardShortcutConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.graphic.EmptyDrawable
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TaskStateChangedEvent
import org.mariotaku.twidere.model.message.UnreadCountUpdatedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.service.StreamingService
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.view.HomeDrawerLayout
import org.mariotaku.twidere.view.TabPagerIndicator

class HomeActivity : BaseActivity(), OnClickListener, OnPageChangeListener, SupportFragmentCallback, OnLongClickListener, DrawerLayout.DrawerListener {

    private val handler = Handler()

    private val accountChangeObserver = AccountChangeObserver(this, handler)

    private var selectedAccountToSearch: ParcelableAccount? = null
    private var tabColumns: Int = 0


    private var multiSelectHandler: MultiSelectEventHandler? = null

    private var pagerAdapter: SupportTabsAdapter? = null

    private var updateUnreadCountTask: UpdateUnreadCountTask? = null
    private val readStateChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key -> updateUnreadCount() }
    private val controlBarShowHideHelper = IControlBarActivity.ControlBarShowHideHelper(this)
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val homeDrawerToggleDelegate = object : ActionBarDrawerToggle.Delegate {
        override fun setActionBarUpIndicator(upDrawable: Drawable, @StringRes contentDescRes: Int) {
            drawerToggleButton.setImageDrawable(upDrawable)
            drawerToggleButton.contentDescription = getString(contentDescRes)
        }

        override fun setActionBarDescription(@StringRes contentDescRes: Int) {
            drawerToggleButton.contentDescription = getString(contentDescRes)
        }

        override fun getThemeUpIndicator(): Drawable {
            val a = TintTypedArray.obtainStyledAttributes(
                    actionBarThemedContext, null, HOME_AS_UP_ATTRS)
            val result = a.getDrawable(0)
            a.recycle()
            return result
        }

        override fun getActionBarThemedContext(): Context {
            return toolbar!!.context
        }

        override fun isNavigationVisible(): Boolean {
            return true
        }
    }

    fun closeAccountsDrawer() {
        if (homeMenu == null) return
        homeMenu.closeDrawers()
    }

    val activatedAccountKeys: Array<UserKey>
        get() {
            val fragment = leftDrawerFragment
            if (fragment is AccountsDashboardFragment) {
                return fragment.activatedAccountIds
            }
            return DataStoreUtils.getActivatedAccountKeys(this)
        }

    override val currentVisibleFragment: Fragment?
        get() {
            val currentItem = mainPager!!.currentItem
            if (currentItem < 0 || currentItem >= pagerAdapter!!.count) return null
            return pagerAdapter!!.instantiateItem(mainPager, currentItem) as Fragment
        }

    override fun triggerRefresh(position: Int): Boolean {
        val f = pagerAdapter!!.instantiateItem(mainPager, position) as Fragment
        if (f !is RefreshScrollTopInterface) return false
        if (f.activity == null || f.isDetached) return false
        return f.triggerRefresh()
    }

    val leftDrawerFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.leftDrawer)

    override fun getSystemWindowsInsets(insets: Rect): Boolean {
        if (mainTabs == null || homeContent == null) return false
        val height = mainTabs.height
        if (height != 0) {
            insets.top = height
        } else {
            insets.top = ThemeUtils.getActionBarHeight(this)
        }
        return true
    }

    override fun setControlBarVisibleAnimate(visible: Boolean) {
        controlBarShowHideHelper.setControlBarVisibleAnimate(visible)
    }

    override fun setControlBarVisibleAnimate(visible: Boolean, listener: IControlBarActivity.ControlBarShowHideHelper.ControlBarAnimationListener) {
        controlBarShowHideHelper.setControlBarVisibleAnimate(visible, listener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            android.R.id.home -> {
                val fm = supportFragmentManager
                val count = fm.backStackEntryCount
                if (homeMenu.isDrawerOpen(GravityCompat.START) || homeMenu.isDrawerOpen(GravityCompat.END)) {
                    homeMenu.closeDrawers()
                    return true
                } else if (count == 0) {
                    homeMenu.openDrawer(GravityCompat.START)
                    return true
                }
                return true
            }
            R.id.search -> {
                openSearchView(selectedAccountToSearch)
                return true
            }
            R.id.actions -> {
                triggerActionsClick()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true
        var action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_HOME, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                KeyboardShortcutConstants.ACTION_HOME_ACCOUNTS_DASHBOARD -> {
                    if (homeMenu.isDrawerOpen(GravityCompat.START)) {
                        homeMenu.closeDrawers()
                    } else {
                        homeMenu.openDrawer(GravityCompat.START)
                        setControlBarVisibleAnimate(true)
                    }
                    return true
                }
            }
        }
        action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                KeyboardShortcutConstants.ACTION_NAVIGATION_PREVIOUS_TAB -> {
                    val previous = mainPager!!.currentItem - 1
                    if (previous < 0 && DrawerLayoutAccessor.findDrawerWithGravity(homeMenu, Gravity.START) != null) {
                        homeMenu.openDrawer(GravityCompat.START)
                        setControlBarVisibleAnimate(true)
                    } else if (previous < pagerAdapter!!.count) {
                        if (homeMenu.isDrawerOpen(GravityCompat.END)) {
                            homeMenu.closeDrawers()
                        } else {
                            mainPager!!.setCurrentItem(previous, true)
                        }
                    }
                    return true
                }
                KeyboardShortcutConstants.ACTION_NAVIGATION_NEXT_TAB -> {
                    val next = mainPager!!.currentItem + 1
                    if (next >= pagerAdapter!!.count && DrawerLayoutAccessor.findDrawerWithGravity(homeMenu, Gravity.END) != null) {
                        homeMenu.openDrawer(GravityCompat.END)
                        setControlBarVisibleAnimate(true)
                    } else if (next >= 0) {
                        if (homeMenu.isDrawerOpen(GravityCompat.START)) {
                            homeMenu.closeDrawers()
                        } else {
                            mainPager!!.setCurrentItem(next, true)
                        }
                    }
                    return true
                }
            }
        }
        return handler.handleKey(this, null, keyCode, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true
        return super.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        if (handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState))
            return true
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                val drawer = homeMenu
                if (isDrawerOpen) {
                    drawer!!.closeDrawers()
                } else {
                    drawer!!.openDrawer(GravityCompat.START)
                }
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                val drawer = homeMenu
                if (isDrawerOpen) {
                    drawer!!.closeDrawers()
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private val isDrawerOpen: Boolean
        get() {
            val drawer = homeMenu ?: return false
            return drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)
        }

    /**
     * Called when the context is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiSelectHandler = MultiSelectEventHandler(this)
        multiSelectHandler!!.dispatchOnCreate()
        if (!DataStoreUtils.hasAccount(this)) {
            val signInIntent = Intent(INTENT_ACTION_TWITTER_LOGIN)
            signInIntent.setClass(this, SignInActivity::class.java)
            startActivity(signInIntent)
            finish()
            return
        } else {
            notifyAccountsChanged()
        }
        val intent = intent
        if (openSettingsWizard()) {
            finish()
            return
        }
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_ACTION_MODE_OVERLAY)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        ThemeUtils.setCompatContentViewOverlay(window, EmptyDrawable())

        val refreshOnStart = preferences.getBoolean(SharedPreferenceConstants.KEY_REFRESH_ON_START, false)
        var tabDisplayOptionInt = Utils.getTabDisplayOptionInt(this)

        tabColumns = resources.getInteger(R.integer.default_tab_columns)

        drawerToggle = ActionBarDrawerToggle(this, homeMenu, R.string.open_accounts_dashboard,
                R.string.close_accounts_dashboard)
        homeContent!!.setOnFitSystemWindowsListener(this)
        pagerAdapter = SupportTabsAdapter(this, supportFragmentManager, mainTabs, tabColumns)
        mainPager!!.adapter = pagerAdapter
        mainTabs.setViewPager(mainPager)
        mainTabs.setOnPageChangeListener(this)
        mainTabs.setColumns(tabColumns)
        if (tabDisplayOptionInt == 0) {
            tabDisplayOptionInt = TabPagerIndicator.ICON
        }
        mainTabs.setTabDisplayOption(tabDisplayOptionInt)
        mainTabs.setTabExpandEnabled(tabDisplayOptionInt and TabPagerIndicator.LABEL == 0)
        mainTabs.setDisplayBadge(preferences.getBoolean(SharedPreferenceConstants.KEY_UNREAD_COUNT, true))
        mainTabs.updateAppearance()

        if (preferences.getBoolean(SharedPreferenceConstants.KEY_DRAWER_TOGGLE)) {
            drawerToggleButton.visibility = View.VISIBLE
        } else {
            drawerToggleButton.visibility = View.GONE
        }

        homeContent!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (top != oldTop) {
                val fragment = leftDrawerFragment
                if (fragment is AccountsDashboardFragment) {
                    fragment.setStatusBarHeight(top)
                }
            }
        }

        actionsButton.setOnClickListener(this)
        actionsButton.setOnLongClickListener(this)
        drawerToggleButton.setOnClickListener(this)
        emptyTabHint.setOnClickListener(this)

        setupSlidingMenu()
        setupBars()
        showDataProfilingRequest()
        initUnreadCount()
        setupHomeTabs()
        updateActionsButton()

        if (savedInstanceState == null) {
            if (refreshOnStart) {
                twitterWrapper.refreshAll(activatedAccountKeys)
            }
            if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
                openAccountsDrawer()
            }
        }

        val initialTabPosition = handleIntent(intent, savedInstanceState == null,
                savedInstanceState != null)
        setTabPosition(initialTabPosition)

        if (Utils.isStreamingEnabled()) {
            startService(Intent(this, StreamingService::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        multiSelectHandler!!.dispatchOnStart()
        val resolver = contentResolver
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, accountChangeObserver)
        bus.register(this)

        readStateManager.registerOnSharedPreferenceChangeListener(readStateChangeListener)
        updateUnreadCount()
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        updateActionsButton()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        multiSelectHandler!!.dispatchOnStop()
        readStateManager.unregisterOnSharedPreferenceChangeListener(readStateChangeListener)
        bus.unregister(this)
        val resolver = contentResolver
        resolver.unregisterContentObserver(accountChangeObserver)
        preferences.edit().putInt(SharedPreferenceConstants.KEY_SAVED_TAB_POSITION, mainPager!!.currentItem).apply()

        super.onStop()
    }

    fun notifyAccountsChanged() {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    @Subscribe
    fun notifyTaskStateChanged(event: TaskStateChangedEvent) {
        updateActionsButton()
    }

    @Subscribe
    fun notifyUnreadCountUpdated(event: UnreadCountUpdatedEvent) {
        updateUnreadCount()
    }

    override fun onClick(v: View) {
        when (v) {
            actionsButton -> {
                triggerActionsClick()
            }
            emptyTabHint -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, CustomTabsFragment::class.java.name)
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.tabs)
                startActivityForResult(intent, REQUEST_SETTINGS)
            }
            drawerToggleButton -> {
                if (homeMenu.isDrawerOpen(GravityCompat.START) || homeMenu.isDrawerOpen(GravityCompat.END)) {
                    homeMenu.closeDrawers()
                } else {
                    homeMenu.openDrawer(GravityCompat.START)
                }
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v) {
            actionsButton -> {
                Utils.showMenuItemToast(v, v.contentDescription, true)
                return true
            }
        }
        return false
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        //TODO handle secondary drawer
        if (homeMenu.isDrawerOpen(GravityCompat.START)) {
            homeMenu.closeDrawers()
        }
        updateActionsButton()
    }

    override fun onPageScrollStateChanged(state: Int) {
        setControlBarVisibleAnimate(true)
    }

    override fun onSearchRequested(): Boolean {
        startActivity(Intent(this, QuickSearchBarActivity::class.java))
        return true
    }

    fun openSearchView(account: ParcelableAccount?) {
        selectedAccountToSearch = account
        onSearchRequested()
    }

    override fun onFitSystemWindows(insets: Rect) {
        super.onFitSystemWindows(insets)
        val fragment = leftDrawerFragment
        if (fragment is AccountsDashboardFragment) {
            fragment.requestFitSystemWindows()
        }
    }

    fun updateUnreadCount() {
        if (mainTabs == null || updateUnreadCountTask != null && updateUnreadCountTask!!.status == AsyncTask.Status.RUNNING)
            return
        updateUnreadCountTask = UpdateUnreadCountTask(this, readStateManager, mainTabs,
                pagerAdapter!!.tabs.toTypedArray())
        AsyncTaskUtils.executeTask<UpdateUnreadCountTask, Any>(updateUnreadCountTask)
        mainTabs.setDisplayBadge(preferences.getBoolean(SharedPreferenceConstants.KEY_UNREAD_COUNT, true))
    }

    val tabs: List<SupportTabSpec>
        get() = pagerAdapter!!.tabs

    override fun onNewIntent(intent: Intent) {
        val tabPosition = handleIntent(intent, false, false)
        if (tabPosition >= 0) {
            mainPager!!.currentItem = TwidereMathUtils.clamp(tabPosition, pagerAdapter!!.count, 0)
        }
    }

    override fun onDestroy() {

        stopService(Intent(this, StreamingService::class.java))

        // Delete unused items in databases.

        val context = applicationContext
        TaskStarter.execute(object : AbstractTask<Any?, Any?, Any?>() {
            override fun doLongOperation(o: Any?): Any? {
                DataStoreUtils.cleanDatabasesByItemLimit(context)
                return null
            }
        })
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggle
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun getControlBarOffset(): Float {
        if (tabColumns > 1) {
            val lp = actionsButton.layoutParams
            val total: Float
            if (lp is MarginLayoutParams) {
                total = (lp.bottomMargin + actionsButton.height).toFloat()
            } else {
                total = actionsButton.height.toFloat()
            }
            return 1 - actionsButton.translationY / total
        }
        val totalHeight = controlBarHeight.toFloat()
        return 1 + toolbar!!.translationY / totalHeight
    }

    override fun setControlBarOffset(offset: Float) {
        val translationY = if (tabColumns > 1) 0 else (controlBarHeight * (offset - 1)).toInt()
        toolbar.translationY = translationY.toFloat()
        windowOverlay.translationY = translationY.toFloat()
        val lp = actionsButton.layoutParams
        if (lp is MarginLayoutParams) {
            actionsButton.translationY = (lp.bottomMargin + actionsButton.height) * (1 - offset)
        } else {
            actionsButton.translationY = actionsButton.height * (1 - offset)
        }
        notifyControlBarOffsetChanged()
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerOpened(drawerView: View) {

    }

    override fun onDrawerClosed(drawerView: View) {

    }

    override fun onDrawerStateChanged(newState: Int) {
        val fragment = leftDrawerFragment
        if (fragment is AccountsDashboardFragment) {
            fragment.loadAccounts()
        }
    }

    override fun getDrawerToggleDelegate(): ActionBarDrawerToggle.Delegate? {
        return homeDrawerToggleDelegate
    }

    override fun getControlBarHeight(): Int {
        return mainTabs.height - mainTabs.stripHeight
    }

    private val keyboardShortcutRecipient: Fragment?
        get() {
            if (homeMenu.isDrawerOpen(GravityCompat.START)) {
                return leftDrawerFragment
            } else if (homeMenu.isDrawerOpen(GravityCompat.END)) {
                return null
            } else {
                return currentVisibleFragment
            }
        }

    private fun handleFragmentKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
                                                     keyCode: Int, repeatCount: Int,
                                                     event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState)
        }
        return false
    }

    private fun handleFragmentKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
                                                     keyCode: Int, event: KeyEvent,
                                                     metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState)
        }
        return false
    }

    private fun isFragmentKeyboardShortcutHandled(handler: KeyboardShortcutsHandler,
                                                  keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.isKeyboardShortcutHandled(handler, keyCode,
                    event, metaState)
        }
        return false
    }

    private fun handleIntent(intent: Intent, handleExtraIntent: Boolean,
                             restoreInstanceState: Boolean): Int {
        // use package's class loader to prevent BadParcelException
        intent.setExtrasClassLoader(classLoader)
        // reset intent
        setIntent(Intent(this, HomeActivity::class.java))
        val action = intent.action
        if (Intent.ACTION_SEARCH == action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            val appSearchData = intent.getBundleExtra(SearchManager.APP_DATA)
            val accountKey: UserKey?
            if (appSearchData != null && appSearchData.containsKey(EXTRA_ACCOUNT_KEY)) {
                accountKey = appSearchData.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            } else {
                accountKey = Utils.getDefaultAccountKey(this)
            }
            IntentUtils.openSearch(this, accountKey, query)
            return -1
        }
        val refreshOnStart = preferences.getBoolean(SharedPreferenceConstants.KEY_REFRESH_ON_START, false)
        if (handleExtraIntent && refreshOnStart) {
            twitterWrapper.refreshAll()
        }
        val extraIntent = intent.getParcelableExtra<Intent>(EXTRA_EXTRA_INTENT)

        val uri = intent.data
        @CustomTabType
        val tabType = if (uri != null) Utils.matchTabType(uri) else null
        var initialTab = -1
        if (tabType != null) {
            val accountKey = UserKey.valueOf(uri!!.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY))
            for (i in 0 until pagerAdapter!!.count) {
                val tab = pagerAdapter!!.getTab(i)
                if (tabType == CustomTabUtils.getTabTypeAlias(tab.type)) {
                    val args = tab.args
                    if (args != null && CustomTabUtils.hasAccountId(this, args,
                            activatedAccountKeys, accountKey)) {
                        initialTab = i
                        break
                    }
                }
            }
            if (initialTab == -1 && (extraIntent == null || !handleExtraIntent)) {
                // Tab not found, open account specific page
                when (tabType) {
                    CustomTabType.NOTIFICATIONS_TIMELINE -> {
                        IntentUtils.openInteractions(this, accountKey)
                        return -1
                    }
                    CustomTabType.DIRECT_MESSAGES -> {
                        IntentUtils.openDirectMessages(this, accountKey)
                        return -1
                    }
                }
            }
        }
        if (extraIntent != null && handleExtraIntent) {
            extraIntent.setExtrasClassLoader(classLoader)
            startActivity(extraIntent)
        }
        return initialTab
    }

    private fun initUnreadCount() {
        for (i in 0 until mainTabs.count) {
            mainTabs.setBadge(i, 0)
        }
    }

    private fun openAccountsDrawer() {
        if (homeMenu == null) return
        homeMenu.openDrawer(GravityCompat.START)
    }

    private fun openSettingsWizard(): Boolean {
        if (preferences.getBoolean(SharedPreferenceConstants.KEY_SETTINGS_WIZARD_COMPLETED, false))
            return false
        startActivity(Intent(this, SettingsWizardActivity::class.java))
        return true
    }

    private fun setTabPosition(initialTab: Int) {
        val rememberPosition = preferences.getBoolean(SharedPreferenceConstants.KEY_REMEMBER_POSITION, true)
        if (initialTab >= 0) {
            mainPager!!.currentItem = TwidereMathUtils.clamp(initialTab, pagerAdapter!!.count, 0)
        } else if (rememberPosition) {
            val position = preferences.getInt(SharedPreferenceConstants.KEY_SAVED_TAB_POSITION, 0)
            mainPager!!.currentItem = TwidereMathUtils.clamp(position, pagerAdapter!!.count, 0)
        }
    }

    private fun setupBars() {
        val backgroundOption = currentThemeBackgroundOption
        val isTransparent = ThemeUtils.isTransparentBackground(backgroundOption)
        val actionBarAlpha = if (isTransparent) ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(this)) else 0xFF
        actionsButton.alpha = actionBarAlpha / 255f
    }

    private fun setupHomeTabs() {
        pagerAdapter!!.clear()
        pagerAdapter!!.addTabs(CustomTabUtils.getHomeTabs(this))
        val hasNoTab = pagerAdapter!!.count == 0
        emptyTabHint.visibility = if (hasNoTab) View.VISIBLE else View.GONE
        mainPager!!.visibility = if (hasNoTab) View.GONE else View.VISIBLE
        //        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() / 2);
    }

    private fun setupSlidingMenu() {
        homeMenu.setDrawerShadow(R.drawable.drawer_shadow_start, GravityCompat.START)
        homeMenu.addDrawerListener(drawerToggle)
        homeMenu.addDrawerListener(this)
        homeMenu.setShouldDisableDecider(HomeDrawerLayout.ShouldDisableDecider { e ->
            val fragment = leftDrawerFragment
            if (fragment is AccountsDashboardFragment) {
                val accountsSelector = fragment.accountsSelector
                if (accountsSelector != null) {
                    return@ShouldDisableDecider TwidereViewUtils.hitView(e.rawX, e.rawY, accountsSelector)
                }
            }
            false
        })
    }

    private fun showDataProfilingRequest() {
        //spice
        if (preferences.contains(KEY_USAGE_STATISTICS)) {
            return
        }
        val intent = Intent(this, UsageStatisticsActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this)
        builder.setAutoCancel(true)
        builder.setSmallIcon(R.drawable.ic_stat_info)
        builder.setTicker(getString(R.string.usage_statistics))
        builder.setContentTitle(getString(R.string.usage_statistics))
        builder.setContentText(getString(R.string.usage_statistics_notification_summary))
        builder.setContentIntent(contentIntent)
        notificationManager.notify(NOTIFICATION_ID_DATA_PROFILING, builder.build())
    }

    private fun triggerActionsClick() {
        if (mainPager == null || pagerAdapter == null) return
        val position = mainPager!!.currentItem
        if (pagerAdapter!!.count == 0) return
        val tab = pagerAdapter!!.getTab(position)
        if (DirectMessagesFragment::class.java == tab.cls) {
            IntentUtils.openMessageConversation(this, null, null)
        } else if (MessagesEntriesFragment::class.java == tab.cls) {
            IntentUtils.openMessageConversation(this, null, null)
        } else if (TrendsSuggestionsFragment::class.java == tab.cls) {
            openSearchView(null)
        } else {
            startActivity(Intent(INTENT_ACTION_COMPOSE))
        }
    }

    private fun updateActionsButton() {
        if (mainPager == null || pagerAdapter == null) return
        val icon: Int
        val title: Int
        val position = mainPager!!.currentItem
        if (pagerAdapter!!.count == 0) return
        val tab = pagerAdapter!!.getTab(position)
        if (DirectMessagesFragment::class.java == tab.cls) {
            icon = R.drawable.ic_action_add
            title = R.string.new_direct_message
        } else if (MessagesEntriesFragment::class.java == tab.cls) {
            icon = R.drawable.ic_action_add
            title = R.string.new_direct_message
        } else if (TrendsSuggestionsFragment::class.java == tab.cls) {
            icon = R.drawable.ic_action_search
            title = android.R.string.search_go
        } else {
            icon = R.drawable.ic_action_status_compose
            title = R.string.compose
        }
        actionsButton.setImageResource(icon)
        actionsButton.contentDescription = getString(title)
    }

    private class AccountChangeObserver(private val mActivity: HomeActivity, handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            mActivity.notifyAccountsChanged()
            mActivity.updateUnreadCount()
        }
    }

    private class UpdateUnreadCountTask internal constructor(
            private val context: Context,
            private val readStateManager: ReadStateManager,
            private val indicator: TabPagerIndicator,
            private val tabs: Array<SupportTabSpec>) : AsyncTask<Any, UpdateUnreadCountTask.TabBadge, SparseIntArray>() {

        override fun doInBackground(vararg params: Any): SparseIntArray {
            val result = SparseIntArray()
            tabs.forEachIndexed { i, spec ->
                if (spec.type == null) {
                    publishProgress(TabBadge(i, -1))
                    return@forEachIndexed
                }
                when (spec.type) {
                    CustomTabType.HOME_TIMELINE -> {
                        val accountKeys = Utils.getAccountKeys(context, spec.args) ?:
                                DataStoreUtils.getActivatedAccountKeys(context)
                        val position = accountKeys.map {
                            val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.HOME_TIMELINE, it)
                            readStateManager.getPosition(tag)
                        }.fold(0L, Math::max)
                        val count = DataStoreUtils.getStatusesCount(context, Statuses.CONTENT_URI,
                                spec.args, position, Statuses.STATUS_TIMESTAMP, true, accountKeys)
                        result.put(i, count)
                        publishProgress(TabBadge(i, count))
                    }
                    CustomTabType.NOTIFICATIONS_TIMELINE -> {
                        val accountKeys = Utils.getAccountKeys(context, spec.args) ?:
                                DataStoreUtils.getActivatedAccountKeys(context)
                        val position = accountKeys.map {
                            val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.ACTIVITIES_ABOUT_ME, it)
                            readStateManager.getPosition(tag)
                        }.fold(0L, Math::max)
                        val count = DataStoreUtils.getInteractionsCount(context, spec.args,
                                accountKeys, position, Activities.TIMESTAMP)
                        publishProgress(TabBadge(i, count))
                        result.put(i, count)
                    }
                    else -> {
                        publishProgress(TabBadge(i, -1))
                    }
                }
            }
            return result
        }

        override fun onPostExecute(result: SparseIntArray) {
            indicator.clearBadge()
            for (i in 0 until result.size()) {
                indicator.setBadge(result.keyAt(i), result.valueAt(i))
            }
        }

        override fun onProgressUpdate(vararg values: TabBadge) {
            for (value in values) {
                indicator.setBadge(value.index, value.count)
            }
        }

        internal class TabBadge(var index: Int, var count: Int)
    }

    companion object {
        private val HOME_AS_UP_ATTRS = intArrayOf(android.support.v7.appcompat.R.attr.homeAsUpIndicator)
    }


}
