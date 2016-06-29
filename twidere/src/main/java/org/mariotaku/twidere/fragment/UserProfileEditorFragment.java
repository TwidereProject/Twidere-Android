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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.twitter.Validator;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ProfileUpdate;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.ThemedImagePickerActivity;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.loader.ParcelableUserLoader;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.task.UpdateAccountInfoTask;
import org.mariotaku.twidere.task.UpdateProfileBackgroundImageTask;
import org.mariotaku.twidere.task.UpdateProfileBannerImageTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileImageTask;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterValidatorMETLengthChecker;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import static android.text.TextUtils.isEmpty;

public class UserProfileEditorFragment extends BaseSupportFragment implements OnSizeChangedListener, TextWatcher,
        OnClickListener, LoaderCallbacks<SingleResponse<ParcelableUser>>,
        KeyboardShortcutsHandler.TakeAllKeyboardShortcut {

    private static final int LOADER_ID_USER = 1;

    private static final int REQUEST_UPLOAD_PROFILE_IMAGE = 1;
    private static final int REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2;
    private static final int REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE = 3;
    private static final int REQUEST_PICK_LINK_COLOR = 11;
    private static final int REQUEST_PICK_BACKGROUND_COLOR = 12;

    private static final int RESULT_REMOVE_BANNER = 101;
    private static final String UPDATE_PROFILE_DIALOG_FRAGMENT_TAG = "update_profile";

    private AbstractTask<?, ?, UserProfileEditorFragment> mTask;
    private ImageView mProfileImageView;
    private ImageView mProfileBannerView;
    private ImageView mProfileBackgroundView;
    private MaterialEditText mEditName;
    private MaterialEditText mEditDescription;
    private MaterialEditText mEditLocation;
    private MaterialEditText mEditUrl;
    private View mProgressContainer, mEditProfileContent;
    private View mEditProfileImage;
    private View mEditProfileBanner;
    private View mEditProfileBackground;
    private View mSetLinkColor, mSetBackgroundColor;
    private ForegroundColorView mLinkColor, mBackgroundColor;
    private UserKey mAccountId;
    private ParcelableUser mUser;
    private boolean mUserInfoLoaderInitialized;
    private boolean mGetUserInfoCalled;

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
    public void onClick(final View view) {
        final ParcelableUser user = mUser;
        if (user == null || (mTask != null && !mTask.isFinished()))
            return;
        switch (view.getId()) {
            case R.id.profile_image: {
                break;
            }
            case R.id.profileBanner: {
                break;
            }
            case R.id.edit_profile_image: {
                final Intent intent = ThemedImagePickerActivity.withThemed(getActivity()).aspectRatio(1, 1)
                        .maximumSize(512, 512).build();
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE);
                break;
            }
            case R.id.edit_profile_banner: {
                final Intent intent = ThemedImagePickerActivity.withThemed(getActivity()).aspectRatio(3, 1)
                        .maximumSize(1500, 500).addEntry(getString(R.string.remove), "remove_banner", RESULT_REMOVE_BANNER).build();
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BANNER_IMAGE);
                break;
            }
            case R.id.edit_profile_background: {
                final Intent intent = ThemedImagePickerActivity.withThemed(getActivity()).build();
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE);
                break;
            }
            case R.id.set_link_color: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, user.link_color);
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                startActivityForResult(intent, REQUEST_PICK_LINK_COLOR);
                break;
            }
            case R.id.set_background_color: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
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
        mEditProfileContent.setVisibility(View.GONE);
        return new ParcelableUserLoader(getActivity(), mAccountId, mAccountId, null, getArguments(),
                false, false);
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
                               final SingleResponse<ParcelableUser> data) {
        if (data.getData() != null && data.getData().key != null) {
            displayUser(data.getData());
        } else if (mUser == null) {
        }
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                final String name = ParseUtils.parseString(mEditName.getText());
                final String url = ParseUtils.parseString(mEditUrl.getText());
                final String location = ParseUtils.parseString(mEditLocation.getText());
                final String description = ParseUtils.parseString(mEditDescription.getText());
                final int linkColor = mLinkColor.getColor();
                final int backgroundColor = mBackgroundColor.getColor();
                mTask = new UpdateProfileTaskInternal(this, mAccountId, mUser, name, url, location,
                        description, linkColor, backgroundColor);
                TaskStarter.execute(mTask);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        mAccountId = accountKey;
        if (!Utils.isMyAccount(getActivity(), accountKey)) {
            getActivity().finish();
            return;
        }

        final TwitterValidatorMETLengthChecker lengthChecker = new TwitterValidatorMETLengthChecker(new Validator());
        mEditName.addTextChangedListener(this);
        mEditDescription.addTextChangedListener(this);
        mEditLocation.addTextChangedListener(this);
        mEditUrl.addTextChangedListener(this);

        mEditDescription.setLengthChecker(lengthChecker);

        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mProfileBackgroundView.setOnClickListener(this);

        mEditProfileImage.setOnClickListener(this);
        mEditProfileBanner.setOnClickListener(this);
        mEditProfileBackground.setOnClickListener(this);

        mSetLinkColor.setOnClickListener(this);
        mSetBackgroundColor.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.getParcelable(EXTRA_USER) != null) {
            final ParcelableUser user = savedInstanceState.getParcelable(EXTRA_USER);
            assert user != null;
            displayUser(user);
            mEditName.setText(savedInstanceState.getString(EXTRA_NAME, user.name));
            mEditLocation.setText(savedInstanceState.getString(EXTRA_LOCATION, user.location));
            mEditDescription.setText(savedInstanceState.getString(EXTRA_DESCRIPTION, ParcelableUserUtils.getExpandedDescription(user)));
            mEditUrl.setText(savedInstanceState.getString(EXTRA_URL, user.url_expanded));
        } else {
            getUserInfo();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER, mUser);
        outState.putString(EXTRA_NAME, ParseUtils.parseString(mEditName.getText()));
        outState.putString(EXTRA_DESCRIPTION, ParseUtils.parseString(mEditDescription.getText()));
        outState.putString(EXTRA_LOCATION, ParseUtils.parseString(mEditLocation.getText()));
        outState.putString(EXTRA_URL, ParseUtils.parseString(mEditUrl.getText()));
    }

    @Override
    public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mEditProfileContent = view.findViewById(R.id.edit_profile_content);
        mProfileImageView = (ImageView) view.findViewById(R.id.profile_image);
        mProfileBannerView = (ImageView) view.findViewById(R.id.profileBanner);
        mProfileBackgroundView = (ImageView) view.findViewById(R.id.profile_background);
        mEditName = (MaterialEditText) view.findViewById(R.id.name);
        mEditDescription = (MaterialEditText) view.findViewById(R.id.description);
        mEditLocation = (MaterialEditText) view.findViewById(R.id.location);
        mEditUrl = (MaterialEditText) view.findViewById(R.id.url);
        mEditProfileImage = view.findViewById(R.id.edit_profile_image);
        mEditProfileBanner = view.findViewById(R.id.edit_profile_banner);
        mEditProfileBackground = view.findViewById(R.id.edit_profile_background);
        mLinkColor = (ForegroundColorView) view.findViewById(R.id.link_color);
        mBackgroundColor = (ForegroundColorView) view.findViewById(R.id.background_color);
        mSetLinkColor = view.findViewById(R.id.set_link_color);
        mSetBackgroundColor = view.findViewById(R.id.set_background_color);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == FragmentActivity.RESULT_CANCELED) return;
        switch (requestCode) {
            case REQUEST_UPLOAD_PROFILE_BANNER_IMAGE: {
                if (mTask != null && !mTask.isFinished()) return;
                if (resultCode == RESULT_REMOVE_BANNER) {
                    mTask = new RemoveProfileBannerTaskInternal(mAccountId);
                } else {
                    mTask = new UpdateProfileBannerImageTaskInternal(getActivity(), mAccountId,
                            data.getData(), true);
                }
                break;
            }
            case REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE: {
                //TODO upload profile background
                if (mTask != null && !mTask.isFinished()) return;
                mTask = new UpdateProfileBackgroundImageTaskInternal(getActivity(), mAccountId,
                        data.getData(), false, true);
                break;
            }
            case REQUEST_UPLOAD_PROFILE_IMAGE: {
                if (mTask != null && !mTask.isFinished()) return;
                mTask = new UpdateProfileImageTaskInternal(getActivity(), mAccountId,
                        data.getData(), true);
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

    private void displayUser(final ParcelableUser user) {
        if (!mGetUserInfoCalled) return;
        mGetUserInfoCalled = false;
        mUser = user;
        if (user != null) {
            mProgressContainer.setVisibility(View.GONE);
            mEditProfileContent.setVisibility(View.VISIBLE);
            mEditName.setText(user.name);
            mEditDescription.setText(ParcelableUserUtils.getExpandedDescription(user));
            mEditLocation.setText(user.location);
            mEditUrl.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
            mediaLoader.displayProfileImage(mProfileImageView, user);
            final int defWidth = getResources().getDisplayMetrics().widthPixels;
            mediaLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, defWidth);
            mediaLoader.displayImage(mProfileBackgroundView, user.profile_background_url);
            mLinkColor.setColor(user.link_color);
            mBackgroundColor.setColor(user.background_color);
            if (USER_TYPE_FANFOU_COM.equals(user.key.getHost())) {
                mEditProfileBanner.setVisibility(View.GONE);
            } else {
                mEditProfileBanner.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressContainer.setVisibility(View.GONE);
            mEditProfileContent.setVisibility(View.GONE);
        }
        updateDoneButton();
    }

    private void getUserInfo() {
        if (getActivity() == null || isDetached()) return;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        mGetUserInfoCalled = true;
        if (mUserInfoLoaderInitialized) {
            lm.restartLoader(LOADER_ID_USER, null, this);
        } else {
            lm.initLoader(LOADER_ID_USER, null, this);
            mUserInfoLoaderInitialized = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTask != null && !mTask.isFinished()) {
            TaskStarter.execute(mTask);
        }
    }

    private void dismissDialogFragment(final String tag) {
        executeAfterFragmentResumed(new Action() {
            @Override
            public void execute(IBaseFragment fragment) {
                final FragmentManager fm = getChildFragmentManager();
                final Fragment f = fm.findFragmentByTag(tag);
                if (f instanceof DialogFragment) {
                    ((DialogFragment) f).dismiss();
                }
            }
        });
    }

    private void setUpdateState(final boolean start) {
        if (!start) {
            dismissDialogFragment(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG);
            return;
        }
        executeAfterFragmentResumed(new Action() {
            @Override
            public void execute(IBaseFragment fragment) {
                final FragmentManager fm = getChildFragmentManager();
                ProgressDialogFragment df = new ProgressDialogFragment();
                df.show(fm, UPDATE_PROFILE_DIALOG_FRAGMENT_TAG);
                df.setCancelable(false);
            }
        });
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

    }

    static class UpdateProfileTaskInternal extends AbstractTask<Object, SingleResponse<ParcelableUser>,
            UserProfileEditorFragment> {

        private static final String DIALOG_FRAGMENT_TAG = "updating_user_profile";
        private final FragmentActivity mActivity;

        // Data fields
        private final UserKey mAccountKey;
        private final ParcelableUser mOriginal;
        private final String mName;
        private final String mUrl;
        private final String mLocation;
        private final String mDescription;
        private final int mLinkColor;
        private final int mBackgroundColor;

        public UpdateProfileTaskInternal(final UserProfileEditorFragment fragment,
                                         final UserKey accountKey, final ParcelableUser original,
                                         final String name, final String url, final String location,
                                         final String description, final int linkColor,
                                         final int backgroundColor) {
            mActivity = fragment.getActivity();
            mAccountKey = accountKey;
            mOriginal = original;
            mName = name;
            mUrl = url;
            mLocation = location;
            mDescription = description;
            mLinkColor = linkColor;
            mBackgroundColor = backgroundColor;
        }

        @Override
        protected SingleResponse<ParcelableUser> doLongOperation(final Object params) {
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(mActivity, mAccountKey);
            if (credentials == null) return SingleResponse.getInstance();
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mActivity, credentials,
                    true, true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                User user = null;
                if (isProfileChanged()) {
                    final ProfileUpdate profileUpdate = new ProfileUpdate();
                    profileUpdate.name(HtmlEscapeHelper.escapeBasic(mName));
                    profileUpdate.location(HtmlEscapeHelper.escapeBasic(mLocation));
                    profileUpdate.description(HtmlEscapeHelper.escapeBasic(mDescription));
                    profileUpdate.url(mUrl);
                    profileUpdate.linkColor(mLinkColor);
                    profileUpdate.backgroundColor(mBackgroundColor);
                    user = twitter.updateProfile(profileUpdate);
                }
                if (user == null) {
                    // User profile unchanged
                    return SingleResponse.getInstance();
                }
                final SingleResponse<ParcelableUser> response = SingleResponse.getInstance(
                        ParcelableUserUtils.fromUser(user, mAccountKey));
                response.getExtras().putParcelable(EXTRA_ACCOUNT, credentials);
                return response;
            } catch (MicroBlogException e) {
                return SingleResponse.getInstance(e);
            }
        }

        private boolean isProfileChanged() {
            final ParcelableUser orig = mOriginal;
            if (orig == null) return true;
            if (mLinkColor != orig.link_color) return true;
            if (mBackgroundColor != orig.background_color) return true;
            if (!stringEquals(mName, orig.name)) return true;
            if (!stringEquals(mDescription, ParcelableUserUtils.getExpandedDescription(orig)))
                return true;
            if (!stringEquals(mLocation, orig.location)) return true;
            if (!stringEquals(mUrl, isEmpty(orig.url_expanded) ? orig.url : orig.url_expanded))
                return true;
            return false;
        }

        @Override
        protected void afterExecute(UserProfileEditorFragment callback, SingleResponse<ParcelableUser> result) {
            super.afterExecute(callback, result);
            if (result.hasData()) {
                final ParcelableAccount account = result.getExtras().getParcelable(EXTRA_ACCOUNT);
                if (account != null) {
                    final UpdateAccountInfoTask task = new UpdateAccountInfoTask(mActivity);
                    task.setParams(Pair.create(account, result.getData()));
                    TaskStarter.execute(task);
                }
            }
            callback.executeAfterFragmentResumed(new Action() {
                @Override
                public void execute(IBaseFragment fragment) {
                    final Fragment f = ((UserProfileEditorFragment) fragment).getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
                    if (f instanceof DialogFragment) {
                        ((DialogFragment) f).dismissAllowingStateLoss();
                    }
                    f.getActivity().finish();
                }
            });
        }

        @Override
        protected void beforeExecute() {
            super.beforeExecute();
            final UserProfileEditorFragment callback = getCallback();
            if (callback != null) {
                callback.executeAfterFragmentResumed(new Action() {
                    @Override
                    public void execute(IBaseFragment fragment) {
                        final DialogFragment df = ProgressDialogFragment.show(((UserProfileEditorFragment) fragment).getActivity(), DIALOG_FRAGMENT_TAG);
                        df.setCancelable(false);
                    }
                });
            }
        }

    }

    class RemoveProfileBannerTaskInternal extends AbstractTask<Object, SingleResponse<Boolean>, UserProfileEditorFragment> {

        private final UserKey mAccountKey;

        RemoveProfileBannerTaskInternal(final UserKey accountKey) {
            this.mAccountKey = accountKey;
        }

        @Override
        protected SingleResponse<Boolean> doLongOperation(final Object params) {
            return TwitterWrapper.deleteProfileBannerImage(getActivity(), mAccountKey);
        }

        @Override
        protected void afterExecute(UserProfileEditorFragment callback, final SingleResponse<Boolean> result) {
            super.afterExecute(callback, result);
            if (result.getData() != null && result.getData()) {
                getUserInfo();
                Toast.makeText(getActivity(), R.string.profile_banner_image_updated, Toast.LENGTH_SHORT).show();
            } else {
                Utils.showErrorMessage(getActivity(), R.string.action_removing_profile_banner_image,
                        result.getException(), true);
            }
            setUpdateState(false);
        }

        @Override
        protected void beforeExecute() {
            super.beforeExecute();
            setUpdateState(true);
        }

    }

    private class UpdateProfileBannerImageTaskInternal extends UpdateProfileBannerImageTask<UserProfileEditorFragment> {

        public UpdateProfileBannerImageTaskInternal(final Context context, final UserKey accountKey,
                                                    final Uri imageUri, final boolean deleteImage) {
            super(context, accountKey, imageUri, deleteImage);
        }

        @Override
        protected void afterExecute(UserProfileEditorFragment callback, final SingleResponse<ParcelableUser> result) {
            super.afterExecute(callback, result);
            setUpdateState(false);
            getUserInfo();
        }

        @Override
        protected void beforeExecute() {
            super.beforeExecute();
            setUpdateState(true);
        }

    }

    private class UpdateProfileBackgroundImageTaskInternal extends UpdateProfileBackgroundImageTask<UserProfileEditorFragment> {

        public UpdateProfileBackgroundImageTaskInternal(final Context context, final UserKey accountKey,
                                                        final Uri imageUri, final boolean tile,
                                                        final boolean deleteImage) {
            super(context, accountKey, imageUri, tile, deleteImage);
        }

        @Override
        protected void afterExecute(UserProfileEditorFragment callback, final SingleResponse<ParcelableUser> result) {
            super.afterExecute(callback, result);
            setUpdateState(false);
            getUserInfo();
        }

        @Override
        protected void beforeExecute() {
            super.beforeExecute();
            setUpdateState(true);
        }

    }

    private class UpdateProfileImageTaskInternal extends UpdateProfileImageTask<UserProfileEditorFragment> {

        public UpdateProfileImageTaskInternal(final Context context, final UserKey accountKey,
                                              final Uri imageUri, final boolean deleteImage) {
            super(context, accountKey, imageUri, deleteImage);
        }

        @Override
        protected void afterExecute(UserProfileEditorFragment callback, SingleResponse<ParcelableUser> result) {
            super.afterExecute(callback, result);
            if (result != null && result.getData() != null) {
                displayUser(result.getData());
            }
            setUpdateState(false);
        }

        @Override
        protected void beforeExecute() {
            super.beforeExecute();
            setUpdateState(true);
        }

    }
}
