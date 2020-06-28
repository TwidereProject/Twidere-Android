/*
 * Twidere - Twitter client for Android
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
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.core.content.ContextCompat
import androidx.loader.content.FixedAsyncTaskLoader
import androidx.loader.content.Loader
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.util.Linkify
import android.util.SparseBooleanArray
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.loader.app.LoaderManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.header_user.*
import kotlinx.android.synthetic.main.header_user.view.*
import kotlinx.android.synthetic.main.layout_content_fragment_common.*
import kotlinx.android.synthetic.main.layout_content_pages_common.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.FriendshipUpdate
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.fragment.AbsStatusesFragment.StatusesFragmentDelegate
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowInsetsCallback
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.fragment.statuses.UserFavoritesFragment
import org.mariotaku.twidere.fragment.statuses.UserMediaTimelineFragment
import org.mariotaku.twidere.fragment.statuses.UserTimelineFragment
import org.mariotaku.twidere.graphic.ActionBarColorDrawable
import org.mariotaku.twidere.graphic.ActionIconDrawable
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.model.event.FriendshipUpdatedEvent
import org.mariotaku.twidere.model.event.ProfileUpdatedEvent
import org.mariotaku.twidere.model.event.TaskStateChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.model.util.ParcelableRelationshipUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.text.TwidereURLSpan
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener
import org.mariotaku.twidere.util.UserColorNameManager.UserColorChangedListener
import org.mariotaku.twidere.util.UserColorNameManager.UserNicknameChangedListener
import org.mariotaku.twidere.util.menu.TwidereMenuInfo
import org.mariotaku.twidere.util.shortcut.ShortcutCreator
import org.mariotaku.twidere.util.support.ActivitySupport
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat
import org.mariotaku.twidere.util.support.ViewSupport
import org.mariotaku.twidere.util.support.WindowSupport
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback
import org.mariotaku.twidere.view.TabPagerIndicator
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class UserFragment : BaseFragment(), OnClickListener, OnLinkClickListener,
        OnSizeChangedListener, OnTouchListener, DrawerCallback, SupportFragmentCallback,
        SystemWindowInsetsCallback, RefreshScrollTopInterface, ViewPager.OnPageChangeListener,
        KeyboardShortcutCallback, UserColorChangedListener, UserNicknameChangedListener,
        IToolBarSupportFragment, StatusesFragmentDelegate,
        AbsContentRecyclerViewFragment.RefreshCompleteListener {

    override val toolbar: Toolbar
        get() = profileContentContainer.toolbar

    private lateinit var profileBirthdayBanner: View
    private lateinit var actionBarBackground: ActionBarDrawable
    private lateinit var pagerAdapter: SupportTabsAdapter

    // Data fields
    var user: ParcelableUser? = null
        private set
    private var account: AccountDetails? = null
    private var relationship: ParcelableRelationship? = null

    private var systemWindowsInsets: Rect = Rect()
    private var userInfoLoaderInitialized: Boolean = false
    private var friendShipLoaderInitialized: Boolean = false
    private var bannerWidth: Int = 0
    private var cardBackgroundColor: Int = 0
    private var actionBarShadowColor: Int = 0
    private var uiColor: Int = 0
    private var primaryColor: Int = 0
    private var primaryColorDark: Int = 0
    private var nameFirst: Boolean = false
    private var previousTabItemIsDark: Int = 0
    private var previousActionBarItemIsDark: Int = 0
    private var hideBirthdayView: Boolean = false

    private val friendshipLoaderCallbacks = object : LoaderCallbacks<SingleResponse<ParcelableRelationship>> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<SingleResponse<ParcelableRelationship>> {
            activity!!.invalidateOptionsMenu()
            val accountKey = args?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            val user = args?.getParcelable<ParcelableUser>(EXTRA_USER)
            if (user != null && user.key == accountKey) {
                followingYouIndicator.visibility = View.GONE
                followContainer.follow.visibility = View.VISIBLE
                followProgress.visibility = View.VISIBLE
            } else {
                followingYouIndicator.visibility = View.GONE
                followContainer.follow.visibility = View.GONE
                followProgress.visibility = View.VISIBLE
            }
            return UserRelationshipLoader(activity!!, accountKey, user)
        }

        override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableRelationship>>) {

        }

        override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableRelationship>>,
                                    data: SingleResponse<ParcelableRelationship>) {
            followProgress.visibility = View.GONE
            displayRelationship(data.data)
            updateOptionsMenuVisibility()
        }

    }
    private val userInfoLoaderCallbacks = object : LoaderCallbacks<SingleResponse<ParcelableUser>> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<SingleResponse<ParcelableUser>> {
            val omitIntentExtra = args!!.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
            val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
            val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
            val screenName = args.getString(EXTRA_SCREEN_NAME)
            if (user == null && (!omitIntentExtra || !args.containsKey(EXTRA_USER))) {
                cardContent.visibility = View.GONE
                errorContainer.visibility = View.GONE
                progressContainer.visibility = View.VISIBLE
                errorText.text = null
                errorText.visibility = View.GONE
            }
            val user = this@UserFragment.user
            val loadFromCache = user == null || !user.is_cache && user.key.maybeEquals(userKey)
            return ParcelableUserLoader(activity!!, accountKey, userKey, screenName, arguments,
                    omitIntentExtra, loadFromCache)
        }

        override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableUser>>) {

        }

        override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableUser>>,
                                    data: SingleResponse<ParcelableUser>) {
            val activity = activity ?: return
            when {
                data.data != null -> {
                    val user = data.data
                    cardContent.visibility = View.VISIBLE
                    errorContainer.visibility = View.GONE
                    progressContainer.visibility = View.GONE
                    val account: AccountDetails = data.extras.getParcelable(EXTRA_ACCOUNT)!!
                    displayUser(user, account)
                    if (user.is_cache) {
                        val args = Bundle()
                        args.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key)
                        args.putParcelable(EXTRA_USER_KEY, user.key)
                        args.putString(EXTRA_SCREEN_NAME, user.screen_name)
                        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
                        loaderManager.restartLoader(LOADER_ID_USER, args, this)
                    }
                    updateOptionsMenuVisibility()
                }
                user?.is_cache == true -> {
                    cardContent.visibility = View.VISIBLE
                    errorContainer.visibility = View.GONE
                    progressContainer.visibility = View.GONE
                    displayUser(user, account)
                    updateOptionsMenuVisibility()
                }
                else -> {
                    if (data.hasException()) {
                        errorText.text = data.exception?.getErrorMessage(activity)
                        errorText.visibility = View.VISIBLE
                    }
                    cardContent.visibility = View.GONE
                    errorContainer.visibility = View.VISIBLE
                    progressContainer.visibility = View.GONE
                    displayUser(null, null)
                    updateOptionsMenuVisibility()
                }
            }
        }

    }

    private fun updateOptionsMenuVisibility() {
        setHasOptionsMenu(user != null && relationship != null)
    }

    private fun displayRelationship(relationship: ParcelableRelationship?) {
        val user = this.user ?: run {
            this.relationship = null
            return
        }
        if (user.key.maybeEquals(user.account_key)) {
            setFollowEditButton(R.drawable.ic_action_edit, R.color.material_light_blue,
                    R.string.action_edit)
            followContainer.follow.visibility = View.VISIBLE
            this.relationship = relationship
            return
        }
        if (relationship == null || !relationship.check(user)) {
            this.relationship = null
            return
        } else {
            this.relationship = relationship
        }
        activity?.invalidateOptionsMenu()
        when {
            relationship.blocked_by -> {
                pagesErrorContainer.visibility = View.GONE
                pagesErrorText.text = null
                pagesContent.visibility = View.VISIBLE
            }
            !relationship.following && user.hide_protected_contents -> {
                pagesErrorContainer.visibility = View.VISIBLE
                pagesErrorText.setText(R.string.user_protected_summary)
                pagesErrorIcon.setImageResource(R.drawable.ic_info_locked)
                pagesContent.visibility = View.GONE
            }
            else -> {
                pagesErrorContainer.visibility = View.GONE
                pagesErrorText.text = null
                pagesContent.visibility = View.VISIBLE
            }
        }
        when {
            relationship.blocking -> setFollowEditButton(R.drawable.ic_action_block, R.color.material_red,
                    R.string.action_unblock)
            relationship.blocked_by -> setFollowEditButton(R.drawable.ic_action_block, R.color.material_grey,
                    R.string.action_block)
            relationship.following -> setFollowEditButton(R.drawable.ic_action_confirm, R.color.material_light_blue,
                    R.string.action_unfollow)
            user.is_follow_request_sent -> setFollowEditButton(R.drawable.ic_action_time, R.color.material_light_blue,
                    R.string.label_follow_request_sent)
            else -> setFollowEditButton(R.drawable.ic_action_add, android.R.color.white,
                    R.string.action_follow)
        }
        followingYouIndicator.visibility = if (relationship.followed_by) View.VISIBLE else View.GONE

        context?.applicationContext?.contentResolver?.let { resolver ->
            task {
                resolver.insert(CachedUsers.CONTENT_URI, user, ParcelableUser::class.java)
                resolver.insert(CachedRelationships.CONTENT_URI, relationship, ParcelableRelationship::class.java)
            }
        }
        followContainer.follow.visibility = View.VISIBLE
    }

    override fun canScroll(dy: Float): Boolean {
        val fragment = currentVisibleFragment
        return fragment is DrawerCallback && fragment.canScroll(dy)
    }

    override fun cancelTouch() {
        val fragment = currentVisibleFragment
        if (fragment is DrawerCallback) {
            fragment.cancelTouch()
        }
    }

    override fun fling(velocity: Float) {
        val fragment = currentVisibleFragment
        if (fragment is DrawerCallback) {
            fragment.fling(velocity)
        }
    }

    override fun isScrollContent(x: Float, y: Float): Boolean {
        val v = viewPager
        val location = IntArray(2)
        v.getLocationInWindow(location)
        return x >= location[0] && x <= location[0] + v.width
                && y >= location[1] && y <= location[1] + v.height
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        updateSubtitle()
    }

    override fun onPageScrollStateChanged(state: Int) {
        userProfileSwipeLayout.isEnabled = state == ViewPager.SCROLL_STATE_IDLE
    }

    override fun scrollBy(dy: Float) {
        val fragment = currentVisibleFragment
        if (fragment is DrawerCallback) {
            fragment.scrollBy(dy)
        }
    }

    override fun shouldLayoutHeaderBottom(): Boolean {
        val drawer = userProfileDrawer
        val card = profileDetailsContainer
        if (drawer == null || card == null) return false
        return card.top + drawer.headerTop - drawer.paddingTop <= 0
    }

    override fun topChanged(top: Int) {
        val drawer = userProfileDrawer ?: return
        val offset = drawer.paddingTop - top
        updateScrollOffset(offset)

        val fragment = currentVisibleFragment
        if (fragment is DrawerCallback) {
            fragment.topChanged(top)
        }
    }

    @UiThread
    fun displayUser(user: ParcelableUser?, account: AccountDetails?) {
        val activity = activity ?: return
        this.user = user
        this.account = account
        if (user?.key == null) {
            profileImage.visibility = View.GONE
            profileType.visibility = View.GONE
            val theme = Chameleon.getOverrideTheme(activity, activity)
            setUiColor(theme.colorPrimary)
            return
        }
        val adapter = pagerAdapter
        for (i in 0 until adapter.count) {
            val sf = adapter.instantiateItem(viewPager, i) as? AbsStatusesFragment
            sf?.initLoaderIfNeeded()
        }
        profileImage.visibility = View.VISIBLE
        val resources = resources
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        cardContent.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        progressContainer.visibility = View.GONE
        this.user = user
        profileImage.setBorderColor(if (user.color != 0) user.color else Color.WHITE)
        profileNameContainer.drawEnd(user.account_color)
        profileNameContainer.name.setText(bidiFormatter.unicodeWrap(when {
            user.nickname.isNullOrEmpty() -> user.name
            else -> getString(R.string.name_with_nickname, user.name, user.nickname)
        }), TextView.BufferType.SPANNABLE)
        val typeIconRes = Utils.getUserTypeIconRes(user.is_verified, user.is_protected)
        if (typeIconRes != 0) {
            profileType.setImageResource(typeIconRes)
            profileType.visibility = View.VISIBLE
        } else {
            profileType.setImageDrawable(null)
            profileType.visibility = View.GONE
        }
        @SuppressLint("SetTextI18n")
        profileNameContainer.screenName.spannable = "@${user.acct}"
        val linkHighlightOption = preferences[linkHighlightOptionKey]
        val linkify = TwidereLinkify(this, linkHighlightOption)
        if (user.description_unescaped != null) {
            val text = SpannableStringBuilder.valueOf(user.description_unescaped).apply {
                user.description_spans?.applyTo(this)
                linkify.applyAllLinks(this, user.account_key, false, false)
            }
            descriptionContainer.description.spannable = text
        } else {
            descriptionContainer.description.spannable = user.description_plain
            Linkify.addLinks(descriptionContainer.description, Linkify.WEB_URLS)
        }
        descriptionContainer.hideIfEmpty(descriptionContainer.description)

        locationContainer.location.spannable = user.location
        locationContainer.visibility = if (locationContainer.location.empty) View.GONE else View.VISIBLE
        urlContainer.url.spannable = user.urlPreferred?.let {
            val ssb = SpannableStringBuilder(it)
            ssb.setSpan(TwidereURLSpan(it, highlightStyle = linkHighlightOption), 0, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return@let ssb
        }
        urlContainer.hideIfEmpty(urlContainer.url)
        if (user.created_at >= 0) {
            val createdAt = Utils.formatToLongTimeString(activity, user.created_at)
            val daysSinceCreation = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24.toFloat()
            val dailyTweets = (user.statuses_count / max(1f, daysSinceCreation)).roundToInt()

            createdAtContainer.visibility = View.VISIBLE
            createdAtContainer.createdAt.text = resources.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                    createdAt, dailyTweets)
        } else {
            createdAtContainer.visibility = View.GONE
        }
        val locale = Locale.getDefault()

        listedContainer.listedCount.text = Utils.getLocalizedNumber(locale, user.listed_count)
        groupsContainer.groupsCount.text = Utils.getLocalizedNumber(locale, user.groups_count)
        followersContainer.followersCount.text = Utils.getLocalizedNumber(locale, user.followers_count)
        friendsContainer.friendsCount.text = Utils.getLocalizedNumber(locale, user.friends_count)

        listedContainer.visibility = if (user.listed_count < 0) View.GONE else View.VISIBLE
        groupsContainer.visibility = if (user.groups_count < 0) View.GONE else View.VISIBLE

        when {
            user.color != 0 -> {
                setUiColor(user.color)
            }
            user.link_color != 0 -> {
                setUiColor(user.link_color)
            }
            else -> {
                val theme = Chameleon.getOverrideTheme(activity, activity)
                setUiColor(theme.colorPrimary)
            }
        }
        val defWidth = resources.displayMetrics.widthPixels
        val width = if (bannerWidth > 0) bannerWidth else defWidth
        context?.let { requestManager.loadProfileBanner(it, user, width).into(profileBanner) }
        context?.let {
            requestManager.loadOriginalProfileImage(it, user, profileImage.style,
                profileImage.cornerRadius, profileImage.cornerRadiusRatio)
                .thumbnail(requestManager.loadProfileImage(it, user, profileImage.style,
                        profileImage.cornerRadius, profileImage.cornerRadiusRatio,
                        getString(R.string.profile_image_size))).into(profileImage)
        }
        val relationship = relationship
        if (relationship == null) {
            getFriendship()
        }
        activity.title = SpannableStringBuilder.valueOf(UserColorNameManager.decideDisplayName(user.nickname, user.name,
                user.screen_name, nameFirst)).also {
            externalThemeManager.emoji?.applyTo(it)
        }

        val userCreationDay = if (user.created_at >= 0) {
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            cal.timeInMillis = user.created_at
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.DAY_OF_MONTH) == currentDay
        } else {
            false
        }

        if (userCreationDay && !hideBirthdayView) {
            if (profileBirthdayStub != null) {
                profileBirthdayBanner = profileBirthdayStub.inflate()
                profileBirthdayBanner.setOnClickListener(this)
            } else {
                profileBirthdayBanner.visibility = View.VISIBLE
            }
        } else if (profileBirthdayStub == null) {
            profileBirthdayBanner.visibility = View.GONE
        }

        urlContainer.url.movementMethod = null

        updateTitleAlpha()
        activity.invalidateOptionsMenu()
        updateSubtitle()
    }

    override val currentVisibleFragment: Fragment?
        get() {
            val currentItem = viewPager.currentItem
            if (currentItem < 0 || currentItem >= pagerAdapter.count) return null
            return pagerAdapter.instantiateItem(viewPager, currentItem)
        }

    override fun triggerRefresh(position: Int): Boolean {
        return false
    }

    override fun getSystemWindowInsets(caller: Fragment, insets: Rect): Boolean {
        insetsCallback?.getSystemWindowInsets(this, insets)
        insets.top = 0
        return true
    }

    fun getUserInfo(accountKey: UserKey, userKey: UserKey?, screenName: String?,
            omitIntentExtra: Boolean) {
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
        args.putParcelable(EXTRA_USER_KEY, userKey)
        args.putString(EXTRA_SCREEN_NAME, screenName)
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra)
        if (!userInfoLoaderInitialized) {
            lm.initLoader(LOADER_ID_USER, args, userInfoLoaderCallbacks)
            userInfoLoaderInitialized = true
        } else {
            lm.restartLoader(LOADER_ID_USER, args, userInfoLoaderCallbacks)
        }
        if (userKey == null && screenName == null) {
            cardContent.visibility = View.GONE
            errorContainer.visibility = View.GONE
        }
    }

    @Subscribe
    fun notifyFriendshipUpdated(event: FriendshipUpdatedEvent) {
        val user = user
        if (user == null || !event.isAccount(user.account_key) || !event.isUser(user.key.id))
            return
        getFriendship()
    }

    @Subscribe
    fun notifyFriendshipUserUpdated(event: FriendshipTaskEvent) {
        val user = user
        if (user == null || !event.isSucceeded || !event.isUser(user)) return
        getFriendship()
    }

    @Subscribe
    fun notifyProfileUpdated(event: ProfileUpdatedEvent) {
        val user = user
        // TODO check account status
        if (user == null || user != event.user) return
        displayUser(event.user, account)
    }

    @Subscribe
    fun notifyTaskStateChanged(event: TaskStateChangedEvent) {
        activity?.invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val user = user ?: return
        val accountKey = user.account_key ?: return
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return
                    val color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT)
                    userColorNameManager.setUserColor(user.key, color)
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    userColorNameManager.clearUserColor(user.key)
                }
            }
            REQUEST_ADD_TO_LIST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val twitter = twitterWrapper
                    val list = data.getParcelableExtra<ParcelableUserList>(EXTRA_USER_LIST) ?: return
                    twitter.addUserListMembersAsync(accountKey, list.id, user)
                }
            }
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return
                    val selectedAccountKey: UserKey = data.getParcelableExtra(EXTRA_ACCOUNT_KEY) ?: return
                    var userKey = user.key
                    if (account?.type == AccountType.MASTODON && account?.key?.host != selectedAccountKey.host) {
                        userKey = AcctPlaceholderUserKey(user.key.host)
                    }
                    activity?.let {
                        IntentUtils.openUserProfile(it, selectedAccountKey, userKey, user.screen_name,
                                user.extras?.statusnet_profile_url, preferences[newDocumentApiKey],
                                null)
                    }
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        nameFirst = preferences[nameFirstKey]
        cardBackgroundColor = ThemeUtils.getCardBackgroundColor(activity!!,
                preferences[themeBackgroundOptionKey], preferences[themeBackgroundAlphaKey])
        actionBarShadowColor = 0xA0000000.toInt()
        val args = arguments
        val accountKey = args?.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY) ?: run {
            activity.finish()
            return
        }
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)

        Utils.setNdefPushMessageCallback(activity, CreateNdefMessageCallback {
            val user = user ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(LinkCreator.getUserWebLink(user))))
        })


        userFragmentView.windowInsetsListener = OnApplyWindowInsetsListener listener@ { _, insets ->
            insets.getSystemWindowInsets(systemWindowsInsets)
            val top = insets.systemWindowInsetTop
            profileContentContainer.setPadding(0, top, 0, 0)
            profileBannerSpace.statusBarHeight = top

            if (profileBannerSpace.toolbarHeight == 0) {
                var toolbarHeight = toolbar.measuredHeight
                if (toolbarHeight == 0) {
                    toolbarHeight = ThemeUtils.getActionBarHeight(requireContext())
                }
                profileBannerSpace.toolbarHeight = toolbarHeight
            }
            updateRefreshProgressOffset()
            return@listener insets
        }

        profileContentContainer.onSizeChangedListener = object : OnSizeChangedListener {
            override fun onSizeChanged(view: View, w: Int, h: Int, oldw: Int, oldh: Int) {
                val toolbarHeight = toolbar.measuredHeight
                userProfileDrawer.setPadding(0, toolbarHeight, 0, 0)
                profileBannerSpace.toolbarHeight = toolbarHeight
            }

        }

        userProfileDrawer.setDrawerCallback(this)

        pagerAdapter = SupportTabsAdapter(activity, childFragmentManager)

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pagerAdapter
        toolbarTabs.setViewPager(viewPager)
        toolbarTabs.setTabDisplayOption(TabPagerIndicator.DisplayOption.LABEL)
        toolbarTabs.setOnPageChangeListener(this)

        followContainer.follow.setOnClickListener(this)
        profileImage.setOnClickListener(this)
        profileBanner.setOnClickListener(this)
        listedContainer.setOnClickListener(this)
        groupsContainer.setOnClickListener(this)
        followersContainer.setOnClickListener(this)
        friendsContainer.setOnClickListener(this)
        errorIcon.setOnClickListener(this)
        urlContainer.setOnClickListener(this)
        profileBanner.onSizeChangedListener = this
        profileBannerSpace.setOnTouchListener(this)

        userProfileSwipeLayout.setOnRefreshListener {
            if (!triggerRefresh()) {
                userProfileSwipeLayout.isRefreshing = false
            }
        }

        profileNameBackground.setBackgroundColor(cardBackgroundColor)
        profileDetailsContainer.setBackgroundColor(cardBackgroundColor)
        toolbarTabs.setBackgroundColor(cardBackgroundColor)

        val actionBarElevation = ThemeUtils.getSupportActionBarElevation(activity)
        ViewCompat.setElevation(toolbarTabs, actionBarElevation)

        actionBarBackground = ActionBarDrawable(ResourcesCompat.getDrawable(activity.resources,
                R.drawable.shadow_user_banner_action_bar, null)!!)
        setupBaseActionBar()
        setupViewStyle()
        setupUserPages()

        getUserInfo(accountKey, userKey, screenName, false)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
        userColorNameManager.registerColorChangedListener(this)
        userColorNameManager.registerNicknameChangedListener(this)
    }


    override fun onStop() {
        userColorNameManager.unregisterColorChangedListener(this)
        userColorNameManager.unregisterNicknameChangedListener(this)
        bus.unregister(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        setUiColor(uiColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_USER, user)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        user = null
        relationship = null
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_user_profile, menu)
    }

    @UiThread
    override fun onPrepareOptionsMenu(menu: Menu) {
        val user = this.user ?: return
        val accountKey = user.account_key ?: return
        val account = this.account
        val relationship = this.relationship
        val linkAvailable = LinkCreator.hasWebLink(user)
        val isMyself = accountKey.maybeEquals(user.key)
        val mentionItem = menu.findItem(R.id.mention)
        if (mentionItem != null) {
            val displayName = UserColorNameManager.decideDisplayName(user.nickname,
                    user.name, user.screen_name, nameFirst)
            mentionItem.title = getString(R.string.mention_user_name, displayName)
        }

        menu.setItemAvailability(R.id.qr_code, linkAvailable)
        menu.setItemAvailability(R.id.copy_url, linkAvailable)
        menu.setItemAvailability(R.id.open_in_browser, linkAvailable)

        menu.setItemAvailability(R.id.mention, !isMyself)
        menu.setItemAvailability(R.id.incoming_friendships, isMyself)
        menu.setItemAvailability(R.id.saved_searches, isMyself)

        menu.setItemAvailability(R.id.blocked_users, isMyself)
        menu.setItemAvailability(R.id.block, !isMyself)

        menu.setItemAvailability(R.id.add_to_home_screen_submenu,
                ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext()))

        var canAddToList = false
        var canMute = false
        var canReportSpam = false
        var canEnableRetweet = false
        var canEnableNotifications = false
        when (account?.type) {
            AccountType.TWITTER -> {
                canAddToList = true
                canMute = true
                canReportSpam = true
                canEnableRetweet = true
                canEnableNotifications = true
            }
            AccountType.MASTODON -> {
                canMute = true
            }
        }

        menu.setItemAvailability(R.id.add_to_list, canAddToList)
        menu.setItemAvailability(R.id.mute_user, !isMyself && canMute)
        menu.setItemAvailability(R.id.muted_users, isMyself && canMute)
        menu.setItemAvailability(R.id.report_spam, !isMyself && canReportSpam)
        menu.setItemAvailability(R.id.enable_retweets, !isMyself && canEnableRetweet)

        if (relationship != null) {
            menu.findItem(R.id.add_to_filter)?.apply {
                isChecked = relationship.filtering
            }

            if (isMyself) {
                menu.setItemAvailability(R.id.send_direct_message, false)
                menu.setItemAvailability(R.id.enable_notifications, false)
            } else {
                menu.setItemAvailability(R.id.send_direct_message, relationship.can_dm)
                menu.setItemAvailability(R.id.block, true)
                menu.setItemAvailability(R.id.enable_notifications, canEnableNotifications &&
                        relationship.following)

                menu.findItem(R.id.block)?.apply {
                    ActionIconDrawable.setMenuHighlight(this, TwidereMenuInfo(relationship.blocking))
                    this.setTitle(if (relationship.blocking) R.string.action_unblock else R.string.action_block)
                }
                menu.findItem(R.id.mute_user)?.apply {
                    isChecked = relationship.muting
                }
                menu.findItem(R.id.enable_retweets)?.apply {
                    isChecked = relationship.retweet_enabled
                }
                menu.findItem(R.id.enable_notifications)?.apply {
                    isChecked = relationship.notifications_enabled
                }
            }

        } else {
            menu.setItemAvailability(R.id.send_direct_message, false)
            menu.setItemAvailability(R.id.enable_notifications, false)
        }
        val intent = Intent(INTENT_ACTION_EXTENSION_OPEN_USER)
        val extras = Bundle()
        extras.putParcelable(EXTRA_USER, user)
        intent.putExtras(extras)
        menu.removeGroup(MENU_GROUP_USER_EXTENSION)
        activity?.let { MenuUtils.addIntentToMenu(it, menu, intent, MENU_GROUP_USER_EXTENSION) }
        val drawer = userProfileDrawer
        if (drawer != null) {
            val offset = drawer.paddingTop - drawer.headerTop
            previousActionBarItemIsDark = 0
            previousTabItemIsDark = 0
            updateScrollOffset(offset)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = context ?: return false
        val twitter = twitterWrapper
        val user = user ?: return false
        val accountKey = user.account_key ?: return false
        val currentFragmentManager = parentFragmentManager
        val userRelationship = relationship
        when (item.itemId) {
            R.id.block -> {
                if (userRelationship == null) return true
                if (userRelationship.blocking) {
                    twitter.destroyBlockAsync(accountKey, user.key)
                } else {
                    CreateUserBlockDialogFragment.show(currentFragmentManager, user)
                }
            }
            R.id.report_spam -> {
                ReportUserSpamDialogFragment.show(currentFragmentManager, user)
            }
            R.id.add_to_filter -> {
                if (userRelationship == null) return true
                if (userRelationship.filtering) {
                    DataStoreUtils.removeFromFilter(context, listOf(user))
                    Toast.makeText(activity, R.string.message_toast_user_filters_removed,
                            Toast.LENGTH_SHORT).show()
                    getFriendship()
                } else {
                    AddUserFilterDialogFragment.show(currentFragmentManager, user)
                }
            }
            R.id.mute_user -> {
                if (userRelationship == null) return true
                if (userRelationship.muting) {
                    twitter.destroyMuteAsync(accountKey, user.key)
                } else {
                    CreateUserMuteDialogFragment.show(currentFragmentManager, user)
                }
            }
            R.id.mention -> {
                val intent = Intent(INTENT_ACTION_MENTION)
                val bundle = Bundle()
                bundle.putParcelable(EXTRA_USER, user)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.send_direct_message -> {
                val am = AccountManager.get(activity)
                val builder = Uri.Builder().apply {
                    scheme(SCHEME_TWIDERE)
                    authority(AUTHORITY_MESSAGES)
                    path(PATH_MESSAGES_CONVERSATION_NEW)
                    appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
                }
                val intent = Intent(Intent.ACTION_VIEW, builder.build())
                intent.putExtra(EXTRA_ACCOUNT, AccountUtils.getAccountDetails(am, accountKey,
                        true))
                intent.putExtra(EXTRA_USERS, arrayOf(user))
                intent.putExtra(EXTRA_OPEN_CONVERSATION, true)
                startActivity(intent)
            }
            R.id.set_color -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, userColorNameManager.getUserColor(user.key))
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                intent.putExtra(EXTRA_CLEAR_BUTTON, true)
                startActivityForResult(intent, REQUEST_SET_COLOR)
            }
            R.id.clear_nickname -> {
                userColorNameManager.clearUserNickname(user.key)
            }
            R.id.set_nickname -> {
                val nick = userColorNameManager.getUserNickname(user.key)
                SetUserNicknameDialogFragment.show(currentFragmentManager, user.key, nick)
            }
            R.id.add_to_list -> {
                showAddToListDialog(user)
            }
            R.id.open_with_account -> {
                val intent = Intent(INTENT_ACTION_SELECT_ACCOUNT)
                activity?.let { intent.setClass(it, AccountSelectorActivity::class.java) }
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                when (account?.type) {
                    AccountType.MASTODON -> intent.putExtra(EXTRA_ACCOUNT_TYPE, AccountType.MASTODON)
                    else -> intent.putExtra(EXTRA_ACCOUNT_HOST, user.key.host)
                }
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
            }
            R.id.follow -> {
                if (userRelationship == null) return true
                val updatingRelationship = twitter.isUpdatingRelationship(accountKey,
                        user.key)
                if (!updatingRelationship) {
                    if (userRelationship.following) {
                        DestroyFriendshipDialogFragment.show(currentFragmentManager, user)
                    } else {
                        twitter.createFriendshipAsync(accountKey, user.key, user.screen_name)
                    }
                }
                return true
            }
            R.id.enable_retweets -> {
                val newState = !item.isChecked
                val update = FriendshipUpdate()
                update.retweets(newState)
                twitter.updateFriendship(accountKey, user.key, update)
                item.isChecked = newState
                return true
            }
            R.id.enable_notifications -> {
                val newState = !item.isChecked
                val update = FriendshipUpdate()
                update.deviceNotifications(newState)
                twitter.updateFriendship(accountKey, user.key, update)
                item.isChecked = newState
                return true
            }
            R.id.muted_users -> {
                activity?.let { IntentUtils.openMutesUsers(it, accountKey) }
                return true
            }
            R.id.blocked_users -> {
                IntentUtils.openUserBlocks(activity, accountKey)
                return true
            }
            R.id.incoming_friendships -> {
                activity?.let { IntentUtils.openIncomingFriendships(it, accountKey) }
                return true
            }
            R.id.user_mentions -> {
                IntentUtils.openUserMentions(context, accountKey, user.screen_name)
                return true
            }
            R.id.saved_searches -> {
                IntentUtils.openSavedSearches(context, accountKey)
                return true
            }
            R.id.open_in_browser -> {
                val uri = LinkCreator.getUserWebLink(user) ?: return true
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
                if (intent.resolveActivity(context.packageManager) != null) {
                    startActivity(intent)
                }
                return true
            }
            R.id.copy_url -> {
                val uri = LinkCreator.getUserWebLink(user)
                ClipboardUtils.setText(context, uri.toString())
                Toast.makeText(context, R.string.message_toast_link_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.qr_code -> {
                executeAfterFragmentResumed {
                    val df = UserQrDialogFragment()
                    df.arguments = Bundle {
                        this[EXTRA_USER] = user
                    }
                    df.show(it.childFragmentManager, "user_qr_code")
                }
                return true
            }
            R.id.add_user_to_home_screen -> {
                ShortcutCreator.performCreation(this) {
                    ShortcutCreator.user(context, accountKey, user)
                }
            }
            R.id.add_statuses_to_home_screen -> {
                ShortcutCreator.performCreation(this) {
                    ShortcutCreator.userTimeline(context, accountKey, user)
                }
            }
            R.id.add_favorites_to_home_screen -> {
                ShortcutCreator.performCreation(this) {
                    ShortcutCreator.userFavorites(context, accountKey, user)
                }
            }
            R.id.add_media_to_home_screen -> {
                ShortcutCreator.performCreation(this) {
                    ShortcutCreator.userMediaTimeline(context, accountKey, user)
                }
            }
            else -> {
                val intent = item.intent
                if (intent?.resolveActivity(context.packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
        return true
    }


    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                ACTION_NAVIGATION_PREVIOUS_TAB -> {
                    val previous = viewPager.currentItem - 1
                    if (previous >= 0 && previous < pagerAdapter.count) {
                        viewPager.setCurrentItem(previous, true)
                    }
                    return true
                }
                ACTION_NAVIGATION_NEXT_TAB -> {
                    val next = viewPager.currentItem + 1
                    if (next >= 0 && next < pagerAdapter.count) {
                        viewPager.setCurrentItem(next, true)
                    }
                    return true
                }
            }
        }
        return handler.handleKey(activity, null, keyCode, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                ACTION_NAVIGATION_PREVIOUS_TAB, ACTION_NAVIGATION_NEXT_TAB -> return true
            }
        }
        return false
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
            keyCode: Int, repeatCount: Int,
            event: KeyEvent, metaState: Int): Boolean {
        return handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    private fun updateSubtitle() {
        val activity = activity as AppCompatActivity
        val actionBar = activity.supportActionBar ?: return
        val user = this.user
        if (user == null) {
            actionBar.subtitle = null
            return
        }
        val spec = pagerAdapter.get(viewPager.currentItem)
        if (BuildConfig.DEBUG && spec.type == null) { error("Assertion failed") }
        when (spec.type) {
            TAB_TYPE_STATUSES, TAB_TYPE_STATUSES_WITH_REPLIES -> {
                actionBar.subtitle = resources.getQuantityString(R.plurals.N_statuses,
                        user.statuses_count.toInt(), user.statuses_count)
            }
            TAB_TYPE_MEDIA -> {
                if (user.media_count < 0) {
                    actionBar.setSubtitle(R.string.recent_media)
                } else {
                    actionBar.subtitle = resources.getQuantityString(R.plurals.N_media,
                            user.media_count.toInt(), user.media_count)
                }
            }
            TAB_TYPE_FAVORITES -> {
                if (user.favorites_count < 0) {
                    if (preferences[iWantMyStarsBackKey]) {
                        actionBar.setSubtitle(R.string.title_favorites)
                    } else {
                        actionBar.setSubtitle(R.string.title_likes)
                    }
                } else if (preferences[iWantMyStarsBackKey]) {
                    actionBar.subtitle = resources.getQuantityString(R.plurals.N_favorites,
                            user.favorites_count.toInt(), user.favorites_count)
                } else {
                    actionBar.subtitle = resources.getQuantityString(R.plurals.N_likes,
                            user.favorites_count.toInt(), user.favorites_count)
                }
            }
            else -> {
                actionBar.subtitle = null
            }
        }
        updateTitleAlpha()
    }

    private fun handleFragmentKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
            keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
        }
        return false
    }

    private fun handleFragmentKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
            keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
        }
        return false
    }

    private fun isFragmentKeyboardShortcutHandled(handler: KeyboardShortcutsHandler,
            keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
        }
        return false
    }

    private val keyboardShortcutRecipient: Fragment?
        get() = currentVisibleFragment

    override fun onApplySystemWindowInsets(insets: Rect) {
    }

    override fun setupWindow(activity: FragmentActivity): Boolean {
        if (activity is AppCompatActivity) {
            activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
            activity.supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY)
        }
        WindowSupport.setStatusBarColor(activity.window, Color.TRANSPARENT)
        return true
    }

    override fun onClick(view: View) {
        val activity = activity ?: return
        val fragmentManager = parentFragmentManager
        val user = user ?: return
        val accountKey = user.account_key ?: return
        when (view.id) {
            R.id.errorContainer -> {
                getUserInfo(true)
            }
            R.id.follow -> {
                if (accountKey.maybeEquals(user.key)) {
                    IntentUtils.openProfileEditor(activity, accountKey)
                } else {
                    val userRelationship = relationship
                    val twitter = twitterWrapper
                    if (userRelationship == null) return
                    when {
                        userRelationship.blocking -> {
                            twitter.destroyBlockAsync(accountKey, user.key)
                        }
                        userRelationship.blocked_by -> {
                            CreateUserBlockDialogFragment.show(childFragmentManager, user)
                        }
                        userRelationship.following -> {
                            DestroyFriendshipDialogFragment.show(fragmentManager, user)
                        }
                        else -> {
                            twitter.createFriendshipAsync(accountKey, user.key, user.screen_name)
                        }
                    }
                }
            }
            R.id.profileImage -> {
                val url = user.originalProfileImage ?: return
                val profileImage = ParcelableMediaUtils.image(url)
                profileImage.type = ParcelableMedia.Type.IMAGE
                profileImage.preview_url = user.profile_image_url
                val media = arrayOf(profileImage)
                IntentUtils.openMedia(activity, accountKey, media, null, false,
                        preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
            }
            R.id.profileBanner -> {
                val url = user.getBestProfileBanner(0) ?: return
                val profileBanner = ParcelableMediaUtils.image(url)
                profileBanner.type = ParcelableMedia.Type.IMAGE
                val media = arrayOf(profileBanner)
                IntentUtils.openMedia(activity, accountKey, media, null, false,
                        preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
            }
            R.id.listedContainer -> {
                IntentUtils.openUserLists(activity, accountKey, user.key,
                        user.screen_name)
            }
            R.id.groupsContainer -> {
                IntentUtils.openUserGroups(activity, accountKey, user.key,
                        user.screen_name)
            }
            R.id.followersContainer -> {
                IntentUtils.openUserFollowers(activity, accountKey, user.key,
                        user.screen_name)
            }
            R.id.friendsContainer -> {
                IntentUtils.openUserFriends(activity, accountKey, user.key,
                        user.screen_name)
            }
            R.id.nameContainer -> {
                if (accountKey == user.key) return
                IntentUtils.openProfileEditor(activity, accountKey)
            }
            R.id.urlContainer -> {
                val uri = user.urlPreferred?.let(Uri::parse) ?: return
                OnLinkClickHandler.openLink(activity, preferences, uri)
            }
            R.id.profileBirthdayBanner -> {
                hideBirthdayView = true
                profileBirthdayBanner.startAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_out))
                profileBirthdayBanner.visibility = View.GONE
            }
        }

    }

    override fun onLinkClick(link: String, orig: String?, accountKey: UserKey?,
            extraId: Long, type: Int, sensitive: Boolean,
            start: Int, end: Int): Boolean {
        val user = user ?: return false
        val activity = activity ?: return false
        when (type) {
            TwidereLinkify.LINK_TYPE_MENTION -> {
                IntentUtils.openUserProfile(activity, user.account_key, null, link, null,
                        preferences[newDocumentApiKey], null)
                return true
            }
            TwidereLinkify.LINK_TYPE_HASHTAG -> {
                IntentUtils.openTweetSearch(activity, user.account_key, "#$link")
                return true
            }
            TwidereLinkify.LINK_TYPE_LINK_IN_TEXT, TwidereLinkify.LINK_TYPE_ENTITY_URL -> {
                val uri = Uri.parse(link)
                val intent: Intent
                intent = if (uri.scheme != null) {
                    Intent(Intent.ACTION_VIEW, uri)
                } else {
                    Intent(Intent.ACTION_VIEW, uri.buildUpon().scheme("http").build())
                }
                startActivity(intent)
                return true
            }
            TwidereLinkify.LINK_TYPE_LIST -> {
                val mentionList = link.split("/").dropLastWhile(String::isEmpty)
                if (mentionList.size != 2) {
                    return false
                }
                return true
            }
        }
        return false
    }

    override fun onUserNicknameChanged(userKey: UserKey, nick: String?) {
        if (user?.key != userKey) return
        displayUser(user, account)
    }

    override fun onUserColorChanged(userKey: UserKey, color: Int) {
        if (user?.key != userKey) return
        displayUser(user, account)
    }

    override fun onSizeChanged(view: View, w: Int, h: Int, oldw: Int, oldh: Int) {
        bannerWidth = w
        if (w != oldw || h != oldh) {
            requestApplyInsets()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (profileBirthdayStub == null && profileBirthdayBanner.visibility == View.VISIBLE) {
            return profileBirthdayBanner.dispatchTouchEvent(event)
        }
        return profileBanner.dispatchTouchEvent(event)
    }

    override fun scrollToStart(): Boolean {
        val fragment = currentVisibleFragment as? RefreshScrollTopInterface ?: return false
        fragment.scrollToStart()
        return true
    }

    override fun triggerRefresh(): Boolean {
        val fragment = currentVisibleFragment as? RefreshScrollTopInterface ?: return false
        fragment.triggerRefresh()
        return true
    }

    override fun onRefreshComplete(fragment: AbsContentRecyclerViewFragment<*, *>) {
        userProfileSwipeLayout.isRefreshing = false
    }

    private fun updateRefreshProgressOffset() {
        val insets = this.systemWindowsInsets
        if (insets.top == 0 || userProfileSwipeLayout == null || userProfileSwipeLayout.isRefreshing) {
            return
        }
        val progressCircleDiameter = userProfileSwipeLayout.progressCircleDiameter
        if (progressCircleDiameter == 0) return
        val progressViewStart = 0 - progressCircleDiameter
        val progressViewEnd = profileBannerSpace.toolbarHeight + resources.getDimensionPixelSize(R.dimen.element_spacing_normal)
        userProfileSwipeLayout.setProgressViewOffset(false, progressViewStart, progressViewEnd)
    }

    private fun getFriendship() {
        val user = user ?: return
        relationship = null
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key)
        args.putParcelable(EXTRA_USER, user)
        if (!friendShipLoaderInitialized) {
            lm.initLoader(LOADER_ID_FRIENDSHIP, args, friendshipLoaderCallbacks)
            friendShipLoaderInitialized = true
        } else {
            lm.restartLoader(LOADER_ID_FRIENDSHIP, args, friendshipLoaderCallbacks)
        }
    }

    private fun getUserInfo(omitIntentExtra: Boolean) {
        val user = this.user ?: return
        val accountKey = user.account_key ?: return
        getUserInfo(accountKey, user.key, user.screen_name, omitIntentExtra)
    }

    private fun setUiColor(color: Int) {
        val activity = activity as? BaseActivity ?: return
        val theme = Chameleon.getOverrideTheme(activity, activity)
        uiColor = if (color != 0) color else theme.colorPrimary
        previousActionBarItemIsDark = 0
        previousTabItemIsDark = 0
        setupBaseActionBar()
        primaryColor = if (theme.isToolbarColored) {
            color
        } else {
            theme.colorToolbar
        }
        primaryColorDark = ChameleonUtils.darkenColor(primaryColor)
        actionBarBackground.color = primaryColor
        val taskColor: Int
        taskColor = if (theme.isToolbarColored) {
            ColorUtils.setAlphaComponent(color, 0xFF)
        } else {
            ColorUtils.setAlphaComponent(theme.colorToolbar, 0xFF)
        }
        val user = this.user
        if (user != null) {
            val name = userColorNameManager.getDisplayName(user, nameFirst)
            ActivitySupport.setTaskDescription(activity, TaskDescriptionCompat(name, null, taskColor))
        } else {
            ActivitySupport.setTaskDescription(activity, TaskDescriptionCompat(null, null, taskColor))
        }
        val optimalAccentColor = ThemeUtils.getOptimalAccentColor(color,
                descriptionContainer.description.currentTextColor)
        descriptionContainer.description.setLinkTextColor(optimalAccentColor)
        locationContainer.location.setLinkTextColor(optimalAccentColor)
        urlContainer.url.setLinkTextColor(optimalAccentColor)
        profileBanner.setBackgroundColor(color)

        toolbarTabs.setBackgroundColor(primaryColor)

        val drawer = userProfileDrawer
        if (drawer != null) {
            val offset = drawer.paddingTop - drawer.headerTop
            updateScrollOffset(offset)
        }
    }

    private fun setupBaseActionBar() {
        val activity = activity as? LinkHandlerActivity ?: return
        val actionBar = activity.supportActionBar ?: return
        if (!ThemeUtils.isWindowFloating(activity) && ThemeUtils.isTransparentBackground(activity.currentThemeBackgroundOption)) {
            profileBanner.alpha = activity.currentThemeBackgroundAlpha / 255f
        }
        actionBar.setBackgroundDrawable(actionBarBackground)
    }


    private fun setupViewStyle() {
        profileImage.style = preferences[profileImageStyleKey]

        val lightFont = preferences[lightFontKey]

        profileNameContainer.name.applyFontFamily(lightFont)
        profileNameContainer.screenName.applyFontFamily(lightFont)
        profileNameContainer.followingYouIndicator.applyFontFamily(lightFont)
        descriptionContainer.description.applyFontFamily(lightFont)
        urlContainer.url.applyFontFamily(lightFont)
        locationContainer.location.applyFontFamily(lightFont)
        createdAtContainer.createdAt.applyFontFamily(lightFont)
    }

    private fun setupUserPages() {
        val args = arguments ?: return
        val tabArgs = Bundle()
        val user = args.getParcelable<ParcelableUser>(EXTRA_USER)
        val userKey: UserKey?
        if (user != null) {
            userKey = user.account_key
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, userKey)
            tabArgs.putParcelable(EXTRA_USER_KEY, user.key)
            tabArgs.putString(EXTRA_SCREEN_NAME, user.screen_name)
            tabArgs.putString(EXTRA_PROFILE_URL, user.extras?.statusnet_profile_url)
        } else {
            userKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, userKey)
            tabArgs.putParcelable(EXTRA_USER_KEY, args.getParcelable<Parcelable>(EXTRA_USER_KEY))
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME))
            tabArgs.putString(EXTRA_PROFILE_URL, args.getString(EXTRA_PROFILE_URL))
        }
        pagerAdapter.add(cls = UserTimelineFragment::class.java, args = Bundle(tabArgs).apply {
            this[UserTimelineFragment.EXTRA_ENABLE_TIMELINE_FILTER] = true
            this[UserTimelineFragment.EXTRA_LOAD_PINNED_STATUS] = true
        }, name = getString(R.string.title_statuses), type = TAB_TYPE_STATUSES,
                position = TAB_POSITION_STATUSES)
        pagerAdapter.add(cls = UserMediaTimelineFragment::class.java, args = tabArgs,
                name = getString(R.string.media), type = TAB_TYPE_MEDIA, position = TAB_POSITION_MEDIA)
        if (account?.type != AccountType.MASTODON || account?.key == userKey) {
            if (preferences[iWantMyStarsBackKey]) {
                pagerAdapter.add(cls = UserFavoritesFragment::class.java, args = tabArgs,
                        name = getString(R.string.title_favorites), type = TAB_TYPE_FAVORITES,
                        position = TAB_POSITION_FAVORITES)
            } else {
                pagerAdapter.add(cls = UserFavoritesFragment::class.java, args = tabArgs,
                        name = getString(R.string.title_likes), type = TAB_TYPE_FAVORITES,
                        position = TAB_POSITION_FAVORITES)
            }
        }
    }

    private fun updateScrollOffset(offset: Int) {
        val spaceHeight = profileBannerSpace.height
        val factor = (if (spaceHeight == 0) 0f else offset / spaceHeight.toFloat()).coerceIn(0f, 1f)
        profileBannerContainer.translationY = (-offset).toFloat()
        profileBanner.translationY = (offset / 2).toFloat()
        if (profileBirthdayStub == null) {
            profileBirthdayBanner.translationY = (offset / 2).toFloat()
        }

        val activity = activity as BaseActivity


        val statusBarColor = sArgbEvaluator.evaluate(factor, 0xA0000000.toInt(),
                ChameleonUtils.darkenColor(primaryColorDark)) as Int
        val window = activity.window
        userFragmentView.statusBarColor = statusBarColor
        WindowSupport.setLightStatusBar(window, ThemeUtils.isLightColor(statusBarColor))
        val stackedTabColor = primaryColor


        val profileContentHeight = (profileNameContainer!!.height + profileDetailsContainer.height).toFloat()
        val tabOutlineAlphaFactor: Float
        tabOutlineAlphaFactor = if (offset - spaceHeight > 0) {
            1f - ((offset - spaceHeight) / profileContentHeight).coerceIn(0f, 1f)
        } else {
            1f
        }

        actionBarBackground.apply {
            this.factor = factor
            this.outlineAlphaFactor = tabOutlineAlphaFactor
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            windowOverlay.alpha = factor * tabOutlineAlphaFactor
        }

        val currentTabColor = sArgbEvaluator.evaluate(tabOutlineAlphaFactor,
                stackedTabColor, cardBackgroundColor) as Int

        val tabBackground = toolbarTabs.background
        (tabBackground as ColorDrawable).color = currentTabColor
        val tabItemIsDark = ThemeUtils.isLightColor(currentTabColor)

        if (previousTabItemIsDark == 0 || (if (tabItemIsDark) 1 else -1) != previousTabItemIsDark) {
            val tabContrastColor = ThemeUtils.getColorDependent(currentTabColor)
            toolbarTabs.setIconColor(tabContrastColor)
            toolbarTabs.setLabelColor(tabContrastColor)
            val theme = Chameleon.getOverrideTheme(activity, activity)
            if (theme.isToolbarColored) {
                toolbarTabs.setStripColor(tabContrastColor)
            } else {
                toolbarTabs.setStripColor(ThemeUtils.getOptimalAccentColor(uiColor, tabContrastColor))
            }
            toolbarTabs.updateAppearance()
        }
        previousTabItemIsDark = if (tabItemIsDark) 1 else -1

        val currentActionBarColor = sArgbEvaluator.evaluate(factor, actionBarShadowColor,
                stackedTabColor) as Int
        val actionItemIsDark = ThemeUtils.isLightColor(currentActionBarColor)
        if (previousActionBarItemIsDark == 0 || (if (actionItemIsDark) 1 else -1) != previousActionBarItemIsDark) {
            ThemeUtils.applyToolbarItemColor(activity, toolbar, currentActionBarColor)
        }
        previousActionBarItemIsDark = if (actionItemIsDark) 1 else -1

        updateTitleAlpha()
    }

    override var controlBarOffset: Float
        get() = 0f
        set(value) = Unit //Ignore

    override val controlBarHeight: Int
        get() = 0


    override val shouldInitLoader: Boolean
        get() = user != null

    private fun updateTitleAlpha() {
        val location = IntArray(2)
        profileNameContainer.name.getLocationInWindow(location)
        val nameShowingRatio = (userProfileDrawer.paddingTop - location[1]) / profileNameContainer.name.height.toFloat()
        val textAlpha = nameShowingRatio.coerceIn(0f, 1f)
        val titleView = ViewSupport.findViewByText(toolbar, toolbar.title)
        if (titleView != null) {
            titleView.alpha = textAlpha
        }
        val subtitleView = ViewSupport.findViewByText(toolbar, toolbar.subtitle)
        if (subtitleView != null) {
            subtitleView.alpha = textAlpha
        }
    }

    private fun ParcelableRelationship.check(user: ParcelableUser): Boolean {
        if (account_key != user.account_key) {
            return false
        }
        return user_key.id == user.extras?.unique_id || user_key.id == user.key.id
    }

    private fun setFollowEditButton(@DrawableRes icon: Int, @ColorRes color: Int, @StringRes label: Int) {
        val followButton = followContainer.follow
        followButton.setImageResource(icon)
        ViewCompat.setBackgroundTintMode(followButton, PorterDuff.Mode.SRC_ATOP)
        ViewCompat.setBackgroundTintList(followButton, context?.let { ContextCompat.getColorStateList(it, color) })
        followButton.contentDescription = getString(label)
    }

    private fun showAddToListDialog(user: ParcelableUser) {
        val accountKey = user.account_key ?: return
        val weakThis = WeakReference(this)
        executeAfterFragmentResumed {
            ProgressDialogFragment.show(it.childFragmentManager, "get_list_progress")
        }.then {
            val fragment = weakThis.get() ?: throw InterruptedException()
            fun MicroBlog.getUserListOwnerMemberships(id: String): ArrayList<UserList> {
                val result = ArrayList<UserList>()
                var nextCursor: Long
                val paging = Paging()
                paging.count(100)
                do {
                    val resp = getUserListMemberships(id, paging, true)
                    result.addAll(resp)
                    nextCursor = resp.nextCursor
                    paging.cursor(nextCursor)
                } while (nextCursor > 0)

                return result
            }

            val microBlog = MicroBlogAPIFactory.getInstance(fragment.requireContext(), accountKey)
            val ownedLists = ArrayList<ParcelableUserList>()
            val listMemberships = microBlog.getUserListOwnerMemberships(user.key.id)
            val paging = Paging()
            paging.count(100)
            var nextCursor: Long
            do {
                val resp = microBlog.getUserListOwnerships(paging)
                resp.mapTo(ownedLists) { item ->
                    val userList = item.toParcelable(accountKey)
                    userList.is_user_inside = listMemberships.any { it.id == item.id }
                    return@mapTo userList
                }
                nextCursor = resp.nextCursor
                paging.cursor(nextCursor)
            } while (nextCursor > 0)
            return@then ownedLists.toTypedArray()
        }.alwaysUi {
            val fragment = weakThis.get() ?: return@alwaysUi
            fragment.executeAfterFragmentResumed {
                it.childFragmentManager.dismissDialogFragment("get_list_progress")
            }
        }.successUi { result ->
            val fragment = weakThis.get() ?: return@successUi
            fragment.executeAfterFragmentResumed { f ->
                val df = AddRemoveUserListDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_ACCOUNT_KEY] = accountKey
                    this[EXTRA_USER_KEY] = user.key
                    this[EXTRA_USER_LISTS] = result
                }
                df.show(f.childFragmentManager, "add_remove_list")
            }
        }.failUi {
            val fragment = weakThis.get() ?: return@failUi
            Toast.makeText(fragment.context, it.getErrorMessage(fragment.requireContext()),
                    Toast.LENGTH_SHORT).show()
        }
    }

    private class ActionBarDrawable(shadow: Drawable) : LayerDrawable(arrayOf(shadow, ActionBarColorDrawable.create(true))) {

        private val shadowDrawable = getDrawable(0)
        private val colorDrawable = getDrawable(1) as ColorDrawable
        private var alphaValue: Int = 0

        var factor: Float = 0f
            set(value) {
                field = value
                updateValue()
            }

        var color: Int = 0
            set(value) {
                field = value
                colorDrawable.color = value
                updateValue()
            }

        var outlineAlphaFactor: Float = 0f
            set(value) {
                field = value
                updateValue()
            }

        init {
            alpha = 0xFF
            updateValue()
        }

        override fun setAlpha(alpha: Int) {
            alphaValue = alpha
            updateValue()
        }

        override fun getAlpha(): Int {
            return alphaValue
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun getOutline(outline: Outline) {
            colorDrawable.getOutline(outline)
            outline.alpha = factor * outlineAlphaFactor * 0.99f
        }

        override fun getIntrinsicWidth(): Int {
            return colorDrawable.intrinsicWidth
        }

        override fun getIntrinsicHeight(): Int {
            return colorDrawable.intrinsicHeight
        }

        private fun updateValue() {
            val shadowAlpha = (alpha * (1 - factor).coerceIn(0f, 1f)).roundToInt()
            shadowDrawable.alpha = shadowAlpha
            val hasColor = color != 0
            val colorAlpha = if (hasColor) (alpha * factor.coerceIn(0f, 1f)).roundToInt() else 0
            colorDrawable.alpha = colorAlpha
            invalidateSelf()
        }

    }


    internal class UserRelationshipLoader(
            context: Context,
            private val accountKey: UserKey?,
            private val user: ParcelableUser?
    ) : FixedAsyncTaskLoader<SingleResponse<ParcelableRelationship>>(context) {

        override fun loadInBackground(): SingleResponse<ParcelableRelationship> {
            if (accountKey == null || user == null) {
                return SingleResponse(MicroBlogException("Null parameters"))
            }
            val userKey = user.key
            val isFiltering = DataStoreUtils.isFilteringUser(context, userKey)
            if (accountKey == user.key) {
                return SingleResponse(ParcelableRelationship().apply {
                    account_key = accountKey
                    user_key = userKey
                    filtering = isFiltering
                })
            }
            val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                    accountKey, true) ?: return SingleResponse(MicroBlogException("No Account"))
            if (details.type == AccountType.TWITTER) {
                if (!accountKey.hasSameHost(user.key)) {
                    return SingleResponse.getInstance(ParcelableRelationshipUtils.create(user, isFiltering))
                }
            }
            try {
                val data = when (details.type) {
                    AccountType.MASTODON -> {
                        val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                        mastodon.getRelationships(arrayOf(userKey.id))?.firstOrNull()
                                ?.toParcelable(accountKey, userKey, isFiltering)
                                ?: throw MicroBlogException("No relationship")
                    }
                    else -> {
                        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                        microBlog.showFriendship(user.key.id).toParcelable(accountKey, userKey,
                                isFiltering)
                    }
                }

                if (data.blocking || data.blocked_by) {
                    Utils.setLastSeen(context, userKey, -1)
                } else {
                    Utils.setLastSeen(context, userKey, System.currentTimeMillis())
                }
                val resolver = context.contentResolver
                val values = ObjectCursor.valuesCreatorFrom(ParcelableRelationship::class.java).create(data)
                resolver.insert(CachedRelationships.CONTENT_URI, values)
                return SingleResponse(data)
            } catch (e: MicroBlogException) {
                return SingleResponse(e)
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }
    }

    class AddRemoveUserListDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val lists = requireArguments().getTypedArray<ParcelableUserList>(EXTRA_USER_LISTS)
            val userKey = requireArguments().getParcelable<UserKey>(EXTRA_USER_KEY)!!
            val accountKey = requireArguments().getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!!
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.title_add_or_remove_from_list)
            val entries = Array(lists.size) { idx ->
                lists[idx].name
            }
            val states = BooleanArray(lists.size) { idx ->
                lists[idx].is_user_inside
            }
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNeutralButton(R.string.new_user_list, null)
            builder.setNegativeButton(android.R.string.cancel, null)

            builder.setMultiChoiceItems(entries, states, null)
            val dialog = builder.create()
            dialog.onShow { d ->
                d.applyTheme()
                d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val checkedPositions = d.listView.checkedItemPositions
                    val weakActivity = WeakReference(activity)
                    (activity as IBaseActivity<*>).executeAfterFragmentResumed {
                        ProgressDialogFragment.show(it.supportFragmentManager, "update_lists_progress")
                    }.then {
                        val activity = weakActivity.get() ?: throw IllegalStateException()
                        val twitter = MicroBlogAPIFactory.getInstance(activity, accountKey)
                        val successfulStates = SparseBooleanArray()
                        try {
                            for (i in 0 until checkedPositions.size()) {
                                val pos = checkedPositions.keyAt(i)
                                val checked = checkedPositions.valueAt(i)
                                if (states[pos] != checked) {
                                    if (checked) {
                                        twitter.addUserListMember(lists[pos].id, userKey.id)
                                    } else {
                                        twitter.deleteUserListMember(lists[pos].id, userKey.id)
                                    }
                                    successfulStates.put(pos, checked)
                                }
                            }
                        } catch (e: MicroBlogException) {
                            throw UpdateListsException(e, successfulStates)
                        }
                    }.alwaysUi {
                        val activity = weakActivity.get() as? IBaseActivity<*> ?: return@alwaysUi
                        activity.executeAfterFragmentResumed { a ->
                            val manager = a.supportFragmentManager
                            val df = manager.findFragmentByTag("update_lists_progress") as? DialogFragment
                            df?.dismiss()
                        }
                    }.successUi {
                        dismiss()
                    }.failUi { e ->
                        if (e is UpdateListsException) {
                            val successfulStates = e.successfulStates
                            for (i in 0 until successfulStates.size()) {
                                val pos = successfulStates.keyAt(i)
                                val checked = successfulStates.valueAt(i)
                                d.listView.setItemChecked(pos, checked)
                                states[pos] = checked
                            }
                        }
                        Toast.makeText(context, e.getErrorMessage(requireContext()), Toast.LENGTH_SHORT).show()
                    }
                }
                d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                    val df = CreateUserListDialogFragment()
                    df.arguments = Bundle {
                        this[EXTRA_ACCOUNT_KEY] = accountKey
                    }
                    df.show(parentFragmentManager, "create_user_list")
                }
            }
            return dialog
        }

        class UpdateListsException(cause: Throwable, val successfulStates: SparseBooleanArray) : MicroBlogException(cause)
    }

    companion object {

        private val sArgbEvaluator = ArgbEvaluator()
        private const val LOADER_ID_USER = 1
        private const val LOADER_ID_FRIENDSHIP = 2

        private const val TAB_POSITION_STATUSES = 0
        private const val TAB_POSITION_MEDIA = 1
        private const val TAB_POSITION_FAVORITES = 2
        private const val TAB_TYPE_STATUSES = "statuses"
        private const val TAB_TYPE_STATUSES_WITH_REPLIES = "statuses_with_replies"
        private const val TAB_TYPE_MEDIA = "media"
        private const val TAB_TYPE_FAVORITES = "favorites"

        private val ParcelableUser.hide_protected_contents: Boolean
            get() = user_type != AccountType.MASTODON && is_protected
    }
}
