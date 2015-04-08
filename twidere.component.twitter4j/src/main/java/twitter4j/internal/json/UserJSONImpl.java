/*
 * Twidere - Twitter client for Android
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

package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import twitter4j.PageableResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getBoolean;
import static twitter4j.internal.util.InternalParseUtil.getDate;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

/**
 * A data class representing Basic user information element
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class UserJSONImpl extends TwitterResponseImpl implements User {
    private static final long serialVersionUID = 1401119968489672262L;
    private long id;
    private String name;

    private String screenName;
    private String location;
    private String description;
    private boolean isContributorsEnabled;
    private String profileImageUrl;
    private String profileImageUrlHttps;
    private String url;
    private boolean isProtected;
    private int followersCount;
    private Status status;
    private String profileBackgroundColor;
    private String profileTextColor;

    private String profileLinkColor;

    private String profileSidebarFillColor;
    private String profileSidebarBorderColor;
    private boolean profileUseBackgroundImage;
    private boolean showAllInlineMedia;
    private int friendsCount;
    private Date createdAt;
    private int favouritesCount;
    private int utcOffset;
    private String timeZone;
    private String profileBackgroundImageUrl;
    private String profileBackgroundImageUrlHttps;
    private boolean profileBackgroundTiled;
    private String lang;
    private int statusesCount;
    private boolean isGeoEnabled;
    private boolean isVerified;
    private boolean isTranslator;
    private boolean isFollowing;
    private int listedCount;
    private boolean isFollowRequestSent;
    private String profileBannerImageUrl;
    private boolean isDefaultProfileImage;
    private URLEntity[] urlEntities;
    private URLEntity[] descriptionEntities;

    /* package */UserJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
        super(res);
        final JSONObject json = res.asJSONObject();
        init(json);
    }

    /* package */UserJSONImpl(final JSONObject json) throws TwitterException {
        super();
        init(json);
    }

    @Override
    public int compareTo(final User that) {
        return (int) (id - that.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) return false;
        if (this == obj) return true;
        return obj instanceof User && ((User) obj).getId() == id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public URLEntity[] getDescriptionEntities() {
        return descriptionEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFavouritesCount() {
        return favouritesCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFollowersCount() {
        return followersCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFriendsCount() {
        return friendsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLang() {
        return lang;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getListedCount() {
        return listedCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocation() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileBackgroundImageUrlHttps() {
        return profileBackgroundImageUrlHttps;
    }

    @Override
    public String getProfileBannerImageUrl() {
        return profileBannerImageUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileImageURL() {
        return profileImageUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }

    @Override
    public String getProfileTextColor() {
        return profileTextColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScreenName() {
        return screenName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusesCount() {
        return statusesCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURL() {
        return url;
    }

    @Override
    public URLEntity[] getURLEntities() {
        return urlEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUtcOffset() {
        return utcOffset;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContributorsEnabled() {
        return isContributorsEnabled;
    }

    @Override
    public boolean isDefaultProfileImage() {
        return isDefaultProfileImage;
    }

    @Override
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFollowRequestSent() {
        return isFollowRequestSent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGeoEnabled() {
        return isGeoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTiled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProfileUseBackgroundImage() {
        return profileUseBackgroundImage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProtected() {
        return isProtected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShowAllInlineMedia() {
        return showAllInlineMedia;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTranslator() {
        return isTranslator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public String toString() {
        return "UserJSONImpl{" + "id=" + id + ", name='" + name + '\'' + ", screenName='" + screenName + '\''
                + ", location='" + location + '\'' + ", description='" + description + '\''
                + ", isContributorsEnabled=" + isContributorsEnabled + ", profileImageUrl='" + profileImageUrl + '\''
                + ", profileImageUrlHttps='" + profileImageUrlHttps + '\'' + ", url='" + url + '\'' + ", isProtected="
                + isProtected + ", followersCount=" + followersCount + ", status=" + status
                + ", profileBackgroundColor='" + profileBackgroundColor + '\'' + ", profileTextColor='"
                + profileTextColor + '\'' + ", profileLinkColor='" + profileLinkColor + '\''
                + ", profileSidebarFillColor='" + profileSidebarFillColor + '\'' + ", profileSidebarBorderColor='"
                + profileSidebarBorderColor + '\'' + ", profileUseBackgroundImage=" + profileUseBackgroundImage
                + ", showAllInlineMedia=" + showAllInlineMedia + ", friendsCount=" + friendsCount + ", createdAt="
                + createdAt + ", favouritesCount=" + favouritesCount + ", utcOffset=" + utcOffset + ", timeZone='"
                + timeZone + '\'' + ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\''
                + ", profileBackgroundImageUrlHttps='" + profileBackgroundImageUrlHttps + '\''
                + ", profileBackgroundTiled=" + profileBackgroundTiled + ", lang='" + lang + '\'' + ", statusesCount="
                + statusesCount + ", isGeoEnabled=" + isGeoEnabled + ", isVerified=" + isVerified + ", isTranslator="
                + isTranslator + ", listedCount=" + listedCount + ", isFollowRequestSent=" + isFollowRequestSent + '}';
    }

    private void init(final JSONObject json) throws TwitterException {
        try {
            id = getLong("id", json);
            name = getRawString("name", json);
            screenName = getRawString("screen_name", json);
            location = getRawString("location", json);
            description = getRawString("description", json);
            isContributorsEnabled = getBoolean("contributors_enabled", json);
            profileImageUrl = getRawString("profile_image_url", json);
            profileImageUrlHttps = getRawString("profile_image_url_https", json);
            url = getRawString("url", json);
            isProtected = getBoolean("protected", json);
            isGeoEnabled = getBoolean("geo_enabled", json);
            isVerified = getBoolean("verified", json);
            isTranslator = getBoolean("is_translator", json);
            isFollowing = getBoolean("following", json);
            followersCount = getInt("followers_count", json);
            profileBannerImageUrl = getRawString("profile_banner_url", json);
            profileBackgroundColor = getRawString("profile_background_color", json);
            profileTextColor = getRawString("profile_text_color", json);
            profileLinkColor = getRawString("profile_link_color", json);
            profileSidebarFillColor = getRawString("profile_sidebar_fill_color", json);
            profileSidebarBorderColor = getRawString("profile_sidebar_border_color", json);
            profileUseBackgroundImage = getBoolean("profile_use_background_image", json);
            showAllInlineMedia = getBoolean("show_all_inline_media", json);
            friendsCount = getInt("friends_count", json);
            createdAt = getDate("created_at", json, "EEE MMM dd HH:mm:ss z yyyy");
            favouritesCount = getInt("favourites_count", json);
            utcOffset = getInt("utc_offset", json);
            timeZone = getRawString("time_zone", json);
            profileBackgroundImageUrl = getRawString("profile_background_image_url", json);
            profileBackgroundImageUrlHttps = getRawString("profile_background_image_url_https", json);
            profileBackgroundTiled = getBoolean("profile_background_tile", json);
            lang = getRawString("lang", json);
            statusesCount = getInt("statuses_count", json);
            listedCount = getInt("listed_count", json);
            isFollowRequestSent = getBoolean("follow_request_sent", json);
            isDefaultProfileImage = getBoolean("default_profile_image", json);
            if (!json.isNull("status")) {
                final JSONObject statusJSON = json.getJSONObject("status");
                status = new StatusJSONImpl(statusJSON);
            }
            if (!json.isNull("entities")) {
                try {
                    final JSONObject entities = json.getJSONObject("entities");
                    int len;
                    if (!entities.isNull("description")) {
                        final JSONObject description = entities.getJSONObject("description");
                        if (!description.isNull("urls")) {
                            final JSONArray urlsArray = description.getJSONArray("urls");
                            len = urlsArray.length();
                            descriptionEntities = new URLEntity[len];
                            for (int i = 0; i < len; i++) {
                                descriptionEntities[i] = new URLEntityJSONImpl(urlsArray.getJSONObject(i));
                            }
                        }
                    }
                    if (!entities.isNull("url")) {
                        final JSONObject url = entities.getJSONObject("url");
                        if (!url.isNull("urls")) {
                            final JSONArray urlsArray = url.getJSONArray("urls");
                            len = urlsArray.length();
                            urlEntities = new URLEntity[len];
                            for (int i = 0; i < len; i++) {
                                urlEntities[i] = new URLEntityJSONImpl(urlsArray.getJSONObject(i));
                            }
                        }
                    }
                } catch (final JSONException jsone) {
                    throw new TwitterException(jsone);
                }
            }
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
        }
    }

    /* package */
    static PageableResponseList<User> createPagableUserList(final HttpResponse res, final Configuration conf)
            throws TwitterException {
        try {
            final JSONObject json = res.asJSONObject();
            final JSONArray list = json.getJSONArray("users");
            final int size = list.length();
            @SuppressWarnings("unchecked")
            final PageableResponseList<User> users = new PageableResponseListImpl<User>(size, json, res);
            for (int i = 0; i < size; i++) {
                final JSONObject userJson = list.getJSONObject(i);
                final User user = new UserJSONImpl(userJson);
                users.add(user);
            }
            return users;
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone);
        } catch (final TwitterException te) {
            throw te;
        }
    }

    /* package */
    static ResponseList<User> createUserList(final HttpResponse res, final Configuration conf) throws TwitterException {
        return createUserList(res.asJSONArray(), res, conf);
    }

    /* package */
    static ResponseList<User> createUserList(final JSONArray list, final HttpResponse res, final Configuration conf)
            throws TwitterException {
        try {
            final int size = list.length();
            final ResponseList<User> users = new ResponseListImpl<User>(size, res);
            for (int i = 0; i < size; i++) {
                final JSONObject json = list.getJSONObject(i);
                final User user = new UserJSONImpl(json);
                users.add(user);
            }
            return users;
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone);
        } catch (final TwitterException te) {
            throw te;
        }
    }
}
