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

package org.mariotaku.twidere.fragment.support;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.twitter.Extractor;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddStatusFilterDialogFragment extends BaseSupportDialogFragment implements OnMultiChoiceClickListener,
        OnClickListener {

    public static final String FRAGMENT_TAG = "add_status_filter";

    private final Extractor mExtractor = new Extractor();
    private FilterItemInfo[] mFilterItems;
    private final Set<FilterItemInfo> mCheckedFilterItems = new HashSet<>();

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final Set<Long> user_ids = new HashSet<>();
        final Set<String> keywords = new HashSet<>();
        final Set<String> sources = new HashSet<>();
        final ArrayList<ContentValues> userValues = new ArrayList<>();
        final ArrayList<ContentValues> keywordValues = new ArrayList<>();
        final ArrayList<ContentValues> sourceValues = new ArrayList<>();
        for (final FilterItemInfo info : mCheckedFilterItems) {
            final Object value = info.value;
            if (value instanceof ParcelableUserMention) {
                final ParcelableUserMention mention = (ParcelableUserMention) value;
                user_ids.add(mention.id);
                userValues.add(ContentValuesCreator.createFilteredUser(mention));
            } else if (value instanceof UserItem) {
                final UserItem item = (UserItem) value;
                user_ids.add(item.id);
                userValues.add(createFilteredUser(item));
            } else if (info.type == FilterItemInfo.FILTER_TYPE_KEYWORD) {
                if (value != null) {
                    final String keyword = ParseUtils.parseString(value);
                    keywords.add(keyword);
                    final ContentValues values = new ContentValues();
                    values.put(Filters.Keywords.VALUE, "#" + keyword);
                    keywordValues.add(values);
                }
            } else if (info.type == FilterItemInfo.FILTER_TYPE_SOURCE) {
                if (value != null) {
                    final String source = ParseUtils.parseString(value);
                    sources.add(source);
                    final ContentValues values = new ContentValues();
                    values.put(Filters.Sources.VALUE, source);
                    sourceValues.add(values);
                }
            }
        }
        final ContentResolver resolver = getContentResolver();
        ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.USER_ID, user_ids, null, false);
        ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI, Filters.Keywords.VALUE, keywords, null, true);
        ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI, Filters.Sources.VALUE, sources, null, true);
        ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, userValues);
        ContentResolverUtils.bulkInsert(resolver, Filters.Keywords.CONTENT_URI, keywordValues);
        ContentResolverUtils.bulkInsert(resolver, Filters.Sources.CONTENT_URI, sourceValues);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
        if (isChecked) {
            mCheckedFilterItems.add(mFilterItems[which]);
        } else {
            mCheckedFilterItems.remove(mFilterItems[which]);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
        mFilterItems = getFilterItemsInfo();
        final String[] entries = new String[mFilterItems.length];
        final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        for (int i = 0, j = entries.length; i < j; i++) {
            final FilterItemInfo info = mFilterItems[i];
            switch (info.type) {
                case FilterItemInfo.FILTER_TYPE_USER:
                    entries[i] = getString(R.string.user_filter_name, getName(mUserColorNameManager, info.value, nameFirst));
                    break;
                case FilterItemInfo.FILTER_TYPE_KEYWORD:
                    entries[i] = getString(R.string.keyword_filter_name, getName(mUserColorNameManager, info.value, nameFirst));
                    break;
                case FilterItemInfo.FILTER_TYPE_SOURCE:
                    entries[i] = getString(R.string.source_filter_name, getName(mUserColorNameManager, info.value, nameFirst));
                    break;
            }
        }
        builder.setTitle(R.string.add_to_filter);
        builder.setMultiChoiceItems(entries, null, this);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private FilterItemInfo[] getFilterItemsInfo() {
        final Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_STATUS)) return new FilterItemInfo[0];
        final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
        if (status == null) return new FilterItemInfo[0];
        final ArrayList<FilterItemInfo> list = new ArrayList<>();
        if (status.is_retweet) {
            list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.retweeted_by_user_id,
                    status.retweeted_by_user_name, status.retweeted_by_user_screen_name)));
        }
        if (status.is_quote) {
            list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.quoted_user_id,
                    status.quoted_user_name, status.quoted_user_screen_name)));
        }
        list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.user_id,
                status.user_name, status.user_screen_name)));
        final ParcelableUserMention[] mentions = status.mentions;
        if (mentions != null) {
            for (final ParcelableUserMention mention : mentions) {
                if (mention.id != status.user_id) {
                    list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, mention));
                }
            }
        }
        final HashSet<String> hashtags = new HashSet<>();
        hashtags.addAll(mExtractor.extractHashtags(status.text_plain));
        for (final String hashtag : hashtags) {
            list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_KEYWORD, hashtag));
        }
        final String source = HtmlEscapeHelper.toPlainText(status.source);
        list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_SOURCE, source));
        return list.toArray(new FilterItemInfo[list.size()]);
    }

    private String getName(final UserColorNameManager manager, final Object value, boolean nameFirst) {
        if (value instanceof ParcelableUserMention) {
            final ParcelableUserMention mention = (ParcelableUserMention) value;
            return manager.getDisplayName(mention.id, mention.name, mention.screen_name, nameFirst,
                    true);
        } else if (value instanceof UserItem) {
            final UserItem item = (UserItem) value;
            return manager.getDisplayName(item.id, item.name, item.screen_name, nameFirst, true);
        } else
            return ParseUtils.parseString(value);
    }

    private static ContentValues createFilteredUser(UserItem item) {
        if (item == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_ID, item.id);
        values.put(Filters.Users.NAME, item.name);
        values.put(Filters.Users.SCREEN_NAME, item.screen_name);
        return values;
    }

    public static AddStatusFilterDialogFragment show(final FragmentManager fm, final ParcelableStatus status) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        final AddStatusFilterDialogFragment f = new AddStatusFilterDialogFragment();
        f.setArguments(args);
        f.show(fm, FRAGMENT_TAG);
        return f;
    }

    private static class FilterItemInfo {

        static final int FILTER_TYPE_USER = 1;
        static final int FILTER_TYPE_KEYWORD = 2;
        static final int FILTER_TYPE_SOURCE = 3;

        final int type;
        final Object value;

        FilterItemInfo(final int type, final Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof FilterItemInfo)) return false;
            final FilterItemInfo other = (FilterItemInfo) obj;
            if (type != other.type) return false;
            if (value == null) {
                if (other.value != null) return false;
            } else if (!value.equals(other.value)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + type;
            result = prime * result + (value == null ? 0 : value.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "FilterItemInfo{type=" + type + ", value=" + value + "}";
        }

    }

    private static class UserItem {
        private final long id;
        private final String name, screen_name;

        public UserItem(long id, String name, String screen_name) {
            this.id = id;
            this.name = name;
            this.screen_name = screen_name;
        }
    }

}
