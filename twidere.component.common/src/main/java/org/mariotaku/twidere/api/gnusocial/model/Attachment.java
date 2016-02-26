package org.mariotaku.twidere.api.gnusocial.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * GNUSocial attachment model
 *
 * Created by mariotaku on 16/1/26.
 */
@JsonObject
public class Attachment {
    @JsonField(name = "width")
    int width;
    @JsonField(name = "height")
    int height;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "thumb_url")
    String thumbUrl;
    @JsonField(name = "large_thumb_url")
    String largeThumbUrl;
    @JsonField(name = "mimetype")
    String mimetype;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "oembed")
    boolean oembed;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "version")
    String version;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getLargeThumbUrl() {
        return largeThumbUrl;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getId() {
        return id;
    }

    public boolean isOembed() {
        return oembed;
    }

    public long getSize() {
        return size;
    }

    public String getVersion() {
        return version;
    }
}
