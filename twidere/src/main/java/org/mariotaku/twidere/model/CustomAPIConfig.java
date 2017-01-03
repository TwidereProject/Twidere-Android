package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.cred.Credentials;
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
@ParcelablePlease
@JsonObject
public final class CustomAPIConfig implements Parcelable {

    @JsonField(name = "name")
    String name;
    @AccountType
    @JsonField(name = "type")
    @Nullable
    String type;
    @JsonField(name = "localized_name")
    String localizedName;
    @JsonField(name = "api_url_format")
    String apiUrlFormat;
    @Credentials.Type
    @JsonField(name = "auth_type")
    String credentialsType;
    @JsonField(name = "same_oauth_url")
    boolean sameOAuthUrl;
    @JsonField(name = "no_version_suffix")
    boolean noVersionSuffix;
    @JsonField(name = "consumer_key")
    String consumerKey;
    @JsonField(name = "consumer_secret")
    String consumerSecret;

    public CustomAPIConfig() {
    }

    public CustomAPIConfig(String name, @Nullable String type, String apiUrlFormat,
                           String credentialsType, boolean sameOAuthUrl, boolean noVersionSuffix,
                           String consumerKey, String consumerSecret) {
        this.name = name;
        this.type = type;
        this.apiUrlFormat = apiUrlFormat;
        this.credentialsType = credentialsType;
        this.sameOAuthUrl = sameOAuthUrl;
        this.noVersionSuffix = noVersionSuffix;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    @Nullable
    public String getType() {
        return type;
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

    public String getCredentialsType() {
        return credentialsType;
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

    public void setApiUrlFormat(String apiUrlFormat) {
        this.apiUrlFormat = apiUrlFormat;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setCredentialsType(String credentialsType) {
        this.credentialsType = credentialsType;
    }

    public void setSameOAuthUrl(boolean sameOAuthUrl) {
        this.sameOAuthUrl = sameOAuthUrl;
    }

    public void setNoVersionSuffix(boolean noVersionSuffix) {
        this.noVersionSuffix = noVersionSuffix;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CustomAPIConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<CustomAPIConfig> CREATOR = new Creator<CustomAPIConfig>() {
        public CustomAPIConfig createFromParcel(Parcel source) {
            CustomAPIConfig target = new CustomAPIConfig();
            CustomAPIConfigParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public CustomAPIConfig[] newArray(int size) {
            return new CustomAPIConfig[size];
        }
    };

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

    public static CustomAPIConfig builtin(@NonNull Context context) {
        return new CustomAPIConfig(context.getString(R.string.provider_default), AccountType.TWITTER,
                DEFAULT_TWITTER_API_URL_FORMAT, Credentials.Type.OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
    }

    public static List<CustomAPIConfig> listBuiltin(@NonNull Context context) {
        return Collections.singletonList(builtin(context));
    }
}
