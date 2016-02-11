package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.ParcelBagger;
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 16/1/2.
 */
@ParcelablePlease
@JsonObject
public class UploadLogEvent extends BaseEvent implements Parcelable {
    private static final String X_DNEXT_PREFIX = "X-Dnext";
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
    @ParcelableThisPlease
    @Bagger(StringMapBagger.class)
    @JsonField(name = "extra_headers")
    Map<String, String> extraHeaders;

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

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    public void setExtraHeaders(Map<String, String> extraHeaders) {
        this.extraHeaders = extraHeaders;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UploadLogEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public void finish(HttpURLConnection response) {
        HashMap<String, String> extraHeaders = new HashMap<>();
        final Map<String, List<String>> headers = response.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            final String name = entry.getKey();
            if (name == null) continue;
            if (name.regionMatches(true, 0, X_DNEXT_PREFIX, 0, X_DNEXT_PREFIX.length())) {
                for (String value : entry.getValue()) {
                    extraHeaders.put(name, value);
                }
            }
        }
        setExtraHeaders(extraHeaders);
        markEnd();
    }


    public static UploadLogEvent create(Context context, File file) {
        UploadLogEvent event = new UploadLogEvent();
        event.markStart(context);
        event.setFileLength(file.length());
        event.setFileName(file.getName());
        return event;
    }

    @Override
    public String toString() {
        return "UploadLogEvent{" +
                "fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                ", extraHeaders=" + extraHeaders +
                "} " + super.toString();
    }

    public boolean shouldSkip() {
        return fileName.contains(getLogFileName());
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "upload_log";
    }

    public static class StringMapBagger implements ParcelBagger<Map<String, String>> {
        @Override
        public void write(Map<String, String> value, Parcel out, int flags) {
            if (value == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(value.size());
                for (Map.Entry<String, String> entry : value.entrySet()) {
                    out.writeString(entry.getKey());
                    out.writeString(entry.getValue());
                }
            }
        }

        @Override
        public Map<String, String> read(Parcel in) {
            final int size = in.readInt();
            if (size < 0) return null;
            final Map<String, String> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                final String key = in.readString();
                final String value = in.readString();
                map.put(key, value);
            }
            return map;
        }
    }
}
