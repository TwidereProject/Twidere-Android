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
import android.text.TextUtils;

import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.JSONObjectConverter;
import org.mariotaku.twidere.model.util.JSONParcelBagger;
import org.mariotaku.twidere.model.util.LoganSquareCursorFieldConverter;
import org.mariotaku.twidere.model.util.LongArrayConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;

@ParcelablePlease
@CursorObject
public class DraftItem implements Parcelable {

    @ParcelableThisPlease
    @CursorField(value = Drafts.ACCOUNT_IDS, converter = LongArrayConverter.class)
    public long[] account_ids;
    @ParcelableThisPlease
    @CursorField(Drafts._ID)
    public long _id;
    @ParcelableThisPlease
    @CursorField(Drafts.IN_REPLY_TO_STATUS_ID)
    public long in_reply_to_status_id;
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
    @CursorField(Drafts.IS_POSSIBLY_SENSITIVE)
    public boolean is_possibly_sensitive;
    @ParcelableThisPlease
    @CursorField(value = Drafts.LOCATION, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableLocation location;
    @ParcelableThisPlease
    @CursorField(Drafts.ACTION_TYPE)
    public int action_type;
    @Nullable
    @Bagger(JSONParcelBagger.class)
    @ParcelableThisPlease
    @CursorField(value = Drafts.ACTION_EXTRAS, converter = JSONObjectConverter.class)
    public JSONObject action_extras;


    public DraftItem() {

    }

    public DraftItem(ParcelableStatusUpdate status) {
        _id = 0;
        account_ids = ParcelableAccount.getAccountIds(status.accounts);
        in_reply_to_status_id = status.in_reply_to_status_id;
        text = status.text;
        media = status.media;
        is_possibly_sensitive = status.is_possibly_sensitive;
        location = status.location;
        timestamp = System.currentTimeMillis();
        action_type = Drafts.ACTION_UPDATE_STATUS;
        action_extras = createJSONObject(null);
    }

    private static JSONObject createJSONObject(String json) {
        if (TextUtils.isEmpty(json)) return null;
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DraftItemParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<DraftItem> CREATOR = new Creator<DraftItem>() {
        public DraftItem createFromParcel(Parcel source) {
            DraftItem target = new DraftItem();
            DraftItemParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public DraftItem[] newArray(int size) {
            return new DraftItem[size];
        }
    };
}
