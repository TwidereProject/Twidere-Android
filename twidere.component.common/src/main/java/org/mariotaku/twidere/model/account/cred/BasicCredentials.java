package org.mariotaku.twidere.model.account.cred;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 2016/12/2.
 */

@JsonObject
public class BasicCredentials extends Credentials {
    @JsonField(name = "username")
    public String username;
    @JsonField(name = "password")
    public String password;
}
