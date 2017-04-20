/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    @JsonField(name = "excluded_reply_user_ids")
    @ParcelableThisPlease
    String[] excludedReplyUserIds;
    @JsonField(name = "extended_reply_mode")
    @ParcelableThisPlease
    boolean extendedReplyMode;
    @JsonField(name = "editing_text")
    @ParcelableThisPlease
    String editingText;

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

    public String[] getExcludedReplyUserIds() {
        return excludedReplyUserIds;
    }

    public void setExcludedReplyUserIds(final String[] excludedReplyUserIds) {
        this.excludedReplyUserIds = excludedReplyUserIds;
    }

    public boolean isExtendedReplyMode() {
        return extendedReplyMode;
    }

    public void setExtendedReplyMode(final boolean extendedReplyMode) {
        this.extendedReplyMode = extendedReplyMode;
    }

    public String getEditingText() {
        return editingText;
    }

    public void setEditingText(final String editingText) {
        this.editingText = editingText;
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
