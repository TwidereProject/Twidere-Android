/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.commonsware.cwac.layouts.AspectLockedFrameLayout;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.sprylab.android.widget.TextureVideoView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment;
import org.mariotaku.mediaviewer.library.FileCache;
import org.mariotaku.mediaviewer.library.IMediaViewerActivity;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.mediaviewer.library.MediaViewerFragment;
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IExtendedActivity;
import org.mariotaku.twidere.fragment.SupportProgressDialogFragment;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.CacheProvider;
import org.mariotaku.twidere.provider.ShareProvider;
import org.mariotaku.twidere.task.SaveFileTask;
import org.mariotaku.twidere.task.SaveMediaToGalleryTask;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.PermissionUtils;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.media.MediaExtra;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.MediaDownloadEvent;
import pl.droidsonroids.gif.GifTextureView;
import pl.droidsonroids.gif.InputSource;

public final class MediaViewerActivity extends BaseActivity implements Constants, IExtendedActivity,
        ATEToolbarCustomizer, IMediaViewerActivity {

    private static final int REQUEST_SHARE_MEDIA = 201;
    private static final int REQUEST_PERMISSION_SAVE_MEDIA = 202;
    private static final int REQUEST_PERMISSION_SHARE_MEDIA = 203;

    @Inject
    FileCache mFileCache;
    @Inject
    MediaDownloader mMediaDownloader;

    private ParcelableMedia[] mMedia;
    private int mSaveToStoragePosition = -1;
    private int mShareMediaPosition = -1;


    private Helper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneralComponentHelper.build(this).inject(this);
        mHelper = new Helper(this);
        mHelper.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SHARE_MEDIA: {
                ShareProvider.clearTempFiles(this);
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mHelper.onContentChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_media_viewer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final int currentItem = viewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= adapter.getCount()) return false;
        final Object object = adapter.instantiateItem(viewPager, currentItem);
        if (!(object instanceof MediaViewerFragment)) return false;
        if (object instanceof CacheDownloadMediaViewerFragment) {
            CacheDownloadMediaViewerFragment f = (CacheDownloadMediaViewerFragment) object;
            final boolean running = f.getLoaderManager().hasRunningLoaders();
            final boolean downloaded = f.hasDownloadedData();
            MenuUtils.setMenuItemAvailability(menu, R.id.refresh, !running && !downloaded);
            MenuUtils.setMenuItemAvailability(menu, R.id.share, !running && downloaded);
            MenuUtils.setMenuItemAvailability(menu, R.id.save, !running && downloaded);
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.refresh, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.share, true);
            MenuUtils.setMenuItemAvailability(menu, R.id.save, false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final int currentItem = viewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= adapter.getCount()) return false;
        final Object object = adapter.instantiateItem(viewPager, currentItem);
        if (!(object instanceof MediaViewerFragment)) return false;
        switch (item.getItemId()) {
            case R.id.refresh: {
                if (object instanceof CacheDownloadMediaViewerFragment) {
                    final CacheDownloadMediaViewerFragment fragment = (CacheDownloadMediaViewerFragment) object;
                    fragment.startLoading(true);
                    fragment.showProgress(true, 0);
                    fragment.setMediaViewVisible(false);
                }
                return true;
            }
            case R.id.share: {
                if (object instanceof CacheDownloadMediaViewerFragment) {
                    requestAndShareMedia(currentItem);
                } else {
                    final ParcelableMedia media = getMedia()[currentItem];
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, media.url);
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                }
                return true;
            }
            case R.id.save: {
                requestAndSaveToStorage(currentItem);
                return true;
            }
            case R.id.open_in_browser: {
                final ParcelableMedia media = getMedia()[currentItem];
                try {
                    final Uri uri = Uri.parse(media.url);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setPackage(IntentUtils.getDefaultBrowserPackage(this, uri));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // TODO show error, or improve app url
                }
                return true;
            }
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_SAVE_MEDIA: {
                if (PermissionUtils.hasPermission(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    saveToStorage();
                } else {
                    Toast.makeText(this, R.string.save_media_no_storage_permission_message, Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_PERMISSION_SHARE_MEDIA: {
                if (!PermissionUtils.hasPermission(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.share_media_no_storage_permission_message, Toast.LENGTH_LONG).show();
                }
                shareMedia();
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void toggleBar() {
        setBarVisibility(!isBarShowing());
    }

    @Override
    @Nullable
    public String getATEKey() {
        return VALUE_THEME_NAME_DARK;
    }

    @Override
    public int getInitialPosition() {
        return ArrayUtils.indexOf(getMedia(), getInitialMedia());
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_media_viewer;
    }

    @Override
    @NonNull
    public ViewPager findViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        if (viewPager == null) throw new NullPointerException();
        return viewPager;
    }

    @Override
    public boolean isBarShowing() {
        final ActionBar actionBar = getSupportActionBar();
        return actionBar != null && actionBar.isShowing();
    }

    @Override
    public void setBarVisibility(boolean visible) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (visible) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    @Override
    public MediaDownloader getDownloader() {
        return mMediaDownloader;
    }

    @Override
    public FileCache getFileCache() {
        return mFileCache;
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public MediaViewerFragment instantiateMediaFragment(int position) {
        final ParcelableMedia media = getMedia()[position];
        final Bundle args = new Bundle();
        final Intent intent = getIntent();
        args.putParcelable(EXTRA_ACCOUNT_KEY, intent.getParcelableExtra(EXTRA_ACCOUNT_KEY));
        args.putParcelable(EXTRA_MEDIA, media);
        args.putParcelable(EXTRA_STATUS, intent.getParcelableExtra(EXTRA_STATUS));
        switch (media.type) {
            case ParcelableMedia.Type.IMAGE: {
                if (media.media_url == null) {
                    return (MediaViewerFragment) Fragment.instantiate(this,
                            ExternalBrowserPageFragment.class.getName(), args);
                }
                args.putParcelable(ImagePageFragment.EXTRA_MEDIA_URI, Uri.parse(media.media_url));
                if (media.media_url.endsWith(".gif")) {
                    return (MediaViewerFragment) Fragment.instantiate(this,
                            GifPageFragment.class.getName(), args);
                } else {
                    return (MediaViewerFragment) Fragment.instantiate(this,
                            ImagePageFragment.class.getName(), args);
                }
            }
            case ParcelableMedia.Type.ANIMATED_GIF:
            case ParcelableMedia.Type.CARD_ANIMATED_GIF: {
                args.putBoolean(VideoPageFragment.EXTRA_LOOP, true);
                return (MediaViewerFragment) Fragment.instantiate(this,
                        VideoPageFragment.class.getName(), args);
            }
            case ParcelableMedia.Type.VIDEO: {
                return (MediaViewerFragment) Fragment.instantiate(this,
                        VideoPageFragment.class.getName(), args);
            }
            case ParcelableMedia.Type.EXTERNAL_PLAYER: {
                return (MediaViewerFragment) Fragment.instantiate(this,
                        ExternalBrowserPageFragment.class.getName(), args);
            }
        }
        throw new UnsupportedOperationException(String.valueOf(media));
    }

    @Override
    public int getMediaCount() {
        return getMedia().length;
    }


    @Override
    public int getLightToolbarMode(@Nullable Toolbar toolbar) {
        return Config.LIGHT_TOOLBAR_OFF;
    }

    @Override
    public int getToolbarColor(@Nullable Toolbar toolbar) {
        return 0;
    }

    public boolean hasStatus() {
        return getIntent().hasExtra(EXTRA_STATUS);
    }

    private ParcelableStatus getStatus() {
        return getIntent().getParcelableExtra(EXTRA_STATUS);
    }

    private ParcelableMedia getInitialMedia() {
        return getIntent().getParcelableExtra(EXTRA_CURRENT_MEDIA);
    }

    private ParcelableMedia[] getMedia() {
        if (mMedia != null) return mMedia;
        return mMedia = Utils.newParcelableArray(getIntent().getParcelableArrayExtra(EXTRA_MEDIA),
                ParcelableMedia.CREATOR);
    }

    protected void processShareIntent(Intent intent) {
        if (!hasStatus()) return;
        final ParcelableStatus status = getStatus();
        intent.putExtra(Intent.EXTRA_SUBJECT, IntentUtils.getStatusShareSubject(this, status));
        intent.putExtra(Intent.EXTRA_TEXT, IntentUtils.getStatusShareText(this, status));
    }

    protected void requestAndSaveToStorage(int position) {
        mSaveToStoragePosition = position;
        if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            saveToStorage();
        } else {
            final String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_SAVE_MEDIA);
        }
    }

    protected void requestAndShareMedia(int position) {
        mShareMediaPosition = position;
        if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            shareMedia();
        } else {
            final String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_SHARE_MEDIA);
        }
    }

    protected void shareMedia() {
        if (mShareMediaPosition == -1) return;
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final Object object = adapter.instantiateItem(viewPager, mShareMediaPosition);
        if (!(object instanceof CacheDownloadMediaViewerFragment)) return;
        CacheDownloadLoader.Result result = ((CacheDownloadMediaViewerFragment) object).getDownloadResult();
        if (result == null || result.cacheUri == null) {
            // TODO show error
            return;
        }
        final File destination = ShareProvider.getFilesDir(this);
        if (destination == null) return;
        String type;
        if (object instanceof VideoPageFragment) {
            type = CacheProvider.Type.VIDEO;
        } else if (object instanceof ImagePageFragment) {
            type = CacheProvider.Type.IMAGE;
        } else if (object instanceof GifPageFragment) {
            type = CacheProvider.Type.IMAGE;
        } else {
            throw new UnsupportedOperationException("Unsupported fragment " + object);
        }
        final SaveFileTask task = new SaveFileTask(this, result.cacheUri, destination,
                new CacheProvider.CacheFileTypeCallback(this, type)) {
            private static final String PROGRESS_FRAGMENT_TAG = "progress";

            @Override
            protected void dismissProgress() {
                final IExtendedActivity activity = (IExtendedActivity) getContext();
                if (activity == null) return;
                activity.executeAfterFragmentResumed(new IExtendedActivity.Action() {
                    @Override
                    public void execute(IExtendedActivity activity) {
                        final FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
                        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
                        if (fragment != null) {
                            fragment.dismiss();
                        }
                    }
                });
            }

            @Override
            protected void showProgress() {
                final IExtendedActivity activity = (IExtendedActivity) getContext();
                if (activity == null) return;
                activity.executeAfterFragmentResumed(new IExtendedActivity.Action() {
                    @Override
                    public void execute(IExtendedActivity activity) {
                        final DialogFragment fragment = new SupportProgressDialogFragment();
                        fragment.setCancelable(false);
                        fragment.show(((FragmentActivity) activity).getSupportFragmentManager(), PROGRESS_FRAGMENT_TAG);
                    }
                });
            }

            @Override
            protected void onFileSaved(@NonNull File savedFile, @Nullable String mimeType) {
                final MediaViewerActivity activity = (MediaViewerActivity) getContext();
                if (activity == null) return;

                final Uri fileUri = ShareProvider.getUriForFile(activity, AUTHORITY_TWIDERE_SHARE,
                        savedFile);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setDataAndType(fileUri, mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                }
                activity.processShareIntent(intent);
                startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.share)),
                        REQUEST_SHARE_MEDIA);
            }

            @Override
            protected void onFileSaveFailed() {
                final MediaViewerActivity activity = (MediaViewerActivity) getContext();
                if (activity == null) return;
                Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        };
        AsyncTaskUtils.executeTask(task);
    }

    protected void saveToStorage() {
        if (mSaveToStoragePosition == -1) return;
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final Object object = adapter.instantiateItem(viewPager, mSaveToStoragePosition);
        if (!(object instanceof CacheDownloadMediaViewerFragment)) return;
        final CacheDownloadMediaViewerFragment f = (CacheDownloadMediaViewerFragment) object;
        final CacheDownloadLoader.Result result = f.getDownloadResult();
        if (result == null) return;
        final Uri cacheUri = result.cacheUri;
        final boolean hasMedia = cacheUri != null;
        if (!hasMedia) return;
        SaveFileTask task;
        if (f instanceof ImagePageFragment) {
            task = SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.IMAGE);
        } else if (f instanceof VideoPageFragment) {
            task = SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.VIDEO);
        } else if (f instanceof GifPageFragment) {
            task = SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.IMAGE);
        } else {
            throw new UnsupportedOperationException();
        }
        AsyncTaskUtils.executeTask(task);
    }

    public static class ImagePageFragment extends SubsampleImageViewerFragment {
        private int mMediaLoadState;
        private MediaDownloadEvent mMediaDownloadEvent;
        private CacheDownloadLoader.ResultCreator mResultCreator;

        static Bitmap decodeBitmap(ContentResolver cr, Uri uri, BitmapFactory.Options o) throws IOException {
            InputStream is = null;
            try {
                is = cr.openInputStream(uri);
                return BitmapFactory.decodeStream(is, null, o);
            } finally {
                Utils.closeSilently(is);
            }
        }

        static Uri replaceTwitterMediaUri(Uri downloadUri) {
//            String uriString = downloadUri.toString();
//            if (TwitterMediaProvider.isSupported(uriString)) {
//                final String suffix = ".jpg";
//                int lastIndexOfJpegSuffix = uriString.lastIndexOf(suffix);
//                if (lastIndexOfJpegSuffix == -1) return downloadUri;
//                final int endOfSuffix = lastIndexOfJpegSuffix + suffix.length();
//                if (endOfSuffix == uriString.length()) {
//                    return Uri.parse(uriString.substring(0, lastIndexOfJpegSuffix) + ".png");
//                } else {
//                    // Seems :orig suffix won't work jpegs -> pngs
//                    String sizeSuffix = uriString.substring(endOfSuffix);
//                    if (":orig".equals(sizeSuffix)) {
//                        sizeSuffix = ":large";
//                    }
//                    return Uri.parse(uriString.substring(0, lastIndexOfJpegSuffix) + ".png" +
//                            sizeSuffix);
//                }
//            }
            return downloadUri;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                getActivity().supportInvalidateOptionsMenu();
            }
        }

        @Override
        protected Object getDownloadExtra() {
            final MediaExtra mediaExtra = new MediaExtra();
            mediaExtra.setAccountKey(getAccountKey());
            final Uri origDownloadUri = super.getDownloadUri();
            final Uri downloadUri = getDownloadUri();
            if (origDownloadUri != null && downloadUri != null) {
                final String fallbackUrl = origDownloadUri.toString();
                mediaExtra.setFallbackUrl(fallbackUrl);
                mediaExtra.setSkipUrlReplacing(!fallbackUrl.equals(downloadUri.toString()));
            }
            return mediaExtra;
        }

        @Nullable
        @Override
        protected Uri getDownloadUri() {
            final Uri downloadUri = super.getDownloadUri();
            if (downloadUri == null) return null;
            return replaceTwitterMediaUri(downloadUri);
        }

        @Override
        public boolean hasDownloadedData() {
            return super.hasDownloadedData() && mMediaLoadState != State.ERROR;
        }

        @Override
        protected void onMediaLoadStateChange(@State int state) {
            mMediaLoadState = state;
            final FragmentActivity activity = getActivity();
            if (getUserVisibleHint() && activity != null) {
                activity.supportInvalidateOptionsMenu();
            }
        }

        @Override
        protected void setupImageView(SubsamplingScaleImageView imageView) {
            imageView.setMaxScale(getResources().getDisplayMetrics().density);
            imageView.setBitmapDecoderClass(PreviewBitmapDecoder.class);
        }

        @NonNull
        @Override
        protected ImageSource getImageSource(@NonNull CacheDownloadLoader.Result data) {
            assert data.cacheUri != null;
            if (data instanceof SizedResult) {
                final ImageSource uri = ImageSource.uri(data.cacheUri);
                uri.dimensions(((SizedResult) data).getWidth(), ((SizedResult) data).getHeight());
                return uri;
            }
            return super.getImageSource(data);
        }

        @Override
        protected ImageSource getPreviewImageSource(@NonNull CacheDownloadLoader.Result data) {
            if (!(data instanceof SizedResult)) return null;
            assert data.cacheUri != null;
            return ImageSource.uri(data.cacheUri);
        }

        @Nullable
        @Override
        protected CacheDownloadLoader.ResultCreator getResultCreator() {
            if (mResultCreator != null) return mResultCreator;
            return mResultCreator = new SizedResultCreator(getContext());
        }

        private ParcelableMedia getMedia() {
            return getArguments().getParcelable(EXTRA_MEDIA);
        }

        private UserKey getAccountKey() {
            return getArguments().getParcelable(EXTRA_ACCOUNT_KEY);
        }

        @Override
        public void onDownloadRequested(long nonce) {
            super.onDownloadRequested(nonce);
            final Context context = getContext();
            if (context != null) {
                mMediaDownloadEvent = MediaDownloadEvent.create(context, getMedia(), nonce);
            } else {
                mMediaDownloadEvent = null;
            }
        }

        @Override
        public void onDownloadStart(long total, long nonce) {
            super.onDownloadStart(total, nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.setOpenedTime(System.currentTimeMillis());
                mMediaDownloadEvent.setSize(nonce);
            }
        }

        @Override
        public void onDownloadFinished(long nonce) {
            super.onDownloadFinished(nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.markEnd();
                HotMobiLogger.getInstance(getContext()).log(getAccountKey(), mMediaDownloadEvent);
                mMediaDownloadEvent = null;
            }
        }

        static class SizedResult extends CacheDownloadLoader.Result {

            private final int width, height;

            public SizedResult(@NonNull Uri cacheUri, int width, int height) {
                super(cacheUri, null);
                this.width = width;
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }
        }

        static class SizedResultCreator implements CacheDownloadLoader.ResultCreator {

            private final Context context;

            SizedResultCreator(Context context) {
                this.context = context;
            }

            @Override
            public CacheDownloadLoader.Result create(Uri uri) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                try {
                    decodeBitmap(context.getContentResolver(), uri, o);
                } catch (IOException e) {
                    return CacheDownloadLoader.Result.getInstance(uri);
                }
                if (o.outWidth > 0 && o.outHeight > 0) {
                    return new SizedResult(uri, o.outWidth, o.outHeight);
                }
                return CacheDownloadLoader.Result.getInstance(uri);
            }

        }

        public static class PreviewBitmapDecoder implements ImageDecoder {
            @Override
            public Bitmap decode(Context context, Uri uri) throws Exception {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                o.inPreferredConfig = Bitmap.Config.RGB_565;
                final ContentResolver cr = context.getContentResolver();
                decodeBitmap(cr, uri, o);
                final DisplayMetrics dm = context.getResources().getDisplayMetrics();
                final int targetSize = Math.min(1024, Math.max(dm.widthPixels, dm.heightPixels));
                o.inSampleSize = TwidereMathUtils.nextPowerOf2(Math.max(1, Math.min(o.outHeight, o.outWidth) / targetSize));
                o.inJustDecodeBounds = false;
                final Bitmap bitmap = decodeBitmap(cr, uri, o);
                if (bitmap == null) throw new IOException();
                return bitmap;
            }

        }
    }

    public static class GifPageFragment extends CacheDownloadMediaViewerFragment {

        private GifTextureView mGifView;
        private MediaDownloadEvent mMediaDownloadEvent;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            startLoading(false);
        }

        @Nullable
        @Override
        protected Uri getDownloadUri() {
            return getArguments().getParcelable(ImagePageFragment.EXTRA_MEDIA_URI);
        }

        @Nullable
        @Override
        protected Object getDownloadExtra() {
            return null;
        }

        @Override
        protected void displayMedia(CacheDownloadLoader.Result result) {
            final Context context = getContext();
            if (context == null) return;
            if (result.cacheUri != null) {
                mGifView.setInputSource(new InputSource.UriSource(context.getContentResolver(), result.cacheUri));
            } else {
                mGifView.setInputSource(null);
            }
        }

        @Override
        protected boolean isAbleToLoad() {
            return getDownloadUri() != null;
        }

        @Override
        protected View onCreateMediaView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_media_viewer_gif, parent, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mGifView = (GifTextureView) view.findViewById(R.id.gif_view);
            mGifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MediaViewerActivity) getActivity()).toggleBar();
                }
            });
        }

        @Override
        protected void recycleMedia() {
            mGifView.setInputSource(null);
        }


        private ParcelableMedia getMedia() {
            return getArguments().getParcelable(EXTRA_MEDIA);
        }

        private UserKey getAccountKey() {
            return getArguments().getParcelable(EXTRA_ACCOUNT_KEY);
        }

        @Override
        public void onDownloadRequested(long nonce) {
            super.onDownloadRequested(nonce);
            final Context context = getContext();
            if (context != null) {
                mMediaDownloadEvent = MediaDownloadEvent.create(context, getMedia(), nonce);
            } else {
                mMediaDownloadEvent = null;
            }
        }

        @Override
        public void onDownloadStart(long total, long nonce) {
            super.onDownloadStart(total, nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.setOpenedTime(System.currentTimeMillis());
                mMediaDownloadEvent.setSize(nonce);
            }
        }

        @Override
        public void onDownloadFinished(long nonce) {
            super.onDownloadFinished(nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.markEnd();
                HotMobiLogger.getInstance(getContext()).log(getAccountKey(), mMediaDownloadEvent);
                mMediaDownloadEvent = null;
            }
        }
    }

    public static class VideoPageFragment extends CacheDownloadMediaViewerFragment
            implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener, View.OnClickListener {

        private static final String EXTRA_LOOP = "loop";
        private static final String[] SUPPORTED_VIDEO_TYPES;
        private static final String[] FALLBACK_VIDEO_TYPES;

        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                SUPPORTED_VIDEO_TYPES = new String[]{"video/mp4"};
            } else {
                SUPPORTED_VIDEO_TYPES = new String[]{"video/webm", "video/mp4"};
            }
            FALLBACK_VIDEO_TYPES = new String[]{"video/mp4"};
        }

        private TextureVideoView mVideoView;
        private View mVideoViewOverlay;
        private ProgressBar mVideoViewProgress;
        private TextView mDurationLabel, mPositionLabel;
        private ImageButton mPlayPauseButton, mVolumeButton;
        private View mVideoControl;
        private boolean mPlayAudio;
        private VideoPlayProgressRunnable mVideoProgressRunnable;
        private MediaPlayer mMediaPlayer;
        private int mMediaPlayerError;
        private MediaDownloadEvent mMediaDownloadEvent;

        @Override
        protected Object getDownloadExtra() {
            final MediaExtra extra = new MediaExtra();
            extra.setUseThumbor(false);
            final Pair<String, String> fallbackUrlAndType = getBestVideoUrlAndType(getMedia(), FALLBACK_VIDEO_TYPES);
            if (fallbackUrlAndType != null) {
                extra.setFallbackUrl(fallbackUrlAndType.first);
            }
            return extra;
        }

        public boolean isLoopEnabled() {
            return getArguments().getBoolean(EXTRA_LOOP, false);
        }

        @Override
        protected boolean isAbleToLoad() {
            return getDownloadUri() != null;
        }

        @Nullable
        @Override
        protected Uri getDownloadUri() {
            final Pair<String, String> bestVideoUrlAndType = getBestVideoUrlAndType(getMedia(),
                    SUPPORTED_VIDEO_TYPES);
            if (bestVideoUrlAndType != null && bestVideoUrlAndType.first != null) {
                return Uri.parse(bestVideoUrlAndType.first);
            }
            return getArguments().getParcelable(ImagePageFragment.EXTRA_MEDIA_URI);
        }


        @Override
        protected void displayMedia(CacheDownloadLoader.Result result) {
            mVideoView.setVideoURI(result.cacheUri);
            setMediaViewVisible(true);
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.supportInvalidateOptionsMenu();
            }
        }

        @Override
        protected void recycleMedia() {

        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            updatePlayerState();
//            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
//            mVideoViewProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mMediaPlayer = null;
            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
            mVideoViewProgress.setVisibility(View.GONE);
            mMediaPlayerError = what;
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (getUserVisibleHint()) {
                mMediaPlayer = mp;
                mMediaPlayerError = 0;
                mp.setScreenOnWhilePlaying(true);
                updateVolume();
                mp.setLooping(isLoopEnabled());
                mp.start();
                mVideoViewProgress.setVisibility(View.VISIBLE);
                mVideoViewProgress.post(mVideoProgressRunnable);
                updatePlayerState();
                mVideoControl.setVisibility(View.VISIBLE);
            }
        }

        private void updateVolume() {
            final ImageButton b = mVolumeButton;
            if (b != null) {
                b.setImageResource(mPlayAudio ? R.drawable.ic_action_speaker_max : R.drawable.ic_action_speaker_muted);
            }
            final MediaPlayer mp = mMediaPlayer;
            if (mp == null) return;
            if (mPlayAudio) {
                mp.setVolume(1, 1);
            } else {
                mp.setVolume(0, 0);
            }
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mVideoView = (TextureVideoView) view.findViewById(R.id.video_view);
            mVideoViewOverlay = view.findViewById(R.id.video_view_overlay);
            mVideoViewProgress = (ProgressBar) view.findViewById(R.id.video_view_progress);
            mDurationLabel = (TextView) view.findViewById(R.id.duration_label);
            mPositionLabel = (TextView) view.findViewById(R.id.position_label);
            mPlayPauseButton = (ImageButton) view.findViewById(R.id.play_pause_button);
            mVolumeButton = (ImageButton) view.findViewById(R.id.volume_button);
            mVideoControl = view.findViewById(R.id.video_control);

        }


        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                getActivity().supportInvalidateOptionsMenu();
            } else if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.pause();
                updatePlayerState();
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);

            Handler handler = mVideoViewProgress.getHandler();
            if (handler == null) {
                handler = new Handler(getActivity().getMainLooper());
            }
            mVideoProgressRunnable = new VideoPlayProgressRunnable(handler, mVideoViewProgress,
                    mDurationLabel, mPositionLabel, mVideoView);


            mVideoViewOverlay.setOnClickListener(this);
            mVideoView.setOnPreparedListener(this);
            mVideoView.setOnErrorListener(this);
            mVideoView.setOnCompletionListener(this);

            mPlayPauseButton.setOnClickListener(this);
            mVolumeButton.setOnClickListener(this);
            startLoading(false);
            setMediaViewVisible(false);
            updateVolume();
        }

        @SuppressLint("SwitchIntDef")
        @Nullable
        private Pair<String, String> getBestVideoUrlAndType(@Nullable final ParcelableMedia media,
                                                            @NonNull final String[] supportedTypes) {
            if (media == null) return null;
            switch (media.type) {
                case ParcelableMedia.Type.VIDEO:
                case ParcelableMedia.Type.ANIMATED_GIF: {
                    if (media.video_info == null) {
                        return Pair.create(media.media_url, null);
                    }
                    for (String supportedType : supportedTypes) {
                        for (ParcelableMedia.VideoInfo.Variant variant : media.video_info.variants) {
                            if (supportedType.equalsIgnoreCase(variant.content_type))
                                return Pair.create(variant.url, variant.content_type);
                        }
                    }
                    return null;
                }
                case ParcelableMedia.Type.CARD_ANIMATED_GIF: {
                    return Pair.create(media.media_url, "video/mp4");
                }
                default: {
                    return null;
                }
            }
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.volume_button: {
                    mPlayAudio = !mPlayAudio;
                    updateVolume();
                    break;
                }
                case R.id.play_pause_button: {
                    final MediaPlayer mp = mMediaPlayer;
                    if (mp != null) {
                        if (mp.isPlaying()) {
                            mp.pause();
                        } else {
                            mp.start();
                        }
                    }
                    updatePlayerState();
                    break;
                }
                case R.id.video_view_overlay: {
                    final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
                    if (mVideoControl.getVisibility() == View.VISIBLE) {
                        mVideoControl.setVisibility(View.GONE);
                        activity.setBarVisibility(false);
                    } else {
                        mVideoControl.setVisibility(View.VISIBLE);
                        activity.setBarVisibility(true);
                    }
                    break;
                }
            }
        }

        private void updatePlayerState() {
            final MediaPlayer mp = mMediaPlayer;
            if (mp != null) {
                final boolean playing = mp.isPlaying();
                mPlayPauseButton.setContentDescription(getString(playing ? R.string.pause : R.string.play));
                mPlayPauseButton.setImageResource(playing ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow);
            } else {
                mPlayPauseButton.setContentDescription(getString(R.string.play));
                mPlayPauseButton.setImageResource(R.drawable.ic_action_play_arrow);
            }
        }

        @Override
        public View onCreateMediaView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_media_viewer_texture_video_view, container, false);
        }


        @Override
        public void onDownloadRequested(long nonce) {
            super.onDownloadRequested(nonce);
            final Context context = getContext();
            if (context != null) {
                mMediaDownloadEvent = MediaDownloadEvent.create(context, getMedia(), nonce);
            } else {
                mMediaDownloadEvent = null;
            }
        }

        @Override
        public void onDownloadStart(long total, long nonce) {
            super.onDownloadStart(total, nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.setOpenedTime(System.currentTimeMillis());
                mMediaDownloadEvent.setSize(nonce);
            }
        }

        @Override
        public void onDownloadFinished(long nonce) {
            super.onDownloadFinished(nonce);
            if (mMediaDownloadEvent != null && mMediaDownloadEvent.getNonce() == nonce) {
                mMediaDownloadEvent.markEnd();
                HotMobiLogger.getInstance(getContext()).log(getAccountKey(), mMediaDownloadEvent);
                mMediaDownloadEvent = null;
            }
        }

        @Nullable
        private ParcelableMedia getMedia() {
            return getArguments().getParcelable(EXTRA_MEDIA);
        }

        private UserKey getAccountKey() {
            return getArguments().getParcelable(EXTRA_ACCOUNT_KEY);
        }

        private static class VideoPlayProgressRunnable implements Runnable {

            private final Handler mHandler;
            private final ProgressBar mProgressBar;
            private final TextView mDurationLabel, mPositionLabel;
            private final MediaController.MediaPlayerControl mMediaPlayerControl;

            VideoPlayProgressRunnable(Handler handler, ProgressBar progressBar, TextView durationLabel,
                                      TextView positionLabel, MediaController.MediaPlayerControl mediaPlayerControl) {
                mHandler = handler;
                mProgressBar = progressBar;
                mDurationLabel = durationLabel;
                mPositionLabel = positionLabel;
                mMediaPlayerControl = mediaPlayerControl;
                mProgressBar.setMax(1000);
            }

            @Override
            public void run() {
                final int duration = mMediaPlayerControl.getDuration();
                final int position = mMediaPlayerControl.getCurrentPosition();
                if (duration <= 0 || position < 0) return;
                mProgressBar.setProgress(Math.round(1000 * position / (float) duration));
                final long durationSecs = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS),
                        positionSecs = TimeUnit.SECONDS.convert(position, TimeUnit.MILLISECONDS);
                mDurationLabel.setText(String.format(Locale.ROOT, "%02d:%02d", durationSecs / 60, durationSecs % 60));
                mPositionLabel.setText(String.format(Locale.ROOT, "%02d:%02d", positionSecs / 60, positionSecs % 60));
                mHandler.postDelayed(this, 16);
            }
        }
    }

    public static class ExternalBrowserPageFragment extends MediaViewerFragment {
        private WebView mWebView;
        private AspectLockedFrameLayout mWebViewContainer;

        @Override
        protected View onCreateMediaView(LayoutInflater inflater, ViewGroup parent,
                                         Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_media_viewer_browser_fragment, parent, false);
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadsImagesAutomatically(true);
            final ParcelableMedia media = getArguments().getParcelable(EXTRA_MEDIA);
            if (media == null) throw new NullPointerException();
            mWebView.loadUrl(TextUtils.isEmpty(media.media_url) ? media.url : media.media_url);
            mWebViewContainer.setAspectRatioSource(new AspectLockedFrameLayout.AspectRatioSource() {
                @Override
                public int getWidth() {
                    return media.width;
                }

                @Override
                public int getHeight() {
                    return media.height;
                }
            });
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mWebViewContainer = ((AspectLockedFrameLayout) view.findViewById(R.id.webview_container));
            mWebView = (WebView) view.findViewById(R.id.webview);
        }


        @Override
        public void onResume() {
            super.onResume();
            mWebView.onResume();
        }


        @Override
        public void onPause() {
            mWebView.onPause();
            super.onPause();
        }

        @Override
        public void onDestroy() {
            mWebView.destroy();
            super.onDestroy();
        }

        @Override
        protected void recycleMedia() {

        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                getActivity().supportInvalidateOptionsMenu();
            }
        }
    }
}
