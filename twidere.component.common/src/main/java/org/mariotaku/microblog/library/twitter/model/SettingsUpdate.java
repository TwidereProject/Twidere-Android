/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
