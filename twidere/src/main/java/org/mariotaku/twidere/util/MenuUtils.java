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

package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.activity.AccountSelectorActivity;
import org.mariotaku.twidere.activity.ColorPickerDialogActivity;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.fragment.AbsStatusesFragment;
import org.mariotaku.twidere.fragment.AddStatusFilterDialogFragment;
import org.mariotaku.twidere.fragment.DestroyStatusDialogFragment;
import org.mariotaku.twidere.fragment.SetUserNicknameDialogFragment;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.graphic.PaddingDrawable;
import org.mariotaku.twidere.menu.SupportStatusShareProvider;
import org.mariotaku.twidere.menu.support.FavoriteItemProvider;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;

import java.util.List;

/**
 * Created by mariotaku on 15/4/12.
 */
public class MenuUtils implements Constants {
    public static void setMenuItemAvailability(final Menu menu, final int id, final boolean available) {
        if (menu == null) return;
        final MenuItem item = menu.findItem(id);
        if (item == null) return;
        item.setVisible(available);
        item.setEnabled(available);
    }

    public static void setMenuItemChecked(final Menu menu, final int id, final boolean checked) {
        if (menu == null) return;
        final MenuItem item = menu.findItem(id);
        if (item == null) return;
        item.setChecked(checked);
    }

    public static void setMenuItemIcon(final Menu menu, final int id, @DrawableRes final int icon) {
        if (menu == null) return;
        final MenuItem item = menu.findItem(id);
        if (item == null) return;
        item.setIcon(icon);
    }

    public static void setMenuItemShowAsActionFlags(Menu menu, int id, int flags) {
        if (menu == null) return;
        final MenuItem item = menu.findItem(id);
        if (item == null) return;
        item.setShowAsActionFlags(flags);
        MenuItemCompat.setShowAsAction(item, flags);
    }

    public static void setMenuItemTitle(final Menu menu, final int id, @StringRes final int icon) {
        if (menu == null) return;
        final MenuItem item = menu.findItem(id);
        if (item == null) return;
        item.setTitle(icon);
    }

    public static void addIntentToMenu(final Context context, final Menu menu, final Intent queryIntent) {
        addIntentToMenu(context, menu, queryIntent, Menu.NONE);
    }

