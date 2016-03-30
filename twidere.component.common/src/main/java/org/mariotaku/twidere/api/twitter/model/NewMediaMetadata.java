package org.mariotaku.twidere.api.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/30.
 */
@JsonObject
public class NewMediaMetadata {
    @JsonField(name = "media_id")
    String mediaId;
    @JsonField(name = "alt_text")
    AltText altText;

    NewMediaMetadata() {

    }

    public NewMediaMetadata(String mediaId, String altText) {
        this.mediaId = mediaId;
        this.altText = new AltText(altText);
    }

    @Override
    public String toString() {
        return "NewMediaMetadata{" +
                "mediaId='" + mediaId + '\'' +
                ", altText=" + altText +
                '}';
    }

    @JsonObject
    public static class AltText {
        @JsonField(name = "text")
        String text;

        AltText() {

        }

        public AltText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "AltText{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}
