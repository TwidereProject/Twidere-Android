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

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.AccountSelectorActivity;
import org.mariotaku.twidere.activity.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserListUpdate;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.UserListSubscriptionEvent;
import org.mariotaku.twidere.model.message.UserListUpdatedEvent;
import org.mariotaku.twidere.model.util.ParcelableUserListUtils;
import org.mariotaku.twidere.text.validator.UserListNameValidator;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;

public class UserListFragment extends AbsToolbarTabPagesFragment implements OnClickListener,
        LoaderCallbacks<SingleResponse<ParcelableUserList>>, SystemWindowsInsetsCallback,
        SupportFragmentCallback {

    private boolean mUserListLoaderInitialized;

    private ParcelableUserList mUserList;

    public void displayUserList(final ParcelableUserList userList) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        getLoaderManager().destroyLoader(0);
        mUserList = userList;

        if (userList != null) {
            activity.setTitle(userList.name);
        } else {
            activity.setTitle(R.string.user_list);
        }
        invalidateOptionsMenu();
    }

    public void getUserListInfo(final boolean omitIntentExtra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(0);
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
        if (!mUserListLoaderInitialized) {
            lm.initLoader(0, args, this);
            mUserListLoaderInitialized = true;
        } else {
            lm.restartLoader(0, args, this);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        switch (requestCode) {
            case REQUEST_SELECT_USER: {
                final ParcelableUserList userList = mUserList;
                if (resultCode != Activity.RESULT_OK || !data.hasExtra(EXTRA_USER) || twitter == null
                        || userList == null) return;
                final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
                twitter.addUserListMembersAsync(userList.account_key, userList.id, user);
                return;
            }
            case REQUEST_SELECT_ACCOUNT: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final ParcelableUserList userList = mUserList;
                    final UserKey accountKey = data.getParcelableExtra(EXTRA_ACCOUNT_KEY);
                    IntentUtils.openUserListDetails(getActivity(), accountKey, userList.id,
                            userList.user_key, userList.user_screen_name, userList.name);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FragmentActivity activity = getActivity();
        setHasOptionsMenu(true);

        Utils.setNdefPushMessageCallback(activity, new CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableUserList userList = getUserList();
                if (userList == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(LinkCreator.getTwitterUserListLink(userList.user_screen_name, userList.name)),
                });
            }
        });

        getUserListInfo(false);
    }

    @Override
    protected void addTabs(SupportTabsAdapter adapter) {
        final Bundle args = getArguments(), tabArgs = new Bundle();
        if (args.containsKey(EXTRA_USER_LIST)) {
            final ParcelableUserList userList = args.getParcelable(EXTRA_USER_LIST);
            assert userList != null;
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, userList.account_key);
            tabArgs.putParcelable(EXTRA_USER_KEY, userList.user_key);
            tabArgs.putString(EXTRA_SCREEN_NAME, userList.user_screen_name);
            tabArgs.putString(EXTRA_LIST_ID, userList.id);
            tabArgs.putString(EXTRA_LIST_NAME, userList.name);
        } else {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, args.getParcelable(EXTRA_ACCOUNT_KEY));
            tabArgs.putParcelable(EXTRA_USER_KEY, args.getParcelable(EXTRA_USER_KEY));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
            tabArgs.putString(EXTRA_LIST_ID, args.getString(EXTRA_LIST_ID));
            tabArgs.putString(EXTRA_LIST_NAME, args.getString(EXTRA_LIST_NAME));
        }
        adapter.addTab(UserListTimelineFragment.class, tabArgs, getString(R.string.statuses), null, 0, null);
        adapter.addTab(UserListMembersFragment.class, tabArgs, getString(R.string.members), null, 1, null);
        adapter.addTab(UserListSubscribersFragment.class, tabArgs, getString(R.string.subscribers), null, 2, null);
    }

    public ParcelableUserList getUserList() {
        return mUserList;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mUserList = null;
        getLoaderManager().destroyLoader(0);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final ParcelableUserList userList = mUserList;
        MenuUtils.setMenuItemAvailability(menu, R.id.info, userList != null);
        menu.removeGroup(MENU_GROUP_USER_LIST_EXTENSION);
        if (userList != null) {
            final boolean isMyList = userList.user_key.equals(userList.account_key);
            final boolean isFollowing = userList.is_following;
            MenuUtils.setMenuItemAvailability(menu, R.id.edit, isMyList);
            MenuUtils.setMenuItemAvailability(menu, R.id.follow, !isMyList);
            MenuUtils.setMenuItemAvailability(menu, R.id.add, isMyList);
            MenuUtils.setMenuItemAvailability(menu, R.id.delete, isMyList);
            final MenuItem followItem = menu.findItem(R.id.follow);
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_action_cancel);
                followItem.setTitle(R.string.unsubscribe);
            } else {
                followItem.setIcon(R.drawable.ic_action_add);
                followItem.setTitle(R.string.subscribe);
            }
            final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
            extensionsIntent.setExtrasClassLoader(getActivity().getClassLoader());
            extensionsIntent.putExtra(EXTRA_USER_LIST, userList);
            MenuUtils.addIntentToMenu(getActivity(), menu, extensionsIntent, MENU_GROUP_USER_LIST_EXTENSION);
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.edit, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.follow, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.add, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.delete, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUserList userList = mUserList;
        if (twitter == null || userList == null) return false;
        switch (item.getItemId()) {
            case R.id.add: {
                if (!userList.user_key.equals(userList.account_key)) return false;
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_KEY, userList.account_key);
                startActivityForResult(intent, REQUEST_SELECT_USER);
                break;
            }
            case R.id.delete: {
                if (!userList.user_key.equals(userList.account_key)) return false;
                DestroyUserListDialogFragment.show(getFragmentManager(), userList);
                break;
            }
            case R.id.edit: {
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_ACCOUNT_KEY, userList.account_key);
                args.putString(EXTRA_LIST_NAME, userList.name);
                args.putString(EXTRA_DESCRIPTION, userList.description);
                args.putBoolean(EXTRA_IS_PUBLIC, userList.is_public);
                args.putString(EXTRA_LIST_ID, userList.id);
                final DialogFragment f = new EditUserListDialogFragment();
                f.setArguments(args);
                f.show(getFragmentManager(), "edit_user_list_details");
                return true;
            }
            case R.id.follow: {
                if (userList.is_following) {
                    DestroyUserListSubscriptionDialogFragment.show(getFragmentManager(), userList);
                } else {
                    twitter.createUserListSubscriptionAsync(userList.account_key, userList.id);
                }
                return true;
            }
            case R.id.open_with_account: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
                intent.setClass(getActivity(), AccountSelectorActivity.class);
                intent.putExtra(EXTRA_SINGLE_SELECTION, true);
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
                break;
            }
            default: {
                if (item.getIntent() != null) {
                    try {
                        startActivity(item.getIntent());
                    } catch (final ActivityNotFoundException e) {
                        Log.w(LOGTAG, e);
                        return false;
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.error_container: {
                getUserListInfo(true);
                break;
            }
            case R.id.profile_image: {
                final ParcelableUserList userList = mUserList;
                if (userList == null) return;
                IntentUtils.openUserProfile(getActivity(), userList.account_key,
                        userList.user_key, userList.user_screen_name, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API), null);
                break;
            }
        }

    }

    @Override
    public Loader<SingleResponse<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String listId = args.getString(EXTRA_LIST_ID);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
        return new ParcelableUserListLoader(getActivity(), omitIntentExtra, getArguments(), accountKey, listId,
                listName, userKey, screenName);
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableUserList>> loader,
                               final SingleResponse<ParcelableUserList> data) {
        if (data == null) return;
        if (getActivity() == null) return;
        if (data.hasData()) {
            final ParcelableUserList list = data.getData();
            displayUserList(list);
        } else if (data.hasException()) {
        }
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

    }

    @Subscribe
    public void onUserListUpdated(UserListUpdatedEvent event) {
        if (mUserList == null) return;
        if (TextUtils.equals(event.getUserList().id, mUserList.id)) {
            getUserListInfo(true);
        }
    }

    @Subscribe
    public void onUserListSubscriptionChanged(UserListSubscriptionEvent event) {
        if (mUserList == null) return;
        if (TextUtils.equals(event.getUserList().id, mUserList.id)) {
            getUserListInfo(true);
        }
    }

    public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        private String mName, mDescription;
        private UserKey mAccountKey;
        private String mListId;
        private boolean mIsPublic;

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final AlertDialog alertDialog = (AlertDialog) dialog;
                    final MaterialEditText editName = (MaterialEditText) alertDialog.findViewById(R.id.name);
                    final MaterialEditText editDescription = (MaterialEditText) alertDialog.findViewById(R.id.description);
                    final CheckBox editIsPublic = (CheckBox) alertDialog.findViewById(R.id.is_public);
                    assert editName != null && editDescription != null && editIsPublic != null;
                    final String name = ParseUtils.parseString(editName.getText());
                    final String description = ParseUtils.parseString(editDescription.getText());
                    final boolean isPublic = editIsPublic.isChecked();
                    if (TextUtils.isEmpty(name)) return;
                    final UserListUpdate update = new UserListUpdate();
                    update.setMode(isPublic ? UserList.Mode.PUBLIC : UserList.Mode.PRIVATE);
                    update.setName(name);
                    update.setDescription(description);
                    mTwitterWrapper.updateUserListDetails(mAccountKey, mListId, update);
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
            mAccountKey = bundle != null ? bundle.<UserKey>getParcelable(EXTRA_ACCOUNT_KEY) : null;
            mListId = bundle != null ? bundle.getString(EXTRA_LIST_ID) : null;
            mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
            mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
            mIsPublic = bundle == null || bundle.getBoolean(EXTRA_IS_PUBLIC, true);
            final Context context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(R.layout.dialog_user_list_detail_editor);
            builder.setTitle(R.string.user_list);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {

                    AlertDialog alertDialog = (AlertDialog) dialog;
                    MaterialEditText editName = (MaterialEditText) alertDialog.findViewById(R.id.name);
                    MaterialEditText editDescription = (MaterialEditText) alertDialog.findViewById(R.id.description);
                    CheckBox editPublic = (CheckBox) alertDialog.findViewById(R.id.is_public);
                    assert editName != null && editDescription != null && editPublic != null;
                    editName.addValidator(new UserListNameValidator(getString(R.string.invalid_list_name)));
                    if (mName != null) {
                        editName.setText(mName);
                    }
                    if (mDescription != null) {
                        editDescription.setText(mDescription);
                    }
                    editPublic.setChecked(mIsPublic);
                }
            });
            return dialog;
        }

        @Override
        public void onSaveInstanceState(final Bundle outState) {
            outState.putParcelable(EXTRA_ACCOUNT_KEY, mAccountKey);
            outState.putString(EXTRA_LIST_ID, mListId);
            outState.putString(EXTRA_LIST_NAME, mName);
            outState.putString(EXTRA_DESCRIPTION, mDescription);
            outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
            super.onSaveInstanceState(outState);
        }

    }

    static class ParcelableUserListLoader extends AsyncTaskLoader<SingleResponse<ParcelableUserList>> {

        private final boolean mOmitIntentExtra;
        private final Bundle mExtras;
        private final UserKey mAccountKey;
        private final UserKey mUserKey;
        private final String mListId;
        private final String mScreenName, mListName;

        private ParcelableUserListLoader(final Context context, final boolean omitIntentExtra,
                                         final Bundle extras, final UserKey accountKey,
                                         final String listId, final String listName,
                                         final UserKey userKey, final String screenName) {
            super(context);
            mOmitIntentExtra = omitIntentExtra;
            mExtras = extras;
            mAccountKey = accountKey;
            mUserKey = userKey;
            mListId = listId;
            mScreenName = screenName;
            mListName = listName;
        }

        @Override
        public SingleResponse<ParcelableUserList> loadInBackground() {
            if (!mOmitIntentExtra && mExtras != null) {
                final ParcelableUserList cache = mExtras.getParcelable(EXTRA_USER_LIST);
                if (cache != null) return SingleResponse.getInstance(cache);
            }
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountKey,
                    true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList list;
                if (mListId != null) {
                    list = twitter.showUserList(mListId);
                } else if (mUserKey != null) {
                    list = twitter.showUserList(mListName, mUserKey.getId());
                } else if (mScreenName != null) {
                    list = twitter.showUserListByScrenName(mListName, mScreenName);
                } else
                    return SingleResponse.getInstance();
                return SingleResponse.getInstance(ParcelableUserListUtils.from(list, mAccountKey));
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        public void onStartLoading() {
            forceLoad();
        }

    }

}
