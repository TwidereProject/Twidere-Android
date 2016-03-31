package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.UserKey;

import java.util.Arrays;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class TabArguments implements TwidereConstants {
    @JsonField(name = "account_id")
    String accountId;

    @JsonField(name = "account_keys")
    UserKey[] accountKeys;

    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    public void setAccountKeys(UserKey[] accountKeys) {
        this.accountKeys = accountKeys;
    }

    public String getAccountId() {
        return accountId;
    }

    @CallSuper
    public void copyToBundle(@NonNull Bundle bundle) {
        if (!ArrayUtils.isEmpty(accountKeys)) {
            for (UserKey key : accountKeys) {
                if (key == null) return;
            }
            bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, accountKeys);
        } else if (accountId != null) {
            final long id = NumberUtils.toLong(accountId, Long.MIN_VALUE);
            if (id != Long.MIN_VALUE && id <= 0) {
                // account_id = -1, means no account selected
                bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, null);
                return;
            }
            bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, new UserKey[]{UserKey.valueOf(accountId)});
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
