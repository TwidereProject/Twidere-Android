package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.model.UserKey;

import java.util.Arrays;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class TabArguments implements IntentConstants {
    @JsonField(name = "account_id")
    long accountId = -1;

    @JsonField(name = "account_keys")
    UserKey[] accountKeys;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    public void setAccountKeys(UserKey[] accountKeys) {
        this.accountKeys = accountKeys;
    }

    @CallSuper
    public void copyToBundle(@NonNull Bundle bundle) {
        if (accountId > 0) {
            bundle.putLong(EXTRA_ACCOUNT_ID, accountId);
        }
    }

    @Override
    public String toString() {
        return "TabArguments{" +
                "accountId=" + accountId +
                ", accountKeys=" + Arrays.toString(accountKeys) +
                '}';
    }
}
