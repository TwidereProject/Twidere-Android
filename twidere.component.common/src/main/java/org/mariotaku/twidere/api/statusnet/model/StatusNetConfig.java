package org.mariotaku.twidere.api.statusnet.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/29.
 */
@JsonObject
public class StatusNetConfig {

    @JsonField(name = "site")
    Site site;

    public Site getSite() {
        return site;
    }

    @JsonObject
    public static class Site {
        @JsonField(name = "textlimit")
        int textLimit;

        public int getTextLimit() {
            return textLimit;
        }

    }
}
