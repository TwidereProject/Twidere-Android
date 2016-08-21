package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/8/20.
 */
@JsonObject
public class PinTweetResult {
    @JsonField(name = "pinned_tweets")
    String[] pinnedTweets;

    public String[] getPinnedTweets() {
        return pinnedTweets;
    }
}
