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

package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.File;

public class CameraCropActivity extends Activity {

	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final int REQUEST_CROP_IMAGE = 2;

	private Uri mImageUri;
	private Uri mImageUriUncropped;

	public static final String INTENT_ACTION = "org.mariotaku.twidere.CAMERA_CROP";

	public static final String EXTRA_OUTPUT_X = "outputX";
	public static final String EXTRA_OUTPUT_Y = "outputY";
	public static final String EXTRA_ASPECT_X = "aspectX";
	public static final String EXTRA_ASPECT_Y = "aspectY";
	public static final String EXTRA_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK) {
			if (mImageUriUncropped != null) {
				final String path = mImageUriUncropped.getPath();
				if (path != null) {
					new File(path).delete();
				}
			}
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (mImageUriUncropped == null) {
					setResult(RESULT_CANCELED);
					finish();
					return;
				}
				final Intent intent = getIntent();
				final int aspectX = intent.getIntExtra(EXTRA_ASPECT_X, 1), aspectY = intent.getIntExtra(EXTRA_ASPECT_Y,
						1);
				final int outputX = intent.getIntExtra(EXTRA_OUTPUT_X, 512), outputY = intent.getIntExtra(
						EXTRA_OUTPUT_Y, 512);
				final boolean scaleUpIfNeeded = intent.getBooleanExtra(EXTRA_SCALE_UP_IF_NEEDED, false);
				final Intent crop_intent = new Intent("com.android.camera.action.CROP");
				crop_intent.setDataAndType(mImageUriUncropped, "image/*");
				crop_intent.putExtra(EXTRA_OUTPUT_X, outputX);
				crop_intent.putExtra(EXTRA_OUTPUT_Y, outputY);
				crop_intent.putExtra(EXTRA_ASPECT_X, aspectX);
				crop_intent.putExtra(EXTRA_ASPECT_Y, aspectY);
				crop_intent.putExtra("scale", true);
				crop_intent.putExtra("crop", "true");
				crop_intent.putExtra("scaleUpIfNeeded", scaleUpIfNeeded);
				crop_intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				try {
					startActivityForResult(crop_intent, REQUEST_CROP_IMAGE);
				} catch (final Exception e) {
					setResult(RESULT_CANCELED);
					finish();
					return;
				}
				break;
			}
			case REQUEST_CROP_IMAGE: {
				if (mImageUriUncropped != null) {
					final String path = mImageUriUncropped.getPath();
					if (path != null) {
						new File(path).delete();
					}
				}
				final Intent intent = new Intent();
				intent.setData(mImageUri);
				setResult(RESULT_OK, intent);
				finish();
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		try {
			mImageUri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
			mImageUriUncropped = Uri.parse(mImageUri.toString() + "_uncropped");
			final Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUriUncropped);
			startActivityForResult(camera_intent, REQUEST_TAKE_PHOTO);
		} catch (final Exception e) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
	}

}
