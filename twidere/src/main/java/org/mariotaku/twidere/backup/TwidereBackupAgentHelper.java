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

package org.mariotaku.twidere.backup;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.Build;

import org.mariotaku.twidere.Constants;

@TargetApi(Build.VERSION_CODES.FROYO)
public class TwidereBackupAgentHelper extends BackupAgentHelper implements Constants {

	// A key to uniquely identify the set of backup data
	static final String PREFS_BACKUP_KEY = "preference_backup";

	@Override
	public void onCreate() {
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, SHARED_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, HOST_MAPPING_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, USER_COLOR_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, PERMISSION_PREFERENCES_NAME));
	}
}
