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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.UrlEntity;

/**
 * Created by mariotaku on 15/3/31.
 */
@JsonObject
public class UrlEntityImpl implements UrlEntity {
    @JsonField(name = "indices", typeConverter = IndicesConverter.class)
    Indices indices;
    @JsonField(name = "display_url")
    String displayUrl;
    @JsonField(name = "expanded_url")
    String expandedUrl;

    @JsonField(name = "url")
    String url;

    @Override
    public String getDisplayUrl() {
        return displayUrl;
    }

    @Override
    public String toString() {
        return "UrlEntityImpl{" +
                "indices=" + indices +
                ", displayUrl='" + displayUrl + '\'' +
                ", expandedUrl='" + expandedUrl + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public int getEnd() {
        return indices.getEnd();
    }

    @Override
    public int getStart() {
        return indices.getStart();
    }

    @Override
    public String getExpandedUrl() {
        return expandedUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
