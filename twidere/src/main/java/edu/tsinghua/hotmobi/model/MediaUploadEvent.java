package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableMediaUpdate;

/**
 * Created by mariotaku on 16/3/11.
 */
@JsonObject
@ParcelablePlease
public class MediaUploadEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "type")
    int type;
    @JsonField(name = "file_size")
    long fileSize;
    @JsonField(name = "width")
    int width;
    @JsonField(name = "height")
    int height;
    @JsonField(name = "network_type")
    int networkType;

    public void setMedia(ParcelableMediaUpdate media) {
        this.type = media.type;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setGeometry(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "media_upload";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaUploadEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "MediaUploadEvent{" +
                "height=" + height +
                ", type=" + type +
                ", size=" + fileSize +
                ", width=" + width +
                ", networkType=" + networkType +
                "} " + super.toString();
    }

    public static MediaUploadEvent create(@NonNull Context context, ParcelableMediaUpdate media) {
        final MediaUploadEvent event = new MediaUploadEvent();
        event.markStart(context);
        event.setMedia(media);
        event.setNetworkType(NetworkEvent.getActivateNetworkType(context));
        return event;
    }

    public static final Creator<MediaUploadEvent> CREATOR = new Creator<MediaUploadEvent>() {
        @Override
        public MediaUploadEvent createFromParcel(Parcel source) {
            MediaUploadEvent target = new MediaUploadEvent();
            MediaUploadEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public MediaUploadEvent[] newArray(int size) {
            return new MediaUploadEvent[size];
        }
    };
}
