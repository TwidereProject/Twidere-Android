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

package org.mariotaku.microblog.library.twitter.model;

import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 15/1/6.
 */
public class SettingsUpdate extends SimpleValueMap {

    public void put(String key, boolean value) {
        super.put(key, String.valueOf(value));
    }

    public void put(String key, int value) {
        super.put(key, String.valueOf(value));
    }

    public void put(String key, String value) {
        super.put(key, value);
    }

    public void setTrendLocationWoeid(int woeid) {
        put("trend_location_woeid", woeid);
    }

    public void setSleepTimeEnabled(boolean enabled) {
        put("sleep_time_enabled", enabled);
    }

    public void setStartSleepTime(int startSleepTime) {
        put("start_sleep_time", startSleepTime);
    }

    public void setEndSleepTime(int endSleepTime) {
        put("end_sleep_time", endSleepTime);
    }

    public void setTimezone(String timezone) {
        put("time_zone", timezone);
    }

    public void setProtected(boolean userProtected) {
        put("protected", userProtected);
    }

    public void setLang(String lang) {
        put("lang", lang);
    }

    public void setScreenName(String screenName) {
        put("screen_name", screenName);
    }

    public void setUniversalQualityFiltering(boolean enabled) {
        put("universal_quality_filtering", enabled ? "enabled" : "disabled");
    }

    public void setSmartMute(boolean enabled) {
        put("smart_mute", enabled ? "enabled" : "disabled");
    }

}
