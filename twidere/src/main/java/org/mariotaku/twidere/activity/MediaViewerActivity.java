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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.commonsware.cwac.layouts.AspectLockedFrameLayout;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sprylab.android.widget.TextureVideoView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.mediaviewer.library.AbsMediaViewerActivity;
import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment;
import org.mariotaku.mediaviewer.library.FileCache;
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
import org.mariotaku.twidere.task.SaveFileTask;
import org.mariotaku.twidere.task.SaveImageToGalleryTask;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.PermissionUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.media.MediaExtra;
import org.mariotaku.twidere.util.media.preview.provider.TwitterMediaProvider;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.MediaDownloadEvent;

public final class MediaViewerActivity extends AbsMediaViewerActivity implements Constants,
        AppCompatCallback, TaskStackBuilder.SupportParentable, ActionBarDrawerToggle.DelegateProvider,
        IExtendedActivity {

    private static final int REQUEST_SHARE_MEDIA = 201;
    private static final int REQUEST_PERMISSION_SAVE_MEDIA = 202;

    @Inject
    FileCache mFileCache;
    @Inject
    MediaDownloader mMediaDownloader;

    private ParcelableMedia[] mMedia;
    private AppCompatDelegate mDelegate;
    private ActionHelper mActionHelper = new ActionHelper(this);
    private SaveFileTask mSaveFileTask;
    private int mSaveToStoragePosition = -1;
    private File mShareFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        GeneralComponentHelper.build(this).inject(this);
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

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

    public void processShareIntent(Intent intent) {
        if (!hasStatus()) return;
        final ParcelableStatus status = getStatus();
        intent.putExtra(Intent.EXTRA_SUBJECT, IntentUtils.getStatusShareSubject(this, status));
        intent.putExtra(Intent.EXTRA_TEXT, IntentUtils.getStatusShareText(this, status));
    }

    public boolean hasStatus() {
        return getIntent().hasExtra(EXTRA_STATUS);
    }

    private ParcelableStatus getStatus() {
        return getIntent().getParcelableExtra(EXTRA_STATUS);
    }

    @Override
    protected int getInitialPosition() {
        return ArrayUtils.indexOf(getMedia(), getInitialMedia());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_media_viewer;
    }

    @Override
    @NonNull
    protected ViewPager findViewPager() {
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
    protected void onPause() {
        mActionHelper.dispatchOnPause();
        super.onPause();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mActionHelper.dispatchOnResumeFragments();
    }

    @Override
    public void executeAfterFragmentResumed(Action action) {
        mActionHelper.executeAfterFragmentResumed(action);
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

    @Override
    protected MediaViewerFragment instantiateMediaFragment(int position) {
        final ParcelableMedia media = getMedia()[position];
        final Bundle args = new Bundle();
        final Intent intent = getIntent();
        args.putParcelable(EXTRA_ACCOUNT_KEY, intent.getParcelableExtra(EXTRA_ACCOUNT_KEY));
        args.putParcelable(EXTRA_MEDIA, media);
        args.putParcelable(EXTRA_STATUS, intent.getParcelableExtra(EXTRA_STATUS));
        switch (media.type) {
            case ParcelableMedia.Type.IMAGE: {
                args.putParcelable(ImagePageFragment.EXTRA_MEDIA_URI, Uri.parse(media.media_url));
                return (MediaViewerFragment) Fragment.instantiate(this,
                        ImagePageFragment.class.getName(), args);
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
    protected int getMediaCount() {
        return getMedia().length;
    }

    private ParcelableMedia getInitialMedia() {
        return getIntent().getParcelableExtra(EXTRA_CURRENT_MEDIA);
    }

    private ParcelableMedia[] getMedia() {
        if (mMedia != null) return mMedia;
        return mMedia = Utils.newParcelableArray(getIntent().getParcelableArrayExtra(EXTRA_MEDIA),
                ParcelableMedia.CREATOR);
    }

    private Object getDownloadExtra() {
        return null;
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
                    if (object instanceof VideoPageFragment) {
                        shareMedia(CacheProvider.Type.VIDEO);
                    } else if (object instanceof ImagePageFragment) {
                        shareMedia(CacheProvider.Type.IMAGE);
                    }
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(media.url)));
                } catch (ActivityNotFoundException e) {
                    // TODO show error, or improve app url
                }
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
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    protected final void shareMedia(@CacheProvider.Type final String type) {
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final Object object = adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
        if (!(object instanceof CacheDownloadMediaViewerFragment)) return;
        CacheDownloadLoader.Result result = ((CacheDownloadMediaViewerFragment) object).getDownloadResult();
        if (result == null || result.cacheUri == null) {
            // TODO show error
            return;
        }
        final File destination = new File(getCacheDir(), "shared_files");
        final SaveFileTask task = new SaveFileTask(this, result.cacheUri, destination,
                new CacheProvider.CacheFileTypeCallback(this, type)) {
            private static final String PROGRESS_FRAGMENT_TAG = "progress";

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

            protected void onFileSaved(@NonNull File savedFile, @Nullable String mimeType) {
                final MediaViewerActivity activity = (MediaViewerActivity) getContext();
                if (activity == null) return;

                activity.mShareFile = savedFile;
                final Uri fileUri = FileProvider.getUriForFile(activity, AUTHORITY_TWIDERE_FILE,
                        savedFile);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setDataAndType(fileUri, mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        task.execute();
    }

    protected final void requestAndSaveToStorage(int position) {
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


    protected final void saveToStorage() {
        if (mSaveToStoragePosition == -1) return;
        final ViewPager viewPager = findViewPager();
        final PagerAdapter adapter = viewPager.getAdapter();
        final Object object = adapter.instantiateItem(viewPager, mSaveToStoragePosition);
        if (!(object instanceof CacheDownloadMediaViewerFragment)) return;
        final CacheDownloadMediaViewerFragment f = (CacheDownloadMediaViewerFragment) object;
        final CacheDownloadLoader.Result result = f.getDownloadResult();
        if (result == null) return;
        if (mSaveFileTask != null && mSaveFileTask.getStatus() == AsyncTask.Status.RUNNING) return;
        final Uri cacheUri = result.cacheUri;
        final boolean hasMedia = cacheUri != null;
        if (!hasMedia) return;
        if (f instanceof ImagePageFragment) {
            mSaveFileTask = SaveImageToGalleryTask.create(this, cacheUri, CacheProvider.Type.IMAGE);
        } else if (f instanceof VideoPageFragment) {
            mSaveFileTask = SaveImageToGalleryTask.create(this, cacheUri, CacheProvider.Type.VIDEO);
        } else {
            throw new UnsupportedOperationException();
        }
        AsyncTaskUtils.executeTask(mSaveFileTask);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    /**
     * Support library version of {@link android.app.Activity#getActionBar}.
     * <p/>
     * <p>Retrieve a reference to this activity's ActionBar.
     *
     * @return The Activity's ActionBar, or null if it does not have one.
     */
    @Nullable
    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    /**
     * Set a {@link android.widget.Toolbar Toolbar} to act as the {@link android.support.v7.app.ActionBar} for this
     * Activity window.
     * <p/>
     * <p>When set to a non-null value the {@link #getActionBar()} method will return
     * an {@link android.support.v7.app.ActionBar} object that can be used to control the given toolbar as if it were
     * a traditional window decor action bar. The toolbar's menu will be populated with the
     * Activity's options menu and the navigation button will be wired through the standard
     * {@link android.R.id#home home} menu select action.</p>
     * <p/>
     * <p>In order to use a Toolbar within the Activity's window content the application
     * must not request the window feature
     * {@link android.view.Window#FEATURE_ACTION_BAR FEATURE_SUPPORT_ACTION_BAR}.</p>
     *
     * @param toolbar Toolbar to set as the Activity's action bar
     */
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    public final boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
        if (super.onMenuItemSelected(featureId, item)) {
            return true;
        }

        final ActionBar ab = getSupportActionBar();
        if (item.getItemId() == android.R.id.home && ab != null &&
                (ab.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0) {
            return onSupportNavigateUp();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    /**
     * Enable extended support library window features.
     * <p>
     * This is a convenience for calling
     * {@link android.view.Window#requestFeature getWindow().requestFeature()}.
     * </p>
     *
     * @param featureId The desired feature as defined in
     *                  {@link android.view.Window} or {@link android.support.v4.view.WindowCompat}.
     * @return Returns true if the requested feature is supported and now enabled.
     * @see android.app.Activity#requestWindowFeature
     * @see android.view.Window#requestFeature
     */
    public boolean supportRequestWindowFeature(int featureId) {
        return getDelegate().requestWindowFeature(featureId);
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    /**
     * @hide
     */
    @Override
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    /**
     * Notifies the Activity that a support action mode has been started.
     * Activity subclasses overriding this method should call the superclass implementation.
     *
     * @param mode The new action mode.
     */
    @CallSuper
    public void onSupportActionModeStarted(ActionMode mode) {
    }

    /**
     * Notifies the activity that a support action mode has finished.
     * Activity subclasses overriding this method should call the superclass implementation.
     *
     * @param mode The action mode that just finished.
     */
    @CallSuper
    public void onSupportActionModeFinished(ActionMode mode) {
    }

    /**
     * Called when a support action mode is being started for this window. Gives the
     * callback an opportunity to handle the action mode in its own unique and
     * beautiful way. If this method returns null the system can choose a way
     * to present the mode or choose not to start the mode at all.
     *
     * @param callback Callback to control the lifecycle of this action mode
     * @return The ActionMode that was started, or null if the system should present it
     */
    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        return getDelegate().startSupportActionMode(callback);
    }

    /**
     * Support version of {@link #onCreateNavigateUpTaskStack(android.app.TaskStackBuilder)}.
     * This method will be called on all platform versions.
     * <p/>
     * Define the synthetic task stack that will be generated during Up navigation from
     * a different task.
     * <p/>
     * <p>The default implementation of this method adds the parent chain of this activity
     * as specified in the manifest to the supplied {@link android.support.v4.app.TaskStackBuilder}. Applications
     * may choose to override this method to construct the desired task stack in a different
     * way.</p>
     * <p/>
     * <p>This method will be invoked by the default implementation of {@link #onNavigateUp()}
     * if {@link #shouldUpRecreateTask(android.content.Intent)} returns true when supplied with the intent
     * returned by {@link #getParentActivityIntent()}.</p>
     * <p/>
     * <p>Applications that wish to supply extra Intent parameters to the parent stack defined
     * by the manifest should override
     * {@link #onPrepareSupportNavigateUpTaskStack(android.support.v4.app.TaskStackBuilder)}.</p>
     *
     * @param builder An empty TaskStackBuilder - the application should add intents representing
     *                the desired task stack
     */
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        builder.addParentStack(this);
    }

    /**
     * Support version of {@link #onPrepareNavigateUpTaskStack(android.app.TaskStackBuilder)}.
     * This method will be called on all platform versions.
     * <p/>
     * Prepare the synthetic task stack that will be generated during Up navigation
     * from a different task.
     * <p/>
     * <p>This method receives the {@link android.support.v4.app.TaskStackBuilder} with the constructed series of
     * Intents as generated by {@link #onCreateSupportNavigateUpTaskStack(android.support.v4.app.TaskStackBuilder)}.
     * If any extra data should be added to these intents before launching the new task,
     * the application should override this method and add that data here.</p>
     *
     * @param builder A TaskStackBuilder that has been populated with Intents by
     *                onCreateNavigateUpTaskStack.
     */
    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     * <p/>
     * <p>If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * {@link #getSupportParentActivityIntent()} for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method {@link #onPrepareSupportNavigateUpTaskStack(android.support.v4.app.TaskStackBuilder)}
     * to supply those arguments.</p>
     * <p/>
     * <p>See <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and
     * Back Stack</a> from the developer guide and
     * <a href="{@docRoot}design/patterns/navigation.html">Navigation</a> from the design guide
     * for more information about navigating within your app.</p>
     * <p/>
     * <p>See the {@link android.support.v4.app.TaskStackBuilder} class and the Activity methods
     * {@link #getSupportParentActivityIntent()}, {@link #supportShouldUpRecreateTask(android.content.Intent)}, and
     * {@link #supportNavigateUpTo(android.content.Intent)} for help implementing custom Up navigation.</p>
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    public boolean onSupportNavigateUp() {
        Intent upIntent = getSupportParentActivityIntent();

        if (upIntent != null) {
            if (supportShouldUpRecreateTask(upIntent)) {
                TaskStackBuilder b = TaskStackBuilder.create(this);
                onCreateSupportNavigateUpTaskStack(b);
                onPrepareSupportNavigateUpTaskStack(b);
                b.startActivities();

                try {
                    ActivityCompat.finishAffinity(this);
                } catch (IllegalStateException e) {
                    // This can only happen on 4.1+, when we don't have a parent or a result set.
                    // In that case we should just finish().
                    finish();
                }
            } else {
                // This activity is part of the application's task, so simply
                // navigate up to the hierarchical parent activity.
                supportNavigateUpTo(upIntent);
            }
            return true;
        }
        return false;
    }

    /**
     * Obtain an {@link android.content.Intent} that will launch an explicit target activity
     * specified by sourceActivity's {@link android.support.v4.app.NavUtils#PARENT_ACTIVITY} &lt;meta-data&gt;
     * element in the application's manifest. If the device is running
     * Jellybean or newer, the android:parentActivityName attribute will be preferred
     * if it is present.
     *
     * @return a new Intent targeting the defined parent activity of sourceActivity
     */
    @Nullable
    public Intent getSupportParentActivityIntent() {
        return NavUtils.getParentActivityIntent(this);
    }

    /**
     * Returns true if sourceActivity should recreate the task when navigating 'up'
     * by using targetIntent.
     * <p/>
     * <p>If this method returns false the app can trivially call
     * {@link #supportNavigateUpTo(android.content.Intent)} using the same parameters to correctly perform
     * up navigation. If this method returns false, the app should synthesize a new task stack
     * by using {@link android.support.v4.app.TaskStackBuilder} or another similar mechanism to perform up navigation.</p>
     *
     * @param targetIntent An intent representing the target destination for up navigation
     * @return true if navigating up should recreate a new task stack, false if the same task
     * should be used for the destination
     */
    public boolean supportShouldUpRecreateTask(Intent targetIntent) {
        return NavUtils.shouldUpRecreateTask(this, targetIntent);
    }

    /**
     * Navigate from sourceActivity to the activity specified by upIntent, finishing sourceActivity
     * in the process. upIntent will have the flag {@link android.content.Intent#FLAG_ACTIVITY_CLEAR_TOP} set
     * by this method, along with any others required for proper up navigation as outlined
     * in the Android Design Guide.
     * <p/>
     * <p>This method should be used when performing up navigation from within the same task
     * as the destination. If up navigation should cross tasks in some cases, see
     * {@link #supportShouldUpRecreateTask(android.content.Intent)}.</p>
     *
     * @param upIntent An intent representing the target destination for up navigation
     */
    public void supportNavigateUpTo(Intent upIntent) {
        NavUtils.navigateUpTo(this, upIntent);
    }

    @Nullable
    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return getDelegate().getDrawerToggleDelegate();
    }

    /**
     * @return The {@link AppCompatDelegate} being used by this Activity.
     */
    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, this);
        }
        return mDelegate;
    }


    public static class ImagePageFragment extends SubsampleImageViewerFragment {
        private int mMediaLoadState;
        private MediaDownloadEvent mMediaDownloadEvent;

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

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final ProgressWheel progressWheel = (ProgressWheel) view.findViewById(org.mariotaku.mediaviewer.library.R.id.load_progress);
            progressWheel.setBarColor(ThemeUtils.getUserAccentColor(getContext()));
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                getActivity().supportInvalidateOptionsMenu();
            }
        }

        @Nullable
        @Override
        protected Uri getDownloadUri() {
            final Uri downloadUri = super.getDownloadUri();
            if (downloadUri == null) return null;
            return replaceTwitterMediaUri(downloadUri);
        }

        static Uri replaceTwitterMediaUri(Uri downloadUri) {
            String uriString = downloadUri.toString();
            if (TwitterMediaProvider.isSupported(uriString)) {
                final String suffix = ".jpg";
                int lastIndexOfJpegSuffix = uriString.lastIndexOf(suffix);
                if (lastIndexOfJpegSuffix == -1) return downloadUri;
                final int endOfSuffix = lastIndexOfJpegSuffix + suffix.length();
                if (endOfSuffix == uriString.length()) {
                    return Uri.parse(uriString.substring(0, lastIndexOfJpegSuffix) + ".png");
                } else {
                    // Seems :orig suffix won't work jpegs -> pngs
                    String sizeSuffix = uriString.substring(endOfSuffix);
                    if (":orig".equals(sizeSuffix)) {
                        sizeSuffix = ":large";
                    }
                    return Uri.parse(uriString.substring(0, lastIndexOfJpegSuffix) + ".png" +
                            sizeSuffix);
                }
            }
            return downloadUri;
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
        }

        private ParcelableMedia getMedia() {
            return getArguments().getParcelable(EXTRA_MEDIA);
        }

        private UserKey getAccountKey() {
            return getArguments().getParcelable(EXTRA_ACCOUNT_KEY);
        }

        @Override
        public void onDownloadStart(long total) {
            super.onDownloadStart(total);
            final Context context = getContext();
            if (context != null) {
                mMediaDownloadEvent = MediaDownloadEvent.create(context, getMedia(), total);
            } else {
                mMediaDownloadEvent = null;
            }
        }

        @Override
        public void onDownloadFinished() {
            super.onDownloadFinished();
            if (mMediaDownloadEvent != null) {
                HotMobiLogger.getInstance(getContext()).log(getAccountKey(), mMediaDownloadEvent);
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
            if (bestVideoUrlAndType == null || bestVideoUrlAndType.first == null) return null;
            return Uri.parse(bestVideoUrlAndType.first);
        }


        @Override
        protected void displayMedia(CacheDownloadLoader.Result result) {
            mVideoView.setVideoURI(result.cacheUri);
            setMediaViewVisible(true);
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

            final ProgressWheel progressWheel = (ProgressWheel) view.findViewById(org.mariotaku.mediaviewer.library.R.id.load_progress);
            progressWheel.setBarColor(ThemeUtils.getUserAccentColor(getContext()));
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

        @Nullable
        private Pair<String, String> getBestVideoUrlAndType(final ParcelableMedia media,
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
        public void onDownloadStart(long total) {
            super.onDownloadStart(total);
            final Context context = getContext();
            if (context != null) {
                mMediaDownloadEvent = MediaDownloadEvent.create(context, getMedia(), total);
            } else {
                mMediaDownloadEvent = null;
            }
        }

        @Override
        public void onDownloadFinished() {
            super.onDownloadFinished();
            if (mMediaDownloadEvent != null) {
                HotMobiLogger.getInstance(getContext()).log(getAccountKey(), mMediaDownloadEvent);
            }
        }

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
                mDurationLabel.setText(String.format("%02d:%02d", durationSecs / 60, durationSecs % 60));
                mPositionLabel.setText(String.format("%02d:%02d", positionSecs / 60, positionSecs % 60));
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
