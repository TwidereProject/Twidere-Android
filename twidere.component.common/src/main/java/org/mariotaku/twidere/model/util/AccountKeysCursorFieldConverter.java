package org.mariotaku.twidere.model.util;

import org.mariotaku.twidere.model.AccountKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class AccountKeysCursorFieldConverter extends AbsObjectArrayConverter<AccountKey> {


    @Override
    protected AccountKey[] newArray(int size) {
        return new AccountKey[size];
    }

    @Override
    protected AccountKey parseItem(String s) {
        return AccountKey.valueOf(s);
    }
}
