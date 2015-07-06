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

import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;

@JsonObject
public class ParcelableUser implements Parcelable, Comparable<ParcelableUser> {

    public static final Parcelable.Creator<ParcelableUser> CREATOR = new Parcelable.Creator<ParcelableUser>() {
        @Override
        public ParcelableUser createFromParcel(final Parcel in) {
            return new ParcelableUser(in);
        }

        @Override
        public ParcelableUser[] newArray(final int size) {
            return new ParcelableUser[size];
        }
    };


    @JsonField(name = "account_id")
    public long account_id;
    @JsonField(name = "id")
    public long id;
    @JsonField(name = "created_at")
    public long created_at;
    @JsonField(name = "position")
    public long position;

    @JsonField(name = "is_protected")
    public boolean is_protected;
    @JsonField(name = "is_verified")
    public boolean is_verified;
    @JsonField(name = "is_follow_request_sent")
    public boolean is_follow_request_sent;
    @JsonField(name = "is_following")
    public boolean is_following;

    @JsonField(name = "description_plain")
    public String description_plain;
    @JsonField(name = "name")
    public String name;
    @JsonField(name = "screen_name")
    public String screen_name;
    @JsonField(name = "location")
    public String location;
    @JsonField(name = "profile_image_url")
    public String profile_image_url;
    @JsonField(name = "profile_banner_url")
    public String profile_banner_url;
    @JsonField(name = "url")
    public String url;
    @JsonField(name = "url_expanded")
    public String url_expanded;
    @JsonField(name = "description_html")
    public String description_html;
    @JsonField(name = "description_unescaped")
    public String description_unescaped;
    @JsonField(name = "description_expanded")
    public String description_expanded;

    @JsonField(name = "followers_count")
    public long followers_count;
    @JsonField(name = "friends_count")
    public long friends_count;
    @JsonField(name = "statuses_count")
    public long statuses_count;
    @JsonField(name = "favorites_count")
    public long favorites_count;
    @JsonField(name = "listed_count")
    public long listed_count;
    @JsonField(name = "media_count")
    public long media_count;

    @JsonField(name = "background_color")
    public int background_color;
    @JsonField(name = "link_color")
    public int link_color;
    @JsonField(name = "text_color")
    public int text_color;

    @JsonField(name = "is_cache")
    public boolean is_cache;
    @JsonField(name = "is_basic")
    public boolean is_basic;

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

    public ParcelableUser(final Cursor cursor, CachedIndices indices, final long account_id) {
        this.account_id = account_id;
        position = -1;
        is_follow_request_sent = false;
        id = indices.id != -1 ? cursor.getLong(indices.id) : -1;
        name = indices.name != -1 ? cursor.getString(indices.name) : null;
        screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
        profile_image_url = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
        created_at = indices.created_at != -1 ? cursor.getLong(indices.created_at) : -1;
        is_protected = indices.is_protected != -1 && cursor.getInt(indices.is_protected) == 1;
        is_verified = indices.is_verified != -1 && cursor.getInt(indices.is_verified) == 1;
        favorites_count = indices.favorites_count != -1 ? cursor.getInt(indices.favorites_count) : 0;
        listed_count = indices.listed_count != -1 ? cursor.getInt(indices.listed_count) : 0;
        followers_count = indices.followers_count != -1 ? cursor.getInt(indices.followers_count) : 0;
        friends_count = indices.friends_count != -1 ? cursor.getInt(indices.friends_count) : 0;
        statuses_count = indices.statuses_count != -1 ? cursor.getInt(indices.statuses_count) : 0;
        location = indices.location != -1 ? cursor.getString(indices.location) : null;
        description_plain = indices.description_plain != -1 ? cursor.getString(indices.description_plain) : null;
        description_html = indices.description_html != -1 ? cursor.getString(indices.description_html) : null;
        description_expanded = indices.description_expanded != -1 ? cursor.getString(indices.description_expanded) : null;
        url = indices.url != -1 ? cursor.getString(indices.url) : null;
        url_expanded = indices.url_expanded != -1 ? cursor.getString(indices.url_expanded) : null;
        profile_banner_url = indices.profile_banner_url != -1 ? cursor.getString(indices.profile_banner_url) : null;
        description_unescaped = HtmlEscapeHelper.toPlainText(description_html);
        is_following = indices.is_following != -1 && cursor.getInt(indices.is_following) == 1;
        background_color = indices.background_color != -1 ? cursor.getInt(indices.background_color) : 0;
        link_color = indices.link_color != -1 ? cursor.getInt(indices.link_color) : 0;
        text_color = indices.text_color != -1 ? cursor.getInt(indices.text_color) : 0;
        media_count = indices.media_count != -1 ? cursor.getInt(indices.media_count) : 0;
        is_cache = true;
        is_basic = indices.description_plain == -1 || indices.url == -1 || indices.location == -1;
    }


