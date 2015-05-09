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

import org.mariotaku.twidere.api.twitter.model.GeoLocation;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.ResponseList;

import java.io.Serializable;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public interface SimilarPlaces extends ResponseList<Place>, Serializable {
    /**
     * Returns the token needed to be able to create a new place with
     * {@link org.mariotaku.twidere.api.twitter.api.PlacesGeoResources#createPlace(String, String, String, GeoLocation, String)}
     * .
     *
     * @return token the token needed to be able to create a new place with
     * {@link org.mariotaku.twidere.api.twitter.api.PlacesGeoResources#createPlace(String, String, String, GeoLocation, String)}
     */
    String getToken();
}
