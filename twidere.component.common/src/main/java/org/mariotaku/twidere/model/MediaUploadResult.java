package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

@ParcelablePlease
@JsonObject
public class MediaUploadResult implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "media_uris")
    public String[] media_uris;

    @ParcelableThisPlease
    @JsonField(name = "error_code")
    public int error_code;

    @ParcelableThisPlease
    @JsonField(name = "error_message")
    public String error_message;

    @ParcelableThisPlease
    @JsonField(name = "extras")
    public String extras;

    @ParcelableThisPlease
    @JsonField(name = "shared_owners")
    public UserKey[] shared_owners;

    public static final Creator<MediaUploadResult> CREATOR = new Creator<MediaUploadResult>() {
        @Override
        public MediaUploadResult createFromParcel(Parcel source) {
            MediaUploadResult target = new MediaUploadResult();
            MediaUploadResultParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public MediaUploadResult[] newArray(int size) {
            return new MediaUploadResult[size];
        }
    };

    MediaUploadResult() {
    }

    public MediaUploadResult(final int errorCode, final String errorMessage) {
        if (errorCode == 0) throw new IllegalArgumentException("Error code must not be 0");
        media_uris = null;
        error_code = errorCode;
        error_message = errorMessage;
    }

    public MediaUploadResult(final String[] mediaUris) {
        if (mediaUris == null) throw new IllegalArgumentException("Media uris must not be null");
        media_uris = mediaUris;
        error_code = 0;
        error_message = null;
    }

    @Override
    public String toString() {
        return "MediaUploadResult{media_uris=" + Arrays.toString(media_uris) + ", error_code=" + error_code
                + ", error_message=" + error_message + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaUploadResultParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static MediaUploadResult getInstance(final int errorCode, final String errorMessage) {
        return new MediaUploadResult(errorCode, errorMessage);
    }

    public static MediaUploadResult getInstance(final String... mediaUris) {
        return new MediaUploadResult(mediaUris);
    }
}
