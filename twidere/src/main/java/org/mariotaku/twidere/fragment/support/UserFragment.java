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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
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
import org.mariotaku.twidere.view.HeaderDrawerLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.ProfileBannerImageView;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
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
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFriends;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.showInfoMessage;

public class UserFragment extends BaseSupportFragment implements OnClickListener,
        OnMenuItemClickListener, OnLinkClickListener, OnSizeChangedListener,
        OnSharedPreferenceChangeListener, OnTouchListener, ImageLoadingListener, DrawerCallback,
        SupportFragmentCallback, SystemWindowsInsetsCallback {

    public static final String TRANSITION_NAME_PROFILE_IMAGE = "profile_image";
    public static final String TRANSITION_NAME_PROFILE_TYPE = "profile_type";

    private static final int LOADER_ID_USER = 1;
    private static final int LOADER_ID_FRIENDSHIP = 2;

    private ImageLoaderWrapper mProfileImageLoader;

    private CircularImageView mProfileImageView;
    private ImageView mProfileTypeView;
    private ProfileBannerImageView mProfileBannerView;
    private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
            mListedCount, mFollowersCount, mFriendsCount, mErrorMessageView;
    private View mDescriptionContainer, mLocationContainer, mURLContainer, mListedContainer, mFollowersContainer,
            mFriendsContainer;
    private Button mRetryButton;
    private ColorLabelLinearLayout mProfileNameContainer;
    private View mProgressContainer, mErrorRetryContainer;
    private View mFollowingYouIndicator;
    private View mMainContent;
    private View mProfileBannerSpace;
    private TintedStatusFrameLayout mTintedStatusContent;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerIndicator;

    private SupportTabsAdapter mPagerAdapter;

    private Relationship mRelationship;
    private ParcelableUser mUser = null;
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
                mMainContent.setVisibility(View.VISIBLE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.VISIBLE);
                mErrorMessageView.setText(null);
                mErrorMessageView.setVisibility(View.GONE);
                setListShown(false);
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
                mMainContent.setVisibility(View.VISIBLE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.GONE);
                setListShown(true);
                displayUser(user);
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
                mProgressContainer.setVisibility(View.GONE);
                setListShown(true);
                displayUser(mUser);
            } else {
                if (data.hasException()) {
                    mErrorMessageView.setText(getErrorMessage(getActivity(), data.getException()));
                    mErrorMessageView.setVisibility(View.VISIBLE);
                }
                mMainContent.setVisibility(View.GONE);
                mErrorRetryContainer.setVisibility(View.VISIBLE);
                mProgressContainer.setVisibility(View.GONE);
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
    private Locale mLocale;
    private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;
    private int mBannerWidth;
    private ActionBarDrawable mActionBarBackground;
    private Fragment mCurrentVisibleFragment;
    private View mUuckyFooter;

    public void displayUser(final ParcelableUser user) {
        mRelationship = null;
        mUser = null;
        if (user == null || user.id <= 0 || getActivity() == null) return;
        final Resources res = getResources();
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final boolean userIsMe = user.account_id == user.id;
        mErrorRetryContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
        mUser = user;
        final int userColor = getUserColor(getActivity(), user.id, true);
        mProfileImageView.setBorderColor(userColor);
        mProfileNameContainer.drawEnd(getAccountColor(getActivity(), user.account_id));
        final String nick = getUserNickname(getActivity(), user.id, true);
        mNameView
                .setText(TextUtils.isEmpty(nick) ? user.name : getString(R.string.name_with_nickname, user.name, nick));
        final int typeIconRes = getUserTypeIconRes(user.is_verified, user.is_protected);
        if (typeIconRes != 0) {
            mProfileTypeView.setImageResource(typeIconRes);
            mProfileTypeView.setVisibility(View.VISIBLE);
        } else {
            mProfileTypeView.setImageDrawable(null);
            mProfileTypeView.setVisibility(View.GONE);
        }
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
        mListedCount.setText(getLocalizedNumber(mLocale, user.listed_count));
        mFollowersCount.setText(getLocalizedNumber(mLocale, user.followers_count));
        mFriendsCount.setText(getLocalizedNumber(mLocale, user.friends_count));
        if (userColor != 0) {
            mProfileImageLoader.displayProfileImage(mProfileImageView,
                    getOriginalTwitterProfileImage(user.profile_image_url));
            setupUserColorActionBar(userColor);
        } else {
            mProfileImageLoader.displayProfileImage(mProfileImageView,
                    getOriginalTwitterProfileImage(user.profile_image_url), this);
        }
        final int defWidth = res.getDisplayMetrics().widthPixels;
        final int width = mBannerWidth > 0 ? mBannerWidth : defWidth;
        mProfileImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
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
        mUuckyFooter.setVisibility(isUucky(user.id, user.screen_name, user) ? View.VISIBLE : View.GONE);
        if (!user.is_cache) {
            getFriendship();
        }
        invalidateOptionsMenu();
    }

    @Override
    public void fling(float velocity) {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).fling(velocity);
        }
    }

    @Override
    public void scrollBy(float dy) {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).scrollBy(dy);
        }
    }

    @Override
    public boolean canScroll(float dy) {
        final Fragment fragment = mCurrentVisibleFragment;
        return fragment instanceof DrawerCallback && ((DrawerCallback) fragment).canScroll(dy);
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
    public void cancelTouch() {
        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).cancelTouch();
        }
    }

    @Override
    public void topChanged(int top) {
        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        if (drawer == null) return;
        final int offset = drawer.getPaddingTop() - top;
        updateScrollOffset(offset);

        final Fragment fragment = mCurrentVisibleFragment;
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).topChanged(top);
        }
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
                    openUserProfile(getActivity(), accountId, user.id, null, null);
                }
                break;
            }
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTintedStatusContent = (TintedStatusFrameLayout) activity.findViewById(R.id.main_content);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
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

        ViewCompat.setTransitionName(mProfileImageView, TRANSITION_NAME_PROFILE_IMAGE);
        ViewCompat.setTransitionName(mProfileTypeView, TRANSITION_NAME_PROFILE_TYPE);

        mHeaderDrawerLayout.setDrawerCallback(this);

        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mPagerIndicator.setViewPager(mViewPager);

        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mListedContainer.setOnClickListener(this);
        mFollowersContainer.setOnClickListener(this);
        mFriendsContainer.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        mProfileBannerView.setOnSizeChangedListener(this);
        mProfileBannerSpace.setOnTouchListener(this);

        getUserInfo(accountId, userId, screenName, false);

        setupBaseActionBar();

        setupUserPages();
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
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
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
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (!shouldUseNativeMenu()) return;
        inflater.inflate(R.menu.menu_user_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        if (!shouldUseNativeMenu() || !menu.hasVisibleItems()) return;
        setMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return handleMenuItemClick(item);
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
            case R.id.listed_container: {
                openUserLists(getActivity(), user.account_id, user.id, user.screen_name);
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
    public void onLinkClick(final String link, final String orig, final long account_id, final int type,
                            final boolean sensitive) {
        final ParcelableUser user = mUser;
        if (user == null) return;
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION: {
                openUserProfile(getActivity(), user.account_id, -1, link, null);
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
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        Palette.generateAsync(loadedImage, new PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                final ParcelableUser user = mUser;
                if (user == null) return;
                final int color = palette.getVibrantColor(0);
                setupUserColorActionBar(color);
            }
        });
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return handleMenuItemClick(item);

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
    public boolean onTouch(final View v, final MotionEvent event) {
        return mProfileBannerView.dispatchTouchEvent(event);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        final Context context = view.getContext();
        super.onViewCreated(view, savedInstanceState);
        mMainContent = view.findViewById(R.id.details_container);
        mHeaderDrawerLayout = (HeaderDrawerLayout) view.findViewById(R.id.user_profile_drawer);
        mErrorRetryContainer = view.findViewById(R.id.error_retry_container);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mRetryButton = (Button) view.findViewById(R.id.retry);
        mErrorMessageView = (TextView) view.findViewById(R.id.error_message);
        mProfileBannerView = (ProfileBannerImageView) view.findViewById(R.id.profile_banner);
        final View headerView = mHeaderDrawerLayout.getHeader();
        final View contentView = mHeaderDrawerLayout.getContent();
        mNameView = (TextView) headerView.findViewById(R.id.name);
        mScreenNameView = (TextView) headerView.findViewById(R.id.screen_name);
        mDescriptionView = (TextView) headerView.findViewById(R.id.description);
        mLocationView = (TextView) headerView.findViewById(R.id.location);
        mURLView = (TextView) headerView.findViewById(R.id.url);
        mCreatedAtView = (TextView) headerView.findViewById(R.id.created_at);
        mListedContainer = headerView.findViewById(R.id.listed_container);
        mListedCount = (TextView) headerView.findViewById(R.id.listed_count);
        mFollowersContainer = headerView.findViewById(R.id.followers_container);
        mFollowersCount = (TextView) headerView.findViewById(R.id.followers_count);
        mFriendsContainer = headerView.findViewById(R.id.friends_container);
        mFriendsCount = (TextView) headerView.findViewById(R.id.friends_count);
        mProfileNameContainer = (ColorLabelLinearLayout) headerView.findViewById(R.id.profile_name_container);
        mProfileImageView = (CircularImageView) headerView.findViewById(R.id.profile_image);
        mProfileTypeView = (ImageView) headerView.findViewById(R.id.profile_type);
        mDescriptionContainer = headerView.findViewById(R.id.description_container);
        mLocationContainer = headerView.findViewById(R.id.location_container);
        mURLContainer = headerView.findViewById(R.id.url_container);
        mFollowingYouIndicator = headerView.findViewById(R.id.following_you_indicator);
        mProfileBannerSpace = headerView.findViewById(R.id.profile_banner_space);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerIndicator = (PagerSlidingTabStrip) contentView.findViewById(R.id.view_pager_tabs);
        mUuckyFooter = headerView.findViewById(R.id.uucky_footer);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View view = getView();
        if (view != null) {
            final View progress = view.findViewById(R.id.progress_container);
            progress.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
        mErrorRetryContainer.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        mHeaderDrawerLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        mHeaderDrawerLayout.setClipToPadding(ThemeUtils.isTransparentBackground(getActivity()));
        final int bannerHeight = mProfileBannerView.getHeight();
        if (bannerHeight != 0) {
            final ViewGroup.LayoutParams params = mProfileBannerSpace.getLayoutParams();
            params.height = bannerHeight - insets.top;
            mProfileBannerSpace.setLayoutParams(params);
            mProfileBannerSpace.requestLayout();
        }
    }

    public void setListShown(boolean shown) {
        final TintedStatusFrameLayout tintedStatus = mTintedStatusContent;
        if (tintedStatus == null) return;
        final FragmentActivity activity = getActivity();
        final LinkHandlerActivity linkHandler = (LinkHandlerActivity) activity;
        final boolean drawColor = !ThemeUtils.isDarkTheme(linkHandler.getCurrentThemeResourceId());
        tintedStatus.setDrawShadow(shown);
        tintedStatus.setDrawColor(drawColor);
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

    private boolean isUucky(long userId, String screenName, Parcelable parcelable) {
        if (userId == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(screenName)) return true;
        if (parcelable instanceof ParcelableUser) {
            final ParcelableUser user = (ParcelableUser) parcelable;
            return user.id == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(user.screen_name);
        }
        return false;
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
        Utils.setMenuItemAvailability(menu, MENU_MENTION, !isMyself);
//        final MenuItem followItem = menu.findItem(MENU_FOLLOW);
//        followItem.setVisible(!isMyself);
//        final boolean shouldShowFollowItem = !creatingFriendship && !destroyingFriendship && !isMyself
//                && relationship != null;
//        followItem.setEnabled(shouldShowFollowItem);
//        if (shouldShowFollowItem) {
//            followItem.setTitle(isFollowing ? R.string.unfollow : isProtected ? R.string.send_follow_request
//                    : R.string.follow);
//            followItem.setIcon(isFollowing ? R.drawable.ic_action_cancel : R.drawable.ic_action_add);
//        } else {
//            followItem.setTitle(null);
//            followItem.setIcon(null);
//        }
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

    private void setupBaseActionBar() {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof LinkHandlerActivity)) return;
        final LinkHandlerActivity linkHandler = (LinkHandlerActivity) activity;
        final ActionBar actionBar = linkHandler.getActionBar();
        if (actionBar == null) return;
        final int themeResId = linkHandler.getCurrentThemeResourceId();
        final Drawable shadow = activity.getResources().getDrawable(R.drawable.shadow_user_banner_action_bar);
        final Drawable background = ThemeUtils.getActionBarBackground(activity, themeResId);
        mActionBarBackground = new ActionBarDrawable(getResources(), shadow, background, ThemeUtils.isDarkTheme(themeResId));
        mActionBarBackground.setAlpha(ThemeUtils.getThemeAlpha(activity));
        actionBar.setBackgroundDrawable(mActionBarBackground);
    }

    private void setupUserColorActionBar(int color) {
        if (mActionBarBackground == null) {
            setupBaseActionBar();
        }
        mActionBarBackground.setColor(color);
        mTintedStatusContent.setColor(color, ThemeUtils.getThemeAlpha(getActivity()));
        mPagerIndicator.setIndicatorColor(color);
    }

    private void setupUserPages() {
        final Context context = getActivity();
        final Bundle args = getArguments(), tabArgs = new Bundle();
        final long accountId;
        if (args.containsKey(EXTRA_USER)) {
            final ParcelableUser user = args.getParcelable(EXTRA_USER);
            tabArgs.putLong(EXTRA_ACCOUNT_ID, accountId = user.account_id);
            tabArgs.putLong(EXTRA_USER_ID, user.id);
            tabArgs.putString(EXTRA_SCREEN_NAME, user.screen_name);
        } else {
            accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            tabArgs.putLong(EXTRA_ACCOUNT_ID, accountId);
            tabArgs.putLong(EXTRA_USER_ID, args.getLong(EXTRA_USER_ID, -1));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
        }
        mPagerAdapter.addTab(UserTimelineFragment.class, tabArgs, getString(R.string.statuses), null, 0);
        if (Utils.isOfficialKeyAccount(context, accountId)) {
            mPagerAdapter.addTab(UserMediaTimelineFragment.class, tabArgs, getString(R.string.media), null, 1);
        }
        mPagerAdapter.addTab(UserFavoritesFragment.class, tabArgs, getString(R.string.favorites), null, 2);
        mPagerIndicator.notifyDataSetChanged();
    }

    private boolean shouldUseNativeMenu() {
        return getActivity() instanceof LinkHandlerActivity;
    }

    private void updateScrollOffset(int offset) {
        final View space = mProfileBannerSpace;
        if (space == null) return;
        final int spaceHeight = space.getHeight();
        final float factor = MathUtils.clamp(offset / (float) spaceHeight, 0, 1);
        final ProfileBannerImageView profileBannerView = mProfileBannerView;
        profileBannerView.setAlpha(1.0f - factor / 8f);
        profileBannerView.setTranslationY(Math.min(offset, spaceHeight) / -2);
        profileBannerView.setBottomClip(Math.min(offset, spaceHeight));

        if (mActionBarBackground != null && mTintedStatusContent != null) {
            mActionBarBackground.setFactor(factor);
            mTintedStatusContent.setFactor(factor);
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

        private final Drawable mShadowDrawable;
        private final Drawable mBackgroundDrawable;
        private final LineBackgroundDrawable mLineDrawable;
        private final ColorDrawable mColorDrawable;
        private final boolean mColorLineOnly;

        private float mFactor;
        private int mColor;
        private int mAlpha;

        public ActionBarDrawable(Resources resources, Drawable shadow, Drawable background,
                                 boolean colorLineOnly) {
            super(new Drawable[]{shadow, background, new LineBackgroundDrawable(resources, 2.0f),
                    new ColorDrawable()});
            mShadowDrawable = shadow;
            mBackgroundDrawable = getDrawable(1);
            mLineDrawable = (LineBackgroundDrawable) getDrawable(2);
            mColorDrawable = (ColorDrawable) getDrawable(3);
            mColorLineOnly = colorLineOnly;
            setAlpha(0xFF);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(Outline outline) {
            final boolean showColor = !mColorLineOnly && mColor != 0;
            if (showColor) {
                mColorDrawable.getOutline(outline);
            } else {
                mBackgroundDrawable.getOutline(outline);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            super.setAlpha(alpha);
            mAlpha = alpha;
            setFactor(mFactor);
        }

        @Override
        public int getIntrinsicWidth() {
            final boolean showColor = !mColorLineOnly && mColor != 0;
            if (showColor) {
                return mColorDrawable.getIntrinsicWidth();
            } else {
                return mBackgroundDrawable.getIntrinsicWidth();
            }
        }

        @Override
        public int getIntrinsicHeight() {
            final boolean showColor = !mColorLineOnly && mColor != 0;
            if (showColor) {
                return mColorDrawable.getIntrinsicHeight();
            } else {
                return mBackgroundDrawable.getIntrinsicHeight();
            }
        }

        public void setColor(int color) {
            mColor = color;
            final float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = Math.min(hsv[2], 0.8f);
            final int processedColor = Color.HSVToColor(hsv);
            mColorDrawable.setColor(processedColor);
            mLineDrawable.setColor(color);
            setFactor(mFactor);
        }

        public void setFactor(float f) {
            mFactor = f;
            mShadowDrawable.setAlpha(Math.round(mAlpha * MathUtils.clamp(1 - f, 0, 1)));
            final boolean hasColor = mColor != 0;
            final boolean showBackground = mColorLineOnly || !hasColor;
            final boolean showLine = mColorLineOnly && hasColor;
            final boolean showColor = !mColorLineOnly && hasColor;
            mBackgroundDrawable.setAlpha(showBackground ? Math.round(mAlpha * MathUtils.clamp(f, 0, 1)) : 0);
            mLineDrawable.setAlpha(showLine ? Math.round(mAlpha * MathUtils.clamp(f, 0, 1)) : 0);
            mColorDrawable.setAlpha(showColor ? Math.round(mAlpha * MathUtils.clamp(f, 0, 1)) : 0);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private static class LineBackgroundDrawable extends Drawable {

            private final Rect mBounds;
            private final Paint mPaint;
            private final float mLineSize;

            private int mAlpha;
            private int mColor;

            LineBackgroundDrawable(Resources resources, float lineSizeDp) {
                mBounds = new Rect();
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mLineSize = resources.getDisplayMetrics().density * lineSizeDp;
                setColor(Color.TRANSPARENT);
            }

            @Override
            public void draw(Canvas canvas) {
                canvas.drawRect(mBounds.left, mBounds.bottom - mLineSize, mBounds.right,
                        mBounds.bottom, mPaint);
            }

            public int getColor() {
                return mColor;
            }

            @Override
            protected void onBoundsChange(Rect bounds) {
                super.onBoundsChange(bounds);
                mBounds.set(bounds);
            }

            public void setColor(int color) {
                mColor = color;
                updatePaint();
            }

            private void updatePaint() {
                mPaint.setColor(mColor);
                mPaint.setAlpha(Color.alpha(mColor) * mAlpha / 0xFF);
                invalidateSelf();
            }

            @Override
            public int getAlpha() {
                return mAlpha;
            }


            @Override
            public void setAlpha(int alpha) {
                mAlpha = alpha;
                updatePaint();
            }

            @Override
            public void setColorFilter(ColorFilter cf) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

        }
    }
}
