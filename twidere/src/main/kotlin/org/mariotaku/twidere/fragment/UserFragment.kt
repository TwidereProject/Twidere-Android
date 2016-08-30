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

import android.animation.ArgbEvaluator
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
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
import android.support.annotation.UiThread
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v4.view.WindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.util.Linkify
import android.util.Pair
import android.util.SparseBooleanArray
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import com.afollestad.appthemeengine.ATEActivity
import com.afollestad.appthemeengine.Config
import com.squareup.otto.Subscribe
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.UserEvent
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.header_user.*
import kotlinx.android.synthetic.main.header_user.view.*
import kotlinx.android.synthetic.main.layout_content_fragment_common.*
import kotlinx.android.synthetic.main.layout_content_pages_common.*
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.apache.commons.lang3.ObjectUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.FriendshipUpdate
import org.mariotaku.microblog.library.twitter.model.Relationship
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.iface.IExtendedActivity
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.fragment.AbsStatusesFragment.StatusesFragmentDelegate
import org.mariotaku.twidere.fragment.UserTimelineFragment.UserTimelineFragmentDelegate
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.graphic.ActionBarColorDrawable
import org.mariotaku.twidere.graphic.ActionIconDrawable
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.message.FriendshipTaskEvent
import org.mariotaku.twidere.model.message.FriendshipUpdatedEvent
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent
import org.mariotaku.twidere.model.message.TaskStateChangedEvent
import org.mariotaku.twidere.model.util.*
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener
import org.mariotaku.twidere.util.UserColorNameManager.UserColorChangedListener
import org.mariotaku.twidere.util.UserColorNameManager.UserNicknameChangedListener
import org.mariotaku.twidere.util.menu.TwidereMenuInfo
import org.mariotaku.twidere.util.support.ActivitySupport
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat
import org.mariotaku.twidere.util.support.ViewSupport
import org.mariotaku.twidere.util.support.WindowSupport
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback
import org.mariotaku.twidere.view.TabPagerIndicator
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener
import java.util.*

