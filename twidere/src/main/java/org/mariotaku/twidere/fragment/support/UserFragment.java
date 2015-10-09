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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meizu.flyme.reflect.StatusBarProxy;
import com.squareup.otto.Subscribe;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.activity.support.ThemedAppCompatActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.FriendshipUpdate;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.ActionBarColorDrawable;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereColorUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.util.message.FriendshipUpdatedEvent;
import org.mariotaku.twidere.util.message.FriendshipUserUpdatedEvent;
import org.mariotaku.twidere.util.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.util.support.ActivitySupport;
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.ProfileBannerImageView;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
import org.mariotaku.twidere.view.TwidereToolbar;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserFragment extends BaseSupportFragment implements OnClickListener,
        OnLinkClickListener, OnSizeChangedListener, OnSharedPreferenceChangeListener,
        OnTouchListener, DrawerCallback, SupportFragmentCallback, SystemWindowsInsetsCallback,
        RefreshScrollTopInterface, OnPageChangeListener, KeyboardShortcutCallback {

    private static final ArgbEvaluator sArgbEvaluator = new ArgbEvaluator();

    public static final String TRANSITION_NAME_PROFILE_IMAGE = "profile_image";
    public static final String TRANSITION_NAME_PROFILE_TYPE = "profile_type";
    public static final String TRANSITION_NAME_CARD = "card";

    private static final int LOADER_ID_USER = 1;
    private static final int LOADER_ID_FRIENDSHIP = 2;

    private static final int TAB_POSITION_STATUSES = 0;
    private static final int TAB_POSITION_MEDIA = 1;
    private static final int TAB_POSITION_FAVORITES = 2;
    private static final String TAB_TYPE_STATUSES = "statuses";
    private static final String TAB_TYPE_MEDIA = "media";
    private static final String TAB_TYPE_FAVORITES = "favorites";

    private ShapedImageView mProfileImageView;
    private ImageView mProfileTypeView;
    private ProfileBannerImageView mProfileBannerView;
    private View mProfileBirthdayBannerView;
    private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
            mListedCount, mFollowersCount, mFriendsCount, mHeaderErrorTextView;
    private View mDescriptionContainer, mLocationContainer, mURLContainer, mListedContainer, mFollowersContainer,
            mFriendsContainer;
    private ImageView mHeaderErrorIcon;
    private ColorLabelRelativeLayout mProfileNameContainer;
    private View mProgressContainer, mHeaderErrorContainer;
    private View mCardContent;
    private View mProfileBannerSpace;
    private TintedStatusFrameLayout mTintedStatusContent;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private View mPagerOverlay;
    private View mErrorOverlay;
    private View mProfileBannerContainer;
    private Button mFollowButton;
    private ProgressBar mFollowProgress;
    private View mPagesContent, mPagesErrorContainer;
    private ImageView mPagesErrorIcon;
    private TextView mPagesErrorText;
    private View mProfileNameBackground;
    private View mProfileDetailsContainer;
    private View mFollowingYouIndicator;


    private ActionBarDrawable mActionBarBackground;
    private SupportTabsAdapter mPagerAdapter;

    private ParcelableUser mUser;
    private Relationship mRelationship;
    private Locale mLocale;
    private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;
    private int mBannerWidth;
    private int mCardBackgroundColor;
    private int mActionBarShadowColor;
    private int mUiColor;
    private boolean mNameFirst;
    private int mPreviousTabItemIsDark, mPreviousActionBarItemIsDark;
    private boolean mHideBirthdayView;


    private final LoaderCallbacks<SingleResponse<Relationship>> mFriendshipLoaderCallbacks = new LoaderCallbacks<SingleResponse<Relationship>>() {

        @Override
        public Loader<SingleResponse<Relationship>> onCreateLoader(final int id, final Bundle args) {
            invalidateOptionsMenu();
            mFollowButton.setVisibility(View.GONE);
            mFollowProgress.setVisibility(View.VISIBLE);
            mFollowingYouIndicator.setVisibility(View.GONE);
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
            showRelationship(user, relationship);
        }

    };

    private void showRelationship(ParcelableUser user, Relationship relationship) {
        mRelationship = relationship;
        if (user == null) return;
        invalidateOptionsMenu();
        final boolean isMyself = user.account_id == user.id;
        if (isMyself) {
            mFollowButton.setText(R.string.edit);
            mFollowButton.setVisibility(View.VISIBLE);
        } else if (relationship != null) {
            mFollowButton.setEnabled(!relationship.isSourceBlockedByTarget());
            if (relationship.isSourceBlockedByTarget()) {
                mPagesErrorContainer.setVisibility(View.VISIBLE);
                final String displayName = mUserColorNameManager.getDisplayName(user, mNameFirst, true);
                mPagesErrorText.setText(getString(R.string.blocked_by_user_summary, displayName));
                mPagesErrorIcon.setImageResource(R.drawable.ic_info_error_generic);
                mPagesContent.setVisibility(View.GONE);
            } else if (!relationship.isSourceFollowingTarget() && user.is_protected) {
                mPagesErrorContainer.setVisibility(View.VISIBLE);
                final String displayName = mUserColorNameManager.getDisplayName(user, mNameFirst, true);
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
            } else if (relationship.isSourceFollowingTarget()) {
                mFollowButton.setText(R.string.unfollow);
            } else if (user.is_follow_request_sent) {
                mFollowButton.setText(R.string.requested);
            } else {
                mFollowButton.setText(R.string.follow);
            }
            mFollowButton.setCompoundDrawablePadding(Math.round(mFollowButton.getTextSize() * 0.25f));
            mFollowingYouIndicator.setVisibility(relationship.isTargetFollowingSource() ? View.VISIBLE : View.GONE);

            final ContentResolver resolver = getContentResolver();
            final ContentValues cachedValues = ContentValuesCreator.makeCachedUserContentValues(user);
            resolver.insert(CachedUsers.CONTENT_URI, cachedValues);
            mFollowButton.setVisibility(View.VISIBLE);
        } else {
            mFollowButton.setText(null);
            mFollowButton.setVisibility(View.GONE);
            mPagesErrorContainer.setVisibility(View.GONE);
            mPagesContent.setVisibility(View.VISIBLE);
        }
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
                mHeaderErrorContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.VISIBLE);
                mHeaderErrorTextView.setText(null);
                mHeaderErrorTextView.setVisibility(View.GONE);
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
                mHeaderErrorContainer.setVisibility(View.GONE);
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
                mHeaderErrorContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.GONE);
                setListShown(true);
                displayUser(mUser);
            } else {
                if (data.hasException()) {
                    mHeaderErrorTextView.setText(Utils.getErrorMessage(getActivity(), data.getException()));
                    mHeaderErrorTextView.setVisibility(View.VISIBLE);
                }
                mCardContent.setVisibility(View.GONE);
                mHeaderErrorContainer.setVisibility(View.VISIBLE);
                mProgressContainer.setVisibility(View.GONE);
            }
            setProgressBarIndeterminateVisibility(false);
        }

    };

    @Override
    public boolean canScroll(float dy) {
        final Fragment fragment = getCurrentVisibleFragment();
        return fragment instanceof DrawerCallback && ((DrawerCallback) fragment).canScroll(dy);
    }

    @Override
    public void cancelTouch() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).cancelTouch();
        }
    }

    @Override
    public void fling(float velocity) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).fling(velocity);
        }
    }

    @Override
    public boolean isScrollContent(float x, float y) {
        final View v = mViewPager;
        final int[] location = new int[2];
        v.getLocationInWindow(location);
        return x >= location[0] && x <= location[0] + v.getWidth()
                && y >= location[1] && y <= location[1] + v.getHeight();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        updateSubtitle();
    }

    private void updateSubtitle() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        final ParcelableUser user = mUser;
        if (user == null) {
            actionBar.setSubtitle(null);
            return;
        }
        final SupportTabSpec spec = mPagerAdapter.getTab(mViewPager.getCurrentItem());
        switch (spec.type) {
            case TAB_TYPE_STATUSES: {
                actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_statuses,
                        (int) user.statuses_count, user.statuses_count));
                break;
            }
            case TAB_TYPE_MEDIA: {
                actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_media,
                        (int) user.media_count, user.media_count));
                break;
            }
            case TAB_TYPE_FAVORITES: {
                actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_favorites,
                        (int) user.favorites_count, user.favorites_count));
                break;
            }
            default: {
                actionBar.setSubtitle(null);
                break;
            }
        }
        updateTitleAlpha();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void scrollBy(float dy) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).scrollBy(dy);
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
    public void topChanged(int top) {
        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        if (drawer == null) return;
        final int offset = drawer.getPaddingTop() - top;
        updateScrollOffset(offset);

        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof DrawerCallback) {
            ((DrawerCallback) fragment).topChanged(top);
        }
    }

    public void displayUser(final ParcelableUser user) {
        mUser = user;
        final FragmentActivity activity = getActivity();
        if (user == null || user.id <= 0 || activity == null) return;
        final Resources resources = getResources();
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final boolean userIsMe = user.account_id == user.id;
        mCardContent.setVisibility(View.VISIBLE);
        mHeaderErrorContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
        mUser = user;
        final int userColor = mUserColorNameManager.getUserColor(user.id, true);
        mProfileImageView.setBorderColor(userColor != 0 ? userColor : Color.WHITE);
        mProfileNameContainer.drawEnd(Utils.getAccountColor(activity, user.account_id));
        final String nick = mUserColorNameManager.getUserNickname(user.id, true);
        mNameView.setText(TextUtils.isEmpty(nick) ? user.name : getString(R.string.name_with_nickname, user.name, nick));
        final int typeIconRes = Utils.getUserTypeIconRes(user.is_verified, user.is_protected);
        if (typeIconRes != 0) {
            mProfileTypeView.setImageResource(typeIconRes);
            mProfileTypeView.setVisibility(View.VISIBLE);
        } else {
            mProfileTypeView.setImageDrawable(null);
            mProfileTypeView.setVisibility(View.GONE);
        }
        mScreenNameView.setText("@" + user.screen_name);
        mDescriptionContainer.setVisibility(TextUtils.isEmpty(user.description_html) ? View.GONE : View.VISIBLE);
        mDescriptionView.setText(user.description_html != null ? Html.fromHtml(user.description_html) : user.description_plain);
        final TwidereLinkify linkify = new TwidereLinkify(this);
        linkify.applyAllLinks(mDescriptionView, user.account_id, false);
        mDescriptionView.setMovementMethod(null);
        mLocationContainer.setVisibility(TextUtils.isEmpty(user.location) ? View.GONE : View.VISIBLE);
        mLocationView.setText(user.location);
        mURLContainer.setVisibility(TextUtils.isEmpty(user.url) && TextUtils.isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        mURLView.setText(TextUtils.isEmpty(user.url_expanded) ? user.url : user.url_expanded);
        mURLView.setMovementMethod(null);
        final String createdAt = Utils.formatToLongTimeString(activity, user.created_at);
        final float daysSinceCreation = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24;
        final int dailyTweets = Math.round(user.statuses_count / Math.max(1, daysSinceCreation));
        mCreatedAtView.setText(resources.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                createdAt, dailyTweets));
        mListedCount.setText(Utils.getLocalizedNumber(mLocale, user.listed_count));
        mFollowersCount.setText(Utils.getLocalizedNumber(mLocale, user.followers_count));
        mFriendsCount.setText(Utils.getLocalizedNumber(mLocale, user.friends_count));

        mMediaLoader.displayProfileImage(mProfileImageView, Utils.getOriginalTwitterProfileImage(user.profile_image_url));
        if (userColor != 0) {
            setUiColor(userColor);
        } else {
            setUiColor(user.link_color);
        }
        final int defWidth = resources.getDisplayMetrics().widthPixels;
        final int width = mBannerWidth > 0 ? mBannerWidth : defWidth;
        mMediaLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
        final Relationship relationship = mRelationship;
        if (relationship == null || relationship.getTargetUserId() != user.id) {
            getFriendship();
        }
        activity.setTitle(mUserColorNameManager.getDisplayName(user, mNameFirst, true));

        Calendar cal = Calendar.getInstance();
        final int currentMonth = cal.get(Calendar.MONTH), currentDay = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(user.created_at);
        if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.DAY_OF_MONTH) == currentDay && !mHideBirthdayView) {
            mProfileBirthdayBannerView.setVisibility(View.VISIBLE);
        } else {
            mProfileBirthdayBannerView.setVisibility(View.GONE);
        }
        updateTitleAlpha();
        invalidateOptionsMenu();
        updateSubtitle();
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

    public ParcelableUser getUser() {
        return mUser;
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
            mHeaderErrorContainer.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void notifyFriendshipUpdated(FriendshipUpdatedEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || event.accountId != user.account_id || event.userId != user.id) return;
        getFriendship();
    }

    @Subscribe
    public void notifyFriendshipUserUpdated(FriendshipUserUpdatedEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || !event.user.equals(user)) return;
        getFriendship();
    }

    @Subscribe
    public void notifyProfileUpdated(ProfileUpdatedEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || !user.equals(event.user)) return;
        displayUser(event.user);
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
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
                    mUserColorNameManager.setUserColor(mUser.id, color);
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    mUserColorNameManager.clearUserColor(mUser.id);
                }
                break;
            }
            case REQUEST_ADD_TO_LIST: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
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
                    Utils.openUserProfile(getActivity(), accountId, user.id, user.screen_name, null);
                }
                break;
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mTintedStatusContent = (TintedStatusFrameLayout) ((FragmentActivity) context).findViewById(R.id.main_content);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
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
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        mLocale = getResources().getConfiguration().locale;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(activity,
                ThemeUtils.getThemeBackgroundOption(activity),
                ThemeUtils.getUserThemeBackgroundAlpha(activity));
        mActionBarShadowColor = 0xA0000000;
        final TwidereApplication app = TwidereApplication.getInstance(activity);
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
        mPagerIndicator.setOnPageChangeListener(this);

        mFollowButton.setOnClickListener(this);
        mProfileImageView.setOnClickListener(this);
        mProfileBannerView.setOnClickListener(this);
        mListedContainer.setOnClickListener(this);
        mFollowersContainer.setOnClickListener(this);
        mFriendsContainer.setOnClickListener(this);
        mHeaderErrorIcon.setOnClickListener(this);
        mProfileBirthdayBannerView.setOnClickListener(this);
        mProfileBannerView.setOnSizeChangedListener(this);
        mProfileBannerSpace.setOnTouchListener(this);


        mProfileNameBackground.setBackgroundColor(mCardBackgroundColor);
        mProfileDetailsContainer.setBackgroundColor(mCardBackgroundColor);
        mPagerIndicator.setBackgroundColor(mCardBackgroundColor);

        final float actionBarElevation = ThemeUtils.getSupportActionBarElevation(activity);
        ViewCompat.setElevation(mPagerIndicator, actionBarElevation);

        if (activity instanceof IThemedActivity) {
            ViewSupport.setBackground(mPagerOverlay, ThemeUtils.getNormalWindowContentOverlay(activity,
                    ((IThemedActivity) activity).getCurrentThemeResourceId()));
            ViewSupport.setBackground(mErrorOverlay, ThemeUtils.getNormalWindowContentOverlay(activity,
                    ((IThemedActivity) activity).getCurrentThemeResourceId()));
        }

        setupBaseActionBar();
        setupUserPages();
        if (activity instanceof IThemedActivity) {
            setUiColor(((IThemedActivity) activity).getCurrentThemeColor());
        }

        getUserInfo(accountId, userId, screenName, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER, getUser());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
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
        inflater.inflate(R.menu.menu_user_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUser user = getUser();
        final Relationship relationship = mRelationship;
        if (twitter == null || user == null) return;

        final boolean isMyself = user.account_id == user.id;
        final MenuItem mentionItem = menu.findItem(R.id.mention);
        if (mentionItem != null) {
            final String displayName = mUserColorNameManager.getDisplayName(user, mNameFirst, true);
            mentionItem.setTitle(getString(R.string.mention_user_name, displayName));
        }
        MenuUtils.setMenuItemAvailability(menu, R.id.mention, !isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.incoming_friendships, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.saved_searches, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.scheduled_statuses, isMyself
                && Utils.getOfficialKeyType(getActivity(), user.account_id) == ConsumerKeyType.TWEETDECK);
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
            MenuUtils.setMenuItemAvailability(menu, R.id.send_direct_message, relationship.canSourceDMTarget());
            MenuUtils.setMenuItemAvailability(menu, R.id.block, true);
            MenuUtils.setMenuItemAvailability(menu, R.id.mute_user, true);
            final MenuItem blockItem = menu.findItem(R.id.block);
            if (blockItem != null) {
                final boolean blocking = relationship.isSourceBlockingTarget();
                ActionIconDrawable.setMenuHighlight(blockItem, new TwidereMenuInfo(blocking));
                blockItem.setTitle(blocking ? R.string.unblock : R.string.block);
            }
            final MenuItem muteItem = menu.findItem(R.id.mute_user);
            if (muteItem != null) {
                final boolean muting = relationship.isSourceMutingTarget();
                ActionIconDrawable.setMenuHighlight(muteItem, new TwidereMenuInfo(muting));
                muteItem.setTitle(muting ? R.string.unmute : R.string.mute);
            }
            final MenuItem filterItem = menu.findItem(R.id.add_to_filter);
            if (filterItem != null) {
                final boolean filtering = Utils.isFilteringUser(getActivity(), user.id);
                ActionIconDrawable.setMenuHighlight(filterItem, new TwidereMenuInfo(filtering));
                filterItem.setTitle(filtering ? R.string.remove_from_filter : R.string.add_to_filter);
            }
            final MenuItem wantRetweetsItem = menu.findItem(R.id.enable_retweets);
            if (wantRetweetsItem != null) {

                wantRetweetsItem.setChecked(relationship.isSourceWantRetweetsFromTarget());
            }
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.send_direct_message, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.enable_retweets, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.block, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.mute_user, false);
            MenuUtils.setMenuItemAvailability(menu, R.id.report_spam, false);
        }
        MenuUtils.setMenuItemAvailability(menu, R.id.muted_users, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.blocked_users, isMyself);
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
        final Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_USER, user);
        intent.putExtras(extras);
        menu.removeGroup(MENU_GROUP_USER_EXTENSION);
        Utils.addIntentToMenu(getActivity(), menu, intent, MENU_GROUP_USER_EXTENSION);
        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        if (drawer != null) {
            final int offset = drawer.getPaddingTop() - drawer.getHeaderTop();
            mPreviousActionBarItemIsDark = 0;
            mPreviousTabItemIsDark = 0;
            updateScrollOffset(offset);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUser user = getUser();
        final Relationship relationship = mRelationship;
        if (user == null || twitter == null) return false;
        switch (item.getItemId()) {
            case R.id.block: {
                if (mRelationship != null) {
                    if (mRelationship.isSourceBlockingTarget()) {
                        twitter.destroyBlockAsync(user.account_id, user.id);
                    } else {
                        CreateUserBlockDialogFragment.show(getFragmentManager(), user);
                    }
                }
                break;
            }
            case R.id.report_spam: {
                ReportSpamDialogFragment.show(getFragmentManager(), user);
                break;
            }
            case R.id.add_to_filter: {
                final boolean filtering = Utils.isFilteringUser(getActivity(), user.id);
                final ContentResolver cr = getContentResolver();
                if (filtering) {
                    final Expression where = Expression.equals(Filters.Users.USER_ID, user.id);
                    cr.delete(Filters.Users.CONTENT_URI, where.getSQL(), null);
                    Utils.showInfoMessage(getActivity(), R.string.message_user_unmuted, false);
                } else {
                    cr.insert(Filters.Users.CONTENT_URI, ContentValuesCreator.createFilteredUser(user));
                    Utils.showInfoMessage(getActivity(), R.string.message_user_muted, false);
                }
                break;
            }
            case R.id.mute_user: {
                if (mRelationship != null) {
                    if (mRelationship.isSourceMutingTarget()) {
                        twitter.destroyMuteAsync(user.account_id, user.id);
                    } else {
                        CreateUserMuteDialogFragment.show(getFragmentManager(), user);
                    }
                }
                break;
            }
            case R.id.mention: {
                final Intent intent = new Intent(INTENT_ACTION_MENTION);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case R.id.send_direct_message: {
                final Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME_TWIDERE);
                builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
                builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user.id));
                final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
                intent.putExtra(EXTRA_ACCOUNT, ParcelableCredentials.getCredentials(getActivity(), user.account_id));
                intent.putExtra(EXTRA_USER, user);
                startActivity(intent);
                break;
            }
            case R.id.set_color: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, mUserColorNameManager.getUserColor(user.id, true));
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                intent.putExtra(EXTRA_CLEAR_BUTTON, true);
                startActivityForResult(intent, REQUEST_SET_COLOR);
                break;
            }
            case R.id.clear_nickname: {
                mUserColorNameManager.clearUserNickname(user.id);
                break;
            }
            case R.id.set_nickname: {
                final String nick = mUserColorNameManager.getUserNickname(user.id, true);
                SetUserNicknameDialogFragment.show(getFragmentManager(), user.id, nick);
                break;
            }
            case R.id.add_to_list: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER_LIST);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_ID, user.account_id);
                intent.putExtra(EXTRA_SCREEN_NAME, Utils.getAccountScreenName(getActivity(), user.account_id));
                startActivityForResult(intent, REQUEST_ADD_TO_LIST);
                break;
            }
            case R.id.open_with_account: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
                intent.setClass(getActivity(), AccountSelectorActivity.class);
                intent.putExtra(EXTRA_SINGLE_SELECTION, true);
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
                break;
            }
            case R.id.follow: {
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
            case R.id.enable_retweets: {
                final boolean newState = !item.isChecked();
                final FriendshipUpdate update = new FriendshipUpdate();
                update.retweets(newState);
                twitter.updateFriendship(user.account_id, user.id, update);
                item.setChecked(newState);
                return true;
            }
            case R.id.muted_users: {
                Utils.openMutesUsers(getActivity(), user.account_id);
                return true;
            }
            case R.id.blocked_users: {
                Utils.openUserBlocks(getActivity(), user.account_id);
                return true;
            }
            case R.id.incoming_friendships: {
                Utils.openIncomingFriendships(getActivity(), user.account_id);
                return true;
            }
            case R.id.user_mentions: {
                Utils.openUserMentions(getActivity(), user.account_id, user.screen_name);
                return true;
            }
            case R.id.saved_searches: {
                Utils.openSavedSearches(getActivity(), user.account_id);
                return true;
            }
            case R.id.scheduled_statuses: {
                Utils.openScheduledStatuses(getActivity(), user.account_id);
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
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mHeaderDrawerLayout = (HeaderDrawerLayout) view.findViewById(R.id.user_profile_drawer);
        final View headerView = mHeaderDrawerLayout.getHeader();
        final View contentView = mHeaderDrawerLayout.getContent();
        mCardContent = headerView.findViewById(R.id.card_content);
        mHeaderErrorContainer = headerView.findViewById(R.id.error_container);
        mHeaderErrorTextView = (TextView) headerView.findViewById(R.id.error_text);
        mHeaderErrorIcon = (ImageView) headerView.findViewById(R.id.error_icon);
        mProgressContainer = headerView.findViewById(R.id.progress_container);
        mProfileBannerView = (ProfileBannerImageView) view.findViewById(R.id.profile_banner);
        mProfileBirthdayBannerView = view.findViewById(R.id.profile_birthday_banner);
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
        mPagerOverlay = contentView.findViewById(R.id.pager_window_overlay);
        mErrorOverlay = contentView.findViewById(R.id.error_window_overlay);
        mFollowButton = (Button) headerView.findViewById(R.id.follow);
        mFollowProgress = (ProgressBar) headerView.findViewById(R.id.follow_progress);
        mPagesContent = view.findViewById(R.id.pages_content);
        mPagesErrorContainer = view.findViewById(R.id.pages_error_container);
        mPagesErrorIcon = (ImageView) view.findViewById(R.id.pages_error_icon);
        mPagesErrorText = (TextView) view.findViewById(R.id.pages_error_text);
        mProfileNameBackground = view.findViewById(R.id.profile_name_background);
        mProfileDetailsContainer = view.findViewById(R.id.profile_details_container);
        mFollowingYouIndicator = view.findViewById(R.id.following_you_indicator);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_NAVIGATION_PREVIOUS_TAB: {
                    final int previous = mViewPager.getCurrentItem() - 1;
                    if (previous >= 0 && previous < mPagerAdapter.getCount()) {
                        mViewPager.setCurrentItem(previous, true);
                    }
                    return true;
                }
                case ACTION_NAVIGATION_NEXT_TAB: {
                    final int next = mViewPager.getCurrentItem() + 1;
                    if (next >= 0 && next < mPagerAdapter.getCount()) {
                        mViewPager.setCurrentItem(next, true);
                    }
                    return true;
                }
            }
        }
        return handler.handleKey(getActivity(), null, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_NAVIGATION_PREVIOUS_TAB:
                case ACTION_NAVIGATION_NEXT_TAB:
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    private boolean handleFragmentKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler,
                                                         int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler,
                                                         int keyCode, @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
        }
        return false;
    }

    private boolean isFragmentKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler,
                                                      int keyCode, @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).isKeyboardShortcutHandled(handler, keyCode, event, metaState);
        }
        return false;
    }

    private Fragment getKeyboardShortcutRecipient() {
        return getCurrentVisibleFragment();
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        final ThemedAppCompatActivity activity = (ThemedAppCompatActivity) getActivity();
        mHeaderDrawerLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        final String backgroundOption = activity.getCurrentThemeBackgroundOption();
        final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(backgroundOption);
        mHeaderDrawerLayout.setClipToPadding(isTransparentBackground);
    }

    @Override
    public void onClick(final View view) {
        final FragmentActivity activity = getActivity();
        final ParcelableUser user = getUser();
        if (activity == null || user == null) return;
        switch (view.getId()) {
            case R.id.error_container: {
                getUserInfo(true);
                break;
            }
            case R.id.follow: {
                if (user.id == user.account_id) {
                    Utils.openProfileEditor(getActivity(), user.account_id);
                    break;
                }
                final Relationship relationship = mRelationship;
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
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
                final String url = Utils.getOriginalTwitterProfileImage(user.profile_image_url);
                final ParcelableMedia[] media = {ParcelableMedia.newImage(url, url)};
                //TODO open media animation
                Bundle options = null;
                Utils.openMedia(activity, user.account_id, false, null, media, options);
                break;
            }
            case R.id.profile_banner: {
                if (user.profile_banner_url == null) return;
                final String url = user.profile_banner_url + "/ipad_retina";
                final ParcelableMedia[] media = {ParcelableMedia.newImage(url, url)};
                //TODO open media animation
                Bundle options = null;
                Utils.openMedia(activity, user.account_id, false, null, media, options);
                break;
            }
            case R.id.listed_container: {
                Utils.openUserLists(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.followers_container: {
                Utils.openUserFollowers(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.friends_container: {
                Utils.openUserFriends(getActivity(), user.account_id, user.id, user.screen_name);
                break;
            }
            case R.id.name_container: {
                if (user.account_id != user.id) return;
                Utils.openProfileEditor(getActivity(), user.account_id);
                break;
            }
            case R.id.profile_birthday_banner: {
                mHideBirthdayView = true;
                mProfileBirthdayBannerView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                mProfileBirthdayBannerView.setVisibility(View.GONE);
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
                Utils.openUserProfile(getActivity(), user.account_id, -1, link, null);
                break;
            }
            case TwidereLinkify.LINK_TYPE_HASHTAG: {
                Utils.openTweetSearch(getActivity(), user.account_id, "#" + link);
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
                Utils.openStatus(getActivity(), accountId, ParseUtils.parseLong(link));
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
        if (mProfileBirthdayBannerView.getVisibility() == View.VISIBLE) {
            return mProfileBirthdayBannerView.dispatchTouchEvent(event);
        }
        return mProfileBannerView.dispatchTouchEvent(event);
    }

    @Override
    public boolean scrollToStart() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (!(fragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) fragment).scrollToStart();
        return true;
    }

    @Override
    public boolean triggerRefresh() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (!(fragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) fragment).triggerRefresh();
        return true;
    }

    public void setListShown(boolean shown) {
        final TintedStatusFrameLayout tintedStatus = mTintedStatusContent;
        if (tintedStatus == null) return;
//        tintedStatus.setDrawShadow(shown);
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

    private static void setCompatToolbarOverlayAlpha(FragmentActivity activity, float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        final View windowOverlay = activity.findViewById(R.id.window_overlay);
        if (windowOverlay != null) {
            windowOverlay.setAlpha(alpha);
            return;
        }
        final Drawable drawable = ThemeUtils.getCompatToolbarOverlay(activity);
        if (drawable == null) return;
        drawable.setAlpha(Math.round(alpha * 255));
    }

    private void setUiColor(int color) {
        mUiColor = color;
        if (mActionBarBackground == null) {
            setupBaseActionBar();
        }
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        final IThemedActivity themed = (IThemedActivity) activity;
        final int themeRes = themed.getCurrentThemeResourceId();
        final String backgroundOption = themed.getThemeBackgroundOption();
        final int actionBarColor = ThemeUtils.getActionBarColor(activity, color, themeRes, backgroundOption);
        if (mTintedStatusContent != null) {
            final int alpha = ThemeUtils.isTransparentBackground(backgroundOption) ? themed.getCurrentThemeBackgroundAlpha() : 0xFF;
            mTintedStatusContent.setColor(actionBarColor, ThemeUtils.getActionBarAlpha(alpha));
        }
        if (mActionBarBackground != null) {
            mActionBarBackground.setColor(actionBarColor);
        }
        ActivitySupport.setTaskDescription(activity, new TaskDescriptionCompat(null, null, actionBarColor));
        final int optimalLinkColor = ThemeUtils.getOptimalLinkColor(color,
                mDescriptionView.getCurrentTextColor());
        mDescriptionView.setLinkTextColor(optimalLinkColor);
        mLocationView.setLinkTextColor(optimalLinkColor);
        mURLView.setLinkTextColor(optimalLinkColor);
        mProfileBannerView.setBackgroundColor(color);
        ViewSupport.setBackground(mPagerIndicator, ThemeUtils.getActionBarStackedBackground(activity,
                themeRes, color, backgroundOption, true));

        final HeaderDrawerLayout drawer = mHeaderDrawerLayout;
        if (drawer != null) {
            final int offset = drawer.getPaddingTop() - drawer.getHeaderTop();
            updateScrollOffset(offset);
        }
    }

    private void setupBaseActionBar() {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof LinkHandlerActivity)) return;
        final LinkHandlerActivity linkHandler = (LinkHandlerActivity) activity;
        final ActionBarContainer actionBarContainer = linkHandler.getActionBarContainer();
        final ActionBar actionBar = linkHandler.getSupportActionBar();
        if (actionBarContainer == null || actionBar == null) return;
        final Drawable shadow = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.shadow_user_banner_action_bar, null);
        mActionBarBackground = new ActionBarDrawable(shadow);
        if (!ThemeUtils.isWindowFloating(linkHandler, linkHandler.getCurrentThemeResourceId())
                && ThemeUtils.isTransparentBackground(linkHandler.getCurrentThemeBackgroundOption())) {
//            mActionBarBackground.setAlpha(ThemeUtils.getActionBarAlpha(linkHandler.getCurrentThemeBackgroundAlpha()));
            mProfileBannerView.setAlpha(linkHandler.getCurrentThemeBackgroundAlpha() / 255f);
        }
        actionBarContainer.setPrimaryBackground(mActionBarBackground);
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
        mPagerAdapter.addTab(UserTimelineFragment.class, tabArgs, getString(R.string.statuses), R.drawable.ic_action_quote, TAB_TYPE_STATUSES, TAB_POSITION_STATUSES, null);
        if (Utils.isOfficialKeyAccount(context, accountId)) {
            mPagerAdapter.addTab(UserMediaTimelineFragment.class, tabArgs, getString(R.string.media), R.drawable.ic_action_gallery, TAB_TYPE_MEDIA, TAB_POSITION_MEDIA, null);
        }
        mPagerAdapter.addTab(UserFavoritesFragment.class, tabArgs, getString(R.string.favorites), R.drawable.ic_action_star, TAB_TYPE_FAVORITES, TAB_POSITION_FAVORITES, null);
    }

    private void updateFollowProgressState() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
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

    private void updateRefreshState() {
        final ParcelableUser user = getUser();
        if (user == null) return;
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final boolean is_creating_friendship = twitter != null
                && twitter.isCreatingFriendship(user.account_id, user.id);
        final boolean is_destroying_friendship = twitter != null
                && twitter.isDestroyingFriendship(user.account_id, user.id);
        setProgressBarIndeterminateVisibility(is_creating_friendship || is_destroying_friendship);
        invalidateOptionsMenu();
    }

    private void updateScrollOffset(int offset) {
        final View space = mProfileBannerSpace;
        final ProfileBannerImageView profileBannerView = mProfileBannerView;
        final View profileBirthdayBannerView = mProfileBirthdayBannerView;
        final View profileBannerContainer = mProfileBannerContainer;
        final int spaceHeight = space.getHeight();
        final float factor = MathUtils.clamp(spaceHeight == 0 ? 0 : (offset / (float) spaceHeight), 0, 1);
//        profileBannerContainer.setTranslationY(Math.max(-offset, -spaceHeight));
//        profileBannerView.setTranslationY(Math.min(offset, spaceHeight) / 2);
        profileBannerContainer.setTranslationY(-offset);
        profileBannerView.setTranslationY(offset / 2);
        profileBirthdayBannerView.setTranslationY(offset / 2);

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

            final ThemedAppCompatActivity activity = (ThemedAppCompatActivity) getActivity();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                setCompatToolbarOverlayAlpha(activity, factor * tabOutlineAlphaFactor);
            }

            final Drawable tabBackground = mPagerIndicator.getBackground();
            final int themeId = activity.getCurrentThemeResourceId();
            int stackedTabColor = ThemeUtils.getActionBarColor(activity, mUiColor, themeId,
                    activity.getThemeBackgroundOption());

            if (ThemeUtils.isTransparentBackground(activity.getCurrentThemeBackgroundOption())) {
                stackedTabColor = ColorUtils.setAlphaComponent(stackedTabColor,
                        ThemeUtils.getActionBarAlpha(activity.getCurrentThemeBackgroundAlpha()));
            }
            final int tabColor = (Integer) sArgbEvaluator.evaluate(tabOutlineAlphaFactor, stackedTabColor, mCardBackgroundColor);
            ((ColorDrawable) tabBackground).setColor(tabColor);
            final boolean tabItemIsDark = TwidereColorUtils.getYIQLuminance(tabColor) > ThemeUtils.ACCENT_COLOR_THRESHOLD;

            if (mPreviousTabItemIsDark == 0 || (tabItemIsDark ? 1 : -1) != mPreviousTabItemIsDark) {
                final int[] primaryColors = new int[2];
                ThemeUtils.getDarkLightForegroundColors(activity, themeId, primaryColors);
                final int tabContrastColor = primaryColors[tabItemIsDark ? 0 : 1];
                mPagerIndicator.setIconColor(tabContrastColor);
                mPagerIndicator.setLabelColor(tabContrastColor);
                if (ThemeUtils.isDarkTheme(themeId)) {
                    mPagerIndicator.setStripColor(mUiColor);
                } else {
                    mPagerIndicator.setStripColor(tabContrastColor);
                }
                mPagerIndicator.updateAppearance();
            }
            mPreviousTabItemIsDark = (tabItemIsDark ? 1 : -1);

            final int barColor = (Integer) sArgbEvaluator.evaluate(factor, mActionBarShadowColor, stackedTabColor);
            final boolean actionItemIsDark = TwidereColorUtils.getYIQLuminance(barColor) > ThemeUtils.ACCENT_COLOR_THRESHOLD;
            if (mPreviousActionBarItemIsDark == 0 || (actionItemIsDark ? 1 : -1) != mPreviousActionBarItemIsDark) {
                StatusBarProxy.setStatusBarDarkIcon(activity.getWindow(), actionItemIsDark);
                final int contrastForegroundColor = ThemeUtils.getContrastForegroundColor(activity, themeId, barColor);
                final Toolbar actionBarView = activity.getActionBarToolbar();
                if (actionBarView != null) {
                    actionBarView.setTitleTextColor(contrastForegroundColor);
                    actionBarView.setSubtitleTextColor(contrastForegroundColor);
                    ThemeUtils.setActionBarOverflowColor(actionBarView, contrastForegroundColor);
                    ThemeUtils.wrapToolbarMenuIcon(ViewSupport.findViewByType(actionBarView, ActionMenuView.class),
                            contrastForegroundColor, contrastForegroundColor);
                    if (actionBarView instanceof TwidereToolbar) {
                        ((TwidereToolbar) actionBarView).setItemColor(contrastForegroundColor);
                    }
                }
            }
            mPreviousActionBarItemIsDark = actionItemIsDark ? 1 : -1;
        }
        updateTitleAlpha();
    }

    private void updateTitleAlpha() {
        final int[] location = new int[2];
        mNameView.getLocationInWindow(location);
        final float nameShowingRatio = (mHeaderDrawerLayout.getPaddingTop() - location[1])
                / (float) mNameView.getHeight();
        final float textAlpha = MathUtils.clamp(nameShowingRatio, 0, 1);
        final ThemedAppCompatActivity activity = (ThemedAppCompatActivity) getActivity();
        final Toolbar actionBarView = activity.getActionBarToolbar();
        if (actionBarView != null) {
            final TextView titleView = ViewSupport.findViewByText(actionBarView, actionBarView.getTitle());
            if (titleView != null) {
                titleView.setAlpha(textAlpha);
            }
            final TextView subtitleView = ViewSupport.findViewByText(actionBarView, actionBarView.getSubtitle());
            if (subtitleView != null) {
                subtitleView.setAlpha(textAlpha);
            }
        }
    }

    private static class ActionBarDrawable extends LayerDrawable {

        private final Drawable mShadowDrawable;
        private final ColorDrawable mColorDrawable;

        private float mFactor;
        private int mColor;
        private int mAlpha;
        private float mOutlineAlphaFactor;

        public ActionBarDrawable(Drawable shadow) {
            super(new Drawable[]{shadow, ActionBarColorDrawable.create(true)});
            mShadowDrawable = getDrawable(0);
            mColorDrawable = (ColorDrawable) getDrawable(1);
            setAlpha(0xFF);
            setOutlineAlphaFactor(1);
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(int color) {
            mColor = color;
            mColorDrawable.setColor(color);
            setFactor(mFactor);
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

        @Override
        public int getIntrinsicWidth() {
            return mColorDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mColorDrawable.getIntrinsicHeight();
        }

        public void setFactor(float f) {
            mFactor = f;
            mShadowDrawable.setAlpha(Math.round(mAlpha * MathUtils.clamp(1 - f, 0, 1)));
            final boolean hasColor = mColor != 0;
            mColorDrawable.setAlpha(hasColor ? Math.round(mAlpha * MathUtils.clamp(f, 0, 1)) : 0);
        }

        public void setOutlineAlphaFactor(float f) {
            mOutlineAlphaFactor = f;
            invalidateSelf();
        }

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
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Relationship relationship = twitter.showFriendship(user_id);
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

}
