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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.mariotaku.twidere.R;

import java.io.File;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

public class ClearCachePreference extends AsyncTaskPreference {

    public ClearCachePreference(final Context context) {
        this(context, null);
    }

    public ClearCachePreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public ClearCachePreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void doInBackground() {
        final Context context = getContext();
        if (context == null) return;
        final File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            final File[] files = externalCacheDir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    deleteRecursive(file);
                }
            }
        }
        final File internalCacheDir = context.getCacheDir();
        if (internalCacheDir != null) {
            final File[] files = internalCacheDir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    deleteRecursive(file);
                }
            }
        }
    }

    private static void deleteRecursive(final File f) {
        if (f.isDirectory()) {
            final File[] files = f.listFiles();
            if (files == null) return;
            for (final File c : files) {
                deleteRecursive(c);
            }
        }
        if (!f.delete()) {
            Log.w(LOGTAG, String.format("Unable to delete %s", f));
        }
    }

}
