/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;


import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class User extends TwitterResponseObject implements Comparable<User>, Parcelable {

    // BEGIN Basic information

    @JsonField(name = "id")
    String id;

    /**
     * Fanfou uses this ID
     */
    @JsonField(name = "unique_id")
    String uniqueId;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "screen_name")
    String screenName;

    @JsonField(name = "location")
    String location;

    @JsonField(name = "description")
    String description;

    @JsonField(name = "url")
    String url;

    // END Basic information

    @JsonField(name = "entities")
    UserEntities entities;

    @JsonField(name = "protected")
    boolean isProtected;

    @JsonField(name = "followers_count")
    long followersCount = -1;

    @JsonField(name = "friends_count")
    long friendsCount = -1;

    @JsonField(name = "listed_count")
    long listedCount = -1;

    @JsonField(name = "groups_count")
    long groupsCount = -1;

    @JsonField(name = "favourites_count")
    long favouritesCount = -1;

    @JsonField(name = "utc_offset")
    int utcOffset;

    @JsonField(name = "time_zone")
    String timeZone;

    @JsonField(name = "geo_enabled")
    boolean geoEnabled;

    @JsonField(name = "verified")
    boolean isVerified;

    @JsonField(name = "statuses_count")
    long statusesCount = -1;

    /**
     * <code>photo_count</code> is for Fanfou compatibility
     */
    @JsonField(name = {"media_count", "photo_count"})
    long mediaCount = -1;

    @JsonField(name = "lang")
    String lang;

    @JsonField(name = "status")
    Status status;

    @JsonField(name = "contributors_enabled")
    boolean contributorsEnabled;

    @JsonField(name = "is_translator")
    boolean isTranslator;

    @JsonField(name = "is_translation_enabled")
    boolean isTranslationEnabled;

    /**
     * <code>backgroundcolor</code> is for GNU social compatibility
     */
    @JsonField(name = {"profile_background_color", "backgroundcolor"})
    String profileBackgroundColor;

    @JsonField(name = "profile_background_image_url")
    String profileBackgroundImageUrl;

    @JsonField(name = "profile_background_image_url_https")
    String profileBackgroundImageUrlHttps;

    @JsonField(name = "profile_background_tile")
    boolean profileBackgroundTile;

    @JsonField(name = "profile_image_url")
    String profileImageUrl;

    @JsonField(name = "profile_image_url_https")
    String profileImageUrlHttps;

    /**
     * Fanfou has field {@code "profile_image_url_large"}
     * GNU Social has field {@code "profile_image_url_profile_size"}
     */
    @JsonField(name = {"profile_image_url_large", "profile_image_url_profile_size"})
    String profileImageUrlLarge;

    @JsonField(name = {"profile_banner_url", "cover_photo"})
    String profileBannerUrl;
    /**
     * <code>backgroundcolor</code> is for GNU social compatibility
     */
    @JsonField(name = {"profile_link_color", "linkcolor"})
    String profileLinkColor;

    @JsonField(name = "profile_sidebar_border_color")
    String profileSidebarBorderColor;

    @JsonField(name = "profile_sidebar_fill_color")
    String profileSidebarFillColor;

    @JsonField(name = "profile_text_color")
    String profileTextColor;

    @JsonField(name = "profile_use_background_image")
    boolean profileUseBackgroundImage;

    @JsonField(name = "default_profile")
    boolean defaultProfile;

    @JsonField(name = "default_profile_image")
    boolean defaultProfileImage;

    @JsonField(name = "has_custom_timelines")
    boolean hasCustomTimelines;

    @JsonField(name = "suspended")
    boolean isSuspended;

    @JsonField(name = "needs_phone_verification")
    boolean needsPhoneVerification;

    @JsonField(name = "statusnet_profile_url")
    String statusnetProfileUrl;

    @JsonField(name = "ostatus_uri")
    String ostatusUri;

    @JsonField(name = "profile_image_url_original")
    String profileImageUrlOriginal;

    // BEGIN Twitter fields

    @JsonField(name = "pinned_tweet_ids")
    String[] pinnedTweetIds;

    // END Twitter fields

    // BEGIN Relationship fields

    /**
     * <code>follows_you</code> is for GNU social compatibility
     */
    @JsonField(name = {"followed_by", "follows_you"})
    @Nullable
    Boolean followedBy;

    @JsonField(name = "following")
    @Nullable
    Boolean following;

    /**
     * <code>blocks_you</code> is for GNU social compatibility
     */
    @JsonField(name = {"blocked_by", "blocks_you"})
    @Nullable
    Boolean blockedBy;

    /**
     * <code>statusnet_blocking</code> is for GNU social compatibility
     */
    @JsonField(name = {"blocking", "statusnet_blocking"})
    @Nullable
    Boolean blocking;

    @JsonField(name = "muting")
    @Nullable
    Boolean muting;

    @JsonField(name = "follow_request_sent")
    @Nullable
    Boolean followRequestSent;

    @JsonField(name = "notifications")
    @Nullable
    Boolean notificationsEnabled;

    @JsonField(name = "can_media_tag")
    @Nullable
    Boolean canMediaTag;

    // END Relationship fields

    public String getId() {
        return id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public UserEntities getEntities() {
        return entities;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public long getFriendsCount() {
        return friendsCount;
    }

    public long getListedCount() {
        return listedCount;
    }

    public long getGroupsCount() {
        return groupsCount;
    }

    public long getFavouritesCount() {
        return favouritesCount;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isGeoEnabled() {
        return geoEnabled;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public long getStatusesCount() {
        return statusesCount;
    }

    public long getMediaCount() {
        return mediaCount;
    }

    public String getLang() {
        return lang;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isContributorsEnabled() {
        return contributorsEnabled;
    }

    public boolean isTranslator() {
        return isTranslator;
    }

    public boolean isTranslationEnabled() {
        return isTranslationEnabled;
    }

    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public String getProfileBackgroundImageUrlHttps() {
        return profileBackgroundImageUrlHttps;
    }

    public boolean isProfileBackgroundTile() {
        return profileBackgroundTile;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    public String getProfileImageUrlLarge() {
        return profileImageUrlLarge;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }

    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }

    public String getProfileTextColor() {
        return profileTextColor;
    }

    public boolean isProfileUseBackgroundImage() {
        return profileUseBackgroundImage;
    }

    public boolean isDefaultProfile() {
        return defaultProfile;
    }

    public boolean isDefaultProfileImage() {
        return defaultProfileImage;
    }

    public boolean hasCustomTimelines() {
        return hasCustomTimelines;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public boolean isNeedsPhoneVerification() {
        return needsPhoneVerification;
    }

    public String getStatusnetProfileUrl() {
        return statusnetProfileUrl;
    }

    public String getOstatusUri() {
        return ostatusUri;
    }

    public String getProfileImageUrlOriginal() {
        return profileImageUrlOriginal;
    }

    public String[] getPinnedTweetIds() {
        return pinnedTweetIds;
    }

    @Nullable
    public Boolean isFollowedBy() {
        return followedBy;
    }

    @Nullable
    public Boolean isFollowing() {
        return following;
    }

    @Nullable
    public Boolean isBlockedBy() {
        return blockedBy;
    }

    @Nullable
    public Boolean isBlocking() {
        return blocking;
    }

    @Nullable
    public Boolean isMuting() {
        return muting;
    }

    @Nullable
    public Boolean isFollowRequestSent() {
        return followRequestSent;
    }

    @Nullable
    public Boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    @Nullable
    public Boolean canMediaTag() {
        return canMediaTag;
    }

    public UrlEntity[] getDescriptionEntities() {
        if (entities == null) return null;
        return entities.getDescriptionEntities();
    }


    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrlEntities();
    }


    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                ", entities=" + entities +
                ", isProtected=" + isProtected +
                ", followersCount=" + followersCount +
                ", friendsCount=" + friendsCount +
                ", listedCount=" + listedCount +
                ", groupsCount=" + groupsCount +
                ", favouritesCount=" + favouritesCount +
                ", utcOffset=" + utcOffset +
                ", timeZone='" + timeZone + '\'' +
                ", geoEnabled=" + geoEnabled +
                ", isVerified=" + isVerified +
                ", statusesCount=" + statusesCount +
                ", mediaCount=" + mediaCount +
                ", lang='" + lang + '\'' +
                ", status=" + status +
                ", contributorsEnabled=" + contributorsEnabled +
                ", isTranslator=" + isTranslator +
                ", isTranslationEnabled=" + isTranslationEnabled +
                ", profileBackgroundColor='" + profileBackgroundColor + '\'' +
                ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\'' +
                ", profileBackgroundImageUrlHttps='" + profileBackgroundImageUrlHttps + '\'' +
                ", profileBackgroundTile=" + profileBackgroundTile +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", profileImageUrlHttps='" + profileImageUrlHttps + '\'' +
                ", profileImageUrlLarge='" + profileImageUrlLarge + '\'' +
                ", profileBannerUrl='" + profileBannerUrl + '\'' +
                ", profileLinkColor='" + profileLinkColor + '\'' +
                ", profileSidebarBorderColor='" + profileSidebarBorderColor + '\'' +
                ", profileSidebarFillColor='" + profileSidebarFillColor + '\'' +
                ", profileTextColor='" + profileTextColor + '\'' +
                ", profileUseBackgroundImage=" + profileUseBackgroundImage +
                ", defaultProfile=" + defaultProfile +
                ", defaultProfileImage=" + defaultProfileImage +
                ", hasCustomTimelines=" + hasCustomTimelines +
                ", canMediaTag=" + canMediaTag +
                ", followedBy=" + followedBy +
                ", following=" + following +
                ", followRequestSent=" + followRequestSent +
                ", notifications=" + notificationsEnabled +
                ", isSuspended=" + isSuspended +
                ", needsPhoneVerification=" + needsPhoneVerification +
                ", statusnetProfileUrl='" + statusnetProfileUrl + '\'' +
                ", ostatusUri='" + ostatusUri + '\'' +
                ", profileImageUrlOriginal='" + profileImageUrlOriginal + '\'' +
                ", blockedBy=" + blockedBy +
                ", blocking=" + blocking +
                ", muting=" + muting +
                ", pinnedTweetIds=" + Arrays.toString(pinnedTweetIds) +
                "} " + super.toString();
    }

    @Override
    public int compareTo(@NonNull final User that) {
        return id.compareTo(that.id);
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id == null) {
            throw new IOException("Malformed User object (no id)");
        }
        if (screenName == null) {
            throw new IOException("Malformed User object (no screen_name)");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            User target = new User();
            UserParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
