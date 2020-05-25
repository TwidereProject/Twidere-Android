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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.fragment.InvalidTabFragment;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.Tab;
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
import org.mariotaku.twidere.model.tab.extra.TrendsTabExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.collections.ArraysKt;

public class CustomTabUtils implements Constants {

    private CustomTabUtils() {
    }

    public static List<Tab> getTabs(@NonNull final Context context) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
        if (cur == null) return Collections.emptyList();
        try {
            final ArrayList<Tab> tabs = new ArrayList<>();
            cur.moveToFirst();
            ObjectCursor.CursorIndices<Tab> indices = ObjectCursor.indicesFrom(cur, Tab.class);
            while (!cur.isAfterLast()) {
                tabs.add(indices.newObject(cur));
                cur.moveToNext();
            }
            return tabs;
        } catch (IOException e) {
            return Collections.emptyList();
        } finally {
            cur.close();
        }
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
            final TabConfiguration conf = TabConfiguration.Companion.ofType(type);
            final Class<? extends Fragment> cls = conf != null ? conf.getFragmentClass() : InvalidTabFragment.class;
            final String tabTypeName = getTabTypeName(context, type);
            final DrawableHolder icon = DrawableHolder.parse(iconType);
            specs.add(new SupportTabSpec(TextUtils.isEmpty(name) ? tabTypeName : name, icon,
                    type, cls, args, position, tag));
        }
        return specs;
    }

    /**
     * Remember to make this method correspond to {@link TabArguments#parse(String, String)}
     *
     * @see TabArguments#parse(String, String)
     */
    @Nullable
    public static TabArguments newTabArguments(@NonNull @CustomTabType String type) {
        switch (type) {
            case CustomTabType.HOME_TIMELINE:
            case CustomTabType.NOTIFICATIONS_TIMELINE:
            case CustomTabType.DIRECT_MESSAGES:
            case CustomTabType.TRENDS_SUGGESTIONS:
            case CustomTabType.PUBLIC_TIMELINE:
            case CustomTabType.NETWORK_PUBLIC_TIMELINE: {
                return new TabArguments();
            }
            case CustomTabType.USER_TIMELINE:
            case CustomTabType.FAVORITES: {
                return new UserArguments();
            }
            case CustomTabType.LIST_TIMELINE: {
                return new UserListArguments();
            }
            case CustomTabType.SEARCH_STATUSES: {
                return new TextQueryArguments();
            }
        }
        return null;
    }


    /**
     * Remember to make this method correspond to {@link TabExtras#parse(String, String)}
     *
     * @see TabExtras#parse(String, String)
     */
    @Nullable
    public static TabExtras newTabExtras(@NonNull @CustomTabType String type) {
        switch (type) {
            case CustomTabType.NOTIFICATIONS_TIMELINE: {
                return new InteractionsTabExtras();
            }
            case CustomTabType.LIST_TIMELINE:
            case CustomTabType.HOME_TIMELINE: {
                return new HomeTabExtras();
            }
            case CustomTabType.TRENDS_SUGGESTIONS: {
                return new TrendsTabExtras();
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

    public static Drawable getTabIconDrawable(final Context context, final DrawableHolder icon) {
        if (icon == null) {
            return ContextCompat.getDrawable(context, R.drawable.ic_action_list);
        }
        return icon.createDrawable(context);
    }

    public static String getTabTypeName(final Context context, @CustomTabType final String type) {
        if (context == null) return null;
        final TabConfiguration conf = TabConfiguration.Companion.ofType(type);
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

    public static boolean isTabTypeValid(@NonNull final String tabType) {
        return TabConfiguration.Companion.ofType(Tab.getTypeAlias(tabType)) != null;
    }

    public static boolean hasAccountKey(final Context context, @NonNull final Bundle args,
            final UserKey[] activatedAccountKeys, UserKey accountKey) {
        final UserKey[] accountKeys = Utils.INSTANCE.getAccountKeys(context, args);
        if (accountKeys != null) {
            return ArraysKt.contains(accountKeys, accountKey);
        }
        if (activatedAccountKeys != null) {
            return ArraysKt.contains(activatedAccountKeys, accountKey);
        }
        return false;
    }
}
