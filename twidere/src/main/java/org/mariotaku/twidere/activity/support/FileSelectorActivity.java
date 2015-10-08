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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;

import org.mariotaku.twidere.fragment.support.FileSelectorDialogFragment;
import org.mariotaku.twidere.util.PermissionUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

public class FileSelectorActivity extends BaseSupportDialogActivity implements FileSelectorDialogFragment.Callback {


    private Runnable mResumeFragmentsRunnable;

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
    protected void onStart() {
        super.onStart();
        setVisible(true);
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
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mResumeFragmentsRunnable != null) {
            mResumeFragmentsRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions,
                                           final @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_REQUEST_PERMISSIONS) {
            mResumeFragmentsRunnable = new Runnable() {
                @Override
                public void run() {
                    if (PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        showPickFileDialog();
                    } else {
                        finishWithDeniedMessage();
                    }
                }
            };
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        final String action = intent.getAction();
        if (!INTENT_ACTION_PICK_FILE.equals(action) && !INTENT_ACTION_PICK_DIRECTORY.equals(action)) {
            finish();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showPickFileDialog();
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_REQUEST_PERMISSIONS);
        } else {
            finishWithDeniedMessage();
        }
    }

    private void finishWithDeniedMessage() {
        if (isFinishing()) return;
        finish();
    }

    private void showPickFileDialog() {
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String action = intent.getAction();
        File initialDirectory = data != null ? new File(data.getPath()) : getExternalStorageDirectory();
        if (initialDirectory == null) {
            initialDirectory = new File("/");
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
