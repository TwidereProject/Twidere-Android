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

package org.mariotaku.twidere.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;


@ParcelablePlease(allFields = false)
@JsonObject
@CursorObject(valuesCreator = true)
public class ParcelableUser implements Parcelable, Comparable<ParcelableUser> {

    @ParcelableThisPlease
    @JsonField(name = "account_id")
    public long account_id;

    @ParcelableThisPlease
    public int account_color;

    @ParcelableThisPlease
    @JsonField(name = "id")
    @CursorField(CachedUsers.USER_ID)
    public long id;
    @ParcelableThisPlease
    @JsonField(name = "created_at")
    @CursorField(CachedUsers.CREATED_AT)
    public long created_at;
    @ParcelableThisPlease
    @JsonField(name = "position")
    public long position;

    @ParcelableThisPlease
    @JsonField(name = "is_protected")
    @CursorField(CachedUsers.IS_PROTECTED)
    public boolean is_protected;
    @ParcelableThisPlease
    @JsonField(name = "is_verified")
    @CursorField(CachedUsers.IS_VERIFIED)
    public boolean is_verified;
    @ParcelableThisPlease
    @JsonField(name = "is_follow_request_sent")
    public boolean is_follow_request_sent;
    @ParcelableThisPlease
    @JsonField(name = "is_following")
    @CursorField(CachedUsers.IS_FOLLOWING)
    public boolean is_following;

    @ParcelableThisPlease
    @JsonField(name = "description_plain")
    @CursorField(CachedUsers.DESCRIPTION_PLAIN)
    public String description_plain;
    @ParcelableThisPlease
    @JsonField(name = "name")
    @CursorField(CachedUsers.NAME)
    public String name;
    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    @CursorField(CachedUsers.SCREEN_NAME)
    public String screen_name;
    @ParcelableThisPlease
    @JsonField(name = "location")
    @CursorField(CachedUsers.LOCATION)
    public String location;
    @ParcelableThisPlease
    @JsonField(name = "profile_image_url")
    @CursorField(CachedUsers.PROFILE_IMAGE_URL)
    public String profile_image_url;
    @ParcelableThisPlease
    @JsonField(name = "profile_banner_url")
    @CursorField(CachedUsers.PROFILE_BANNER_URL)
    public String profile_banner_url;
    @ParcelableThisPlease
    @JsonField(name = "url")
    @CursorField(CachedUsers.URL)
    public String url;
    @ParcelableThisPlease
    @JsonField(name = "url_expanded")
    @CursorField(CachedUsers.URL_EXPANDED)
    public String url_expanded;
    @ParcelableThisPlease
    @JsonField(name = "description_html")
    @CursorField(CachedUsers.DESCRIPTION_HTML)
    public String description_html;
    @ParcelableThisPlease
    @JsonField(name = "description_unescaped")
    public String description_unescaped;
    @ParcelableThisPlease
    @JsonField(name = "description_expanded")
    @CursorField(CachedUsers.DESCRIPTION_EXPANDED)
    public String description_expanded;

    @ParcelableThisPlease
    @JsonField(name = "followers_count")
    @CursorField(CachedUsers.FOLLOWERS_COUNT)
    public long followers_count;
    @ParcelableThisPlease
    @JsonField(name = "friends_count")
    @CursorField(CachedUsers.FRIENDS_COUNT)
    public long friends_count;
    @ParcelableThisPlease
    @JsonField(name = "statuses_count")
    @CursorField(CachedUsers.STATUSES_COUNT)
    public long statuses_count;
    @ParcelableThisPlease
    @JsonField(name = "favorites_count")
    @CursorField(CachedUsers.FAVORITES_COUNT)
    public long favorites_count;
    @ParcelableThisPlease
    @JsonField(name = "listed_count")
    @CursorField(CachedUsers.LISTED_COUNT)
    public long listed_count;
    @ParcelableThisPlease
    @JsonField(name = "media_count")
    @CursorField(CachedUsers.MEDIA_COUNT)
    public long media_count;

    @ParcelableThisPlease
    @JsonField(name = "background_color")
    @CursorField(CachedUsers.BACKGROUND_COLOR)
    public int background_color;
    @ParcelableThisPlease
    @JsonField(name = "link_color")
    @CursorField(CachedUsers.LINK_COLOR)
    public int link_color;
    @ParcelableThisPlease
    @JsonField(name = "text_color")
    @CursorField(CachedUsers.TEXT_COLOR)
    public int text_color;

