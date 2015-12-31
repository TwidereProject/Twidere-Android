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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.api.twitter.model.CardEntity;

import java.util.Map;

/**
 * Created by mariotaku on 15/12/31.
 */
@ParcelablePlease
@JsonObject
public final class ParcelableBindingValue implements Parcelable {

    public static final Creator<ParcelableBindingValue> CREATOR = new Creator<ParcelableBindingValue>() {
        public ParcelableBindingValue createFromParcel(Parcel source) {
            ParcelableBindingValue target = new ParcelableBindingValue();
            ParcelableBindingValueParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableBindingValue[] newArray(int size) {
            return new ParcelableBindingValue[size];
        }
    };
    @ParcelableThisPlease
    @JsonField(name = "type")
    public String type;
    @ParcelableThisPlease
    @JsonField(name = "value")
    public String value;

    public ParcelableBindingValue() {
    }

    public ParcelableBindingValue(CardEntity.BindingValue value) {
        if (value instanceof CardEntity.ImageValue) {
            this.type = CardEntity.BindingValue.TYPE_IMAGE;
            this.value = ((CardEntity.ImageValue) value).getUrl();
        } else if (value instanceof CardEntity.StringValue) {
            this.type = CardEntity.BindingValue.TYPE_STRING;
            this.value = ((CardEntity.StringValue) value).getValue();
        } else if (value instanceof CardEntity.BooleanValue) {
            this.type = CardEntity.BindingValue.TYPE_BOOLEAN;
            this.value = String.valueOf(((CardEntity.BooleanValue) value).getValue());
        } else if (value instanceof CardEntity.UserValue) {
            this.type = CardEntity.BindingValue.TYPE_USER;
            this.value = String.valueOf(((CardEntity.UserValue) value).getUserId());
        }
    }

    public static Map<String, ParcelableBindingValue> from(Map<String, CardEntity.BindingValue> bindingValues) {
        if (bindingValues == null) return null;
        final ArrayMap<String, ParcelableBindingValue> map = new ArrayMap<>();
        for (Map.Entry<String, CardEntity.BindingValue> entry : bindingValues.entrySet()) {
            map.put(entry.getKey(), new ParcelableBindingValue(entry.getValue()));
        }
        return map;
    }

    @Override
    public String toString() {
        return value + " (" + type + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableBindingValueParcelablePlease.writeToParcel(this, dest, flags);
    }
}
