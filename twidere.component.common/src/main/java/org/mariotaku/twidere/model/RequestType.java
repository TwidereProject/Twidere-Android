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

package org.mariotaku.twidere.model;

/**
 * Created by mariotaku on 15/6/24.
 */
public enum RequestType {
    OTHER("other", 0), API("api", 1), MEDIA("media", 2), USAGE_STATISTICS("usage_statistics", 3);

    private final int value;

    private final String name;

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    RequestType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static int getValue(String name) {
        if ("api".equalsIgnoreCase(name)) return API.getValue();
        else if ("media".equalsIgnoreCase(name)) return MEDIA.getValue();
        else if ("usage_statistics".equalsIgnoreCase(name)) return USAGE_STATISTICS.getValue();
        return OTHER.getValue();
    }
}
