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

import java.io.File;
import java.io.FileFilter;

public class ClearCachePreference extends AsyncTaskPreference {

	public ClearCachePreference(final Context context) {
		this(context, null);
	}

	public ClearCachePreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ClearCachePreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void doInBackground() {
		final Context context = getContext();
		if (context == null) return;
		final File external_cache_dir = context.getExternalCacheDir();
		if (external_cache_dir != null) {
			for (final File file : external_cache_dir.listFiles((FileFilter) null)) {
				deleteRecursive(file);
			}
		}
		final File internal_cache_dir = context.getCacheDir();
		if (internal_cache_dir != null) {
			for (final File file : internal_cache_dir.listFiles((FileFilter) null)) {
				deleteRecursive(file);
			}
		}
	}

	private static void deleteRecursive(final File f) {
		if (f.isDirectory()) {
			for (final File c : f.listFiles()) {
				deleteRecursive(c);
			}
		}
		f.delete();
	}

}
