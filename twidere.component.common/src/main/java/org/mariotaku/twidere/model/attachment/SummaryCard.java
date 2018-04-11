package org.mariotaku.twidere.model.attachment;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
@JsonObject
public class SummaryCard implements Parcelable {

    @JsonField(name = "title")
    public String title;
    @JsonField(name = "description")
    public String description;
    @JsonField(name = "thumbnail")
    public String thumbnail;
    @JsonField(name = "domain")
    public String domain;
    @JsonField(name = "url")
    public String url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SummaryCardParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "SummaryCard{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", domain='" + domain + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public static final Creator<SummaryCard> CREATOR = new Creator<SummaryCard>() {
        public SummaryCard createFromParcel(Parcel source) {
            SummaryCard target = new SummaryCard();
            SummaryCardParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SummaryCard[] newArray(int size) {
            return new SummaryCard[size];
        }
    };
}
