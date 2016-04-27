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
public class SendDirectMessageActionExtra implements ActionExtra {
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
        SendDirectMessageActionExtraParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SendDirectMessageActionExtra> CREATOR = new Creator<SendDirectMessageActionExtra>() {
        @Override
        public SendDirectMessageActionExtra createFromParcel(Parcel source) {
            SendDirectMessageActionExtra target = new SendDirectMessageActionExtra();
            SendDirectMessageActionExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public SendDirectMessageActionExtra[] newArray(int size) {
            return new SendDirectMessageActionExtra[size];
        }
    };
}
