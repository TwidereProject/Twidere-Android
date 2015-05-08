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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.simplerestapi.http.ValueMap;

/**
 * A data class represents search query.<br>
 * An instance of this class is NOT thread safe.<br>
 * Instances can be shared across threads, but should not be mutated while a
 * search is ongoing.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/search">GET search |
 * Twitter Developers</a>
 * @see <a href="http://search.twitter.com/operators">Twitter API / Search
 * Operators</a>
 */
public final class SearchQuery implements ValueMap {
    private String query = null;
    private String lang = null;
    private String locale = null;
    private long maxId = -1l;
    private int count = -1;
    private int page = -1;
    private String since = null;
    private long sinceId = -1;
    private String geocode = null;
    private String until = null;
    private String resultType = null;

    public static final String MILES = "mi";

    public static final String KILOMETERS = "km";

    /**
     * mixed: Include both popular and real time results in the response.
     * recent: return only the most recent results in the response popular:
     * return only the most popular results in the response.
     */
    public final static String MIXED = "mixed";

    public final static String POPULAR = "popular";

    public final static String RECENT = "recent";

    public SearchQuery() {
    }

    public SearchQuery(final String query) {
        this.query = query;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SearchQuery query1 = (SearchQuery) o;

        if (maxId != query1.maxId) return false;
        if (page != query1.page) return false;
        if (count != query1.count) return false;
        if (sinceId != query1.sinceId) return false;
        if (geocode != null ? !geocode.equals(query1.geocode) : query1.geocode != null)
            return false;
        if (lang != null ? !lang.equals(query1.lang) : query1.lang != null) return false;
        if (locale != null ? !locale.equals(query1.locale) : query1.locale != null) return false;
        if (query != null ? !query.equals(query1.query) : query1.query != null) return false;
        if (since != null ? !since.equals(query1.since) : query1.since != null) return false;
        if (until != null ? !until.equals(query1.until) : query1.until != null) return false;
        if (resultType != null ? !resultType.equals(query1.resultType) : query1.resultType != null)
            return false;

        return true;
    }

    /**
     * returns tweets by users located within a given radius of the given
     * latitude/longitude, where the user's location is taken from their Twitter
     * profile
     *
     * @param location geo location
     * @param radius   radius
     * @param unit     Query.MILES or Query.KILOMETERS
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery geoCode(final GeoLocation location, final double radius, final String unit) {
        setGeoCode(location, radius, unit);
        return this;
    }

    /**
     * Returns the specified geocode
     *
     * @return geocode
     */
    public String getGeocode() {
        return geocode;
    }

    /**
     * Returns the lang
     *
     * @return lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Returns the language of the query you are sending (only ja is currently
     * effective). This is intended for language-specific clients and the
     * default should work in the majority of cases.
     *
     * @return locale
     * @since Twitter4J 2.1.1
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns tweets with status ids less than the given id.
     *
     * @return maxId
     * @since Twitter4J 2.1.1
     */
    public long getMaxId() {
        return maxId;
    }

    /**
     * Returns the page number (starting at 1) to return, up to a max of roughly
     * 1500 results
     *
     * @return the page number (starting at 1) to return
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the specified query
     *
     * @return query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns resultType
     *
     * @return the resultType
     * @since Twitter4J 2.1.3
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * Returns the number of tweets to return per page, up to a max of 100
     *
     * @return count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns tweets with since the given date. Date should be formatted as
     * YYYY-MM-DD
     *
     * @return since
     * @since Twitter4J 2.1.1
     */
    public String getSince() {
        return since;
    }

    /**
     * returns sinceId
     *
     * @return sinceId
     */
    public long getSinceId() {
        return sinceId;
    }

    /**
     * Returns until
     *
     * @return until
     * @since Twitter4J 2.1.1
     */
    public String getUntil() {
        return until;
    }

