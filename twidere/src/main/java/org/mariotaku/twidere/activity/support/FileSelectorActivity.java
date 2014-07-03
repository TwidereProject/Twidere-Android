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

package org.mariotaku.twidere.activity.support;

import static android.os.Environment.getExternalStorageDirectory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.mariotaku.twidere.fragment.support.FileSelectorDialogFragment;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;

public class FileSelectorActivity extends BaseSupportDialogActivity implements FileSelectorDialogFragment.Callback {

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getNoDisplayThemeResource(this);
	}

	@Override
	public void onCancelled(final DialogFragment df) {
		if (!isFinishing()) {
			finish();
		}
	}

	@Override
	public void onDismissed(final DialogFragment df) {
		if (!isFinishing()) {
			finish();
		}
	}

	@Override
	public void onFilePicked(final File file) {
		final Intent intent = new Intent();
		intent.setData(Uri.fromFile(file));
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		File initialDirectory = data != null ? new File(data.getPath()) : getExternalStorageDirectory();
		if (initialDirectory == null) {
			initialDirectory = new File("/");
		}
		final String action = intent.getAction();
		if (!INTENT_ACTION_PICK_FILE.equals(action) && !INTENT_ACTION_PICK_DIRECTORY.equals(action)) {
			finish();
			return;
		}

		final FileSelectorDialogFragment f = new FileSelectorDialogFragment();
		final Bundle args = new Bundle();
		args.putString(EXTRA_ACTION, action);
		args.putString(EXTRA_PATH, initialDirectory.getAbsolutePath());
		args.putStringArray(EXTRA_FILE_EXTENSIONS, intent.getStringArrayExtra(EXTRA_FILE_EXTENSIONS));
		f.setArguments(args);
		f.show(getSupportFragmentManager(), "select_file");
	}

}
