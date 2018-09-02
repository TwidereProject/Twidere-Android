package org.mariotaku.microblog.library.mastodon.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#emoji}
 */
@JsonObject
public class Emoji {
    /**
     * The shortcode of the emoji
     */
    @JsonField(name = "shortcode")
    String shortCode;
    /**
     * URL to the emoji static image
     */
    @JsonField(name = "static_url")
    String static_url;
    /**
     * URL to the emoji image
     */
    @JsonField(name = "url")
    String url;
    /**
     * Boolean that indicates if the emoji is visible in picker
     */
    @JsonField(name = "visible_in_picker")
    boolean visible_in_picker;

    public String getShortCode() {
        return shortCode;
    }

    public String getStaticUrl() {
        return static_url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isVisibleInPicker() {
        return visible_in_picker;
    }

    @Override
    public String toString() {
        return "Emoji{" +
                "shortcode='" + shortCode + '\'' +
                ", static_url='" + static_url + '\'' +
                ", url='" + url + '\'' +
                ", visible_in_picker=" + visible_in_picker +
                '}';
    }
}
