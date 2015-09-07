/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.StringLongPair;
import org.mariotaku.twidere.util.collection.CompactHashSet;

import java.util.Set;

public class ReadStateManager implements Constants {

    private final SharedPreferencesWrapper mPreferences;

    public ReadStateManager(final Context context) {
        mPreferences = SharedPreferencesWrapper.getInstance(context,
                TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public long getPosition(final String key) {
        if (TextUtils.isEmpty(key)) return -1;
        return mPreferences.getLong(key, -1);
    }

    @NonNull
    public StringLongPair[] getPositionPairs(final String key) {
        if (TextUtils.isEmpty(key)) return new StringLongPair[0];
        final Set<String> set = mPreferences.getStringSet(key, null);
        if (set == null) return new StringLongPair[0];
        final StringLongPair[] pairs = new StringLongPair[set.size()];
        int count = 0;
        for (String entry : set.toArray(new String[set.size()])) {
            try {
                pairs[count++] = StringLongPair.valueOf(entry);
            } catch (NumberFormatException e) {
                return new StringLongPair[0];
            }
        }
        return pairs;
    }


    public long getPosition(final String key, final long keyId) {
        return getPosition(key, String.valueOf(keyId));
    }

    public long getPosition(final String key, final String keyId) {
        if (TextUtils.isEmpty(key)) return -1;
        final Set<String> set = mPreferences.getStringSet(key, null);
        if (set == null) return -1;
        final String prefix = keyId + ":";
        for (String entry : set) {
            if (entry.startsWith(prefix)) return StringLongPair.valueOf(entry).getValue();
        }
        return -1;
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean setPosition(String key, String keyId, long position) {
        return setPosition(key, keyId, position, false);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }


    public boolean setPosition(final String key, final long keyId, final long position, boolean acceptOlder) {
        return setPosition(key, String.valueOf(keyId), position, acceptOlder);
    }

    public boolean setPosition(final String key, final String keyId, final long position, boolean acceptOlder) {
        if (TextUtils.isEmpty(key)) return false;
        Set<String> set = mPreferences.getStringSet(key, null);
        if (set == null) {
            set = new CompactHashSet<>();
        }
        String keyValue = null;
        final String prefix = keyId + ":";
        for (String entry : set) {
            if (entry.startsWith(prefix)) {
                keyValue = entry;
                break;
            }
        }
        final StringLongPair pair;
        if (keyValue != null) {
            // Found value
            pair = StringLongPair.valueOf(keyValue);
            if (!acceptOlder && pair.getValue() > position) return false;
            set.remove(keyValue);
            pair.setValue(position);
        } else {
            pair = new StringLongPair(keyId, position);
        }
        set.add(pair.toString());
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(key, set);
        editor.apply();
        return true;
    }


    public boolean setPositionPairs(final String key, @Nullable final StringLongPair[] pairs) {
        if (TextUtils.isEmpty(key)) return false;
        final SharedPreferences.Editor editor = mPreferences.edit();
        if (pairs == null) {
            editor.remove(key);
        } else {
            final Set<String> set = new CompactHashSet<>();
            for (StringLongPair pair : pairs) {
                set.add(pair.toString());
            }
            editor.putStringSet(key, set);
        }
        editor.apply();
        return true;
    }

    public boolean setPosition(final String key, final long id) {
        return setPosition(key, id, false);
    }

    public boolean setPosition(final String key, final long id, boolean acceptOlder) {
        if (TextUtils.isEmpty(key) || !acceptOlder && getPosition(key) > id) return false;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, id);
        editor.apply();
        return true;
    }

    public interface OnReadStateChangeListener {
        void onReadStateChanged();
    }

}
