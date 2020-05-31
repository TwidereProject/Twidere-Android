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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.commons.logansquare.JsonStringConverter;
import org.mariotaku.twidere.model.draft.ActionExtras;
import org.mariotaku.twidere.model.util.DraftExtrasFieldConverter;

import java.io.IOException;
import java.util.Arrays;

@ParcelablePlease
@JsonObject
public class ParcelableStatusUpdate implements Parcelable {

    @JsonField(name = "accounts")
    @NonNull
    @ParcelableThisPlease
    public AccountDetails[] accounts;
    @JsonField(name = "media")
    @ParcelableThisPlease
    public ParcelableMediaUpdate[] media;
    @JsonField(name = "text")
    @ParcelableThisPlease
    public String text;
    @JsonField(name = "location")
    @ParcelableThisPlease
    public ParcelableLocation location;
    @JsonField(name = "display_coordinates")
    @ParcelableThisPlease
    public boolean display_coordinates = true;
    @JsonField(name = "in_reply_to_status")
    @ParcelableThisPlease
    public ParcelableStatus in_reply_to_status;
    @JsonField(name = "is_possibly_sensitive")
    @ParcelableThisPlease
    public boolean is_possibly_sensitive;
    @JsonField(name = "repost_status_id")
    @ParcelableThisPlease
    public String repost_status_id;
    @JsonField(name = "attachment_url")
    @ParcelableThisPlease
    public String attachment_url;
    @JsonField(name = "excluded_reply_user_ids")
    @ParcelableThisPlease
    public String[] excluded_reply_user_ids;
    @JsonField(name = "extended_reply_mode")
    @ParcelableThisPlease
    public boolean extended_reply_mode;
    @JsonField(name = "summary")
    @ParcelableThisPlease
    public String summary;
    @JsonField(name = "visibility")
    @ParcelableThisPlease
    public String visibility;
    @JsonField(name = "draft_unique_id")
    @ParcelableThisPlease
    public String draft_unique_id;
    @JsonField(name = "draft_action")
    @ParcelableThisPlease
    @Draft.Action
    public String draft_action;
    @ParcelableThisPlease
    @Nullable
    public ActionExtras draft_extras;

    @JsonField(name = "draft_extras", typeConverter = JsonStringConverter.class)
    String rawDraftExtras;

    public ParcelableStatusUpdate() {
    }

    @Override
    public String toString() {
        return "ParcelableStatusUpdate{" +
                "accounts=" + Arrays.toString(accounts) +
                ", media=" + Arrays.toString(media) +
                ", text='" + text + '\'' +
                ", location=" + location +
                ", display_coordinates=" + display_coordinates +
                ", in_reply_to_status=" + in_reply_to_status +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                ", repost_status_id='" + repost_status_id + '\'' +
                ", attachment_url='" + attachment_url + '\'' +
                ", excluded_reply_user_ids=" + Arrays.toString(excluded_reply_user_ids) +
                ", extended_reply_mode=" + extended_reply_mode +
                ", summary='" + summary + '\'' +
                ", visibility='" + visibility + '\'' +
                ", draft_unique_id='" + draft_unique_id + '\'' +
                ", draft_action='" + draft_action + '\'' +
                ", draft_extras=" + draft_extras +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusUpdateParcelablePlease.writeToParcel(this, dest, flags);
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        draft_extras = DraftExtrasFieldConverter.parseExtras(draft_action, rawDraftExtras);
    }

    @OnPreJsonSerialize
    void onPreJsonSerialize() throws IOException {
        rawDraftExtras = DraftExtrasFieldConverter.serializeExtras(draft_extras);
    }

    public static final Creator<ParcelableStatusUpdate> CREATOR = new Creator<ParcelableStatusUpdate>() {
        @Override
        public ParcelableStatusUpdate createFromParcel(Parcel source) {
            ParcelableStatusUpdate target = new ParcelableStatusUpdate();
            ParcelableStatusUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableStatusUpdate[] newArray(int size) {
            return new ParcelableStatusUpdate[size];
        }
    };
}
