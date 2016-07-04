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

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.view.SupportMenuInflater
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewSwitcher
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.ktextension.setMenuItemIcon
import org.mariotaku.ktextension.setMenuItemTitle
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.*
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.menu.AccountToggleProvider
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver
import org.mariotaku.twidere.view.ShapedImageView
import java.util.*

class AccountsDashboardFragment : BaseSupportFragment(), LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener, OnClickListener, KeyboardShortcutCallback, NavigationView.OnNavigationItemSelectedListener {

    private val mSystemWindowsInsets = Rect()
    private var mResolver: ContentResolver? = null

    private var mAccountsAdapter: AccountSelectorAdapter? = null

    private var mNavigationView: NavigationView? = null
    private var mAccountSelectorView: View? = null
    var accountsSelector: RecyclerView? = null
        private set
    private var mAccountProfileBannerView: ViewSwitcher? = null
    private var mFloatingProfileImageSnapshotView: ImageView? = null
    private var mAccountProfileImageView: ShapedImageView? = null
    private var mAccountProfileNameView: TextView? = null
    private var mAccountProfileScreenNameView: TextView? = null
    private var mAccountsToggleMenu: ActionMenuView? = null
    private var mAccountProfileContainer: View? = null
    private var mNoAccountContainer: View? = null

