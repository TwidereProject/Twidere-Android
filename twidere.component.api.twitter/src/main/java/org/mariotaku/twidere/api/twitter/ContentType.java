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

package org.mariotaku.twidere.api.twitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/4.
 */
public class ContentType {

    private final String contentType;

    @Override
    public String toString() {
        return "ContentType{" +
                "contentType='" + contentType + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    private final List<KeyValuePair> parameters;

    public ContentType(String contentType, List<KeyValuePair> parameters) {
        this.contentType = contentType;
        this.parameters = parameters;
    }

    public static ContentType parse(String string) {
        final List<KeyValuePair> parameters = new ArrayList<>();
        int previousIndex = string.indexOf(';', 0);
        String contentType;
        if (previousIndex == -1) {
            contentType = string;
        } else {
            contentType = string.substring(0, previousIndex);
        }
        while (previousIndex != -1) {
            final int idx = string.indexOf(';', previousIndex + 1);
            final String[] segs;
            if (idx < 0) {
                segs = Utils.split(string.substring(previousIndex + 1, string.length()).trim(), "=");
            } else {
                segs = Utils.split(string.substring(previousIndex + 1, idx).trim(), "=");
            }
            if (segs.length == 2) {
                parameters.add(new KeyValuePair(segs[0], segs[1]));
            }
            if (idx < 0) {
                break;
            }
            previousIndex = idx;
        }
        return new ContentType(contentType, parameters);
    }

    public String getContentType() {
        return contentType;
    }
}
