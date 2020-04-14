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

import org.mariotaku.restfu.http.SimpleValueMap;

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
public final class SearchQuery extends SimpleValueMap {

    @interface Unit {
        String MILES = "mi", KILOMETERS = "km";
    }


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
        setQuery(query);
    }


    /**
     * returns tweets by users located within a given radius of the given
     * latitude/longitude, where the user's location is taken from their Twitter
     * profile
     *
     * @param location geo location
     * @param radius   radius
     * @param unit     {@link Unit#KILOMETERS} or {@link Unit#MILES}
     * @return the instance
     * @since Twitter4J 2.1.0
     */
    public SearchQuery geoCode(final GeoLocation location, final double radius,
                               final @Unit String unit) {
        setGeoCode(location, radius, unit);
        return this;
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
    public SearchQuery maxId(final String maxId) {
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
    public SearchQuery count(final int rpp) {
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
     * @param unit     {@link Unit#KILOMETERS} or {@link Unit#MILES}
     */
    public void setGeoCode(@NonNull final GeoLocation location, final double radius,
                           @NonNull final @Unit String unit) {
        put("geocode", location.getLatitude() + "," + location.getLongitude() + "," + radius + unit);
    }

    /**
     * restricts tweets to the given language, given by an <a
     * href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1 code</a>
     *
     * @param lang an <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
     *             code</a>
     */
    public void setLang(final String lang) {
        put("lang", lang);
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
        put("locale", locale);
    }

    /**
     * If specified, returns tweets with status ids less than the given id.
     *
     * @param maxId maxId
     * @since Twitter4J 2.1.1
     */
    public void setMaxId(final String maxId) {
        put("max_id", maxId);
    }

    /**
     * sets the page number (starting at 1) to return, up to a max of roughly
     * 1500 results
     *
     * @param page the page number (starting at 1) to return
     */
    public void setPage(final int page) {
        put("page", page);
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
        put("q", query);
    }

    /**
     * Default value is Query.MIXED if parameter not specified
     *
     * @param resultType Query.MIXED or Query.POPULAR or Query.RECENT
     * @since Twitter4J 2.1.3
     */
    public void setResultType(final String resultType) {
        put("result_type", resultType);
    }

    /**
     * sets the number of tweets to return per page, up to a max of 100
     *
     * @param count the number of tweets to return per page
     */
    public void setCount(final int count) {
        put("count", count);
    }

    /**
     * If specified, returns tweets with since the given date. Date should be
     * formatted as YYYY-MM-DD
     *
     * @param since since
     * @since Twitter4J 2.1.1
     */
    public void setSince(final String since) {
        put("since", since);
    }

    /**
     * returns tweets with status ids greater than the given id.
     *
     * @param sinceId returns tweets with status ids greater than the given id
     */
    public void setSinceId(final String sinceId) {
        put("since_id", sinceId);
    }

    /**
     * If specified, returns tweets with generated before the given date. Date
     * should be formatted as YYYY-MM-DD
     *
     * @param until until
     * @since Twitter4J 2.1.1
     */
    public void setUntil(final String until) {
        put("until", until);
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
    public SearchQuery sinceId(final String sinceId) {
        setSinceId(sinceId);
        return this;
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


    public SearchQuery paging(Paging paging) {
        setPaging(paging);
        return this;
    }

    public void setPaging(Paging paging) {
        if (paging == null) return;
        copyValue(paging, "since_id");
        copyValue(paging, "max_id");
        copyValue(paging, "count");
        copyValue(paging, "page");
    }

}
