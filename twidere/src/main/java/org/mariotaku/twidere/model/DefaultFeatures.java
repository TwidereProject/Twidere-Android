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

    private final static String REMOTE_SETTINGS_URL = "https://raw.githubusercontent.com/TwidereProject/Twidere-Android/master/twidere/src/main/assets/data/default_features.json";

    @JsonField(name = "media_link_counts_in_status")
    boolean mediaLinkCountsInStatus = true;


    public boolean isMediaLinkCountsInStatus() {
        return mediaLinkCountsInStatus;
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
    }

    public void save(SharedPreferences preferences) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_MEDIA_LINK_COUNTS_IN_STATUS, mediaLinkCountsInStatus);
        editor.apply();
    }
}
