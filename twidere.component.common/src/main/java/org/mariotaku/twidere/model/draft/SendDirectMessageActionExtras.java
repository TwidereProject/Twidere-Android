package org.mariotaku.twidere.model.draft;

import android.os.Parcel;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/2/21.
 */
@ParcelablePlease
@JsonObject
public class SendDirectMessageActionExtras implements ActionExtras {
    @ParcelableThisPlease
    @JsonField(name = "recipient_id")
    String recipientId;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SendDirectMessageActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendDirectMessageActionExtras that = (SendDirectMessageActionExtras) o;

        return recipientId != null ? recipientId.equals(that.recipientId) : that.recipientId == null;

    }

    @Override
    public int hashCode() {
        return recipientId != null ? recipientId.hashCode() : 0;
    }

    public static final Creator<SendDirectMessageActionExtras> CREATOR = new Creator<SendDirectMessageActionExtras>() {
        @Override
        public SendDirectMessageActionExtras createFromParcel(Parcel source) {
            SendDirectMessageActionExtras target = new SendDirectMessageActionExtras();
            SendDirectMessageActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public SendDirectMessageActionExtras[] newArray(int size) {
            return new SendDirectMessageActionExtras[size];
        }
    };
}
