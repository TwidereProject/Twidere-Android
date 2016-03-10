package org.mariotaku.twidere.api.fanfou.model;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import org.mariotaku.twidere.api.twitter.model.Status;

import java.io.IOException;

/**
 * Created by mariotaku on 16/3/10.
 */
@JsonObject
public class FanfouSearchStatus extends Status {

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (getId() == null || getText() == null) throw new IOException("Malformed Status object");
        fixStatus();
    }

}
