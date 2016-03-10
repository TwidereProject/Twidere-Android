package org.mariotaku.twidere.api.fanfou.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/10.
 */
@JsonObject
public class Photo {
    @JsonField(name = "url")
    String url;
    @JsonField(name = "imageurl")
    String imageUrl;
    @JsonField(name = "thumburl")
    String thumbUrl;
    @JsonField(name = "largeurl")
    String largeUrl;

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", largeUrl='" + largeUrl + '\'' +
                '}';
    }
}
