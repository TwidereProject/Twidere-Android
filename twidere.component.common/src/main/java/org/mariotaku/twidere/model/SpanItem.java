package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/3/21.
 */
@JsonObject
@ParcelablePlease
public class SpanItem implements Parcelable {
    public static final Creator<SpanItem> CREATOR = new Creator<SpanItem>() {
        @Override
        public SpanItem createFromParcel(Parcel in) {
            final SpanItem obj = new SpanItem();
            SpanItemParcelablePlease.readFromParcel(obj, in);
            return obj;
        }

        @Override
        public SpanItem[] newArray(int size) {
            return new SpanItem[size];
        }
    };

    @JsonField(name = "start")
    @ParcelableThisPlease
    public int start;
    @JsonField(name = "end")
    @ParcelableThisPlease
    public int end;
    @JsonField(name = "link")
    @ParcelableThisPlease
    public String link;

    @ParcelableNoThanks
    public int orig_start = -1;
    @ParcelableNoThanks
    public int orig_end = -1;

    @Override
    public String toString() {
        return "SpanItem{" +
                "start=" + start +
                ", end=" + end +
                ", link='" + link + '\'' +
                ", orig_start=" + orig_start +
                ", orig_end=" + orig_end +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SpanItemParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static SpanItem from(Spanned spanned, URLSpan span) {
        SpanItem spanItem = new SpanItem();
        spanItem.link = span.getURL();
        spanItem.start = spanned.getSpanStart(span);
        spanItem.end = spanned.getSpanEnd(span);
        return spanItem;
    }
}