class UserFragment : BaseSupportFragment(), OnClickListener, OnLinkClickListener,
        OnSizeChangedListener, OnTouchListener, DrawerCallback, SupportFragmentCallback,
        SystemWindowsInsetsCallback, RefreshScrollTopInterface, OnPageChangeListener,
        KeyboardShortcutCallback, UserColorChangedListener, UserNicknameChangedListener,
        IToolBarSupportFragment, StatusesFragmentDelegate, UserTimelineFragmentDelegate {

    override val toolbar: Toolbar
        get() = profileContentContainer.toolbar

    private var mActionBarBackground: ActionBarDrawable? = null
    private var pagerAdapter: SupportTabsAdapter? = null

    // Data fields
    var user: ParcelableUser? = null
        private set
    private var account: ParcelableAccount? = null
    private var relationship: UserRelationship? = null
    private var locale: Locale? = null
    private var mGetUserInfoLoaderInitialized: Boolean = false
    private var mGetFriendShipLoaderInitialized: Boolean = false
    private var mBannerWidth: Int = 0
    private var cardBackgroundColor: Int = 0
    private var actionBarShadowColor: Int = 0
    private var uiColor: Int = 0
    private var mPrimaryColor: Int = 0
    private var mPrimaryColorDark: Int = 0
    private var nameFirst: Boolean = false
    private var mPreviousTabItemIsDark: Int = 0
    private var mPreviousActionBarItemIsDark: Int = 0
    private var mHideBirthdayView: Boolean = false
    private var userEvent: UserEvent? = null

    private val mFriendshipLoaderCallbacks = object : LoaderCallbacks<SingleResponse<UserRelationship>> {

        override fun onCreateLoader(id: Int, args: Bundle): Loader<SingleResponse<UserRelationship>> {
            invalidateOptionsMenu()
            val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            val user = args.getParcelable<ParcelableUser>(EXTRA_USER)
            if (user != null && user.key == accountKey) {
                followingYouIndicator.visibility = View.GONE
                followContainer.follow.visibility = View.VISIBLE
                followProgress!!.visibility = View.VISIBLE
            } else {
                followingYouIndicator.visibility = View.GONE
                followContainer.follow.visibility = View.GONE
                followProgress!!.visibility = View.VISIBLE
            }
            return UserRelationshipLoader(activity, accountKey, user)
        }

        override fun onLoaderReset(loader: Loader<SingleResponse<UserRelationship>>) {

        }

        override fun onLoadFinished(loader: Loader<SingleResponse<UserRelationship>>,
                                    data: SingleResponse<UserRelationship>) {
            followProgress!!.visibility = View.GONE
            val relationship = data.data
            displayRelationship(user, relationship)
            updateOptionsMenuVisibility()
        }

    }
    private val mUserInfoLoaderCallbacks = object : LoaderCallbacks<SingleResponse<ParcelableUser>> {

        override fun onCreateLoader(id: Int, args: Bundle): Loader<SingleResponse<ParcelableUser>> {
            val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
            val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            val userId = args.getParcelable<UserKey>(EXTRA_USER_KEY)
            val screenName = args.getString(EXTRA_SCREEN_NAME)
            if (user == null && (!omitIntentExtra || !args.containsKey(EXTRA_USER))) {
                cardContent.visibility = View.GONE
                errorContainer.visibility = View.GONE
                progressContainer.visibility = View.VISIBLE
                errorText.text = null
                errorText.visibility = View.GONE
            }
            val user = this@UserFragment.user
            val loadFromCache = user == null || !user.is_cache && user.key.maybeEquals(userId)
            return ParcelableUserLoader(activity, accountKey, userId, screenName, arguments,
                    omitIntentExtra, loadFromCache)
        }

        override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableUser>>) {

        }

        override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableUser>>,
                                    data: SingleResponse<ParcelableUser>) {
            val activity = activity ?: return
            if (data.data != null) {
                val user = data.data
                cardContent.visibility = View.VISIBLE
                errorContainer.visibility = View.GONE
                progressContainer.visibility = View.GONE
                val account = data.extras.getParcelable<ParcelableAccount>(EXTRA_ACCOUNT)
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
            } else if (user != null && user!!.is_cache) {
                cardContent.visibility = View.VISIBLE
                errorContainer.visibility = View.GONE
                progressContainer.visibility = View.GONE
                displayUser(user, account)
                updateOptionsMenuVisibility()
            } else {
                if (data.hasException()) {
                    errorText.text = Utils.getErrorMessage(activity, data.exception)
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
    override val pinnedStatusIds: Array<String>?
        get() = user?.extras?.pinned_status_ids

    private fun updateOptionsMenuVisibility() {
        setHasOptionsMenu(user != null && relationship != null)
    }

    private fun displayRelationship(user: ParcelableUser?,
                                    userRelationship: UserRelationship?) {
        if (user == null) {
            relationship = null
            return
        }
        if (user.account_key.maybeEquals(user.key)) {
            followContainer.follow.setText(R.string.edit)
            followContainer.follow.visibility = View.VISIBLE
            relationship = userRelationship
            return
        }
        if (userRelationship == null || !userRelationship.check(user)) {
            relationship = null
            return
        } else {
            relationship = userRelationship
        }
        invalidateOptionsMenu()
        followContainer.follow.isEnabled = userRelationship.blocking || !userRelationship.blocked_by
        if (userRelationship.blocked_by) {
            pagesErrorContainer!!.visibility = View.GONE
            pagesErrorText!!.text = null
            pagesContent!!.visibility = View.VISIBLE
        } else if (!userRelationship.following && user.is_protected) {
            pagesErrorContainer!!.visibility = View.VISIBLE
            pagesErrorText!!.setText(R.string.user_protected_summary)
            pagesErrorIcon!!.setImageResource(R.drawable.ic_info_locked)
            pagesContent!!.visibility = View.GONE
        } else {
            pagesErrorContainer!!.visibility = View.GONE
            pagesErrorText!!.text = null
            pagesContent!!.visibility = View.VISIBLE
        }
        if (userRelationship.blocking) {
            followContainer.follow.setText(R.string.unblock)
        } else if (userRelationship.following) {
            followContainer.follow.setText(R.string.unfollow)
        } else if (user.is_follow_request_sent) {
            followContainer.follow.setText(R.string.requested)
        } else {
            followContainer.follow.setText(R.string.follow)
        }
        followContainer.follow.compoundDrawablePadding = Math.round(followContainer.follow.textSize * 0.25f)
        followingYouIndicator.visibility = if (userRelationship.followed_by) View.VISIBLE else View.GONE

        val task = CacheUserInfoTask(context.applicationContext)
        task.setParams(Pair.create(user, userRelationship))
        TaskStarter.execute(task)
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
        v!!.getLocationInWindow(location)
        return x >= location[0] && x <= location[0] + v.width
                && y >= location[1] && y <= location[1] + v.height
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        updateSubtitle()
    }

    private fun updateSubtitle() {
        val activity = activity as AppCompatActivity
        val actionBar = activity.supportActionBar ?: return
        val user = this.user
        if (user == null) {
            actionBar.subtitle = null
            return
        }
        val spec = pagerAdapter!!.getTab(viewPager.currentItem)
        assert(spec.type != null)
        when (spec.type) {
            TAB_TYPE_STATUSES -> {
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
                if (preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
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

    override fun onPageScrollStateChanged(state: Int) {

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
    fun displayUser(user: ParcelableUser?, account: ParcelableAccount?) {
        val activity = activity ?: return
        this.user = user
        this.account = account
        if (user == null || user.key == null) {
            profileImage.visibility = View.GONE
            profileType!!.visibility = View.GONE
            if (activity is ATEActivity) {
                setUiColor(Config.primaryColor(activity, activity.ateKey))
            }
            return
        }
        val adapter = pagerAdapter!!
        for (i in 0 until adapter.count) {
            val sf = adapter.instantiateItem(viewPager, i) as? AbsStatusesFragment
            sf?.initLoaderIfNeeded()
        }
        profileImage.visibility = View.VISIBLE
        val resources = resources
        val lm = loaderManager
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        cardContent!!.visibility = View.VISIBLE
        errorContainer!!.visibility = View.GONE
        progressContainer.visibility = View.GONE
        this.user = user
        profileImage.setBorderColor(if (user.color != 0) user.color else Color.WHITE)
        profileNameContainer.drawEnd(user.account_color)
        profileNameContainer.name.text = bidiFormatter.unicodeWrap(if (TextUtils.isEmpty(user.nickname)) user.name else getString(R.string.name_with_nickname, user.name, user.nickname))
        val typeIconRes = Utils.getUserTypeIconRes(user.is_verified, user.is_protected)
        if (typeIconRes != 0) {
            profileType.setImageResource(typeIconRes)
            profileType.visibility = View.VISIBLE
        } else {
            profileType.setImageDrawable(null)
            profileType.visibility = View.GONE
        }
        profileNameContainer.screenName.text = "@${user.screen_name}"
        val linkify = TwidereLinkify(this)
        if (user.description_unescaped != null) {
            val text = SpannableStringBuilder.valueOf(user.description_unescaped)
            ParcelableStatusUtils.applySpans(text, user.description_spans)
            linkify.applyAllLinks(text, user.account_key, false, false)
            descriptionContainer.description.text = text
        } else {
            descriptionContainer.description.text = user.description_plain
            Linkify.addLinks(descriptionContainer.description, Linkify.WEB_URLS)
        }
        descriptionContainer.visibility = if (descriptionContainer.description.empty) View.GONE else View.VISIBLE

        locationContainer.visibility = if (TextUtils.isEmpty(user.location)) View.GONE else View.VISIBLE
        locationContainer.location.text = user.location
        urlContainer.visibility = if (TextUtils.isEmpty(user.url) && TextUtils.isEmpty(user.url_expanded)) View.GONE else View.VISIBLE
        urlContainer.url.text = if (TextUtils.isEmpty(user.url_expanded)) user.url else user.url_expanded
        val createdAt = Utils.formatToLongTimeString(activity, user.created_at)
        val daysSinceCreation = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24.toFloat()
        val dailyTweets = Math.round(user.statuses_count / Math.max(1f, daysSinceCreation))
        createdAtContainer.createdAt.text = resources.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                createdAt, dailyTweets)
        listedContainer.listedCount.text = Utils.getLocalizedNumber(locale, user.listed_count)
        val groupsCount = if (user.extras != null) user.extras.groups_count else -1
        groupsContainer.groupsCount.text = Utils.getLocalizedNumber(locale, groupsCount)
        followersContainer.followersCount!!.text = Utils.getLocalizedNumber(locale, user.followers_count)
        friendsContainer.friendsCount!!.text = Utils.getLocalizedNumber(locale, user.friends_count)

        listedContainer.visibility = if (user.listed_count < 0) View.GONE else View.VISIBLE
        groupsContainer.visibility = if (groupsCount < 0) View.GONE else View.VISIBLE

        mediaLoader.displayOriginalProfileImage(profileImage, user)
        if (user.color != 0) {
            setUiColor(user.color)
        } else if (user.link_color != 0) {
            setUiColor(user.link_color)
        } else if (activity is ATEActivity) {
            setUiColor(Config.primaryColor(activity, activity.ateKey))
        }
        val defWidth = resources.displayMetrics.widthPixels
        val width = if (mBannerWidth > 0) mBannerWidth else defWidth
        val bannerUrl = ParcelableUserUtils.getProfileBannerUrl(user)
        if (ObjectUtils.notEqual(profileBanner.tag, bannerUrl) || profileBanner.drawable == null) {
            profileBanner.tag = bannerUrl
            mediaLoader.displayProfileBanner(profileBanner, bannerUrl, width)
        }
        val relationship = relationship
        if (relationship == null) {
            getFriendship()
        }
        activity.title = UserColorNameManager.decideDisplayName(user.nickname, user.name,
                user.screen_name, nameFirst)

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        cal.timeInMillis = user.created_at
        if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.DAY_OF_MONTH) == currentDay && !mHideBirthdayView) {
            profileBirthdayBanner.visibility = View.VISIBLE
        } else {
            profileBirthdayBanner.visibility = View.GONE
        }
        updateTitleAlpha()
        invalidateOptionsMenu()
        updateSubtitle()
    }

    override val currentVisibleFragment: Fragment?
        get() {
            val currentItem = viewPager.currentItem
            if (currentItem < 0 || currentItem >= pagerAdapter!!.count) return null
            return pagerAdapter!!.instantiateItem(viewPager, currentItem) as Fragment
        }

    override fun triggerRefresh(position: Int): Boolean {
        return false
    }

    override fun getSystemWindowsInsets(insets: Rect): Boolean {
        return false
    }

    fun getUserInfo(accountKey: UserKey, userKey: UserKey?, screenName: String?,
                    omitIntentExtra: Boolean) {
        val lm = loaderManager
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
        args.putParcelable(EXTRA_USER_KEY, userKey)
        args.putString(EXTRA_SCREEN_NAME, screenName)
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra)
        if (!mGetUserInfoLoaderInitialized) {
            lm.initLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks)
            mGetUserInfoLoaderInitialized = true
        } else {
            lm.restartLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks)
        }
        if (userKey == null && screenName == null) {
            cardContent!!.visibility = View.GONE
            errorContainer!!.visibility = View.GONE
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
        invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val user = user
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                if (user == null) return
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return
                    val color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT)
                    userColorNameManager.setUserColor(this.user!!.key, color)
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    userColorNameManager.clearUserColor(this.user!!.key)
                }
            }
            REQUEST_ADD_TO_LIST -> {
                if (user == null) return
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val twitter = twitterWrapper
                    val list = data.getParcelableExtra<ParcelableUserList>(EXTRA_USER_LIST) ?: return
                    twitter.addUserListMembersAsync(user.account_key, list.id, user)
                }
            }
            REQUEST_SELECT_ACCOUNT -> {
                if (user == null) return
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    @Referral
                    val referral = arguments.getString(EXTRA_REFERRAL)
                    IntentUtils.openUserProfile(activity, accountKey, user.key, user.screen_name,
                            null, preferences.getBoolean(KEY_NEW_DOCUMENT_API), referral)
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        userColorNameManager.registerColorChangedListener(this)
        userColorNameManager.registerNicknameChangedListener(this)
        nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        locale = resources.configuration.locale
        cardBackgroundColor = ThemeUtils.getCardBackgroundColor(activity,
                ThemeUtils.getThemeBackgroundOption(activity),
                ThemeUtils.getUserThemeBackgroundAlpha(activity))
        actionBarShadowColor = 0xA0000000.toInt()
        val args = arguments
        val accountId: UserKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userId: UserKey? = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName: String? = args.getString(EXTRA_SCREEN_NAME)

        Utils.setNdefPushMessageCallback(activity, CreateNdefMessageCallback {
            val user = user ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(LinkCreator.getUserWebLink(user))))
        })


        userFragmentView.setWindowInsetsListener { left, top, right, bottom ->
            profileContentContainer.setPadding(0, top, 0, 0)
            profileBannerSpace.statusBarHeight = top

            if (profileBannerSpace.toolbarHeight == 0) {
                var toolbarHeight = toolbar.measuredHeight
                if (toolbarHeight == 0) {
                    toolbarHeight = ThemeUtils.getActionBarHeight(context)
                }
                profileBannerSpace.toolbarHeight = toolbarHeight
            }
        }
        profileContentContainer.setOnSizeChangedListener { view, w, h, oldw, oldh ->
            val toolbarHeight = toolbar.measuredHeight
            userProfileDrawer.setPadding(0, toolbarHeight, 0, 0)
            profileBannerSpace.toolbarHeight = toolbarHeight
        }

        userProfileDrawer.setDrawerCallback(this)

        pagerAdapter = SupportTabsAdapter(activity, childFragmentManager)

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pagerAdapter
        toolbarTabs.setViewPager(viewPager)
        toolbarTabs.setTabDisplayOption(TabPagerIndicator.LABEL)
        toolbarTabs.setOnPageChangeListener(this)

        followContainer.follow.setOnClickListener(this)
        profileImage.setOnClickListener(this)
        profileBanner.setOnClickListener(this)
        listedContainer.setOnClickListener(this)
        groupsContainer.setOnClickListener(this)
        followersContainer.setOnClickListener(this)
        friendsContainer.setOnClickListener(this)
        errorIcon.setOnClickListener(this)
        profileBirthdayBanner.setOnClickListener(this)
        profileBanner.setOnSizeChangedListener(this)
        profileBannerSpace.setOnTouchListener(this)


        profileNameBackground.setBackgroundColor(cardBackgroundColor)
        profileDetailsContainer.setBackgroundColor(cardBackgroundColor)
        toolbarTabs.setBackgroundColor(cardBackgroundColor)

        val actionBarElevation = ThemeUtils.getSupportActionBarElevation(activity)
        ViewCompat.setElevation(toolbarTabs, actionBarElevation)

        setupBaseActionBar()
        setupUserPages()

        getUserInfo(accountId, userId, screenName, false)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)

        @Referral
        val referral = arguments.getString(EXTRA_REFERRAL)
        val context = context
        if (userEvent == null) {
            userEvent = UserEvent.create(context, referral)
        } else {
            userEvent!!.markStart(context)
        }
    }

    override fun onStop() {
        val context = context
        if (userEvent != null && context != null && user != null) {
            userEvent!!.setUser(user!!)
            userEvent!!.markEnd()
            HotMobiLogger.getInstance(context).log<UserEvent>(userEvent)
        }
        bus.unregister(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        setUiColor(uiColor)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putParcelable(EXTRA_USER, user)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        user = null
        relationship = null
        val lm = loaderManager
        lm.destroyLoader(LOADER_ID_USER)
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_profile, menu)
    }

    @UiThread
    override fun onPrepareOptionsMenu(menu: Menu?) {
        val user = user ?: return

        val isMyself = user.account_key.maybeEquals(user.key)
        val mentionItem = menu!!.findItem(R.id.mention)
        if (mentionItem != null) {
            val displayName = UserColorNameManager.decideDisplayName(user.nickname,
                    user.name, user.screen_name, nameFirst)
            mentionItem.title = getString(R.string.mention_user_name, displayName)
        }
        MenuUtils.setItemAvailability(menu, R.id.mention, !isMyself)
        MenuUtils.setItemAvailability(menu, R.id.incoming_friendships, isMyself)
        MenuUtils.setItemAvailability(menu, R.id.saved_searches, isMyself)
        MenuUtils.setItemAvailability(menu, R.id.scheduled_statuses, isMyself && MicroBlogAPIFactory.getOfficialKeyType(activity, user.account_key) == ConsumerKeyType.TWEETDECK)
        MenuUtils.setItemAvailability(menu, R.id.muted_users, isMyself)
        MenuUtils.setItemAvailability(menu, R.id.blocked_users, isMyself)

        MenuUtils.setItemAvailability(menu, R.id.block, !isMyself)
        MenuUtils.setItemAvailability(menu, R.id.mute_user, !isMyself)
        MenuUtils.setItemAvailability(menu, R.id.report_spam, !isMyself)
        MenuUtils.setItemAvailability(menu, R.id.enable_retweets, !isMyself)
        if (account != null) {
            MenuUtils.setItemAvailability(menu, R.id.add_to_list, TextUtils.equals(ParcelableAccount.Type.TWITTER,
                    ParcelableAccountUtils.getAccountType(account!!)))
        } else {
            MenuUtils.setItemAvailability(menu, R.id.add_to_list, false)
        }

        val userRelationship = relationship
        if (userRelationship != null) {

            val filterItem = menu.findItem(R.id.add_to_filter)
            if (filterItem != null) {
                filterItem.isChecked = userRelationship.filtering
            }
            if (isMyself) {
                MenuUtils.setItemAvailability(menu, R.id.send_direct_message, false)
            } else {
                MenuUtils.setItemAvailability(menu, R.id.send_direct_message, userRelationship.can_dm)
                MenuUtils.setItemAvailability(menu, R.id.block, true)
                val blockItem = menu.findItem(R.id.block)
                if (blockItem != null) {
                    ActionIconDrawable.setMenuHighlight(blockItem, TwidereMenuInfo(userRelationship.blocking))
                    blockItem.setTitle(if (userRelationship.blocking) R.string.unblock else R.string.block)
                }
                val muteItem = menu.findItem(R.id.mute_user)
                if (muteItem != null) {
                    muteItem.isChecked = userRelationship.muting
                }
                val wantRetweetsItem = menu.findItem(R.id.enable_retweets)
                if (wantRetweetsItem != null) {
                    wantRetweetsItem.isChecked = userRelationship.retweet_enabled
                }
            }
        } else {
            MenuUtils.setItemAvailability(menu, R.id.send_direct_message, false)
        }
        val intent = Intent(INTENT_ACTION_EXTENSION_OPEN_USER)
        val extras = Bundle()
        extras.putParcelable(EXTRA_USER, user)
        intent.putExtras(extras)
        menu.removeGroup(MENU_GROUP_USER_EXTENSION)
        MenuUtils.addIntentToMenu(activity, menu, intent, MENU_GROUP_USER_EXTENSION)
        val drawer = userProfileDrawer
        if (drawer != null) {
            val offset = drawer.paddingTop - drawer.headerTop
            mPreviousActionBarItemIsDark = 0
            mPreviousTabItemIsDark = 0
            updateScrollOffset(offset)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val context = context
        val twitter = twitterWrapper
        val user = user
        val userRelationship = relationship
        if (user == null) return false
        when (item!!.itemId) {
            R.id.block -> {
                if (userRelationship == null) return true
                if (userRelationship.blocking) {
                    twitter.destroyBlockAsync(user.account_key, user.key)
                } else {
                    CreateUserBlockDialogFragment.show(fragmentManager, user)
                }
            }
            R.id.report_spam -> {
                ReportSpamDialogFragment.show(fragmentManager, user)
            }
            R.id.add_to_filter -> {
                if (userRelationship == null) return true
                val cr = contentResolver
                if (userRelationship.filtering) {
                    val where = Expression.equalsArgs(Filters.Users.USER_KEY).sql
                    val whereArgs = arrayOf(user.key.toString())
                    cr.delete(Filters.Users.CONTENT_URI, where, whereArgs)
                    Utils.showInfoMessage(activity, R.string.message_user_unmuted, false)
                } else {
                    cr.insert(Filters.Users.CONTENT_URI, ContentValuesCreator.createFilteredUser(user))
                    Utils.showInfoMessage(activity, R.string.message_user_muted, false)
                }
                getFriendship()
            }
            R.id.mute_user -> {
                if (userRelationship == null) return true
                if (userRelationship.muting) {
                    twitter.destroyMuteAsync(user.account_key, user.key)
                } else {
                    CreateUserMuteDialogFragment.show(fragmentManager, user)
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
                val builder = Uri.Builder()
                builder.scheme(SCHEME_TWIDERE)
                builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION)
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, user.account_key.toString())
                builder.appendQueryParameter(QUERY_PARAM_USER_KEY, user.key.toString())
                val intent = Intent(Intent.ACTION_VIEW, builder.build())
                intent.putExtra(EXTRA_ACCOUNT, ParcelableCredentialsUtils.getCredentials(activity, user.account_key))
                intent.putExtra(EXTRA_USER, user)
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
                SetUserNicknameDialogFragment.show(fragmentManager, user.key, nick)
            }
            R.id.add_to_list -> {
                promiseOnUi {
                    executeAfterFragmentResumed {
                        ProgressDialogFragment.show(fragmentManager, "get_list_progress")
                    }
                }.then {
                    val microBlog = MicroBlogAPIFactory.getInstance(context, user.account_key, true)

                    val ownedLists = microBlog.getUserListOwnerships(null)
                    val userListMemberships = microBlog.getUserListMemberships(user.key.id, null, true)
                    return@then Array<ParcelableUserList>(ownedLists.size) { idx ->
                        val list = ParcelableUserListUtils.from(ownedLists[idx], user.account_key)
                        list.is_user_inside = userListMemberships.firstOrNull { it.id == ownedLists[idx].id } != null
                        return@Array list
                    }
                }.alwaysUi {
                    executeAfterFragmentResumed {
                        val df = fragmentManager.findFragmentByTag("get_list_progress") as? DialogFragment
                        df?.dismiss()
                    }
                }.successUi { result ->
                    executeAfterFragmentResumed {
                        val df = AddRemoveUserListDialogFragment()
                        df.arguments = Bundle {
                            this[EXTRA_ACCOUNT_KEY] = user.account_key
                            this[EXTRA_USER_KEY] = user.key
                            this[EXTRA_USER_LISTS] = result
                        }
                        df.show(fragmentManager, "add_remove_list")
                    }
                }.failUi {
                    Utils.showErrorMessage(context, R.string.action_getting_user_lists, it, false)
                }
            }
            R.id.open_with_account -> {
                val intent = Intent(INTENT_ACTION_SELECT_ACCOUNT)
                intent.setClass(activity, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                intent.putExtra(EXTRA_ACCOUNT_HOST, user.key.host)
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
            }
            R.id.follow -> {
                if (userRelationship == null) return true
                val updatingRelationship = twitter.isUpdatingRelationship(user.account_key,
                        user.key)
                if (!updatingRelationship) {
                    if (userRelationship.following) {
                        DestroyFriendshipDialogFragment.show(fragmentManager, user)
                    } else {
                        twitter.createFriendshipAsync(user.account_key, user.key)
                    }
                }
                return true
            }
            R.id.enable_retweets -> {
                val newState = !item.isChecked
                val update = FriendshipUpdate()
                update.retweets(newState)
                twitter.updateFriendship(user.account_key, user.key, update)
                item.isChecked = newState
                return true
            }
            R.id.muted_users -> {
                IntentUtils.openMutesUsers(activity, user.account_key)
                return true
            }
            R.id.blocked_users -> {
                IntentUtils.openUserBlocks(activity, user.account_key)
                return true
            }
            R.id.incoming_friendships -> {
                IntentUtils.openIncomingFriendships(activity, user.account_key)
                return true
            }
            R.id.user_mentions -> {
                IntentUtils.openUserMentions(context, user.account_key, user.screen_name)
                return true
            }
            R.id.saved_searches -> {
                IntentUtils.openSavedSearches(context, user.account_key)
                return true
            }
            R.id.scheduled_statuses -> {
                IntentUtils.openScheduledStatuses(context, user.account_key)
                return true
            }
            R.id.open_in_browser -> {
                val uri = LinkCreator.getUserWebLink(user)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
                if (intent.resolveActivity(context.packageManager) != null) {
                    startActivity(intent)
                }
                return true
            }
            else -> {
                val intent = item.intent
                if (intent != null && intent.resolveActivity(context.packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
        return true
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = host
        if (host is AppCompatActivity) {
            host.setSupportActionBar(toolbar)
        }
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                ACTION_NAVIGATION_PREVIOUS_TAB -> {
                    val previous = viewPager.currentItem - 1
                    if (previous >= 0 && previous < pagerAdapter!!.count) {
                        viewPager.setCurrentItem(previous, true)
                    }
                    return true
                }
                ACTION_NAVIGATION_NEXT_TAB -> {
                    val next = viewPager.currentItem + 1
                    if (next >= 0 && next < pagerAdapter!!.count) {
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

    override fun fitSystemWindows(insets: Rect) {
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
        val activity = activity
        val user = user
        if (activity == null || user == null) return
        when (view.id) {
            R.id.errorContainer -> {
                getUserInfo(true)
            }
            R.id.follow -> {
                if (user.account_key.maybeEquals(user.key)) {
                    IntentUtils.openProfileEditor(getActivity(), user.account_key)
                } else {
                    val userRelationship = relationship
                    val twitter = twitterWrapper
                    if (userRelationship == null) return
                    if (userRelationship.blocking) {
                        twitter.destroyBlockAsync(user.account_key, user.key)
                    } else if (userRelationship.following) {
                        DestroyFriendshipDialogFragment.show(fragmentManager, user)
                    } else {
                        twitter.createFriendshipAsync(user.account_key, user.key)
                    }
                }
            }
            R.id.profileImage -> {
                val url = Utils.getOriginalTwitterProfileImage(user.profile_image_url)
                val profileImage = ParcelableMediaUtils.image(url)
                profileImage.type = ParcelableMedia.Type.IMAGE
                val media = arrayOf(profileImage)
                IntentUtils.openMedia(activity, user.account_key, false, null, media, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API))
            }
            R.id.profileBanner -> {
                val bannerUrl = ParcelableUserUtils.getProfileBannerUrl(user) ?: return
                val url = InternalTwitterContentUtils.getBestBannerUrl(bannerUrl,
                        Integer.MAX_VALUE)
                val profileBanner = ParcelableMediaUtils.image(url)
                profileBanner.type = ParcelableMedia.Type.IMAGE
                val media = arrayOf(profileBanner)
                IntentUtils.openMedia(activity, user.account_key, false, null, media, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API))
            }
            R.id.listedContainer -> {
                IntentUtils.openUserLists(getActivity(), user.account_key, user.key,
                        user.screen_name)
            }
            R.id.groupsContainer -> {
                IntentUtils.openUserGroups(getActivity(), user.account_key, user.key,
                        user.screen_name)
            }
            R.id.followersContainer -> {
                IntentUtils.openUserFollowers(getActivity(), user.account_key, user.key,
                        user.screen_name)
            }
            R.id.friendsContainer -> {
                IntentUtils.openUserFriends(getActivity(), user.account_key, user.key,
                        user.screen_name)
            }
            R.id.nameContainer -> {
                if (user.account_key == user.key) return
                IntentUtils.openProfileEditor(getActivity(), user.account_key)
            }
            R.id.profileBirthdayBanner -> {
                mHideBirthdayView = true
                profileBirthdayBanner.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out))
                profileBirthdayBanner.visibility = View.GONE
            }
        }

    }

    override fun onLinkClick(link: String, orig: String?, accountKey: UserKey?,
                             extraId: Long, type: Int, sensitive: Boolean,
                             start: Int, end: Int): Boolean {
        val user = user ?: return false
        when (type) {
            TwidereLinkify.LINK_TYPE_MENTION -> {
                IntentUtils.openUserProfile(activity, user.account_key, null, link, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API), Referral.USER_MENTION)
                return true
            }
            TwidereLinkify.LINK_TYPE_HASHTAG -> {
                IntentUtils.openTweetSearch(activity, user.account_key, "#" + link)
                return true
            }
            TwidereLinkify.LINK_TYPE_LINK_IN_TEXT, TwidereLinkify.LINK_TYPE_ENTITY_URL -> {
                val uri = Uri.parse(link)
                val intent: Intent
                if (uri.scheme != null) {
                    intent = Intent(Intent.ACTION_VIEW, uri)
                } else {
                    intent = Intent(Intent.ACTION_VIEW, uri.buildUpon().scheme("http").build())
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

    override fun onUserNicknameChanged(userId: UserKey, nick: String) {
        if (user == null || user!!.key != userId) return
        displayUser(user, account)
    }

    override fun onUserColorChanged(userId: UserKey, color: Int) {
        if (user == null || user!!.key != userId) return
        displayUser(user, account)
    }

    override fun onSizeChanged(view: View, w: Int, h: Int, oldw: Int, oldh: Int) {
        mBannerWidth = w
        if (w != oldw || h != oldh) {
            requestFitSystemWindows()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (profileBirthdayBanner.visibility == View.VISIBLE) {
            return profileBirthdayBanner.dispatchTouchEvent(event)
        }
        return profileBanner.dispatchTouchEvent(event)
    }

    override fun scrollToStart(): Boolean {
        val fragment = currentVisibleFragment
        if (fragment !is RefreshScrollTopInterface) return false
        fragment.scrollToStart()
        return true
    }

    override fun triggerRefresh(): Boolean {
        val fragment = currentVisibleFragment
        if (fragment !is RefreshScrollTopInterface) return false
        fragment.triggerRefresh()
        return true
    }

    private fun getFriendship() {
        val user = user ?: return
        relationship = null
        val lm = loaderManager
        lm.destroyLoader(LOADER_ID_FRIENDSHIP)
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key)
        args.putParcelable(EXTRA_USER, user)
        if (!mGetFriendShipLoaderInitialized) {
            lm.initLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks)
            mGetFriendShipLoaderInitialized = true
        } else {
            lm.restartLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks)
        }
    }

    private fun getUserInfo(omitIntentExtra: Boolean) {
        val user = this.user ?: return
        getUserInfo(user.account_key, user.key, user.screen_name, omitIntentExtra)
    }

    private fun setUiColor(color: Int) {
        uiColor = color
        mPreviousActionBarItemIsDark = 0
        mPreviousTabItemIsDark = 0
        if (mActionBarBackground == null) {
            setupBaseActionBar()
        }
        val activity = activity as BaseActivity
        if (Config.coloredActionBar(activity, activity.ateKey)) {
            mPrimaryColor = color
            mPrimaryColorDark = ThemeUtils.computeDarkColor(color)
        } else {
            mPrimaryColor = Config.primaryColor(activity, activity.ateKey)
            mPrimaryColorDark = Color.BLACK
        }
        if (mActionBarBackground != null) {
            mActionBarBackground!!.color = mPrimaryColor
        }
        val taskColor: Int
        if (Config.coloredActionBar(activity, activity.ateKey)) {
            taskColor = color
        } else {
            taskColor = Config.toolbarColor(activity, activity.ateKey, toolbar)
        }
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

        toolbarTabs.setBackgroundColor(mPrimaryColor)

        val drawer = userProfileDrawer
        if (drawer != null) {
            val offset = drawer.paddingTop - drawer.headerTop
            updateScrollOffset(offset)
        }
    }

    private fun setupBaseActionBar() {
        val activity = activity
        if (activity !is LinkHandlerActivity) return
        val actionBar = activity.supportActionBar ?: return
        val shadow = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.shadow_user_banner_action_bar, null)
        mActionBarBackground = ActionBarDrawable(shadow!!)
        if (!ThemeUtils.isWindowFloating(activity) && ThemeUtils.isTransparentBackground(activity.currentThemeBackgroundOption)) {
            //            mActionBarBackground.setAlpha(ThemeUtils.getActionBarAlpha(linkHandler.getCurrentThemeBackgroundAlpha()));
            profileBanner.alpha = activity.currentThemeBackgroundAlpha / 255f
        }
        actionBar.setBackgroundDrawable(mActionBarBackground)
    }

    private fun setupUserPages() {
        val args = arguments
        val tabArgs = Bundle()
        val user = args.getParcelable<ParcelableUser>(EXTRA_USER)
        if (user != null) {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key)
            tabArgs.putParcelable(EXTRA_USER_KEY, user.key)
            tabArgs.putString(EXTRA_SCREEN_NAME, user.screen_name)
        } else {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, args.getParcelable<Parcelable>(EXTRA_ACCOUNT_KEY))
            tabArgs.putParcelable(EXTRA_USER_KEY, args.getParcelable<Parcelable>(EXTRA_USER_KEY))
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME))
        }
        pagerAdapter!!.addTab(UserTimelineFragment::class.java, tabArgs, getString(R.string.statuses),
                R.drawable.ic_action_quote, TAB_TYPE_STATUSES, TAB_POSITION_STATUSES, null)
        pagerAdapter!!.addTab(UserMediaTimelineFragment::class.java, tabArgs, getString(R.string.media),
                R.drawable.ic_action_gallery, TAB_TYPE_MEDIA, TAB_POSITION_MEDIA, null)
        if (preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
            pagerAdapter!!.addTab(UserFavoritesFragment::class.java, tabArgs, getString(R.string.favorites),
                    R.drawable.ic_action_star, TAB_TYPE_FAVORITES, TAB_POSITION_FAVORITES, null)
        } else {
            pagerAdapter!!.addTab(UserFavoritesFragment::class.java, tabArgs, getString(R.string.likes),
                    R.drawable.ic_action_heart, TAB_TYPE_FAVORITES, TAB_POSITION_FAVORITES, null)
        }
    }

    private fun updateScrollOffset(offset: Int) {
        val space = profileBannerSpace
        val profileBannerView = profileBanner
        val profileBirthdayBannerView = profileBirthdayBanner
        val profileBannerContainer = profileBannerContainer
        val spaceHeight = space!!.height
        val factor = TwidereMathUtils.clamp(if (spaceHeight == 0) 0f else offset / spaceHeight.toFloat(), 0f, 1f)
        profileBannerContainer!!.translationY = (-offset).toFloat()
        profileBannerView!!.translationY = (offset / 2).toFloat()
        profileBirthdayBannerView!!.translationY = (offset / 2).toFloat()

        val activity = activity as BaseActivity


        val statusBarColor = sArgbEvaluator.evaluate(factor, 0xA0000000.toInt(),
                ThemeUtils.computeDarkColor(mPrimaryColorDark)) as Int
        val window = activity.window
        userFragmentView!!.setStatusBarColor(statusBarColor)
        ThemeUtils.setLightStatusBar(window, ThemeUtils.isLightColor(statusBarColor))
        val stackedTabColor = mPrimaryColor


        val profileContentHeight = (profileNameContainer!!.height + profileDetailsContainer.height).toFloat()
        val tabOutlineAlphaFactor: Float
        if (offset - spaceHeight > 0) {
            tabOutlineAlphaFactor = 1f - TwidereMathUtils.clamp((offset - spaceHeight) / profileContentHeight, 0f, 1f)
        } else {
            tabOutlineAlphaFactor = 1f
        }

        if (mActionBarBackground != null) {
            mActionBarBackground!!.setFactor(factor)
            mActionBarBackground!!.setOutlineAlphaFactor(tabOutlineAlphaFactor)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            windowOverlay!!.alpha = factor * tabOutlineAlphaFactor
            //            setCompatToolbarOverlayAlpha(activity, factor * tabOutlineAlphaFactor);
        }

        val currentTabColor = sArgbEvaluator.evaluate(tabOutlineAlphaFactor,
                stackedTabColor, cardBackgroundColor) as Int

        val tabBackground = toolbarTabs.background
        (tabBackground as ColorDrawable).color = currentTabColor
        val tabItemIsDark = ThemeUtils.isLightColor(currentTabColor)

        if (mPreviousTabItemIsDark == 0 || (if (tabItemIsDark) 1 else -1) != mPreviousTabItemIsDark) {
            val tabContrastColor = ThemeUtils.getColorDependent(currentTabColor)
            toolbarTabs.setIconColor(tabContrastColor)
            toolbarTabs.setLabelColor(tabContrastColor)
            if (Config.coloredActionBar(activity, activity.ateKey)) {
                toolbarTabs.setStripColor(tabContrastColor)
            } else {
                toolbarTabs.setStripColor(ThemeUtils.getOptimalAccentColor(uiColor,
                        tabContrastColor))
            }
            toolbarTabs.updateAppearance()
        }
        mPreviousTabItemIsDark = if (tabItemIsDark) 1 else -1

        val currentActionBarColor = sArgbEvaluator.evaluate(factor, actionBarShadowColor,
                stackedTabColor) as Int
        val actionItemIsDark = ThemeUtils.isLightColor(currentActionBarColor)
        if (mPreviousActionBarItemIsDark == 0 || (if (actionItemIsDark) 1 else -1) != mPreviousActionBarItemIsDark) {
            ThemeUtils.applyToolbarItemColor(activity, toolbar, currentActionBarColor)
        }
        mPreviousActionBarItemIsDark = if (actionItemIsDark) 1 else -1

        updateTitleAlpha()
    }

    private fun updateTitleAlpha() {
        val location = IntArray(2)
        profileNameContainer.name.getLocationInWindow(location)
        val nameShowingRatio = (userProfileDrawer.paddingTop - location[1]) / profileNameContainer.name.height.toFloat()
        val textAlpha = TwidereMathUtils.clamp(nameShowingRatio, 0f, 1f)
        val titleView = ViewSupport.findViewByText(toolbar, toolbar.title)
        if (titleView != null) {
            titleView.alpha = textAlpha
        }
        val subtitleView = ViewSupport.findViewByText(toolbar, toolbar.subtitle)
        if (subtitleView != null) {
            subtitleView.alpha = textAlpha
        }
    }

    override var controlBarOffset: Float
        get() {
            return 0f
        }
        set(value) {//Ignore
        }


    override val controlBarHeight: Int
        get() {
            return 0
        }

    override val shouldInitLoader: Boolean
        get() = user != null

    private class ActionBarDrawable(shadow: Drawable) : LayerDrawable(arrayOf(shadow, ActionBarColorDrawable.create(true))) {

        private val mShadowDrawable: Drawable
        private val mColorDrawable: ColorDrawable

        private var mFactor: Float = 0.toFloat()
        var color: Int = 0
            set(value) {
                field = value
                mColorDrawable.color = value
                setFactor(mFactor)
            }
        private var mAlpha: Int = 0
        private var mOutlineAlphaFactor: Float = 0.toFloat()

        init {
            mShadowDrawable = getDrawable(0)
            mColorDrawable = getDrawable(1) as ColorDrawable
            alpha = 0xFF
            setOutlineAlphaFactor(1f)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun getOutline(outline: Outline) {
            mColorDrawable.getOutline(outline)
            outline.alpha = mFactor * mOutlineAlphaFactor * 0.99f
        }

        override fun setAlpha(alpha: Int) {
            mAlpha = alpha
            setFactor(mFactor)
        }

        override fun getIntrinsicWidth(): Int {
            return mColorDrawable.intrinsicWidth
        }

        override fun getIntrinsicHeight(): Int {
            return mColorDrawable.intrinsicHeight
        }

        fun setFactor(f: Float) {
            mFactor = f
            val shadowAlpha = Math.round(mAlpha * TwidereMathUtils.clamp(1 - f, 0f, 1f))
            mShadowDrawable.alpha = shadowAlpha
            val hasColor = color != 0
            val colorAlpha = if (hasColor) Math.round(mAlpha * TwidereMathUtils.clamp(f, 0f, 1f)) else 0
            mColorDrawable.alpha = colorAlpha
            invalidateSelf()
        }

        fun setOutlineAlphaFactor(f: Float) {
            mOutlineAlphaFactor = f
            invalidateSelf()
        }

    }

    internal class UserRelationshipLoader(
            context: Context,
            private val accountKey: UserKey?,
            private val user: ParcelableUser?
    ) : AsyncTaskLoader<SingleResponse<UserRelationship>>(context) {

        override fun loadInBackground(): SingleResponse<UserRelationship> {
            if (accountKey == null || user == null) {
                return SingleResponse.Companion.getInstance<UserRelationship>(MicroBlogException("Null parameters"))
            }
            val userKey = user.key
            val isFiltering = DataStoreUtils.isFilteringUser(context, userKey)
            if (accountKey == user.key) {
                return SingleResponse.Companion.getInstance(UserRelationship(accountKey, userKey,
                        null, isFiltering))
            }
            val credentials = ParcelableCredentialsUtils.getCredentials(context,
                    accountKey) ?: return SingleResponse.Companion.getInstance<UserRelationship>(MicroBlogException("No Account"))
            if (MicroBlogAPIFactory.isStatusNetCredentials(credentials)) {
                if (!UserKeyUtils.isSameHost(accountKey, user.key)) {
                    return SingleResponse.Companion.getInstance(UserRelationship(user, isFiltering))
                }
            }
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey, false) ?: return SingleResponse.Companion.getInstance<UserRelationship>(MicroBlogException("No Account"))
            try {
                val relationship = twitter.showFriendship(user.key.id)
                if (relationship.isSourceBlockingTarget || relationship.isSourceBlockedByTarget) {
                    Utils.setLastSeen(context, userKey, -1)
                } else {
                    Utils.setLastSeen(context, userKey, System.currentTimeMillis())
                }
                Utils.updateRelationship(context, accountKey, userKey, relationship)
                return SingleResponse.Companion.getInstance(UserRelationship(accountKey, userKey,
                        relationship, isFiltering))
            } catch (e: MicroBlogException) {
                return SingleResponse.Companion.getInstance<UserRelationship>(e)
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }
    }

    internal class UserRelationship : CachedRelationship {
        var filtering: Boolean = false
        var can_dm: Boolean = false

        constructor(accountKey: UserKey, userKey: UserKey,
                    relationship: Relationship?, filtering: Boolean) : super(relationship, accountKey, userKey) {
            this.filtering = filtering
            if (relationship != null) {
                this.can_dm = relationship.canSourceDMTarget()
            }
        }

        constructor(user: ParcelableUser, filtering: Boolean) {
            this.account_key = user.account_key
            this.user_key = user.key
            this.filtering = filtering
            if (user.extras != null) {
                this.following = user.is_following
                this.followed_by = user.extras.followed_by
                this.blocking = user.extras.blocking
                this.blocked_by = user.extras.blocked_by
                this.can_dm = user.extras.followed_by
            }
        }

        fun check(user: ParcelableUser): Boolean {
            if (account_key != user.account_key) {
                return false
            }
            return user.extras != null && TextUtils.equals(user_key.id, user.extras.unique_id) || TextUtils.equals(user_key.id, user.key.id)
        }

    }

    private class CacheUserInfoTask(
            private val context: Context
    ) : AbstractTask<Pair<ParcelableUser, CachedRelationship>, Any, Any?>() {

        public override fun doLongOperation(args: Pair<ParcelableUser, CachedRelationship>): Any? {
            val resolver = context.contentResolver
            val user = args.first
            resolver.insert(CachedUsers.CONTENT_URI, ParcelableUserValuesCreator.create(user))
//            resolver.insert(CachedRelationships.CONTENT_URI, CachedRelationshipValuesCreator.create(args.second))
            return null
        }
    }

    class AddRemoveUserListDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val lists = arguments.getParcelableArray(EXTRA_USER_LISTS).toTypedArray(ParcelableUserList.CREATOR)
            val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)
            val accountKey = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.add_or_remove_from_list)
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
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val checkedPositions = dialog.listView.checkedItemPositions

                    promiseOnUi {
                        val activity = activity as IExtendedActivity
                        activity.executeAfterFragmentResumed {
                            ProgressDialogFragment.show(fragmentManager, "update_lists_progress")
                        }
                    }.then {
                        val twitter = MicroBlogAPIFactory.getInstance(context, accountKey, false)
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
                            throw UpdateListsException(successfulStates)
                        }
                    }.alwaysUi {
                        val activity = activity as IExtendedActivity
                        activity.executeAfterFragmentResumed {
                            val df = fragmentManager.findFragmentByTag("update_lists_progress") as? DialogFragment
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
                                dialog.listView.setItemChecked(pos, checked)
                                states[pos] = checked
                            }
                        }
                        Utils.showErrorMessage(context, R.string.action_modifying_lists, e, false)
                    }
                }
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                    val df = CreateUserListDialogFragment()
                    df.arguments = Bundle {
                        this[EXTRA_ACCOUNT_KEY] = accountKey
                    }
                    df.show(fragmentManager, "create_user_list")
                }
            }
            return dialog
        }

        class UpdateListsException(val successfulStates: SparseBooleanArray) : MicroBlogException()
    }

    companion object {

        private val sArgbEvaluator = ArgbEvaluator()
        private val LOADER_ID_USER = 1
        private val LOADER_ID_FRIENDSHIP = 2

        private val TAB_POSITION_STATUSES = 0
        private val TAB_POSITION_MEDIA = 1
        private val TAB_POSITION_FAVORITES = 2
        private val TAB_TYPE_STATUSES = "statuses"
        private val TAB_TYPE_MEDIA = "media"
        private val TAB_TYPE_FAVORITES = "favorites"
    }
}
