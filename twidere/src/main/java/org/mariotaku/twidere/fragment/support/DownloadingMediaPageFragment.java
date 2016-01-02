package org.mariotaku.twidere.fragment.support;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.widget.Toast;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IExtendedActivity;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.loader.support.CacheDownloadLoader;
import org.mariotaku.twidere.provider.CacheProvider;
import org.mariotaku.twidere.task.SaveFileTask;
import org.mariotaku.twidere.task.SaveImageToGalleryTask;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.PermissionUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/1/2.
 */
public abstract class DownloadingMediaPageFragment extends BaseSupportFragment implements LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>, CacheDownloadLoader.Listener {

    protected static final int REQUEST_SHARE_MEDIA = 201;
    private boolean mLoaderInitialized;
    private SaveFileTask mSaveFileTask;
    private CacheDownloadLoader.Result mData;
    private File mShareFile;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SHARE_MEDIA: {
                if (mShareFile != null) {
                    mShareFile.delete();
                    mShareFile = null;
                }
                break;
            }
        }
    }

    @Override
    public final void onDownloadError(Throwable t) {
        hideProgress();
    }

    @Override
    public final void onDownloadFinished() {
        hideProgress();
    }

    @Override
    public final void onDownloadStart(long total) {
        showProgress(true, 0);
    }

    protected abstract void showProgress(boolean indeterminate, float progress);

    protected abstract void hideProgress();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_REQUEST_PERMISSIONS: {
                if (PermissionUtils.hasPermission(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    saveToGallery();
                } else {
                    Toast.makeText(getContext(), R.string.save_media_no_storage_permission_message, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public final void onProgressUpdate(long current, long total) {
        showProgress(false, current / (float) total);
    }

    @Override
    public final Loader<CacheDownloadLoader.Result> onCreateLoader(int id, Bundle args) {
        return new CacheDownloadLoader(getContext(), new MediaDownloader(getContext()), this, getMediaUrl());
    }


    protected final void saveToGallery() {
        if (mData == null) return;
        if (mSaveFileTask != null && mSaveFileTask.getStatus() == AsyncTask.Status.RUNNING) return;
        final Uri cacheUri = mData.cacheUri;
        final boolean hasImage = cacheUri != null;
        if (!hasImage) return;
        mSaveFileTask = SaveImageToGalleryTask.create(getActivity(), cacheUri);
        AsyncTaskUtils.executeTask(mSaveFileTask);
    }

    public final void loadMedia() {
        if (!hasValidMedia()) return;
        getLoaderManager().destroyLoader(0);
        if (!mLoaderInitialized) {
            getLoaderManager().initLoader(0, null, this);
            mLoaderInitialized = true;
        } else {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    protected abstract boolean hasValidMedia();


    protected final void shareMedia() {
        if (mData == null) return;
        final FragmentActivity activity = getActivity();
        final File destination = new File(activity.getCacheDir(), "shared_files");
        final SaveFileTask task = new SaveFileTask(activity, mData.cacheUri, destination,
                new CacheProvider.CacheFileTypeCallback(activity)) {
            private static final String PROGRESS_FRAGMENT_TAG = "progress";

            protected void dismissProgress() {
                final IExtendedActivity activity = (IExtendedActivity) getActivity();
                if (activity == null) return;
                activity.executeAfterFragmentResumed(new IExtendedActivity.Action() {
                    @Override
                    public void execute(IExtendedActivity activity) {
                        final FragmentManager fm = ((Activity) activity).getFragmentManager();
                        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
                        if (fragment != null) {
                            fragment.dismiss();
                        }
                    }
                });
            }

            protected void showProgress() {
                final IExtendedActivity activity = (IExtendedActivity) getActivity();
                if (activity == null) return;
                activity.executeAfterFragmentResumed(new IExtendedActivity.Action() {
                    @Override
                    public void execute(IExtendedActivity activity) {
                        final DialogFragment fragment = new ProgressDialogFragment();
                        fragment.setCancelable(false);
                        fragment.show(((Activity) activity).getFragmentManager(), PROGRESS_FRAGMENT_TAG);
                    }
                });
            }

            protected void onFileSaved(File savedFile, String mimeType) {
                final IExtendedActivity activity = (IExtendedActivity) getActivity();
                if (activity == null) return;
                final Context context = (Context) activity;

                mShareFile = savedFile;
                final Uri fileUri = FileProvider.getUriForFile(context, AUTHORITY_TWIDERE_FILE,
                        savedFile);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setDataAndType(fileUri, mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (activity instanceof ShareIntentProcessor) {
                    ((ShareIntentProcessor) activity).processShareIntent(intent);
                }
                startActivityForResult(Intent.createChooser(intent, context.getString(R.string.share)),
                        REQUEST_SHARE_MEDIA);
            }

        };
        task.execute();
    }

    protected final void requestAndSaveToGallery() {
        if (PermissionUtils.hasPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            saveToGallery();
        } else {
            final String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            requestPermissions(permissions, REQUEST_REQUEST_PERMISSIONS);
        }
    }

    protected abstract Uri getMediaUrl();

    @Override
    public final void onLoadFinished(Loader<CacheDownloadLoader.Result> loader, @NonNull CacheDownloadLoader.Result data) {
        mData = data;
        hideProgress();
        displayMedia(data);
    }

    protected abstract void displayMedia(CacheDownloadLoader.Result data);

    @Override
    public final void onLoaderReset(Loader<CacheDownloadLoader.Result> loader) {
        recycleMedia();
    }

    protected abstract void recycleMedia();

    public interface ShareIntentProcessor {
        void processShareIntent(Intent intent);
    }

    public static class MediaDownloader implements CacheDownloadLoader.Downloader {
        @Inject
        RestHttpClient mRestHttpClient;

        public MediaDownloader(Context context) {
            GeneralComponentHelper.build(context).inject(this);
        }

        @Override
        public InputStream get(String url) throws IOException {
            final RestHttpRequest.Builder builder = new RestHttpRequest.Builder();
            builder.method(GET.METHOD);
            builder.url(url);
            return mRestHttpClient.execute(builder.build()).getBody().stream();
        }
    }
}
