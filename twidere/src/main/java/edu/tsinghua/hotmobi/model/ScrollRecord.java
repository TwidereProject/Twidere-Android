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
    @JsonField(name = "account_id")
    long accountId;
    @JsonField(name = "timestamp")
    long timestamp;
    @JsonField(name = "time_offset")
    long timeOffset;
    @JsonField(name = "scroll_state")
    int scrollState;

    public static ScrollRecord create(long id, long accountId, long timestamp, long timeOffset, int scrollState) {
        final ScrollRecord record = new ScrollRecord();
        record.setId(id);
        record.setAccountId(accountId);
        record.setTimestamp(timestamp);
        record.setTimeOffset(timeOffset);
        record.setScrollState(scrollState);
        return record;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public String toString() {
        return "ScrollRecord{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", timestamp=" + timestamp +
                ", timeOffset=" + timeOffset +
                ", scrollState=" + scrollState +
                '}';
    }
}
