/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import org.mariotaku.ktextension.setItemChecked
import org.mariotaku.ktextension.setMenuItemIcon
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.fragment.AbsStatusesFragment
import org.mariotaku.twidere.fragment.AddStatusFilterDialogFragment
import org.mariotaku.twidere.fragment.DestroyStatusDialogFragment
import org.mariotaku.twidere.fragment.SetUserNicknameDialogFragment
import org.mariotaku.twidere.graphic.ActionIconDrawable
import org.mariotaku.twidere.graphic.PaddingDrawable
import org.mariotaku.twidere.menu.FavoriteItemProvider
import org.mariotaku.twidere.menu.SupportStatusShareProvider
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.util.menu.TwidereMenuInfo

/**
 * Created by mariotaku on 15/4/12.
 */
object MenuUtils {

    fun setItemAvailability(menu: Menu?, id: Int, available: Boolean) {
        if (menu == null) return
        val item = menu.findItem(id) ?: return
        item.isVisible = available
        item.isEnabled = available
    }

    fun setItemChecked(menu: Menu?, id: Int, checked: Boolean) {
        menu?.setItemChecked(id, checked)
    }

    fun setMenuItemIcon(menu: Menu?, id: Int, @DrawableRes icon: Int) {
        menu?.setMenuItemIcon(id, icon)
    }

    fun setMenuItemTitle(menu: Menu?, id: Int, @StringRes icon: Int) {
        if (menu == null) return
        val item = menu.findItem(id) ?: return
        item.setTitle(icon)
    }

