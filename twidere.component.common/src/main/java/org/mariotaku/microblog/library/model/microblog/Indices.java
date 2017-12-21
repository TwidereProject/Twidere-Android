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

package org.mariotaku.microblog.library.model.microblog;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Indices implements Parcelable {

    int start, end;

    Indices() {

    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public Indices(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "Index{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        IndicesParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Indices> CREATOR = new Creator<Indices>() {
        @Override
        public Indices createFromParcel(Parcel source) {
            Indices target = new Indices();
            IndicesParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Indices[] newArray(int size) {
            return new Indices[size];
        }
    };
}