    private var mAccountActionProvider: AccountToggleProvider? = null
    private val mReloadContentObserver = object : SupportFragmentReloadCursorObserver(
            this, 0, this) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val cr = contentResolver
            val c = cr.query(Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION)
            try {
                updateAccountProviderData(c)
            } finally {
                Utils.closeSilently(c)
            }
            super.onChange(selfChange, uri)
        }
    }
    private var mSwitchAccountAnimationPlaying: Boolean = false
    private var mUseStarsForLikes: Boolean = false
    private var mLoaderInitialized: Boolean = false

    val activatedAccountIds: Array<UserKey>
        get() {
            if (mAccountActionProvider != null) {
                return mAccountActionProvider!!.activatedAccountIds
            }
            return DataStoreUtils.getActivatedAccountKeys(activity)
        }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
                                              keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_PREVIOUS == action || ACTION_NAVIGATION_NEXT == action
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
                                              keyCode: Int, repeatCount: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState) ?: return false
        val offset: Int
        when (action) {
            ACTION_NAVIGATION_PREVIOUS -> {
                offset = -1
            }
            ACTION_NAVIGATION_NEXT -> {
                offset = 1
            }
            else -> {
                return false
            }
        }
        //        final int selectedItem = mNavigationView.getSelectedItemPosition();
        //        final int count = mNavigationView.getCount();
        //        int resultPosition;
        //        if (!mNavigationView.isFocused() || selectedItem == ListView.INVALID_POSITION) {
        //            resultPosition = firstVisiblePosition;
        //        } else {
        //            resultPosition = selectedItem + offset;
        //            while (resultPosition >= 0 && resultPosition < count && !mAdapter.isEnabled(resultPosition)) {
        //                resultPosition += offset;
        //            }
        //        }
        //        final View focusedChild = mNavigationView.getFocusedChild();
        //        if (focusedChild == null) {
        //            mNavigationView.requestChildFocus(mNavigationView.getChildAt(0), null);
        //        }
        //        if (resultPosition >= 0 && resultPosition < count) {
        //            mNavigationView.setSelection(resultPosition);
        //        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SETTINGS -> {
                if (data == null) return
                val activity = activity
                if (data.getBooleanExtra(EXTRA_CHANGED, false)) {
                    activity.recreate()
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        updateDefaultAccountState()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.profileContainer -> {
                val account = mAccountsAdapter!!.selectedAccount ?: return
                val activity = activity
                if (account.account_user != null) {
                    IntentUtils.openUserProfile(activity, account.account_user!!, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            Referral.SELF_PROFILE)
                } else {
                    IntentUtils.openUserProfile(activity, account.account_key,
                            account.account_key, account.screen_name, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            Referral.SELF_PROFILE)
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        return CursorLoader(activity, Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        updateAccountProviderData(data)
    }

    private fun updateAccountProviderData(cursor: Cursor?) {
        if (cursor == null) return
        val menu = mAccountsToggleMenu!!.menu
        mAccountActionProvider = MenuItemCompat.getActionProvider(menu.findItem(R.id.select_account)) as AccountToggleProvider
        val accounts = ParcelableAccountUtils.getAccounts(cursor)
        if (accounts.size > 0) {
            mNoAccountContainer!!.visibility = View.GONE
            mAccountProfileContainer!!.visibility = View.VISIBLE
        } else {
            mNoAccountContainer!!.visibility = View.VISIBLE
            mAccountProfileContainer!!.visibility = View.INVISIBLE
        }
        var defaultId: UserKey? = null
        for (account in accounts) {
            if (account.is_activated) {
                defaultId = account.account_key
                break
            }
        }
        mUseStarsForLikes = preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)

        mAccountsAdapter!!.accounts = accounts
        var accountKey = UserKey.valueOf(preferences.getString(KEY_DEFAULT_ACCOUNT_KEY, null))
        if (accountKey == null) {
            accountKey = defaultId
        }
        var selectedAccount: ParcelableAccount? = null
        for (account in accounts) {
            if (account.account_key.maybeEquals(accountKey)) {
                selectedAccount = account
                break
            }
        }
        mAccountsAdapter!!.selectedAccount = selectedAccount

        if (mAccountActionProvider != null) {
            mAccountActionProvider!!.isExclusive = false
            mAccountActionProvider!!.accounts = accounts
        }
        updateAccountActions()
        val currentAccount = mAccountsAdapter!!.selectedAccount
        if (currentAccount != null) {
            displayAccountBanner(currentAccount)
            displayCurrentAccount(null)
        }
        updateDefaultAccountState()
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (KEY_DEFAULT_ACCOUNT_KEY == key) {
            updateDefaultAccountState()
        }
    }

    override fun fitSystemWindows(insets: Rect) {
        mSystemWindowsInsets.set(insets)
        updateSystemWindowsInsets()
    }

    private fun updateSystemWindowsInsets() {
        if (mAccountProfileContainer == null) return
        val insets = mSystemWindowsInsets
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mResolver = contentResolver
        val view = view!!
        val context = view.context
        val inflater = getLayoutInflater(savedInstanceState)
        mAccountsAdapter = AccountSelectorAdapter(inflater, this)
        val layoutManager = FixedLinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        layoutManager.stackFromEnd = true
        accountsSelector!!.layoutManager = layoutManager
        accountsSelector!!.adapter = mAccountsAdapter
        accountsSelector!!.itemAnimator = null
        val menuInflater = SupportMenuInflater(context)
        menuInflater.inflate(R.menu.action_dashboard_timeline_toggle, mAccountsToggleMenu!!.menu)
        mAccountsToggleMenu!!.setOnMenuItemClickListener(OnMenuItemClickListener { item ->
            if (item.groupId != AccountToggleProvider.MENU_GROUP) {
                when (item.itemId) {
                    R.id.compose -> {
                        val account = mAccountsAdapter!!.selectedAccount ?: return@OnMenuItemClickListener true
                        val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                        composeIntent.setClass(activity, ComposeActivity::class.java)
                        composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key)
                        startActivity(composeIntent)
                        return@OnMenuItemClickListener true
                    }
                }
                return@OnMenuItemClickListener false
            }
            val accounts = mAccountActionProvider!!.accounts
            val account = accounts[item.order]
            val values = ContentValues()
            val newActivated = !account.is_activated
            mAccountActionProvider!!.setAccountActivated(account.account_key, newActivated)
            values.put(Accounts.IS_ACTIVATED, newActivated)
            val where = Expression.equalsArgs(Accounts.ACCOUNT_KEY).sql
            val whereArgs = arrayOf(account.account_key.toString())
            mResolver!!.update(Accounts.CONTENT_URI, values, where, whereArgs)
            true
        })

        mAccountProfileContainer!!.setOnClickListener(this)

        mAccountProfileBannerView!!.setInAnimation(getContext(), android.R.anim.fade_in)
        mAccountProfileBannerView!!.setOutAnimation(getContext(), android.R.anim.fade_out)
        mAccountProfileBannerView!!.setFactory {
            inflater.inflate(R.layout.layout_account_dashboard_profile_image,
                    mAccountProfileBannerView, false)
        }

        mNavigationView!!.setNavigationItemSelectedListener(this)
        preferences.registerOnSharedPreferenceChangeListener(this)

        loadAccounts()

        updateSystemWindowsInsets()
    }

    fun loadAccounts() {
        if (!mLoaderInitialized) {
            mLoaderInitialized = true
            loaderManager.initLoader(0, null, this)
        } else {
            loaderManager.restartLoader(0, null, this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_accounts_dashboard, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNavigationView = view!!.findViewById(R.id.navigation_view) as NavigationView
        mAccountSelectorView = mNavigationView!!.getHeaderView(0)
        accountsSelector = mAccountSelectorView!!.findViewById(R.id.otherAccountsList) as RecyclerView
        mAccountProfileContainer = mAccountSelectorView!!.findViewById(R.id.profileContainer)
        mNoAccountContainer = mAccountSelectorView!!.findViewById(R.id.noAccountContainer)
        mAccountProfileImageView = mAccountSelectorView!!.findViewById(R.id.profileImage) as ShapedImageView
        mAccountProfileBannerView = mAccountSelectorView!!.findViewById(R.id.accountProfileBanner) as ViewSwitcher
        mFloatingProfileImageSnapshotView = mAccountSelectorView!!.findViewById(R.id.floatingProfileImageSnapshot) as ImageView
        mAccountProfileNameView = mAccountSelectorView!!.findViewById(R.id.name) as TextView
        mAccountProfileScreenNameView = mAccountSelectorView!!.findViewById(R.id.screenName) as TextView
        mAccountsToggleMenu = mAccountSelectorView!!.findViewById(R.id.accountDashboardMenu) as ActionMenuView
    }

    override fun onStart() {
        super.onStart()
        val resolver = contentResolver
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver)
        loaderManager.restartLoader(0, null, this)
    }

    override fun onStop() {
        val resolver = contentResolver
        resolver.unregisterContentObserver(mReloadContentObserver)
        super.onStop()
    }

    internal fun updateAccountActions() {
        val activity = activity as HomeActivity ?: return
        val tabs = activity.tabs
        val account = mAccountsAdapter!!.selectedAccount ?: return
        var hasDmTab = false
        var hasInteractionsTab = false
        for (tab in tabs) {
            if (tab.type == null) continue
            when (tab.type) {
                CustomTabType.DIRECT_MESSAGES -> {
                    if (!hasDmTab) {
                        hasDmTab = hasAccountInTab(tab, account.account_key, account.is_activated)
                    }
                }
                CustomTabType.NOTIFICATIONS_TIMELINE -> {
                    if (!hasInteractionsTab) {
                        hasInteractionsTab = hasAccountInTab(tab, account.account_key, account.is_activated)
                    }
                }
            }
        }
        val menu = mNavigationView!!.menu
        menu.setItemAvailability(R.id.interactions, !hasInteractionsTab)
        menu.setItemAvailability(R.id.messages, !hasDmTab)

        if (mUseStarsForLikes) {
            menu.setMenuItemTitle(R.id.favorites, R.string.favorites)
            menu.setMenuItemIcon(R.id.favorites, R.drawable.ic_action_star)
        } else {
            menu.setMenuItemTitle(R.id.favorites, R.string.likes)
            menu.setMenuItemIcon(R.id.favorites, R.drawable.ic_action_heart)
        }
        var hasLists = false
        var hasGroups = false
        var hasPublicTimeline = false
        when (ParcelableAccountUtils.getAccountType(account)) {
            ParcelableAccount.Type.TWITTER -> {
                hasLists = true
            }
            ParcelableAccount.Type.STATUSNET -> {
                hasGroups = true
            }
            ParcelableAccount.Type.FANFOU -> {
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
        return ArrayUtils.contains(accountKeys, accountId)
    }

    private fun closeAccountsDrawer() {
        val activity = activity
        if (activity is HomeActivity) {
            activity.closeAccountsDrawer()
        }
    }

    private fun getLocationOnScreen(view: View, rectF: RectF) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        rectF.set(location[0].toFloat(), location[1].toFloat(), (location[0] + view.width).toFloat(), (location[1] + view.height).toFloat())
    }

    private fun onAccountSelected(holder: AccountProfileImageViewHolder, account: ParcelableAccount) {
        if (mSwitchAccountAnimationPlaying) return
        val snapshotView = mFloatingProfileImageSnapshotView!!
        val profileImageView = mAccountProfileImageView!!
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
                val profileDrawable = profileImageView!!.drawable
                clickedDrawable = clickedImageView.drawable
                clickedColors = clickedImageView.borderColors
                val oldSelectedAccount = mAccountsAdapter!!.selectedAccount ?: return
                mediaLoader.displayDashboardProfileImage(clickedImageView,
                        oldSelectedAccount, profileDrawable)
                clickedImageView.setBorderColors(*profileImageView.borderColors)

                displayAccountBanner(account)

                mSwitchAccountAnimationPlaying = true
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
                val editor = preferences.edit()
                editor.putString(KEY_DEFAULT_ACCOUNT_KEY, account.account_key.toString())
                editor.apply()
                mAccountsAdapter!!.selectedAccount = account
                updateAccountActions()
                displayCurrentAccount(clickedDrawable)
                snapshotView.visibility = View.INVISIBLE
                snapshotView.setImageDrawable(null)
                profileImageView!!.setImageDrawable(clickedDrawable)
                profileImageView.setBorderColors(*clickedColors!!)
                profileImageView.alpha = 1f
                clickedImageView.scaleX = 1f
                clickedImageView.scaleY = 1f
                clickedImageView.alpha = 1f
                mSwitchAccountAnimationPlaying = false
            }
        })
        set.start()

    }

    protected fun displayAccountBanner(account: ParcelableAccount) {
        val bannerWidth = mAccountProfileBannerView!!.width
        val res = resources
        val defWidth = res.displayMetrics.widthPixels
        val width = if (bannerWidth > 0) bannerWidth else defWidth
        val bannerView = mAccountProfileBannerView!!.nextView as ImageView
        if (bannerView.drawable == null || !CompareUtils.objectEquals(account, bannerView.tag)) {
            mediaLoader.displayProfileBanner(bannerView, account, width)
            bannerView.tag = account
        } else {
            mediaLoader.cancelDisplayTask(bannerView)
        }
    }

    private fun displayCurrentAccount(profileImageSnapshot: Drawable?) {
        val account = mAccountsAdapter!!.selectedAccount ?: return
        mAccountProfileNameView!!.text = account.name
        mAccountProfileScreenNameView!!.text = String.format("@%s", account.screen_name)
        mediaLoader.displayDashboardProfileImage(mAccountProfileImageView!!, account,
                profileImageSnapshot)
        mAccountProfileImageView!!.setBorderColors(account.color)
        mAccountProfileBannerView!!.showNext()
    }

    private fun updateDefaultAccountState() {
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val account = mAccountsAdapter!!.selectedAccount ?: return false
        when (item.itemId) {
            R.id.search -> {
                val intent = Intent(activity, QuickSearchBarActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key)
                startActivity(intent)
                closeAccountsDrawer()
            }
            R.id.compose -> {
                val composeIntent = Intent(INTENT_ACTION_COMPOSE)
                composeIntent.setClass(activity, ComposeActivity::class.java)
                composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key)
                startActivity(composeIntent)
            }
            R.id.favorites -> {
                IntentUtils.openUserFavorites(activity, account.account_key,
                        account.account_key, account.screen_name)
            }
            R.id.lists -> {
                IntentUtils.openUserLists(activity, account.account_key,
                        account.account_key, account.screen_name)
            }
            R.id.groups -> {
                IntentUtils.openUserGroups(activity, account.account_key,
                        account.account_key, account.screen_name)
            }
            R.id.public_timeline -> {
                IntentUtils.openPublicTimeline(activity, account.account_key)
            }
            R.id.messages -> {
                IntentUtils.openDirectMessages(activity, account.account_key)
            }
            R.id.interactions -> {
                IntentUtils.openInteractions(activity, account.account_key)
            }
            R.id.edit -> {
                IntentUtils.openProfileEditor(activity, account.account_key)
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
            R.id.plus_service -> {
                val intent = Intent(activity, PlusServiceDashboardActivity::class.java)
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
        mAccountProfileContainer!!.setPadding(0, top, 0, 0)
    }

    internal class AccountProfileImageViewHolder(val adapter: AccountSelectorAdapter, itemView: View) : ViewHolder(itemView), OnClickListener {
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
    ) : Adapter<AccountProfileImageViewHolder>() {
        private val mediaLoader: MediaLoaderWrapper
        var accounts: Array<ParcelableAccount>? = null
            set(value) {
                if (value != null) {
                    val previousAccounts = accounts
                    if (previousAccounts != null) {
                        val tmpList = arrayListOf(*value)
                        val tmpResult = ArrayList<ParcelableAccount>()
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
                notifyDataSetChanged()
            }

        init {
            mediaLoader = fragment.mediaLoader
            setHasStableIds(true)
        }

        private fun indexOfAccount(accounts: List<ParcelableAccount>, accountId: UserKey): Int {
            var i = 0
            val j = accounts.size
            while (i < j) {
                if (accounts[i].account_key == accountId) return i
                i++
            }
            return -1
        }

        fun getAdapterAccount(adapterPosition: Int): ParcelableAccount? {
            if (accounts == null || accounts!!.size < 1) {
                return null
            }
            return accounts!![adapterPosition + 1]
        }

        var selectedAccount: ParcelableAccount?
            get() {
                if (accounts == null || accounts!!.size == 0) {
                    return null
                }
                return accounts!![0]
            }
            set(account) {
                val selectedAccount = selectedAccount
                if (selectedAccount == null || account == null) return
                swap(account, selectedAccount)
            }

        val selectedAccountKey: UserKey?
            get() {
                val selectedAccount = selectedAccount ?: return null
                return selectedAccount.account_key
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountProfileImageViewHolder {
            val view = inflater.inflate(R.layout.adapter_item_dashboard_account, parent, false)
            return AccountProfileImageViewHolder(this, view)
        }

        override fun onBindViewHolder(holder: AccountProfileImageViewHolder, position: Int) {
            val account = getAdapterAccount(position)
            mediaLoader.displayDashboardProfileImage(holder.iconView, account!!, null)
            holder.iconView.setBorderColor(account.color)
        }

        override fun getItemId(position: Int): Long {
            return getAdapterAccount(position)!!.hashCode().toLong()
        }

        override fun getItemCount(): Int {
            if (accounts == null || accounts!!.size == 0) return 0
            return accounts!!.size - 1
        }

        fun dispatchItemSelected(holder: AccountProfileImageViewHolder) {
            fragment.onAccountSelected(holder, getAdapterAccount(holder.adapterPosition)!!)
        }

        private fun swap(from: ParcelableAccount, to: ParcelableAccount) {
            val accounts = accounts ?: return
            val fromIdx = accounts.indexOfFirst { it == from }
            val toIdx = accounts.indexOfFirst { it == to }
            if (fromIdx < 0 || toIdx < 0) return
            val temp = accounts[toIdx]
            accounts[toIdx] = accounts[fromIdx]
            accounts[fromIdx] = temp
            notifyDataSetChanged()
        }
    }


    data class OptionItem(val name: Int, val icon: Int, val id: Int)

}