    @Override
    public int hashCode() {
        int result = query != null ? query.hashCode() : 0;
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (int) (maxId ^ maxId >>> 32);
        result = 31 * result + count;
        result = 31 * result + page;
        result = 31 * result + (since != null ? since.hashCode() : 0);
        result = 31 * result + (int) (sinceId ^ sinceId >>> 32);
        result = 31 * result + (geocode != null ? geocode.hashCode() : 0);
        result = 31 * result + (until != null ? until.hashCode() : 0);
        result = 31 * result + (resultType != null ? resultType.hashCode() : 0);
        return result;
    }

    /**
     * restricts tweets to the given language, given by an <a
     * href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1 code</a>
     *
     * @param lang an <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
     *             code</a>
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery lang(final String lang) {
        setLang(lang);
        return this;
    }

    /**
     * Specify the language of the query you are sending (only ja is currently
     * effective). This is intended for language-specific clients and the
     * default should work in the majority of cases.
     *
     * @param locale the locale
     * @return the instance
     * @since Twitter4J 2.1.1
     */
    public SearchQuery locale(final String locale) {
        setLocale(locale);
        return this;
    }

    /**
     * If specified, returns tweets with status ids less than the given id.
     *
     * @param maxId maxId
     * @return this instance
     * @since Twitter4J 2.1.1
     */
    public SearchQuery maxId(final long maxId) {
        setMaxId(maxId);
        return this;
    }

    /**
     * sets the page number (starting at 1) to return, up to a max of roughly
     * 1500 results
     *
     * @param page the page number (starting at 1) to return
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery page(final int page) {
        setPage(page);
        return this;
    }

    /**
     * Sets the query string
     *
     * @param query the query string
     * @return the instance
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/search">GET search
     * | Twitter Developers</a>
     * @see <a href="http://search.twitter.com/operators">Twitter API / Search
     * Operators</a>
     * @since Twitter4J 2.1.0
     */
    public SearchQuery query(final String query) {
        setQuery(query);
        return this;
    }

    /**
     * If specified, returns tweets included popular or real time or both in the
     * responce
     *
     * @param resultType resultType
     * @return the instance
     * @since Twitter4J 2.1.3
     */
    public SearchQuery resultType(final String resultType) {
        setResultType(resultType);
        return this;
    }

    /**
     * sets the number of tweets to return per page, up to a max of 100
     *
     * @param rpp the number of tweets to return per page
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery rpp(final int rpp) {
        setCount(rpp);
        return this;
    }

    /**
     * returns tweets by users located within a given radius of the given
     * latitude/longitude, where the user's location is taken from their Twitter
     * profile
     *
     * @param location geo location
     * @param radius   radius
     * @param unit     Query.MILES or Query.KILOMETERS
     */
    public void setGeoCode(final GeoLocation location, final double radius, final String unit) {
        geocode = location.getLatitude() + "," + location.getLongitude() + "," + radius + unit;
    }

    /**
     * restricts tweets to the given language, given by an <a
     * href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1 code</a>
     *
     * @param lang an <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
     *             code</a>
     */
    public void setLang(final String lang) {
        this.lang = lang;
    }

    /**
     * Specify the language of the query you are sending (only ja is currently
     * effective). This is intended for language-specific clients and the
     * default should work in the majority of cases.
     *
     * @param locale the locale
     * @since Twitter4J 2.1.1
     */
    public void setLocale(final String locale) {
        this.locale = locale;
    }

    /**
     * If specified, returns tweets with status ids less than the given id.
     *
     * @param maxId maxId
     * @since Twitter4J 2.1.1
     */
    public void setMaxId(final long maxId) {
        this.maxId = maxId;
    }

    /**
     * sets the page number (starting at 1) to return, up to a max of roughly
     * 1500 results
     *
     * @param page the page number (starting at 1) to return
     */
    public void setPage(final int page) {
        this.page = page;
    }

