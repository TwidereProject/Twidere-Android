package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.AccountKey;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/7.
 */
public class AccountKeyConverter extends StringBasedTypeConverter<AccountKey> {

    @Override
    public AccountKey getFromString(String string) {
        return AccountKey.valueOf(string);
    }

    @Override
    public String convertToString(AccountKey object) {
        if (object == null) return null;
        return object.toString();
    }
}
