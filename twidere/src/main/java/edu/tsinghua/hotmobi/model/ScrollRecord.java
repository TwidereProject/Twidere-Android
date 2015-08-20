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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/8/8.
 */
@JsonObject
public class ScrollRecord {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "timestamp")
    long timestamp;
    @JsonField(name = "scroll_state")
    int scrollState;

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }

    public static ScrollRecord create(long id, long timestamp, int scrollState) {
        final ScrollRecord record = new ScrollRecord();
        record.setId(id);
        record.setTimestamp(timestamp);
        record.setScrollState(scrollState);
        return record;
    }
}
