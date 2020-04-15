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

import androidx.annotation.NonNull;

import org.mariotaku.restfu.http.ValueMap;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public final class GeoQuery implements ValueMap {

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
        return location != null ? location.equals(geoQuery.location) : geoQuery.location == null;
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
    public boolean has(@NonNull String key) {
        switch (key) {
            case "lat":
            case "long": {
                return location != null;
            }
            case "ip": {
                return ip != null;
            }
            case "accuracy": {
                return accuracy != null;
            }
            case "granularity": {
                return granularity != null;
            }
            case "max_results": {
                return maxResults > 0;
            }
        }
        return false;
    }

    @Override
    public String get(@NonNull String key) {
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
            case "accuracy": {
                return accuracy;
            }
            case "granularity": {
                return granularity;
            }
            case "max_results": {
                if (maxResults <= 0) return null;
                return String.valueOf(maxResults);
            }
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{"lat", "long", "ip", "accuracy", "granularity", "max_results"};
    }
}
