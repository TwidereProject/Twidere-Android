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
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.ContentObserver
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.ActionMenuView.OnMenuItemClickListener
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.FixedAsyncTaskLoader
import androidx.loader.content.Loader
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.header_drawer_account_selector.view.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.*
import org.mariotaku.twidere.Constants.EXTRA_FEATURES_NOTICE_VERSION
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.activity.HomeActivity
import org.mariotaku.twidere.activity.PremiumDashboardActivity
import org.mariotaku.twidere.activity.QuickSearchBarActivity
import org.mariotaku.twidere.adapter.AccountSelectorAdapter
import org.mariotaku.twidere.adapter.RecyclerPagerAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.ProfileImageSize
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.constant.extraFeaturesNoticeVersionKey
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.loadProfileBanner
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.setActivated
import org.mariotaku.twidere.extension.queryCount
import org.mariotaku.twidere.fragment.AccountsDashboardFragment.AccountsInfo
import org.mariotaku.twidere.graphic.BadgeDrawable
import org.mariotaku.twidere.menu.AccountToggleProvider
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.view.holder.AccountProfileImageViewHolder
import org.mariotaku.twidere.view.transformer.AccountsSelectorTransformer
import java.lang.ref.WeakReference

class AccountsDashboardFragment : BaseFragment(), LoaderCallbacks<AccountsInfo>,
        OnSharedPreferenceChangeListener, OnClickListener, KeyboardShortcutCallback,
        NavigationView.OnNavigationItemSelectedListener, AccountSelectorAdapter.Listener {

    private val systemWindowsInsets = Rect()

    private lateinit var accountsAdapter: AccountSelectorAdapter

    private val navigationView by lazy { view as NavigationView }
    private val accountsHeader by lazy { navigationView.getHeaderView(0) }
    private val hasNextAccountIndicator by lazy { accountsHeader.hasNextAccountIndicator }
    private val hasPrevAccountIndicator by lazy { accountsHeader.hasPrevAccountIndicator }
    private val accountsSelector by lazy { accountsHeader.otherAccountsList }
    private val accountProfileBanner by lazy { accountsHeader.accountProfileBanner }
    private val floatingProfileImageSnapshot by lazy { accountsHeader.floatingProfileImageSnapshot }
    private val accountProfileImageView by lazy { accountsHeader.profileImage }
    private val accountProfileNameView by lazy { accountsHeader.name }
    private val accountProfileScreenNameView by lazy { accountsHeader.screenName }
    private val accountDashboardMenu by lazy { accountsHeader.accountDashboardMenu }
    private val profileContainer by lazy { accountsHeader.profileContainer }
    private val noAccountContainer by lazy { accountsHeader.noAccountContainer }

    private lateinit var accountActionProvider: AccountToggleProvider

    private var switchAccountAnimationPlaying: Boolean = false
    private var useStarsForLikes: Boolean = false
    private var loaderInitialized: Boolean = false

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        accountsAdapter = AccountSelectorAdapter(layoutInflater, preferences, requestManager).also {
            it.listener = this
        }
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
                    hasPrevAccountIndicator.alpha = pagePosition.coerceIn(0f, 1f)
                    hasNextAccountIndicator.alpha = (pageCount - (pagePosition + visiblePages))
                            .coerceIn(0f, 1f)
                }
            }
        })
        accountsSelector.setPageTransformer(false, AccountsSelectorTransformer)

        hasPrevAccountIndicator.alpha = 0f
        hasNextAccountIndicator.alpha = 0f

        val profileImageStyle = preferences[profileImageStyleKey]
        floatingProfileImageSnapshot.style = profileImageStyle
        accountProfileImageView.style = profileImageStyle

        SupportMenuInflater(context).inflate(R.menu.action_dashboard_timeline_toggle,
                accountDashboardMenu.menu)
        accountActionProvider = MenuItemCompat.getActionProvider(
                accountDashboardMenu.menu.findItem(R.id.select_account)) as AccountToggleProvider
        accountDashboardMenu.setOnMenuItemClickListener(OnMenuItemClickListener { item ->
            if (item.groupId == AccountToggleProvider.MENU_GROUP) {
                val accounts = accountActionProvider.accounts
                val account = accounts[item.order]
                val newActivated = !account.activated
                accountActionProvider.setAccountActivated(account.key, newActivated)
                account.account.setActivated(AccountManager.get(context), newActivated)
                return@OnMenuItemClickListener true
            } else {
                when (item.itemId) {
                    R.id.compose -> {
                        val account = accountsAdapter.selectedAccount ?: return@OnMenuItemClickListener true
                        val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                        activity?.let { composeIntent.setClass(it, ComposeActivity::class.java) }
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
            layoutInflater.inflate(R.layout.layout_account_dashboard_profile_banner,
                    accountProfileBanner, false)
        }

        navigationView.setNavigationItemSelectedListener(this)
        preferences.registerOnSharedPreferenceChangeListener(this)

        updateSystemWindowsInsets()
    }

    override fun onStart() {
        super.onStart()
        loadAccounts()
    }

    override fun onResume() {
        super.onResume()
        updateDefaultAccountState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_accounts_dashboard, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SETTINGS -> {
                if (data == null) return
                if (data.getBooleanExtra(EXTRA_SHOULD_RESTART, false)) {
                    Utils.restartActivity(activity)
                } else if (data.getBooleanExtra(EXTRA_SHOULD_RECREATE, false)) {
                    activity?.recreate()
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
                    activity?.let {
                        IntentUtils.openUserProfile(it, account.user!!,
                                preferences[newDocumentApiKey], null)
                    }
                } else {
                    activity?.let {
                        IntentUtils.openUserProfile(it, account.key, account.key,
                                account.user.screen_name, null, preferences[newDocumentApiKey],
                                null)
                    }
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<AccountsInfo> {
        return AccountsInfoLoader(requireActivity(), accountsAdapter.accounts == null)
    }


    override fun onLoadFinished(loader: Loader<AccountsInfo>, data: AccountsInfo) {
        if (context == null || isDetached || (activity?.isFinishing != false)) return
        updateAccountProviderData(data)
    }

    override fun onLoaderReset(loader: Loader<AccountsInfo>) {
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (KEY_DEFAULT_ACCOUNT_KEY == key) {
            updateDefaultAccountState()
        }
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        view?.findViewById<View?>(com.google.android.material.R.id.design_navigation_view)?.
                setPadding(0, 0, 0, insets.bottom)
        systemWindowsInsets.set(insets)
        updateSystemWindowsInsets()
    }

    fun loadAccounts() {
        if (!loaderInitialized) {
            loaderInitialized = true
            LoaderManager.getInstance(this).initLoader(0, null, this)
        } else {
            LoaderManager.getInstance(this).restartLoader(0, null, this)
        }
    }

    private fun updateAccountProviderData(data: AccountsInfo) {
        val accounts = data.accounts
        if (accounts.isNotEmpty()) {
            noAccountContainer.visibility = View.GONE
            profileContainer.visibility = View.VISIBLE
        } else {
            noAccountContainer.visibility = View.VISIBLE
            profileContainer.visibility = View.INVISIBLE
        }
        useStarsForLikes = preferences[iWantMyStarsBackKey]
        accountsAdapter.accounts = accounts
        val defaultKey = preferences.getString(KEY_DEFAULT_ACCOUNT_KEY, null)?.let(UserKey::valueOf)
                ?: accounts.firstOrNull { it.activated }?.key
        val defaultAccount = accounts.firstOrNull { it.key.maybeEquals(defaultKey) }
        accountsAdapter.selectedAccount = defaultAccount

        accountActionProvider.isExclusive = false
        accountActionProvider.accounts = accounts
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

    private fun updateSystemWindowsInsets() {
        profileContainer?.setPadding(0, systemWindowsInsets.top, 0, 0)
    }

    internal fun updateAccountActions() {
        val activity = activity as? HomeActivity ?: return
        val tabs = activity.tabs
        val account = accountsAdapter.selectedAccount ?: return
        var hasDmTab = false
        var hasInteractionsTab = false
        var hasPublicTimelineTab = false
        var hasNetworkPublicTimelineTab = false
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
                CustomTabType.PUBLIC_TIMELINE -> {
                    if (!hasPublicTimelineTab) {
                        hasPublicTimelineTab = hasAccountInTab(tab, account.key, true)
                    }
                }
                CustomTabType.NETWORK_PUBLIC_TIMELINE -> {
                    if (!hasNetworkPublicTimelineTab) {
                        hasNetworkPublicTimelineTab = hasAccountInTab(tab, account.key, true)
                    }
                }
            }
        }
        val menu = navigationView.menu
        menu.setItemAvailability(R.id.interactions, !hasInteractionsTab)
        menu.setItemAvailability(R.id.favorites, useStarsForLikes)
        menu.setItemAvailability(R.id.likes, !useStarsForLikes)
        menu.setItemAvailability(R.id.premium_features, extraFeaturesService.isSupported())
        if (preferences[extraFeaturesNoticeVersionKey] < EXTRA_FEATURES_NOTICE_VERSION) {
            val icon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_action_infinity) }
            val color = context?.let { ContextCompat.getColor(it, R.color.material_red) }
            val size = resources.getDimensionPixelSize(R.dimen.element_spacing_msmall)
            if (icon != null && color != null) {
                menu.setItemIcon(R.id.premium_features, BadgeDrawable(icon, color, size))
            }
        } else {
            menu.setItemIcon(R.id.premium_features, R.drawable.ic_action_infinity)
        }
        var hasLists = false
        var hasGroups = false
        var hasPublicTimeline = false
        var hasNetworkPublicTimeline = false
        var hasDirectMessages = false
        when (account.type) {
            AccountType.TWITTER -> {
                hasDirectMessages = !hasDmTab
                hasLists = true
            }
            AccountType.STATUSNET -> {
                hasDirectMessages = !hasDmTab
                hasGroups = true
                hasPublicTimeline = !hasPublicTimelineTab
                hasNetworkPublicTimeline = !hasNetworkPublicTimelineTab
            }
            AccountType.FANFOU -> {
                hasDirectMessages = !hasDmTab
                hasPublicTimeline = !hasPublicTimelineTab
            }
            AccountType.MASTODON -> {
                hasPublicTimeline = !hasPublicTimelineTab
                hasNetworkPublicTimeline = !hasNetworkPublicTimelineTab
            }
        }
        menu.setItemAvailability(R.id.messages, hasDirectMessages)
        menu.setItemAvailability(R.id.groups, hasGroups)
        menu.setItemAvailability(R.id.lists, hasLists)
        menu.setItemAvailability(R.id.public_timeline, hasPublicTimeline)
        menu.setItemAvailability(R.id.network_public_timeline, hasNetworkPublicTimeline)
    }

    private fun hasAccountInTab(tab: SupportTabSpec, accountKey: UserKey, isActivated: Boolean): Boolean {
        if (tab.args == null) return false
        val accountKeys = context?.let { Utils.getAccountKeys(it, tab.args) } ?: return isActivated
        return accountKey in accountKeys
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

    override fun onAccountSelected(holder: AccountProfileImageViewHolder, details: AccountDetails) {
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
                if (context == null || isDetached || (activity?.isFinishing != false)) return
                snapshotView.visibility = View.VISIBLE
                snapshotView.setImageBitmap(snapshotBitmap)
                val profileDrawable = profileImageView.drawable
                clickedDrawable = clickedImageView.drawable
                //TODO complete border color
                clickedColors = clickedImageView.borderColors
                val oldSelectedAccount = accountsAdapter.selectedAccount ?: return
                val profileImageStyle = preferences[profileImageStyleKey]
                requestManager.loadProfileImage(context!!, oldSelectedAccount,
                        profileImageStyle, clickedImageView.cornerRadius, clickedImageView.cornerRadiusRatio)
                        .into(clickedImageView).onLoadStarted(profileDrawable)
                //TODO complete border color
                clickedImageView.setBorderColors(*profileImageView.borderColors)

                displayAccountBanner(details)

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
                        .putString(KEY_DEFAULT_ACCOUNT_KEY, details.key.toString())
                        .apply()
                accountsAdapter.selectedAccount = details
                updateAccountActions()
                displayCurrentAccount(clickedDrawable)
                snapshotView.visibility = View.INVISIBLE
                snapshotView.setImageDrawable(null)
                profileImageView.setImageDrawable(clickedDrawable)
                //TODO complete border color
                //profileImageView.setBorderColors(*clickedColors!!)
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
        if (context == null || isDetached || (activity?.isFinishing != false)) return
        val bannerWidth = accountProfileBanner.width
        val res = resources
        val defWidth = res.displayMetrics.widthPixels
        val width = if (bannerWidth > 0) bannerWidth else defWidth
        val bannerView = accountProfileBanner.nextView as ImageView
        val user = account.user
        val fallbackBanner = when {
            user.link_color != 0 -> {
                ColorDrawable(user.link_color)
            }
            user.account_color != 0 -> {
                ColorDrawable(user.account_color)
            }
            else -> {
                ColorDrawable(Chameleon.getOverrideTheme(requireActivity(), activity).colorPrimary)
            }
        }

        requestManager.loadProfileBanner(requireContext(), account.user, width).fallback(fallbackBanner)
                .into(bannerView)
    }

    private fun displayCurrentAccount(profileImageSnapshot: Drawable?) {
        if (context == null || isDetached || (activity?.isFinishing != false)) return
        val account = accountsAdapter.selectedAccount ?: return
        accountProfileNameView.spannable = account.user.name
        val showType = accountsAdapter.accounts?.groupBy { it.type }?.count()?.let {
            it > 1
        } ?: false
        accountProfileScreenNameView.spannable = if (account.type == AccountType.MASTODON || account.type == AccountType.STATUSNET) {
            account.account.name
        } else {
            "${if (showType) account.type else ""}@${account.user.screen_name}"
        }
        requestManager.loadProfileImage(requireContext(), account, preferences[profileImageStyleKey],
                accountProfileImageView.cornerRadius, accountProfileImageView.cornerRadiusRatio,
                ProfileImageSize.REASONABLY_SMALL).placeholder(profileImageSnapshot).into(accountProfileImageView)
        //TODO complete border color
        accountProfileImageView.setBorderColors(account.color)
        accountProfileBanner.showNext()
    }

    private fun updateDefaultAccountState() {
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val account = accountsAdapter.selectedAccount ?: return false
        val currentActivity = activity ?: return false
        when (item.itemId) {
            R.id.search -> {
                val intent = Intent(currentActivity, QuickSearchBarActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
                startActivity(intent)
                closeAccountsDrawer()
            }
            R.id.compose -> {
                val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                composeIntent.setClass(currentActivity, ComposeActivity::class.java)
                composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
                startActivity(composeIntent)
            }
            R.id.likes, R.id.favorites -> {
                IntentUtils.openUserFavorites(currentActivity, account.key, account.key,
                        account.user.screen_name)
            }
            R.id.lists -> {
                IntentUtils.openUserLists(currentActivity, account.key,
                        account.key, account.user.screen_name)
            }
            R.id.groups -> {
                IntentUtils.openUserGroups(currentActivity, account.key,
                        account.key, account.user.screen_name)
            }
            R.id.public_timeline -> {
                IntentUtils.openPublicTimeline(currentActivity, account.key)
            }
            R.id.network_public_timeline -> {
                IntentUtils.openNetworkPublicTimeline(currentActivity, account.key)
            }
            R.id.messages -> {
                IntentUtils.openDirectMessages(currentActivity, account.key)
            }
            R.id.interactions -> {
                IntentUtils.openInteractions(currentActivity, account.key)
            }
            R.id.edit -> {
                IntentUtils.openProfileEditor(currentActivity, account.key)
            }
            R.id.accounts -> {
                IntentUtils.openAccountsManager(currentActivity)
                closeAccountsDrawer()
            }
            R.id.drafts -> {
                IntentUtils.openDrafts(currentActivity)
                closeAccountsDrawer()
            }
            R.id.filters -> {
                IntentUtils.openFilters(currentActivity)
                closeAccountsDrawer()
            }
            R.id.premium_features -> {
                val intent = Intent(currentActivity, PremiumDashboardActivity::class.java)
                startActivity(intent)
                preferences[extraFeaturesNoticeVersionKey] = EXTRA_FEATURES_NOTICE_VERSION
                closeAccountsDrawer()
            }
            R.id.settings -> {
                val intent = IntentUtils.settings()
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivityForResult(intent, REQUEST_SETTINGS)
                closeAccountsDrawer()
            }
        }
        return false
    }

    fun setStatusBarHeight(height: Int) {
        val top = activity?.let { Utils.getInsetsTopWithoutActionBarHeight(it, height) }
        if (top != null) {
            profileContainer.setPadding(0, top, 0, 0)
        }
    }

    fun shouldDisableDrawerSlide(e: MotionEvent): Boolean {
        if (accountsSelector == null) return false
        return TwidereViewUtils.hitView(e, accountsSelector)
    }

    internal class AccountSpaceViewHolder(itemView: View) : RecyclerPagerAdapter.ViewHolder(itemView)

    data class AccountsInfo(
            val accounts: Array<AccountDetails>,
            val draftsCount: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AccountsInfo

            if (!accounts.contentEquals(other.accounts)) return false
            if (draftsCount != other.draftsCount) return false

            return true
        }

        override fun hashCode(): Int {
            var result = accounts.contentHashCode()
            result = 31 * result + draftsCount
            return result
        }
    }

    class AccountsInfoLoader(
            context: Context,
            val firsSyncLoad: Boolean
    ) : FixedAsyncTaskLoader<AccountsInfo>(context) {

        private var contentObserver: ContentObserver? = null
            set(value) {
                field?.let {
                    context.contentResolver.unregisterContentObserver(it)
                }
                if (value != null) {
                    context.contentResolver.registerContentObserver(Drafts.CONTENT_URI, true, value)
                }
            }
        private var accountListener: OnAccountsUpdateListener? = null
            set(value) {
                val am = AccountManager.get(context)
                field?.let {
                    am.removeOnAccountsUpdatedListenerSafe(it)
                }
                if (value != null) {
                    am.addOnAccountsUpdatedListenerSafe(value, updateImmediately = true)
                }
            }

        private var firstLoad: Boolean

        init {
            firstLoad = true
        }

        override fun loadInBackground(): AccountsInfo {
            return loadAccountsInfo(true)
        }

        override fun onForceLoad() {
            if (firsSyncLoad && firstLoad) {
                deliverResult(loadAccountsInfo(false))
                return
            }
            super.onForceLoad()
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        override fun onReset() {
            super.onReset()

            // Ensure the loader is stopped
            onStopLoading()

            // Stop monitoring for changes.
            contentObserver = null
            accountListener = null
        }

        /**
         * Handles a request to start the Loader.
         */
        override fun onStartLoading() {
            val weakLoader = WeakReference(this)
            // Start watching for changes in the app data.
            if (contentObserver == null) {
                contentObserver = object : ContentObserver(null) {
                    override fun onChange(selfChange: Boolean) {
                        weakLoader.get()?.onContentChanged()
                    }

                    override fun onChange(selfChange: Boolean, uri: Uri?) {
                        weakLoader.get()?.onContentChanged()
                    }
                }
            }
            if (accountListener == null) {
                accountListener = OnAccountsUpdateListener {
                    weakLoader.get()?.onContentChanged()
                }
            }

            if (takeContentChanged() || firstLoad) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad()
                firstLoad = false
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        override fun onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad()
        }

        private fun loadAccountsInfo(loadFromDb: Boolean): AccountsInfo {
            val accounts = AccountUtils.getAllAccountDetails(AccountManager.get(context), true)
            val draftsCount = if (loadFromDb) {
                context.contentResolver.queryCount(Drafts.CONTENT_URI_UNSENT, null,
                        null)
            } else {
                -1
            }
            return AccountsInfo(accounts, draftsCount)
        }
    }

}