package org.mariotaku.twidere.model.tab;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class Arguments {
    @JsonField(name = "account_id")
    long accountId;

    @JsonField(name = "account_key")
    UserKey accountKey;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public UserKey getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(UserKey accountKey) {
        this.accountKey = accountKey;
    }
}
