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
 * Created by mariotaku on 15/9/28.
 */
@JsonObject
public class BatteryRecord {
    @JsonField(name = "level")
    float level;
    @JsonField(name = "state")
    int state;
    @JsonField(name = "timestamp")
    long timestamp;
    @JsonField(name = "time_offset")
    long timeOffset;

    public long getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BatteryRecord{" +
                "level=" + level +
                ", state=" + state +
                ", timestamp=" + timestamp +
                ", timeOffset=" + timeOffset +
                '}';
    }

}
