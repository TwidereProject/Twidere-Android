package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.io.File;

/**
 * Created by mariotaku on 16/1/2.
 */
@ParcelablePlease
@JsonObject
public class UploadLogEvent extends BaseEvent implements Parcelable {
    public static final Creator<UploadLogEvent> CREATOR = new Creator<UploadLogEvent>() {
        public UploadLogEvent createFromParcel(Parcel source) {
            UploadLogEvent target = new UploadLogEvent();
            UploadLogEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UploadLogEvent[] newArray(int size) {
            return new UploadLogEvent[size];
        }
    };
    @ParcelableThisPlease
    @JsonField(name = "file_name")
    String fileName;
    @ParcelableThisPlease
    @JsonField(name = "file_length")

    long fileLength;

    public long getFileLength() {

        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UploadLogEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static UploadLogEvent create(Context context, File file) {
        UploadLogEvent event = new UploadLogEvent();
        event.markStart(context);
        event.setFileLength(file.length());
        event.setFileName(file.getName());
        return event;
    }

    public boolean shouldSkip() {
        return fileName.contains("upload_log");
    }
}
