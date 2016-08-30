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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;

        if (url != null ? !url.equals(photo.url) : photo.url != null) return false;
        if (imageUrl != null ? !imageUrl.equals(photo.imageUrl) : photo.imageUrl != null)
            return false;
        if (thumbUrl != null ? !thumbUrl.equals(photo.thumbUrl) : photo.thumbUrl != null)
            return false;
        return largeUrl != null ? largeUrl.equals(photo.largeUrl) : photo.largeUrl == null;

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (thumbUrl != null ? thumbUrl.hashCode() : 0);
        result = 31 * result + (largeUrl != null ? largeUrl.hashCode() : 0);
        return result;
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
