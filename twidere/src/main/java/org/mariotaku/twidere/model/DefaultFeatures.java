package org.mariotaku.twidere.model;

import android.content.SharedPreferences;
import android.support.annotation.WorkerThread;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.util.Utils;

import java.io.IOException;

/**
 * Created by mariotaku on 16/9/9.
 */
@JsonObject
public class DefaultFeatures {

    private final static String REMOTE_SETTINGS_URL = "https://twidere.mariotaku.org/assets/data/default_features.json";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_KEY = "default_twitter_consumer_key";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_SECRET = "default_twitter_consumer_secret";

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

    public String getDefaultTwitterConsumerSecret() {
        return defaultTwitterConsumerSecret;
    }

    public long getTwitterDirectMessageMediaLimit() {
        return twitterDirectMessageMediaLimit;
    }

    public long getTwitterDirectMessageMaxParticipants() {
        return twitterDirectMessageMaxParticipants;
    }

    @WorkerThread
    public boolean loadRemoteSettings(RestHttpClient client) throws IOException {
        HttpRequest request = new HttpRequest.Builder().method(GET.METHOD).url(REMOTE_SETTINGS_URL).build();
        final HttpResponse response = client.newCall(request).execute();
        try {
            final JsonMapper<DefaultFeatures> mapper = LoganSquareMapperFinder.mapperFor(DefaultFeatures.class);
            final JsonParser jsonParser = LoganSquare.JSON_FACTORY.createParser(response.getBody().stream());
            if (jsonParser.getCurrentToken() == null) {
                jsonParser.nextToken();
            }
            if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
                return false;
            }
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                mapper.parseField(this, fieldName, jsonParser);
                jsonParser.skipChildren();
            }
        } finally {
            Utils.closeSilently(response);
        }
        return true;
    }


    public void load(SharedPreferences preferences) {
        defaultTwitterConsumerKey = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, null);
        defaultTwitterConsumerSecret = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, null);
    }

    public void save(SharedPreferences preferences) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, defaultTwitterConsumerKey);
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, defaultTwitterConsumerSecret);
        editor.apply();
    }
}
