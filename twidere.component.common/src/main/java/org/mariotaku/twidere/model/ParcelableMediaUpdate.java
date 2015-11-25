package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.io.IOException;
import java.util.List;

@JsonObject
@ParcelablePlease(allFields = false)
public class ParcelableMediaUpdate implements Parcelable {

    public static final Parcelable.Creator<ParcelableMediaUpdate> CREATOR = new Parcelable.Creator<ParcelableMediaUpdate>() {
        @Override
        public ParcelableMediaUpdate createFromParcel(final Parcel in) {
            return new ParcelableMediaUpdate(in);
        }

        @Override
        public ParcelableMediaUpdate[] newArray(final int size) {
            return new ParcelableMediaUpdate[size];
        }
    };

    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "uri")
    public String uri;
    @JsonField(name = "type")
    public int type;

    public ParcelableMediaUpdate() {
    }

    public ParcelableMediaUpdate(final Parcel in) {
        uri = in.readString();
        type = in.readInt();
    }

    public ParcelableMediaUpdate(@NonNull final String uri, final int type) {
        this.uri = uri;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "ParcelableMediaUpdate{uri=" + uri + ", type=" + type + "}";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(uri);
        dest.writeInt(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMediaUpdate that = (ParcelableMediaUpdate) o;

        if (type != that.type) return false;
        return uri.equals(that.uri);

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + type;
        return result;
    }

    @Deprecated
    public static ParcelableMediaUpdate[] fromJSONString(final String json) {
        if (TextUtils.isEmpty(json)) return null;
        try {
            final List<ParcelableMediaUpdate> list = LoganSquare.parseList(json, ParcelableMediaUpdate.class);
            return list.toArray(new ParcelableMediaUpdate[list.size()]);
        } catch (final IOException e) {
            return null;
        }
    }

}