package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.DataExportImportTypeSelectorDialogFragment;
import org.mariotaku.twidere.util.DataImportExportUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.io.IOException;

public class DataImportActivity extends ThemedFragmentActivity implements DataExportImportTypeSelectorDialogFragment.Callback {

    private ImportSettingsTask mImportSettingsTask;
    private OpenImportTypeTask mOpenImportTypeTask;
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
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_FILE: {
                mResumeFragmentsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (resultCode == RESULT_OK) {
                            final String path = data.getData().getPath();
                            if (mOpenImportTypeTask == null || mOpenImportTypeTask.getStatus() != AsyncTask.Status.RUNNING) {
                                mOpenImportTypeTask = new OpenImportTypeTask(DataImportActivity.this, path);
                                mOpenImportTypeTask.execute();
                            }
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
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mResumeFragmentsRunnable != null) {
            mResumeFragmentsRunnable.run();
        }
    }

    @Override
    public void onPositiveButtonClicked(final String path, final int flags) {
        if (path == null || flags == 0) {
            finish();
            return;
        }
        if (mImportSettingsTask == null || mImportSettingsTask.getStatus() != AsyncTask.Status.RUNNING) {
            mImportSettingsTask = new ImportSettingsTask(this, path, flags);
            mImportSettingsTask.execute();
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
            intent.setAction(INTENT_ACTION_PICK_FILE);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        }
    }

    static class ImportSettingsTask extends AsyncTask<Object, Object, Boolean> {
        private static final String FRAGMENT_TAG = "import_settings_dialog";

        private final DataImportActivity mActivity;
        private final String mPath;
        private final int mFlags;

        ImportSettingsTask(final DataImportActivity activity, final String path, final int flags) {
            mActivity = activity;
            mPath = path;
            mFlags = flags;
        }

        @Override
        protected Boolean doInBackground(final Object... params) {
            if (mPath == null) return false;
            final File file = new File(mPath);
            if (!file.isFile()) return false;
            try {
                DataImportExportUtils.importData(mActivity, file, mFlags);
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            final FragmentManager fm = mActivity.getSupportFragmentManager();
            final Fragment f = fm.findFragmentByTag(FRAGMENT_TAG);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
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

    static class OpenImportTypeTask extends AsyncTask<Object, Object, Integer> {

        private static final String FRAGMENT_TAG = "read_settings_data_dialog";

        private final DataImportActivity mActivity;
        private final String mPath;

        OpenImportTypeTask(final DataImportActivity activity, final String path) {
            mActivity = activity;
            mPath = path;
        }

        @Override
        protected Integer doInBackground(final Object... params) {
            if (mPath == null) return 0;
            final File file = new File(mPath);
            if (!file.isFile()) return 0;
            try {
                return DataImportExportUtils.getImportedSettingsFlags(file);
            } catch (final IOException e) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(final Integer flags) {
            final FragmentManager fm = mActivity.getSupportFragmentManager();
            final Fragment f = fm.findFragmentByTag(FRAGMENT_TAG);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            final DialogFragment df = new DataExportImportTypeSelectorDialogFragment();
            final Bundle args = new Bundle();
            args.putString(EXTRA_PATH, mPath);
            args.putString(EXTRA_TITLE, mActivity.getString(R.string.import_settings_type_dialog_title));
            if (flags != null) {
                args.putInt(EXTRA_FLAGS, flags);
            } else {
                args.putInt(EXTRA_FLAGS, 0);
            }
            df.setArguments(args);
            df.show(mActivity.getSupportFragmentManager(), "select_import_type");
        }

        @Override
        protected void onPreExecute() {
            ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
        }

    }
}
