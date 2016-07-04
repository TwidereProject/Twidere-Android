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

package org.mariotaku.twidere.fragment;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
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
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Pair;
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
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.ObjectUtils;
import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.FriendshipUpdate;
import org.mariotaku.microblog.library.twitter.model.Relationship;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.AccountSelectorActivity;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.activity.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.LinkHandlerActivity;
import org.mariotaku.twidere.activity.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.ActionBarColorDrawable;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.loader.ParcelableUserLoader;
import org.mariotaku.twidere.model.CachedRelationship;
import org.mariotaku.twidere.model.CachedRelationshipValuesCreator;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.ParcelableUserValuesCreator;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.message.FriendshipUpdatedEvent;
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.model.message.TaskStateChangedEvent;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableMediaUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.UserColorNameManager.UserColorChangedListener;
import org.mariotaku.twidere.util.UserColorNameManager.UserNicknameChangedListener;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.util.support.ActivitySupport;
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.WindowSupport;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ExtendedRelativeLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.ProfileBannerImageView;
import org.mariotaku.twidere.view.ProfileBannerSpace;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import java.util.Calendar;
import java.util.Locale;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.UserEvent;

public class UserFragment extends BaseSupportFragment implements OnClickListener, OnLinkClickListener,
        OnSizeChangedListener, OnTouchListener, DrawerCallback, SupportFragmentCallback,
        SystemWindowsInsetsCallback, RefreshScrollTopInterface, OnPageChangeListener, KeyboardShortcutCallback,
        UserColorChangedListener, UserNicknameChangedListener, IToolBarSupportFragment {

    private static final ArgbEvaluator sArgbEvaluator = new ArgbEvaluator();
    private static final int LOADER_ID_USER = 1;
    private static final int LOADER_ID_FRIENDSHIP = 2;

    private static final int TAB_POSITION_STATUSES = 0;
    private static final int TAB_POSITION_MEDIA = 1;
    private static final int TAB_POSITION_FAVORITES = 2;
    private static final String TAB_TYPE_STATUSES = "statuses";
    private static final String TAB_TYPE_MEDIA = "media";
    private static final String TAB_TYPE_FAVORITES = "favorites";

    // Views
    private ShapedImageView mProfileImageView;
    private ImageView mProfileTypeView;
    private ProfileBannerImageView mProfileBannerView;
    private View mProfileBirthdayBannerView;
    private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
            mHeaderErrorTextView;
    private TextView mListedCount, mFollowersCount, mFriendsCount, mGroupsCount;
    private View mDescriptionContainer, mLocationContainer, mURLContainer;
    private View mListedContainer, mFollowersContainer, mFriendsContainer, mGroupsContainer;
    private ImageView mHeaderErrorIcon;
    private ColorLabelRelativeLayout mProfileNameContainer;
    private View mProgressContainer, mHeaderErrorContainer;
    private View mCardContent;
    private ProfileBannerSpace mProfileBannerSpace;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private View mWindowOverlay;
    private View mProfileBannerContainer;
    private ExtendedRelativeLayout mProfileContentContainer;
    private Button mFollowButton;
    private ProgressBar mFollowProgress;
    private View mPagesContent, mPagesErrorContainer;
    private ImageView mPagesErrorIcon;
    private TextView mPagesErrorText;
    private View mProfileNameBackground;
    private View mProfileDetailsContainer;
    private View mFollowingYouIndicator;
    private Toolbar mToolbar;
    private TintedStatusFrameLayout mTintedStatusFrameLayout;

    private ActionBarDrawable mActionBarBackground;
    private SupportTabsAdapter mPagerAdapter;

    // Data fields
    private ParcelableUser mUser;
    private ParcelableAccount mAccount;
    private UserRelationship mRelationship;
    private Locale mLocale;
    private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;
    private int mBannerWidth;
    private int mCardBackgroundColor;
    private int mActionBarShadowColor;
    private int mUiColor, mPrimaryColor, mPrimaryColorDark;
    private boolean mNameFirst;
    private int mPreviousTabItemIsDark, mPreviousActionBarItemIsDark;
    private boolean mHideBirthdayView;
    private UserEvent mUserEvent;

    private final LoaderCallbacks<SingleResponse<UserRelationship>> mFriendshipLoaderCallbacks =
            new LoaderCallbacks<SingleResponse<UserRelationship>>() {

                @Override
                public Loader<SingleResponse<UserRelationship>> onCreateLoader(final int id, final Bundle args) {
                    invalidateOptionsMenu();
                    final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
                    final ParcelableUser user = args.getParcelable(EXTRA_USER);
                    if (user != null && user.key.equals(accountKey)) {
                        mFollowingYouIndicator.setVisibility(View.GONE);
                        mFollowButton.setVisibility(View.VISIBLE);
                        mFollowProgress.setVisibility(View.VISIBLE);
                    } else {
                        mFollowingYouIndicator.setVisibility(View.GONE);
                        mFollowButton.setVisibility(View.GONE);
                        mFollowProgress.setVisibility(View.VISIBLE);
                    }
                    return new UserRelationshipLoader(getActivity(), accountKey, user);
                }

                @Override
                public void onLoaderReset(final Loader<SingleResponse<UserRelationship>> loader) {

                }

                @Override
                public void onLoadFinished(final Loader<SingleResponse<UserRelationship>> loader,
                                           final SingleResponse<UserRelationship> data) {
                    mFollowProgress.setVisibility(View.GONE);
                    final ParcelableUser user = getUser();
                    final UserRelationship relationship = data.getData();
                    displayRelationship(user, relationship);
                    updateOptionsMenuVisibility();
                }

            };
    private final LoaderCallbacks<SingleResponse<ParcelableUser>> mUserInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableUser>>() {

        @Override
        public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
            final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
            final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
            final UserKey userId = args.getParcelable(EXTRA_USER_KEY);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            if (mUser == null && (!omitIntentExtra || !args.containsKey(EXTRA_USER))) {
                mCardContent.setVisibility(View.GONE);
                mHeaderErrorContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.VISIBLE);
                mHeaderErrorTextView.setText(null);
                mHeaderErrorTextView.setVisibility(View.GONE);
            }
            final ParcelableUser user = mUser;
            final boolean loadFromCache = user == null || !user.is_cache && user.key.maybeEquals(userId);
            return new ParcelableUserLoader(getActivity(), accountKey, userId, screenName, getArguments(),
                    omitIntentExtra, loadFromCache);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
                                   final SingleResponse<ParcelableUser> data) {
            final FragmentActivity activity = getActivity();
            if (activity == null) return;
            if (data.hasData()) {
                final ParcelableUser user = data.getData();
                mCardContent.setVisibility(View.VISIBLE);
                mHeaderErrorContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.GONE);
                ParcelableAccount account = data.getExtras().getParcelable(EXTRA_ACCOUNT);
                displayUser(user, account);
                if (user.is_cache) {
                    final Bundle args = new Bundle();
                    args.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key);
                    args.putParcelable(EXTRA_USER_KEY, user.key);
                    args.putString(EXTRA_SCREEN_NAME, user.screen_name);
                    args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
                    getLoaderManager().restartLoader(LOADER_ID_USER, args, this);
                }
                updateOptionsMenuVisibility();
            } else if (mUser != null && mUser.is_cache) {
                mCardContent.setVisibility(View.VISIBLE);
                mHeaderErrorContainer.setVisibility(View.GONE);
                mProgressContainer.setVisibility(View.GONE);
                displayUser(mUser, mAccount);
                updateOptionsMenuVisibility();
            } else {
                if (data.hasException()) {
                    mHeaderErrorTextView.setText(Utils.getErrorMessage(activity, data.getException()));
                    mHeaderErrorTextView.setVisibility(View.VISIBLE);
                }
                mCardContent.setVisibility(View.GONE);
                mHeaderErrorContainer.setVisibility(View.VISIBLE);
                mProgressContainer.setVisibility(View.GONE);
                displayUser(null, null);
                updateOptionsMenuVisibility();
            }
        }

    };

    private void updateOptionsMenuVisibility() {
        setHasOptionsMenu(mUser != null && mRelationship != null);
    }

    private void displayRelationship(@Nullable final ParcelableUser user,
                                     @Nullable final UserRelationship userRelationship) {
        if (user == null) {
            mRelationship = null;
            return;
        }
        if (user.account_key.maybeEquals(user.key)) {
            mFollowButton.setText(R.string.edit);
            mFollowButton.setVisibility(View.VISIBLE);
            mRelationship = userRelationship;
            return;
        }
        if (userRelationship == null || !userRelationship.check(user)) {
            mRelationship = null;
            return;
        } else {
            mRelationship = userRelationship;
        }
        invalidateOptionsMenu();
        mFollowButton.setEnabled(userRelationship.blocking || !userRelationship.blocked_by);
        if (userRelationship.blocked_by) {
            mPagesErrorContainer.setVisibility(View.GONE);
            mPagesErrorText.setText(null);
            mPagesContent.setVisibility(View.VISIBLE);
        } else if (!userRelationship.following && user.is_protected) {
            mPagesErrorContainer.setVisibility(View.VISIBLE);
            mPagesErrorText.setText(R.string.user_protected_summary);
            mPagesErrorIcon.setImageResource(R.drawable.ic_info_locked);
            mPagesContent.setVisibility(View.GONE);
        } else {
            mPagesErrorContainer.setVisibility(View.GONE);
            mPagesErrorText.setText(null);
            mPagesContent.setVisibility(View.VISIBLE);
        }
        if (userRelationship.blocking) {
            mFollowButton.setText(R.string.unblock);
        } else if (userRelationship.following) {
            mFollowButton.setText(R.string.unfollow);
        } else if (user.is_follow_request_sent) {
            mFollowButton.setText(R.string.requested);
        } else {
            mFollowButton.setText(R.string.follow);
        }
        mFollowButton.setCompoundDrawablePadding(Math.round(mFollowButton.getTextSize() * 0.25f));
        mFollowingYouIndicator.setVisibility(userRelationship.followed_by ? View.VISIBLE : View.GONE);

        final CacheUserInfoTask task = new CacheUserInfoTask(getContext().getApplicationContext());
        task.setParams(Pair.create(user, userRelationship));
        TaskStarter.execute(task);
        mFollowButton.setVisibility(View.VISIBLE);
    }

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
        assert spec.type != null;
        switch (spec.type) {
            case TAB_TYPE_STATUSES: {
                actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_statuses,
                        (int) user.statuses_count, user.statuses_count));
                break;
            }
            case TAB_TYPE_MEDIA: {
                if (user.media_count < 0) {
                    actionBar.setSubtitle(R.string.recent_media);
                } else {
                    actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_media,
                            (int) user.media_count, user.media_count));
                }
                break;
            }
            case TAB_TYPE_FAVORITES: {
                if (mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
                    actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_favorites,
                            (int) user.favorites_count, user.favorites_count));
                } else {
                    actionBar.setSubtitle(getResources().getQuantityString(R.plurals.N_likes,
                            (int) user.favorites_count, user.favorites_count));

                }
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

    @UiThread
    public void displayUser(final ParcelableUser user, ParcelableAccount account) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        mUser = user;
        mAccount = account;
        if (user == null || user.key == null) {
            mProfileImageView.setVisibility(View.GONE);
            mProfileTypeView.setVisibility(View.GONE);
            if (activity instanceof ATEActivity) {
                setUiColor(Config.primaryColor(activity, ((ATEActivity) activity).getATEKey()));
            }
            return;
        }
        mProfileImageView.setVisibility(View.VISIBLE);
        final Resources resources = getResources();
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        mCardContent.setVisibility(View.VISIBLE);
        mHeaderErrorContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
        mUser = user;
        mProfileImageView.setBorderColor(user.color != 0 ? user.color : Color.WHITE);
        mProfileNameContainer.drawEnd(user.account_color);
        mNameView.setText(mBidiFormatter.unicodeWrap(TextUtils.isEmpty(user.nickname) ? user.name : getString(R.string.name_with_nickname, user.name, user.nickname)));
        final int typeIconRes = Utils.getUserTypeIconRes(user.is_verified, user.is_protected);
        if (typeIconRes != 0) {
            mProfileTypeView.setImageResource(typeIconRes);
            mProfileTypeView.setVisibility(View.VISIBLE);
        } else {
            mProfileTypeView.setImageDrawable(null);
            mProfileTypeView.setVisibility(View.GONE);
        }
        mScreenNameView.setText(String.format("@%s", user.screen_name));
        final TwidereLinkify linkify = new TwidereLinkify(this);
        if (user.description_unescaped != null) {
            final SpannableStringBuilder text = SpannableStringBuilder.valueOf(user.description_unescaped);
            ParcelableStatusUtils.applySpans(text, user.description_spans);
            linkify.applyAllLinks(text, user.account_key, false, false);
            mDescriptionView.setText(text);
        } else {
            mDescriptionView.setText(user.description_plain);
            Linkify.addLinks(mDescriptionView, Linkify.WEB_URLS);
        }
        mDescriptionContainer.setVisibility(mDescriptionView.length() > 0 ? View.VISIBLE : View.GONE);

        mLocationContainer.setVisibility(TextUtils.isEmpty(user.location) ? View.GONE : View.VISIBLE);
        mLocationView.setText(user.location);
        mURLContainer.setVisibility(TextUtils.isEmpty(user.url) && TextUtils.isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        mURLView.setText(TextUtils.isEmpty(user.url_expanded) ? user.url : user.url_expanded);
        final String createdAt = Utils.formatToLongTimeString(activity, user.created_at);
        final float daysSinceCreation = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24;
        final int dailyTweets = Math.round(user.statuses_count / Math.max(1, daysSinceCreation));
        mCreatedAtView.setText(resources.getQuantityString(R.plurals.created_at_with_N_tweets_per_day, dailyTweets,
                createdAt, dailyTweets));
        mListedCount.setText(Utils.getLocalizedNumber(mLocale, user.listed_count));
        final long groupsCount = user.extras != null ? user.extras.groups_count : -1;
        mGroupsCount.setText(Utils.getLocalizedNumber(mLocale, groupsCount));
        mFollowersCount.setText(Utils.getLocalizedNumber(mLocale, user.followers_count));
        mFriendsCount.setText(Utils.getLocalizedNumber(mLocale, user.friends_count));

        mListedContainer.setVisibility(user.listed_count < 0 ? View.GONE : View.VISIBLE);
        mGroupsContainer.setVisibility(groupsCount < 0 ? View.GONE : View.VISIBLE);

        mMediaLoader.displayOriginalProfileImage(mProfileImageView, user);
        if (user.color != 0) {
            setUiColor(user.color);
        } else if (user.link_color != 0) {
            setUiColor(user.link_color);
        } else if (activity instanceof ATEActivity) {
            setUiColor(Config.primaryColor(activity, ((ATEActivity) activity).getATEKey()));
        }
        final int defWidth = resources.getDisplayMetrics().widthPixels;
        final int width = mBannerWidth > 0 ? mBannerWidth : defWidth;
        final String bannerUrl = ParcelableUserUtils.getProfileBannerUrl(user);
        if (ObjectUtils.notEqual(mProfileBannerView.getTag(), bannerUrl) ||
                mProfileBannerView.getDrawable() == null) {
            mProfileBannerView.setTag(bannerUrl);
            mMediaLoader.displayProfileBanner(mProfileBannerView, bannerUrl, width);
        }
        final UserRelationship relationship = mRelationship;
        if (relationship == null) {
            getFriendship();
        }
        activity.setTitle(UserColorNameManager.decideDisplayName(user.nickname, user.name,
                user.screen_name, mNameFirst));

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

    @Nullable
    public ParcelableUser getUser() {
        return mUser;
    }

    public void getUserInfo(final UserKey accountKey, final UserKey userKey, final String screenName,
                            final boolean omitIntentExtra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_USER);
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey);
        args.putParcelable(EXTRA_USER_KEY, userKey);
        args.putString(EXTRA_SCREEN_NAME, screenName);
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
        if (!mGetUserInfoLoaderInitialized) {
            lm.initLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
            mGetUserInfoLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
        }
        if (accountKey == null || userKey == null && screenName == null) {
            mCardContent.setVisibility(View.GONE);
            mHeaderErrorContainer.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void notifyFriendshipUpdated(FriendshipUpdatedEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || !event.isAccount(user.account_key) || !event.isUser(user.key.getId()))
            return;
        getFriendship();
    }

    @Subscribe
    public void notifyFriendshipUserUpdated(FriendshipTaskEvent event) {
        final ParcelableUser user = getUser();
        if (user == null || !event.isSucceeded() || !event.isUser(user)) return;
        getFriendship();
    }

    @Subscribe
    public void notifyProfileUpdated(ProfileUpdatedEvent event) {
        final ParcelableUser user = getUser();
        // TODO check account status
        if (user == null || !user.equals(event.user)) return;
        displayUser(event.user, mAccount);
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        invalidateOptionsMenu();
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
                    mUserColorNameManager.setUserColor(mUser.key, color);
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    mUserColorNameManager.clearUserColor(mUser.key);
                }
                break;
            }
            case REQUEST_ADD_TO_LIST: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
                    final ParcelableUserList list = data.getParcelableExtra(EXTRA_USER_LIST);
                    if (list == null || twitter == null) return;
                    twitter.addUserListMembersAsync(user.account_key, list.id, user);
                }
                break;
            }
            case REQUEST_SELECT_ACCOUNT: {
                if (user == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final UserKey accountKey = data.getParcelableExtra(EXTRA_ACCOUNT_KEY);
                    @Referral
                    final String referral = getArguments().getString(EXTRA_REFERRAL);
                    IntentUtils.openUserProfile(getActivity(), accountKey, user.key,
                            user.screen_name, null, mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            referral);
                }
                break;
            }
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
        mUserColorNameManager.registerColorChangedListener(this);
        mUserColorNameManager.registerNicknameChangedListener(this);
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        mLocale = getResources().getConfiguration().locale;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(activity,
                ThemeUtils.getThemeBackgroundOption(activity),
                ThemeUtils.getUserThemeBackgroundAlpha(activity));
        mActionBarShadowColor = 0xA0000000;
        final Bundle args = getArguments();
        UserKey accountId = null;
        UserKey userId = null;
        String screenName = null;
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        } else {
            accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
            userId = args.getParcelable(EXTRA_USER_KEY);
            screenName = args.getString(EXTRA_SCREEN_NAME);
        }

        Utils.setNdefPushMessageCallback(activity, new CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableUser user = getUser();
                if (user == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(LinkCreator.getUserWebLink(user)),
                });
            }
        });


        mTintedStatusFrameLayout.setWindowInsetsListener(new TintedStatusFrameLayout.WindowInsetsListener() {
            @Override
            public void onApplyWindowInsets(int left, int top, int right, int bottom) {
                mProfileContentContainer.setPadding(0, top, 0, 0);
                mProfileBannerSpace.setStatusBarHeight(top);

                if (mProfileBannerSpace.getToolbarHeight() == 0) {
                    int toolbarHeight = mToolbar.getMeasuredHeight();
                    if (toolbarHeight == 0) {
                        toolbarHeight = ThemeUtils.getActionBarHeight(getContext());
                    }
                    mProfileBannerSpace.setToolbarHeight(toolbarHeight);
                }
            }

        });
        mProfileContentContainer.setOnSizeChangedListener(new OnSizeChangedListener() {
            @Override
            public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
                final int toolbarHeight = mToolbar.getMeasuredHeight();
                mHeaderDrawerLayout.setPadding(0, toolbarHeight, 0, 0);
                mProfileBannerSpace.setToolbarHeight(toolbarHeight);
            }
        });

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
        mGroupsContainer.setOnClickListener(this);
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

        setupBaseActionBar();
        setupUserPages();

        getUserInfo(accountId, userId, screenName, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);

        @Referral
        final String referral = getArguments().getString(EXTRA_REFERRAL);
        final Context context = getContext();
        if (mUserEvent == null) {
            mUserEvent = UserEvent.create(context, referral);
        } else {
            mUserEvent.markStart(context);
        }
    }

    @Override
    public void onStop() {
        final Context context = getContext();
        if (mUserEvent != null && context != null && mUser != null) {
            mUserEvent.setUser(mUser);
            mUserEvent.markEnd();
            HotMobiLogger.getInstance(context).log(mUserEvent);
        }
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUiColor(mUiColor);
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
        inflater.inflate(R.menu.menu_user_profile, menu);
    }

    @Override
    @UiThread
    public void onPrepareOptionsMenu(final Menu menu) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUser user = getUser();
        if (twitter == null || user == null) return;

        final boolean isMyself = user.account_key.maybeEquals(user.key);
        final MenuItem mentionItem = menu.findItem(R.id.mention);
        if (mentionItem != null) {
            final String displayName = UserColorNameManager.decideDisplayName(user.nickname,
                    user.name, user.screen_name, mNameFirst);
            mentionItem.setTitle(getString(R.string.mention_user_name, displayName));
        }
        MenuUtils.setMenuItemAvailability(menu, R.id.mention, !isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.incoming_friendships, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.saved_searches, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.scheduled_statuses, isMyself
                && MicroBlogAPIFactory.getOfficialKeyType(getActivity(), user.account_key) == ConsumerKeyType.TWEETDECK);
        MenuUtils.setMenuItemAvailability(menu, R.id.muted_users, isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.blocked_users, isMyself);

        MenuUtils.setMenuItemAvailability(menu, R.id.block, !isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.mute_user, !isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.report_spam, !isMyself);
        MenuUtils.setMenuItemAvailability(menu, R.id.enable_retweets, !isMyself);
        if (mAccount != null) {
            MenuUtils.setMenuItemAvailability(menu, R.id.add_to_list, TextUtils.equals(ParcelableAccount.Type.TWITTER,
                    ParcelableAccountUtils.getAccountType(mAccount)));
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.add_to_list, false);
        }

        final UserRelationship userRelationship = mRelationship;
        if (userRelationship != null) {

            final MenuItem filterItem = menu.findItem(R.id.add_to_filter);
            if (filterItem != null) {
                filterItem.setChecked(userRelationship.filtering);
            }
            if (isMyself) {
                MenuUtils.setMenuItemAvailability(menu, R.id.send_direct_message, false);
            } else {
                MenuUtils.setMenuItemAvailability(menu, R.id.send_direct_message, userRelationship.can_dm);
                MenuUtils.setMenuItemAvailability(menu, R.id.block, true);
                final MenuItem blockItem = menu.findItem(R.id.block);
                if (blockItem != null) {
                    ActionIconDrawable.setMenuHighlight(blockItem, new TwidereMenuInfo(userRelationship.blocking));
                    blockItem.setTitle(userRelationship.blocking ? R.string.unblock : R.string.block);
                }
                final MenuItem muteItem = menu.findItem(R.id.mute_user);
                if (muteItem != null) {
                    muteItem.setChecked(userRelationship.muting);
                }
                final MenuItem wantRetweetsItem = menu.findItem(R.id.enable_retweets);
                if (wantRetweetsItem != null) {
                    wantRetweetsItem.setChecked(userRelationship.retweet_enabled);
                }
            }
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.send_direct_message, false);
        }
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
        final Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_USER, user);
        intent.putExtras(extras);
        menu.removeGroup(MENU_GROUP_USER_EXTENSION);
        MenuUtils.addIntentToMenu(getActivity(), menu, intent, MENU_GROUP_USER_EXTENSION);
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
        final Context context = getContext();
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUser user = getUser();
        final UserRelationship userRelationship = mRelationship;
        if (user == null || twitter == null) return false;
        switch (item.getItemId()) {
            case R.id.block: {
                if (userRelationship == null) return true;
                if (userRelationship.blocking) {
                    twitter.destroyBlockAsync(user.account_key, user.key);
                } else {
                    CreateUserBlockDialogFragment.show(getFragmentManager(), user);
                }
                break;
            }
            case R.id.report_spam: {
                ReportSpamDialogFragment.show(getFragmentManager(), user);
                break;
            }
            case R.id.add_to_filter: {
                if (userRelationship == null) return true;
                final ContentResolver cr = getContentResolver();
                if (userRelationship.filtering) {
                    final String where = Expression.equalsArgs(Filters.Users.USER_KEY).getSQL();
                    final String[] whereArgs = {user.key.toString()};
                    cr.delete(Filters.Users.CONTENT_URI, where, whereArgs);
                    Utils.showInfoMessage(getActivity(), R.string.message_user_unmuted, false);
                } else {
                    cr.insert(Filters.Users.CONTENT_URI, ContentValuesCreator.createFilteredUser(user));
                    Utils.showInfoMessage(getActivity(), R.string.message_user_muted, false);
                }
                getFriendship();
                break;
            }
            case R.id.mute_user: {
                if (userRelationship == null) return true;
                if (userRelationship.muting) {
                    twitter.destroyMuteAsync(user.account_key, user.key);
                } else {
                    CreateUserMuteDialogFragment.show(getFragmentManager(), user);
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
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, user.account_key.toString());
                builder.appendQueryParameter(QUERY_PARAM_USER_KEY, user.key.toString());
                final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
                intent.putExtra(EXTRA_ACCOUNT, ParcelableCredentialsUtils.getCredentials(getActivity(), user.account_key));
                intent.putExtra(EXTRA_USER, user);
                startActivity(intent);
                break;
            }
            case R.id.set_color: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, mUserColorNameManager.getUserColor(user.key));
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                intent.putExtra(EXTRA_CLEAR_BUTTON, true);
                startActivityForResult(intent, REQUEST_SET_COLOR);
                break;
            }
            case R.id.clear_nickname: {
                mUserColorNameManager.clearUserNickname(user.key);
                break;
            }
            case R.id.set_nickname: {
                final String nick = mUserColorNameManager.getUserNickname(user.key);
                SetUserNicknameDialogFragment.show(getFragmentManager(), user.key, nick);
                break;
            }
            case R.id.add_to_list: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER_LIST);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_KEY, user.account_key);
                intent.putExtra(EXTRA_SCREEN_NAME, DataStoreUtils.getAccountScreenName(getActivity(),
                        user.account_key));
                startActivityForResult(intent, REQUEST_ADD_TO_LIST);
                break;
            }
            case R.id.open_with_account: {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
                intent.setClass(getActivity(), AccountSelectorActivity.class);
                intent.putExtra(EXTRA_SINGLE_SELECTION, true);
                intent.putExtra(EXTRA_ACCOUNT_HOST, user.key.getHost());
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
                break;
            }
            case R.id.follow: {
                if (userRelationship == null) return true;
                final boolean updatingRelationship = twitter.isUpdatingRelationship(user.account_key,
                        user.key);
                if (!updatingRelationship) {
                    if (userRelationship.following) {
                        DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
                    } else {
                        twitter.createFriendshipAsync(user.account_key, user.key);
                    }
                }
                return true;
            }
            case R.id.enable_retweets: {
                final boolean newState = !item.isChecked();
                final FriendshipUpdate update = new FriendshipUpdate();
                update.retweets(newState);
                twitter.updateFriendship(user.account_key, user.key.getId(), update);
                item.setChecked(newState);
                return true;
            }
            case R.id.muted_users: {
                IntentUtils.openMutesUsers(getActivity(), user.account_key);
                return true;
            }
            case R.id.blocked_users: {
                IntentUtils.openUserBlocks(getActivity(), user.account_key);
                return true;
            }
            case R.id.incoming_friendships: {
                IntentUtils.openIncomingFriendships(getActivity(), user.account_key);
                return true;
            }
            case R.id.user_mentions: {
                IntentUtils.openUserMentions(context, user.account_key, user.screen_name);
                return true;
            }
            case R.id.saved_searches: {
                IntentUtils.openSavedSearches(context, user.account_key);
                return true;
            }
            case R.id.scheduled_statuses: {
                IntentUtils.openScheduledStatuses(context, user.account_key);
                return true;
            }
            case R.id.open_in_browser: {
                final Uri uri = LinkCreator.getUserWebLink(user);
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setPackage(IntentUtils.getDefaultBrowserPackage(context, uri, true));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            }
            default: {
                final Intent intent = item.getIntent();
                if (intent != null && intent.resolveActivity(context.getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTintedStatusFrameLayout = (TintedStatusFrameLayout) view;
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
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
        mProfileContentContainer = (ExtendedRelativeLayout) view.findViewById(R.id.profile_content_container);
        mNameView = (TextView) headerView.findViewById(R.id.name);
        mScreenNameView = (TextView) headerView.findViewById(R.id.screen_name);
        mDescriptionView = (TextView) headerView.findViewById(R.id.description);
        mLocationView = (TextView) headerView.findViewById(R.id.location);
        mURLView = (TextView) headerView.findViewById(R.id.url);
        mCreatedAtView = (TextView) headerView.findViewById(R.id.created_at);
        mListedContainer = headerView.findViewById(R.id.listed_container);
        mGroupsContainer = headerView.findViewById(R.id.groups_container);
        mListedCount = (TextView) headerView.findViewById(R.id.listed_count);
        mGroupsCount = (TextView) headerView.findViewById(R.id.groups_count);
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
        mProfileBannerSpace = (ProfileBannerSpace) headerView.findViewById(R.id.profile_banner_space);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) contentView.findViewById(R.id.toolbar_tabs);
        mFollowButton = (Button) headerView.findViewById(R.id.follow);
        mFollowProgress = (ProgressBar) headerView.findViewById(R.id.follow_progress);
        mWindowOverlay = view.findViewById(R.id.window_overlay);
        mPagesContent = view.findViewById(R.id.pages_content);
        mPagesErrorContainer = view.findViewById(R.id.pages_error_container);
        mPagesErrorIcon = (ImageView) view.findViewById(R.id.pages_error_icon);
        mPagesErrorText = (TextView) view.findViewById(R.id.pages_error_text);
        mProfileNameBackground = view.findViewById(R.id.profile_name_background);
        mProfileDetailsContainer = view.findViewById(R.id.profile_details_container);
        mFollowingYouIndicator = view.findViewById(R.id.following_you_indicator);

        final Object host = getHost();
        if (host instanceof AppCompatActivity) {
            ((AppCompatActivity) host).setSupportActionBar(mToolbar);
        }

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
    }

    @Override
    public boolean setupWindow(FragmentActivity activity) {
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            ((AppCompatActivity) activity).supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY);
        }
        WindowSupport.setStatusBarColor(activity.getWindow(), Color.TRANSPARENT);
        return true;
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
                if (user.account_key.maybeEquals(user.key)) {
                    IntentUtils.openProfileEditor(getActivity(), user.account_key);
                    break;
                }
                final UserRelationship userRelationship = mRelationship;
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (userRelationship == null || twitter == null) return;
                if (userRelationship.blocking) {
                    twitter.destroyBlockAsync(user.account_key, user.key);
                } else if (userRelationship.following) {
                    DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
                } else {
                    twitter.createFriendshipAsync(user.account_key, user.key);
                }
                break;
            }
            case R.id.profile_image: {
                final String url = Utils.getOriginalTwitterProfileImage(user.profile_image_url);
                ParcelableMedia profileImage = ParcelableMediaUtils.image(url);
                profileImage.type = ParcelableMedia.Type.IMAGE;
                final ParcelableMedia[] media = {profileImage};
                IntentUtils.openMedia(activity, user.account_key, false, null, media, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API));
                break;
            }
            case R.id.profile_banner: {
                final String bannerUrl = ParcelableUserUtils.getProfileBannerUrl(user);
                if (bannerUrl == null) return;
                final String url = InternalTwitterContentUtils.getBestBannerUrl(bannerUrl,
                        Integer.MAX_VALUE);
                ParcelableMedia profileBanner = ParcelableMediaUtils.image(url);
                profileBanner.type = ParcelableMedia.Type.IMAGE;
                final ParcelableMedia[] media = {profileBanner};
                IntentUtils.openMedia(activity, user.account_key, false, null, media, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API));
                break;
            }
            case R.id.listed_container: {
                IntentUtils.openUserLists(getActivity(), user.account_key, user.key,
                        user.screen_name);
                break;
            }
            case R.id.groups_container: {
                IntentUtils.openUserGroups(getActivity(), user.account_key, user.key,
                        user.screen_name);
                break;
            }
            case R.id.followers_container: {
                IntentUtils.openUserFollowers(getActivity(), user.account_key, user.key,
                        user.screen_name);
                break;
            }
            case R.id.friends_container: {
                IntentUtils.openUserFriends(getActivity(), user.account_key, user.key,
                        user.screen_name);
                break;
            }
            case R.id.name_container: {
                if (user.account_key.equals(user.key)) return;
                IntentUtils.openProfileEditor(getActivity(), user.account_key);
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
    public boolean onLinkClick(final String link, final String orig, final UserKey accountKey,
                               final long extraId, final int type, final boolean sensitive,
                               int start, int end) {
        final ParcelableUser user = getUser();
        if (user == null) return false;
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION: {
                IntentUtils.openUserProfile(getActivity(), user.account_key, null, link, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API), Referral.USER_MENTION);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_HASHTAG: {
                IntentUtils.openTweetSearch(getActivity(), user.account_key, "#" + link);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_LINK_IN_TEXT:
            case TwidereLinkify.LINK_TYPE_ENTITY_URL: {
                final Uri uri = Uri.parse(link);
                final Intent intent;
                if (uri.getScheme() != null) {
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, uri.buildUpon().scheme("http").build());
                }
                startActivity(intent);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_LIST: {
                if (link == null) break;
                final String[] mentionList = link.split("/");
                if (mentionList.length != 2) {
                    break;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUserNicknameChanged(@NonNull UserKey userId, String nick) {
        if (mUser == null || !mUser.key.equals(userId)) return;
        displayUser(mUser, mAccount);
    }

    @Override
    public void onUserColorChanged(@NonNull UserKey userId, int color) {
        if (mUser == null || !mUser.key.equals(userId)) return;
        displayUser(mUser, mAccount);
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

    private void getFriendship() {
        final ParcelableUser user = getUser();
        if (user == null) return;
        mRelationship = null;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_FRIENDSHIP);
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key);
        args.putParcelable(EXTRA_USER, user);
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
        getUserInfo(user.account_key, user.key, user.screen_name, omitIntentExtra);
    }

    private void setUiColor(int color) {
        mUiColor = color;
        mPreviousActionBarItemIsDark = 0;
        mPreviousTabItemIsDark = 0;
        if (mActionBarBackground == null) {
            setupBaseActionBar();
        }
        final BaseActivity activity = (BaseActivity) getActivity();
        if (Config.coloredActionBar(activity, activity.getATEKey())) {
            mPrimaryColor = color;
            mPrimaryColorDark = ThemeUtils.computeDarkColor(color);
        } else {
            mPrimaryColor = Config.primaryColor(activity, activity.getATEKey());
            mPrimaryColorDark = Color.BLACK;
        }
        if (mActionBarBackground != null) {
            mActionBarBackground.setColor(mPrimaryColor);
        }
        int taskColor;
        if (Config.coloredActionBar(activity, activity.getATEKey())) {
            taskColor = color;
        } else {
            taskColor = Config.toolbarColor(activity, activity.getATEKey(), mToolbar);
        }
        if (mUser != null) {
            final String name = mUserColorNameManager.getDisplayName(mUser, mNameFirst);
            ActivitySupport.setTaskDescription(activity, new TaskDescriptionCompat(name, null, taskColor));
        } else {
            ActivitySupport.setTaskDescription(activity, new TaskDescriptionCompat(null, null, taskColor));
        }
        final int optimalAccentColor = ThemeUtils.getOptimalAccentColor(color,
                mDescriptionView.getCurrentTextColor());
        mDescriptionView.setLinkTextColor(optimalAccentColor);
        mLocationView.setLinkTextColor(optimalAccentColor);
        mURLView.setLinkTextColor(optimalAccentColor);
        mProfileBannerView.setBackgroundColor(color);

        mPagerIndicator.setBackgroundColor(mPrimaryColor);

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
        final ActionBar actionBar = linkHandler.getSupportActionBar();
        if (actionBar == null) return;
        final Drawable shadow = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.shadow_user_banner_action_bar, null);
        mActionBarBackground = new ActionBarDrawable(shadow);
        if (!ThemeUtils.isWindowFloating(linkHandler)
                && ThemeUtils.isTransparentBackground(linkHandler.getCurrentThemeBackgroundOption())) {
//            mActionBarBackground.setAlpha(ThemeUtils.getActionBarAlpha(linkHandler.getCurrentThemeBackgroundAlpha()));
            mProfileBannerView.setAlpha(linkHandler.getCurrentThemeBackgroundAlpha() / 255f);
        }
        actionBar.setBackgroundDrawable(mActionBarBackground);
    }

    private void setupUserPages() {
        final Bundle args = getArguments(), tabArgs = new Bundle();
        final ParcelableUser user = args.getParcelable(EXTRA_USER);
        if (user != null) {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, user.account_key);
            tabArgs.putParcelable(EXTRA_USER_KEY, user.key);
            tabArgs.putString(EXTRA_SCREEN_NAME, user.screen_name);
        } else {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, args.getParcelable(EXTRA_ACCOUNT_KEY));
            tabArgs.putParcelable(EXTRA_USER_KEY, args.getParcelable(EXTRA_USER_KEY));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
        }
        mPagerAdapter.addTab(UserTimelineFragment.class, tabArgs, getString(R.string.statuses),
                R.drawable.ic_action_quote, TAB_TYPE_STATUSES, TAB_POSITION_STATUSES, null);
        mPagerAdapter.addTab(UserMediaTimelineFragment.class, tabArgs, getString(R.string.media),
                R.drawable.ic_action_gallery, TAB_TYPE_MEDIA, TAB_POSITION_MEDIA, null);
        if (mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
            mPagerAdapter.addTab(UserFavoritesFragment.class, tabArgs, getString(R.string.favorites),
                    R.drawable.ic_action_star, TAB_TYPE_FAVORITES, TAB_POSITION_FAVORITES, null);
        } else {
            mPagerAdapter.addTab(UserFavoritesFragment.class, tabArgs, getString(R.string.likes),
                    R.drawable.ic_action_heart, TAB_TYPE_FAVORITES, TAB_POSITION_FAVORITES, null);
        }
    }

    private void updateScrollOffset(int offset) {
        final View space = mProfileBannerSpace;
        final ProfileBannerImageView profileBannerView = mProfileBannerView;
        final View profileBirthdayBannerView = mProfileBirthdayBannerView;
        final View profileBannerContainer = mProfileBannerContainer;
        final int spaceHeight = space.getHeight();
        final float factor = TwidereMathUtils.clamp(spaceHeight == 0 ? 0 : (offset / (float) spaceHeight), 0, 1);
        profileBannerContainer.setTranslationY(-offset);
        profileBannerView.setTranslationY(offset / 2);
        profileBirthdayBannerView.setTranslationY(offset / 2);

        final BaseActivity activity = (BaseActivity) getActivity();


        final int statusBarColor = (int) sArgbEvaluator.evaluate(factor, 0xA0000000,
                ThemeUtils.computeDarkColor(mPrimaryColorDark));
        final Window window = activity.getWindow();
        mTintedStatusFrameLayout.setStatusBarColor(statusBarColor);
        ThemeUtils.setLightStatusBar(window, ThemeUtils.isLightColor(statusBarColor));
        int stackedTabColor = mPrimaryColor;


        final float profileContentHeight = mProfileNameContainer.getHeight() + mProfileDetailsContainer.getHeight();
        final float tabOutlineAlphaFactor;
        if ((offset - spaceHeight) > 0) {
            tabOutlineAlphaFactor = 1f - TwidereMathUtils.clamp((offset - spaceHeight) / profileContentHeight, 0, 1);
        } else {
            tabOutlineAlphaFactor = 1f;
        }

        if (mActionBarBackground != null) {
            mActionBarBackground.setFactor(factor);
            mActionBarBackground.setOutlineAlphaFactor(tabOutlineAlphaFactor);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mWindowOverlay.setAlpha(factor * tabOutlineAlphaFactor);
//            setCompatToolbarOverlayAlpha(activity, factor * tabOutlineAlphaFactor);
        }

        final int currentTabColor = (Integer) sArgbEvaluator.evaluate(tabOutlineAlphaFactor,
                stackedTabColor, mCardBackgroundColor);

        final Drawable tabBackground = mPagerIndicator.getBackground();
        ((ColorDrawable) tabBackground).setColor(currentTabColor);
        final boolean tabItemIsDark = ThemeUtils.isLightColor(currentTabColor);

        if (mPreviousTabItemIsDark == 0 || (tabItemIsDark ? 1 : -1) != mPreviousTabItemIsDark) {
            final int tabContrastColor = ThemeUtils.getColorDependent(currentTabColor);
            mPagerIndicator.setIconColor(tabContrastColor);
            mPagerIndicator.setLabelColor(tabContrastColor);
            if (Config.coloredActionBar(activity, activity.getATEKey())) {
                mPagerIndicator.setStripColor(tabContrastColor);
            } else {
                mPagerIndicator.setStripColor(ThemeUtils.getOptimalAccentColor(mUiColor,
                        tabContrastColor));
            }
            mPagerIndicator.updateAppearance();
        }
        mPreviousTabItemIsDark = tabItemIsDark ? 1 : -1;

        final int currentActionBarColor = (Integer) sArgbEvaluator.evaluate(factor, mActionBarShadowColor,
                stackedTabColor);
        final boolean actionItemIsDark = ThemeUtils.isLightColor(currentActionBarColor);
        if (mPreviousActionBarItemIsDark == 0 || (actionItemIsDark ? 1 : -1) != mPreviousActionBarItemIsDark) {
            ThemeUtils.applyToolbarItemColor(activity, mToolbar, currentActionBarColor);
        }
        mPreviousActionBarItemIsDark = actionItemIsDark ? 1 : -1;

        updateTitleAlpha();
    }

    private void updateTitleAlpha() {
        final int[] location = new int[2];
        mNameView.getLocationInWindow(location);
        final float nameShowingRatio = (mHeaderDrawerLayout.getPaddingTop() - location[1])
                / (float) mNameView.getHeight();
        final float textAlpha = TwidereMathUtils.clamp(nameShowingRatio, 0, 1);
        final Toolbar actionBarView = mToolbar;
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

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public float getControlBarOffset() {
        return 0;
    }

    @Override
    public void setControlBarOffset(float offset) {

    }

    @Override
    public int getControlBarHeight() {
        return 0;
    }

    @StringDef({Referral.SEARCH_RESULT, Referral.USER_MENTION, Referral.STATUS,
            Referral.TIMELINE_STATUS, Referral.DIRECT, Referral.EXTERNAL, Referral.SELF_PROFILE})
    public @interface Referral {

        String SEARCH_RESULT = "search_result";
        String USER_MENTION = "user_mention";
        String STATUS = "status";
        String TIMELINE_STATUS = "timeline_status";
        String DIRECT = "direct";
        String EXTERNAL = "external";
        String SELF_PROFILE = "self_profile";
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
        public void getOutline(@NonNull Outline outline) {
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
            final int shadowAlpha = Math.round(mAlpha * TwidereMathUtils.clamp(1 - f, 0, 1));
            mShadowDrawable.setAlpha(shadowAlpha);
            final boolean hasColor = mColor != 0;
            final int colorAlpha = hasColor ? Math.round(mAlpha * TwidereMathUtils.clamp(f, 0, 1)) : 0;
            mColorDrawable.setAlpha(colorAlpha);
            invalidateSelf();
        }

        public void setOutlineAlphaFactor(float f) {
            mOutlineAlphaFactor = f;
            invalidateSelf();
        }

    }

    static class UserRelationshipLoader extends AsyncTaskLoader<SingleResponse<UserRelationship>> {

        private final Context context;
        @Nullable
        private final UserKey mAccountKey;
        @Nullable
        private final ParcelableUser mUser;

        public UserRelationshipLoader(final Context context, @Nullable final UserKey accountKey,
                                      @Nullable final ParcelableUser user) {
            super(context);
            this.context = context;
            this.mAccountKey = accountKey;
            this.mUser = user;
        }

        @Override
        public SingleResponse<UserRelationship> loadInBackground() {
            if (mAccountKey == null || mUser == null) {
                return SingleResponse.getInstance(new MicroBlogException("Null parameters"));
            }
            final UserKey userKey = mUser.key;
            final boolean isFiltering = DataStoreUtils.isFilteringUser(context, userKey);
            if (mAccountKey.equals(mUser.key)) {
                return SingleResponse.getInstance(new UserRelationship(mAccountKey, userKey,
                        null, isFiltering));
            }
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                    mAccountKey);
            if (credentials == null) {
                return SingleResponse.getInstance(new MicroBlogException("No Account"));
            }
            if (MicroBlogAPIFactory.isStatusNetCredentials(credentials)) {
                if (!UserKeyUtils.isSameHost(mAccountKey, mUser.key)) {
                    return SingleResponse.getInstance(new UserRelationship(mUser, isFiltering));
                }
            }
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, mAccountKey, false);
            if (twitter == null) {
                return SingleResponse.getInstance(new MicroBlogException("No Account"));
            }
            try {
                final Relationship relationship = twitter.showFriendship(mUser.key.getId());
                if (relationship.isSourceBlockingTarget() || relationship.isSourceBlockedByTarget()) {
                    Utils.setLastSeen(context, userKey, -1);
                } else {
                    Utils.setLastSeen(context, userKey, System.currentTimeMillis());
                }
                Utils.updateRelationship(context, mAccountKey, userKey, relationship);
                return SingleResponse.getInstance(new UserRelationship(mAccountKey, userKey,
                        relationship, isFiltering));
            } catch (final MicroBlogException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    static class UserRelationship extends CachedRelationship {
        boolean filtering;
        boolean can_dm;

        public UserRelationship(@NonNull UserKey accountKey, @NonNull UserKey userKey,
                                @Nullable Relationship relationship, boolean filtering) {
            super(relationship, accountKey, userKey);
            this.filtering = filtering;
            if (relationship != null) {
                this.can_dm = relationship.canSourceDMTarget();
            }
        }

        public UserRelationship(@NonNull ParcelableUser user, boolean filtering) {
            this.account_key = user.account_key;
            this.user_key = user.key;
            this.filtering = filtering;
            if (user.extras != null) {
                this.following = user.is_following;
                this.followed_by = user.extras.statusnet_followed_by;
                this.blocking = user.extras.statusnet_blocking;
                this.blocked_by = user.extras.statusnet_blocked_by;
                this.can_dm = user.extras.statusnet_followed_by;
            }
        }

        public boolean check(@NonNull ParcelableUser user) {
            if (!account_key.equals(user.account_key)) {
                return false;
            }
            return (user.extras != null && TextUtils.equals(user_key.getId(), user.extras.unique_id))
                    || TextUtils.equals(user_key.getId(), user.key.getId());
        }

    }

    private static class CacheUserInfoTask extends AbstractTask<Pair<ParcelableUser,
            ? extends CachedRelationship>, Object, Object> {
        private final Context context;

        public CacheUserInfoTask(Context context) {
            this.context = context;
        }

        @Override
        public Object doLongOperation(Pair<ParcelableUser, ? extends CachedRelationship> args) {
            final ContentResolver resolver = context.getContentResolver();
            final ParcelableUser user = args.first;
            resolver.insert(CachedUsers.CONTENT_URI, ParcelableUserValuesCreator.create(user));
            resolver.insert(CachedRelationships.CONTENT_URI,
                    CachedRelationshipValuesCreator.create(args.second));
            return null;
        }
    }
}