    public ParcelableUser(final Parcel in) {
        position = in.readLong();
        account_id = in.readLong();
        id = in.readLong();
        created_at = in.readLong();
        is_protected = in.readInt() == 1;
        is_verified = in.readInt() == 1;
        name = in.readString();
        screen_name = in.readString();
        description_plain = in.readString();
        description_html = in.readString();
        description_expanded = in.readString();
        description_unescaped = in.readString();
        location = in.readString();
        profile_image_url = in.readString();
        profile_banner_url = in.readString();
        url = in.readString();
        is_follow_request_sent = in.readInt() == 1;
        followers_count = in.readLong();
        friends_count = in.readLong();
        statuses_count = in.readLong();
        favorites_count = in.readLong();
        listed_count = in.readLong();
        media_count = in.readLong();
        url_expanded = in.readString();
        is_following = in.readInt() == 1;
        background_color = in.readInt();
        link_color = in.readInt();
        text_color = in.readInt();
        is_cache = in.readInt() == 1;
        is_basic = in.readInt() == 1;
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

    @Override
    public int compareTo(@NonNull final ParcelableUser that) {
        final long diff = position - that.position;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static ParcelableUser[] fromUsersArray(@Nullable final User[] users, long account_id) {
        if (users == null) return null;
        final ParcelableUser[] result = new ParcelableUser[users.length];
        for (int i = 0, j = users.length; i < j; i++) {
            result[i] = new ParcelableUser(users[i], account_id);
        }
        return result;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeLong(position);
        out.writeLong(account_id);
        out.writeLong(id);
        out.writeLong(created_at);
        out.writeByte((byte) (is_protected ? 1 : 0));
        out.writeByte((byte) (is_verified ? 1 : 0));
        out.writeString(name);
        out.writeString(screen_name);
        out.writeString(description_plain);
        out.writeString(description_html);
        out.writeString(description_expanded);
        out.writeString(description_unescaped);
        out.writeString(location);
        out.writeString(profile_image_url);
        out.writeString(profile_banner_url);
        out.writeString(url);
        out.writeByte((byte) (is_follow_request_sent ? 1 : 0));
        out.writeLong(followers_count);
        out.writeLong(friends_count);
        out.writeLong(statuses_count);
        out.writeLong(favorites_count);
        out.writeLong(listed_count);
        out.writeLong(media_count);
        out.writeString(url_expanded);
        out.writeByte((byte) (is_following ? 1 : 0));
        out.writeInt(background_color);
        out.writeInt(link_color);
        out.writeInt(text_color);
        out.writeByte((byte) (is_cache ? 1 : 0));
        out.writeByte((byte) (is_basic ? 1 : 0));
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (account_id ^ account_id >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
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

    public static final class CachedIndices {

        public final int id, name, screen_name, profile_image_url, created_at, is_protected,
                is_verified, favorites_count, listed_count, media_count, followers_count, friends_count,
                statuses_count, location, description_plain, description_html, description_expanded,
                url, url_expanded, profile_banner_url, is_following, background_color, link_color, text_color;

        public CachedIndices(Cursor cursor) {
            id = cursor.getColumnIndex(CachedUsers.USER_ID);
            name = cursor.getColumnIndex(CachedUsers.NAME);
            screen_name = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
            profile_image_url = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
            created_at = cursor.getColumnIndex(CachedUsers.CREATED_AT);
            is_protected = cursor.getColumnIndex(CachedUsers.IS_PROTECTED);
            is_verified = cursor.getColumnIndex(CachedUsers.IS_VERIFIED);
            favorites_count = cursor.getColumnIndex(CachedUsers.FAVORITES_COUNT);
            listed_count = cursor.getColumnIndex(CachedUsers.LISTED_COUNT);
            media_count = cursor.getColumnIndex(CachedUsers.LISTED_COUNT);
            followers_count = cursor.getColumnIndex(CachedUsers.FOLLOWERS_COUNT);
            friends_count = cursor.getColumnIndex(CachedUsers.FRIENDS_COUNT);
            statuses_count = cursor.getColumnIndex(CachedUsers.STATUSES_COUNT);
            location = cursor.getColumnIndex(CachedUsers.LOCATION);
            description_plain = cursor.getColumnIndex(CachedUsers.DESCRIPTION_PLAIN);
            description_html = cursor.getColumnIndex(CachedUsers.DESCRIPTION_HTML);
            description_expanded = cursor.getColumnIndex(CachedUsers.DESCRIPTION_EXPANDED);
            url = cursor.getColumnIndex(CachedUsers.URL);
            url_expanded = cursor.getColumnIndex(CachedUsers.URL_EXPANDED);
            profile_banner_url = cursor.getColumnIndex(CachedUsers.PROFILE_BANNER_URL);
            is_following = cursor.getColumnIndex(CachedUsers.IS_FOLLOWING);
            background_color = cursor.getColumnIndex(CachedUsers.BACKGROUND_COLOR);
            link_color = cursor.getColumnIndex(CachedUsers.LINK_COLOR);
            text_color = cursor.getColumnIndex(CachedUsers.TEXT_COLOR);
        }

    }

}
