package org.mariotaku.twidere.model;

import android.content.SharedPreferences;
import android.support.annotation.WorkerThread;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.RestHttpClient;

import java.io.IOException;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_MEDIA_LINK_COUNTS_IN_STATUS;

/**
 * Created by mariotaku on 16/9/9.
 */
@JsonObject
public class DefaultFeatures {

    private final static String REMOTE_SETTINGS_URL = "https://twidere.mariotaku.org/assets/data/default_features.json";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_KEY = "default_twitter_consumer_key";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_SECRET = "default_twitter_consumer_secret";

    @JsonField(name = "media_link_counts_in_status")
    boolean mediaLinkCountsInStatus = false;

    @JsonField(name = "default_twitter_consumer_key")
    String defaultTwitterConsumerKey;
    @JsonField(name = "default_twitter_consumer_secret")
    String defaultTwitterConsumerSecret;

    public boolean isMediaLinkCountsInStatus() {
        return mediaLinkCountsInStatus;
    }

    public String getDefaultTwitterConsumerKey() {
        return defaultTwitterConsumerKey;
    }

    public String getDefaultTwitterConsumerSecret() {
        return defaultTwitterConsumerSecret;
    }

    @WorkerThread
    public boolean loadRemoteSettings(RestHttpClient client) throws IOException {
        HttpRequest request = new HttpRequest.Builder().method(GET.METHOD).url(REMOTE_SETTINGS_URL).build();
        final HttpResponse response = client.newCall(request).execute();
        try {
            final JsonMapper<DefaultFeatures> mapper = LoganSquare.mapperFor(DefaultFeatures.class);
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
            response.close();
        }
        return true;
    }


    public void load(SharedPreferences preferences) {
        mediaLinkCountsInStatus = preferences.getBoolean(KEY_MEDIA_LINK_COUNTS_IN_STATUS,
                mediaLinkCountsInStatus);
        defaultTwitterConsumerKey = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, null);
        defaultTwitterConsumerSecret = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, null);
    }

    public void save(SharedPreferences preferences) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_MEDIA_LINK_COUNTS_IN_STATUS, mediaLinkCountsInStatus);
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, defaultTwitterConsumerKey);
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, defaultTwitterConsumerSecret);
        editor.apply();
    }
}
