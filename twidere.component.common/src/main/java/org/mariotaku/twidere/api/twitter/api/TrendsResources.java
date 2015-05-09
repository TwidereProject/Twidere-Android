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

package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.GeoLocation;
import org.mariotaku.twidere.api.twitter.model.Location;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Trends;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
@SuppressWarnings("RedundantThrows")
public interface TrendsResources {
	/**
	 * Returns the locations that Twitter has trending topic information for.
	 * The response is an array of &quot;locations&quot; that encode the
	 * location's WOEID (a <a
	 * href="http://developer.yahoo.com/geo/geoplanet/">Yahoo! Where On Earth
	 * ID</a>) and some other human-readable information such as a canonical
	 * name and country the location belongs in. <br>
	 * This method calls http://api.twitter.com/1.1/trends/available.json
	 * 
	 * @return the locations
	 * @throws TwitterException when Twitter service or network is
	 *             unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/trends/available">GET
	 *      trends/available | Twitter Developers</a>
	 * @since Twitter4J 2.1.1
	 */
	ResponseList<Location> getAvailableTrends() throws TwitterException;

	/**
	 * Returns the sorted locations that Twitter has trending topic information
	 * for. The response is an array of &quot;locations&quot; that encode the
	 * location's WOEID (a <a
	 * href="http://developer.yahoo.com/geo/geoplanet/">Yahoo! Where On Earth
	 * ID</a>) and some other human-readable information such as a canonical
	 * name and country the location belongs in. <br>
	 * This method calls http://api.twitter.com/1.1/trends/available.json
	 * 
	 * @param location the available trend locations will be sorted by distance
	 *            to the lat and long passed in. The sort is nearest to
	 *            furthest.
	 * @return the locations
	 * @throws TwitterException when Twitter service or network is
	 *             unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/trends/available">GET
	 *      trends/available | Twitter Developers</a>
	 * @since Twitter4J 2.1.1
	 */
	ResponseList<Location> getAvailableTrends(GeoLocation location) throws TwitterException;

	/**
	 * Returns the locations that Twitter has trending topic information for,
	 * closest to a specified location.<br>
	 * The response is an array of "locations" that encode the location's WOEID
	 * and some other human-readable information such as a canonical name and
	 * country the location belongs in.<br>
	 * A WOEID is a <a href="http://developer.yahoo.com/geo/geoplanet/">Yahoo!
	 * Where On Earth ID</a>. <br>
	 * This method calls http://api.twitter.com/1.1/trends/closest.json
	 * 
	 * @param location the available trend locations will be sorted by distance
	 *            to the lat and long passed in. The sort is nearest to
	 *            furthest.
	 * @return the locations
	 * @throws TwitterException when Twitter service or network is
	 *             unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/trends/closest">GET
	 *      trends/closest | Twitter Developers</a>
	 * @since Twitter4J 3.0.2
	 */
	ResponseList<Location> getClosestTrends(GeoLocation location) throws TwitterException;

	/**
	 * Returns the top 10 trending topics for a specific location Twitter has
	 * trending topic information for. The response is an array of "trend"
	 * objects that encode the name of the trending topic, the query parameter
	 * that can be used to search for the topic on Search, and the direct URL
	 * that can be issued against Search. This information is cached for five
	 * minutes, and therefore users are discouraged from querying these
	 * endpoints faster than once every five minutes. Global trends information
	 * is also available from this API by using a WOEID of 1. <br>
	 * This method calls http://api.twitter.com/1.1/trends/:woeid.json
	 * 
	 * @param woeid The WOEID of the location to be querying for
	 * @return trends
	 * @throws TwitterException when Twitter service or network is
	 *             unavailable
	 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/trends/:woeid">GET
	 *      trends/:woeid | Twitter Developers</a>
	 * @since Twitter4J 2.1.1
	 */
	Trends getLocationTrends(int woeid) throws TwitterException;

	/**
	 * Returns the top 10 trending topics for a specific WOEID, if trending
	 * information is available for it.<br>
	 * The response is an array of "trend" objects that encode the name of the
	 * trending topic, the query parameter that can be used to search for the
	 * topic on <a href="http://search.twitter.com/">Twitter Search</a>, and the
	 * Twitter Search URL.<br>
	 * This information is cached for 5 minutes. Requesting more frequently than
	 * that will not return any more data, and will count against your rate
	 * limit usage.<br>
	 * <br>
	 * This method calls http://api.twitter.com/1.1/trends/place.json
	 * 
	 * @param woeid <a href="http://developer.yahoo.com/geo/geoplanet/">The
	 *            Yahoo! Where On Earth ID</a> of the location to return
	 *            trending information for. Global information is available by
	 *            using 1 as the WOEID.
	 * @return trends
	 * @throws TwitterException when Twitter service or network is
	 *             unavailable
	 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/trends/place">GET
	 *      trends/place | Twitter Developers</a>
	 * @since Twitter4J 3.0.2
	 */
	Trends getPlaceTrends(int woeid) throws TwitterException;
}
