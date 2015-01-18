package org.mariotaku.twidere.activity.support;

import static android.os.Environment.getExternalStorageState;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.utils.IoUtils;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImagePickerActivity extends ThemedActionBarActivity {

	public static final int REQUEST_PICK_IMAGE = 101;
	public static final int REQUEST_TAKE_PHOTO = 102;

	private Uri mTempPhotoUri;
	private CopyImageTask mTask;
	private Runnable mImageSelectedRunnable;

	@Override
	public int getThemeColor() {
		return 0;
	}

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getNoDisplayThemeResource(this);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		if (resultCode != RESULT_OK) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		final Uri src;
		switch (requestCode) {
			case REQUEST_PICK_IMAGE: {
				src = intent.getData();
				break;
			}
			case REQUEST_TAKE_PHOTO: {
				src = mTempPhotoUri;
				break;
			}
			default: {
				finish();
				return;
			}
		}
		if (src == null) return;
		mImageSelectedRunnable = new Runnable() {

			@Override
			public void run() {
				imageSelected(src);
			}
		};
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (INTENT_ACTION_TAKE_PHOTO.equals(action)) {
			takePhoto();
		} else if (INTENT_ACTION_PICK_FILE.equals(action)) {
			pickImage();
		} else {
			new ImageSourceDialogFragment().show(getSupportFragmentManager(), "image_source");
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		if (mImageSelectedRunnable != null) {
			runOnUiThread(mImageSelectedRunnable);
		}
	}

	@Override
	protected void onStop() {
		mImageSelectedRunnable = null;
		super.onStop();
	}

	private void imageSelected(final Uri uri) {
		final CopyImageTask task = mTask;
		if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) return;
		mTask = new CopyImageTask(this, uri);
		mTask.execute();
	}

	private void pickImage() {
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
			startActivityForResult(intent, REQUEST_PICK_IMAGE);
		} catch (final ActivityNotFoundException e) {
		}
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (!getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return;
		final File extCacheDir = getExternalCacheDir();
		final File file;
		try {
			file = File.createTempFile("temp_image_", ".tmp", extCacheDir);
		} catch (final IOException e) {
			return;
		}
		mTempPhotoUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
		try {
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		} catch (final ActivityNotFoundException e) {
		}
	}

	public static class ImageSourceDialogFragment extends BaseSupportDialogFragment implements OnClickListener {

		@Override
		public void onCancel(final DialogInterface dialog) {
			super.onCancel(dialog);
			final FragmentActivity a = getActivity();
			if (a != null) {
				a.finish();
			}
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final FragmentActivity activity = getActivity();
			if (!(activity instanceof ImagePickerActivity)) return;
			final ImagePickerActivity addImageActivity = (ImagePickerActivity) activity;
			final String source = getResources().getStringArray(R.array.value_image_sources)[which];
			if ("gallery".equals(source)) {
				addImageActivity.pickImage();
			} else if ("camera".equals(source)) {
				addImageActivity.takePhoto();
			}
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setItems(R.array.entries_image_sources, this);
			return builder.create();
		}

		@Override
		public void onDismiss(final DialogInterface dialog) {
			super.onDismiss(dialog);
		}
	}

	private static class CopyImageTask extends AsyncTask<Void, Void, SingleResponse<File>> {
		private final ImagePickerActivity mActivity;
		private final Uri mUri;

		private static final String TAG_COPYING_IMAGE = "copying_image";

		public CopyImageTask(final ImagePickerActivity activity, final Uri uri) {
			mActivity = activity;
			mUri = uri;
		}

		@Override
		protected SingleResponse<File> doInBackground(final Void... params) {
			final ContentResolver cr = mActivity.getContentResolver();
			InputStream is = null;
			OutputStream os = null;
			try {
				final File cacheDir = mActivity.getCacheDir();
				is = cr.openInputStream(mUri);
				final BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(cr.openInputStream(mUri), null, opts);
				final String mimeType = opts.outMimeType;
				final String suffix = mimeType != null ? "."
						+ MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) : null;
				final File outFile = File.createTempFile("temp_image_", suffix, cacheDir);
				os = new FileOutputStream(outFile);
				IoUtils.copyStream(is, os, null);
				return SingleResponse.getInstance(outFile);
			} catch (final IOException e) {
				return SingleResponse.getInstance(e);
			} finally {
				IoUtils.closeSilently(os);
				IoUtils.closeSilently(is);
			}
		}

		@Override
		protected void onPostExecute(final SingleResponse<File> result) {
			final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(TAG_COPYING_IMAGE);
			if (f instanceof DialogFragment) {
				((DialogFragment) f).dismiss();
			}
			if (result.hasData()) {
				final Intent data = new Intent();
				data.setData(Uri.fromFile(result.getData()));
				mActivity.setResult(RESULT_OK, data);
			} else if (result.hasException()) {
				Log.w(LOGTAG, result.getException());
			}
			mActivity.finish();
		}

		@Override
		protected void onPreExecute() {
			final ProgressDialogFragment f = ProgressDialogFragment.show(mActivity, TAG_COPYING_IMAGE);
			f.setCancelable(false);
		}
	}
}
