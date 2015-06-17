package org.mariotaku.twidere.activity.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.github.ooxi.jdatauri.DataUri;
import com.soundcloud.android.crop.Crop;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ImageCropperActivity;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.RestFuNetworkStreamDownloader;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static android.os.Environment.getExternalStorageState;

public class ImagePickerActivity extends ThemedFragmentActivity {

    public static final int REQUEST_PICK_IMAGE = 101;
    public static final int REQUEST_TAKE_PHOTO = 102;
    public static final int REQUEST_CROP = 103;

    public static final String INTENT_ACTION_TAKE_PHOTO = INTENT_PACKAGE_PREFIX + "TAKE_PHOTO";
    public static final String INTENT_ACTION_PICK_IMAGE = INTENT_PACKAGE_PREFIX + "PICK_IMAGE";
    public static final String INTENT_ACTION_GET_IMAGE = INTENT_PACKAGE_PREFIX + "GET_IMAGE";

    public static final String EXTRA_ASPECT_X = "aspect_x";
    public static final String EXTRA_ASPECT_Y = "aspect_y";
    public static final String EXTRA_MAX_WIDTH = "max_width";
    public static final String EXTRA_MAX_HEIGHT = "max_height";

    private Uri mTempPhotoUri;
    private CopyImageTask mTask;
    private Runnable mImageSelectedRunnable;

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
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
        final boolean needsCrop;
        final Uri src;
        switch (requestCode) {
            case REQUEST_PICK_IMAGE: {
                needsCrop = true;
                src = intent.getData();
                break;
            }
            case REQUEST_TAKE_PHOTO: {
                needsCrop = true;
                src = mTempPhotoUri;
                break;
            }
            case REQUEST_CROP: {
                needsCrop = false;
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
                imageSelected(src, needsCrop, !needsCrop);
            }
        };
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mImageSelectedRunnable != null) {
            runOnUiThread(mImageSelectedRunnable);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (INTENT_ACTION_TAKE_PHOTO.equals(action)) {
            takePhoto();
        } else if (INTENT_ACTION_PICK_IMAGE.equals(action)) {
            pickImage();
        } else if (INTENT_ACTION_GET_IMAGE.equals(action)) {
            imageSelected(intent.getData(), true, false);
        } else {
            new ImageSourceDialogFragment().show(getSupportFragmentManager(), "image_source");
        }
    }

    @Override
    protected void onStop() {
        mImageSelectedRunnable = null;
        super.onStop();
    }

    private void imageSelected(final Uri uri, final boolean needsCrop, final boolean deleteSource) {
        final CopyImageTask task = mTask;
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) return;
        mTask = new CopyImageTask(this, uri, needsCrop, deleteSource);
        mTask.execute();
    }

    private void pickImage() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } catch (final ActivityNotFoundException ignored) {
        }
    }

    private void takePhoto() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Uri uri = createTempImageUri();
        mTempPhotoUri = uri;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        try {
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (final ActivityNotFoundException ignored) {
            takePhotoFallback(mTempPhotoUri);
        }
    }

    private Uri createTempImageUri() {
        if (!getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return null;
        final File extCacheDir = getExternalCacheDir();
        final File file;
        try {
            file = File.createTempFile("temp_image_", ".tmp", extCacheDir);
        } catch (final IOException e) {
            return null;
        }
        return Uri.fromFile(file);
    }

    private boolean takePhotoFallback(Uri uri) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        try {
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (final ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    private static class CopyImageTask extends AsyncTask<Object, Object, SingleResponse<File>> {
        private static final String TAG_COPYING_IMAGE = "copying_image";
        private final ImagePickerActivity mActivity;
        private final Uri mUri;
        private final boolean mNeedsCrop;
        private final boolean mDeleteSource;

        public CopyImageTask(final ImagePickerActivity activity, final Uri uri, final boolean needsCrop, final boolean deleteSource) {
            mActivity = activity;
            mUri = uri;
            mNeedsCrop = needsCrop;
            mDeleteSource = deleteSource;
        }

        @Override
        protected SingleResponse<File> doInBackground(final Object... params) {
            final ContentResolver cr = mActivity.getContentResolver();
            InputStream is = null;
            OutputStream os = null;
            try {
                final File cacheDir = mActivity.getCacheDir();
                final Uri uri = this.mUri;
                final String mimeType;
                final String scheme = uri.getScheme();
                if (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme)) {
                    final NetworkStreamDownloader downloader = new RestFuNetworkStreamDownloader(mActivity);
                    final NetworkStreamDownloader.DownloadResult result = downloader.get(uri);
                    is = result.stream;
                    mimeType = result.mimeType;
                } else if (SCHEME_DATA.equals(scheme)) {
                    final DataUri dataUri = DataUri.parse(uri.toString(), Charset.defaultCharset());
                    is = new ByteArrayInputStream(dataUri.getData());
                    mimeType = dataUri.getMime();
                } else {
                    is = cr.openInputStream(uri);
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(cr.openInputStream(uri), null, opts);
                    mimeType = opts.outMimeType;
                }
                final String suffix = mimeType != null ? "."
                        + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) : null;
                final File outFile = File.createTempFile("temp_image_", suffix, cacheDir);
                os = new FileOutputStream(outFile);
                Utils.copyStream(is, os);
                if (mDeleteSource && SCHEME_FILE.equals(scheme)) {
                    final File sourceFile = new File(mUri.getPath());
                    sourceFile.delete();
                }
                return SingleResponse.getInstance(outFile);
            } catch (final IOException e) {
                return SingleResponse.getInstance(e);
            } finally {
                Utils.closeSilently(os);
                Utils.closeSilently(is);
            }
        }

        @Override
        protected void onPreExecute() {
            final ProgressDialogFragment f = ProgressDialogFragment.show(mActivity, TAG_COPYING_IMAGE);
            f.setCancelable(false);
        }

        @Override
        protected void onPostExecute(final SingleResponse<File> result) {
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(TAG_COPYING_IMAGE);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            if (result.hasData()) {
                final Uri dstUri = Uri.fromFile(result.getData());
                final Intent callingIntent = mActivity.getIntent();
                if (mNeedsCrop && ((callingIntent.hasExtra(EXTRA_ASPECT_X) && callingIntent.hasExtra(EXTRA_ASPECT_Y))
                        || (callingIntent.hasExtra(EXTRA_MAX_WIDTH) && callingIntent.hasExtra(EXTRA_MAX_HEIGHT)))) {
                    final Uri tempImageUri = mActivity.createTempImageUri();
                    final Crop crop = Crop.of(dstUri, tempImageUri);
                    final int aspectX = callingIntent.getIntExtra(EXTRA_ASPECT_X, -1);
                    final int aspectY = callingIntent.getIntExtra(EXTRA_ASPECT_Y, -1);
                    if (aspectX > 0 && aspectY > 0) {
                        crop.withAspect(aspectX, aspectY);
                    }
                    final int maxWidth = callingIntent.getIntExtra(EXTRA_MAX_WIDTH, -1);
                    final int maxHeight = callingIntent.getIntExtra(EXTRA_MAX_HEIGHT, -1);
                    if (maxWidth > 0 && maxHeight > 0) {
                        crop.withMaxSize(maxWidth, maxHeight);
                    }
                    final Intent cropIntent = crop.getIntent(mActivity);
                    cropIntent.setClass(mActivity, ImageCropperActivity.class);
                    mActivity.mTempPhotoUri = tempImageUri;
                    mActivity.startActivityForResult(cropIntent, REQUEST_CROP);
                    return;
                }
                final Intent data = new Intent();
                data.setData(dstUri);
                mActivity.setResult(RESULT_OK, data);
            } else if (result.hasException()) {
                Log.w(LOGTAG, result.getException());
            }
            mActivity.finish();
        }
    }

    public static class ImageSourceDialogFragment extends BaseSupportDialogFragment implements OnClickListener {

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

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.entries_image_sources, this);
            return builder.create();
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            super.onCancel(dialog);
            final FragmentActivity a = getActivity();
            if (a != null) {
                a.finish();
            }
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);
        }
    }

    public static abstract class NetworkStreamDownloader {

        private final Context mContext;

        protected NetworkStreamDownloader(Context context) {
            mContext = context;
        }

        public final Context getContext() {
            return mContext;
        }

        public abstract DownloadResult get(Uri uri) throws IOException;

        public static final class DownloadResult {

            private final InputStream stream;
            private final String mimeType;

            public DownloadResult(final InputStream stream, final String mimeType) {
                this.stream = stream;
                this.mimeType = mimeType;
            }

            public static DownloadResult get(InputStream stream, String mimeType) {
                return new DownloadResult(stream, mimeType);
            }

        }
    }

}
