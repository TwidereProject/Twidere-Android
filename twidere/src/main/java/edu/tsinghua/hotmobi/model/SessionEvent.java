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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.DataStoreUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/8/8.
 */
@ParcelablePlease
@JsonObject
public class SessionEvent extends BaseEvent implements Parcelable {


    @JsonField(name = "configuration")
    @ParcelableThisPlease
    String configuration;

    @JsonField(name = "preferences")
    @ParcelableThisPlease
    HashMap<String, String> preferences;

    @JsonField(name = "device_preferences")
    @ParcelableThisPlease
    HashMap<String, String> devicePreferences;

    protected SessionEvent(Parcel in) {
        SessionEventParcelablePlease.readFromParcel(this, in);
    }

    public SessionEvent() {

    }

    public static SessionEvent create(Context context) {
        final SessionEvent event = new SessionEvent();
        event.markStart(context);
        final Context appContext = context.getApplicationContext();
        final Configuration conf = appContext.getResources().getConfiguration();
        event.setConfiguration(conf.toString());
        return event;
    }

    public String getConfiguration() {
        return configuration;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(HashMap<String, String> preferences) {
        this.preferences = preferences;
    }

    public Map<String, String> getDevicePreferences() {
        return devicePreferences;
    }

    public void setDevicePreferences(HashMap<String, String> devicePreferences) {
        this.devicePreferences = devicePreferences;
    }

    public void dumpPreferences(Context context) {
        final HashMap<String, String> preferences = new HashMap<>();
        for (AccountPreferences pref : AccountPreferences.getAccountPreferences(context, DataStoreUtils.getAccountKeys(context))) {
            final UserKey accountKey = pref.getAccountKey();
            preferences.put("notification_" + accountKey + "_home", String.valueOf(pref.isHomeTimelineNotificationEnabled()));
            preferences.put("notification_" + accountKey + "_interactions", String.valueOf(pref.isInteractionsNotificationEnabled()));
        }
        setPreferences(preferences);
        final HashMap<String, String> devicePreferences = new HashMap<>();
        devicePreferences.put("device_secure", isDeviceSecure(context));
        setDevicePreferences(devicePreferences);
    }

    private static String isDeviceSecure(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return String.valueOf(false);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return String.valueOf(km.isKeyguardSecure());
        } else {
            return String.valueOf(km.isDeviceSecure());
        }
    }

    @Override
    public String toString() {
        return "SessionEvent{" +
                "configuration='" + configuration + '\'' +
                ", preferences=" + preferences +
                "} " + super.toString();
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SessionEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SessionEvent> CREATOR = new Creator<SessionEvent>() {
        public SessionEvent createFromParcel(Parcel source) {
            SessionEvent target = new SessionEvent();
            SessionEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SessionEvent[] newArray(int size) {
            return new SessionEvent[size];
        }
    };

    @NonNull
    @Override
    public String getLogFileName() {
        return "session";
    }
}