    @ParcelableThisPlease
    @JsonField(name = "is_cache")
    public boolean is_cache;
    @ParcelableThisPlease
    @JsonField(name = "is_basic")
    public boolean is_basic;
    public static final Creator<ParcelableUser> CREATOR = new Creator<ParcelableUser>() {
        public ParcelableUser createFromParcel(Parcel source) {
            ParcelableUser target = new ParcelableUser();
            ParcelableUserParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableUser[] newArray(int size) {
            return new ParcelableUser[size];
        }
    };

    public ParcelableUser() {
    }

    public ParcelableUser(final long account_id, final long id, final String name,
                          final String screen_name, final String profile_image_url) {
        this.account_id = account_id;
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.profile_image_url = profile_image_url;
        this.created_at = 0;
        this.position = 0;
        is_protected = false;
        is_verified = false;
        is_follow_request_sent = false;
        is_following = false;
        description_plain = null;
        location = null;
        profile_banner_url = null;
        url = null;
        url_expanded = null;
        description_html = null;
        description_unescaped = null;
        description_expanded = null;
        followers_count = 0;
        friends_count = 0;
        statuses_count = 0;
        favorites_count = 0;
        listed_count = 0;
        media_count = 0;
        background_color = 0;
        link_color = 0;
        text_color = 0;
        is_cache = true;
        is_basic = true;
    }

    public ParcelableUser(final User user, final long account_id) {
        this(user, account_id, 0);
    }

    public ParcelableUser(final User user, final long account_id, final long position) {
        this.position = position;
        this.account_id = account_id;
        final UrlEntity[] urls_url_entities = user.getUrlEntities();
        id = user.getId();
        created_at = user.getCreatedAt().getTime();
        is_protected = user.isProtected();
        is_verified = user.isVerified();
        name = user.getName();
        screen_name = user.getScreenName();
        description_plain = user.getDescription();
        description_html = TwitterContentUtils.formatUserDescription(user);
        description_expanded = TwitterContentUtils.formatExpandedUserDescription(user);
        description_unescaped = HtmlEscapeHelper.toPlainText(description_html);
        location = user.getLocation();
        profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        profile_banner_url = user.getProfileBannerImageUrl();
        url = user.getUrl();
        url_expanded = url != null && urls_url_entities != null && urls_url_entities.length > 0 ? urls_url_entities[0].getExpandedUrl() : null;
        is_follow_request_sent = user.isFollowRequestSent();
        followers_count = user.getFollowersCount();
        friends_count = user.getFriendsCount();
        statuses_count = user.getStatusesCount();
        favorites_count = user.getFavouritesCount();
        listed_count = user.getListedCount();
        media_count = user.getMediaCount();
        is_following = user.isFollowing();
        background_color = ParseUtils.parseColor("#" + user.getProfileBackgroundColor(), 0);
        link_color = ParseUtils.parseColor("#" + user.getProfileLinkColor(), 0);
        text_color = ParseUtils.parseColor("#" + user.getProfileTextColor(), 0);
        is_cache = false;
        is_basic = false;
    }

    public static int calculateHashCode(long accountId, long userId) {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (accountId ^ accountId >>> 32);
        result = prime * result + (int) (userId ^ userId >>> 32);
        return result;
    }

    public static ParcelableUser[] fromUsersArray(@Nullable final User[] users, long account_id) {
        if (users == null) return null;
        final ParcelableUser[] result = new ParcelableUser[users.length];
        for (int i = 0, j = users.length; i < j; i++) {
            result[i] = new ParcelableUser(users[i], account_id);
        }
        return result;
    }

    public static ParcelableUser fromDirectMessageConversationEntry(final Cursor cursor) {
        final long account_id = cursor.getLong(ConversationEntries.IDX_ACCOUNT_ID);
        final long id = cursor.getLong(ConversationEntries.IDX_CONVERSATION_ID);
        final String name = cursor.getString(ConversationEntries.IDX_NAME);
        final String screen_name = cursor.getString(ConversationEntries.IDX_SCREEN_NAME);
        final String profile_image_url = cursor.getString(ConversationEntries.IDX_PROFILE_IMAGE_URL);
        return new ParcelableUser(account_id, id, name, screen_name, profile_image_url);
    }

    public static ParcelableUser[] fromUsers(final User[] users, long accountId) {
        if (users == null) return null;
        int size = users.length;
        final ParcelableUser[] result = new ParcelableUser[size];
        for (int i = 0; i < size; i++) {
            result[i] = new ParcelableUser(users[i], accountId);
        }
        return result;
    }

    @AfterCursorObjectCreated
    void afterCursorObjectCreated() {
        is_cache = true;
        description_unescaped = HtmlEscapeHelper.toPlainText(description_html);
        is_basic = description_plain == null || url == null || location == null;
    }

    @Override
    public int compareTo(@NonNull final ParcelableUser that) {
        final long diff = position - that.position;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableUser)) return false;
        final ParcelableUser other = (ParcelableUser) obj;
        if (account_id != other.account_id) return false;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_id, id);
    }

    @Override
    public String toString() {
        return "ParcelableUser{account_id=" + account_id + ", id=" + id + ", created_at=" + created_at + ", position="
                + position + ", is_protected=" + is_protected + ", is_verified=" + is_verified
                + ", is_follow_request_sent=" + is_follow_request_sent + ", is_following=" + is_following
                + ", description_plain=" + description_plain + ", name=" + name + ", screen_name=" + screen_name
                + ", location=" + location + ", profile_image_url=" + profile_image_url + ", profile_banner_url="
                + profile_banner_url + ", url=" + url + ", url_expanded=" + url_expanded + ", description_html="
                + description_html + ", description_unescaped=" + description_unescaped + ", description_expanded="
                + description_expanded + ", followers_count=" + followers_count + ", friends_count=" + friends_count
                + ", statuses_count=" + statuses_count + ", favorites_count=" + favorites_count + ", is_cache="
                + is_cache + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableUserParcelablePlease.writeToParcel(this, dest, flags);
    }
}
