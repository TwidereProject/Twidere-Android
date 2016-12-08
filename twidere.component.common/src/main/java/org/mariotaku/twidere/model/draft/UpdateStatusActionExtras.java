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
public class UpdateStatusActionExtras implements ActionExtras {
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_status")
    ParcelableStatus inReplyToStatus;
    @ParcelableThisPlease
    @JsonField(name = "is_possibly_sensitive")
    boolean possiblySensitive;
    @ParcelableThisPlease
    @JsonField(name = "repost_status_id")
    String repostStatusId;
    @ParcelableThisPlease
    @JsonField(name = "display_coordinates")
    boolean displayCoordinates;
    @ParcelableThisPlease
    @JsonField(name = "attachment_url")
    String attachmentUrl;

    public ParcelableStatus getInReplyToStatus() {
        return inReplyToStatus;
    }

    public void setInReplyToStatus(ParcelableStatus inReplyToStatus) {
        this.inReplyToStatus = inReplyToStatus;
    }

    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    public void setPossiblySensitive(boolean isPossiblySensitive) {
        this.possiblySensitive = isPossiblySensitive;
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

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UpdateStatusActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateStatusActionExtras that = (UpdateStatusActionExtras) o;

        if (possiblySensitive != that.possiblySensitive) return false;
        if (displayCoordinates != that.displayCoordinates) return false;
        if (inReplyToStatus != null ? !inReplyToStatus.equals(that.inReplyToStatus) : that.inReplyToStatus != null)
            return false;
        if (repostStatusId != null ? !repostStatusId.equals(that.repostStatusId) : that.repostStatusId != null)
            return false;
        return attachmentUrl != null ? attachmentUrl.equals(that.attachmentUrl) : that.attachmentUrl == null;

    }

    @Override
    public int hashCode() {
        int result = inReplyToStatus != null ? inReplyToStatus.hashCode() : 0;
        result = 31 * result + (possiblySensitive ? 1 : 0);
        result = 31 * result + (repostStatusId != null ? repostStatusId.hashCode() : 0);
        result = 31 * result + (displayCoordinates ? 1 : 0);
        result = 31 * result + (attachmentUrl != null ? attachmentUrl.hashCode() : 0);
        return result;
    }

    public static final Creator<UpdateStatusActionExtras> CREATOR = new Creator<UpdateStatusActionExtras>() {
        @Override
        public UpdateStatusActionExtras createFromParcel(Parcel source) {
            UpdateStatusActionExtras target = new UpdateStatusActionExtras();
            UpdateStatusActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UpdateStatusActionExtras[] newArray(int size) {
            return new UpdateStatusActionExtras[size];
        }
    };
}
