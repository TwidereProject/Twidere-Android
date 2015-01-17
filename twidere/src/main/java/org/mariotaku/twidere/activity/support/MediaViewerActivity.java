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

package org.mariotaku.twidere.activity.support;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.ProgressBar;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable.OnInitializeListener;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.support.TileImageLoader;
import org.mariotaku.twidere.util.SaveImageTask;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.File;

public final class MediaViewerActivity extends BaseSupportActivity implements Constants,
        TileImageLoader.DownloadListener, LoaderManager.LoaderCallbacks<TileImageLoader.Result>,
        OnMenuVisibilityListener {


    private ActionBar mActionBar;

    private ProgressBar mProgress;
    private TouchImageView mImageView;

    private long mContentLength;

    private File mImageFile;
    private boolean mLoaderInitialized;

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getViewerThemeResource(this);
    }


    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mImageView = (TouchImageView) findViewById(R.id.image_viewer);
        mProgress = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public Loader<TileImageLoader.Result> onCreateLoader(final int id, final Bundle args) {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setIndeterminate(true);
        invalidateOptionsMenu();
        final Uri uri = args.getParcelable(EXTRA_URI);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        return new TileImageLoader(this, this, accountId, uri);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_viewer_action_bar, menu);
        return true;
    }


    @Override
    public void onDownloadError(final Throwable t) {
        mContentLength = 0;
    }

    @Override
    public void onDownloadFinished() {
        mContentLength = 0;
    }

    @Override
    public void onDownloadStart(final long total) {
        mContentLength = total;
        mProgress.setIndeterminate(total <= 0);
        mProgress.setMax(total > 0 ? (int) (total / 1024) : 0);
    }


    @Override
    public void onLoaderReset(final Loader<TileImageLoader.Result> loader) {

    }

    @Override
    public void onLoadFinished(final Loader<TileImageLoader.Result> loader, final TileImageLoader.Result data) {
        if (data.hasData()) {
            mImageView.setVisibility(View.VISIBLE);
//            mImageView.setBitmapRegionDecoder(data.decoder, data.bitmap);
//            mImageView.setScale(1);
            if (data.useDecoder) {
                TileBitmapDrawable.attachTileBitmapDrawable(mImageView, data.file.getAbsolutePath(), null, new OnInitializeListener() {
                    @Override
                    public void onStartInitialization() {

                    }

                    @Override
                    public void onEndInitialization() {

                    }
                });
            } else {
                mImageView.setImageBitmap(data.bitmap);
            }
            mImageFile = data.file;
        } else {
            mImageView.setVisibility(View.GONE);
            mImageFile = null;
            Utils.showErrorMessage(this, null, data.exception, true);
        }
        mProgress.setVisibility(View.GONE);
        mProgress.setProgress(0);
        invalidateOptionsMenu();
        updateShareIntent();
    }

//    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE: {
                if (mImageFile != null) {
                    new SaveImageTask(this, mImageFile).execute();
                }
                break;
            }
            case MENU_OPEN_IN_BROWSER: {
                final Intent intent = getIntent();
                intent.setExtrasClassLoader(getClassLoader());
                final Uri uri = intent.getData();
                final Uri orig = intent.getParcelableExtra(EXTRA_URI_ORIG);
                final Uri uriPreferred = orig != null ? orig : uri;
                if (uriPreferred == null) return false;
                final String scheme = uriPreferred.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    final Intent open_intent = new Intent(Intent.ACTION_VIEW, uriPreferred);
                    open_intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    try {
                        startActivity(open_intent);
                    } catch (final ActivityNotFoundException e) {
                        // Ignore.
                    }
                }
                break;
            }
            default: {
                final Intent intent = item.getIntent();
                if (intent != null) {
                    try {
                        startActivity(intent);
                    } catch (final ActivityNotFoundException e) {
                        // Ignore.
                    }
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void onMenuVisibilityChanged(final boolean isVisible) {
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                onBackPressed();
                break;
            }
            case MENU_REFRESH: {
                loadImage();
                break;
            }
        }
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final LoaderManager lm = getSupportLoaderManager();
        Utils.setMenuItemAvailability(menu, MENU_REFRESH, !lm.hasRunningLoaders());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onProgressUpdate(final long downloaded) {
        if (mContentLength == 0) {
            mProgress.setIndeterminate(true);
            return;
        }
        mProgress.setIndeterminate(false);
        mProgress.setProgress((int) (downloaded / 1024));
    }


    public void showProgress() {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setIndeterminate(true);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.addOnMenuVisibilityListener(this);
        if (savedInstanceState == null) {
            loadImage();
        }

//        mImageView.setScaleToFit(false);
        mImageView.setMaxScale(2);


    }


    @Override
    protected void onDestroy() {
        mActionBar.removeOnMenuVisibilityListener(this);
        super.onDestroy();

    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
        loadImage();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    private void loadImage() {
        getSupportLoaderManager().destroyLoader(0);
        final Intent intent = getIntent();
        final Uri uri = intent.getData();
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        if (uri == null) {
            finish();
            return;
        }
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
        if (!mLoaderInitialized) {
            getSupportLoaderManager().initLoader(0, args, this);
            mLoaderInitialized = true;
        } else {
            getSupportLoaderManager().restartLoader(0, args, this);
        }
    }


    void updateShareIntent() {
//        final MenuItem item = mMenuBar.getMenu().findItem(MENU_SHARE);
//        if (item == null || !item.hasSubMenu()) return;
//        final SubMenu subMenu = item.getSubMenu();
//        subMenu.clear();
//        final Intent intent = getIntent();
//        final Uri uri = intent.getData();
//        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        if (mImageFile != null && mImageFile.exists()) {
//            shareIntent.setType("image/*");
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mImageFile));
//        } else {
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
//        }
//        Utils.addIntentToMenu(this, subMenu, shareIntent);
    }

}
