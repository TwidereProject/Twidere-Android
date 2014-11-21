/*
 * Twidere - Twitter client for Android
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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.ListActionAdapter;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ListAction;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.FlymeUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.view.CircularImageView;
import org.mariotaku.twidere.view.ColorLabelLinearLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ProfileBannerImageView;
import org.mariotaku.twidere.view.TwidereMenuBar;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.util.Locale;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseLong;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getErrorMessage;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getOriginalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openIncomingFriendships;
import static org.mariotaku.twidere.util.Utils.openMutesUsers;
import static org.mariotaku.twidere.util.Utils.openSavedSearches;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFriends;
import static org.mariotaku.twidere.util.Utils.openUserListMemberships;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserMediaTimeline;
import static org.mariotaku.twidere.util.Utils.openUserMentions;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUserTimeline;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.showInfoMessage;

public class UserProfileFragmentOld extends BaseSupportListFragment implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener, OnMenuItemClickListener, OnLinkClickListener, Panes.Right, OnSizeChangedListener,
        OnSharedPreferenceChangeListener, OnTouchListener {

    private static final int LOADER_ID_USER = 1;
    private static final int LOADER_ID_FRIENDSHIP = 2;

    private ImageLoaderWrapper mProfileImageLoader;
    private SharedPreferences mPreferences;

    private CircularImageView mProfileImageView;
    private ProfileBannerImageView mProfileBannerView;
    private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
            mTweetCount, mFollowersCount, mFriendsCount, mErrorMessageView;
    private View mDescriptionContainer, mLocationContainer, mURLContainer, mTweetsContainer, mFollowersContainer,
            mFriendsContainer;
    private Button mRetryButton;
    private ColorLabelLinearLayout mProfileNameContainer;
    private ListView mListView;
    private View mHeaderView;
    private View mErrorRetryContainer;
    private View mFollowingYouIndicator;
    private View mMainContent;
    private ProgressBar mDetailsLoadProgress;
    private TwidereMenuBar mMenuBar;
    private View mProfileBannerSpace;

    private ListActionAdapter mAdapter;

    private Relationship mRelationship;
    private ParcelableUser mUser = null;
    private Locale mLocale;

    private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;

    private int mBannerWidth;

    private Drawable mActionBarShadow;
    private Drawable mActionBarBackground;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            if (mUser == null) return;
            final String action = intent.getAction();
            switch (action) {
                case BROADCAST_FRIENDSHIP_CHANGED: {
                    if (intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id) {
                        getFriendship();
                    }
                    break;
                }
                case BROADCAST_PROFILE_UPDATED:
                case BROADCAST_PROFILE_IMAGE_UPDATED:
                case BROADCAST_PROFILE_BANNER_UPDATED: {
                    if (intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id) {
                        getUserInfo(true);
                    }
                    break;
                }
                case BROADCAST_TASK_STATE_CHANGED: {
                    final AsyncTwitterWrapper twitter = getTwitterWrapper();
                    final boolean is_creating_friendship = twitter != null
                            && twitter.isCreatingFriendship(mUser.account_id, mUser.id);
                    final boolean is_destroying_friendship = twitter != null
                            && twitter.isDestroyingFriendship(mUser.account_id, mUser.id);
                    setProgressBarIndeterminateVisibility(is_creating_friendship || is_destroying_friendship);
                    invalidateOptionsMenu();
                    break;
                }
            }
        }
    };

    private final LoaderCallbacks<SingleResponse<ParcelableUser>> mUserInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableUser>>() {

        @Override
        public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
            if (mUser == null) {
                mMainContent.setVisibility(View.GONE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mDetailsLoadProgress.setVisibility(View.VISIBLE);
                mErrorMessageView.setText(null);
                mErrorMessageView.setVisibility(View.GONE);
            }
            setProgressBarIndeterminateVisibility(true);
            final ParcelableUser user = mUser;
            final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            return new ParcelableUserLoader(getActivity(), accountId, userId, screenName, getArguments(),
                    omitIntentExtra, user == null || !user.is_cache && userId != user.id);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
                                   final SingleResponse<ParcelableUser> data) {
            if (getActivity() == null) return;
            if (data.getData() != null && data.getData().id > 0) {
                final ParcelableUser user = data.getData();
                displayUser(user);
                mMainContent.setVisibility(View.VISIBLE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mDetailsLoadProgress.setVisibility(View.GONE);
                if (user.is_cache) {
                    final Bundle args = new Bundle();
                    args.putLong(EXTRA_ACCOUNT_ID, user.account_id);
                    args.putLong(EXTRA_USER_ID, user.id);
                    args.putString(EXTRA_SCREEN_NAME, user.screen_name);
                    args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
                    getLoaderManager().restartLoader(LOADER_ID_USER, args, this);
                }
            } else if (mUser != null && mUser.is_cache) {
                mMainContent.setVisibility(View.VISIBLE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mDetailsLoadProgress.setVisibility(View.GONE);
                displayUser(mUser);
            } else {
                if (data.hasException()) {
                    mErrorMessageView.setText(getErrorMessage(getActivity(), data.getException()));
                    mErrorMessageView.setVisibility(View.VISIBLE);
                }
                mMainContent.setVisibility(View.GONE);
                mErrorRetryContainer.setVisibility(View.VISIBLE);
            }
            setProgressBarIndeterminateVisibility(false);
        }

    };

    private final LoaderCallbacks<SingleResponse<Relationship>> mFriendshipLoaderCallbacks = new LoaderCallbacks<SingleResponse<Relationship>>() {

        @Override
        public Loader<SingleResponse<Relationship>> onCreateLoader(final int id, final Bundle args) {
            invalidateOptionsMenu();
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            return new FriendshipLoader(getActivity(), accountId, userId);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<Relationship>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<Relationship>> loader,
                                   final SingleResponse<Relationship> data) {
            mRelationship = null;
            final ParcelableUser user = mUser;
            final Relationship relationship = mRelationship = data.getData();
            if (user == null) return;
            invalidateOptionsMenu();
            setMenu(mMenuBar.getMenu());
            mMenuBar.show();
            if (relationship != null) {
                final boolean isMyself = user.account_id == user.id;
                final boolean isFollowingYou = relationship.isTargetFollowingSource();
                mFollowingYouIndicator.setVisibility(!isMyself && isFollowingYou ? View.VISIBLE : View.GONE);
                final ContentResolver resolver = getContentResolver();
                final String where = Where.equals(CachedUsers.USER_ID, user.id).getSQL();
                resolver.delete(CachedUsers.CONTENT_URI, where, null);
                // I bet you don't want to see blocked user in your auto
                // complete list.
                if (!data.getData().isSourceBlockingTarget()) {
                    final ContentValues cachedValues = ParcelableUser.makeCachedUserContentValues(user);
                    if (cachedValues != null) {
                        resolver.insert(CachedUsers.CONTENT_URI, cachedValues);
                    }
                }
            } else {
                mFollowingYouIndicator.setVisibility(View.GONE);
            }
        }

    };

    public void displayUser(final ParcelableUser user) {
        mRelationship = null;
        mUser = null;
        mAdapter.clear();
        if (user == null || user.id <= 0 || getActivity() == null) return;
        final Resources res = getResources();
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final boolean userIsMe = user.account_id == user.id;
        mErrorRetryContainer.setVisibility(View.GONE);
        mUser = user;
        mProfileNameContainer.drawStart(getUserColor(getActivity(), user.id, true));
        mProfileNameContainer.drawEnd(getAccountColor(getActivity(), user.account_id));
        final String nick = getUserNickname(getActivity(), user.id, true);
        mNameView
                .setText(TextUtils.isEmpty(nick) ? user.name : getString(R.string.name_with_nickname, user.name, nick));
//        mProfileImageView.setUserType(user.is_verified, user.is_protected);
        mScreenNameView.setText("@" + user.screen_name);
        mDescriptionContainer.setVisibility(userIsMe || !isEmpty(user.description_html) ? View.VISIBLE : View.GONE);
        mDescriptionView.setText(user.description_html != null ? Html.fromHtml(user.description_html) : null);
        final TwidereLinkify linkify = new TwidereLinkify(this);
        linkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(getActivity()));
        linkify.applyAllLinks(mDescriptionView, user.account_id, false);
        mDescriptionView.setMovementMethod(null);
        mLocationContainer.setVisibility(userIsMe || !isEmpty(user.location) ? View.VISIBLE : View.GONE);
        mLocationView.setText(user.location);
        mURLContainer.setVisibility(userIsMe || !isEmpty(user.url) || !isEmpty(user.url_expanded) ? View.VISIBLE
                : View.GONE);
        mURLView.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
        mURLView.setMovementMethod(null);
        final String createdAt = formatToLongTimeString(getActivity(), user.created_at);
        final float daysSinceCreated = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24;
        final int dailyTweets = Math.round(user.statuses_count / Math.max(1, daysSinceCreated));
        mCreatedAtView.setText(res.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                createdAt, dailyTweets));
        mTweetCount.setText(getLocalizedNumber(mLocale, user.statuses_count));
        mFollowersCount.setText(getLocalizedNumber(mLocale, user.followers_count));
        mFriendsCount.setText(getLocalizedNumber(mLocale, user.friends_count));
        if (mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
            mProfileImageLoader.displayProfileImage(mProfileImageView,
                    getOriginalTwitterProfileImage(user.profile_image_url));
            final int def_width = res.getDisplayMetrics().widthPixels;
            final int width = mBannerWidth > 0 ? mBannerWidth : def_width;
            mProfileBannerView.setImageBitmap(null);
            mProfileImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
        } else {
            mProfileImageView.setImageResource(R.drawable.ic_profile_image_default);
            mProfileBannerView.setImageResource(android.R.color.transparent);
        }
        if (isMyAccount(getActivity(), user.id)) {
            final ContentResolver resolver = getContentResolver();
            final ContentValues values = new ContentValues();
            values.put(Accounts.NAME, user.name);
            values.put(Accounts.SCREEN_NAME, user.screen_name);
            values.put(Accounts.PROFILE_IMAGE_URL, user.profile_image_url);
            values.put(Accounts.PROFILE_BANNER_URL, user.profile_banner_url);
            final String where = Accounts.ACCOUNT_ID + " = " + user.id;
            resolver.update(Accounts.CONTENT_URI, values, where, null);
        }
        if (Utils.isOfficialKeyAccount(getActivity(), user.account_id)) {
            mAdapter.add(new MediaTimelineAction(1));
        }
        mAdapter.add(new FavoritesAction(2));
        mAdapter.add(new UserMentionsAction(3));
        mAdapter.add(new UserListsAction(4));
        mAdapter.add(new UserListMembershipsAction(5));
        if (userIsMe) {
            mAdapter.add(new SavedSearchesAction(11));
            if (user.is_protected) {
                mAdapter.add(new IncomingFriendshipsAction(12));
            }
            mAdapter.add(new UserBlocksAction(13));
            mAdapter.add(new MutesUsersAction(14));
        }
        mAdapter.notifyDataSetChanged();
        if (!user.is_cache) {
            getFriendship();
        }
        invalidateOptionsMenu();
        setMenu(mMenuBar.getMenu());
        mMenuBar.show();
    }

    public void getUserInfo(final long accountId, final long userId, final String screenName,
                            final boolean omitIntentExtra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        if (!isMyAccount(getActivity(), accountId)) {
            mMainContent.setVisibility(View.GONE);
            mErrorRetryContainer.setVisibility(View.GONE);
            return;
        }
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
        args.putLong(EXTRA_USER_ID, userId);
        args.putString(EXTRA_SCREEN_NAME, screenName);
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
        if (!mGetUserInfoLoaderInitialized) {
            lm.initLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
            mGetUserInfoLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
        }
        if (accountId == -1 || userId == -1 && screenName == null) {
            mMainContent.setVisibility(View.GONE);
            mErrorRetryContainer.setVisibility(View.GONE);
            return;
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        mLocale = getResources().getConfiguration().locale;
        final Bundle args = getArguments();
        long accountId = -1, userId = -1;
        String screenName = null;
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        } else {
            accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            userId = args.getLong(EXTRA_USER_ID, -1);
            screenName = args.getString(EXTRA_SCREEN_NAME);
        }
        mProfileImageLoader = getApplication().getImageLoaderWrapper();
        final FragmentActivity activity = getActivity();
        mAdapter = new ListActionAdapter(activity);
        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mTweetsContainer.setOnClickListener(this);
        mFollowersContainer.setOnClickListener(this);
        mFriendsContainer.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        mProfileBannerView.setOnSizeChangedListener(this);
        mProfileBannerSpace.setOnTouchListener(this);
        setListAdapter(null);
        mListView.addHeaderView(mHeaderView, null, false);
        if (isUucky(userId, screenName, args.getParcelable(EXTRA_USER))) {
            final View uuckyFooter = View.inflate(activity,
                    R.layout.list_footer_user_profile_uucky, null);
            mListView.addFooterView(uuckyFooter, null, false);
        }
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mMenuBar.setVisibility(shouldUseNativeMenu() ? View.GONE : View.VISIBLE);
        mMenuBar.inflate(R.menu.menu_user_profile);
        mMenuBar.setIsBottomBar(true);
        mMenuBar.setOnMenuItemClickListener(this);

        setListAdapter(mAdapter);
        getUserInfo(accountId, userId, screenName, false);

        setupBars();
    }

    private void setupBars() {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof LinkHandlerActivity)) return;
        final LinkHandlerActivity linkHandler = (LinkHandlerActivity) activity;
        final ActionBar actionBar = linkHandler.getActionBar();
        if (actionBar == null) return;
        final int themeColor = linkHandler.getThemeColor();
        final int themeResId = linkHandler.getCurrentThemeResourceId();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(themeResId);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getUserThemeBackgroundAlpha(linkHandler) : 0xFF;
        mActionBarShadow = activity.getResources().getDrawable(R.drawable.shadow_user_banner_action_bar);
        if (mActionBarShadow instanceof ShapeDrawable) {
            final ShapeDrawable sd = (ShapeDrawable) mActionBarBackground;
            sd.setIntrinsicHeight(actionBar.getHeight());
            sd.setIntrinsicWidth(activity.getWindowManager().getDefaultDisplay().getWidth());
        }
        if (ThemeUtils.isColoredActionBar(themeResId) && useUserActionBar()) {
            mActionBarBackground = new ColorDrawable(themeColor);
        } else {
            mActionBarBackground = ThemeUtils.getActionBarBackground(activity, themeResId);
        }
        actionBar.setBackgroundDrawable(new ActionBarDrawable(mActionBarShadow, mActionBarBackground));
    }

    private boolean useUserActionBar() {
        return false;
    }

    private boolean isUucky(long userId, String screenName, Parcelable parcelable) {
        if (userId == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(screenName)) return true;
        if (parcelable instanceof ParcelableUser) {
            final ParcelableUser user = (ParcelableUser) parcelable;
            return user.id == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(user.screen_name);
        }
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final ParcelableUser user = mUser;
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
                    setUserColor(getActivity(), mUser.id, color);
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    clearUserColor(getActivity(), mUser.id);
                }
                break;
            }
            case REQUEST_ADD_TO_LIST: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final AsyncTwitterWrapper twitter = getTwitterWrapper();
                    final ParcelableUserList list = data.getParcelableExtra(EXTRA_USER_LIST);
                    if (list == null || twitter == null) return;
                    twitter.addUserListMembersAsync(user.account_id, list.id, user);
                }
                break;
            }
            case REQUEST_SELECT_ACCOUNT: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final long accountId = data.getLongExtra(EXTRA_ID, -1);
                    openUserProfile(getActivity(), accountId, user.id, null);
                }
                break;
            }
        }

    }

    @Override
    public void onClick(final View view) {
        final FragmentActivity activity = getActivity();
        final ParcelableUser user = mUser;
        if (activity == null || user == null) return;
        switch (view.getId()) {
            case R.id.retry: {
                getUserInfo(true);
                break;
            }
            case R.id.profile_image: {
                final String profile_image_url_string = getOriginalTwitterProfileImage(mUser.profile_image_url);
                openImage(activity, user.account_id, profile_image_url_string, false);
                break;
            }
            case R.id.profile_banner: {
                final String profile_banner_url = mUser.profile_banner_url;
                if (profile_banner_url == null) return;
                openImage(getActivity(), user.account_id, profile_banner_url + "/ipad_retina", false);
                break;
            }
            case R.id.tweets_container: {
                openUserTimeline(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.followers_container: {
                openUserFollowers(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.friends_container: {
                openUserFriends(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.name_container: {
                if (user.account_id != user.id) return;
                startActivity(new Intent(getActivity(), UserProfileEditorActivity.class));
                break;
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (!shouldUseNativeMenu()) return;
        inflater.inflate(R.menu.menu_user_profile, menu);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_details_page, null, false);
        final ExtendedFrameLayout detailsContainer = (ExtendedFrameLayout) view.findViewById(R.id.details_container);
        inflater.inflate(R.layout.header_user_profile_banner, detailsContainer, true);
        detailsContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
        mHeaderView = inflater.inflate(R.layout.header_user_profile, null, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        mUser = null;
        mRelationship = null;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
        final ListAction action = mAdapter.findItem(id);
        if (action != null) {
            action.onClick();
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
        final ListAction action = mAdapter.findItem(id);
        return action != null && action.onLongClick();
    }

    @Override
    public void onLinkClick(final String link, final String orig, final long account_id, final int type,
                            final boolean sensitive) {
        final ParcelableUser user = mUser;
        if (user == null) return;
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION: {
                openUserProfile(getActivity(), user.account_id, -1, link);
                break;
            }
            case TwidereLinkify.LINK_TYPE_HASHTAG: {
                openTweetSearch(getActivity(), user.account_id, "#" + link);
                break;
            }
            case TwidereLinkify.LINK_TYPE_LINK: {
                final Uri uri = Uri.parse(link);
                final Intent intent;
                if (uri.getScheme() != null) {
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, uri.buildUpon().scheme("http").build());
                }
                startActivity(intent);
                break;
            }
            case TwidereLinkify.LINK_TYPE_LIST: {
                if (link == null) break;
                final String[] mentionList = link.split("/");
                if (mentionList.length != 2) {
                    break;
                }
                break;
            }
            case TwidereLinkify.LINK_TYPE_STATUS: {
                openStatus(getActivity(), account_id, parseLong(link));
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return handleMenuItemClick(item);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return handleMenuItemClick(item);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        if (!shouldUseNativeMenu() || !menu.hasVisibleItems()) return;
        setMenu(menu);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onScrollDistanceChanged(int delta, int total) {
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                         final int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        final View headerView = mHeaderView;
        final ProfileBannerImageView profileBannerView = mProfileBannerView;
        final ListView listView = mListView;
        if (headerView == null || profileBannerView == null || listView == null) return;
        final float factor = -headerView.getTop() / (float) headerView.getHeight();
        final int headerScroll = listView.getListPaddingTop() - headerView.getTop();
        profileBannerView.setAlpha(1.0f - factor);
        profileBannerView.setTranslationY((headerView.getTop() - listView.getListPaddingTop()) / 2);
        profileBannerView.setBottomClip(headerScroll);

        if (mActionBarShadow != null && mActionBarBackground != null) {
            final float f = headerScroll / (float) mProfileBannerSpace.getHeight();
            mActionBarShadow.setAlpha(Math.round(0xFF * MathUtils.clamp(1 - f, 0, 1)));
            mActionBarBackground.setAlpha(Math.round(0xFF * MathUtils.clamp(f, 0, 1)));
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (mUser == null || !ParseUtils.parseString(mUser.id).equals(key)) return;
        displayUser(mUser);
    }

    @Override
    public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
        mBannerWidth = w;
        if (w != oldw || h != oldh) {
            requestFitSystemWindows();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
        filter.addAction(BROADCAST_FRIENDSHIP_CHANGED);
        filter.addAction(BROADCAST_PROFILE_UPDATED);
        filter.addAction(BROADCAST_PROFILE_IMAGE_UPDATED);
        filter.addAction(BROADCAST_PROFILE_BANNER_UPDATED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        return mProfileBannerView.dispatchTouchEvent(event);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final ViewGroup.LayoutParams params = mProfileBannerSpace.getLayoutParams();
        params.height = Math.max(insets.top, mProfileBannerView.getHeight() - insets.top);
        mProfileBannerSpace.setLayoutParams(params);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        final Context context = view.getContext();
        super.onViewCreated(view, savedInstanceState);
        mMainContent = view.findViewById(R.id.content);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mDetailsLoadProgress = (ProgressBar) view.findViewById(R.id.details_load_progress);
        mMenuBar = (TwidereMenuBar) view.findViewById(R.id.menu_bar);
        mErrorRetryContainer = view.findViewById(R.id.error_retry_container);
        mRetryButton = (Button) view.findViewById(R.id.retry);
        mErrorMessageView = (TextView) view.findViewById(R.id.error_message);
        mProfileBannerView = (ProfileBannerImageView) view.findViewById(R.id.profile_banner);
        mNameView = (TextView) mHeaderView.findViewById(R.id.name);
        mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
        mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
        mLocationView = (TextView) mHeaderView.findViewById(R.id.location);
        mURLView = (TextView) mHeaderView.findViewById(R.id.url);
        mCreatedAtView = (TextView) mHeaderView.findViewById(R.id.created_at);
        mTweetsContainer = mHeaderView.findViewById(R.id.tweets_container);
        mTweetCount = (TextView) mHeaderView.findViewById(R.id.statuses_count);
        mFollowersContainer = mHeaderView.findViewById(R.id.followers_container);
        mFollowersCount = (TextView) mHeaderView.findViewById(R.id.followers_count);
        mFriendsContainer = mHeaderView.findViewById(R.id.friends_container);
        mFriendsCount = (TextView) mHeaderView.findViewById(R.id.friends_count);
        mProfileNameContainer = (ColorLabelLinearLayout) mHeaderView.findViewById(R.id.profile_name_container);
        mProfileImageView = (CircularImageView) mHeaderView.findViewById(R.id.profile_image);
        mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
        mLocationContainer = mHeaderView.findViewById(R.id.location_container);
        mURLContainer = mHeaderView.findViewById(R.id.url_container);
        mFollowingYouIndicator = mHeaderView.findViewById(R.id.following_you_indicator);
        mProfileBannerSpace = mHeaderView.findViewById(R.id.profile_banner_space);
        final View cardView = mHeaderView.findViewById(R.id.card);
        ThemeUtils.applyThemeAlphaToDrawable(context, cardView.getBackground());
    }

    private void getFriendship() {
        final ParcelableUser user = mUser;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, user.account_id);
        args.putLong(EXTRA_USER_ID, user.id);
        if (!mGetFriendShipLoaderInitialized) {
            lm.initLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks);
            mGetFriendShipLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks);
        }
    }

    private void getUserInfo(final boolean omitIntentExtra) {
        final ParcelableUser user = mUser;
        if (user == null) return;
        getUserInfo(user.account_id, user.id, user.screen_name, omitIntentExtra);
    }

    private boolean handleMenuItemClick(final MenuItem item) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUser user = mUser;
        final Relationship relationship = mRelationship;
        if (user == null || twitter == null) return false;
        switch (item.getItemId()) {
            case MENU_BLOCK: {
                if (mRelationship != null) {
                    if (mRelationship.isSourceBlockingTarget()) {
                        twitter.destroyBlockAsync(user.account_id, user.id);
                    } else {
                        CreateUserBlockDialogFragment.show(getFragmentManager(), user);
                    }
                }
                break;
            }
            case MENU_REPORT_SPAM: {
                ReportSpamDialogFragment.show(getFragmentManager(), user);
                break;
            }
            case MENU_ADD_TO_FILTER: {
                final boolean filtering = Utils.isFilteringUser(getActivity(), user.id);
                final ContentResolver cr = getContentResolver();
                if (filtering) {
                    final Where where = Where.equals(Filters.Users.USER_ID, user.id);
                    cr.delete(Filters.Users.CONTENT_URI, where.getSQL(), null);
                    showInfoMessage(getActivity(), R.string.message_user_unmuted, false);
                } else {
                    cr.insert(Filters.Users.CONTENT_URI, ContentValuesCreator.makeFilteredUserContentValues(user));
                    showInfoMessage(getActivity(), R.string.message_user_muted, false);
                }
                setMenu(mMenuBar.getMenu());
                break;
            }
            case MENU_MUTE_USER: {
                if (mRelationship != null) {
                    if (mRelationship.isSourceMutingTarget()) {
                        twitter.destroyMuteAsync(user.account_id, user.id);
                    } else {
                        CreateUserMuteDialogFragment.show(getFragmentManager(), user);
                    }
                }
                break;
            }
            case MENU_MENTION: {
                final Intent intent = new Intent(INTENT_ACTION_MENTION);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case MENU_SEND_DIRECT_MESSAGE: {
                final Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME_TWIDERE);
                builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
                builder.appendQueryParameter(QUERY_PARAM_RECIPIENT_ID, String.valueOf(user.id));
                startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
                break;
            }
            case MENU_SET_COLOR: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, getUserColor(getActivity(), user.id, true));
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                intent.putExtra(EXTRA_CLEAR_BUTTON, true);
                startActivityForResult(intent, REQUEST_SET_COLOR);
                break;
            }
            case MENU_CLEAR_NICKNAME: {
                clearUserNickname(getActivity(), user.id);
                break;
            }
            case MENU_SET_NICKNAME: {
                final String nick = getUserNickname(getActivity(), user.id, true);
                SetUserNicknameDialogFragment.show(getFragmentManager(), user.id, nick);
                break;
            }
            case MENU_ADD_TO_LIST: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER_LIST);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_ID, user.account_id);
                intent.putExtra(EXTRA_SCREEN_NAME, getAccountScreenName(getActivity(), user.account_id));
                startActivityForResult(intent, REQUEST_ADD_TO_LIST);
                break;
            }
            case MENU_OPEN_WITH_ACCOUNT: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
                intent.setClass(getActivity(), AccountSelectorActivity.class);
                intent.putExtra(EXTRA_SINGLE_SELECTION, true);
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
                break;
            }
            case MENU_EDIT: {
                final Bundle extras = new Bundle();
                extras.putLong(EXTRA_ACCOUNT_ID, user.account_id);
                final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
                intent.setClass(getActivity(), UserProfileEditorActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
                return true;
            }
            case MENU_FOLLOW: {
                if (relationship == null) return false;
                final boolean isFollowing = relationship.isSourceFollowingTarget();
                final boolean isCreatingFriendship = twitter.isCreatingFriendship(user.account_id, user.id);
                final boolean isDestroyingFriendship = twitter.isDestroyingFriendship(user.account_id, user.id);
                if (!isCreatingFriendship && !isDestroyingFriendship) {
                    if (isFollowing) {
                        DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
                    } else {
                        twitter.createFriendshipAsync(user.account_id, user.id);
                    }
                }
                return true;
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

    private void setMenu(final Menu menu) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUser user = mUser;
        final Relationship relationship = mRelationship;
        if (twitter == null || user == null) return;
        final boolean isMyself = user.account_id == user.id;
        final boolean isFollowing = relationship != null && relationship.isSourceFollowingTarget();
        final boolean isProtected = user.is_protected;
        final boolean creatingFriendship = twitter.isCreatingFriendship(user.account_id, user.id);
        final boolean destroyingFriendship = twitter.isDestroyingFriendship(user.account_id, user.id);
        setMenuItemAvailability(menu, MENU_EDIT, isMyself);
        final MenuItem mentionItem = menu.findItem(MENU_MENTION);
        if (mentionItem != null) {
            mentionItem.setTitle(getString(R.string.mention_user_name, getDisplayName(getActivity(), user)));
        }
        final MenuItem followItem = menu.findItem(MENU_FOLLOW);
        followItem.setVisible(!isMyself);
        final boolean shouldShowFollowItem = !creatingFriendship && !destroyingFriendship && !isMyself
                && relationship != null;
        followItem.setEnabled(shouldShowFollowItem);
        if (shouldShowFollowItem) {
            followItem.setTitle(isFollowing ? R.string.unfollow : isProtected ? R.string.send_follow_request
                    : R.string.follow);
            followItem.setIcon(isFollowing ? R.drawable.ic_action_cancel : R.drawable.ic_action_add);
        } else {
            followItem.setTitle(null);
            followItem.setIcon(null);
        }
        if (user.id != user.account_id && relationship != null) {
            setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, relationship.canSourceDMTarget());
            setMenuItemAvailability(menu, MENU_BLOCK, true);
            setMenuItemAvailability(menu, MENU_MUTE_USER, true);
            final MenuItem blockItem = menu.findItem(MENU_BLOCK);
            if (blockItem != null) {
                final boolean blocking = relationship.isSourceBlockingTarget();
                MenuUtils.setMenuInfo(blockItem, new TwidereMenuInfo(blocking));
                blockItem.setTitle(blocking ? R.string.unblock : R.string.block);
            }
            final MenuItem muteItem = menu.findItem(MENU_MUTE_USER);
            if (muteItem != null) {
                final boolean muting = relationship.isSourceMutingTarget();
                MenuUtils.setMenuInfo(muteItem, new TwidereMenuInfo(muting));
                muteItem.setTitle(muting ? R.string.unmute : R.string.mute);
            }
            final MenuItem filterItem = menu.findItem(MENU_ADD_TO_FILTER);
            if (filterItem != null) {
                final boolean filtering = Utils.isFilteringUser(getActivity(), user.id);
                MenuUtils.setMenuInfo(filterItem, new TwidereMenuInfo(filtering));
                filterItem.setTitle(filtering ? R.string.remove_from_filter : R.string.add_to_filter);
            }
        } else {
            setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, false);
            setMenuItemAvailability(menu, MENU_BLOCK, false);
            setMenuItemAvailability(menu, MENU_REPORT_SPAM, false);
        }
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
        final Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_USER, user);
        intent.putExtras(extras);
        menu.removeGroup(MENU_GROUP_USER_EXTENSION);
        addIntentToMenu(getActivity(), menu, intent, MENU_GROUP_USER_EXTENSION);
    }

    private boolean shouldUseNativeMenu() {
        final boolean isInLinkHandler = getActivity() instanceof LinkHandlerActivity;
        return isInLinkHandler && FlymeUtils.hasSmartBar();
    }

    private final class MediaTimelineAction extends ListAction {

        public MediaTimelineAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.media);
        }


        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserMediaTimeline(getActivity(), user.account_id, user.id, user.screen_name);
        }

    }

    private final class FavoritesAction extends ListAction {

        public FavoritesAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.favorites);
        }

        @Override
        public String getSummary() {
            if (mUser == null) return null;
            return getLocalizedNumber(mLocale, mUser.favorites_count);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserFavorites(getActivity(), user.account_id, user.id, user.screen_name);
        }

    }

    private final class IncomingFriendshipsAction extends ListAction {

        public IncomingFriendshipsAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.incoming_friendships);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openIncomingFriendships(getActivity(), user.account_id);
        }

    }

    private final class MutesUsersAction extends ListAction {

        public MutesUsersAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.twitter_muted_users);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openMutesUsers(getActivity(), user.account_id);
        }

    }

    private final class SavedSearchesAction extends ListAction {

        public SavedSearchesAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.saved_searches);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openSavedSearches(getActivity(), user.account_id);
        }

    }

    private final class UserBlocksAction extends ListAction {

        public UserBlocksAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.blocked_users);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserBlocks(getActivity(), user.account_id);
        }

    }

    private final class UserListMembershipsAction extends ListAction {
        public UserListMembershipsAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            if (mUser == null) return getString(R.string.lists_following_user);
            final String display_name = getDisplayName(getActivity(), mUser.id, mUser.name, mUser.screen_name);
            return getString(R.string.lists_following_user_with_name, display_name);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserListMemberships(getActivity(), user.account_id, user.id, user.screen_name);
        }
    }

    private final class UserListsAction extends ListAction {

        public UserListsAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            if (mUser == null) return getString(R.string.users_lists);
            final String display_name = getDisplayName(getActivity(), mUser.id, mUser.name, mUser.screen_name);
            return getString(R.string.users_lists_with_name, display_name);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserLists(getActivity(), user.account_id, user.id, user.screen_name);
        }

    }

    private final class UserMentionsAction extends ListAction {

        public UserMentionsAction(final int order) {
            super(order);
        }

        @Override
        public String getName() {
            return getString(R.string.user_mentions);
        }

        @Override
        public void onClick() {
            final ParcelableUser user = mUser;
            if (user == null) return;
            openUserMentions(getActivity(), user.account_id, user.screen_name);
        }

    }

    static class FriendshipLoader extends AsyncTaskLoader<SingleResponse<Relationship>> {

        private final Context context;
        private final long account_id, user_id;

        public FriendshipLoader(final Context context, final long account_id, final long user_id) {
            super(context);
            this.context = context;
            this.account_id = account_id;
            this.user_id = user_id;
        }

        @Override
        public SingleResponse<Relationship> loadInBackground() {
            if (account_id == user_id) return new SingleResponse<>(null, null);
            final Twitter twitter = getTwitterInstance(context, account_id, false);
            if (twitter == null) return new SingleResponse<>(null, null);
            try {
                final Relationship result = twitter.showFriendship(account_id, user_id);
                return new SingleResponse<>(result, null);
            } catch (final TwitterException e) {
                return new SingleResponse<>(null, e);
            }
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    private static class ActionBarDrawable extends LayerDrawable {
        private final Drawable mBackgroundDrawable;

        public ActionBarDrawable(Drawable shadow, Drawable background) {
            super(new Drawable[]{shadow, background});
            mBackgroundDrawable = background;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(Outline outline) {
            mBackgroundDrawable.getOutline(outline);
        }

        @Override
        public int getIntrinsicWidth() {
            return mBackgroundDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mBackgroundDrawable.getIntrinsicHeight();
        }
    }
}
