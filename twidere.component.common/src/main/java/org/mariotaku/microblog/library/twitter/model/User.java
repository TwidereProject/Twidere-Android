/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.microblog.library.twitter.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class User extends TwitterResponseObject implements Comparable<User>, Parcelable {

    @JsonField(name = "id")
    String id;

    /**
     * Fanfou uses this ID
     */
    @JsonField(name = "unique_id")
    String uniqueId;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "screen_name")
    String screenName;

    @JsonField(name = "location")
    String location;

    @JsonField(name = "profile_location")
    String profileLocation;

    @JsonField(name = "description")
    String description;

    @JsonField(name = "url")
    String url;

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

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

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

    @JsonField(name = "media_count")
    long mediaCount = -1;

    @JsonField(name = "photo_count")
    long photoCount = -1;

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

    @JsonField(name = "profile_background_color")
    String profileBackgroundColor;
    /**
     * For GNU social compatibility
     */
    @JsonField(name = "backgroundcolor")
    String backgroundcolor;

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
     * Fanfou has this field
     */
    @JsonField(name = "profile_image_url_large")
    String profileImageUrlLarge;

    @JsonField(name = "profile_banner_url")
    String profileBannerUrl;

    /**
     * For GNU social compatibility
     */
    @JsonField(name = "cover_photo")
    String coverPhoto;

    @JsonField(name = "profile_link_color")
    String profileLinkColor;

    /**
     * For GNU social compatibility
     */
    @JsonField(name = "linkcolor")
    String linkcolor;

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

    @JsonField(name = "can_media_tag")
    boolean canMediaTag;

    @JsonField(name = "followed_by")
    boolean followedBy;

    @JsonField(name = "following")
    boolean following = true;

    @JsonField(name = "follow_request_sent")
    boolean followRequestSent;

    @JsonField(name = "notifications")
    boolean notifications;

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

    @JsonField(name = "profile_image_url_profile_size")
    String profileImageUrlProfileSize;

    @JsonField(name = "follows_you")
    boolean followsYou;

    @JsonField(name = "blocks_you")
    boolean blocksYou;

    @JsonField(name = "blocked_by")
    boolean blockedBy;

    @JsonField(name = "statusnet_blocking")
    boolean statusnetBlocking;

    @JsonField(name = "blocking")
    boolean blocking;
    @JsonField(name = "muting")
    boolean muting;

    @JsonField(name = "pinned_tweet_ids")
    String[] pinnedTweetIds;

    public boolean canMediaTag() {
        return canMediaTag;
    }


    public boolean isContributorsEnabled() {
        return contributorsEnabled;
    }


    public boolean isDefaultProfile() {
        return defaultProfile;
    }


    public String getDescription() {
        return description;
    }


    public UrlEntity[] getDescriptionEntities() {
        if (entities == null) return null;
        return entities.getDescriptionEntities();
    }


    public long getFavouritesCount() {
        return favouritesCount;
    }


    public boolean isFollowRequestSent() {
        return followRequestSent;
    }


    public boolean isFollowedBy() {
        return followedBy || followsYou;
    }


    public long getFollowersCount() {
        return followersCount;
    }

    public boolean isFollowing() {
        return following;
    }

    public long getFriendsCount() {
        return friendsCount;
    }


    public boolean isGeoEnabled() {
        return geoEnabled;
    }


    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTile;
    }


    public boolean hasCustomTimelines() {
        return hasCustomTimelines;
    }


    public String getId() {
        return id;
    }


    public boolean isTranslationEnabled() {
        return isTranslationEnabled;
    }


    public boolean isTranslator() {
        return isTranslator;
    }


    public String getLang() {
        return lang;
    }


    public long getListedCount() {
        return listedCount;
    }

    public long getGroupsCount() {
        return groupsCount;
    }

    public String getLocation() {
        return location;
    }


    public long getMediaCount() {
        if (mediaCount != -1) return mediaCount;
        return photoCount;
    }


    public String getName() {
        return name;
    }


    public boolean isNeedsPhoneVerification() {
        return needsPhoneVerification;
    }


    public boolean isNotifications() {
        return notifications;
    }


    public String getProfileBackgroundColor() {
        if (profileBackgroundColor != null) return profileBackgroundColor;
        return backgroundcolor;
    }


    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }


    public String getProfileBackgroundImageUrlHttps() {
        return profileBackgroundImageUrlHttps;
    }


    public String getProfileBannerImageUrl() {
        if (profileBannerUrl != null) return profileBannerUrl;
        return coverPhoto;
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

    public String getProfileLinkColor() {
        if (profileLinkColor != null) return profileLinkColor;
        return linkcolor;
    }


    public String getProfileLocation() {
        return profileLocation;
    }


    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }


    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }


    public boolean isProfileUseBackgroundImage() {
        return profileUseBackgroundImage;
    }


    public boolean isProtected() {
        return isProtected;
    }


    public String getScreenName() {
        return screenName;
    }


    public Status getStatus() {
        return status;
    }


    public long getStatusesCount() {
        return statusesCount;
    }


    public boolean isSuspended() {
        return isSuspended;
    }


    public String getTimeZone() {
        return timeZone;
    }


    public String getUrl() {
        return url;
    }


    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrlEntities();
    }


    public int getUtcOffset() {
        return utcOffset;
    }


    public boolean isVerified() {
        return isVerified;
    }


    public String getProfileTextColor() {
        return profileTextColor;
    }


    public boolean isDefaultProfileImage() {
        return defaultProfileImage;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public String getOstatusUri() {
        return ostatusUri;
    }

    public String getStatusnetProfileUrl() {
        return statusnetProfileUrl;
    }

    public boolean isCanMediaTag() {
        return canMediaTag;
    }

    public boolean isHasCustomTimelines() {
        return hasCustomTimelines;
    }

    public String getProfileImageUrlProfileSize() {
        return profileImageUrlProfileSize;
    }

    public String getProfileImageUrlOriginal() {
        return profileImageUrlOriginal;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isBlocking() {
        return blocking || statusnetBlocking;
    }

    public boolean isBlockedBy() {
        return blockedBy || blocksYou;
    }

    public boolean isMuting() {
        return muting;
    }

    public String[] getPinnedTweetIds() {
        return pinnedTweetIds;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", location='" + location + '\'' +
                ", profileLocation='" + profileLocation + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", entities=" + entities +
                ", isProtected=" + isProtected +
                ", followersCount=" + followersCount +
                ", friendsCount=" + friendsCount +
                ", listedCount=" + listedCount +
                ", groupsCount=" + groupsCount +
                ", createdAt=" + createdAt +
                ", favouritesCount=" + favouritesCount +
                ", utcOffset=" + utcOffset +
                ", timeZone='" + timeZone + '\'' +
                ", geoEnabled=" + geoEnabled +
                ", isVerified=" + isVerified +
                ", statusesCount=" + statusesCount +
                ", mediaCount=" + mediaCount +
                ", photoCount=" + photoCount +
                ", lang='" + lang + '\'' +
                ", status=" + status +
                ", contributorsEnabled=" + contributorsEnabled +
                ", isTranslator=" + isTranslator +
                ", isTranslationEnabled=" + isTranslationEnabled +
                ", profileBackgroundColor='" + profileBackgroundColor + '\'' +
                ", backgroundcolor='" + backgroundcolor + '\'' +
                ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\'' +
                ", profileBackgroundImageUrlHttps='" + profileBackgroundImageUrlHttps + '\'' +
                ", profileBackgroundTile=" + profileBackgroundTile +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", profileImageUrlHttps='" + profileImageUrlHttps + '\'' +
                ", profileImageUrlLarge='" + profileImageUrlLarge + '\'' +
                ", profileBannerUrl='" + profileBannerUrl + '\'' +
                ", coverPhoto='" + coverPhoto + '\'' +
                ", profileLinkColor='" + profileLinkColor + '\'' +
                ", linkcolor='" + linkcolor + '\'' +
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
                ", notifications=" + notifications +
                ", isSuspended=" + isSuspended +
                ", needsPhoneVerification=" + needsPhoneVerification +
                ", statusnetProfileUrl='" + statusnetProfileUrl + '\'' +
                ", ostatusUri='" + ostatusUri + '\'' +
                ", profileImageUrlOriginal='" + profileImageUrlOriginal + '\'' +
                ", profileImageUrlProfileSize='" + profileImageUrlProfileSize + '\'' +
                ", followsYou=" + followsYou +
                ", blocksYou=" + blocksYou +
                ", statusnetBlocking=" + statusnetBlocking +
                "} " + super.toString();
    }

    @Override
    public int compareTo(@NonNull final User that) {
        return id.compareTo(that.getId());
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id == null || screenName == null) throw new IOException("Malformed User object");
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