    @JvmOverloads fun addIntentToMenu(context: Context?, menu: Menu?, queryIntent: Intent?,
                                      groupId: Int = Menu.NONE) {
        if (context == null || menu == null || queryIntent == null) return
        val pm = context.packageManager
        val res = context.resources
        val density = res.displayMetrics.density
        val padding = Math.round(density * 4)
        val activities = pm.queryIntentActivities(queryIntent, 0)
        for (info in activities) {
            val intent = Intent(queryIntent)
            val icon = info.loadIcon(pm)
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name)
            val item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm))
            item.intent = intent
            val iw = icon.intrinsicWidth
            val ih = icon.intrinsicHeight
            if (iw > 0 && ih > 0) {
                val iconWithPadding = PaddingDrawable(icon, padding)
                iconWithPadding.setBounds(0, 0, iw, ih)
                item.icon = iconWithPadding
            } else {
                item.icon = icon
            }
        }
    }

    fun setupForStatus(context: Context,
                       preferences: SharedPreferencesWrapper,
                       menu: Menu,
                       status: ParcelableStatus,
                       twitter: AsyncTwitterWrapper) {
        val account = ParcelableCredentialsUtils.getCredentials(context,
                status.account_key) ?: return
        setupForStatus(context, preferences, menu, status, account, twitter)
    }

    @UiThread
    fun setupForStatus(context: Context,
                       preferences: SharedPreferencesWrapper,
                       menu: Menu,
                       status: ParcelableStatus,
                       account: ParcelableCredentials,
                       twitter: AsyncTwitterWrapper) {
        if (menu is ContextMenu) {
            menu.setHeaderTitle(context.getString(R.string.status_menu_title_format,
                    UserColorNameManager.decideDisplayName(status.user_nickname, status.user_name,
                            status.user_screen_name, preferences.getBoolean(SharedPreferenceConstants.KEY_NAME_FIRST)),
                    status.text_unescaped))
        }
        val retweetHighlight = ContextCompat.getColor(context, R.color.highlight_retweet)
        val favoriteHighlight = ContextCompat.getColor(context, R.color.highlight_favorite)
        val likeHighlight = ContextCompat.getColor(context, R.color.highlight_like)
        val isMyRetweet: Boolean
        if (twitter.isCreatingRetweet(status.account_key, status.id)) {
            isMyRetweet = true
        } else if (twitter.isDestroyingStatus(status.account_key, status.id)) {
            isMyRetweet = false
        } else {
            isMyRetweet = status.retweeted || Utils.isMyRetweet(status)
        }
        val delete = menu.findItem(R.id.delete)
        if (delete != null) {
            delete.isVisible = Utils.isMyStatus(status)
        }
        val retweet = menu.findItem(R.id.retweet)
        if (retweet != null) {
            ActionIconDrawable.setMenuHighlight(retweet, TwidereMenuInfo(isMyRetweet, retweetHighlight))
            retweet.setTitle(if (isMyRetweet) R.string.cancel_retweet else R.string.retweet)
        }
        val favorite = menu.findItem(R.id.favorite)
        if (favorite != null) {
            val isFavorite: Boolean
            if (twitter.isCreatingFavorite(status.account_key, status.id)) {
                isFavorite = true
            } else if (twitter.isDestroyingFavorite(status.account_key, status.id)) {
                isFavorite = false
            } else {
                isFavorite = status.is_favorite
            }
            val provider = MenuItemCompat.getActionProvider(favorite)
            val useStar = preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK)
            if (provider is FavoriteItemProvider) {
                provider.setIsFavorite(favorite, isFavorite)
            } else {
                if (useStar) {
                    val oldIcon = favorite.icon
                    if (oldIcon is ActionIconDrawable) {
                        val starIcon = ContextCompat.getDrawable(context, R.drawable.ic_action_star)
                        favorite.icon = ActionIconDrawable(starIcon, oldIcon.defaultColor)
                    } else {
                        favorite.setIcon(R.drawable.ic_action_star)
                    }
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, favoriteHighlight))
                } else {
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, likeHighlight))
                }
            }
            if (useStar) {
                favorite.setTitle(if (isFavorite) R.string.unfavorite else R.string.favorite)
            } else {
                favorite.setTitle(if (isFavorite) R.string.undo_like else R.string.like)
            }
        }
        val translate = menu.findItem(R.id.translate)
        if (translate != null) {
            val isOfficialKey = Utils.isOfficialCredentials(context, account)
            val prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            setItemAvailability(menu, R.id.translate, isOfficialKey)
        }
        menu.removeGroup(Constants.MENU_GROUP_STATUS_EXTENSION)
        Utils.addIntentToMenuForExtension(context, menu, Constants.MENU_GROUP_STATUS_EXTENSION, INTENT_ACTION_EXTENSION_OPEN_STATUS,
                EXTRA_STATUS, EXTRA_STATUS_JSON, status)
        val shareItem = menu.findItem(R.id.share)
        val shareProvider = MenuItemCompat.getActionProvider(shareItem)
        if (shareProvider is SupportStatusShareProvider) {
            shareProvider.status = status
        } else if (shareProvider is ShareActionProvider) {
            val shareIntent = Utils.createStatusShareIntent(context, status)
            shareProvider.setShareIntent(shareIntent)
        } else if (shareItem.hasSubMenu()) {
            val shareSubMenu = shareItem.subMenu
            val shareIntent = Utils.createStatusShareIntent(context, status)
            shareSubMenu.removeGroup(Constants.MENU_GROUP_STATUS_SHARE)
            addIntentToMenu(context, shareSubMenu, shareIntent, Constants.MENU_GROUP_STATUS_SHARE)
        } else {
            val shareIntent = Utils.createStatusShareIntent(context, status)
            val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_status))
            Utils.addCopyLinkIntent(context, chooserIntent, LinkCreator.getStatusWebLink(status))
            shareItem.intent = chooserIntent
        }

    }

    fun handleStatusClick(context: Context,
                          fragment: Fragment?,
                          fm: FragmentManager,
                          colorNameManager: UserColorNameManager,
                          twitter: AsyncTwitterWrapper,
                          status: ParcelableStatus,
                          item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> {
                if (ClipboardUtils.setText(context, status.text_plain)) {
                    Utils.showOkMessage(context, R.string.text_copied, false)
                }
            }
            R.id.retweet -> {
                if (Utils.isMyRetweet(status)) {
                    twitter.cancelRetweetAsync(status.account_key,
                            status.id, status.my_retweet_id)
                } else {
                    twitter.retweetStatusAsync(status.account_key,
                            status.id)
                }
            }
            R.id.quote -> {
                val intent = Intent(INTENT_ACTION_QUOTE)
                intent.putExtra(EXTRA_STATUS, status)
                context.startActivity(intent)
            }
            R.id.reply -> {
                val intent = Intent(INTENT_ACTION_REPLY)
                intent.putExtra(EXTRA_STATUS, status)
                context.startActivity(intent)
            }
            R.id.favorite -> {
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_key, status.id)
                } else {
                    val provider = MenuItemCompat.getActionProvider(item)
                    if (provider is FavoriteItemProvider) {
                        provider.invokeItem(item,
                                AbsStatusesFragment.DefaultOnLikedListener(twitter, status))
                    } else {
                        twitter.createFavoriteAsync(status.account_key, status.id)
                    }
                }
            }
            R.id.delete -> {
                DestroyStatusDialogFragment.show(fm, status)
            }
            R.id.add_to_filter -> {
                AddStatusFilterDialogFragment.show(fm, status)
            }
            R.id.set_color -> {
                val intent = Intent(context, ColorPickerDialogActivity::class.java)
                val color = colorNameManager.getUserColor(status.user_key)
                if (color != 0) {
                    intent.putExtra(EXTRA_COLOR, color)
                }
                intent.putExtra(EXTRA_CLEAR_BUTTON, color != 0)
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                if (fragment != null) {
                    fragment.startActivityForResult(intent, REQUEST_SET_COLOR)
                } else if (context is Activity) {
                    context.startActivityForResult(intent, REQUEST_SET_COLOR)
                }
            }
            R.id.clear_nickname -> {
                colorNameManager.clearUserNickname(status.user_key)
            }
            R.id.set_nickname -> {
                val nick = colorNameManager.getUserNickname(status.user_key)
                val df = SetUserNicknameDialogFragment.show(fm,
                        status.user_key, nick)
                if (fragment != null) {
                    df.setTargetFragment(fragment, REQUEST_SET_NICKNAME)
                }
            }
            R.id.open_with_account -> {
                val intent = Intent(INTENT_ACTION_SELECT_ACCOUNT)
                intent.setClass(context, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                intent.putExtra(EXTRA_ACCOUNT_HOST, status.user_key.host)
                if (fragment != null) {
                    fragment.startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
                } else if (context is Activity) {
                    context.startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
                }
            }
            R.id.open_in_browser -> {
                val uri = LinkCreator.getStatusWebLink(status)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.`package` = null
                    context.startActivity(Intent.createChooser(intent,
                            context.getString(R.string.open_in_browser)))
                }

            }
            else -> {
                if (item.intent != null) {
                    try {
                        context.startActivity(item.intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(LOGTAG, e)
                        return false
                    }

                }
            }
        }
        return true
    }
}
