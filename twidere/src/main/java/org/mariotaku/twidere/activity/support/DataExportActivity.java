package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.DataExportImportTypeSelectorDialogFragment;
import org.mariotaku.twidere.util.DataImportExportUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataExportActivity extends ThemedFragmentActivity implements DataExportImportTypeSelectorDialogFragment.Callback {

    private ExportSettingsTask mTask;
    private Runnable mResumeFragmentsRunnable;

    @Override
    public int getThemeColor() {
        return ThemeUtils.getThemeColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoDisplayThemeResource(this);
    }

    @Override
    public void onCancelled(DialogFragment df) {
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
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mResumeFragmentsRunnable != null) {
            mResumeFragmentsRunnable.run();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_DIRECTORY: {
                mResumeFragmentsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (resultCode == RESULT_OK) {
                            final String path = data.getData().getPath();
                            final DialogFragment df = new DataExportImportTypeSelectorDialogFragment();
                            final Bundle args = new Bundle();
                            args.putString(EXTRA_PATH, path);
                            args.putString(EXTRA_TITLE, getString(R.string.export_settings_type_dialog_title));
                            df.setArguments(args);
                            df.show(getSupportFragmentManager(), "select_export_type");
                        } else {
                            if (!isFinishing()) {
                                finish();
                            }
                        }
                    }
                };
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            final Intent intent = new Intent(this, FileSelectorActivity.class);
            intent.setAction(INTENT_ACTION_PICK_DIRECTORY);
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY);
        }
    }

    static class ExportSettingsTask extends AsyncTask<Object, Object, Boolean> {
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
        protected Boolean doInBackground(final Object... params) {
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