    /**
     * Sets the query string
     *
     * @param query the query string
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/search">GET search
     * | Twitter Developers</a>
     * @see <a href="http://search.twitter.com/operators">Twitter API / Search
     * Operators</a>
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * Default value is Query.MIXED if parameter not specified
     *
     * @param resultType Query.MIXED or Query.POPULAR or Query.RECENT
     * @since Twitter4J 2.1.3
     */
    public void setResultType(final String resultType) {
        this.resultType = resultType;
    }

    /**
     * sets the number of tweets to return per page, up to a max of 100
     *
     * @param count the number of tweets to return per page
     */
    public void setCount(final int count) {
        this.count = count;
    }

    /**
     * If specified, returns tweets with since the given date. Date should be
     * formatted as YYYY-MM-DD
     *
     * @param since since
     * @since Twitter4J 2.1.1
     */
    public void setSince(final String since) {
        this.since = since;
    }

    /**
     * returns tweets with status ids greater than the given id.
     *
     * @param sinceId returns tweets with status ids greater than the given id
     */
    public void setSinceId(final long sinceId) {
        this.sinceId = sinceId;
    }

    /**
     * If specified, returns tweets with generated before the given date. Date
     * should be formatted as YYYY-MM-DD
     *
     * @param until until
     * @since Twitter4J 2.1.1
     */
    public void setUntil(final String until) {
        this.until = until;
    }

    /**
     * If specified, returns tweets with since the given date. Date should be
     * formatted as YYYY-MM-DD
     *
     * @param since since
     * @return since
     * @since Twitter4J 2.1.1
     */
    public SearchQuery since(final String since) {
        setSince(since);
        return this;
    }

    /**
     * returns tweets with status ids greater than the given id.
     *
     * @param sinceId returns tweets with status ids greater than the given id
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery sinceId(final long sinceId) {
        setSinceId(sinceId);
        return this;
    }

    @Override
    public String toString() {
        return "Query{" + "query='" + query + '\'' + ", lang='" + lang + '\'' + ", locale='" + locale + '\''
                + ", maxId=" + maxId + ", count=" + count + ", page=" + page + ", since='" + since + '\'' + ", sinceId="
                + sinceId + ", geocode='" + geocode + '\'' + ", until='" + until + '\'' + ", resultType='" + resultType
                + '\'' + '}';
    }

    /**
     * If specified, returns tweets with generated before the given date. Date
     * should be formatted as YYYY-MM-DD
     *
     * @param until until
     * @return the instance
     * @since Twitter4J 2.1.1
     */
    public SearchQuery until(final String until) {
        setUntil(until);
        return this;
    }

    @Override
    public boolean has(String key) {
        switch (key) {
            case "q": {
                return query != null;
            }
            case "lang": {
                return lang != null;
            }
            case "locale": {
                return locale != null;
            }
            case "max_id": {
                return maxId != -1;
            }
            case "since_id": {
                return sinceId != -1;
            }
            case "count": {
                return count != -1;
            }
            case "page": {
                return page != -1;
            }
            case "since": {
                return since != null;
            }
            case "until": {
                return until != null;
            }
            case "geocode": {
                return geocode != null;
            }
            case "result_type": {
                return resultType != null;
            }
        }
        return false;
    }

    @Override
    public String get(String key) {
        switch (key) {
            case "q": {
                return query;
            }
            case "lang": {
                return lang;
            }
            case "locale": {
                return locale;
            }
            case "max_id": {
                if (maxId == -1) return null;
                return String.valueOf(maxId);
            }
            case "since_id": {
                if (sinceId == -1) return null;
                return String.valueOf(sinceId);
            }
            case "count": {
                if (count == -1) return null;
                return String.valueOf(count);
            }
            case "page": {
                if (page == -1) return null;
                return String.valueOf(page);
            }
            case "since": {
                return since;
            }
            case "until": {
                return until;
            }
            case "geocode": {
                return geocode;
            }
            case "result_type": {
                return resultType;
            }
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{"q", "lang", "locale", "max_id", "since_id", "count", "page", "since",
                "until", "geocode", "result_type"};
    }

}
