package org.mariotaku.microblog.library.fanfou.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/3/10.
 */
@ParcelablePlease
@JsonObject
public class Photo implements Parcelable {
    @JsonField(name = "url")
    String url;
    @JsonField(name = "imageurl")
    String imageUrl;
    @JsonField(name = "thumburl")
    String thumbUrl;
    @JsonField(name = "largeurl")
    String largeUrl;

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", largeUrl='" + largeUrl + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PhotoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel source) {
            Photo target = new Photo();
            PhotoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
