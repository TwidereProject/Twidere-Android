/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.SupportProgressDialogFragment;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.task.TwidereAsyncTask.Status;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileBannerImageTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileImageTask;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.io.File;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.createPickImageIntent;
import static org.mariotaku.twidere.util.Utils.createTakePhotoIntent;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;

public class UserProfileEditorActivity extends BaseSupportActivity implements OnSizeChangedListener, TextWatcher,
        OnClickListener, LoaderCallbacks<SingleResponse<ParcelableUser>> {

    private static final int LOADER_ID_USER = 1;

    private static final int REQUEST_UPLOAD_PROFILE_IMAGE = 1;
    private static final int REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2;
    private static final int REQUEST_PICK_LINK_COLOR = 3;
    private static final int REQUEST_PICK_BACKGROUND_COLOR = 4;

    private ImageLoaderWrapper mLazyImageLoader;
    private AsyncTaskManager mAsyncTaskManager;
    private TwidereAsyncTask<Void, Void, ?> mTask;

    private ImageView mProfileImageView;
    private ImageView mProfileBannerView;
    private EditText mEditName, mEditDescription, mEditLocation, mEditUrl;
    private View mProgressContainer, mContent;
    private View mProfileImageCamera, mProfileImageGallery;
    private View mProfileBannerGallery, mProfileBannerRemove;
    private View mActionBarOverlay;
    private View mCancelButton, mDoneButton;
    private View mSetLinkColor, mSetBackgroundColor;
    private ForegroundColorView mLinkColor, mBackgroundColor;

    private long mAccountId;
    private ParcelableUser mUser;

    private boolean mUserInfoLoaderInitialized;

    private boolean mGetUserInfoCalled;
    private Toolbar mToolbar;

    @Override
    public void beforeTextChanged(final CharSequence s, final int length, final int start, final int end) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int length, final int start, final int end) {
        updateDoneButton();
    }

    @Override
    public void afterTextChanged(final Editable s) {
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoActionBarThemeResource(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        if (!isMyAccount(this, accountId)) {
            finish();
            return;
        }
        mAsyncTaskManager = TwidereApplication.getInstance(this).getAsyncTaskManager();
        mLazyImageLoader = TwidereApplication.getInstance(this).getImageLoaderWrapper();
        mAccountId = accountId;


        setContentView(R.layout.activity_user_profile_editor);
        setSupportActionBar(mToolbar);
        ViewAccessor.setBackground(mActionBarOverlay, ThemeUtils.getWindowContentOverlay(this));
        ViewAccessor.setBackground(mToolbar, ThemeUtils.getActionBarBackground(mToolbar.getContext(),
                getCurrentThemeResourceId()));
        // setOverrideExitAniamtion(false);
        mEditName.addTextChangedListener(this);
        mEditDescription.addTextChangedListener(this);
        mEditLocation.addTextChangedListener(this);
        mEditUrl.addTextChangedListener(this);
        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mProfileImageCamera.setOnClickListener(this);
        mProfileImageGallery.setOnClickListener(this);
        mProfileBannerGallery.setOnClickListener(this);
        mProfileBannerRemove.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);
        mSetLinkColor.setOnClickListener(this);
        mSetBackgroundColor.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.getParcelable(EXTRA_USER) != null) {
            final ParcelableUser user = savedInstanceState.getParcelable(EXTRA_USER);
            displayUser(user);
            mEditName.setText(savedInstanceState.getString(EXTRA_NAME, user.name));
            mEditLocation.setText(savedInstanceState.getString(EXTRA_LOCATION, user.location));
            mEditDescription.setText(savedInstanceState.getString(EXTRA_DESCRIPTION, user.description_expanded));
            mEditUrl.setText(savedInstanceState.getString(EXTRA_URL, user.url_expanded));
        } else {
            getUserInfo();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER, mUser);
        outState.putString(EXTRA_NAME, ParseUtils.parseString(mEditName.getText()));
        outState.putString(EXTRA_DESCRIPTION, ParseUtils.parseString(mEditDescription.getText()));
        outState.putString(EXTRA_LOCATION, ParseUtils.parseString(mEditLocation.getText()));
        outState.putString(EXTRA_URL, ParseUtils.parseString(mEditUrl.getText()));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(final View view) {
        final ParcelableUser user = mUser;
        if (user == null || (mTask != null && mTask.getStatus() == Status.RUNNING)) return;
        switch (view.getId()) {
            case R.id.profile_image: {
                break;
            }
            case R.id.profile_banner: {
                break;
            }
            case R.id.profile_image_camera: {
                final Uri uri = createTempFileUri();
                final Intent intent = createTakePhotoIntent(uri, null, null, 1, 1, true);
                mTask = new UpdateProfileImageTaskInternal(this, mAsyncTaskManager, mAccountId, uri, true);
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE);
                break;
            }
            case R.id.profile_image_gallery: {
                final Uri uri = createTempFileUri();
                final Intent intent = createPickImageIntent(uri, null, null, 1, 1, true);
                mTask = new UpdateProfileImageTaskInternal(this, mAsyncTaskManager, mAccountId, uri, true);
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE);
                break;
            }
            case R.id.profile_banner_gallery: {
                final Uri uri = createTempFileUri();
                final Intent intent = createPickImageIntent(uri, null, null, 2, 1, true);
                mTask = new UpdateProfileBannerImageTaskInternal(this, mAsyncTaskManager, mAccountId, uri, true);
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BANNER_IMAGE);
                break;
            }
            case R.id.profile_banner_remove: {
                mTask = new RemoveProfileBannerTaskInternal(user.account_id);
                mTask.executeTask();
                break;
            }
            case R.id.actionbar_cancel: {
                finish();
                break;
            }
            case R.id.actionbar_done: {
                final String name = ParseUtils.parseString(mEditName.getText());
                final String url = ParseUtils.parseString(mEditUrl.getText());
                final String location = ParseUtils.parseString(mEditLocation.getText());
                final String description = ParseUtils.parseString(mEditDescription.getText());
                final int linkColor = mLinkColor.getColor();
                final int backgroundColor = mBackgroundColor.getColor();
                mTask = new UpdateProfileTaskInternal(this, mAccountId, mUser, name, url, location,
                        description, linkColor, backgroundColor);
                mTask.executeTask();
                break;
            }
            case R.id.set_link_color: {
                final Intent intent = new Intent(this, ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, user.link_color);
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                startActivityForResult(intent, REQUEST_PICK_LINK_COLOR);
                break;
            }
            case R.id.set_background_color: {
                final Intent intent = new Intent(this, ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, user.background_color);
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                startActivityForResult(intent, REQUEST_PICK_BACKGROUND_COLOR);
                break;
            }
        }
    }

    @Override
    public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
        mProgressContainer.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        return new ParcelableUserLoader(UserProfileEditorActivity.this, mAccountId, mAccountId, null, getIntent()
                .getExtras(), false, false);
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
                               final SingleResponse<ParcelableUser> data) {
        if (data.getData() != null && data.getData().id > 0) {
            displayUser(data.getData());
        } else if (mUser == null) {
            finish();
        }
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

    }

    @Override
    public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mToolbar = (Toolbar) findViewById(R.id.done_bar);
        mProgressContainer = findViewById(R.id.progress_container);
        mContent = findViewById(R.id.content);
        mProfileBannerView = (ImageView) findViewById(R.id.profile_banner);
        mProfileImageView = (ImageView) findViewById(R.id.profile_image);
        mEditName = (EditText) findViewById(R.id.name);
        mEditDescription = (EditText) findViewById(R.id.description);
        mEditLocation = (EditText) findViewById(R.id.location);
        mEditUrl = (EditText) findViewById(R.id.url);
        mActionBarOverlay = findViewById(R.id.actionbar_overlay);
        mProfileImageCamera = findViewById(R.id.profile_image_camera);
        mProfileImageGallery = findViewById(R.id.profile_image_gallery);
        mProfileBannerGallery = findViewById(R.id.profile_banner_gallery);
        mProfileBannerRemove = findViewById(R.id.profile_banner_remove);
        mLinkColor = (ForegroundColorView) findViewById(R.id.link_color);
        mBackgroundColor = (ForegroundColorView) findViewById(R.id.background_color);
        mCancelButton = findViewById(R.id.actionbar_cancel);
        mDoneButton = findViewById(R.id.actionbar_done);
        mSetLinkColor = findViewById(R.id.set_link_color);
        mSetBackgroundColor = findViewById(R.id.set_background_color);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_CANCELED) return;
        switch (requestCode) {
            case REQUEST_UPLOAD_PROFILE_BANNER_IMAGE: {
                if (mTask == null || mTask.getStatus() != Status.PENDING) return;
                mTask.executeTask();
                break;
            }
            case REQUEST_UPLOAD_PROFILE_IMAGE: {
                if (mTask == null || mTask.getStatus() != Status.PENDING) return;
                mTask.executeTask();
                break;
            }
            case REQUEST_PICK_LINK_COLOR: {
                if (resultCode == ColorPickerDialogActivity.RESULT_OK) {
                    mLinkColor.setColor(data.getIntExtra(EXTRA_COLOR, 0));
                    updateDoneButton();
                }
                break;
            }
            case REQUEST_PICK_BACKGROUND_COLOR: {
                if (resultCode == ColorPickerDialogActivity.RESULT_OK) {
                    mBackgroundColor.setColor(data.getIntExtra(EXTRA_COLOR, 0));
                    updateDoneButton();
                }
                break;
            }
        }

    }

    boolean isProfileChanged() {
        final ParcelableUser user = mUser;
        if (user == null) return true;
        if (!stringEquals(mEditName.getText(), user.name)) return true;
        if (!stringEquals(mEditDescription.getText(), user.description_expanded)) return true;
        if (!stringEquals(mEditLocation.getText(), user.location)) return true;
        if (!stringEquals(mEditUrl.getText(), isEmpty(user.url_expanded) ? user.url : user.url_expanded))
            return true;
        if (mLinkColor.getColor() != user.link_color) return true;
        if (mBackgroundColor.getColor() != user.background_color) return true;
        return false;
    }

    private Uri createTempFileUri() {
        final File cache_dir = getExternalCacheDir();
        final File file = new File(cache_dir, "tmp_image_" + System.currentTimeMillis());
        return Uri.fromFile(file);
    }

    private void displayUser(final ParcelableUser user) {
        if (!mGetUserInfoCalled) return;
        mGetUserInfoCalled = false;
        mUser = user;
        if (user != null) {
            mProgressContainer.setVisibility(View.GONE);
            mContent.setVisibility(View.VISIBLE);
            mEditName.setText(user.name);
            mEditDescription.setText(user.description_expanded);
            mEditLocation.setText(user.location);
            mEditUrl.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
            mLazyImageLoader.displayProfileImage(mProfileImageView, user.profile_image_url);
            final int def_width = getResources().getDisplayMetrics().widthPixels;
            mLazyImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, def_width);
            mLinkColor.setColor(user.link_color);
            mBackgroundColor.setColor(user.background_color);
        } else {
            mProgressContainer.setVisibility(View.GONE);
            mContent.setVisibility(View.GONE);
        }
        updateDoneButton();
    }

    private void getUserInfo() {
        final LoaderManager lm = getSupportLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        mGetUserInfoCalled = true;
        if (mUserInfoLoaderInitialized) {
            lm.restartLoader(LOADER_ID_USER, null, this);
        } else {
            lm.initLoader(LOADER_ID_USER, null, this);
            mUserInfoLoaderInitialized = true;
        }
    }

    private void setUpdateState(final boolean start) {
        mEditName.setEnabled(!start);
        mEditDescription.setEnabled(!start);
        mEditLocation.setEnabled(!start);
        mEditUrl.setEnabled(!start);
        mProfileImageView.setEnabled(!start);
        mProfileImageView.setOnClickListener(start ? null : this);
        mProfileBannerView.setEnabled(!start);
        mProfileBannerView.setOnClickListener(start ? null : this);
        invalidateOptionsMenu();
    }

    private static boolean stringEquals(final CharSequence str1, final CharSequence str2) {
        if (str1 == null || str2 == null) return str1 == str2;
        if (str1.length() != str2.length()) return false;
        for (int i = 0, j = str1.length(); i < j; i++) {
            if (str1.charAt(i) != str2.charAt(i)) return false;
        }
        return true;
    }

    private void updateDoneButton() {
        mDoneButton.setEnabled(isProfileChanged());
    }

    static class UpdateProfileTaskInternal extends TwidereAsyncTask<Void, Void, SingleResponse<ParcelableUser>> {

        private static final String DIALOG_FRAGMENT_TAG = "updating_user_profile";
        private final UserProfileEditorActivity mActivity;
        private final long mAccountId;
        private final ParcelableUser mOriginal;
        private final String mName;
        private final String mUrl;
        private final String mLocation;
        private final String mDescription;
        private final int mLinkColor;
        private final int mBackgroundColor;

        public UpdateProfileTaskInternal(final UserProfileEditorActivity activity,
                                         final long accountId, final ParcelableUser original,
                                         final String name, final String url, final String location,
                                         final String description, final int linkColor,
                                         final int backgroundColor) {
            mActivity = activity;
            mAccountId = accountId;
            mOriginal = original;
            mName = name;
            mUrl = url;
            mLocation = location;
            mDescription = description;
            mLinkColor = linkColor;
            mBackgroundColor = backgroundColor;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Void... params) {
            final Twitter twitter = getTwitterInstance(mActivity, mAccountId, true);
            try {
                User user = null;
                if (isColorChanged()) {
                    final String linkColor = String.format("%08x", mLinkColor).substring(2);
                    final String backgroundColor = String.format("%08x", mBackgroundColor).substring(2);
                    user = twitter.updateProfileColors(backgroundColor, null, linkColor, null, null);
                }
                if (isProfileChanged()) {
                    user = twitter.updateProfile(mName, mUrl, mLocation, mDescription);
                }
                if (user == null) {
                    // User profile unchanged
                    return SingleResponse.getInstance();
                }
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId));
            } catch (TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        private boolean isColorChanged() {
            final ParcelableUser orig = mOriginal;
            if (orig == null) return true;
            if (mLinkColor != orig.link_color) return true;
            if (mBackgroundColor != orig.background_color) return true;
            return false;
        }

        private boolean isProfileChanged() {
            final ParcelableUser orig = mOriginal;
            if (orig == null) return true;
            if (!stringEquals(mName, orig.name)) return true;
            if (!stringEquals(mDescription, isEmpty(orig.description_expanded) ? orig.description_plain : orig.description_expanded))
                return true;
            if (!stringEquals(mLocation, orig.location)) return true;
            if (!stringEquals(mUrl, isEmpty(orig.url_expanded) ? orig.url : orig.url_expanded))
                return true;
            return false;
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            super.onPostExecute(result);
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismissAllowingStateLoss();
            }
        }

        @Override
        protected void onPreExecute() {
            final DialogFragment df = SupportProgressDialogFragment.show(mActivity, DIALOG_FRAGMENT_TAG);
            df.setCancelable(false);
            super.onPreExecute();
        }

    }

    class RemoveProfileBannerTaskInternal extends TwidereAsyncTask<Void, Void, SingleResponse<Boolean>> {

        private final long account_id;

        RemoveProfileBannerTaskInternal(final long account_id) {
            this.account_id = account_id;
        }

        @Override
        protected SingleResponse<Boolean> doInBackground(final Void... params) {
            return TwitterWrapper.deleteProfileBannerImage(UserProfileEditorActivity.this, account_id);
        }

        @Override
        protected void onPostExecute(final SingleResponse<Boolean> result) {
            super.onPostExecute(result);
            if (result.getData() != null && result.getData()) {
                getUserInfo();
                Toast.makeText(UserProfileEditorActivity.this, R.string.profile_banner_image_updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                showErrorMessage(UserProfileEditorActivity.this, R.string.action_removing_profile_banner_image,
                        result.getException(), true);
            }
            setUpdateState(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setUpdateState(true);
        }

    }

    private class UpdateProfileBannerImageTaskInternal extends UpdateProfileBannerImageTask {

        public UpdateProfileBannerImageTaskInternal(final Context context, final AsyncTaskManager manager,
                                                    final long account_id, final Uri image_uri, final boolean delete_image) {
            super(context, manager, account_id, image_uri, delete_image);
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            super.onPostExecute(result);
            setUpdateState(false);
            getUserInfo();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setUpdateState(true);
        }

    }

    private class UpdateProfileImageTaskInternal extends UpdateProfileImageTask {

        public UpdateProfileImageTaskInternal(final Context context, final AsyncTaskManager manager,
                                              final long account_id, final Uri image_uri, final boolean delete_image) {
            super(context, manager, account_id, image_uri, delete_image);
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            super.onPostExecute(result);
            if (result != null && result.getData() != null) {
                displayUser(result.getData());
            }
            setUpdateState(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setUpdateState(true);
        }

    }
}
