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

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
public interface AccountSettings extends TwitterResponse {
	/**
	 * Returns the language used to render Twitter's UII for this user.
	 * 
	 * @return the language ISO 639-1 representation
	 */
	String getLanguage();

	/**
	 * Returns sleep end time.
	 * 
	 * @return sleep end time
	 */
	String getSleepEndTime();

	/**
	 * Returns sleep start time.
	 * 
	 * @return sleep start time
	 */
	String getSleepStartTime();

	/**
	 * Returns the timezone configured for this user.
	 * 
	 * @return the timezone (formated as a Rails TimeZone name)
	 */
	TimeZone getTimeZone();

	/**
	 * Return the user's trend locations
	 * 
	 * @return the user's trend locations
	 */
	Location[] getTrendLocations();

	/**
	 * Returns true if the wants to always access twitter using HTTPS.
	 * 
	 * @return true if the wants to always access twitter using HTTPS
	 */
	boolean isAlwaysUseHttps();

	/**
	 * Returns true if the user is discoverable by email.
	 * 
	 * @return true if the user is discoverable by email
	 */
	boolean isDiscoverableByEmail();

	/**
	 * Return true if the user is enabling geo location
	 * 
	 * @return true if the user is enabling geo location
	 */
	boolean isGeoEnabled();

	/**
	 * Returns true if the user enabled sleep time.
	 * 
	 * @return true if the user enabled sleep time
	 */
	boolean isSleepTimeEnabled();
}
