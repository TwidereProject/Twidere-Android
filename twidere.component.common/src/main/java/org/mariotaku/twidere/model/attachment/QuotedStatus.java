package org.mariotaku.twidere.model.attachment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.UserKeyConverter;

@ParcelablePlease
@JsonObject
public class QuotedStatus implements Parcelable {

    @JsonField(name = "id")
    public String id;

    @JsonField(name = "timestamp")
    public long timestamp;

    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    public UserKey account_key;

    @JsonField(name = "user_id", typeConverter = UserKeyConverter.class)
    @Nullable
    public UserKey user_key;

    @JsonField(name = "user_is_protected")
    public boolean user_is_protected;

    @JsonField(name = "user_is_verified")
    public boolean user_is_verified;

    @JsonField(name = "text_plain")
    public String text_plain;

    @JsonField(name = "text_unescaped")
    public String text_unescaped;

    @JsonField(name = "source")
    public String source;

    @JsonField(name = "user_name")
    public String user_name;

    @JsonField(name = "user_screen_name")
    public String user_screen_name;

    @JsonField(name = "user_profile_image")
    public String user_profile_image;

    @JsonField(name = "media")
    @Nullable
    public ParcelableMedia[] media;

    @JsonField(name = "spans")
    public SpanItem[] spans;

    @JsonField(name = "external_url")
    public String external_url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        QuotedStatusParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<QuotedStatus> CREATOR = new Creator<QuotedStatus>() {
        public QuotedStatus createFromParcel(Parcel source) {
            QuotedStatus target = new QuotedStatus();
            QuotedStatusParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public QuotedStatus[] newArray(int size) {
            return new QuotedStatus[size];
        }
    };
}
