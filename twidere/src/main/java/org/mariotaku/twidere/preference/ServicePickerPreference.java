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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.List;

public abstract class ServicePickerPreference extends AutoInvalidateListPreference {

	private final PackageManager mPackageManager;

	public ServicePickerPreference(final Context context) {
		this(context, null);
	}

	public ServicePickerPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mPackageManager = context.getPackageManager();
		init();
	}

	@Override
	public CharSequence getSummary() {
		if (isNoneValue(getValue())) return getNoneEntry();
		return super.getSummary();
	}

	protected abstract String getIntentAction();

	protected abstract String getNoneEntry();

	private void init() {
		final Intent queryIntent = new Intent(getIntentAction());
		final List<ResolveInfo> infoList = mPackageManager.queryIntentServices(queryIntent, 0);
		final int infoListSize = infoList.size();
		final CharSequence[] entries = new CharSequence[infoListSize + 1], values = new CharSequence[infoListSize + 1];
		entries[0] = getNoneEntry();
		values[0] = "";
		for (int i = 0; i < infoListSize; i++) {
			final ResolveInfo info = infoList.get(i);
			entries[i + 1] = info.loadLabel(mPackageManager);
			values[i + 1] = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name).flattenToString();
		}
		setEntries(entries);
		setEntryValues(values);
	}

	public static boolean isNoneValue(final String value) {
		return TextUtils.isEmpty(value) || "none".equals(value);
	}

}
