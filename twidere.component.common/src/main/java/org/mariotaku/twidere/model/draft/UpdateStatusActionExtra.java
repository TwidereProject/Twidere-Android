package org.mariotaku.twidere.model.draft;

import android.os.Parcel;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 16/2/21.
 */
@ParcelablePlease
@JsonObject
public class UpdateStatusActionExtra implements ActionExtra {
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_status")
    ParcelableStatus inReplyToStatus;
    @ParcelableThisPlease
    @JsonField(name = "is_possibly_sensitive")
    boolean isPossiblySensitive;
    @ParcelableThisPlease
    @JsonField(name = "repost_status_id")
    String repostStatusId;
    @ParcelableThisPlease
    @JsonField(name = "display_coordinates")
    boolean displayCoordinates;

    public ParcelableStatus getInReplyToStatus() {
        return inReplyToStatus;
    }

    public void setInReplyToStatus(ParcelableStatus inReplyToStatus) {
        this.inReplyToStatus = inReplyToStatus;
    }

    public boolean isPossiblySensitive() {
        return isPossiblySensitive;
    }

    public void setIsPossiblySensitive(boolean isPossiblySensitive) {
        this.isPossiblySensitive = isPossiblySensitive;
    }

    public String isRepostStatusId() {
        return repostStatusId;
    }

    public void setRepostStatusId(String repostStatusId) {
        this.repostStatusId = repostStatusId;
    }

    public boolean getDisplayCoordinates() {
        return displayCoordinates;
    }

    public void setDisplayCoordinates(boolean displayCoordinates) {
        this.displayCoordinates = displayCoordinates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UpdateStatusActionExtraParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UpdateStatusActionExtra> CREATOR = new Creator<UpdateStatusActionExtra>() {
        @Override
        public UpdateStatusActionExtra createFromParcel(Parcel source) {
            UpdateStatusActionExtra target = new UpdateStatusActionExtra();
            UpdateStatusActionExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UpdateStatusActionExtra[] newArray(int size) {
            return new UpdateStatusActionExtra[size];
        }
    };
}
