package org.mariotaku.twidere.model.account.cred;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.twidere.provider.TwidereDataStore;

/**
 * Created by mariotaku on 2016/12/2.
 */

@JsonObject
public class OAuthCredentials extends Credentials {
    @JsonField(name = "consumer_key")
    public String consumer_key;
    @JsonField(name = "consumer_secret")
    public String consumer_secret;

    @JsonField(name = "access_token")
    public String access_token;
    @JsonField(name = "access_token_secret")
    public String access_token_secret;

    @JsonField(name = "same_oauth_signing_url")
    public boolean same_oauth_signing_url;
}
