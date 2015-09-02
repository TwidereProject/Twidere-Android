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

package org.mariotaku.twidere.api.twitter.model.impl;


import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 15/3/31.
 */
@JsonObject
public class UserImpl extends TwitterResponseImpl implements User {

    @JsonField(name = "id")
    long id;

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
    UserEntitiesImpl entities;

    @JsonField(name = "protected")
    boolean isProtected;

    @JsonField(name = "followers_count")
    long followersCount;

    @JsonField(name = "friends_count")
    long friendsCount;

    @JsonField(name = "listed_count")
    long listedCount;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "favourites_count")
    long favouritesCount;

    @JsonField(name = "utc_offset")
    int utcOffset;

    @JsonField(name = "time_zone")
    String timeZone;

    @JsonField(name = "geo_enabled")
    boolean geoEnabled;

    @JsonField(name = "verified")
    boolean isVerified;

    @JsonField(name = "statuses_count")
    long statusesCount;

    @JsonField(name = "media_count")
    long mediaCount;

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

    @JsonField(name = "profile_banner_url")
    String profileBannerUrl;

    @JsonField(name = "profile_link_color")
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

    @JsonField(name = "can_media_tag")
    boolean canMediaTag;

    @JsonField(name = "followed_by")
    boolean followedBy;

    @JsonField(name = "following")
    boolean following;

    @JsonField(name = "follow_request_sent")
    boolean followRequestSent;

    @JsonField(name = "notifications")
    boolean notifications;

    @JsonField(name = "suspended")
    boolean isSuspended;

    @JsonField(name = "needs_phone_verification")
    boolean needsPhoneVerification;


    @Override
    public boolean canMediaTag() {
        return canMediaTag;
    }

    @Override
    public boolean isContributorsEnabled() {
        return contributorsEnabled;
    }

    @Override
    public boolean isDefaultProfile() {
        return defaultProfile;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UrlEntity[] getDescriptionEntities() {
        if (entities == null) return null;
        return entities.getDescriptionEntities();
    }

    @Override
    public long getFavouritesCount() {
        return favouritesCount;
    }

    @Override
    public boolean isFollowRequestSent() {
        return followRequestSent;
    }

    @Override
    public boolean isFollowedBy() {
        return followedBy;
    }

    @Override
    public long getFollowersCount() {
        return followersCount;
    }

    @Override
    public boolean isFollowing() {
        return following;
    }

    @Override
    public long getFriendsCount() {
        return friendsCount;
    }

    @Override
    public boolean isGeoEnabled() {
        return geoEnabled;
    }

    @Override
    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTile;
    }

    @Override
    public boolean hasCustomTimelines() {
        return hasCustomTimelines;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isTranslationEnabled() {
        return isTranslationEnabled;
    }

    @Override
    public boolean isTranslator() {
        return isTranslator;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public long getListedCount() {
        return listedCount;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public long getMediaCount() {
        return mediaCount;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isNeedsPhoneVerification() {
        return needsPhoneVerification;
    }

    @Override
    public boolean isNotifications() {
        return notifications;
    }

    @Override
    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    @Override
    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    @Override
    public String getProfileBackgroundImageUrlHttps() {
        return profileBackgroundImageUrlHttps;
    }

    @Override
    public String getProfileBannerImageUrl() {
        return profileBannerUrl;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @Override
    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    @Override
    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    @Override
    public String getProfileLocation() {
        return profileLocation;
    }

    @Override
    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }

    @Override
    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }

    @Override
    public boolean isProfileUseBackgroundImage() {
        return profileUseBackgroundImage;
    }

    @Override
    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public String getScreenName() {
        return screenName;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public long getStatusesCount() {
        return statusesCount;
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrlEntities();
    }

    @Override
    public int getUtcOffset() {
        return utcOffset;
    }

    @Override
    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public String getProfileTextColor() {
        return profileTextColor;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "id=" + id +
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
                ", createdAt=" + createdAt +
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
                ", notifications=" + notifications +
                ", isSuspended=" + isSuspended +
                ", needsPhoneVerification=" + needsPhoneVerification +
                '}';
    }

    @Override
    public boolean isDefaultProfileImage() {
        return defaultProfileImage;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(@NonNull final User that) {
        return (int) (id - that.getId());
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id <= 0 || screenName == null) throw new IOException("Malformed User object");
    }
}
