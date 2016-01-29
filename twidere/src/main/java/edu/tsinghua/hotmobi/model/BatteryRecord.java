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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.TimeZone;

import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/9/28.
 */
@JsonObject
public class BatteryRecord implements LogModel {
    @JsonField(name = "level")
    float level;
    @JsonField(name = "state")
    int state;
    @JsonField(name = "timestamp")
    long timestamp;
    @JsonField(name = "time_offset")
    long timeOffset;

    public static void log(Context context) {
        final Context app = context.getApplicationContext();
        log(context, app.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
    }

    public static void log(Context context, Intent intent) {
        if (intent == null) return;
        if (!intent.hasExtra(BatteryManager.EXTRA_LEVEL) || !intent.hasExtra(BatteryManager.EXTRA_SCALE) ||
                !intent.hasExtra(BatteryManager.EXTRA_STATUS)) return;
        final BatteryRecord record = new BatteryRecord();
        record.setLevel(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (float)
                intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1));
        record.setState(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
        record.setTimestamp(System.currentTimeMillis());
        record.setTimeOffset(TimeZone.getDefault().getRawOffset());
        HotMobiLogger.getInstance(context).log(record, null);
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

    @NonNull
    @Override
    public String getLogFileName() {
        return "battery";
    }
}
