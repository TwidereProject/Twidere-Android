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

package twitter4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import twitter4j.http.HttpParameter;

/**
 * Created by mariotaku on 15/1/6.
 */
public class SettingsUpdate implements Serializable {

    private final HashMap<String, HttpParameter> settingsMap = new HashMap<>();

    public void set(String key, boolean value) {
        settingsMap.put(key, new HttpParameter(key, value));
    }

    public void set(String key, int value) {
        settingsMap.put(key, new HttpParameter(key, value));
    }

    public void set(String key, String value) {
        settingsMap.put(key, new HttpParameter(key, value));
    }

    public void setTrendLocationWoeid(int woeid) {
        set("trend_location_woeid", woeid);
    }

    public void setSleepTimeEnabled(boolean enabled) {
        set("sleep_time_enabled", enabled);
    }

    public void setStartSleepTime(int startSleepTime) {
        set("start_sleep_time", startSleepTime);
    }

    public void setEndSleepTime(int endSleepTime) {
        set("end_sleep_time", endSleepTime);
    }

    public void setTimezone(String timezone) {
        set("time_zone", timezone);
    }

    public void setProtected(boolean userProtected) {
        set("protected", userProtected);
    }

    public void setLang(String lang) {
        set("lang", lang);
    }

    public void setScreenName(String screenName) {
        set("screen_name", screenName);
    }


    void addToHttpParameterList(List<HttpParameter> parameterList) {
        parameterList.addAll(settingsMap.values());
    }

}
