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

import org.mariotaku.restfu.http.ValueMap;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public final class GeoQuery implements ValueMap {

    private static final String ACCURACY = "accuracy";
    private static final String GRANULARITY = "granularity";
    private static final String MAX_RESULTS = "max_results";
    private final GeoLocation location;
    private final String ip;
    private String accuracy;
    private String granularity;
    private int maxResults;
    public static final String NEIGHBORHOOD = "neighborhood";
    public static final String CITY = "city";

    /**
     * Creates a GeoQuery with the specified location
     *
     * @param location Query location
     */
    public GeoQuery(final GeoLocation location) {
        this.location = location;
        ip = null;
    }

    /**
     * Creates a GeoQuery with the specified IP address
     *
     * @param ip IP address
     */
    public GeoQuery(final String ip) {
        this.ip = ip;
        location = null;
    }

    public GeoQuery accuracy(final String accuracy) {
        setAccuracy(accuracy);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GeoQuery geoQuery = (GeoQuery) o;

        if (maxResults != geoQuery.maxResults) return false;
        if (accuracy != null ? !accuracy.equals(geoQuery.accuracy) : geoQuery.accuracy != null)
            return false;
        if (granularity != null ? !granularity.equals(geoQuery.granularity) : geoQuery.granularity != null)
            return false;
        if (ip != null ? !ip.equals(geoQuery.ip) : geoQuery.ip != null) return false;
        if (location != null ? !location.equals(geoQuery.location) : geoQuery.location != null)
            return false;

        return true;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getGranularity() {
        return granularity;
    }

    public String getIp() {
        return ip;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public GeoQuery granularity(final String granularity) {
        setGranularity(granularity);
        return this;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        result = 31 * result + (granularity != null ? granularity.hashCode() : 0);
        result = 31 * result + maxResults;
        return result;
    }

    public GeoQuery maxResults(final int maxResults) {
        setMaxResults(maxResults);
        return this;
    }

    /**
     * Sets a hint on the "region" in which to search. If a number, then this is
     * a radius in meters, but it can also take a string that is suffixed with
     * ft to specify feet. If this is not passed in, then it is assumed to be
     * 0m. If coming from a device, in practice, this value is whatever accuracy
     * the device has measuring its location (whether it be coming from a GPS,
     * WiFi triangulation, etc.).
     *
     * @param accuracy a hint on the "region" in which to search.
     */
    public void setAccuracy(final String accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Sets the minimal granularity of data to return. If this is not passed in,
     * then neighborhood is assumed. city can also be passed.
     *
     * @param granularity the minimal granularity of data to return
     */
    public void setGranularity(final String granularity) {
        this.granularity = granularity;
    }

    /**
     * Sets a hint as to the number of results to return. This does not
     * guarantee that the number of results returned will equal max_results, but
     * instead informs how many "nearby" results to return. Ideally, only pass
     * in the number of places you intend to display to the user here.
     *
     * @param maxResults A hint as to the number of results to return.
     */
    public void setMaxResults(final int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public String toString() {
        return "GeoQuery{" + "location=" + location + ", ip='" + ip + '\'' + ", accuracy='" + accuracy + '\''
                + ", granularity='" + granularity + '\'' + ", maxResults=" + maxResults + '}';
    }

    @Override
    public boolean has(String key) {
        switch (key) {
            case "lat":
            case "long": {
                return location != null;
            }
            case "ip": {
                return ip != null;
            }
            case ACCURACY: {
                return accuracy != null;
            }
            case GRANULARITY: {
                return granularity != null;
            }
            case MAX_RESULTS: {
                return maxResults > 0;
            }
        }
        return false;
    }

    @Override
    public String get(String key) {
        switch (key) {
            case "lat": {
                if (location == null) return null;
                return String.valueOf(location.getLatitude());
            }
            case "long": {
                if (location == null) return null;
                return String.valueOf(location.getLongitude());
            }
            case "ip": {
                return ip;
            }
            case ACCURACY: {
                return accuracy;
            }
            case GRANULARITY: {
                return granularity;
            }
            case MAX_RESULTS: {
                if (maxResults <= 0) return null;
                return String.valueOf(maxResults);
            }
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{"lat", "long", "ip", ACCURACY, GRANULARITY, MAX_RESULTS};
    }
}
