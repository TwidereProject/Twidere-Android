package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableMedia;

/**
 * Created by mariotaku on 16/3/11.
 */
@JsonObject
@ParcelablePlease
public class MediaDownloadEvent extends BaseEvent implements Parcelable {

    @ParcelableMedia.Type
    @JsonField(name = "type")
    int type;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "media_url")
    String mediaUrl;
    @JsonField(name = "size")
    long size;

    public void setMedia(ParcelableMedia media) {
        this.type = media.type;
        this.url = media.url;
        this.mediaUrl = media.media_url;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "media_download";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaDownloadEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaDownloadEvent> CREATOR = new Creator<MediaDownloadEvent>() {
        public MediaDownloadEvent createFromParcel(Parcel source) {
            MediaDownloadEvent target = new MediaDownloadEvent();
            MediaDownloadEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MediaDownloadEvent[] newArray(int size) {
            return new MediaDownloadEvent[size];
        }
    };

    public static MediaDownloadEvent create(Context context, ParcelableMedia media, long total) {
        final MediaDownloadEvent event = new MediaDownloadEvent();
        event.markStart(context);
        event.setMedia(media);
        event.setSize(total);
        return event;
    }
}
