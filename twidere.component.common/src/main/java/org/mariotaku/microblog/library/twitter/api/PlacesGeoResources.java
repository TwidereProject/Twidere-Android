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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.GeoLocation;
import org.mariotaku.microblog.library.twitter.model.GeoQuery;
import org.mariotaku.microblog.library.twitter.model.Place;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SimilarPlaces;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
@SuppressWarnings("RedundantThrows")
public interface PlacesGeoResources {
	/**
	 * Creates a new place at the given latitude and longitude. <br>
	 * This method calls http://api.twitter.com/1.1/geo/place.json
	 * 
	 * @param name The name a place is known as.
	 * @param containedWithin The place_id within which the new place can be
	 *            found. Try and be as close as possible with the containing
	 *            place. For example, for a room in a building, set the
	 *            contained_within as the building place_id.
	 * @param token The token found in the response from geo/similar_places.
	 * @param location The latitude and longitude the place is located at.
	 * @param streetAddress optional: This parameter searches for places which
	 *            have this given street address. There are other well-known,
	 *            and application specific attributes available. Custom
	 *            attributes are also permitted. Learn more about Place
	 *            Attributes.
	 * @return the created place
	 * @throws MicroBlogException when Twitter service or network is unavailable
	 * @see <a href="https://dev.twitter.com/docs/api/1.1/post/geo/place">POST
	 *      geo/place | Twitter Developers</a>
	 * @since Twitter4J 2.1.7
	 */
	Place createPlace(String name, String containedWithin, String token, GeoLocation location, String streetAddress)
			throws MicroBlogException;

	/**
	 * Find out more details of a place that was returned from the
	 * {@link PlacesGeoResources#reverseGeoCode(GeoQuery)}
	 * method. <br>
	 * This method calls http://api.twitter.com/1.1/geo/id/:id.json
	 * 
	 * @param id The ID of the location to query about.
	 * @return details of the specified place
	 * @throws MicroBlogException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/geo/id/:place_id">GET
	 *      geo/id/:place_id | Twitter Developers</a>
	 * @since Twitter4J 2.1.1
	 */
	Place getGeoDetails(String id) throws MicroBlogException;

	/**
	 * Locates places near the given coordinates which are similar in name. <br>
	 * Conceptually you would use this method to get a list of known places to
	 * choose from first. Then, if the desired place doesn't exist, make a
	 * request to post/geo/place to create a new one. <br>
	 * The token contained in the response is the token needed to be able to
	 * create a new place. <br>
	 * This method calls http://api.twitter.com/1.1/geo/similar_places.json
	 * 
	 * @param location The latitude and longitude to search around.
	 * @param name The name a place is known as.
	 * @param containedWithin optional: the place_id which you would like to
	 *            restrict the search results to. Setting this value means only
	 *            places within the given place_id will be found.
	 * @param streetAddress optional: This parameter searches for places which
	 *            have this given street address. There are other well-known,
	 *            and application specific attributes available. Custom
	 *            attributes are also permitted. Learn more about Place
	 *            Attributes.
	 * @return places (cities and neighborhoods) that can be attached to a
	 *         statuses/update
	 * @throws MicroBlogException when Twitter service or network is unavailable
	 * @since Twitter4J 2.1.7
	 */
	SimilarPlaces getSimilarPlaces(GeoLocation location, String name, String containedWithin, String streetAddress)
			throws MicroBlogException;

	/**
	 * Search for places (cities and neighborhoods) that can be attached to a
	 * statuses/update. Given a latitude and a longitude, return a list of all
	 * the valid places that can be used as a place_id when updating a status.
	 * Conceptually, a query can be made from the user's location, retrieve a
	 * list of places, have the user validate the location he or she is at, and
	 * then send the ID of this location up with a call to statuses/update.<br>
	 * There are multiple granularities of places that can be returned --
	 * "neighborhoods", "cities", etc. At this time, only United States data is
	 * available through this method.<br>
	 * This API call is meant to be an informative call and will deliver
	 * generalized results about geography. <br>
	 * This method calls http://api.twitter.com/1.1/geo/reverse_geocode.json
	 * 
	 * @param query search query
	 * @return places (cities and neighborhoods) that can be attached to a
	 *         statuses/update
	 * @throws MicroBlogException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/geo/reverse_geocode">GET
	 *      geo/reverse_geocode | Twitter Developers</a>
	 * @since Twitter4J 2.1.1
	 */
	ResponseList<Place> reverseGeoCode(GeoQuery query) throws MicroBlogException;

	/**
	 * Search for places that can be attached to a statuses/update. Given a
	 * latitude and a longitude pair, an IP address, or a name, this request
	 * will return a list of all the valid places that can be used as the
	 * place_id when updating a status. <br>
	 * Conceptually, a query can be made from the user's location, retrieve a
	 * list of places, have the user validate the location he or she is at, and
	 * then send the ID of this location with a call to statuses/update. <br>
	 * This is the recommended method to use find places that can be attached to
	 * statuses/update. Unlike geo/reverse_geocode which provides raw data
	 * access, this endpoint can potentially re-order places with regards to the
	 * user who is authenticated. This approach is also preferred for
	 * interactive place matching with the user. <br>
	 * This method calls http://api.twitter.com/1.1/geo/search.json
	 * 
	 * @param query search query
	 * @return places (cities and neighborhoods) that can be attached to a
	 *         statuses/update
	 * @throws MicroBlogException when Twitter service or network is unavailable
	 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/geo/search">GET
	 *      geo/search | Twitter Developers</a>
	 * @since Twitter4J 2.1.7
	 */
	ResponseList<Place> searchPlaces(GeoQuery query) throws MicroBlogException;
}
