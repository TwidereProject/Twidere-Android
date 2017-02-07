/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.draft.ActionExtras;
import org.mariotaku.twidere.model.util.DraftExtrasConverter;
import org.mariotaku.twidere.model.util.UserKeysCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

@ParcelablePlease
@CursorObject(valuesCreator = true, tableInfo = true)
public class Draft implements Parcelable {

    @ParcelableThisPlease
    @CursorField(value = Drafts._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;
    @ParcelableThisPlease
    @CursorField(value = Drafts.ACCOUNT_KEYS, converter = UserKeysCursorFieldConverter.class)
    @Nullable
    public UserKey[] account_keys;
    @ParcelableThisPlease
    @CursorField(Drafts.TIMESTAMP)
    public long timestamp;
    @ParcelableThisPlease
    @CursorField(Drafts.TEXT)
    public String text;
    @ParcelableThisPlease
    @CursorField(value = Drafts.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMediaUpdate[] media;
    @ParcelableThisPlease
    @CursorField(value = Drafts.LOCATION, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableLocation location;
    @Action
    @ParcelableThisPlease
    @CursorField(Drafts.ACTION_TYPE)
    public String action_type;
    @Nullable
    @ParcelableThisPlease
    @CursorField(value = Drafts.ACTION_EXTRAS, converter = DraftExtrasConverter.class)
    public ActionExtras action_extras;
    @Nullable
    @ParcelableThisPlease
    @CursorField(value = Drafts.UNIQUE_ID)
    public String unique_id;

    /**
     * For internal use only
     */
    @Nullable
    @ParcelableNoThanks
    public String remote_extras;

    public Draft() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DraftParcelablePlease.writeToParcel(this, dest, flags);
    }

    @AfterCursorObjectCreated
    void afterCursorObjectCreated() {
        if (unique_id == null) {
            unique_id = UUID.nameUUIDFromBytes((_id + ":" + timestamp).getBytes()).toString();
        }
    }

    public static final Creator<Draft> CREATOR = new Creator<Draft>() {
        @Override
        public Draft createFromParcel(Parcel source) {
            Draft target = new Draft();
            DraftParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Draft[] newArray(int size) {
            return new Draft[size];
        }
    };

    @StringDef({Action.UPDATE_STATUS, Action.REPLY, Action.QUOTE, Action.SEND_DIRECT_MESSAGE,
            Action.FAVORITE, Action.RETWEET})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {

        String UPDATE_STATUS = "update_status";
        String UPDATE_STATUS_COMPAT_1 = "0";
        String UPDATE_STATUS_COMPAT_2 = "1";
        String REPLY = "reply";
        String QUOTE = "quote";
        String SEND_DIRECT_MESSAGE = "send_direct_message";
        String SEND_DIRECT_MESSAGE_COMPAT = "2";
        String FAVORITE = "favorite";
        String RETWEET = "retweet";

    }
}
