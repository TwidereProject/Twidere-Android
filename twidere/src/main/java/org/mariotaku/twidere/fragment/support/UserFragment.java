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

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.ActionBarColorDrawable;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ParcelableAccount.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.text.TextAlphaSpan;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ColorUtils;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.util.message.FriendshipUpdatedEvent;
import org.mariotaku.twidere.util.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.ProfileBannerImageView;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.util.List;
import java.util.Locale;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseLong;
import static org.mariotaku.twidere.util.UserColorNameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getErrorMessage;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getOriginalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openIncomingFriendships;
import static org.mariotaku.twidere.util.Utils.openMutesUsers;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFriends;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.showInfoMessage;

public class UserFragment extends BaseSupportFragment implements OnClickListener,
        OnLinkClickListener, OnSizeChangedListener, OnSharedPreferenceChangeListener,
        OnTouchListener, DrawerCallback, SupportFragmentCallback, SystemWindowsInsetsCallback {

    public static final String TRANSITION_NAME_PROFILE_IMAGE = "profile_image";
    public static final String TRANSITION_NAME_PROFILE_TYPE = "profile_type";
    public static final String TRANSITION_NAME_CARD = "card";

    private static final int LOADER_ID_USER = 1;
    private static final int LOADER_ID_FRIENDSHIP = 2;

    private MediaLoaderWrapper mProfileImageLoader;

    private ShapedImageView mProfileImageView;
    private ImageView mProfileTypeView;
    private ProfileBannerImageView mProfileBannerView;
    private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
            mListedCount, mFollowersCount, mFriendsCount, mErrorMessageView;
    private View mDescriptionContainer, mLocationContainer, mURLContainer, mListedContainer, mFollowersContainer,
            mFriendsContainer;
    private Button mRetryButton;
    private ColorLabelRelativeLayout mProfileNameContainer;
    private View mProgressContainer, mErrorRetryContainer;
    private View mCardContent;
    private View mProfileBannerSpace;
    private TintedStatusFrameLayout mTintedStatusContent;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private View mUuckyFooter;
    private View mProfileBannerContainer;
    private Button mFollowButton;
    private ProgressBar mFollowProgress;
    private View mPagesContent, mPagesErrorContainer;
    private ImageView mPagesErrorIcon;
    private TextView mPagesErrorText;
    private View mProfileNameBackground;
    private View mProfileDetailsContainer;

    private SupportTabsAdapter mPagerAdapter;

    private Relationship mRelationship;
    private ParcelableUser mUser = null;

    private Locale mLocale;
    private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;
    private int mBannerWidth;
    private ActionBarDrawable mActionBarBackground;
    private Fragment mCurrentVisibleFragment;
    private int mCardBackgroundColor;


    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
    }

    @Subscribe
    public void notifyFriendshipUpdated(FriendshipUpdatedEvent event) {
        if (!event.user.equals(mUser)) return;
        getFriendship();
    }

    private void updateRefreshState() {
        final ParcelableUser user = getUser();
        if (user == null) return;
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final boolean is_creating_friendship = twitter != null
                && twitter.isCreatingFriendship(user.account_id, user.id);
        final boolean is_destroying_friendship = twitter != null
                && twitter.isDestroyingFriendship(user.account_id, user.id);
        setProgressBarIndeterminateVisibility(is_creating_friendship || is_destroying_friendship);
        invalidateOptionsMenu();
    }

    private final LoaderCallbacks<SingleResponse<ParcelableUser>> mUserInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableUser>>() {

        @Override
        public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
            final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            if (mUser == null && (!omitIntentExtra || !args.containsKey(EXTRA_USER))) {
                mCardContent.setVisibility(View.GONE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.VISIBLE);
                mErrorMessageView.setText(null);
                mErrorMessageView.setVisibility(View.GONE);
                setListShown(false);
            }
            setProgressBarIndeterminateVisibility(true);
            final ParcelableUser user = mUser;
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
            if (data.hasData()) {
                final ParcelableUser user = data.getData();
                mCardContent.setVisibility(View.VISIBLE);
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
                mCardContent.setVisibility(View.VISIBLE);
                mErrorRetryContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.GONE);
                setListShown(true);
                displayUser(mUser);
            } else {
                if (data.hasException()) {
                    mErrorMessageView.setText(getErrorMessage(getActivity(), data.getException()));
                    mErrorMessageView.setVisibility(View.VISIBLE);
                }
                mCardContent.setVisibility(View.GONE);
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
            mFollowButton.setVisibility(View.GONE);
            mFollowProgress.setVisibility(View.VISIBLE);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            return new RelationshipLoader(getActivity(), accountId, userId);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<Relationship>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<Relationship>> loader,
                                   final SingleResponse<Relationship> data) {
            mFollowProgress.setVisibility(View.GONE);
            final ParcelableUser user = getUser();
            final Relationship relationship = data.getData();
            mRelationship = relationship;
            if (user == null) return;
            invalidateOptionsMenu();
            final boolean isMyself = user.account_id == user.id;
            if (isMyself) {
                mFollowButton.setText(R.string.edit);
                mFollowButton.setVisibility(View.VISIBLE);
            } else if (relationship != null) {
                final int drawableRes;
                mFollowButton.setEnabled(!relationship.isSourceBlockedByTarget());
                if (relationship.isSourceBlockedByTarget()) {
                    mPagesErrorContainer.setVisibility(View.VISIBLE);
                    final String displayName = UserColorNameUtils.getDisplayName(getActivity(), user);
                    mPagesErrorText.setText(getString(R.string.blocked_by_user_summary, displayName));
                    mPagesErrorIcon.setImageResource(R.drawable.ic_info_error_generic);
                    mPagesContent.setVisibility(View.GONE);
                } else if (!relationship.isSourceFollowingTarget() && user.is_protected) {
                    mPagesErrorContainer.setVisibility(View.VISIBLE);
                    final String displayName = UserColorNameUtils.getDisplayName(getActivity(), user);
                    mPagesErrorText.setText(getString(R.string.user_protected_summary, displayName));
                    mPagesErrorIcon.setImageResource(R.drawable.ic_info_locked);
                    mPagesContent.setVisibility(View.GONE);
                } else {
                    mPagesErrorContainer.setVisibility(View.GONE);
                    mPagesErrorText.setText(null);
                    mPagesContent.setVisibility(View.VISIBLE);
                }
                if (relationship.isSourceBlockingTarget()) {
                    mFollowButton.setText(R.string.unblock);
                    drawableRes = R.drawable.ic_follow_blocked;
                } else if (relationship.isSourceFollowingTarget()) {
                    mFollowButton.setText(R.string.unfollow);
                    if (relationship.isTargetFollowingSource()) {
                        drawableRes = R.drawable.ic_follow_bidirectional;
                    } else {
                        drawableRes = R.drawable.ic_follow_outgoing;
                    }
                } else if (user.is_follow_request_sent) {
                    mFollowButton.setText(R.string.requested);
                    if (relationship.isTargetFollowingSource()) {
                        drawableRes = R.drawable.ic_follow_incoming;
                    } else {
                        drawableRes = R.drawable.ic_follow_requested;
                    }
                } else {
                    mFollowButton.setText(R.string.follow);
                    if (relationship.isTargetFollowingSource()) {
                        drawableRes = R.drawable.ic_follow_incoming;
                    } else {
                        drawableRes = R.drawable.ic_follow_none;
                    }
                }
                final Drawable icon = ResourcesCompat.getDrawable(getResources(), drawableRes, null);
                final int iconSize = Math.round(mFollowButton.getTextSize() * 1.4f);
                icon.setBounds(0, 0, iconSize, iconSize);
                icon.setColorFilter(mFollowButton.getCurrentTextColor(), Mode.SRC_ATOP);
                mFollowButton.setCompoundDrawables(icon, null, null, null);
                mFollowButton.setCompoundDrawablePadding(Math.round(mFollowButton.getTextSize() * 0.25f));

                final ContentResolver resolver = getContentResolver();
                final ContentValues cachedValues = ParcelableUser.makeCachedUserContentValues(user);
                resolver.insert(CachedUsers.CONTENT_URI, cachedValues);
                mFollowButton.setVisibility(View.VISIBLE);
            } else {
                mFollowButton.setText(null);
                mFollowButton.setVisibility(View.GONE);
                mPagesErrorContainer.setVisibility(View.GONE);
                mPagesContent.setVisibility(View.VISIBLE);
//                mFollowingYouIndicator.setVisibility(View.GONE);
            }
        }

    };

    public void displayUser(final ParcelableUser user) {
        mUser = user;
        final FragmentActivity activity = getActivity();
        if (user == null || user.id <= 0 || activity == null) return;
        final Resources res = getResources();
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final boolean userIsMe = user.account_id == user.id;
        mCardContent.setVisibility(View.VISIBLE);
        mErrorRetryContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
        mUser = user;
        final int userColor = getUserColor(activity, user.id, true);
        mProfileImageView.setBorderColor(userColor != 0 ? userColor : Color.WHITE);
        mProfileNameContainer.drawEnd(getAccountColor(activity, user.account_id));
        final String nick = getUserNickname(activity, user.id, true);
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
        mDescriptionContainer.setVisibility(isEmpty(user.description_html) ? View.GONE : View.VISIBLE);
        mDescriptionView.setText(user.description_html != null ? Html.fromHtml(user.description_html) : user.description_plain);
        final TwidereLinkify linkify = new TwidereLinkify(this);
        linkify.applyAllLinks(mDescriptionView, user.account_id, false);
        mDescriptionView.setMovementMethod(null);
        mLocationContainer.setVisibility(isEmpty(user.location) ? View.GONE : View.VISIBLE);
        mLocationView.setText(user.location);
        mURLContainer.setVisibility(isEmpty(user.url) && isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        mURLView.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
        mURLView.setMovementMethod(null);
        final String createdAt = formatToLongTimeString(activity, user.created_at);
        final float daysSinceCreation = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24;
        final int dailyTweets = Math.round(user.statuses_count / Math.max(1, daysSinceCreation));
        mCreatedAtView.setText(res.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                createdAt, dailyTweets));
        mListedCount.setText(getLocalizedNumber(mLocale, user.listed_count));
        mFollowersCount.setText(getLocalizedNumber(mLocale, user.followers_count));
        mFriendsCount.setText(getLocalizedNumber(mLocale, user.friends_count));

        mProfileImageLoader.displayProfileImage(mProfileImageView, getOriginalTwitterProfileImage(user.profile_image_url));
        if (userColor != 0) {
            setUserUiColor(userColor);
        } else {
            setUserUiColor(user.link_color);
        }
        final int defWidth = res.getDisplayMetrics().widthPixels;
        final int width = mBannerWidth > 0 ? mBannerWidth : defWidth;
        mProfileImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
        mUuckyFooter.setVisibility(isUucky(user.id, user.screen_name, user) ? View.VISIBLE : View.GONE);
        final Relationship relationship = mRelationship;
        if (relationship == null || relationship.getTargetUserId() != user.id) {
            getFriendship();
        }
        activity.setTitle(UserColorNameUtils.getDisplayName(activity, user, true));
        updateTitleColor();
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
    public boolean shouldLayoutHeaderBottom() {
        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        final View card = mProfileDetailsContainer;
        if (drawer == null || card == null) return false;
        return card.getTop() + drawer.getHeaderTop() - drawer.getPaddingTop() <= 0;
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
            mCardContent.setVisibility(View.GONE);
            mErrorRetryContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final ParcelableUser user = getUser();
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
                    UserColorNameUtils.setUserColor(getActivity(), mUser.id, color);
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
                    openUserProfile(getActivity(), accountId, user.id, user.screen_name, null);
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
        final View view = inflater.inflate(R.layout.fragment_user, container, false);
//        final ViewGroup profileDetailsContainer = (ViewGroup) view.findViewById(R.id.profile_details_container);
//        final boolean isCompact = Utils.isCompactCards(getActivity());
//        if (isCompact) {
//            inflater.inflate(R.layout.layout_user_details_compact, profileDetailsContainer);
//        } else {
//            inflater.inflate(R.layout.layout_user_details, profileDetailsContainer);
//        }
        return view;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FragmentActivity activity = getActivity();
        setHasOptionsMenu(true);
        getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        mLocale = getResources().getConfiguration().locale;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(activity);
        mProfileImageLoader = getApplication().getMediaLoaderWrapper();
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

        Utils.setNdefPushMessageCallback(activity, new CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableUser user = getUser();
                if (user == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(LinkCreator.getTwitterUserLink(user.screen_name)),
                });
            }
        });

        activity.setEnterSharedElementCallback(new SharedElementCallback() {

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                int idx = sharedElementNames.indexOf(TRANSITION_NAME_PROFILE_IMAGE);
                if (idx != -1) {
                    final View view = sharedElements.get(idx);
                    int[] location = new int[2];
                    final RectF bounds = new RectF(0, 0, view.getWidth(), view.getHeight());
                    view.getLocationOnScreen(location);
                    bounds.offsetTo(location[0], location[1]);
                    mProfileImageView.setTransitionDestination(bounds);
                }
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                final int idx = sharedElementNames.indexOf(TRANSITION_NAME_PROFILE_IMAGE);
                if (idx != -1) {
                    final View view = sharedElements.get(idx);
                    int[] location = new int[2];
                    final RectF bounds = new RectF(0, 0, view.getWidth(), view.getHeight());
                    view.getLocationOnScreen(location);
                    bounds.offsetTo(location[0], location[1]);
                    mProfileImageView.setTransitionSource(bounds);
                }
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

        });

        ViewCompat.setTransitionName(mProfileImageView, TRANSITION_NAME_PROFILE_IMAGE);
        ViewCompat.setTransitionName(mProfileTypeView, TRANSITION_NAME_PROFILE_TYPE);
