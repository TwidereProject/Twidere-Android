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
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.UserKey;

import java.util.TimeZone;

import edu.tsinghua.hotmobi.util.LocationUtils;

/**
 * Created by mariotaku on 15/10/10.
 */
@ParcelablePlease
@JsonObject
public class NotificationEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "item_id")
    @ParcelableThisPlease
    long itemId;

    @JsonField(name = "item_user_id")
    @ParcelableThisPlease
    long itemUserId;

    @JsonField(name = "account_id")
    @ParcelableThisPlease
    String accountId;

    @JsonField(name = "type")
    @ParcelableThisPlease
    String type;

    @JsonField(name = "action")
    @ParcelableThisPlease
    @Action
    String action;

    @JsonField(name = "ringer_mode")
    @ParcelableThisPlease
    int ringerMode;
    @JsonField(name = "item_user_following")
    @ParcelableThisPlease
    boolean itemUserFollowing;

    public NotificationEvent() {
    }

    public static NotificationEvent create(@NonNull  Context context, @Action String action, long postTime,
                                           long respondTime, String type, String accountId, long itemId,
                                           long itemUserId, boolean itemUserFollowing) {
        final NotificationEvent event = new NotificationEvent();
        event.setAction(action);
        event.setStartTime(postTime);
        event.setEndTime(respondTime);
        event.setTimeOffset(TimeZone.getDefault().getOffset(postTime));
        event.setLocation(LocationUtils.getCachedLatLng(context));
        event.setRingerMode(((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode());
        event.setType(type);
        event.setAccountId(accountId);
        event.setItemId(itemId);
        event.setItemUserId(itemUserId);
        event.setItemUserFollowing(itemUserFollowing);
        return event;
    }

    public static NotificationEvent deleted(@NonNull  Context context, long postTime, String type,
                                            UserKey accountKey, long itemId, long itemUserId,
                                            boolean itemUserFollowing) {
        return create(context, Action.DELETE, System.currentTimeMillis(), postTime, type, accountKey.getId(),
                itemId, itemUserId, itemUserFollowing);
    }

    public static NotificationEvent open(@NonNull  Context context, long postTime, String type, String accountId,
                                         long itemId, long itemUserId, boolean itemUserFollowing) {
        return create(context, Action.OPEN, System.currentTimeMillis(), postTime, type, accountId,
                itemId, itemUserId, itemUserFollowing);
    }

    public static boolean isSupported(String type) {
        if (type == null) return false;
        switch (type) {
            case "status":
            case "statuses":
            case "mention":
            case "mentions":
                return true;
        }
        return false;
    }

    public void setItemUserFollowing(boolean itemUserFollowing) {
        this.itemUserFollowing = itemUserFollowing;
    }

    @Action
    public String getAction() {
        return action;
    }

    public void setAction(@Action String action) {
        this.action = action;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getItemUserId() {
        return itemUserId;
    }

    public void setItemUserId(long itemUserId) {
        this.itemUserId = itemUserId;
    }

    public int getRingerMode() {
        return ringerMode;
    }

    public void setRingerMode(int ringerMode) {
        this.ringerMode = ringerMode;
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "notification";
    }

    @StringDef({Action.OPEN, Action.DELETE, Action.UNKNOWN})
    public @interface Action {
        String OPEN = "open";
        String DELETE = "delete";
        String UNKNOWN = "unknown";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        NotificationEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<NotificationEvent> CREATOR = new Creator<NotificationEvent>() {
        public NotificationEvent createFromParcel(Parcel source) {
            NotificationEvent target = new NotificationEvent();
            NotificationEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public NotificationEvent[] newArray(int size) {
            return new NotificationEvent[size];
        }
    };
}
