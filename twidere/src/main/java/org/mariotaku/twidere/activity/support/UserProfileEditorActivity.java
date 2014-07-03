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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.createPickImageIntent;
import static org.mariotaku.twidere.util.Utils.createTakePhotoIntent;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonLifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.task.AsyncTask.Status;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileBannerImageTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileImageTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileTask;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.view.ProfileImageBannerLayout;
import org.mariotaku.twidere.view.ProfileImageView;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.io.File;

public class UserProfileEditorActivity extends BaseSupportActivity implements OnSizeChangedListener, TextWatcher,
		OnClickListener, CroutonLifecycleCallback, LoaderCallbacks<SingleResponse<ParcelableUser>> {

	private static final int LOADER_ID_USER = 1;

	private static final int REQUEST_UPLOAD_PROFILE_IMAGE = 1;
	private static final int REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2;

	private ImageLoaderWrapper mLazyImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private AsyncTask<Void, Void, ?> mTask;

	private ProfileImageBannerLayout mProfileImageBannerLayout;
	private ProfileImageView mProfileImageView;
	private ImageView mProfileBannerView;
	private EditText mEditName, mEditDescription, mEditLocation, mEditUrl;
	private View mProgress, mContent;

	private PopupMenu mPopupMenu;

	private boolean mBackPressed;
	private long mAccountId;
	private int mBannerWidth;
	private ParcelableUser mUser;

	private boolean mUserInfoLoaderInitialized;

	private boolean mGetUserInfoCalled;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_PROFILE_UPDATED.equals(action)) {
				if (mUser == null || intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id) {
					getUserInfo();
				}
			}
		}
	};

	private final OnMenuItemClickListener mProfileBannerImageMenuListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(final MenuItem item) {
			if (mUser == null) return false;
			switch (item.getItemId()) {
				case MENU_TAKE_PHOTO: {
					final Uri uri = createTempFileUri();
					final Intent intent = createTakePhotoIntent(uri, null, null, 2, 1, true);
					startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BANNER_IMAGE);
					mTask = new UpdateProfileBannerImageTaskInternal(UserProfileEditorActivity.this, mAsyncTaskManager,
							mAccountId, uri, true);
					break;
				}
				case MENU_PICK_FROM_GALLERY: {
					final Uri uri = createTempFileUri();
					final Intent intent = createPickImageIntent(uri, null, null, 2, 1, true);
					try {
						startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BANNER_IMAGE);
						mTask = new UpdateProfileBannerImageTaskInternal(UserProfileEditorActivity.this,
								mAsyncTaskManager, mAccountId, uri, true);
					} catch (final Exception e) {
						Log.w(LOGTAG, e);
					}
					break;
				}
				case MENU_DELETE: {
					mTask = new RemoveProfileBannerTaskInternal(mUser.account_id);
					mTask.execute();
					break;
				}
			}
			return true;
		}

	};

	private final OnMenuItemClickListener mProfileImageMenuListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(final MenuItem item) {
			if (mUser == null) return false;
			switch (item.getItemId()) {
				case MENU_TAKE_PHOTO: {
					final Uri uri = createTempFileUri();
					final Intent intent = createTakePhotoIntent(uri, null, null, 1, 1, true);
					startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE);
					mTask = new UpdateProfileImageTaskInternal(UserProfileEditorActivity.this, mAsyncTaskManager,
							mAccountId, uri, true);
					break;
				}
				case MENU_PICK_FROM_GALLERY: {
					final Uri uri = createTempFileUri();
					final Intent intent = createPickImageIntent(uri, null, null, 1, 1, true);
					try {
						startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE);
						mTask = new UpdateProfileImageTaskInternal(UserProfileEditorActivity.this, mAsyncTaskManager,
								mAccountId, uri, true);
					} catch (final Exception e) {
						Log.w(LOGTAG, e);
					}
					break;
				}
			}
			return true;
		}

	};

	@Override
	public void afterTextChanged(final Editable s) {
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int length, final int start, final int end) {
	}

	@Override
	public void onBackPressed() {
		if (mHasUnsavedChanges() && !mBackPressed) {
			final CroutonStyle.Builder builder = new CroutonStyle.Builder(CroutonStyle.INFO);
			final Crouton crouton = Crouton.makeText(this, R.string.unsaved_change_back_pressed, builder.build());
			crouton.setLifecycleCallback(this);
			crouton.show();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(final View view) {
		if (mUser == null) return;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		switch (view.getId()) {
			case ProfileImageBannerLayout.VIEW_ID_PROFILE_IMAGE: {
				mPopupMenu = PopupMenu.getInstance(this, view);
				mPopupMenu.inflate(R.menu.action_profile_image);
				mPopupMenu.setOnMenuItemClickListener(mProfileImageMenuListener);
				break;
			}
			case ProfileImageBannerLayout.VIEW_ID_PROFILE_BANNER: {
				mPopupMenu = PopupMenu.getInstance(this, view);
				mPopupMenu.inflate(R.menu.action_profile_banner_image);
				final Menu menu = mPopupMenu.getMenu();
				final MenuItem delete_submenu = menu.findItem(MENU_DELETE_SUBMENU);
				final boolean has_profile_banner = !isEmpty(mUser.profile_banner_url);
				delete_submenu.setVisible(has_profile_banner);
				delete_submenu.setEnabled(has_profile_banner);
				mPopupMenu.setOnMenuItemClickListener(mProfileBannerImageMenuListener);
				break;
			}
			default: {
				return;
			}
		}
		mPopupMenu.show();

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mProgress = findViewById(R.id.progress);
		mContent = findViewById(R.id.content);
		mProfileImageBannerLayout = (ProfileImageBannerLayout) findViewById(R.id.profile_image_banner);
		mProfileBannerView = mProfileImageBannerLayout.getProfileBannerImageView();
		mProfileImageView = mProfileImageBannerLayout.getProfileImageView();
		mEditName = (EditText) findViewById(R.id.name);
		mEditDescription = (EditText) findViewById(R.id.description);
		mEditLocation = (EditText) findViewById(R.id.location);
		mEditUrl = (EditText) findViewById(R.id.url);
	}

	@Override
	public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
		mProgress.setVisibility(View.VISIBLE);
		mContent.setVisibility(View.GONE);
		setProgressBarIndeterminateVisibility(true);
		return new ParcelableUserLoader(UserProfileEditorActivity.this, mAccountId, mAccountId, null, getIntent()
				.getExtras(), false, false);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit_user_profile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDisplayed() {
		mBackPressed = true;
	}

	@Override
	public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
			final SingleResponse<ParcelableUser> data) {
		if (data.getData() != null && data.getData().id > 0) {
			displayUser(data.getData());
		} else if (mUser == null) {
			finish();
		}
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_SAVE: {
				final String name = ParseUtils.parseString(mEditName.getText());
				final String url = ParseUtils.parseString(mEditUrl.getText());
				final String location = ParseUtils.parseString(mEditLocation.getText());
				final String description = ParseUtils.parseString(mEditDescription.getText());
				mTask = new UpdateProfileTaskInternal(this, mAsyncTaskManager, mAccountId, name, url, location,
						description);
				mTask.execute();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem save = menu.findItem(MENU_SAVE);
		if (save != null) {
			save.setEnabled(mHasUnsavedChanges() && (mTask == null || mTask.getStatus() != AsyncTask.Status.RUNNING));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onRemoved() {
		mBackPressed = false;
	}

	@Override
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
	}

	@Override
	public void onTextChanged(final CharSequence s, final int length, final int start, final int end) {
		invalidateOptionsMenu();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_CANCELED) return;
		if (mTask == null || mTask.getStatus() != Status.PENDING) return;
		switch (requestCode) {
			case REQUEST_UPLOAD_PROFILE_BANNER_IMAGE: {
				mTask.execute();
				break;
			}
			case REQUEST_UPLOAD_PROFILE_IMAGE: {
				mTask.execute();
				break;
			}
		}

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
		setContentView(R.layout.edit_user_profile);
		// setOverrideExitAniamtion(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mProfileImageBannerLayout.setOnSizeChangedListener(this);
		mEditName.addTextChangedListener(this);
		mEditDescription.addTextChangedListener(this);
		mEditLocation.addTextChangedListener(this);
		mEditUrl.addTextChangedListener(this);
		mProfileImageView.setOnClickListener(this);
		mProfileBannerView.setOnClickListener(this);
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
		final IntentFilter filter = new IntentFilter(BROADCAST_PROFILE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
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
		if (user != null && user.id > 0) {
			mProgress.setVisibility(View.GONE);
			mContent.setVisibility(View.VISIBLE);
			mEditName.setText(user.name);
			mEditDescription.setText(user.description_expanded);
			mEditLocation.setText(user.location);
			mEditUrl.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
			mLazyImageLoader.displayProfileImage(mProfileImageView, user.profile_image_url);
			final int def_width = getResources().getDisplayMetrics().widthPixels;
			final int width = mBannerWidth > 0 ? mBannerWidth : def_width;
			mLazyImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
		} else {
			mProgress.setVisibility(View.GONE);
			mContent.setVisibility(View.GONE);
		}
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
		setProgressBarIndeterminateVisibility(start);
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

	boolean mHasUnsavedChanges() {
		if (mUser == null) return false;
		return !stringEquals(mEditName.getText(), mUser.name)
				|| !stringEquals(mEditDescription.getText(), mUser.description_expanded)
				|| !stringEquals(mEditLocation.getText(), mUser.location)
				|| !stringEquals(mEditUrl.getText(), isEmpty(mUser.url_expanded) ? mUser.url : mUser.url_expanded);
	}

	private static boolean stringEquals(final CharSequence str1, final CharSequence str2) {
		if (str1 == null || str2 == null) return str1 == str2;
		return str1.toString().equals(str2.toString());
	}

	private class UpdateProfileBannerImageTaskInternal extends UpdateProfileBannerImageTask {

		public UpdateProfileBannerImageTaskInternal(final Context context, final AsyncTaskManager manager,
				final long account_id, final Uri image_uri, final boolean delete_image) {
			super(context, manager, account_id, image_uri, delete_image);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Boolean> result) {
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

	class RemoveProfileBannerTaskInternal extends AsyncTask<Void, Void, SingleResponse<Boolean>> {

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

	class UpdateProfileTaskInternal extends UpdateProfileTask {

		public UpdateProfileTaskInternal(final Context context, final AsyncTaskManager manager, final long account_id,
				final String name, final String url, final String location, final String description) {
			super(context, manager, account_id, name, url, location, description);
		}

		@Override
		protected SingleResponse<ParcelableUser> doInBackground(final Void... params) {
			final SingleResponse<ParcelableUser> result = super.doInBackground(params);
			if (result.getData() != null && isMyAccount(getContext(), result.getData().id)) {
				final ContentResolver resolver = getContentResolver();
				final ContentValues values = new ContentValues();
				values.put(Accounts.NAME, result.getData().name);
				values.put(Accounts.SCREEN_NAME, result.getData().screen_name);
				values.put(Accounts.PROFILE_IMAGE_URL, result.getData().profile_image_url);
				values.put(Accounts.PROFILE_BANNER_URL, result.getData().profile_banner_url);
				final String where = Accounts.ACCOUNT_ID + " = " + result.getData().id;
				resolver.update(Accounts.CONTENT_URI, values, where, null);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
			super.onPostExecute(result);
			if (result != null && result.getData() != null) {
				mGetUserInfoCalled = true;
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
