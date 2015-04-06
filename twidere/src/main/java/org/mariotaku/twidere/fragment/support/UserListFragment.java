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
import android.support.v7.widget.CardView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ColorLabelLinearLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.TabPagerIndicator;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;

public class UserListFragment extends BaseSupportFragment implements OnClickListener,
        LoaderCallbacks<SingleResponse<ParcelableUserList>>, DrawerCallback,
        SystemWindowsInsetsCallback, SupportFragmentCallback {

    private MediaLoaderWrapper mProfileImageLoader;
    private AsyncTwitterWrapper mTwitterWrapper;

    private ImageView mProfileImageView;
    private TextView mListNameView, mCreatedByView, mDescriptionView, mErrorMessageView;
    private View mErrorRetryContainer, mProgressContainer;
    private ColorLabelLinearLayout mUserListDetails;
    private Button mRetryButton;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private CardView mCardView;

    private SupportTabsAdapter mPagerAdapter;

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
    private boolean mUserListLoaderInitialized;
    private Fragment mCurrentVisibleFragment;

    @Override
    public boolean canScroll(float dy) {
        final Fragment fragment = mCurrentVisibleFragment;
        return fragment instanceof DrawerCallback && ((DrawerCallback) fragment).canScroll(dy);
    }

    @Override
    public void cancelTouch() {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).cancelTouch();
        }
    }

    @Override
    public void fling(float velocity) {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).fling(velocity);
        }
    }

    @Override
    public boolean isScrollContent(float x, float y) {
        final ViewPager v = mViewPager;
        final int[] location = new int[2];
        v.getLocationOnScreen(location);
        return x >= location[0] && x <= location[0] + v.getWidth()
                && y >= location[1] && y <= location[1] + v.getHeight();
    }

    @Override
    public void scrollBy(float dy) {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).scrollBy(dy);
        }
    }

    @Override
    public boolean shouldLayoutHeaderBottom() {
        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        final CardView card = mCardView;
        if (drawer == null || card == null) return false;
        return card.getTop() + drawer.getHeaderTop() - drawer.getPaddingTop() <= 0;
    }

    @Override
    public void topChanged(int offset) {

    }

    public void displayUserList(final ParcelableUserList userList) {
        if (userList == null || getActivity() == null) return;
        getLoaderManager().destroyLoader(0);
        mErrorRetryContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
        mUserList = userList;
        mUserListDetails.drawEnd(getAccountColor(getActivity(), userList.account_id));
        mListNameView.setText(userList.name);
        final String displayName = UserColorNameUtils.getDisplayName(getActivity(), userList.user_id,
                userList.user_name, userList.user_screen_name, false);
        mCreatedByView.setText(getString(R.string.created_by, displayName));
        final String description = userList.description;
        mDescriptionView.setVisibility(isEmpty(description) ? View.GONE : View.VISIBLE);
        mDescriptionView.setText(description);
        final TwidereLinkify linkify = new TwidereLinkify(new OnLinkClickHandler(getActivity(),
                getMultiSelectManager()));
        linkify.applyAllLinks(mDescriptionView, userList.account_id, false);
        mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        mProfileImageLoader.displayProfileImage(mProfileImageView, userList.user_profile_image_url);
        invalidateOptionsMenu();
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    @Override
    public void onDetachFragment(Fragment fragment) {

    }

    @Override
    public void onSetUserVisibleHint(Fragment fragment, boolean isVisibleToUser) {
        mCurrentVisibleFragment = isVisibleToUser ? fragment : null;
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
        final View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        final ViewGroup listDetailsContainer = (ViewGroup) view.findViewById(R.id.list_details_container);
        final boolean isCompact = Utils.isCompactCards(getActivity());
        if (isCompact) {
            inflater.inflate(R.layout.layout_user_list_details_compact, listDetailsContainer);
        } else {
            inflater.inflate(R.layout.layout_user_list_details, listDetailsContainer);
        }
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        final FragmentActivity activity = getActivity();

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

        mHeaderDrawerLayout.setDrawerCallback(this);

        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);
        if (activity instanceof IThemedActivity) {
            mPagerIndicator.setStripColor(((IThemedActivity) activity).getCurrentThemeColor());
        } else {

        }

        mTwitterWrapper = getApplication().getTwitterWrapper();
        mProfileImageLoader = getApplication().getMediaLoaderWrapper();
        mProfileImageView.setOnClickListener(this);
        mUserListDetails.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
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
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUserList userList = mUserList;
        final MenuItem followItem = menu.findItem(MENU_FOLLOW);
        if (followItem != null) {
            followItem.setEnabled(userList != null);
            if (userList == null) {
                followItem.setIcon(android.R.color.transparent);
            }
        }
        if (twitter == null || userList == null) return;
        final boolean isMyList = userList.user_id == userList.account_id;
        setMenuItemAvailability(menu, MENU_EDIT, isMyList);
        setMenuItemAvailability(menu, MENU_ADD, isMyList);
        setMenuItemAvailability(menu, MENU_DELETE, isMyList);
        final boolean isFollowing = userList.is_following;
        if (followItem != null) {
            followItem.setVisible(!isMyList);
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_action_cancel);
                followItem.setTitle(R.string.unsubscribe);
            } else {
                followItem.setIcon(R.drawable.ic_action_add);
                followItem.setTitle(R.string.subscribe);
            }
        }
        menu.removeGroup(MENU_GROUP_USER_LIST_EXTENSION);
        final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
        extensionsIntent.setExtrasClassLoader(getActivity().getClassLoader());
        extensionsIntent.putExtra(EXTRA_USER_LIST, mUserList);
        addIntentToMenu(getActivity(), menu, extensionsIntent, MENU_GROUP_USER_LIST_EXTENSION);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUserList userList = mUserList;
        if (twitter == null || userList == null) return false;
        switch (item.getItemId()) {
            case MENU_ADD: {
                if (userList.user_id != userList.account_id) return false;
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_ID, userList.account_id);
                startActivityForResult(intent, REQUEST_SELECT_USER);
                break;
            }
            case MENU_DELETE: {
                if (userList.user_id != userList.account_id) return false;
                DestroyUserListDialogFragment.show(getFragmentManager(), userList);
                break;
            }
            case MENU_EDIT: {
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
            case MENU_FOLLOW: {
                if (userList.is_following) {
                    DestroyUserListSubscriptionDialogFragment.show(getFragmentManager(), userList);
                } else {
                    twitter.createUserListSubscriptionAsync(userList.account_id, userList.id);
                }
                return true;
            }
            case MENU_OPEN_WITH_ACCOUNT: {
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
            case R.id.retry: {
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
        mErrorMessageView.setText(null);
        mErrorMessageView.setVisibility(View.GONE);
        mErrorRetryContainer.setVisibility(View.GONE);
        mHeaderDrawerLayout.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.VISIBLE);
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
        if (data.getData() != null) {
            final ParcelableUserList list = data.getData();
            displayUserList(list);
            mHeaderDrawerLayout.setVisibility(View.VISIBLE);
            mErrorRetryContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.GONE);
        } else {
            if (data.hasException()) {
                mErrorMessageView.setText(data.getException().getMessage());
                mErrorMessageView.setVisibility(View.VISIBLE);
            }
            mHeaderDrawerLayout.setVisibility(View.GONE);
            mErrorRetryContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        }
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mHeaderDrawerLayout = (HeaderDrawerLayout) view.findViewById(R.id.details_container);
        mErrorRetryContainer = view.findViewById(R.id.error_retry_container);
        mProgressContainer = view.findViewById(R.id.progress_container);

        final View headerView = mHeaderDrawerLayout.getHeader();
        final View contentView = mHeaderDrawerLayout.getContent();
        mCardView = (CardView) headerView.findViewById(R.id.card);
        mUserListDetails = (ColorLabelLinearLayout) headerView.findViewById(R.id.user_list_details);
        mListNameView = (TextView) headerView.findViewById(R.id.list_name);
        mCreatedByView = (TextView) headerView.findViewById(R.id.created_by);
        mDescriptionView = (TextView) headerView.findViewById(R.id.description);
        mProfileImageView = (ImageView) headerView.findViewById(R.id.profile_image);
        mRetryButton = (Button) mErrorRetryContainer.findViewById(R.id.retry);
        mErrorMessageView = (TextView) mErrorRetryContainer.findViewById(R.id.error_message);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) contentView.findViewById(R.id.view_pager_tabs);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View progress = mProgressContainer, error = mErrorRetryContainer;
        final HeaderDrawerLayout content = mHeaderDrawerLayout;
        if (progress == null || error == null || content == null) {
            return;
        }
        progress.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        error.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        content.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        content.setClipToPadding(false);
    }

    private void setupUserPages() {
        final Context context = getActivity();
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
        mPagerIndicator.notifyDataSetChanged();
    }

    public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        private EditText mEditName, mEditDescription;
        private CheckBox mPublicCheckBox;
        private String mName, mDescription;
        private long mAccountId;
        private long mListId;
        private boolean mIsPublic;
        private AsyncTwitterWrapper mTwitterWrapper;

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            if (mAccountId <= 0) return;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    mName = ParseUtils.parseString(mEditName.getText());
                    mDescription = ParseUtils.parseString(mEditDescription.getText());
                    mIsPublic = mPublicCheckBox.isChecked();
                    if (mName == null || mName.length() <= 0) return;
                    mTwitterWrapper.updateUserListDetails(mAccountId, mListId, mIsPublic, mName, mDescription);
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            mTwitterWrapper = getApplication().getTwitterWrapper();
            final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
            mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
            mListId = bundle != null ? bundle.getLong(EXTRA_LIST_ID, -1) : -1;
            mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
            mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
            mIsPublic = bundle == null || bundle.getBoolean(EXTRA_IS_PUBLIC, true);
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            final View view = LayoutInflater.from(wrapped).inflate(R.layout.edit_user_list_detail, null);
            builder.setView(view);
            mEditName = (EditText) view.findViewById(R.id.name);
            mEditDescription = (EditText) view.findViewById(R.id.description);
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
            final Twitter twitter = getTwitterInstance(getContext(), mAccountId, true);
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