//        ViewCompat.setTransitionName(mCardView, TRANSITION_NAME_CARD);

        mHeaderDrawerLayout.setDrawerCallback(this);

        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager());

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mPagerAdapter);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);

        mFollowButton.setOnClickListener(this);
        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mListedContainer.setOnClickListener(this);
        mFollowersContainer.setOnClickListener(this);
        mFriendsContainer.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        mProfileBannerView.setOnSizeChangedListener(this);
        mProfileBannerSpace.setOnTouchListener(this);


        mProfileNameBackground.setBackgroundColor(mCardBackgroundColor);
        mProfileDetailsContainer.setBackgroundColor(mCardBackgroundColor);
        mUuckyFooter.setBackgroundColor(mCardBackgroundColor);

        getUserInfo(accountId, userId, screenName, false);

        final float actionBarElevation = ThemeUtils.getSupportActionBarElevation(activity);
        ViewCompat.setElevation(mPagerIndicator, actionBarElevation);

        setupBaseActionBar();

        setupUserPages();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
    }


    @Subscribe
    public void notifyProfileUpdated(ProfileUpdatedEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || !user.equals(event.user)) return;
        displayUser(event.user);
    }

    public ParcelableUser getUser() {
        return mUser;
    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER, getUser());
        super.onSaveInstanceState(outState);
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
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUser user = getUser();
        final Relationship relationship = mRelationship;
        if (twitter == null || user == null) return;
        final boolean isMyself = user.account_id == user.id;
        final MenuItem mentionItem = menu.findItem(MENU_MENTION);
        if (mentionItem != null) {
            mentionItem.setTitle(getString(R.string.mention_user_name, UserColorNameUtils.getDisplayName(getActivity(), user)));
        }
        Utils.setMenuItemAvailability(menu, MENU_MENTION, !isMyself);
        Utils.setMenuItemAvailability(menu, R.id.incoming_friendships, isMyself);
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
        if (!isMyself && relationship != null) {
            setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, relationship.canSourceDMTarget());
            setMenuItemAvailability(menu, MENU_BLOCK, true);
            setMenuItemAvailability(menu, MENU_MUTE_USER, true);
            final MenuItem blockItem = menu.findItem(MENU_BLOCK);
            if (blockItem != null) {
                final boolean blocking = relationship.isSourceBlockingTarget();
                ActionIconDrawable.setMenuHighlight(blockItem, new TwidereMenuInfo(blocking));
                blockItem.setTitle(blocking ? R.string.unblock : R.string.block);
            }
            final MenuItem muteItem = menu.findItem(MENU_MUTE_USER);
            if (muteItem != null) {
                final boolean muting = relationship.isSourceMutingTarget();
                ActionIconDrawable.setMenuHighlight(muteItem, new TwidereMenuInfo(muting));
                muteItem.setTitle(muting ? R.string.unmute : R.string.mute);
            }
            final MenuItem filterItem = menu.findItem(MENU_ADD_TO_FILTER);
            if (filterItem != null) {
                final boolean filtering = Utils.isFilteringUser(getActivity(), user.id);
                ActionIconDrawable.setMenuHighlight(filterItem, new TwidereMenuInfo(filtering));
                filterItem.setTitle(filtering ? R.string.remove_from_filter : R.string.add_to_filter);
            }
        } else {
            setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, false);
            setMenuItemAvailability(menu, MENU_BLOCK, false);
            setMenuItemAvailability(menu, MENU_MUTE_USER, false);
            setMenuItemAvailability(menu, MENU_REPORT_SPAM, false);
        }
        setMenuItemAvailability(menu, R.id.muted_users, isMyself);
        setMenuItemAvailability(menu, R.id.blocked_users, isMyself);
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
        final Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_USER, user);
        intent.putExtras(extras);
        menu.removeGroup(MENU_GROUP_USER_EXTENSION);
        addIntentToMenu(getActivity(), menu, intent, MENU_GROUP_USER_EXTENSION);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUser user = getUser();
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
                    final Expression where = Expression.equals(Filters.Users.USER_ID, user.id);
                    cr.delete(Filters.Users.CONTENT_URI, where.getSQL(), null);
                    showInfoMessage(getActivity(), R.string.message_user_unmuted, false);
                } else {
                    cr.insert(Filters.Users.CONTENT_URI, ContentValuesCreator.createFilteredUser(user));
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
                builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user.id));
                final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
                intent.putExtra(EXTRA_ACCOUNT, ParcelableCredentials.getAccount(getActivity(), user.account_id));
                intent.putExtra(EXTRA_USER, user);
                startActivity(intent);
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
            case R.id.muted_users: {
                openMutesUsers(getActivity(), user.account_id);
                return true;
            }
            case R.id.blocked_users: {
                openUserBlocks(getActivity(), user.account_id);
                return true;
            }
            case R.id.incoming_friendships: {
                openIncomingFriendships(getActivity(), user.account_id);
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

    @Override
    public void onClick(final View view) {
        final FragmentActivity activity = getActivity();
        final ParcelableUser user = getUser();
        if (activity == null || user == null) return;
        switch (view.getId()) {
            case R.id.retry: {
                getUserInfo(true);
                break;
            }
            case R.id.follow: {
                if (user.id == user.account_id) {
                    final Bundle extras = new Bundle();
                    extras.putLong(EXTRA_ACCOUNT_ID, user.account_id);
                    final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
                    intent.setClass(getActivity(), UserProfileEditorActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                    break;
                }
                final Relationship relationship = mRelationship;
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (relationship == null || twitter == null) return;
                if (relationship.isSourceBlockingTarget()) {
                    twitter.destroyBlockAsync(user.account_id, user.id);
                } else if (relationship.isSourceFollowingTarget()) {
                    DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
                } else {
                    twitter.createFriendshipAsync(user.account_id, user.id);
                }
                break;
            }
            case R.id.profile_image: {
                final String url = getOriginalTwitterProfileImage(user.profile_image_url);
                final ParcelableMedia[] media = {ParcelableMedia.newImage(url, url)};
                Utils.openMedia(activity, user.account_id, false, null, media);
                break;
            }
            case R.id.profile_banner: {
                if (user.profile_banner_url == null) return;
                final String url = user.profile_banner_url + "/ipad_retina";
                final ParcelableMedia[] media = {ParcelableMedia.newImage(url, url)};
                Utils.openMedia(activity, user.account_id, false, null, media);
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
    public void onLinkClick(final String link, final String orig, final long accountId, long extraId, final int type,
                            final boolean sensitive, int start, int end) {
        final ParcelableUser user = getUser();
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
                openStatus(getActivity(), accountId, parseLong(link));
                break;
            }
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
    public boolean onTouch(final View v, final MotionEvent event) {
        return mProfileBannerView.dispatchTouchEvent(event);
    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mHeaderDrawerLayout = (HeaderDrawerLayout) view.findViewById(R.id.user_profile_drawer);
        final View headerView = mHeaderDrawerLayout.getHeader();
        final View contentView = mHeaderDrawerLayout.getContent();
        mCardContent = headerView.findViewById(R.id.card_content);
        mErrorRetryContainer = headerView.findViewById(R.id.error_retry_container);
        mProgressContainer = headerView.findViewById(R.id.progress_container);
        mRetryButton = (Button) headerView.findViewById(R.id.retry);
        mErrorMessageView = (TextView) headerView.findViewById(R.id.error_message);
        mProfileBannerView = (ProfileBannerImageView) view.findViewById(R.id.profile_banner);
        mProfileBannerContainer = view.findViewById(R.id.profile_banner_container);
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
        mProfileNameContainer = (ColorLabelRelativeLayout) headerView.findViewById(R.id.profile_name_container);
        mProfileImageView = (ShapedImageView) headerView.findViewById(R.id.profile_image);
        mProfileTypeView = (ImageView) headerView.findViewById(R.id.profile_type);
        mDescriptionContainer = headerView.findViewById(R.id.description_container);
        mLocationContainer = headerView.findViewById(R.id.location_container);
        mURLContainer = headerView.findViewById(R.id.url_container);
        mProfileBannerSpace = headerView.findViewById(R.id.profile_banner_space);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) contentView.findViewById(R.id.view_pager_tabs);
        mFollowButton = (Button) headerView.findViewById(R.id.follow);
        mFollowProgress = (ProgressBar) headerView.findViewById(R.id.follow_progress);
        mUuckyFooter = headerView.findViewById(R.id.uucky_footer);
        mPagesContent = view.findViewById(R.id.pages_content);
        mPagesErrorContainer = view.findViewById(R.id.pages_error_container);
        mPagesErrorIcon = (ImageView) view.findViewById(R.id.pages_error_icon);
        mPagesErrorText = (TextView) view.findViewById(R.id.pages_error_text);
        mProfileNameBackground = view.findViewById(R.id.profile_name_background);
        mProfileDetailsContainer = view.findViewById(R.id.profile_details_container);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mHeaderDrawerLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        final FragmentActivity activity = getActivity();
        final boolean isTransparentBackground;
        if (activity instanceof IThemedActivity) {
            final int themeRes = ((IThemedActivity) activity).getCurrentThemeResourceId();
            isTransparentBackground = ThemeUtils.isTransparentBackground(themeRes);
        } else {
            isTransparentBackground = ThemeUtils.isTransparentBackground(getActivity());
        }
        mHeaderDrawerLayout.setClipToPadding(isTransparentBackground);
    }

    public void setListShown(boolean shown) {
        final TintedStatusFrameLayout tintedStatus = mTintedStatusContent;
        if (tintedStatus == null) return;
        tintedStatus.setDrawShadow(shown);
    }

    private void getFriendship() {
        mRelationship = null;
        final ParcelableUser user = getUser();
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

    private boolean isUucky(long userId, String screenName, Parcelable parcelable) {
        if (userId == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(screenName)) return true;
        if (parcelable instanceof ParcelableUser) {
            final ParcelableUser user = (ParcelableUser) parcelable;
            return user.id == UUCKY_ID || UUCKY_SCREEN_NAME.equalsIgnoreCase(user.screen_name);
        }
        return false;
    }

    private void updateFollowProgressState() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUser user = getUser();
        if (twitter == null || user == null) {
            mFollowButton.setVisibility(View.GONE);
            mFollowProgress.setVisibility(View.GONE);
            return;
        }
        final LoaderManager lm = getLoaderManager();
        final boolean loadingRelationship = lm.getLoader(LOADER_ID_FRIENDSHIP) != null;
        final boolean creatingFriendship = twitter.isCreatingFriendship(user.account_id, user.id);
        final boolean destroyingFriendship = twitter.isDestroyingFriendship(user.account_id, user.id);
        final boolean creatingBlock = twitter.isCreatingFriendship(user.account_id, user.id);
        final boolean destroyingBlock = twitter.isDestroyingFriendship(user.account_id, user.id);
        if (loadingRelationship || creatingFriendship || destroyingFriendship || creatingBlock || destroyingBlock) {
            mFollowButton.setVisibility(View.GONE);
            mFollowProgress.setVisibility(View.VISIBLE);
        } else if (mRelationship != null) {
            mFollowButton.setVisibility(View.VISIBLE);
            mFollowProgress.setVisibility(View.GONE);
        } else {
            mFollowButton.setVisibility(View.GONE);
            mFollowProgress.setVisibility(View.GONE);
        }
    }

    private void setupBaseActionBar() {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof LinkHandlerActivity)) return;
        final LinkHandlerActivity linkHandler = (LinkHandlerActivity) activity;
        final ActionBar actionBar = linkHandler.getSupportActionBar();
        if (actionBar == null) return;
        final Drawable shadow = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.shadow_user_banner_action_bar, null);
        mActionBarBackground = new ActionBarDrawable(getResources(), shadow);
        mActionBarBackground.setAlpha(linkHandler.getCurrentThemeBackgroundAlpha());
        mProfileBannerView.setAlpha(linkHandler.getCurrentThemeBackgroundAlpha() / 255f);
        actionBar.setBackgroundDrawable(mActionBarBackground);
    }

    private void setUserUiColor(int color) {
        if (mActionBarBackground == null) {
            setupBaseActionBar();
        }
        final FragmentActivity activity = getActivity();
        final IThemedActivity themed = (IThemedActivity) activity;
        final int themeRes = themed.getCurrentThemeResourceId();
        if (ThemeUtils.isDarkTheme(themeRes)) {
            final int actionBarColor = getResources().getColor(R.color.background_color_action_bar_dark);
            mTintedStatusContent.setColor(actionBarColor, themed.getCurrentThemeBackgroundAlpha());
            mActionBarBackground.setColor(actionBarColor);
        } else {
            mTintedStatusContent.setColor(color, themed.getCurrentThemeBackgroundAlpha());
            mActionBarBackground.setColor(color);
        }
        mDescriptionView.setLinkTextColor(color);
        mProfileBannerView.setBackgroundColor(color);
        mLocationView.setLinkTextColor(color);
        mURLView.setLinkTextColor(color);
        ViewAccessor.setBackground(mPagerIndicator, ThemeUtils.getActionBarStackedBackground(activity, themeRes, color, true));

        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        if (drawer != null) {
            final int offset = drawer.getPaddingTop() - drawer.getHeaderTop();
            updateScrollOffset(offset);
        }
    }

    private void setupUserPages() {
        final Context context = getActivity();
        final Bundle args = getArguments(), tabArgs = new Bundle();
        final long accountId;
        final ParcelableUser user = args.getParcelable(EXTRA_USER);
        if (user != null) {
            tabArgs.putLong(EXTRA_ACCOUNT_ID, accountId = user.account_id);
            tabArgs.putLong(EXTRA_USER_ID, user.id);
            tabArgs.putString(EXTRA_SCREEN_NAME, user.screen_name);
        } else {
            accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            tabArgs.putLong(EXTRA_ACCOUNT_ID, accountId);
            tabArgs.putLong(EXTRA_USER_ID, args.getLong(EXTRA_USER_ID, -1));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
        }
        mPagerAdapter.addTab(UserTimelineFragment.class, tabArgs, getString(R.string.statuses), R.drawable.ic_action_quote, 0, null);
        if (Utils.isOfficialKeyAccount(context, accountId)) {
            mPagerAdapter.addTab(UserMediaTimelineFragment.class, tabArgs, getString(R.string.media), R.drawable.ic_action_gallery, 1, null);
        }
        mPagerAdapter.addTab(UserFavoritesFragment.class, tabArgs, getString(R.string.favorites), R.drawable.ic_action_star, 2, null);
    }

    private boolean shouldUseNativeMenu() {
        return getActivity() instanceof LinkHandlerActivity;
    }

    private static final ArgbEvaluator sArgbEvaluator = new ArgbEvaluator();

    private void updateScrollOffset(int offset) {
        final View space = mProfileBannerSpace;
        final ProfileBannerImageView profileBannerView = mProfileBannerView;
        final View profileBannerContainer = mProfileBannerContainer;
        final int spaceHeight = space.getHeight();
        final float factor = MathUtils.clamp(offset / (float) spaceHeight, 0, 1);
        profileBannerContainer.setTranslationY(Math.max(-offset, -spaceHeight));
        profileBannerView.setTranslationY(Math.min(offset, spaceHeight) / 2);

        if (mActionBarBackground != null && mTintedStatusContent != null) {
            mActionBarBackground.setFactor(factor);
            mTintedStatusContent.setFactor(factor);

            final float profileContentHeight = mProfileNameContainer.getHeight() + mProfileDetailsContainer.getHeight();
            final float tabOutlineAlphaFactor;
            if ((offset - spaceHeight) > 0) {
                tabOutlineAlphaFactor = 1f - MathUtils.clamp((offset - spaceHeight) / profileContentHeight, 0, 1);
            } else {
                tabOutlineAlphaFactor = 1f;
            }
            mActionBarBackground.setOutlineAlphaFactor(tabOutlineAlphaFactor);

            final FragmentActivity activity = getActivity();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                setCompatToolbarOverlayAlpha(activity, factor * tabOutlineAlphaFactor);
            }

            final int color = mActionBarBackground.getColor();

            if (activity instanceof IThemedActivity) {
                final Drawable drawable = mPagerIndicator.getBackground();
                final int stackedTabColor;
                if (ThemeUtils.isDarkTheme(((IThemedActivity) activity).getCurrentThemeResourceId())) {
                    stackedTabColor = getResources().getColor(R.color.background_color_action_bar_dark);
                    final int contrastColor = ColorUtils.getContrastYIQ(stackedTabColor, 192);
                    mPagerIndicator.setIconColor(contrastColor);
                    mPagerIndicator.setLabelColor(contrastColor);
                    mPagerIndicator.setStripColor(color);
                } else if (drawable instanceof ColorDrawable) {
                    stackedTabColor = color;
                    final int tabColor = (Integer) sArgbEvaluator.evaluate(tabOutlineAlphaFactor, stackedTabColor, mCardBackgroundColor);
                    ((ColorDrawable) drawable).setColor(tabColor);
                    final int contrastColor = ColorUtils.getContrastYIQ(tabColor, 192);
                    mPagerIndicator.setIconColor(contrastColor);
                    mPagerIndicator.setLabelColor(contrastColor);
                    mPagerIndicator.setStripColor(contrastColor);
                }
            } else {
                final int contrastColor = ColorUtils.getContrastYIQ(color, 192);
                mPagerIndicator.setIconColor(contrastColor);
                mPagerIndicator.setLabelColor(contrastColor);
                mPagerIndicator.setStripColor(contrastColor);
            }
            mPagerIndicator.updateAppearance();
        }
        updateTitleColor();
    }

    private static void setCompatToolbarOverlayAlpha(FragmentActivity activity, float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        final Drawable drawable = ThemeUtils.getCompatToolbarOverlay(activity);
        if (drawable == null) return;
        drawable.setAlpha(Math.round(alpha * 255));
    }

    private void updateTitleColor() {
        final int[] location = new int[2];
        mNameView.getLocationOnScreen(location);
        final float nameShowingRatio = (mHeaderDrawerLayout.getPaddingTop() - location[1])
                / (float) mNameView.getHeight();
        final int textAlpha = Math.round(0xFF * MathUtils.clamp(nameShowingRatio, 0, 1));
        final FragmentActivity activity = getActivity();
        final SpannableStringBuilder spannedTitle;
        final CharSequence title = activity.getTitle();
        if (title instanceof SpannableStringBuilder) {
            spannedTitle = (SpannableStringBuilder) title;
        } else {
            spannedTitle = SpannableStringBuilder.valueOf(title);
        }
        final TextAlphaSpan[] spans = spannedTitle.getSpans(0, spannedTitle.length(), TextAlphaSpan.class);
        if (spans.length > 0) {
            spans[0].setAlpha(textAlpha);
        } else {
            spannedTitle.setSpan(new TextAlphaSpan(textAlpha), 0, spannedTitle.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        activity.setTitle(spannedTitle);
    }

    static class RelationshipLoader extends AsyncTaskLoader<SingleResponse<Relationship>> {

        private final Context context;
        private final long account_id, user_id;

        public RelationshipLoader(final Context context, final long account_id, final long user_id) {
            super(context);
            this.context = context;
            this.account_id = account_id;
            this.user_id = user_id;
        }

        @Override
        public SingleResponse<Relationship> loadInBackground() {
            if (account_id == user_id) return SingleResponse.getInstance();
            final Twitter twitter = getTwitterInstance(context, account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Relationship relationship = twitter.showFriendship(account_id, user_id);
                if (relationship.isSourceBlockingTarget() || relationship.isSourceBlockedByTarget()) {
                    Utils.setLastSeen(context, user_id, -1);
                } else {
                    Utils.setLastSeen(context, user_id, System.currentTimeMillis());
                }
                Utils.updateRelationship(context, relationship, account_id);
                return SingleResponse.getInstance(relationship);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    private static class ActionBarDrawable extends LayerDrawable {

        private final Drawable mShadowDrawable;
        private final ColorDrawable mColorDrawable;

        private float mFactor;
        private int mColor;
        private int mAlpha;
        private float mOutlineAlphaFactor;

        public ActionBarDrawable(Resources resources, Drawable shadow) {
            super(new Drawable[]{shadow, new ActionBarColorDrawable(true)});
            mShadowDrawable = getDrawable(0);
            mColorDrawable = (ColorDrawable) getDrawable(1);
            setAlpha(0xFF);
            setOutlineAlphaFactor(1);
        }

        public int getColor() {
            return mColor;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(Outline outline) {
            mColorDrawable.getOutline(outline);
            outline.setAlpha(mFactor * mOutlineAlphaFactor * 0.99f);
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
            setFactor(mFactor);
        }

        public void setOutlineAlphaFactor(float f) {
            mOutlineAlphaFactor = f;
            invalidateSelf();
        }

        @Override
        public int getIntrinsicWidth() {
            return mColorDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mColorDrawable.getIntrinsicHeight();
        }

        public void setColor(int color) {
            mColor = color;
            mColorDrawable.setColor(color);
            setFactor(mFactor);
        }

        public void setFactor(float f) {
            mFactor = f;
            mShadowDrawable.setAlpha(Math.round(mAlpha * MathUtils.clamp(1 - f, 0, 1)));
            final boolean hasColor = mColor != 0;
            mColorDrawable.setAlpha(hasColor ? Math.round(mAlpha * MathUtils.clamp(f, 0, 1)) : 0);
        }

    }


}
