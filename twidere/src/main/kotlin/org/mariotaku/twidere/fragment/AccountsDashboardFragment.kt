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

package org.mariotaku.twidere.fragment

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.ContentObserver
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.NavigationView
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.view.SupportMenuInflater
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import kotlinx.android.synthetic.main.header_drawer_account_selector.view.*
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.convert
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.*
import org.mariotaku.twidere.adapter.RecyclerPagerAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.extension.setActivated
import org.mariotaku.twidere.fragment.AccountsDashboardFragment.AccountsInfo
import org.mariotaku.twidere.menu.AccountToggleProvider
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.view.ShapedImageView
import java.util.*

class AccountsDashboardFragment : BaseSupportFragment(), LoaderCallbacks<AccountsInfo>, OnSharedPreferenceChangeListener, OnClickListener, KeyboardShortcutCallback, NavigationView.OnNavigationItemSelectedListener {

    private val systemWindowsInsets = Rect()

    private lateinit var accountsAdapter: AccountSelectorAdapter

    private val hasNextAccountIndicator by lazy { accountsHeader.hasNextAccountIndicator }
    private val hasPrevAccountIndicator by lazy { accountsHeader.hasPrevAccountIndicator }
    private val accountsSelector by lazy { accountsHeader.otherAccountsList }
    private val navigationView by lazy { view as NavigationView }
    private val accountsHeader by lazy { navigationView.getHeaderView(0) }
    private val accountProfileBanner by lazy { accountsHeader.accountProfileBanner }
    private val floatingProfileImageSnapshot by lazy { accountsHeader.floatingProfileImageSnapshot }
    private val accountProfileImageView by lazy { accountsHeader.profileImage }
    private val accountProfileNameView by lazy { accountsHeader.name }
    private val accountProfileScreenNameView by lazy { accountsHeader.screenName }
    private val accountDashboardMenu by lazy { accountsHeader.accountDashboardMenu }
    private val profileContainer by lazy { accountsHeader.profileContainer }
    private val noAccountContainer by lazy { accountsHeader.noAccountContainer }

    private var accountActionProvider: AccountToggleProvider? = null

