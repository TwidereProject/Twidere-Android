package org.mariotaku.twidere.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/22.
 */
@JsonObject
public class CacheMetadata {
    @JsonField(name = "content_type")
    String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "CacheMetadata{" +
                "contentType='" + contentType + '\'' +
                '}';
    }
}
