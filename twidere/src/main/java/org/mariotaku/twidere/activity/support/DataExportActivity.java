package org.mariotaku.twidere.activity.support;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.DataExportImportTypeSelectorDialogFragment;
import org.mariotaku.twidere.fragment.support.FileSelectorDialogFragment;
import org.mariotaku.twidere.util.DataImportExportUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataExportActivity extends BaseSupportActivity implements FileSelectorDialogFragment.Callback,
		DataExportImportTypeSelectorDialogFragment.Callback {

	private ExportSettingsTask mTask;

	@Override
	public Resources getResources() {
		return getDefaultResources();
	}

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
		if (df instanceof DataExportImportTypeSelectorDialogFragment) {
			finish();
		}
	}

	@Override
	public void onFilePicked(final File file) {
		if (file == null) {
			finish();
			return;
		}
		final DialogFragment df = new DataExportImportTypeSelectorDialogFragment();
		final Bundle args = new Bundle();
		args.putString(EXTRA_PATH, file.getAbsolutePath());
		args.putString(EXTRA_TITLE, getString(R.string.export_settings_type_dialog_title));
		df.setArguments(args);
		df.show(getSupportFragmentManager(), "select_export_type");
	}

	@Override
	public void onPositiveButtonClicked(final String path, final int flags) {
		if (path == null || flags == 0) {
			finish();
			return;
		}
		if (mTask == null || mTask.getStatus() != AsyncTask.Status.RUNNING) {
			mTask = new ExportSettingsTask(this, path, flags);
			mTask.execute();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			final File extStorage = Environment.getExternalStorageDirectory();
			final String storagePath = extStorage != null ? extStorage.getAbsolutePath() : "/";
			final FileSelectorDialogFragment f = new FileSelectorDialogFragment();
			final Bundle args = new Bundle();
			args.putString(EXTRA_ACTION, INTENT_ACTION_PICK_DIRECTORY);
			args.putString(EXTRA_PATH, storagePath);
			f.setArguments(args);
			f.show(getSupportFragmentManager(), "select_file");
		}
	}

	static class ExportSettingsTask extends AsyncTask<Void, Void, Boolean> {
		private static final String FRAGMENT_TAG = "import_settings_dialog";

		private final DataExportActivity mActivity;
		private final String mPath;
		private final int mFlags;

		ExportSettingsTask(final DataExportActivity activity, final String path, final int flags) {
			mActivity = activity;
			mPath = path;
			mFlags = flags;
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			if (mPath == null) return false;
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
			final String fileName = String.format("Twidere_Settings_%s.zip", sdf.format(new Date()));
			final File file = new File(mPath, fileName);
			file.delete();
			try {
				DataImportExportUtils.exportData(mActivity, file, mFlags);
				return true;
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			final FragmentManager fm = mActivity.getSupportFragmentManager();
			final DialogFragment f = (DialogFragment) fm.findFragmentByTag(FRAGMENT_TAG);
			if (f != null) {
				f.dismiss();
			}
			if (result != null && result) {
				mActivity.setResult(RESULT_OK);
			} else {
				mActivity.setResult(RESULT_CANCELED);
			}
			mActivity.finish();
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
		}

	}
}
