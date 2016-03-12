package org.mariotaku.twidere.model;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 16/3/12.
 */
public class CustomAPIConfig implements Constants {

    String name;
    String apiUrlFormat;
    int authType;
    boolean sameOAuthUrl;
    boolean noVersionSuffix;
    String consumerKey;
    String consumerSecret;

    public CustomAPIConfig(String name, String apiUrlFormat, int authType, boolean sameOAuthUrl,
                           boolean noVersionSuffix, String consumerKey, String consumerSecret) {
        this.name = name;
        this.apiUrlFormat = apiUrlFormat;
        this.authType = authType;
        this.sameOAuthUrl = sameOAuthUrl;
        this.noVersionSuffix = noVersionSuffix;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public String getName() {
        return name;
    }

    public String getApiUrlFormat() {
        return apiUrlFormat;
    }

    public int getAuthType() {
        return authType;
    }

    public boolean isSameOAuthUrl() {
        return sameOAuthUrl;
    }

    public boolean isNoVersionSuffix() {
        return noVersionSuffix;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    @NonNull
    public static CustomAPIConfig[] listDefault(@NonNull Context context) {
        CustomAPIConfig[] list = new CustomAPIConfig[2];
        list[0] = new CustomAPIConfig(context.getString(R.string.provider_default),
                DEFAULT_TWITTER_API_URL_FORMAT, ParcelableCredentials.AUTH_TYPE_OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        list[1] = new CustomAPIConfig(context.getString(R.string.provider_fanfou),
                DEFAULT_FANFOU_API_URL_FORMAT, ParcelableCredentials.AUTH_TYPE_OAUTH, true, true,
                FANFOU_CONSUMER_KEY, FANFOU_CONSUMER_SECRET);
        return list;
    }
}
