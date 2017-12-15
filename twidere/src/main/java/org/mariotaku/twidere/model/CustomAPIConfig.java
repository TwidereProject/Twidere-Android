package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mariotaku.twidere.Constants.DEFAULT_TWITTER_API_URL_FORMAT;
import static org.mariotaku.twidere.Constants.TWITTER_CONSUMER_KEY;
import static org.mariotaku.twidere.Constants.TWITTER_CONSUMER_SECRET;

@ParcelablePlease
@JsonObject
public final class CustomAPIConfig implements Parcelable {

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

    @JsonField(name = "name")
    String name;
    @AccountType
    @JsonField(name = "type")
    @Nullable
    String type;
    @JsonField(name = "api_url_format")
    @Nullable
    String apiUrlFormat;
    @Credentials.Type
    @JsonField(name = "auth_type")
    String credentialsType;
    @JsonField(name = "same_oauth_url")
    boolean sameOAuthUrl;
    @JsonField(name = "no_version_suffix")
    boolean noVersionSuffix;
    @JsonField(name = "consumer_key")
    @Nullable
    String consumerKey;
    @JsonField(name = "consumer_secret")
    @Nullable
    String consumerSecret;
    @Nullable
    @JsonField(name = "sign_up_url")
    String signUpUrl;
    @JsonField(name = "is_builtin")
    boolean isBuiltIn;
    @Nullable
    @JsonField(name = "overlays")
    @ParcelableNoThanks
    Map<String, CustomAPIConfig> overlays;
    boolean isDefault;
    @ParcelableNoThanks
    private boolean apiUrlFormatChanged;
    @ParcelableNoThanks
    private boolean credentialsTypeChanged;
    @ParcelableNoThanks
    private boolean sameOAuthUrlChanged;
    @ParcelableNoThanks
    private boolean noVersionSuffixChanged;
    @ParcelableNoThanks
    private boolean consumerKeyChanged;
    @ParcelableNoThanks
    private boolean consumerSecretChanged;
    @ParcelableNoThanks
    private boolean signUpUrlChanged;

    public CustomAPIConfig() {
    }

    public CustomAPIConfig(@NonNull String name, @Nullable String type, @Nullable String apiUrlFormat,
            String credentialsType, boolean sameOAuthUrl, boolean noVersionSuffix,
            @Nullable String consumerKey, @Nullable String consumerSecret) {
        this.name = name;
        this.type = type;
        this.apiUrlFormat = apiUrlFormat;
        this.credentialsType = credentialsType;
        this.sameOAuthUrl = sameOAuthUrl;
        this.noVersionSuffix = noVersionSuffix;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    @NonNull
    public static List<CustomAPIConfig> listDefault(@NonNull Context context) {
        final AssetManager assets = context.getAssets();
        try (InputStream is = assets.open("data/default_api_configs.json")) {
            return JsonSerializer.parseList(is, CustomAPIConfig.class);
        } catch (IOException e) {
            return listBuiltin(context);
        }
    }

    public static CustomAPIConfig twitter(@NonNull Context context) {
        return new CustomAPIConfig(context.getString(R.string.provider_twitter), AccountType.TWITTER,
                DEFAULT_TWITTER_API_URL_FORMAT, Credentials.Type.OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
    }

    public static CustomAPIConfig mastodon(@NonNull Context context) {
        return new CustomAPIConfig(context.getString(R.string.provider_mastodon), AccountType.MASTODON,
                null, Credentials.Type.OAUTH2, true, true, null, null);
    }

    public static List<CustomAPIConfig> listBuiltin(@NonNull Context context) {
        return Collections.singletonList(twitter(context));
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getApiUrlFormat() {
        return apiUrlFormat;
    }

    public void setApiUrlFormat(@Nullable String apiUrlFormat) {
        this.apiUrlFormat = apiUrlFormat;
        apiUrlFormatChanged = true;
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    public void setCredentialsType(String credentialsType) {
        this.credentialsType = credentialsType;
        credentialsTypeChanged = true;
    }

    public boolean isSameOAuthUrl() {
        return sameOAuthUrl;
    }

    public void setSameOAuthUrl(boolean sameOAuthUrl) {
        this.sameOAuthUrl = sameOAuthUrl;
        sameOAuthUrlChanged = true;
    }

    public boolean isNoVersionSuffix() {
        return noVersionSuffix;
    }

    public void setNoVersionSuffix(boolean noVersionSuffix) {
        this.noVersionSuffix = noVersionSuffix;
        noVersionSuffixChanged = true;
    }

    @Nullable
    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(@Nullable String consumerKey) {
        this.consumerKey = consumerKey;
        consumerKeyChanged = true;
    }

    @Nullable
    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(@Nullable String consumerSecret) {
        this.consumerSecret = consumerSecret;
        consumerSecretChanged = true;
    }

    @Nullable
    public String getSignUpUrl() {
        return signUpUrl;
    }

    public void setSignUpUrl(@Nullable String signUpUrl) {
        this.signUpUrl = signUpUrl;
        signUpUrlChanged = true;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    public void setBuiltIn(boolean builtIn) {
        isBuiltIn = builtIn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CustomAPIConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomAPIConfig that = (CustomAPIConfig) o;

        if (sameOAuthUrl != that.sameOAuthUrl) return false;
        if (noVersionSuffix != that.noVersionSuffix) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (apiUrlFormat != null ? !apiUrlFormat.equals(that.apiUrlFormat) : that.apiUrlFormat != null)
            return false;
        if (credentialsType != null ? !credentialsType.equals(that.credentialsType) : that.credentialsType != null)
            return false;
        if (consumerKey != null ? !consumerKey.equals(that.consumerKey) : that.consumerKey != null)
            return false;
        return consumerSecret != null ? consumerSecret.equals(that.consumerSecret) : that.consumerSecret == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (apiUrlFormat != null ? apiUrlFormat.hashCode() : 0);
        result = 31 * result + (credentialsType != null ? credentialsType.hashCode() : 0);
        result = 31 * result + (sameOAuthUrl ? 1 : 0);
        result = 31 * result + (noVersionSuffix ? 1 : 0);
        result = 31 * result + (consumerKey != null ? consumerKey.hashCode() : 0);
        result = 31 * result + (consumerSecret != null ? consumerSecret.hashCode() : 0);
        return result;
    }

    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (overlays != null) {
            String bestKey = null;
            int maxMatchingVersion = 0;
            for (String key : overlays.keySet()) {
                try {
                    int version = Integer.parseInt(key);
                    if (version <= BuildConfig.VERSION_CODE && version > maxMatchingVersion) {
                        bestKey = key;
                        maxMatchingVersion = version;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            if (bestKey != null) {
                CustomAPIConfig overlay = overlays.get(bestKey);
                applyOverlay(overlay);
            }
        }
    }

    private void applyOverlay(CustomAPIConfig overlay) {
        if (overlay.apiUrlFormatChanged) {
            apiUrlFormat = overlay.apiUrlFormat;
        }
        if (overlay.credentialsTypeChanged) {
            credentialsType = overlay.credentialsType;
        }
        if (overlay.sameOAuthUrlChanged) {
            sameOAuthUrl = overlay.sameOAuthUrl;
        }
        if (overlay.noVersionSuffixChanged) {
            noVersionSuffix = overlay.noVersionSuffix;
        }
        if (overlay.consumerKeyChanged) {
            consumerKey = overlay.consumerKey;
        }
        if (overlay.consumerSecretChanged) {
            consumerSecret = overlay.consumerSecret;
        }
        if (overlay.signUpUrlChanged) {
            signUpUrl = overlay.signUpUrl;
        }
    }
}
