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

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.view.WindowCompat
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.Window
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarShowHideHelper
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.KeyboardShortcutConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.graphic.EmptyDrawable
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.MultiSelectEventHandler
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.Utils.createFragmentForIntent
import org.mariotaku.twidere.util.Utils.matchLinkId

class LinkHandlerActivity : BaseActivity(), SystemWindowsInsetsCallback, IControlBarActivity, SupportFragmentCallback {

    private val mControlBarShowHideHelper = ControlBarShowHideHelper(this)
    private var mMultiSelectHandler: MultiSelectEventHandler? = null
    private var mFinishOnly: Boolean = false
    private var mActionBarHeight: Int = 0
    private var mSubtitle: CharSequence? = null
    private var mHideOffsetNotSupported: Boolean = false


    override val currentVisibleFragment: Fragment?
        get() {
            return supportFragmentManager.findFragmentById(android.R.id.content)
        }

    override fun triggerRefresh(position: Int): Boolean {
        return false
    }

    override fun onFitSystemWindows(insets: Rect) {
        super.onFitSystemWindows(insets)
        val fragment = currentVisibleFragment
        if (fragment is IBaseFragment) {
            fragment.requestFitSystemWindows()
        }
    }

    override fun getSystemWindowsInsets(insets: Rect): Boolean {
        return super.getSystemWindowsInsets(insets)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (mFinishOnly) {
                    finish()
                } else {
                    NavUtils.navigateUpFromSameTask(this)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            return handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)
        }
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (KeyboardShortcutConstants.ACTION_NAVIGATION_BACK == action) {
            onBackPressed()
            return true
        }
        return handler.handleKey(this, null, keyCode, event, metaState)
    }

    private fun shouldFragmentTakeAllKeyboardShortcuts(): Boolean {
        val fragment = currentVisibleFragment
        return fragment is KeyboardShortcutsHandler.TakeAllKeyboardShortcut
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
        }
        if (handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState))
            return true
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true
        return super.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    private fun isFragmentKeyboardShortcutHandled(handler: KeyboardShortcutsHandler,
                                                  keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = currentVisibleFragment
        if (fragment is KeyboardShortcutCallback) {
            return fragment.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mMultiSelectHandler = MultiSelectEventHandler(this)
        mMultiSelectHandler!!.dispatchOnCreate()
        val intent = intent
        val uri = intent.data
        val linkId = matchLinkId(uri)
        intent.setExtrasClassLoader(classLoader)
        val fragment: Fragment?
        try {
            fragment = createFragmentForIntent(this, linkId, intent)
        } catch (e: Utils.NoAccountException) {
            super.onCreate(savedInstanceState)
            val selectIntent = Intent(this, AccountSelectorActivity::class.java)
            var accountHost: String? = intent.getStringExtra(IntentConstants.EXTRA_ACCOUNT_HOST)
            if (accountHost == null) {
                accountHost = uri.getQueryParameter(TwidereConstants.QUERY_PARAM_ACCOUNT_HOST)
            }
            selectIntent.putExtra(IntentConstants.EXTRA_SINGLE_SELECTION, true)
            selectIntent.putExtra(IntentConstants.EXTRA_SELECT_ONLY_ITEM, true)
            selectIntent.putExtra(IntentConstants.EXTRA_ACCOUNT_HOST, accountHost)
            selectIntent.putExtra(IntentConstants.EXTRA_START_INTENT, intent)
            startActivity(selectIntent)
            finish()
            return
        }

        if (fragment is IToolBarSupportFragment) {
            if (!fragment.setupWindow(this)) {
                supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
                supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY)
            }
        }

        super.onCreate(savedInstanceState)

        if (fragment == null) {
            finish()
            return
        }

        setupActionBarOption()
        Utils.logOpenNotificationFromUri(this, uri)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(android.R.id.content, fragment)
        ft.commit()
        setTitle(linkId, uri)
        mFinishOnly = java.lang.Boolean.parseBoolean(uri.getQueryParameter(TwidereConstants.QUERY_PARAM_FINISH_ONLY))

        if (fragment is IToolBarSupportFragment) {
            ThemeUtils.setCompatContentViewOverlay(window, EmptyDrawable())
        }
    }

    override fun onStart() {
        super.onStart()
        mMultiSelectHandler!!.dispatchOnStart()
    }


    override fun onStop() {
        mMultiSelectHandler!!.dispatchOnStop()
        super.onStop()
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        setupActionBarOption()
    }

    fun setSubtitle(subtitle: CharSequence?) {
        mSubtitle = subtitle
        setupActionBarOption()
    }

    override fun setControlBarVisibleAnimate(visible: Boolean) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (currentVisibleFragment !is HideUiOnScroll) return
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible)
    }

    override fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarShowHideHelper.ControlBarAnimationListener) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (currentVisibleFragment !is HideUiOnScroll) return
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible, listener)
    }

    override fun getControlBarOffset(): Float {
        val fragment = currentVisibleFragment
        val actionBar = supportActionBar
        if (fragment is IToolBarSupportFragment) {
            return fragment.controlBarOffset
        } else if (actionBar != null) {
            return actionBar.hideOffset / controlBarHeight.toFloat()
        }
        return 0f
    }

    override fun setControlBarOffset(offset: Float) {
        val fragment = currentVisibleFragment
        val actionBar = supportActionBar
        if (fragment is IToolBarSupportFragment) {
            fragment.controlBarOffset = offset
        } else if (actionBar != null && !mHideOffsetNotSupported) {
            try {
                actionBar.hideOffset = (controlBarHeight * offset).toInt()
            } catch (e: UnsupportedOperationException) {
                // Some device will throw this exception
                mHideOffsetNotSupported = true
            }

        }
        notifyControlBarOffsetChanged()
    }

    override fun getControlBarHeight(): Int {
        val fragment = currentVisibleFragment
        val actionBar = supportActionBar
        if (fragment is IToolBarSupportFragment) {
            return fragment.controlBarHeight
        } else if (actionBar != null) {
            return actionBar.height
        }
        if (mActionBarHeight != 0) return mActionBarHeight
        mActionBarHeight = ThemeUtils.getActionBarHeight(this)
        return mActionBarHeight
    }

    private fun setTitle(linkId: Int, uri: Uri): Boolean {
        setSubtitle(null)
        when (linkId) {
            Constants.LINK_ID_STATUS -> {
                setTitle(R.string.status)
            }
            Constants.LINK_ID_USER -> {
                setTitle(R.string.user)
            }
            Constants.LINK_ID_USER_TIMELINE -> {
                setTitle(R.string.statuses)
            }
            Constants.LINK_ID_USER_FAVORITES -> {
                if (preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK)) {
                    setTitle(R.string.favorites)
                } else {
                    setTitle(R.string.likes)
                }
            }
            Constants.LINK_ID_USER_FOLLOWERS -> {
                setTitle(R.string.followers)
            }
            Constants.LINK_ID_USER_FRIENDS -> {
                setTitle(R.string.following)
            }
            Constants.LINK_ID_USER_BLOCKS -> {
                setTitle(R.string.blocked_users)
            }
            Constants.LINK_ID_MUTES_USERS -> {
                setTitle(R.string.twitter_muted_users)
            }
            Constants.LINK_ID_DIRECT_MESSAGES_CONVERSATION -> {
                setTitle(R.string.direct_messages)
            }
            Constants.LINK_ID_USER_LIST -> {
                setTitle(R.string.user_list)
            }
            Constants.LINK_ID_GROUP -> {
                setTitle(R.string.group)
            }
            Constants.LINK_ID_USER_LISTS -> {
                setTitle(R.string.user_lists)
            }
            Constants.LINK_ID_USER_GROUPS -> {
                setTitle(R.string.groups)
            }
            Constants.LINK_ID_USER_LIST_TIMELINE -> {
                setTitle(R.string.list_timeline)
            }
            Constants.LINK_ID_USER_LIST_MEMBERS -> {
                setTitle(R.string.list_members)
            }
            Constants.LINK_ID_USER_LIST_SUBSCRIBERS -> {
                setTitle(R.string.list_subscribers)
            }
            Constants.LINK_ID_USER_LIST_MEMBERSHIPS -> {
                setTitle(R.string.lists_following_user)
            }
            Constants.LINK_ID_SAVED_SEARCHES -> {
                setTitle(R.string.saved_searches)
            }
            Constants.LINK_ID_USER_MENTIONS -> {
                setTitle(R.string.user_mentions)
            }
            Constants.LINK_ID_INCOMING_FRIENDSHIPS -> {
                setTitle(R.string.incoming_friendships)
            }
            Constants.LINK_ID_ITEMS -> {
            }// TODO show title
            Constants.LINK_ID_USER_MEDIA_TIMELINE -> {
                setTitle(R.string.media)
            }
            Constants.LINK_ID_STATUS_RETWEETERS -> {
                setTitle(R.string.users_retweeted_this)
            }
            Constants.LINK_ID_STATUS_FAVORITERS -> {
                setTitle(R.string.users_favorited_this)
            }
            Constants.LINK_ID_SEARCH -> {
                setTitle(android.R.string.search_go)
                setSubtitle(uri.getQueryParameter(TwidereConstants.QUERY_PARAM_QUERY))
            }
            Constants.LINK_ID_ACCOUNTS -> {
                setTitle(R.string.accounts)
            }
            Constants.LINK_ID_DRAFTS -> {
                setTitle(R.string.drafts)
            }
            Constants.LINK_ID_FILTERS -> {
                setTitle(R.string.filters)
            }
            Constants.LINK_ID_MAP -> {
                setTitle(R.string.view_map)
            }
            Constants.LINK_ID_PROFILE_EDITOR -> {
                setTitle(R.string.edit_profile)
            }
            Constants.LINK_ID_SCHEDULED_STATUSES -> {
                title = getString(R.string.scheduled_statuses)
            }
            Constants.LINK_ID_DIRECT_MESSAGES -> {
                title = getString(R.string.direct_messages)
            }
            Constants.LINK_ID_INTERACTIONS -> {
                title = getString(R.string.interactions)
            }
            Constants.LINK_ID_PUBLIC_TIMELINE -> {
                title = getString(R.string.public_timeline)
            }
            else -> {
                title = getString(R.string.app_name)
            }
        }
        return true
    }

    private fun handleFragmentKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
                                                     repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = currentVisibleFragment
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState)
        }
        return false
    }

    private fun handleFragmentKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int,
                                                     event: KeyEvent, metaState: Int): Boolean {
        val fragment = currentVisibleFragment
        if (fragment is KeyboardShortcutCallback) {
            if (fragment.handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState)) {
                return true
            }
        }
        return false
    }

    private fun setupActionBarOption() {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.subtitle = mSubtitle
        }
    }

    interface HideUiOnScroll
}