    public static void addIntentToMenu(final Context context, final Menu menu, final Intent queryIntent,
                                       final int groupId) {
        if (context == null || menu == null || queryIntent == null) return;
        final PackageManager pm = context.getPackageManager();
        final Resources res = context.getResources();
        final float density = res.getDisplayMetrics().density;
        final int padding = Math.round(density * 4);
        final List<ResolveInfo> activities = pm.queryIntentActivities(queryIntent, 0);
        for (final ResolveInfo info : activities) {
            final Intent intent = new Intent(queryIntent);
            final Drawable icon = info.loadIcon(pm);
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            final MenuItem item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm));
            item.setIntent(intent);
            final int iw = icon.getIntrinsicWidth(), ih = icon.getIntrinsicHeight();
            if (iw > 0 && ih > 0) {
                final Drawable iconWithPadding = new PaddingDrawable(icon, padding);
                iconWithPadding.setBounds(0, 0, iw, ih);
                item.setIcon(iconWithPadding);
            } else {
                item.setIcon(icon);
            }
        }
    }

    public static void setupForStatus(@NonNull final Context context,
                                      @NonNull final SharedPreferencesWrapper preferences,
                                      @NonNull final Menu menu,
                                      @NonNull final ParcelableStatus status,
                                      @NonNull UserColorNameManager manager,
                                      @NonNull final AsyncTwitterWrapper twitter) {
        final ParcelableCredentials account = ParcelableCredentialsUtils.getCredentials(context,
                status.account_key);
        if (account == null) return;
        setupForStatus(context, preferences, menu, status, account, manager, twitter);
    }

    public static void setupForStatus(@NonNull final Context context,
                                      @NonNull final SharedPreferencesWrapper preferences,
                                      @NonNull final Menu menu,
                                      @NonNull final ParcelableStatus status,
                                      @NonNull final ParcelableCredentials account,
                                      @NonNull UserColorNameManager manager,
                                      @NonNull final AsyncTwitterWrapper twitter) {
        if (menu instanceof ContextMenu) {
            ((ContextMenu) menu).setHeaderTitle(context.getString(R.string.status_menu_title_format,
                    manager.getDisplayName(status, preferences.getBoolean(KEY_NAME_FIRST), false),
                    status.text_unescaped));
        }
        final int retweetHighlight = ContextCompat.getColor(context, R.color.highlight_retweet);
        final int favoriteHighlight = ContextCompat.getColor(context, R.color.highlight_favorite);
        final int likeHighlight = ContextCompat.getColor(context, R.color.highlight_like);
        final boolean isMyRetweet;
        if (twitter.isCreatingRetweet(status.account_key, status.id)) {
            isMyRetweet = true;
        } else if (twitter.isDestroyingStatus(status.account_key, status.id)) {
            isMyRetweet = false;
        } else {
            isMyRetweet = status.retweeted || Utils.isMyRetweet(status);
        }
        final MenuItem delete = menu.findItem(R.id.delete);
        if (delete != null) {
            delete.setVisible(Utils.isMyStatus(status));
        }
        final MenuItem retweet = menu.findItem(R.id.retweet);
        if (retweet != null) {
            ActionIconDrawable.setMenuHighlight(retweet, new TwidereMenuInfo(isMyRetweet, retweetHighlight));
            retweet.setTitle(isMyRetweet ? R.string.cancel_retweet : R.string.retweet);
        }
        final MenuItem favorite = menu.findItem(R.id.favorite);
        if (favorite != null) {
            final boolean isFavorite;
            if (twitter.isCreatingFavorite(status.account_key, status.id)) {
                isFavorite = true;
            } else if (twitter.isDestroyingFavorite(status.account_key, status.id)) {
                isFavorite = false;
            } else {
                isFavorite = status.is_favorite;
            }
            ActionProvider provider = MenuItemCompat.getActionProvider(favorite);
            final boolean useStar = preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK);
            if (provider instanceof FavoriteItemProvider) {
                ((FavoriteItemProvider) provider).setIsFavorite(favorite, isFavorite);
            } else {
                if (useStar) {
                    final Drawable oldIcon = favorite.getIcon();
                    if (oldIcon instanceof ActionIconDrawable) {
                        final Drawable starIcon = ContextCompat.getDrawable(context, R.drawable.ic_action_star);
                        favorite.setIcon(new ActionIconDrawable(starIcon, ((ActionIconDrawable) oldIcon).getDefaultColor()));
                    } else {
                        favorite.setIcon(R.drawable.ic_action_star);
                    }
                    ActionIconDrawable.setMenuHighlight(favorite, new TwidereMenuInfo(isFavorite, favoriteHighlight));
                } else {
                    ActionIconDrawable.setMenuHighlight(favorite, new TwidereMenuInfo(isFavorite, likeHighlight));
                }
            }
            if (useStar) {
                favorite.setTitle(isFavorite ? R.string.unfavorite : R.string.favorite);
            } else {
                favorite.setTitle(isFavorite ? R.string.undo_like : R.string.like);
            }
        }
        final MenuItem translate = menu.findItem(R.id.translate);
        if (translate != null) {
            final boolean isOfficialKey = Utils.isOfficialCredentials(context, account);
            final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, TwidereConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final boolean forcePrivateApis = prefs.getBoolean(SharedPreferenceConstants.KEY_FORCE_USING_PRIVATE_APIS, false);
            setMenuItemAvailability(menu, R.id.translate, forcePrivateApis || isOfficialKey);
        }
        menu.removeGroup(Constants.MENU_GROUP_STATUS_EXTENSION);
        Utils.addIntentToMenuForExtension(context, menu, Constants.MENU_GROUP_STATUS_EXTENSION, IntentConstants.INTENT_ACTION_EXTENSION_OPEN_STATUS,
                IntentConstants.EXTRA_STATUS, IntentConstants.EXTRA_STATUS_JSON, status);
        final MenuItem shareItem = menu.findItem(R.id.share);
        final ActionProvider shareProvider = MenuItemCompat.getActionProvider(shareItem);
        if (shareProvider instanceof SupportStatusShareProvider) {
            ((SupportStatusShareProvider) shareProvider).setStatus(status);
        } else if (shareProvider instanceof ShareActionProvider) {
            final Intent shareIntent = Utils.createStatusShareIntent(context, status);
            ((ShareActionProvider) shareProvider).setShareIntent(shareIntent);
        } else if (shareItem.hasSubMenu()) {
            final Menu shareSubMenu = shareItem.getSubMenu();
            final Intent shareIntent = Utils.createStatusShareIntent(context, status);
            shareSubMenu.removeGroup(Constants.MENU_GROUP_STATUS_SHARE);
            addIntentToMenu(context, shareSubMenu, shareIntent, Constants.MENU_GROUP_STATUS_SHARE);
        } else {
            final Intent shareIntent = Utils.createStatusShareIntent(context, status);
            final Intent chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_status));
            Utils.addCopyLinkIntent(context, chooserIntent, LinkCreator.getStatusWebLink(status));
            shareItem.setIntent(chooserIntent);
        }

    }

    public static boolean handleStatusClick(@NonNull final Context context,
                                            @Nullable final Fragment fragment,
                                            @NonNull final FragmentManager fm,
                                            @NonNull final UserColorNameManager colorNameManager,
                                            @NonNull final AsyncTwitterWrapper twitter,
                                            @NonNull final ParcelableStatus status,
                                            @NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy: {
                if (ClipboardUtils.setText(context, status.text_plain)) {
                    Utils.showOkMessage(context, R.string.text_copied, false);
                }
                break;
            }
            case R.id.retweet: {
                if (Utils.isMyRetweet(status)) {
                    twitter.cancelRetweetAsync(status.account_key,
                            status.id, status.my_retweet_id);
                } else {
                    twitter.retweetStatusAsync(status.account_key,
                            status.id);
                }
                break;
            }
            case R.id.quote: {
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_QUOTE);
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
            }
            case R.id.reply: {
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_REPLY);
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
            }
            case R.id.favorite: {
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_key, status.id);
                } else {
                    ActionProvider provider = MenuItemCompat.getActionProvider(item);
                    if (provider instanceof FavoriteItemProvider) {
                        ((FavoriteItemProvider) provider).invokeItem(item,
                                new AbsStatusesFragment.DefaultOnLikedListener(twitter, status));
                    } else {
                        twitter.createFavoriteAsync(status.account_key, status.id);
                    }
                }
                break;
            }
            case R.id.delete: {
                DestroyStatusDialogFragment.show(fm, status);
                break;
            }
            case R.id.add_to_filter: {
                AddStatusFilterDialogFragment.show(fm, status);
                break;
            }
            case R.id.set_color: {
                final Intent intent = new Intent(context, ColorPickerDialogActivity.class);
                final int color = colorNameManager.getUserColor(status.user_key, true);
                if (color != 0) {
                    intent.putExtra(IntentConstants.EXTRA_COLOR, color);
                }
                intent.putExtra(IntentConstants.EXTRA_CLEAR_BUTTON, color != 0);
                intent.putExtra(IntentConstants.EXTRA_ALPHA_SLIDER, false);
                if (fragment != null) {
                    fragment.startActivityForResult(intent, TwidereConstants.REQUEST_SET_COLOR);
                } else if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, TwidereConstants.REQUEST_SET_COLOR);
                }
                break;
            }
            case R.id.clear_nickname: {
                colorNameManager.clearUserNickname(status.user_key);
                break;
            }
            case R.id.set_nickname: {
                final String nick = colorNameManager.getUserNickname(status.user_key, true);
                SetUserNicknameDialogFragment.show(fm, status.user_key, nick);
                break;
            }
            case R.id.open_with_account: {
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_SELECT_ACCOUNT);
                intent.setClass(context, AccountSelectorActivity.class);
                intent.putExtra(IntentConstants.EXTRA_SINGLE_SELECTION, true);
                if (fragment != null) {
                    fragment.startActivityForResult(intent, TwidereConstants.REQUEST_SELECT_ACCOUNT);
                } else if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, TwidereConstants.REQUEST_SELECT_ACCOUNT);
                }
                break;
            }
            case R.id.open_in_browser: {
                final Intent intent = new Intent(Intent.ACTION_VIEW, LinkCreator.getStatusWebLink(status));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
//                IntentSupport.setSelector(intent, new Intent(Intent.ACTION_VIEW).addCategory(IntentSupport.CATEGORY_APP_BROWSER));
                context.startActivity(intent);
                break;
            }
            default: {
                if (item.getIntent() != null) {
                    try {
                        context.startActivity(item.getIntent());
                    } catch (final ActivityNotFoundException e) {
                        Log.w(TwidereConstants.LOGTAG, e);
                        return false;
                    }
                }
                break;
            }
        }
        return true;
    }
}
