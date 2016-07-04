package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.mariotaku.twidere.TwidereConstants.DEFAULT_TWITTER_API_URL_FORMAT;
import static org.mariotaku.twidere.TwidereConstants.TWITTER_CONSUMER_KEY;
import static org.mariotaku.twidere.TwidereConstants.TWITTER_CONSUMER_SECRET;

/**
 * Created by mariotaku on 16/3/12.
 */
@JsonObject
public final class CustomAPIConfig {

    @JsonField(name = "name")
    String name;
    @JsonField(name = "localized_name")
    String localizedName;
    @JsonField(name = "api_url_format")
    String apiUrlFormat;
    @ParcelableCredentials.AuthType
    @JsonField(name = "auth_type", typeConverter = AuthTypeConverter.class)
    int authType;
    @JsonField(name = "same_oauth_url")
    boolean sameOAuthUrl;
    @JsonField(name = "no_version_suffix")
    boolean noVersionSuffix;
    @JsonField(name = "consumer_key")
    String consumerKey;
    @JsonField(name = "consumer_secret")
    String consumerSecret;

    CustomAPIConfig() {
    }

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

    public String getLocalizedName(Context context) {
        if (localizedName == null) return name;
        final Resources res = context.getResources();
        int id = res.getIdentifier(localizedName, "string", context.getPackageName());
        if (id != 0) {
            return res.getString(id);
        }
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
    public static List<CustomAPIConfig> listDefault(@NonNull Context context) {
        final AssetManager assets = context.getAssets();
        InputStream is = null;
        try {
            is = assets.open("data/default_api_configs.json");
            List<CustomAPIConfig> configList = JsonSerializer.parseList(is, CustomAPIConfig.class);
            if (configList == null) return listBuiltin(context);
            return configList;
        } catch (IOException e) {
            return listBuiltin(context);
        } finally {
            Utils.closeSilently(is);
        }
    }

    public static List<CustomAPIConfig> listBuiltin(@NonNull Context context) {
        return Collections.singletonList(new CustomAPIConfig(context.getString(R.string.provider_default),
                DEFAULT_TWITTER_API_URL_FORMAT, ParcelableCredentials.AuthType.OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET));
    }

    static class AuthTypeConverter extends StringBasedTypeConverter<Integer> {
        @Override
        @ParcelableCredentials.AuthType
        public Integer getFromString(String string) {
            if (string == null) return ParcelableCredentials.AuthType.OAUTH;
            switch (string) {
                case "oauth": {
                    return ParcelableCredentials.AuthType.OAUTH;
                }
                case "xauth": {
                    return ParcelableCredentials.AuthType.XAUTH;
                }
                case "basic": {
                    return ParcelableCredentials.AuthType.BASIC;
                }
                case "twip_o_mode": {
                    return ParcelableCredentials.AuthType.TWIP_O_MODE;
                }
            }
            return ParcelableCredentials.AuthType.OAUTH;
        }

        @Override
        public String convertToString(@ParcelableCredentials.AuthType Integer object) {
            if (object == null) return "oauth";
            switch (object) {
                case ParcelableCredentials.AuthType.OAUTH: {
                    return "oauth";
                }
                case ParcelableCredentials.AuthType.XAUTH: {
                    return "xauth";
                }
                case ParcelableCredentials.AuthType.BASIC: {
                    return "basic";
                }
                case ParcelableCredentials.AuthType.TWIP_O_MODE: {
                    return "twip_o_mode";
                }
            }
            return "oauth";
        }
    }
}
