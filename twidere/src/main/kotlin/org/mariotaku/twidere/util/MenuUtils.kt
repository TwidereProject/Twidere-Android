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

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Parcelable
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.ShareActionProvider
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.setActionIcon
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.microblog.library.mastodon.annotation.StatusVisibility
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.favoriteConfirmationKey
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.fragment.AbsStatusesFragment
import org.mariotaku.twidere.fragment.AddStatusFilterDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.SetUserNicknameDialogFragment
import org.mariotaku.twidere.fragment.status.*
import org.mariotaku.twidere.graphic.ActionIconDrawable
import org.mariotaku.twidere.graphic.PaddingDrawable
import org.mariotaku.twidere.menu.FavoriteItemProvider
import org.mariotaku.twidere.menu.SupportStatusShareProvider
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.task.CreateFavoriteTask
import org.mariotaku.twidere.task.DestroyFavoriteTask
import org.mariotaku.twidere.task.RetweetStatusTask
import org.mariotaku.twidere.util.menu.TwidereMenuInfo
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 15/4/12.
 */
object MenuUtils {

    fun addIntentToMenu(context: Context, menu: Menu, queryIntent: Intent,
            groupId: Int = Menu.NONE) {
        val pm = context.packageManager
        val res = context.resources
        val density = res.displayMetrics.density
        val padding = (density * 4).roundToInt()
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

    fun setupForStatus(context: Context, menu: Menu, preferences: SharedPreferences,
            twitter: AsyncTwitterWrapper, manager: UserColorNameManager, status: ParcelableStatus) {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context),
                status.account_key, true) ?: return
        setupForStatus(context, menu, preferences, twitter, manager, status, account)
    }

    @UiThread
    fun setupForStatus(context: Context, menu: Menu, preferences: SharedPreferences,
            twitter: AsyncTwitterWrapper, manager: UserColorNameManager, status: ParcelableStatus,
            details: AccountDetails) {
        if (menu is ContextMenu) {
            val displayName = manager.getDisplayName(status.user_key, status.user_name,
                    status.user_screen_name, preferences[nameFirstKey])
            menu.setHeaderTitle(context.getString(R.string.status_menu_title_format, displayName,
                    status.text_unescaped))
        }
        val retweetHighlight = ContextCompat.getColor(context, R.color.highlight_retweet)
        val favoriteHighlight = ContextCompat.getColor(context, R.color.highlight_favorite)
        val likeHighlight = ContextCompat.getColor(context, R.color.highlight_like)
        val isMyRetweet: Boolean =
            when {
                RetweetStatusTask.isCreatingRetweet(status.account_key, status.id) -> {
                    true
                }
                twitter.isDestroyingStatus(status.account_key, status.id) -> {
                    false
                }
                else -> {
                    status.retweeted || Utils.isMyRetweet(status)
                }
            }
        val isMyStatus = Utils.isMyStatus(status)
        menu.setItemAvailability(R.id.delete, isMyStatus)
        if (isMyStatus) {
            val isPinned = status.is_pinned_status
            menu.setItemAvailability(R.id.pin, !isPinned)
            menu.setItemAvailability(R.id.unpin, isPinned)
        } else {
            menu.setItemAvailability(R.id.pin, false)
            menu.setItemAvailability(R.id.unpin, false)
        }
        val retweet = menu.findItem(R.id.retweet)
        if (retweet != null) {

            when (status.extras?.visibility) {
                StatusVisibility.PRIVATE -> {
                    retweet.setActionIcon(context, R.drawable.ic_action_lock)
                }
                StatusVisibility.DIRECT -> {
                    retweet.setActionIcon(context, R.drawable.ic_action_message)
                    retweet.setIcon(R.drawable.ic_action_message)
                }
                else -> {
                    retweet.setActionIcon(context, R.drawable.ic_action_retweet)
                }
            }

            retweet.setTitle(if (isMyRetweet) R.string.action_cancel_retweet else R.string.action_retweet)

            ActionIconDrawable.setMenuHighlight(retweet, TwidereMenuInfo(isMyRetweet, retweetHighlight))
        }
        val favorite = menu.findItem(R.id.favorite)
        if (favorite != null) {
            val isFavorite: Boolean =
                when {
                    CreateFavoriteTask.isCreatingFavorite(status.account_key, status.id) -> {
                        true
                    }
                    DestroyFavoriteTask.isDestroyingFavorite(status.account_key, status.id) -> {
                        false
                    }
                    else -> {
                        status.is_favorite
                    }
                }
            val provider = MenuItemCompat.getActionProvider(favorite)
            val useStar = preferences[iWantMyStarsBackKey]
            if (provider is FavoriteItemProvider) {
                provider.setIsFavorite(favorite, isFavorite)
            } else {
                if (useStar) {
                    favorite.setActionIcon(context, R.drawable.ic_action_star)
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, favoriteHighlight))
                } else {
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, likeHighlight))
                }
            }
            if (useStar) {
                favorite.setTitle(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite)
            } else {
                favorite.setTitle(if (isFavorite) R.string.action_undo_like else R.string.action_like)
            }
        }
        val translate = menu.findItem(R.id.translate)
        if (translate != null) {
            val isOfficialKey = details.isOfficial(context)
            menu.setItemAvailability(R.id.translate, isOfficialKey)
        }

        val linkAvailable = LinkCreator.hasWebLink(status)
        menu.setItemAvailability(R.id.copy_url, linkAvailable)
        menu.setItemAvailability(R.id.open_in_browser, linkAvailable)

        menu.removeGroup(MENU_GROUP_STATUS_EXTENSION)
        addIntentToMenuForExtension(context, menu, MENU_GROUP_STATUS_EXTENSION,
                INTENT_ACTION_EXTENSION_OPEN_STATUS, EXTRA_STATUS, EXTRA_STATUS_JSON, status)
        val shareItem = menu.findItem(R.id.share)
        val shareProvider = MenuItemCompat.getActionProvider(shareItem)
        when {
            shareProvider is SupportStatusShareProvider -> {
                shareProvider.status = status
            }
            shareProvider is ShareActionProvider -> {
                val shareIntent = Utils.createStatusShareIntent(context, status)
                shareProvider.setShareIntent(shareIntent)
            }
            shareItem.hasSubMenu() -> {
                val shareSubMenu = shareItem.subMenu
                val shareIntent = Utils.createStatusShareIntent(context, status)
                shareSubMenu.removeGroup(MENU_GROUP_STATUS_SHARE)
                addIntentToMenu(context, shareSubMenu, shareIntent, MENU_GROUP_STATUS_SHARE)
            }
            else -> {
                val shareIntent = Utils.createStatusShareIntent(context, status)
                val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_status))
                shareItem.intent = chooserIntent
            }
        }

    }

    fun handleStatusClick(context: Context, fragment: Fragment?, fm: FragmentManager,
                          preferences: SharedPreferences, colorNameManager: UserColorNameManager,
                          twitter: AsyncTwitterWrapper, status: ParcelableStatus, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> {
                if (ClipboardUtils.setText(context, status.text_plain)) {
                    Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.retweet -> {
                when {
                    fragment is BaseFragment -> {
                        fragment.executeAfterFragmentResumed {
                            RetweetQuoteDialogFragment.show(it.childFragmentManager, status.account_key,
                                status.id, status)
                        }
                    }
                    context is BaseActivity -> {
                        context.executeAfterFragmentResumed {
                            RetweetQuoteDialogFragment.show(it.supportFragmentManager, status.account_key,
                                status.id, status)
                        }
                    }
                    else -> {
                        RetweetQuoteDialogFragment.show(fm, status.account_key,
                            status.id, status)
                    }
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
                if (preferences[favoriteConfirmationKey]) {
                    when {
                        fragment is BaseFragment -> {
                            fragment.executeAfterFragmentResumed {
                                FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    status.account_key, status.id, status)
                            }
                        }
                        context is BaseActivity -> {
                            context.executeAfterFragmentResumed {
                                FavoriteConfirmDialogFragment.show(it.supportFragmentManager,
                                    status.account_key, status.id, status)
                            }
                        }
                        else -> {
                            FavoriteConfirmDialogFragment.show(fm, status.account_key, status.id,
                                status)
                        }
                    }
                } else if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_key, status.id)
                } else {
                    val provider = MenuItemCompat.getActionProvider(item)
                    if (provider is FavoriteItemProvider) {
                        provider.invokeItem(item,
                                AbsStatusesFragment.DefaultOnLikedListener(twitter, status))
                    } else {
                        twitter.createFavoriteAsync(status.account_key, status)
                    }
                }
            }
            R.id.delete -> {
                DestroyStatusDialogFragment.show(fm, status)
            }
            R.id.pin -> {
                PinStatusDialogFragment.show(fm, status)
            }
            R.id.unpin -> {
                UnpinStatusDialogFragment.show(fm, status)
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
                val df = SetUserNicknameDialogFragment.create(status.user_key, nick)
                df.show(fm, SetUserNicknameDialogFragment.FRAGMENT_TAG)
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
                val uri = LinkCreator.getStatusWebLink(status) ?: return true
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.`package` = null
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_open_in_browser)))
                }
            }
            R.id.copy_url -> {
                val uri = LinkCreator.getStatusWebLink(status) ?: return true
                ClipboardUtils.setText(context, uri.toString())
                Toast.makeText(context, R.string.message_toast_link_copied_to_clipboard,
                        Toast.LENGTH_SHORT).show()
            }
            R.id.mute_users -> {
                val df = MuteStatusUsersDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_STATUS] = status
                }
                df.show(fm, "mute_users_selector")
            }
            R.id.block_users -> {
                val df = BlockStatusUsersDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_STATUS] = status
                }
                df.show(fm, "block_users_selector")
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


    fun addIntentToMenuForExtension(context: Context, menu: Menu, groupId: Int, action: String,
            parcelableKey: String, jsonKey: String, obj: Parcelable) {
        val pm = context.packageManager
        val res = context.resources
        val density = res.displayMetrics.density
        val padding = (density * 4).roundToInt()
        val queryIntent = Intent(action)
        queryIntent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        val activities = pm.queryIntentActivities(queryIntent, PackageManager.GET_META_DATA)
        val parcelableJson = try {
            JsonSerializer.serialize(obj)
        } catch (e: IOException) {
            null
        }
        for (info in activities) {
            val intent = Intent(queryIntent)
            if (Utils.isExtensionUseJSON(info) && parcelableJson != null) {
                intent.putExtra(jsonKey, parcelableJson)
            } else {
                intent.putExtra(parcelableKey, obj)
            }
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name)
            val item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm))
            item.intent = intent
            val metaDataDrawable = Utils.getMetadataDrawable(pm, info.activityInfo, METADATA_KEY_EXTENSION_ICON)
            val actionIconColor = ThemeUtils.getThemeForegroundColor(context)
            if (metaDataDrawable != null) {
                metaDataDrawable.mutate()
                metaDataDrawable.setColorFilter(actionIconColor, PorterDuff.Mode.SRC_ATOP)
                item.icon = metaDataDrawable
            } else {
                val icon = info.loadIcon(pm)
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
    }
}

