package org.mariotaku.twidere.model.util;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UserKeyConverter extends StringBasedTypeConverter<UserKey> {

    @Override
    public UserKey getFromString(String string) {
        return UserKey.valueOf(string);
    }

    @Override
    public String convertToString(UserKey object) {
        if (object == null) return null;
        return object.toString();
    }
}
