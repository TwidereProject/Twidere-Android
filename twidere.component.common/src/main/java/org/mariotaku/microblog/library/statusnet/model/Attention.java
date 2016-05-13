package org.mariotaku.microblog.library.statusnet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/3/7.
 */
@ParcelablePlease
@JsonObject
public class Attention implements Parcelable {

    @JsonField(name = "fullname")
    String fullName;
    @JsonField(name = "id")
    String id;
    @JsonField(name = "ostatus_uri")
    String ostatusUri;
    @JsonField(name = "profileurl")
    String profileUrl;
    @JsonField(name = "screen_name")
    String screenName;

    public String getFullName() {
        return fullName;
    }

    public String getId() {
        return id;
    }

    public String getOstatusUri() {
        return ostatusUri;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    @Override
    public String toString() {
        return "Attention{" +
                "fullName='" + fullName + '\'' +
                ", id='" + id + '\'' +
                ", ostatusUri='" + ostatusUri + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", screenName='" + screenName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AttentionParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Attention> CREATOR = new Creator<Attention>() {
        @Override
        public Attention createFromParcel(Parcel source) {
            Attention target = new Attention();
            AttentionParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Attention[] newArray(int size) {
            return new Attention[size];
        }
    };
}
