package org.mariotaku.twidere.extension.streaming.util;

import android.content.Context;
import android.database.Cursor;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.TwidereSharedPreferences;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;

import static android.text.TextUtils.isEmpty;

public class Utils implements TwidereConstants {


    public static long[] getActivatedAccountIds(final Context context) {
        long[] accounts = new long[0];
        if (context == null) return accounts;
        final String[] cols = new String[]{Accounts.ACCOUNT_ID};
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
                null, Accounts.ACCOUNT_ID);
        if (cur != null) {
            final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
            cur.moveToFirst();
            accounts = new long[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                accounts[i] = cur.getLong(idx);
                i++;
                cur.moveToNext();
            }
            cur.close();
        }
        return accounts;
    }

    public static String getNonEmptyString(final TwidereSharedPreferences pref, final String key, final String def) {
        if (pref == null) return def;
        final String val = pref.getString(key, def);
        return isEmpty(val) ? def : val;
    }

    public static String replaceLast(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) return text;
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

}
