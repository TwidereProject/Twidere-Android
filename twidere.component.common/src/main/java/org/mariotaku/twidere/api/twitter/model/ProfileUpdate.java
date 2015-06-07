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

import org.mariotaku.restfu.http.SimpleValueMap;

import java.util.Locale;

/**
 * Created by mariotaku on 15/1/6.
 */
public class ProfileUpdate extends SimpleValueMap {

    public void setName(String name) {
        put("name", name);
    }

    public void setUrl(String url) {
        put("url", url);
    }

    public void setLocation(String location) {
        put("location", location);
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public void setLinkColor(int profileLinkColor) {
        put("profile_link_color", String.format(Locale.ROOT, "%06X", 0xFFFFFF & profileLinkColor));
    }

    public ProfileUpdate name(String name) {
        setName(name);
        return this;
    }

    public ProfileUpdate url(String url) {
        setUrl(url);
        return this;
    }

    public ProfileUpdate location(String location) {
        setLocation(location);
        return this;
    }

    public ProfileUpdate description(String description) {
        setDescription(description);
        return this;
    }

    public ProfileUpdate linkColor(int linkColor) {
        setLinkColor(linkColor);
        return this;
    }
}
