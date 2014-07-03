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

package org.mariotaku.gallery3d;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;

import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLRootView;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.TwidereSwipeBackActivity;
import org.mariotaku.twidere.util.SaveImageTask;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.File;

public final class ImageViewerGLActivity extends TwidereSwipeBackActivity implements Constants, PhotoView.Listener,
		GLImageLoader.DownloadListener, LoaderManager.LoaderCallbacks<GLImageLoader.Result>, OnMenuVisibilityListener,
		SwipeListener, OnMenuItemClickListener {

	private final GLView mRootPane = new GLView() {
		@Override
		protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
			mPhotoView.layout(0, 0, right - left, bottom - top);
		}
	};
	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;

	private static final int MSG_HIDE_BARS = 1;
	private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
	private static final int MSG_UPDATE_ACTION_BAR = 5;
	private static final int MSG_UNFREEZE_GLROOT = 6;
	private static final int MSG_WANT_BARS = 7;
	private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
	private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

	private ActionBar mActionBar;

	private GLView mContentPane;
	private GLRootView mGLRootView;
	private ProgressBar mProgress;
	private ImageView mImageViewer;
	private MenuBar mMenuBar;

	private PhotoView mPhotoView;

	private PhotoView.ITileImageAdapter mAdapter;
	private Handler mHandler;
	protected int mFlags;

	private boolean mShowBars = true;
	private boolean mActionBarAllowed = true;
	private boolean mLoaderInitialized;

	private long mContentLength;
	private ThreadPool mThreadPool;

	private File mImageFile;

	public GLRoot getGLRoot() {
		return mGLRootView;
	}

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getViewerThemeResource(this);
	}

	public ThreadPool getThreadPool() {
		if (mThreadPool != null) return mThreadPool;
		return mThreadPool = new ThreadPool();
	}

	public void hideProgress() {
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
	}

	@Override
	public void onActionBarAllowed(final boolean allowed) {
		mActionBarAllowed = allowed;
		mHandler.sendEmptyMessage(MSG_UPDATE_ACTION_BAR);
	}

	@Override
	public void onActionBarWanted() {
		mHandler.sendEmptyMessage(MSG_WANT_BARS);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
		mImageViewer = (ImageView) findViewById(R.id.image_viewer);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mMenuBar = (MenuBar) findViewById(R.id.menu_bar);
	}

	@Override
	public Loader<GLImageLoader.Result> onCreateLoader(final int id, final Bundle args) {
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
		invalidateOptionsMenu();
		final Uri uri = args.getParcelable(EXTRA_URI);
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		return new GLImageLoader(this, this, accountId, uri);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_image_viewer_action_bar, menu);
		return true;
	}

	@Override
	public void onCurrentImageUpdated() {
		mGLRootView.unfreeze();
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
	public void onEdgeTouch(final int edgeFlag) {
		showBars();
	}

	@Override
	public void onLoaderReset(final Loader<GLImageLoader.Result> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<GLImageLoader.Result> loader, final GLImageLoader.Result data) {
		if (data != null && (data.decoder != null || data.bitmap != null)) {
			if (data.decoder != null) {
				mGLRootView.setVisibility(View.VISIBLE);
				mImageViewer.setVisibility(View.GONE);
				mAdapter.setData(data.decoder, data.bitmap, data.orientation);
				mImageViewer.setImageBitmap(null);
			} else if (data.bitmap != null) {
				mGLRootView.setVisibility(View.GONE);
				mImageViewer.setVisibility(View.VISIBLE);
				mImageViewer.setImageBitmap(data.bitmap);
			}
			mImageFile = data.file;
		} else {
			mImageFile = null;
			if (data != null) {
				Utils.showErrorMessage(this, null, data.exception, true);
			}
		}
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
		invalidateOptionsMenu();
		updateShareIntent();
	}

	@Override
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
	public void onPictureCenter() {
		mPhotoView.setWantPictureCenterCallbacks(false);
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

	@Override
	public void onScrollOverThreshold() {

	}

	@Override
	public void onScrollStateChange(final int state, final float scrollPercent) {

	}

	@Override
	public void onSingleTapUp(final int x, final int y) {
		toggleBars();
	}

	public void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer_gl);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.addOnMenuVisibilityListener(this);
		mHandler = new MyHandler(this);
		mPhotoView = new PhotoView(this);
		mPhotoView.setListener(this);
		final int bgColor = ThemeUtils.getColorBackgroundCacheHint(this);
		final int r = Color.red(bgColor), g = Color.green(bgColor), b = Color.blue(bgColor);
		final float[] rootBg = { r / 255f, g / 255f, b / 255f, 1 };
		mRootPane.setBackgroundColor(rootBg);
		mRootPane.addComponent(mPhotoView);
		mAdapter = new PhotoViewAdapter(mPhotoView);
		mPhotoView.setModel(mAdapter);
		if (savedInstanceState == null) {
			loadImage();
		}
		mMenuBar.setOnMenuItemClickListener(this);
		mMenuBar.inflate(R.menu.menu_image_viewer);
		mMenuBar.setIsBottomBar(true);
		mMenuBar.show();
		setSwipeListener(this);
	}

	@Override
	protected void onDestroy() {
		mActionBar.removeOnMenuVisibilityListener(this);
		super.onDestroy();
		mGLRootView.lockRenderThread();
		try {
			// Remove all pending messages.
			mHandler.removeCallbacksAndMessages(null);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		setIntent(intent);
		loadImage();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLRootView.onPause();
		mGLRootView.lockRenderThread();
		try {
			mGLRootView.unfreeze();
			mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

			if (mAdapter != null) {
				mAdapter.recycleScreenNail();
			}
			mPhotoView.pause();
			mHandler.removeMessages(MSG_HIDE_BARS);
			mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
		} finally {
			mGLRootView.unlockRenderThread();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLRootView.lockRenderThread();
		try {
			if (mAdapter == null) {
				finish();
				return;
			}
			mGLRootView.freeze();
			setContentPane(mRootPane);

			mPhotoView.resume();
			if (!mShowBars) {
				hideBars();
			}
			mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
		} finally {
			mGLRootView.unlockRenderThread();
		}
		mGLRootView.onResume();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mGLRootView.lockRenderThread();
		try {
			super.onSaveInstanceState(outState);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	protected void setContentPane(final GLView content) {
		mContentPane = content;
		mContentPane.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(Color.BLACK));
		mGLRootView.setContentPane(mContentPane);
	}

	private boolean canShowBars() {
		// No bars if it's not allowed.
		if (!mActionBarAllowed) return false;
		return true;
	}

	private void hideBars() {
		if (!mShowBars || isSwiping()) return;
		mShowBars = false;
		mActionBar.hide();
		final TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
		anim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(final Animation animation) {
				mMenuBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(final Animation animation) {

			}

			@Override
			public void onAnimationStart(final Animation animation) {

			}
		});
		mMenuBar.startAnimation(anim);
		mHandler.removeMessages(MSG_HIDE_BARS);
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

	private void showBars() {
		if (mShowBars) return;
		mShowBars = true;
		mActionBar.show();
		mMenuBar.setVisibility(View.VISIBLE);
		final TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
		anim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
		mMenuBar.startAnimation(anim);
	}

	private void toggleBars() {
		if (mShowBars) {
			hideBars();
		} else {
			if (canShowBars()) {
				showBars();
			}
		}
	}

	private void updateBars() {
		if (!canShowBars()) {
			hideBars();
		}
	}

	private void wantBars() {
		if (canShowBars()) {
			showBars();
		}
	}

	void updateShareIntent() {
		final MenuItem item = mMenuBar.getMenu().findItem(MENU_SHARE);
		if (item == null || !item.hasSubMenu()) return;
		final SubMenu subMenu = item.getSubMenu();
		subMenu.clear();
		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		final Intent shareIntent = new Intent(Intent.ACTION_SEND);
		if (mImageFile != null && mImageFile.exists()) {
			shareIntent.setType("image/*");
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mImageFile));
		} else {
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
		}
		Utils.addIntentToMenu(this, subMenu, shareIntent);
	}

	private static class MyHandler extends SynchronizedHandler {
		ImageViewerGLActivity activity;

		private MyHandler(final ImageViewerGLActivity activity) {
			super(activity.getGLRoot());
			this.activity = activity;
		}

		@Override
		public void handleMessage(final Message message) {
			switch (message.what) {
				case MSG_HIDE_BARS: {
					activity.hideBars();
					break;
				}
				case MSG_REFRESH_BOTTOM_CONTROLS: {
					break;
				}
				case MSG_ON_FULL_SCREEN_CHANGED: {
					break;
				}
				case MSG_UPDATE_ACTION_BAR: {
					activity.updateBars();
					break;
				}
				case MSG_WANT_BARS: {
					activity.wantBars();
					break;
				}
				case MSG_UNFREEZE_GLROOT: {
					mGLRoot.unfreeze();
					break;
				}
			}
		}
	}

}
