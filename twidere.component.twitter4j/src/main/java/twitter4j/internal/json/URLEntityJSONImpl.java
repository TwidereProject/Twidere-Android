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

import twitter4j.TwitterException;
import twitter4j.URLEntity;

/**
 * A data class representing one single URL entity.
 *
 * @author Mocel - mocel at guma.jp
 * @since Twitter4J 2.1.9
 */
/* package */final class URLEntityJSONImpl implements URLEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1326410198426703277L;
    private int start = -1;
    private int end = -1;
    private String url;
    private String expandedURL;
    private String displayURL;

    /* For serialization purposes only. */
    /* package */URLEntityJSONImpl() {

    }

    /* package */URLEntityJSONImpl(final int start, final int end, final String url, final String expandedURL,
                                   final String displayURL) {
        super();
        this.start = start;
        this.end = end;
        this.url = url;
        this.expandedURL = expandedURL;
        this.displayURL = displayURL;
    }

    /* package */URLEntityJSONImpl(final JSONObject json) throws TwitterException {
        super();
        init(json);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final URLEntityJSONImpl that = (URLEntityJSONImpl) o;

        if (end != that.end) return false;
        if (start != that.start) return false;
        if (displayURL != null ? !displayURL.equals(that.displayURL) : that.displayURL != null)
            return false;
        if (expandedURL != null ? !expandedURL.equalsIgnoreCase(that.expandedURL)
                : that.expandedURL != null) return false;
        if (url != null ? !url.equalsIgnoreCase(that.url) : that.url != null)
            return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayURL() {
        return displayURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnd() {
        return end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExpandedURL() {
        return expandedURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStart() {
        return start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURL() {
        return url;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (expandedURL != null ? expandedURL.hashCode() : 0);
        result = 31 * result + (displayURL != null ? displayURL.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "URLEntityJSONImpl{" + "start=" + start + ", end=" + end + ", url=" + url + ", expandedURL="
                + expandedURL + ", displayURL=" + displayURL + '}';
    }

    private void init(final JSONObject json) throws TwitterException {
        try {
            final JSONArray indicesArray = json.getJSONArray("indices");
            start = indicesArray.getInt(0);
            end = indicesArray.getInt(1);

            url = json.getString("url");

            if (!json.isNull("expanded_url")) {
                expandedURL = json.getString("expanded_url");
            }
            if (!json.isNull("display_url")) {
                displayURL = json.getString("display_url");
            }
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }
}
