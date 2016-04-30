package org.mariotaku.twidere.model.util;

import org.mariotaku.commons.objectcursor.AbsArrayCursorFieldConverter;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UserKeysCursorFieldConverter extends AbsArrayCursorFieldConverter<UserKey> {

    @Override
    protected UserKey[] newArray(int size) {
        return new UserKey[size];
    }

    @Override
    protected UserKey parseItem(String s) {
        return UserKey.valueOf(s);
    }
}
