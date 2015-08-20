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

package edu.tsinghua.hotmobi.model;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

/**
 * Created by mariotaku on 15/8/18.
 */
public enum TimelineType {
    HOME("home"), INTERACTIONS("interactions"), OTHER("other");

    private final String value;

    TimelineType(String value) {
        this.value = value;
    }

    public static TimelineType parse(String type) {
        if (HOME.value.equalsIgnoreCase(type)) {
            return HOME;
        } else if (INTERACTIONS.value.equalsIgnoreCase(type)) {
            return INTERACTIONS;
        }
        return OTHER;
    }

    public static class TimelineTypeConverter extends StringBasedTypeConverter<TimelineType> {

        @Override
        public TimelineType getFromString(String string) {
            return TimelineType.parse(string);
        }

        @Override
        public String convertToString(TimelineType timelineType) {
            if (timelineType == null) return null;
            return timelineType.value;
        }
    }
}
