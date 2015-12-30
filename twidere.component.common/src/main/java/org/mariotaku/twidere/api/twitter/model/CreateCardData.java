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

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Example
 * <pre>
 * {@code
 * CreateCardData cardData = new CreateCardData("poll2choice_text_only");
 * cardData.putString("choice1_label", "Label 1");
 * cardData.putString("choice2_label", "Label 2");
 * }
 * </pre>
 * Created by mariotaku on 15/12/30.
 */
public class CreateCardData {

    private final Map<String, String> map = new LinkedHashMap<>();

    public CreateCardData(String name) {
        this(name, "1");
    }

    public CreateCardData(String name, String endpoint) {
        map.put("twitter:card", name);
        map.put("twitter:api:api:endpoint", endpoint);
    }

    public void putString(String key, String value) {
        map.put("twitter:string:" + key, value);
    }

    public void putLong(String key, long value) {
        map.put("twitter:long:" + key, String.valueOf(value));
    }

    @Override
    public String toString() {
        try {
            return LoganSquare.serialize(map);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
