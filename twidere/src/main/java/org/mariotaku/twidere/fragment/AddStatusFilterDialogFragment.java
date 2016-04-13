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

package org.mariotaku.twidere.fragment;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.twitter.Extractor;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddStatusFilterDialogFragment extends BaseSupportDialogFragment {

    public static final String FRAGMENT_TAG = "add_status_filter";

    private final Extractor mExtractor = new Extractor();
    private FilterItemInfo[] mFilterItems;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        mFilterItems = getFilterItemsInfo();
        final String[] entries = new String[mFilterItems.length];
        final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        for (int i = 0, j = entries.length; i < j; i++) {
            final FilterItemInfo info = mFilterItems[i];
            switch (info.type) {
                case FilterItemInfo.FILTER_TYPE_USER:
                    entries[i] = getString(R.string.user_filter_name, getName(mUserColorNameManager,
                            info.value, nameFirst));
                    break;
                case FilterItemInfo.FILTER_TYPE_KEYWORD:
                    entries[i] = getString(R.string.keyword_filter_name, getName(mUserColorNameManager,
                            info.value, nameFirst));
                    break;
                case FilterItemInfo.FILTER_TYPE_SOURCE:
                    entries[i] = getString(R.string.source_filter_name, getName(mUserColorNameManager,
                            info.value, nameFirst));
                    break;
            }
        }
        builder.title(R.string.add_to_filter);
        builder.items(entries);
        builder.positiveText(android.R.string.ok);
        builder.itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                return false;
            }
        });
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                final Integer[] selectedIndices = dialog.getSelectedIndices();
                assert selectedIndices != null;

                final Set<UserKey> userKeys = new HashSet<>();
                final Set<String> keywords = new HashSet<>();
                final Set<String> sources = new HashSet<>();
                final ArrayList<ContentValues> userValues = new ArrayList<>();
                final ArrayList<ContentValues> keywordValues = new ArrayList<>();
                final ArrayList<ContentValues> sourceValues = new ArrayList<>();
                for (final int idx : selectedIndices) {
                    final FilterItemInfo info = mFilterItems[idx];
                    final Object value = info.value;
                    if (value instanceof ParcelableUserMention) {
                        final ParcelableUserMention mention = (ParcelableUserMention) value;
                        userKeys.add(mention.key);
                        userValues.add(ContentValuesCreator.createFilteredUser(mention));
                    } else if (value instanceof UserItem) {
                        final UserItem item = (UserItem) value;
                        userKeys.add(item.key);
                        userValues.add(createFilteredUser(item));
                    } else if (info.type == FilterItemInfo.FILTER_TYPE_KEYWORD) {
                        if (value != null) {
                            final String keyword = ParseUtils.parseString(value);
                            keywords.add(keyword);
                            final ContentValues values = new ContentValues();
                            values.put(Filters.Keywords.VALUE, "#" + keyword);
                            keywordValues.add(values);
                        }
                    } else if (info.type == FilterItemInfo.FILTER_TYPE_SOURCE && value != null) {
                        final String source = ParseUtils.parseString(value);
                        sources.add(source);
                        final ContentValues values = new ContentValues();
                        values.put(Filters.Sources.VALUE, source);
                        sourceValues.add(values);
                    }
                }
                final ContentResolver resolver = getContentResolver();
                ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.USER_KEY, userKeys, null);
                ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI, Filters.Keywords.VALUE, keywords, null);
                ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI, Filters.Sources.VALUE, sources, null);
                ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, userValues);
                ContentResolverUtils.bulkInsert(resolver, Filters.Keywords.CONTENT_URI, keywordValues);
                ContentResolverUtils.bulkInsert(resolver, Filters.Sources.CONTENT_URI, sourceValues);
            }
        });
        builder.negativeText(android.R.string.cancel);
        return builder.build();
    }

    private FilterItemInfo[] getFilterItemsInfo() {
        final Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_STATUS)) return new FilterItemInfo[0];
        final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
        if (status == null) return new FilterItemInfo[0];
        final ArrayList<FilterItemInfo> list = new ArrayList<>();
        if (status.is_retweet) {
            list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.retweeted_by_user_key,
                    status.retweeted_by_user_name, status.retweeted_by_user_screen_name)));
        }
        if (status.is_quote) {
            list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.quoted_user_key,
                    status.quoted_user_name, status.quoted_user_screen_name)));
        }
        list.add(new FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, new UserItem(status.user_key,
                status.user_name, status.user_screen_name)));
        final ParcelableUserMention[] mentions = status.mentions;
        if (mentions != null) {
            for (final ParcelableUserMention mention : mentions) {
                if (!mention.key.equals(status.user_key)) {
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
            return manager.getDisplayName(mention.key, mention.name, mention.screen_name, nameFirst
            );
        } else if (value instanceof UserItem) {
            final UserItem item = (UserItem) value;
            return manager.getDisplayName(item.key, item.name, item.screen_name, nameFirst);
        } else
            return ParseUtils.parseString(value);
    }

    private static ContentValues createFilteredUser(UserItem item) {
        if (item == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_KEY, item.key.toString());
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
        private final UserKey key;
        private final String name, screen_name;

        public UserItem(UserKey key, String name, String screen_name) {
            this.key = key;
            this.name = name;
            this.screen_name = screen_name;
        }
    }

}
