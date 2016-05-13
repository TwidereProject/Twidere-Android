package org.mariotaku.microblog.library.statusnet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/2/29.
 */
@ParcelablePlease
@JsonObject
public class StatusNetConfig implements Parcelable {

    @JsonField(name = "site")
    Site site;

    public Site getSite() {
        return site;
    }

    @ParcelablePlease
    @JsonObject
    public static class Site implements Parcelable {
        @JsonField(name = "textlimit")
        int textLimit;

        public int getTextLimit() {
            return textLimit;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            StatusNetConfig$SiteParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Site> CREATOR = new Creator<Site>() {
            @Override
            public Site createFromParcel(Parcel source) {
                Site target = new Site();
                StatusNetConfig$SiteParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Site[] newArray(int size) {
                return new Site[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusNetConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusNetConfig> CREATOR = new Creator<StatusNetConfig>() {
        @Override
        public StatusNetConfig createFromParcel(Parcel source) {
            StatusNetConfig target = new StatusNetConfig();
            StatusNetConfigParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusNetConfig[] newArray(int size) {
            return new StatusNetConfig[size];
        }
    };
}
