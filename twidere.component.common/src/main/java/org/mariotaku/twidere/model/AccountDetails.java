package org.mariotaku.twidere.model;

import android.accounts.Account;
import android.support.annotation.ColorInt;

import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.cred.Credentials;

/**
 * Created by mariotaku on 2016/12/3.
 */

public class AccountDetails {

    public Account account;
    public UserKey key;
    public Credentials credentials;
    public ParcelableUser user;
    @ColorInt
    public int color;
    public int position;
    public boolean activated;
    @AccountType
    public String type;
    @Credentials.Type
    public String credentials_type;
}
