package org.mariotaku.twidere.model.tab;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.model.AccountKey;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class Arguments {
    @JsonField(name = "account_id")
    long accountId;

    @JsonField(name = "account_key")
    AccountKey accountKey;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public AccountKey getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(AccountKey accountKey) {
        this.accountKey = accountKey;
    }
}
