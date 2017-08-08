package org.mariotaku.twidere.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/9/9.
 */
@JsonObject
public class DefaultFeatures {

    @JsonField(name = "twitter_direct_message_media_limit")
    long twitterDirectMessageMediaLimit = 1;

    @JsonField(name = "twitter_direct_message_max_participants")
    long twitterDirectMessageMaxParticipants = 50;

    public long getTwitterDirectMessageMediaLimit() {
        return twitterDirectMessageMediaLimit;
    }

    public long getTwitterDirectMessageMaxParticipants() {
        return twitterDirectMessageMaxParticipants;
    }

}
