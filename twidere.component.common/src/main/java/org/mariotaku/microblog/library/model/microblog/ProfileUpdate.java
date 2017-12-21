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

package org.mariotaku.microblog.library.model.microblog;

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

    public void setBackgroundColor(int profileLinkColor) {
        put("profile_background_color", String.format(Locale.ROOT, "%06X", 0xFFFFFF & profileLinkColor));
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

    public ProfileUpdate backgroundColor(int linkColor) {
        setBackgroundColor(linkColor);
        return this;
    }
}
