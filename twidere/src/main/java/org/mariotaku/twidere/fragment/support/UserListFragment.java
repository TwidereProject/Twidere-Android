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

package org.mariotaku.twidere.fragment.support;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserListUpdate;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.text.validator.UserListNameValidator;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.TabPagerIndicator;

import static org.mariotaku.twidere.util.MenuUtils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class UserListFragment extends BaseSupportFragment implements OnClickListener,
        LoaderCallbacks<SingleResponse<ParcelableUserList>>, SystemWindowsInsetsCallback,
        SupportFragmentCallback {

    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private View mPagerOverlay;

    private SupportTabsAdapter mPagerAdapter;
    private boolean mUserListLoaderInitialized;

    private ParcelableUserList mUserList;
    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            final ParcelableUserList userList = intent.getParcelableExtra(EXTRA_USER_LIST);
            if (userList == null || mUserList == null)
                return;
            if (BROADCAST_USER_LIST_DETAILS_UPDATED.equals(action)) {
                if (userList.id == mUserList.id) {
                    getUserListInfo(true);
                }
            } else if (BROADCAST_USER_LIST_SUBSCRIBED.equals(action) || BROADCAST_USER_LIST_UNSUBSCRIBED.equals(action)) {
                if (userList.id == mUserList.id) {
                    getUserListInfo(true);
                }
            }
        }
    };

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

    @Override
    public Fragment getCurrentVisibleFragment() {
        final int currentItem = mViewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= mPagerAdapter.getCount()) return null;
        return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentItem);
    }

    @Override
    public boolean triggerRefresh(int position) {
        return false;
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return false;
    }

    public void getUserListInfo(final boolean omit_intent_extra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(0);
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omit_intent_extra);
        if (!mUserListLoaderInitialized) {
            lm.initLoader(0, args, this);
            mUserListLoaderInitialized = true;
        } else {
            lm.restartLoader(0, args, this);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_USER: {
                if (resultCode != Activity.RESULT_OK || !data.hasExtra(EXTRA_USER) || mTwitterWrapper == null
                        || mUserList == null) return;
                final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
                mTwitterWrapper.addUserListMembersAsync(mUserList.account_id, mUserList.id, user);
                return;
            }
            case REQUEST_SELECT_ACCOUNT: {
                final ParcelableUserList userList = mUserList;
                if (userList == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final long accountId = data.getLongExtra(EXTRA_ID, -1);
                    openUserListDetails(getActivity(), accountId, userList.id, userList.user_id,
                            userList.user_screen_name, userList.name);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_pages, container, false);
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

        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);
        getUserListInfo(false);
        setupUserPages();
    }

    private ParcelableUserList getUserList() {
        return mUserList;
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_DETAILS_UPDATED);
        filter.addAction(BROADCAST_USER_LIST_SUBSCRIBED);
        filter.addAction(BROADCAST_USER_LIST_UNSUBSCRIBED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
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
        setMenuItemAvailability(menu, R.id.info, userList != null);
        menu.removeGroup(MENU_GROUP_USER_LIST_EXTENSION);
        if (userList != null) {
            final boolean isMyList = userList.user_id == userList.account_id;
            final boolean isFollowing = userList.is_following;
            setMenuItemAvailability(menu, R.id.edit, isMyList);
            setMenuItemAvailability(menu, R.id.follow, !isMyList);
            setMenuItemAvailability(menu, R.id.add, isMyList);
            setMenuItemAvailability(menu, R.id.delete, isMyList);
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
            addIntentToMenu(getActivity(), menu, extensionsIntent, MENU_GROUP_USER_LIST_EXTENSION);
        } else {
            setMenuItemAvailability(menu, R.id.edit, false);
            setMenuItemAvailability(menu, R.id.follow, false);
            setMenuItemAvailability(menu, R.id.add, false);
            setMenuItemAvailability(menu, R.id.delete, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUserList userList = mUserList;
        if (twitter == null || userList == null) return false;
        switch (item.getItemId()) {
            case R.id.add: {
                if (userList.user_id != userList.account_id) return false;
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_ID, userList.account_id);
                startActivityForResult(intent, REQUEST_SELECT_USER);
                break;
            }
            case R.id.delete: {
                if (userList.user_id != userList.account_id) return false;
                DestroyUserListDialogFragment.show(getFragmentManager(), userList);
                break;
            }
            case R.id.edit: {
                final Bundle args = new Bundle();
                args.putLong(EXTRA_ACCOUNT_ID, userList.account_id);
                args.putString(EXTRA_LIST_NAME, userList.name);
                args.putString(EXTRA_DESCRIPTION, userList.description);
                args.putBoolean(EXTRA_IS_PUBLIC, userList.is_public);
                args.putLong(EXTRA_LIST_ID, userList.id);
                final DialogFragment f = new EditUserListDialogFragment();
                f.setArguments(args);
                f.show(getFragmentManager(), "edit_user_list_details");
                return true;
            }
            case R.id.follow: {
                if (userList.is_following) {
                    DestroyUserListSubscriptionDialogFragment.show(getFragmentManager(), userList);
                } else {
                    twitter.createUserListSubscriptionAsync(userList.account_id, userList.id);
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
                if (mUserList == null) return;
                openUserProfile(getActivity(), mUserList.account_id,
                        mUserList.user_id, mUserList.user_screen_name, null);
                break;
            }
        }

    }

    @Override
    public Loader<SingleResponse<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
        setProgressBarIndeterminateVisibility(true);
        final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
        final long userId = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
        final long listId = args != null ? args.getLong(EXTRA_LIST_ID, -1) : -1;
        final String listName = args != null ? args.getString(EXTRA_LIST_NAME) : null;
        final String screenName = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
        final boolean omitIntentExtra = args == null || args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
        return new ParcelableUserListLoader(getActivity(), omitIntentExtra, getArguments(), accountId, listId,
                listName, userId, screenName);
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
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) view.findViewById(R.id.view_pager_tabs);
        mPagerOverlay = view.findViewById(R.id.pager_window_overlay);
    }

    private void setupUserPages() {
        final Bundle args = getArguments(), tabArgs = new Bundle();
        if (args.containsKey(EXTRA_USER)) {
            final ParcelableUserList userList = args.getParcelable(EXTRA_USER_LIST);
            tabArgs.putLong(EXTRA_ACCOUNT_ID, userList.account_id);
            tabArgs.putLong(EXTRA_USER_ID, userList.user_id);
            tabArgs.putString(EXTRA_SCREEN_NAME, userList.user_screen_name);
            tabArgs.putLong(EXTRA_LIST_ID, userList.id);
            tabArgs.putString(EXTRA_LIST_NAME, userList.name);
        } else {
            tabArgs.putLong(EXTRA_ACCOUNT_ID, args.getLong(EXTRA_ACCOUNT_ID, -1));
            tabArgs.putLong(EXTRA_USER_ID, args.getLong(EXTRA_USER_ID, -1));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
            tabArgs.putLong(EXTRA_LIST_ID, args.getLong(EXTRA_LIST_ID, -1));
            tabArgs.putString(EXTRA_LIST_NAME, args.getString(EXTRA_LIST_NAME));
        }
        mPagerAdapter.addTab(UserListTimelineFragment.class, tabArgs, getString(R.string.statuses), null, 0, null);
        mPagerAdapter.addTab(UserListMembersFragment.class, tabArgs, getString(R.string.members), null, 1, null);
        mPagerAdapter.addTab(UserListSubscribersFragment.class, tabArgs, getString(R.string.subscribers), null, 2, null);

        final FragmentActivity activity = getActivity();
        ThemeUtils.initPagerIndicatorAsActionBarTab(activity, mPagerIndicator, mPagerOverlay);
        ThemeUtils.setCompatToolbarOverlay(activity, new EmptyDrawable());
        ThemeUtils.setCompatContentViewOverlay(activity, new EmptyDrawable());
        ThemeUtils.setWindowOverlayViewOverlay(activity, new EmptyDrawable());

        if (activity instanceof IThemedActivity) {
            final String backgroundOption = ((IThemedActivity) activity).getCurrentThemeBackgroundOption();
            final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
            final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(activity)) : 0xFF;
            mPagerIndicator.setAlpha(actionBarAlpha / 255f);
        }
    }

    public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        private MaterialEditText mEditName, mEditDescription;
        private CheckBox mPublicCheckBox;
        private String mName, mDescription;
        private long mAccountId;
        private long mListId;
        private boolean mIsPublic;

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            if (mAccountId <= 0) return;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    mName = ParseUtils.parseString(mEditName.getText());
                    mDescription = ParseUtils.parseString(mEditDescription.getText());
                    mIsPublic = mPublicCheckBox.isChecked();
                    if (mName == null || mName.length() <= 0) return;
                    final UserListUpdate update = new UserListUpdate();
                    update.setMode(mIsPublic ? UserList.Mode.PUBLIC : UserList.Mode.PRIVATE);
                    update.setName(mName);
                    update.setDescription(mDescription);
                    mTwitterWrapper.updateUserListDetails(mAccountId, mListId, update);
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
            mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
            mListId = bundle != null ? bundle.getLong(EXTRA_LIST_ID, -1) : -1;
            mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
            mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
            mIsPublic = bundle == null || bundle.getBoolean(EXTRA_IS_PUBLIC, true);
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            final View view = LayoutInflater.from(wrapped).inflate(R.layout.dialog_user_list_detail_editor, null);
            builder.setView(view);
            mEditName = (MaterialEditText) view.findViewById(R.id.name);
            mEditName.addValidator(new UserListNameValidator(getString(R.string.invalid_list_name)));
            mEditDescription = (MaterialEditText) view.findViewById(R.id.description);
            mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
            if (mName != null) {
                mEditName.setText(mName);
            }
            if (mDescription != null) {
                mEditDescription.setText(mDescription);
            }
            mPublicCheckBox.setChecked(mIsPublic);
            builder.setTitle(R.string.user_list);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(final Bundle outState) {
            outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
            outState.putLong(EXTRA_LIST_ID, mListId);
            outState.putString(EXTRA_LIST_NAME, mName);
            outState.putString(EXTRA_DESCRIPTION, mDescription);
            outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
            super.onSaveInstanceState(outState);
        }

    }

    static class ParcelableUserListLoader extends AsyncTaskLoader<SingleResponse<ParcelableUserList>> {

        private final boolean mOmitIntentExtra;
        private final Bundle mExtras;
        private final long mAccountId, mUserId;
        private final long mListId;
        private final String mScreenName, mListName;

        private ParcelableUserListLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
                                         final long accountId, final long listId, final String listName, final long userId,
                                         final String screenName) {
            super(context);
            mOmitIntentExtra = omitIntentExtra;
            mExtras = extras;
            mAccountId = accountId;
            mUserId = userId;
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
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList list;
                if (mListId > 0) {
                    list = twitter.showUserList(mListId);
                } else if (mUserId > 0) {
                    list = twitter.showUserList(mListName, mUserId);
                } else if (mScreenName != null) {
                    list = twitter.showUserList(mListName, mScreenName);
                } else
                    return SingleResponse.getInstance();
                return SingleResponse.getInstance(new ParcelableUserList(list, mAccountId));
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
