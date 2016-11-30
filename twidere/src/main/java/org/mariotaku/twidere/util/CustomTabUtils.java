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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.fragment.InvalidTabFragment;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.TabCursorIndices;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.argument.TabArguments;
import org.mariotaku.twidere.model.tab.argument.TextQueryArguments;
import org.mariotaku.twidere.model.tab.argument.UserArguments;
import org.mariotaku.twidere.model.tab.argument.UserListArguments;
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras;
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras;
import org.mariotaku.twidere.model.tab.extra.TabExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomTabUtils implements Constants {
    private static final SimpleArrayMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new SimpleArrayMap<>();

    private CustomTabUtils() {
    }

    @Nullable
    public static String findTabIconKey(final int iconRes) {
        for (int i = 0, j = CUSTOM_TABS_ICON_NAME_MAP.size(); i < j; i++) {
            if (CUSTOM_TABS_ICON_NAME_MAP.valueAt(i) == iconRes) {
                return CUSTOM_TABS_ICON_NAME_MAP.keyAt(i);
            }
        }
        return null;
    }


    public static List<Tab> getTabs(@NonNull final Context context) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
        if (cur == null) return Collections.emptyList();
        final ArrayList<Tab> tabs = new ArrayList<>();
        cur.moveToFirst();
        TabCursorIndices indices = new TabCursorIndices(cur);
        while (!cur.isAfterLast()) {
            tabs.add(indices.newObject(cur));
            cur.moveToNext();
        }
        return tabs;
    }

    public static List<SupportTabSpec> getHomeTabs(@NonNull final Context context) {
        List<SupportTabSpec> specs = new ArrayList<>();
        for (Tab tab : getTabs(context)) {
            @CustomTabType
            final String type = tab.getType();
            final int position = tab.getPosition();
            final String iconType = tab.getIcon();
            final String name = tab.getName();
            final Bundle args = new Bundle();
            final TabArguments tabArguments = tab.getArguments();
            if (tabArguments != null) {
                tabArguments.copyToBundle(args);
            }
            @ReadPositionTag
            final String tag = getTagByType(type);
            args.putInt(EXTRA_TAB_POSITION, position);
            args.putLong(EXTRA_TAB_ID, tab.getId());
            final TabExtras tabExtras = tab.getExtras();
            if (tabExtras != null) {
                args.putParcelable(EXTRA_EXTRAS, tabExtras);
            }
            final TabConfiguration conf = TabConfiguration.ofType(type);
            final Class<? extends Fragment> cls = conf != null ? conf.getFragmentClass() : InvalidTabFragment.class;
            final String tabTypeName = getTabTypeName(context, type);
            final DrawableHolder icon = DrawableHolder.parse(iconType);
            specs.add(new SupportTabSpec(TextUtils.isEmpty(name) ? tabTypeName : name, icon,
                    type, cls, args, position, tag));
        }
        return specs;
    }

    @Nullable
    public static TabArguments newTabArguments(@NonNull @CustomTabType String type) {
        return parseTabArguments(type, "{}");
    }

    @Nullable
    public static TabExtras newTabExtras(@NonNull @CustomTabType String type) {
        return parseTabExtras(type, "{}");
    }

    @Nullable
    public static TabArguments parseTabArguments(@NonNull @CustomTabType String type, String json) {
        switch (type) {
            case CustomTabType.HOME_TIMELINE:
            case CustomTabType.NOTIFICATIONS_TIMELINE:
            case CustomTabType.DIRECT_MESSAGES: {
                return JsonSerializer.parse(json, TabArguments.class);
            }
            case CustomTabType.USER_TIMELINE:
            case CustomTabType.FAVORITES: {
                return JsonSerializer.parse(json, UserArguments.class);
            }
            case CustomTabType.LIST_TIMELINE: {
                return JsonSerializer.parse(json, UserListArguments.class);
            }
            case CustomTabType.SEARCH_STATUSES: {
                return JsonSerializer.parse(json, TextQueryArguments.class);
            }
        }
        return null;
    }

    @Nullable
    public static TabExtras parseTabExtras(@NonNull @CustomTabType String type, String json) {
        switch (type) {
            case CustomTabType.NOTIFICATIONS_TIMELINE: {
                return JsonSerializer.parse(json, InteractionsTabExtras.class);
            }
            case CustomTabType.HOME_TIMELINE: {
                return JsonSerializer.parse(json, HomeTabExtras.class);
            }
        }
        return null;
    }

    @Nullable
    @ReadPositionTag
    public static String getTagByType(@NonNull @CustomTabType String tabType) {
        switch (tabType) {
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

    public static TabConfiguration getTabConfiguration(final String tabType) {
        if (tabType == null) return null;
        return TabConfiguration.ofType(getTabTypeAlias(tabType));
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

    public static Drawable getTabIconDrawable(final Context context, final DrawableHolder icon) {
        if (icon == null) {
            return ContextCompat.getDrawable(context, R.drawable.ic_action_list);
        }
        return icon.createDrawable(context);
    }

    public static String getTabTypeName(final Context context, final String type) {
        if (context == null) return null;
        final TabConfiguration conf = TabConfiguration.ofType(type);
        if (conf == null) return null;
        return conf.getName().createString(context);
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
        return tabType != null && TabConfiguration.ofType(getTabTypeAlias(tabType)) != null;
    }

    public static boolean hasAccountId(final Context context, @NonNull final Bundle args,
                                       final UserKey[] activatedAccountKeys, UserKey accountKey) {
        final UserKey[] accountKeys = Utils.getAccountKeys(context, args);
        if (accountKeys != null) {
            return ArrayUtils.contains(accountKeys, accountKey);
        }
        if (activatedAccountKeys != null) {
            return ArrayUtils.contains(activatedAccountKeys, accountKey);
        }
        return false;
    }
}
