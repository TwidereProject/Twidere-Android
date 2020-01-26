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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.io.IOException;

/**
 * Created by mariotaku on 15/5/10.
 */
@ParcelablePlease
@Keep
public class IDs extends TwitterResponseObject implements TwitterResponse, CursorSupport, Parcelable {

    long previousCursor;
    long nextCursor;
    String[] ids;

    @Override
    public long getNextCursor() {
        return nextCursor;
    }

    @Override
    public long getPreviousCursor() {
        return previousCursor;
    }

    @Override
    public boolean hasNext() {
        return nextCursor != 0;
    }

    @Override
    public boolean hasPrevious() {
        return previousCursor != 0;
    }

    public String[] getIDs() {
        return ids;
    }

    public static class Converter implements TypeConverter<IDs> {

        private static final JsonMapper<IDs> IDS_JSON_MAPPER = LoganSquare.mapperFor(IDs.class);

        @Override
        public IDs parse(JsonParser jsonParser) throws IOException {
            return IDS_JSON_MAPPER.parse(jsonParser);
        }

        @Override
        public void serialize(IDs object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
            if (writeFieldNameForObject) {
                jsonGenerator.writeFieldName(fieldName);
            }
            IDS_JSON_MAPPER.serialize(object, jsonGenerator, true);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        IDsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<IDs> CREATOR = new Creator<IDs>() {
        public IDs createFromParcel(Parcel source) {
            IDs target = new IDs();
            IDsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public IDs[] newArray(int size) {
            return new IDs[size];
        }
    };
}
