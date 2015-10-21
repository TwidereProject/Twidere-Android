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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.twidere.api.twitter.model.impl.UserImpl;

import java.util.Date;

/**
 * A data interface representing Basic user information element
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@Implementation(UserImpl.class)
public interface User extends Comparable<User>, TwitterResponse {
	Date getCreatedAt();

	boolean isDefaultProfile();

	/**
	 * Returns the description of the user
	 * 
	 * @return the description of the user
	 */
	String getDescription();

	UrlEntity[] getDescriptionEntities();

	long getFavouritesCount();

	boolean isFollowedBy();

	/**
	 * Returns the number of followers
	 * 
	 * @return the number of followers
	 * @since Twitter4J 1.0.4
	 */
	long getFollowersCount();

	long getFriendsCount();

	boolean hasCustomTimelines();

	/**
	 * Returns the id of the user
	 * 
	 * @return the id of the user
	 */
	long getId();

	/**
	 * Returns the preferred language of the user
	 * 
	 * @return the preferred language of the user
	 * @since Twitter4J 2.1.2
	 */
	String getLang();

	/**
	 * Returns the number of public lists the user is listed on, or -1 if the
	 * count is unavailable.
	 * 
	 * @return the number of public lists the user is listed on.
	 * @since Twitter4J 2.1.4
	 */
	long getListedCount();

	/**
	 * Returns the location of the user
	 * 
	 * @return the location of the user
	 */
	String getLocation();

	/**
	 * Returns the name of the user
	 * 
	 * @return the name of the user
	 */
	String getName();

	boolean isNeedsPhoneVerification();

	boolean isNotifications();

	String getProfileBackgroundColor();

	String getProfileBackgroundImageUrl();

	String getProfileBackgroundImageUrlHttps();

	String getProfileBannerImageUrl();

	/**
	 * Returns the profile image url of the user
	 * 
	 * @return the profile image url of the user
	 */
    String getProfileImageUrl();

	/**
	 * Returns the profile image url of the user, served over SSL
	 * 
	 * @return the profile image url of the user, served over SSL
	 */
	String getProfileImageUrlHttps();

	String getProfileLinkColor();

	String getProfileLocation();

	String getProfileSidebarBorderColor();

	String getProfileSidebarFillColor();

	String getProfileTextColor();

	/**
	 * Returns the screen name of the user
	 * 
	 * @return the screen name of the user
	 */
	String getScreenName();

	/**
	 * Returns the current status of the user<br>
	 * This can be null if the instance if from Status.getUser().
	 * 
	 * @return current status of the user
	 * @since Twitter4J 2.1.1
	 */
	Status getStatus();

	long getStatusesCount();

	long getMediaCount();

	boolean isSuspended();

	String getTimeZone();

	/**
	 * Returns the url of the user
	 * 
	 * @return the url of the user
	 */
    String getUrl();

	UrlEntity[] getUrlEntities();

	int getUtcOffset();

	boolean canMediaTag();

	/**
	 * Tests if the user is enabling contributors
	 * 
	 * @return if the user is enabling contributors
	 * @since Twitter4J 2.1.2
	 */
	boolean isContributorsEnabled();

	boolean isDefaultProfileImage();

	boolean isFollowing();

	/**
	 * Returns true if the authenticating user has requested to follow this
	 * user, otherwise false.
	 * 
	 * @return true if the authenticating user has requested to follow this
	 *         user.
	 * @since Twitter4J 2.1.4
	 */
	boolean isFollowRequestSent();

	/**
	 * @return the user is enabling geo location
	 * @since Twitter4J 2.0.10
	 */
	boolean isGeoEnabled();

	boolean isProfileBackgroundTiled();

	boolean isProfileUseBackgroundImage();

	/**
	 * Test if the user status is protected
	 * 
	 * @return true if the user status is protected
	 */
	boolean isProtected();

	boolean isTranslationEnabled();

	/**
	 * @return returns true if the user is a translator
	 * @since Twitter4J 2.1.9
	 */
	boolean isTranslator();

	/**
	 * @return returns true if the user is a verified celebrity
	 * @since Twitter4J 2.0.10
	 */
	boolean isVerified();

}
