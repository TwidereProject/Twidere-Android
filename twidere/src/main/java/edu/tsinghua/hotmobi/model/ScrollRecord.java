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

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 15/8/8.
 */
@JsonObject
public class ScrollRecord implements LogModel {
    @JsonField(name = "id")
    String id;
    @JsonField(name = "account_id")
    String accountId;
    @JsonField(name = "account_host")
    String accountHost;
    @JsonField(name = "timestamp")
    long timestamp;
    @JsonField(name = "time_offset")
    long timeOffset;
    @JsonField(name = "scroll_state")
    int scrollState;

    public static ScrollRecord create(String id, UserKey accountKey, long timestamp, long timeOffset, int scrollState) {
        final ScrollRecord record = new ScrollRecord();
        record.setId(id);
        record.setAccountId(accountKey.getId());
        record.setAccountHost(accountKey.getHost());
        record.setTimestamp(timestamp);
        record.setTimeOffset(timeOffset);
        record.setScrollState(scrollState);
        return record;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAccountHost(String accountHost) {
        this.accountHost = accountHost;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public void setId(String id) {
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
                ", mAccountKey=" + accountId +
                ", timestamp=" + timestamp +
                ", timeOffset=" + timeOffset +
                ", scrollState=" + scrollState +
                '}';
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "scroll";
    }
}
