package org.mariotaku.twidere.api.statusnet.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/7.
 */
@JsonObject
public class Attention {

    @JsonField(name = "fullname")
    String fullName;
    @JsonField(name = "id")
    String id;
    @JsonField(name = "ostatus_uri")
    String ostatusUri;
    @JsonField(name = "profileurl")
    String profileUrl;
    @JsonField(name = "screen_name")
    String screenName;

    public String getFullName() {
        return fullName;
    }

    public String getId() {
        return id;
    }

    public String getOstatusUri() {
        return ostatusUri;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    @Override
    public String toString() {
        return "Attention{" +
                "fullName='" + fullName + '\'' +
                ", id='" + id + '\'' +
                ", ostatusUri='" + ostatusUri + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", screenName='" + screenName + '\'' +
                '}';
    }
}
