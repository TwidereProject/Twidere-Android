package org.mariotaku.twidere.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/9/9.
 */
@JsonObject
public class DefaultFeatures {

    @JsonField(name = "default_twitter_consumer_key")
    String defaultTwitterConsumerKey;

    @JsonField(name = "default_twitter_consumer_secret")
    String defaultTwitterConsumerSecret;

    @JsonField(name = "twitter_direct_message_media_limit")
    long twitterDirectMessageMediaLimit = 1;

    @JsonField(name = "twitter_direct_message_max_participants")
    long twitterDirectMessageMaxParticipants = 50;

    public String getDefaultTwitterConsumerKey() {
        return defaultTwitterConsumerKey;
    }

    public void setDefaultTwitterConsumerKey(String defaultTwitterConsumerKey) {
        this.defaultTwitterConsumerKey = defaultTwitterConsumerKey;
    }

    public String getDefaultTwitterConsumerSecret() {
        return defaultTwitterConsumerSecret;
    }

    public void setDefaultTwitterConsumerSecret(String defaultTwitterConsumerSecret) {
        this.defaultTwitterConsumerSecret = defaultTwitterConsumerSecret;
    }

    public long getTwitterDirectMessageMediaLimit() {
        return twitterDirectMessageMediaLimit;
    }

    public void setTwitterDirectMessageMediaLimit(long twitterDirectMessageMediaLimit) {
        this.twitterDirectMessageMediaLimit = twitterDirectMessageMediaLimit;
    }

    public long getTwitterDirectMessageMaxParticipants() {
        return twitterDirectMessageMaxParticipants;
    }

    public void setTwitterDirectMessageMaxParticipants(long twitterDirectMessageMaxParticipants) {
        this.twitterDirectMessageMaxParticipants = twitterDirectMessageMaxParticipants;
    }
}
