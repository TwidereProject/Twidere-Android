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

package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.InteractionsTimelineFragment;
import org.mariotaku.twidere.fragment.support.InvalidTabFragment;
import org.mariotaku.twidere.fragment.support.RetweetsOfMeFragment;
import org.mariotaku.twidere.fragment.support.StatusesSearchFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggestionsFragment;
import org.mariotaku.twidere.fragment.support.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.support.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.support.UserTimelineFragment;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.CustomTabConfiguration.ExtraConfiguration;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.TabCursorIndices;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CustomTabUtils implements Constants {
    private static final HashMap<String, CustomTabConfiguration> CUSTOM_TABS_CONFIGURATION_MAP = new HashMap<>();
    private static final HashMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new HashMap<>();

    static {
        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.HOME_TIMELINE, new CustomTabConfiguration(
                HomeTimelineFragment.class, R.string.home, R.drawable.ic_action_home,
                CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 0, false));

        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.NOTIFICATIONS_TIMELINE, new CustomTabConfiguration(
                InteractionsTimelineFragment.class, R.string.interactions, R.drawable.ic_action_notification,
                CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 1, false,
                ExtraConfiguration.newBoolean(EXTRA_MY_FOLLOWING_ONLY, R.string.following_only, false),
                ExtraConfiguration.newBoolean(EXTRA_MENTIONS_ONLY, R.string.mentions_only, false)));

        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.DIRECT_MESSAGES, new CustomTabConfiguration(
                DirectMessagesFragment.class, R.string.direct_messages, R.drawable.ic_action_message,
                CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 2, false));

        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.TRENDS_SUGGESTIONS, new CustomTabConfiguration(
                TrendsSuggestionsFragment.class, R.string.trends, R.drawable.ic_action_hashtag,
                CustomTabConfiguration.ACCOUNT_NONE, CustomTabConfiguration.FIELD_TYPE_NONE, 3, true));
        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.FAVORITES, new CustomTabConfiguration(UserFavoritesFragment.class,
                R.string.likes, R.drawable.ic_action_heart, CustomTabConfiguration.ACCOUNT_REQUIRED,
                CustomTabConfiguration.FIELD_TYPE_USER, 4));
        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.USER_TIMELINE, new CustomTabConfiguration(
                UserTimelineFragment.class, R.string.users_statuses, R.drawable.ic_action_quote,
                CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER, 5));
        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.SEARCH_STATUSES, new CustomTabConfiguration(
                StatusesSearchFragment.class, R.string.search_statuses, R.drawable.ic_action_search,
                CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_TEXT, R.string.query,
                EXTRA_QUERY, 6));

        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.LIST_TIMELINE, new CustomTabConfiguration(
                UserListTimelineFragment.class, R.string.list_timeline, R.drawable.ic_action_list,
                CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER_LIST, 7));

        CUSTOM_TABS_CONFIGURATION_MAP.put(CustomTabType.RETWEETS_OF_ME, new CustomTabConfiguration(
                RetweetsOfMeFragment.class, R.string.retweets_of_me, R.drawable.ic_action_retweet,
                CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_NONE, 10));

        CUSTOM_TABS_ICON_NAME_MAP.put("accounts", R.drawable.ic_action_accounts);
        CUSTOM_TABS_ICON_NAME_MAP.put("hashtag", R.drawable.ic_action_hashtag);
        CUSTOM_TABS_ICON_NAME_MAP.put("heart", R.drawable.ic_action_heart);
        CUSTOM_TABS_ICON_NAME_MAP.put("home", R.drawable.ic_action_home);
        CUSTOM_TABS_ICON_NAME_MAP.put("list", R.drawable.ic_action_list);
        CUSTOM_TABS_ICON_NAME_MAP.put("mention", R.drawable.ic_action_at);
        CUSTOM_TABS_ICON_NAME_MAP.put("notifications", R.drawable.ic_action_notification);
        CUSTOM_TABS_ICON_NAME_MAP.put("gallery", R.drawable.ic_action_gallery);
        CUSTOM_TABS_ICON_NAME_MAP.put("message", R.drawable.ic_action_message);
        CUSTOM_TABS_ICON_NAME_MAP.put("quote", R.drawable.ic_action_quote);
        CUSTOM_TABS_ICON_NAME_MAP.put("search", R.drawable.ic_action_search);
        CUSTOM_TABS_ICON_NAME_MAP.put("staggered", R.drawable.ic_action_view_quilt);
        CUSTOM_TABS_ICON_NAME_MAP.put("star", R.drawable.ic_action_star);
        CUSTOM_TABS_ICON_NAME_MAP.put("trends", R.drawable.ic_action_trends);
        CUSTOM_TABS_ICON_NAME_MAP.put("twidere", R.drawable.ic_action_twidere);
        CUSTOM_TABS_ICON_NAME_MAP.put("twitter", R.drawable.ic_action_twitter);
        CUSTOM_TABS_ICON_NAME_MAP.put("user", R.drawable.ic_action_user);
    }

    public static String findTabIconKey(final int iconRes) {
        for (final Entry<String, Integer> entry : getIconMap().entrySet()) {
            if (entry.getValue() == iconRes) return entry.getKey();
        }
        return null;
    }

    public static String findTabType(final Class<? extends Fragment> cls) {
        for (final Entry<String, CustomTabConfiguration> entry : getConfigurationMap().entrySet()) {
            if (cls == entry.getValue().getFragmentClass()) return entry.getKey();
        }
        return null;
    }


    public static HashMap<String, CustomTabConfiguration> getConfigurationMap() {
        return new HashMap<>(CUSTOM_TABS_CONFIGURATION_MAP);
    }

    public static List<SupportTabSpec> getHomeTabs(final Context context) {
        if (context == null) return Collections.emptyList();
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
        if (cur == null) return Collections.emptyList();
        final ArrayList<SupportTabSpec> tabs = new ArrayList<>();
        cur.moveToFirst();
        TabCursorIndices indices = new TabCursorIndices(cur);
        final int idxArguments = cur.getColumnIndex(Tabs.ARGUMENTS);
        final int idxExtras = cur.getColumnIndex(Tabs.EXTRAS);
        while (!cur.isAfterLast()) {
            @CustomTabType
            final String type = getTabTypeAlias(cur.getString(indices.type));
            final int position = cur.getInt(indices.position);
            final String iconType = cur.getString(indices.icon);
            final String name = cur.getString(indices.name);
            final Bundle args = ParseUtils.jsonToBundle(cur.getString(idxArguments));
            @ReadPositionTag
            final String tag = getTagByType(type);
            args.putInt(EXTRA_TAB_POSITION, position);
            args.putBundle(EXTRA_EXTRAS, ParseUtils.jsonToBundle(cur.getString(idxExtras)));
            final CustomTabConfiguration conf = getTabConfiguration(type);
            final Class<? extends Fragment> cls = conf != null ? conf.getFragmentClass() : InvalidTabFragment.class;
            final String tabTypeName = getTabTypeName(context, type);
            final Object tabIconObject = getTabIconObject(iconType);
            tabs.add(new SupportTabSpec(TextUtils.isEmpty(name) ? tabTypeName : name, tabIconObject,
                    type, cls, args, position, tag));
            cur.moveToNext();
        }
        cur.close();
        Collections.sort(tabs);
        return tabs;
    }

    @Nullable
    @ReadPositionTag
    private static String getTagByType(@NonNull @CustomTabType String tabType) {
        switch (getTabTypeAlias(tabType)) {
            case CustomTabType.HOME_TIMELINE:
                return ReadPositionTag.HOME_TIMELINE;
            case "activities_about_me":
            case CustomTabType.NOTIFICATIONS_TIMELINE:
                return ReadPositionTag.ACTIVITIES_ABOUT_ME;
            case CustomTabType.DIRECT_MESSAGES:
                return ReadPositionTag.DIRECT_MESSAGES;
        }
        return null;
    }

    public static HashMap<String, Integer> getIconMap() {
        return new HashMap<>(CUSTOM_TABS_ICON_NAME_MAP);
    }

    public static CustomTabConfiguration getTabConfiguration(final String tabType) {
        if (tabType == null) return null;
        return CUSTOM_TABS_CONFIGURATION_MAP.get(getTabTypeAlias(tabType));
    }

    @CustomTabType
    public static String getTabTypeAlias(String key) {
        if (key == null) return null;
        switch (key) {
            case "mentions_timeline":
            case "activities_about_me":
                return CustomTabType.NOTIFICATIONS_TIMELINE;
        }
        return key;
    }

    public static Drawable getTabIconDrawable(final Context context, final Object iconObj) {
        return getTabIconDrawable(context.getResources(), iconObj);
    }

    public static Drawable getTabIconDrawable(final Resources res, final Object iconObj) {
        if (res == null) return null;
        if (iconObj instanceof Integer) {
            try {
                return ResourcesCompat.getDrawable(res, (Integer) iconObj, null);
            } catch (final Resources.NotFoundException e) {
                // Ignore.
            }
        } else if (iconObj instanceof Bitmap)
            return new BitmapDrawable(res, (Bitmap) iconObj);
        else if (iconObj instanceof Drawable)
            return (Drawable) iconObj;
        else if (iconObj instanceof File) {
            final Bitmap b = getTabIconFromFile((File) iconObj, res);
            if (b != null) return new BitmapDrawable(res, b);
        }
        return ResourcesCompat.getDrawable(res, R.drawable.ic_action_list, null);
    }

    public static Bitmap getTabIconFromFile(final File file, final Resources res) {
        if (file == null || !file.exists()) return null;
        final String path = file.getPath();
        final BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        if (o.outHeight <= 0 || o.outWidth <= 0) return null;
        o.inSampleSize = (int) (Math.max(o.outWidth, o.outHeight) / (48 * res.getDisplayMetrics().density));
        o.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, o);
    }

    public static Object getTabIconObject(final String type) {
        if (type == null) return R.drawable.ic_action_list;
        final Integer value = CUSTOM_TABS_ICON_NAME_MAP.get(type);
        if (value != null)
            return value;
        else if (type.contains("/")) {
            try {
                final File file = new File(type);
                if (file.exists()) return file;
            } catch (final Exception e) {
                return R.drawable.ic_action_list;
            }
        }
        return R.drawable.ic_action_list;
    }

    public static String getTabTypeName(final Context context, final String type) {
        if (context == null) return null;
        final CustomTabConfiguration conf = getTabConfiguration(type);
        final Integer res_id = conf != null ? conf.getDefaultTitle() : null;
        return res_id != null ? context.getString(res_id) : null;
    }

    public static boolean isSingleTab(final String type) {
        if (type == null) return false;
        final CustomTabConfiguration conf = getTabConfiguration(type);
        return conf != null && conf.isSingleTab();
    }

    public static boolean isTabAdded(final Context context, final String type) {
        if (context == null || type == null) return false;
        final ContentResolver resolver = context.getContentResolver();
        final String where = Tabs.TYPE + " = ?";
        final Cursor cur = resolver.query(Tabs.CONTENT_URI, new String[0], where, new String[]{type},
                Tabs.DEFAULT_SORT_ORDER);
        if (cur == null) return false;
        final boolean added = cur.getCount() > 0;
        cur.close();
        return added;
    }

    public static boolean isTabTypeValid(final String tabType) {
        return tabType != null && CUSTOM_TABS_CONFIGURATION_MAP.containsKey(getTabTypeAlias(tabType));
    }

    public static boolean hasAccountId(final Context context, @NonNull final Bundle args,
                                       final AccountKey[] activatedAccountKeys, AccountKey accountKey) {
        final AccountKey[] accountKeys = Utils.getAccountKeys(context, args);
        if (accountKeys != null) {
            return ArrayUtils.contains(accountKeys, accountKey);
        }
        if (activatedAccountKeys != null) {
            return ArrayUtils.contains(activatedAccountKeys, accountKey);
        }
        return false;
    }
}