    private var switchAccountAnimationPlaying: Boolean = false
    private var useStarsForLikes: Boolean = false
    private var loaderInitialized: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val inflater = getLayoutInflater(savedInstanceState)
        accountsAdapter = AccountSelectorAdapter(inflater, this)
        val layoutManager = FixedLinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        layoutManager.stackFromEnd = true
        accountsSelector.adapter = accountsAdapter
        accountsSelector.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val adapter = accountsAdapter
                val pagePosition = position + positionOffset
                val pageCount = adapter.count
                val visiblePages = 1 / adapter.getPageWidth(position)
                if (pageCount < visiblePages) {
                    hasPrevAccountIndicator.alpha = 0f
                    hasNextAccountIndicator.alpha = 0f
                } else {
                    hasPrevAccountIndicator.alpha = TwidereMathUtils.clamp(pagePosition, 0f, 1f)
                    hasNextAccountIndicator.alpha = TwidereMathUtils.clamp(pageCount - (pagePosition
                            + visiblePages), 0f, 1f)
                }
            }
        })
        accountsSelector.setPageTransformer(false, AccountsSelectorTransformer)

        hasPrevAccountIndicator.alpha = 0f
        hasNextAccountIndicator.alpha = 0f

        val menuInflater = SupportMenuInflater(context)
        menuInflater.inflate(R.menu.action_dashboard_timeline_toggle, accountDashboardMenu.menu)
        accountDashboardMenu.setOnMenuItemClickListener(OnMenuItemClickListener { item ->
            if (item.groupId == AccountToggleProvider.MENU_GROUP) {
                val accounts = accountActionProvider!!.accounts
                val account = accounts[item.order]
                val newActivated = !account.activated
                accountActionProvider!!.setAccountActivated(account.key, newActivated)
                account.account.setActivated(AccountManager.get(context), newActivated)
                return@OnMenuItemClickListener true
            } else {
                when (item.itemId) {
                    R.id.compose -> {
                        val account = accountsAdapter.selectedAccount ?: return@OnMenuItemClickListener true
                        val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                        composeIntent.setClass(activity, ComposeActivity::class.java)
                        composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
                        startActivity(composeIntent)
                        return@OnMenuItemClickListener true
                    }
                }
            }
            return@OnMenuItemClickListener false
        })

        profileContainer.setOnClickListener(this)

        accountProfileBanner.setInAnimation(context, android.R.anim.fade_in)
        accountProfileBanner.setOutAnimation(context, android.R.anim.fade_out)
        accountProfileBanner.setFactory {
            inflater.inflate(R.layout.layout_account_dashboard_profile_banner,
                    accountProfileBanner, false)
        }

        navigationView.setNavigationItemSelectedListener(this)
        preferences.registerOnSharedPreferenceChangeListener(this)

        loadAccounts()

        updateSystemWindowsInsets()
    }

    override fun onStart() {
        super.onStart()
        loaderManager.restartLoader(0, null, this)
    }

    override fun onResume() {
        super.onResume()
        updateDefaultAccountState()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts_dashboard, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SETTINGS -> {
                if (data == null) return
                if (data.getBooleanExtra(EXTRA_SHOULD_RESTART, false)) {
                    Utils.restartActivity(activity)
                } else if (data.getBooleanExtra(EXTRA_SHOULD_RECREATE, false)) {
                    activity.recreate()
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
                                              keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_PREVIOUS == action || ACTION_NAVIGATION_NEXT == action
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
                                              repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.profileContainer -> {
                val account = accountsAdapter.selectedAccount ?: return
                val activity = activity
                if (account.user != null) {
                    IntentUtils.openUserProfile(activity, account.user!!, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            Referral.SELF_PROFILE)
                } else {
                    IntentUtils.openUserProfile(activity, account.key, account.key,
                            account.user.screen_name, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            Referral.SELF_PROFILE)
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<AccountsInfo> {
        return AccountsInfoLoader(activity)
    }


    override fun onLoadFinished(loader: Loader<AccountsInfo>, data: AccountsInfo) {
        updateAccountProviderData(data)
    }

    private fun updateAccountProviderData(data: AccountsInfo) {
        val menu = accountDashboardMenu.menu
        accountActionProvider = MenuItemCompat.getActionProvider(menu.findItem(R.id.select_account)) as AccountToggleProvider
        val accounts = data.accounts
        if (accounts.isNotEmpty()) {
            noAccountContainer.visibility = View.GONE
            profileContainer.visibility = View.VISIBLE
        } else {
            noAccountContainer.visibility = View.VISIBLE
            profileContainer.visibility = View.INVISIBLE
        }
        useStarsForLikes = preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)
        accountsAdapter.accounts = accounts
        val defaultKey = preferences.getString(KEY_DEFAULT_ACCOUNT_KEY, null)?.convert(UserKey::valueOf)
                ?: accounts.firstOrNull { it.activated }?.key
        val defaultAccount = accounts.firstOrNull { it.key.maybeEquals(defaultKey) }
        accountsAdapter.selectedAccount = defaultAccount

        if (accountActionProvider != null) {
            accountActionProvider!!.isExclusive = false
            accountActionProvider!!.accounts = accounts
        }
        updateAccountActions()
        val currentAccount = accountsAdapter.selectedAccount
        if (currentAccount != null) {
            displayAccountBanner(currentAccount)
            displayCurrentAccount(null)
        }
        updateDefaultAccountState()

        if (data.draftsCount > 0) {
            navigationView.menu.findItem(R.id.drafts).title = "${getString(R.string.title_drafts)} (${data.draftsCount})"
        } else {
            navigationView.menu.findItem(R.id.drafts).title = getString(R.string.title_drafts)
        }
    }

    override fun onLoaderReset(loader: Loader<AccountsInfo>) {
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (KEY_DEFAULT_ACCOUNT_KEY == key) {
            updateDefaultAccountState()
        }
    }

    override fun fitSystemWindows(insets: Rect) {
        systemWindowsInsets.set(insets)
        updateSystemWindowsInsets()
    }

    private fun updateSystemWindowsInsets() {
    }

    fun loadAccounts() {
        if (!loaderInitialized) {
            loaderInitialized = true
            loaderManager.initLoader(0, null, this)
        } else {
            loaderManager.restartLoader(0, null, this)
        }
    }

    internal fun updateAccountActions() {
        val activity = activity as HomeActivity
        val tabs = activity.tabs
        val account = accountsAdapter.selectedAccount ?: return
        var hasDmTab = false
        var hasInteractionsTab = false
        for (tab in tabs) {
            when (tab.type) {
                CustomTabType.DIRECT_MESSAGES -> {
                    if (!hasDmTab) {
                        hasDmTab = hasAccountInTab(tab, account.key, account.activated)
                    }
                }
                CustomTabType.NOTIFICATIONS_TIMELINE -> {
                    if (!hasInteractionsTab) {
                        hasInteractionsTab = hasAccountInTab(tab, account.key, account.activated)
                    }
                }
            }
        }
        val menu = navigationView.menu
        menu.setItemAvailability(R.id.interactions, !hasInteractionsTab)
        menu.setItemAvailability(R.id.messages, !hasDmTab)

        menu.setItemAvailability(R.id.favorites, useStarsForLikes)
        menu.setItemAvailability(R.id.likes, !useStarsForLikes)
        var hasLists = false
        var hasGroups = false
        var hasPublicTimeline = false
        when (account.type) {
            AccountType.TWITTER -> {
                hasLists = true
            }
            AccountType.STATUSNET -> {
                hasGroups = true
            }
            AccountType.FANFOU -> {
                hasPublicTimeline = true
            }
        }
        MenuUtils.setItemAvailability(menu, R.id.groups, hasGroups)
        MenuUtils.setItemAvailability(menu, R.id.lists, hasLists)
        MenuUtils.setItemAvailability(menu, R.id.public_timeline, hasPublicTimeline)
    }

    private fun hasAccountInTab(tab: SupportTabSpec, accountId: UserKey, isActivated: Boolean): Boolean {
        if (tab.args == null) return false
        val accountKeys = Utils.getAccountKeys(context, tab.args) ?: return isActivated
        return accountKeys.contains(accountId)
    }

    private fun closeAccountsDrawer() {
        val activity = activity as? HomeActivity ?: return
        activity.closeAccountsDrawer()
    }

    private fun getLocationOnScreen(view: View, rectF: RectF) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        rectF.set(location[0].toFloat(), location[1].toFloat(), (location[0] + view.width).toFloat(), (location[1] + view.height).toFloat())
    }

    private fun onAccountSelected(holder: AccountProfileImageViewHolder, account: AccountDetails) {
        if (switchAccountAnimationPlaying) return
        val snapshotView = floatingProfileImageSnapshot
        val profileImageView = accountProfileImageView
        val clickedImageView = holder.iconView

        // Reset snapshot view position
        snapshotView.pivotX = 0f
        snapshotView.pivotY = 0f
        snapshotView.translationX = 0f
        snapshotView.translationY = 0f

        val matrix = Matrix()
        val sourceBounds = RectF()
        val destBounds = RectF()
        val snapshotBounds = RectF()
        getLocationOnScreen(clickedImageView, sourceBounds)
        getLocationOnScreen(profileImageView, destBounds)
        getLocationOnScreen(snapshotView, snapshotBounds)
        val finalScale = destBounds.width() / sourceBounds.width()
        val snapshotBitmap = TransitionUtils.createViewBitmap(clickedImageView, matrix,
                RectF(0f, 0f, sourceBounds.width(), sourceBounds.height()))
        val lp = snapshotView.layoutParams
        lp.width = clickedImageView.width
        lp.height = clickedImageView.height
        snapshotView.layoutParams = lp
        // Copied from MaterialNavigationDrawer: https://github.com/madcyph3r/AdvancedMaterialDrawer/
        val set = AnimatorSet()
        set.play(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_X, sourceBounds.left - snapshotBounds.left, destBounds.left - snapshotBounds.left))
                .with(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_Y, sourceBounds.top - snapshotBounds.top, destBounds.top - snapshotBounds.top))
                .with(ObjectAnimator.ofFloat<View>(snapshotView, View.SCALE_X, 1f, finalScale))
                .with(ObjectAnimator.ofFloat<View>(snapshotView, View.SCALE_Y, 1f, finalScale))
                .with(ObjectAnimator.ofFloat<View>(profileImageView, View.ALPHA, 1f, 0f))
                .with(ObjectAnimator.ofFloat<View>(clickedImageView, View.SCALE_X, 0f, 1f))
                .with(ObjectAnimator.ofFloat<View>(clickedImageView, View.SCALE_Y, 0f, 1f))
        val animationTransition: Long = 400
        set.duration = animationTransition
        set.interpolator = DecelerateInterpolator()
        set.addListener(object : AnimatorListener {

            private var clickedDrawable: Drawable? = null
            private var clickedColors: IntArray? = null

            override fun onAnimationStart(animation: Animator) {
                snapshotView.visibility = View.VISIBLE
                snapshotView.setImageBitmap(snapshotBitmap)
                val profileDrawable = profileImageView.drawable
                clickedDrawable = clickedImageView.drawable
                clickedColors = clickedImageView.borderColors
                val oldSelectedAccount = accountsAdapter.selectedAccount ?: return
                mediaLoader.displayDashboardProfileImage(clickedImageView,
                        oldSelectedAccount, profileDrawable)
                clickedImageView.setBorderColors(*profileImageView.borderColors)

                displayAccountBanner(account)

                switchAccountAnimationPlaying = true
            }

            override fun onAnimationEnd(animation: Animator) {
                finishAnimation()
            }

            override fun onAnimationCancel(animation: Animator) {
                finishAnimation()
            }

            override fun onAnimationRepeat(animation: Animator) {

            }

            private fun finishAnimation() {
                preferences.edit()
                        .putString(KEY_DEFAULT_ACCOUNT_KEY, account.key.toString())
                        .apply()
                accountsAdapter.selectedAccount = account
                updateAccountActions()
                displayCurrentAccount(clickedDrawable)
                snapshotView.visibility = View.INVISIBLE
                snapshotView.setImageDrawable(null)
                profileImageView.setImageDrawable(clickedDrawable)
                profileImageView.setBorderColors(*clickedColors!!)
                profileImageView.alpha = 1f
                clickedImageView.scaleX = 1f
                clickedImageView.scaleY = 1f
                clickedImageView.alpha = 1f
                switchAccountAnimationPlaying = false
            }
        })
        set.start()

    }

    private fun displayAccountBanner(account: AccountDetails) {
        val bannerWidth = accountProfileBanner.width
        val res = resources
        val defWidth = res.displayMetrics.widthPixels
        val width = if (bannerWidth > 0) bannerWidth else defWidth
        val bannerView = accountProfileBanner.nextView as ImageView
        if (bannerView.drawable == null || account != bannerView.tag) {
            mediaLoader.displayProfileBanner(bannerView, account, width)
            bannerView.tag = account
        } else {
            mediaLoader.cancelDisplayTask(bannerView)
        }
    }

    private fun displayCurrentAccount(profileImageSnapshot: Drawable?) {
        val account = accountsAdapter.selectedAccount ?: return
        accountProfileNameView.text = account.user.name
        accountProfileScreenNameView.text = String.format("@%s", account.user.screen_name)
        mediaLoader.displayDashboardProfileImage(accountProfileImageView, account,
                profileImageSnapshot)
        accountProfileImageView.setBorderColors(account.color)
        accountProfileBanner.showNext()
    }

    private fun updateDefaultAccountState() {
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val account = accountsAdapter.selectedAccount ?: return false
        when (item.itemId) {
            R.id.search -> {
                val intent = Intent(activity, QuickSearchBarActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
                startActivity(intent)
                closeAccountsDrawer()
            }
            R.id.compose -> {
                val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                composeIntent.setClass(activity, ComposeActivity::class.java)
                composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
                startActivity(composeIntent)
            }
            R.id.likes, R.id.favorites -> {
                IntentUtils.openUserFavorites(activity, account.key, account.key,
                        account.user.screen_name)
            }
            R.id.lists -> {
                IntentUtils.openUserLists(activity, account.key,
                        account.key, account.user.screen_name)
            }
            R.id.groups -> {
                IntentUtils.openUserGroups(activity, account.key,
                        account.key, account.user.screen_name)
            }
            R.id.public_timeline -> {
                IntentUtils.openPublicTimeline(activity, account.key)
            }
            R.id.messages -> {
                IntentUtils.openDirectMessages(activity, account.key)
            }
            R.id.interactions -> {
                IntentUtils.openInteractions(activity, account.key)
            }
            R.id.edit -> {
                IntentUtils.openProfileEditor(activity, account.key)
            }
            R.id.accounts -> {
                IntentUtils.openAccountsManager(activity)
                closeAccountsDrawer()
            }
            R.id.drafts -> {
                IntentUtils.openDrafts(activity)
                closeAccountsDrawer()
            }
            R.id.filters -> {
                IntentUtils.openFilters(activity)
                closeAccountsDrawer()
            }
            R.id.premium_features -> {
                val intent = Intent(activity, PremiumDashboardActivity::class.java)
                startActivity(intent)
                closeAccountsDrawer()
            }
            R.id.settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivityForResult(intent, REQUEST_SETTINGS)
                closeAccountsDrawer()
            }
        }
        return false
    }

    fun setStatusBarHeight(height: Int) {
        val top = Utils.getInsetsTopWithoutActionBarHeight(activity, height)
        profileContainer.setPadding(0, top, 0, 0)
    }

    fun shouldDisableDrawerSlide(e: MotionEvent): Boolean {
        if (accountsSelector == null) return false
        return TwidereViewUtils.hitView(e.rawX, e.rawY, accountsSelector)
    }

    internal class AccountSpaceViewHolder(itemView: View) : RecyclerPagerAdapter.ViewHolder(itemView)

    internal class AccountProfileImageViewHolder(val adapter: AccountSelectorAdapter, itemView: View) : RecyclerPagerAdapter.ViewHolder(itemView), OnClickListener {

        val iconView: ShapedImageView

        init {
            itemView.setOnClickListener(this)
            iconView = itemView.findViewById(android.R.id.icon) as ShapedImageView
        }

        override fun onClick(v: View) {
            adapter.dispatchItemSelected(this)
        }

    }

    internal class AccountSelectorAdapter(
            private val inflater: LayoutInflater,
            private val fragment: AccountsDashboardFragment
    ) : RecyclerPagerAdapter() {
        private val mediaLoader: MediaLoaderWrapper

        var accounts: Array<AccountDetails>? = null
            set(value) {
                if (value != null) {
                    val previousAccounts = accounts
                    if (previousAccounts != null) {
                        val tmpList = arrayListOf(*value)
                        val tmpResult = ArrayList<AccountDetails>()
                        previousAccounts.forEach { previousAccount ->
                            val prefIndexOfTmp = tmpList.indexOfFirst { previousAccount == it }
                            if (prefIndexOfTmp >= 0) {
                                tmpResult.add(tmpList.removeAt(prefIndexOfTmp))
                            }
                        }
                        tmpResult.addAll(tmpList)
                        field = tmpResult.toTypedArray()
                    } else {
                        field = value
                    }
                } else {
                    field = null
                }
                notifyPagesChanged(invalidateCache = true)
            }

        init {
            mediaLoader = fragment.mediaLoader
        }

        fun getAdapterAccount(position: Int): AccountDetails? {
            return accounts?.getOrNull(position - accountStart + 1)
        }

        var selectedAccount: AccountDetails?
            get() {
                return accounts?.firstOrNull()
            }
            set(account) {
                val from = account ?: return
                val to = selectedAccount ?: return
                swap(from, to)
            }

        val ITEM_VIEW_TYPE_SPACE = 1
        val ITEM_VIEW_TYPE_ICON = 2

        override fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): ViewHolder {
            when (itemViewType) {
                ITEM_VIEW_TYPE_SPACE -> {
                    val view = inflater.inflate(R.layout.adapter_item_dashboard_account_space, container, false)
                    return AccountSpaceViewHolder(view)
                }
                ITEM_VIEW_TYPE_ICON -> {
                    val view = inflater.inflate(R.layout.adapter_item_dashboard_account, container, false)
                    return AccountProfileImageViewHolder(this, view)
                }
            }
            throw UnsupportedOperationException()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, itemViewType: Int) {
            when (itemViewType) {
                ITEM_VIEW_TYPE_ICON -> {
                    val account = getAdapterAccount(position)!!
                    holder as AccountProfileImageViewHolder
                    holder.iconView.setBorderColor(account.color)
                    if (holder.iconView.tag != account && holder.iconView.drawable == null) {
                        mediaLoader.displayDashboardProfileImage(holder.iconView, account, null)
                    }
                    holder.iconView.tag = account
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position < accountStart) {
                return ITEM_VIEW_TYPE_SPACE
            }
            return ITEM_VIEW_TYPE_ICON
        }

        override fun getCount(): Int {
            return Math.max(3, accountsCount)
        }

        val accountStart: Int
            get() = Math.max(0, 3 - accountsCount)

        val accountsCount: Int
            get() {
                val accounts = this.accounts ?: return 0
                return Math.max(0, accounts.size - 1)
            }

        override fun getPageWidth(position: Int): Float {
            return 1f / AccountsSelectorTransformer.selectorAccountsCount
        }

        fun dispatchItemSelected(holder: AccountProfileImageViewHolder) {
            fragment.onAccountSelected(holder, getAdapterAccount(holder.position)!!)
        }

        private fun swap(from: AccountDetails, to: AccountDetails) {
            val accounts = accounts ?: return
            val fromIdx = accounts.indexOfFirst { it == from }
            val toIdx = accounts.indexOfFirst { it == to }
            if (fromIdx < 0 || toIdx < 0) return
            val temp = accounts[toIdx]
            accounts[toIdx] = accounts[fromIdx]
            accounts[fromIdx] = temp
            notifyPagesChanged(invalidateCache = false)
        }

    }

    data class AccountsInfo(
            val accounts: Array<AccountDetails>,
            val draftsCount: Int
    )

    class AccountsInfoLoader(context: Context) : AsyncTaskLoader<AccountsInfo>(context) {
        private var contentObserver: ContentObserver? = null
        private var accountListener: OnAccountsUpdateListener? = null

        private var firstLoad: Boolean

        init {
            firstLoad = true
        }

        override fun loadInBackground(): AccountsInfo {
            val accounts = AccountUtils.getAllAccountDetails(AccountManager.get(context), true)
            val draftsCount = DataStoreUtils.queryCount(context, Drafts.CONTENT_URI_UNSENT, null, null)
            return AccountsInfo(accounts, draftsCount)
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        override fun onReset() {
            super.onReset()

            // Ensure the loader is stopped
            onStopLoading()

            // Stop monitoring for changes.
            if (contentObserver != null) {
                context.contentResolver.unregisterContentObserver(contentObserver)
                contentObserver = null
            }
            if (accountListener != null) {
                AccountManager.get(context).removeOnAccountsUpdatedListenerSafe(accountListener!!)
                accountListener = null
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        override fun onStartLoading() {

            // Start watching for changes in the app data.
            if (contentObserver == null) {
                contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        onContentChanged()
                    }

                    override fun onChange(selfChange: Boolean, uri: Uri?) {
                        onContentChanged()
                    }
                }
                context.contentResolver.registerContentObserver(Drafts.CONTENT_URI, true, contentObserver)
            }
            if (accountListener == null) {
                accountListener = OnAccountsUpdateListener {
                    onContentChanged()
                }
                AccountManager.get(context).addOnAccountsUpdatedListenerSafe(accountListener!!, updateImmediately = false)
            }

            if (takeContentChanged() || firstLoad) {
                firstLoad = false
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad()
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        override fun onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad()
        }

    }

    object AccountsSelectorTransformer : ViewPager.PageTransformer {
        const internal val selectorAccountsCount: Int = 3

        override fun transformPage(page: View, position: Float) {
            if (position < 0) {
                page.alpha = 1 + position * selectorAccountsCount
            } else if (position > (selectorAccountsCount - 1f) / selectorAccountsCount) {
                page.alpha = (1 - position) * selectorAccountsCount
            } else {
                page.alpha = 1f
            }
        }

    }

}