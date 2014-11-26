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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.adapter.ParcelableStatusesListAdapter;
import org.mariotaku.twidere.adapter.iface.IStatusesListAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.Account.AccountWithCredentials;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.text.method.StatusContentMovementMethod;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.MediaPreviewUtils.OnMediaClickListener;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CircularImageView;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.StatusTextView;
import org.mariotaku.twidere.view.TwidereMenuBar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.ucdavis.earlybird.ProfilingUtil;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.cancelRetweet;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getMapStaticImageUri;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.isSameAccount;
import static org.mariotaku.twidere.util.Utils.openImageDirectly;
import static org.mariotaku.twidere.util.Utils.openMap;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openStatusFavoriters;
import static org.mariotaku.twidere.util.Utils.openStatusReplies;
import static org.mariotaku.twidere.util.Utils.openStatusRetweeters;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.scrollListToPosition;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showOkMessage;
import static org.mariotaku.twidere.util.Utils.startStatusShareChooser;

public class StatusFragment extends ParcelableStatusesListFragment implements OnClickListener,
        OnMediaClickListener, OnSharedPreferenceChangeListener, ActionMode.Callback {

    private static final int LOADER_ID_STATUS = 1;
    private static final int LOADER_ID_FOLLOW = 2;
    private static final int LOADER_ID_LOCATION = 3;

    private ParcelableStatus mStatus;

    private Locale mLocale;

    private boolean mLoadMoreAutomatically;
    private boolean mFollowInfoDisplayed, mLocationInfoDisplayed;
    private boolean mStatusLoaderInitialized, mLocationLoaderInitialized;
    private boolean mFollowInfoLoaderInitialized;

    private boolean mShouldScroll;
    private SharedPreferences mPreferences;
    private AsyncTwitterWrapper mTwitterWrapper;
    private ImageLoaderWrapper mImageLoader;
    private Handler mHandler;
    private TextView mNameView, mScreenNameView, mTimeSourceView, mInReplyToView, mLocationView;
    private TextView mRepliesCountView, mRetweetsCountView, mFavoritesCountView;
    private StatusTextView mTextView;

    private CircularImageView mProfileImageView;
    private ImageView mProfileTypeView, mMapView;
    private Button mFollowButton;
    private Button mRetryButton;
    private View mMainContent, mFollowIndicator, mImagePreviewContainer, mLocationContainer, mLocationBackgroundView;
    private ColorLabelRelativeLayout mProfileView;
    private TwidereMenuBar mMenuBar;
    private ProgressBar mDetailsLoadProgress, mFollowInfoProgress;
    private LinearLayout mImagePreviewGrid;
    private View mHeaderView;
    private View mLoadImagesIndicator;
    private View mRepliesContainer, mRetweetsContainer, mFavoritesContainer;
    private ExtendedFrameLayout mDetailsContainer;
    private ListView mListView;

    private LoadConversationTask mConversationTask;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            switch (action) {
                case BROADCAST_FRIENDSHIP_CHANGED: {
                    if (mStatus != null && mStatus.user_id == intent.getLongExtra(EXTRA_USER_ID, -1)) {
                        showFollowInfo(true);
                    }
                    break;
                }
                case BROADCAST_FAVORITE_CHANGED: {
                    final ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
                    if (mStatus != null && status != null && isSameAccount(context, status.account_id, mStatus.account_id)
                            && status.id == getStatusId()) {
                        getStatus(true);
                    }
                    break;
                }
                case BROADCAST_RETWEET_CHANGED: {
                    final long status_id = intent.getLongExtra(EXTRA_STATUS_ID, -1);
                    if (status_id > 0 && status_id == getStatusId()) {
                        getStatus(true);
                    }
                    break;
                }
            }
        }
    };

    private final LoaderCallbacks<SingleResponse<ParcelableStatus>> mStatusLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableStatus>>() {

        @Override
        public Loader<SingleResponse<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
            mDetailsLoadProgress.setVisibility(View.VISIBLE);
            mMainContent.setVisibility(View.INVISIBLE);
            mRetryButton.setVisibility(View.GONE);
            mMainContent.setEnabled(false);
            setProgressBarIndeterminateVisibility(true);
            final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
            return new ParcelableStatusLoader(getActivity(), omitIntentExtra, getArguments(), accountId, statusId);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<ParcelableStatus>> loader,
                                   final SingleResponse<ParcelableStatus> data) {
            if (data.getData() == null) {
                // TODO
                mRetryButton.setVisibility(View.VISIBLE);
                showErrorMessage(getActivity(), getString(R.string.action_getting_status), data.getException(), true);
            } else {
                mRetryButton.setVisibility(View.GONE);
                displayStatus(data.getData());
                mDetailsLoadProgress.setVisibility(View.GONE);
                mMainContent.setVisibility(View.VISIBLE);
                mMainContent.setEnabled(true);
            }
            setProgressBarIndeterminateVisibility(false);
        }

    };

    private final LoaderCallbacks<String> mLocationLoaderCallbacks = new LoaderCallbacks<String>() {

        @Override
        public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return new LocationInfoLoader(getActivity(), mStatus != null ? mStatus.location : null);
        }

        @Override
        public void onLoaderReset(final Loader<String> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<String> loader, final String data) {
            if (data != null) {
                mLocationView.setText(data);
                mLocationInfoDisplayed = true;
            } else {
                mLocationView.setText(R.string.view_map);
                mLocationInfoDisplayed = false;
            }
        }

    };

    private final LoaderCallbacks<SingleResponse<Boolean>> mFollowInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<Boolean>>() {

        @Override
        public Loader<SingleResponse<Boolean>> onCreateLoader(final int id, final Bundle args) {
            mFollowIndicator.setVisibility(View.VISIBLE);
            mFollowButton.setVisibility(View.GONE);
            mFollowInfoProgress.setVisibility(View.VISIBLE);
            return new FollowInfoLoader(getActivity(), mStatus);
        }

        @Override
        public void onLoaderReset(final Loader<SingleResponse<Boolean>> loader) {

        }

        @Override
        public void onLoadFinished(final Loader<SingleResponse<Boolean>> loader, final SingleResponse<Boolean> data) {
            if (!data.hasException()) {
                mFollowIndicator.setVisibility(!data.hasData() || data.getData() ? View.GONE : View.VISIBLE);
                if (data.getData() != null) {
                    mFollowButton.setVisibility(data.getData() ? View.GONE : View.VISIBLE);
                    mFollowInfoDisplayed = true;
                }
            }
            mFollowInfoProgress.setVisibility(View.GONE);
        }

    };

    private final OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            return handleMenuItemClick(item);
        }
    };

    public void displayStatus(final ParcelableStatus status) {
        final boolean status_unchanged = mStatus != null && status != null && status.equals(mStatus);
        if (!status_unchanged) {
            getListAdapter().setData(null);
            if (mStatus != null) {
                // UCD
                ProfilingUtil.profile(getActivity(), mStatus.account_id, "End, " + mStatus.id);
            }
        } else {
            setSelection(0);
        }
        if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
            mConversationTask.cancel(true);
        }
        mStatus = status;
        if (mStatus != null) {
            // UCD
            ProfilingUtil.profile(getActivity(), mStatus.account_id, "Start, " + mStatus.id);
        }
        if (!status_unchanged) {
            clearPreviewImages();
            hidePreviewImages();
        }
        if (status == null || getActivity() == null) return;
        final Bundle args = getArguments();
        args.putLong(EXTRA_ACCOUNT_ID, status.account_id);
        args.putLong(EXTRA_STATUS_ID, status.id);
        args.putParcelable(EXTRA_STATUS, status);
        setMenuForStatus(getActivity(), mMenuBar.getMenu(), status);
        mMenuBar.show();

        updateUserColor();
        mProfileView.drawEnd(getAccountColor(getActivity(), status.account_id));
        final boolean nickname_only = mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
        final boolean name_first = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        final boolean display_image_preview = mPreferences.getBoolean(KEY_DISPLAY_IMAGE_PREVIEW, false);
        final String nick = getUserNickname(getActivity(), status.user_id, true);
        mNameView.setText(TextUtils.isEmpty(nick) ? status.user_name : nickname_only ? nick : getString(
                R.string.name_with_nickname, status.user_name, nick));
        final int typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
        if (typeIconRes != 0) {
            mProfileTypeView.setImageResource(typeIconRes);
            mProfileTypeView.setVisibility(View.VISIBLE);
        } else {
            mProfileTypeView.setImageDrawable(null);
            mProfileTypeView.setVisibility(View.GONE);
        }
        mScreenNameView.setText("@" + status.user_screen_name);
        mTextView.setText(Html.fromHtml(status.text_html));
        final TwidereLinkify linkify = new TwidereLinkify(
                new OnLinkClickHandler(getActivity(), getMultiSelectManager()));
        linkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(getActivity()));
        linkify.applyAllLinks(mTextView, status.account_id, status.is_possibly_sensitive);
        ThemeUtils.applyParagraphSpacing(mTextView, 1.1f);

        mTextView.setMovementMethod(StatusContentMovementMethod.getInstance());
        mTextView.setCustomSelectionActionModeCallback(this);
        long timestamp = status.retweet_timestamp > 0 ? status.retweet_timestamp : status.timestamp;
        final String timeString = formatToLongTimeString(getActivity(), timestamp);
        final String sourceHtml = status.source;
        if (!isEmpty(timeString) && !isEmpty(sourceHtml)) {
            mTimeSourceView.setText(Html.fromHtml(getString(R.string.time_source, timeString, sourceHtml)));
        } else if (isEmpty(timeString) && !isEmpty(sourceHtml)) {
            mTimeSourceView.setText(Html.fromHtml(getString(R.string.source, sourceHtml)));
        } else if (!isEmpty(timeString) && isEmpty(sourceHtml)) {
            mTimeSourceView.setText(timeString);
        }
        mTimeSourceView.setMovementMethod(LinkMovementMethod.getInstance());

        final String in_reply_to = getDisplayName(getActivity(), status.in_reply_to_user_id, status.in_reply_to_name,
                status.in_reply_to_screen_name, name_first, nickname_only, true);
        mInReplyToView.setText(getString(R.string.in_reply_to_name, in_reply_to));

        if (mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
            mImageLoader.displayProfileImage(mProfileImageView, status.user_profile_image_url);
        } else {
            mProfileImageView.setImageResource(R.drawable.ic_profile_image_default);
        }
        mImagePreviewContainer.setVisibility(status.media == null || status.media.length == 0 ? View.GONE
                : View.VISIBLE);
        if (display_image_preview) {
            loadPreviewImages();
        }
        mRetweetsContainer.setVisibility(!status.user_is_protected ? View.VISIBLE : View.GONE);
        mRepliesContainer.setVisibility(status.reply_count < 0 ? View.GONE : View.VISIBLE);
        mRepliesCountView.setText(getLocalizedNumber(mLocale, status.reply_count));
        mRetweetsCountView.setText(getLocalizedNumber(mLocale, status.retweet_count));
        mFavoritesCountView.setText(getLocalizedNumber(mLocale, status.favorite_count));
        final ParcelableLocation location = status.location;
        final boolean is_valid_location = ParcelableLocation.isValidLocation(location);
        mLocationContainer.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
        if (display_image_preview) {
            mMapView.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
            mLocationBackgroundView.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
            mLocationView.setVisibility(View.VISIBLE);
            if (is_valid_location) {
                mHandler.post(new DisplayMapRunnable(location, mImageLoader, mMapView));
            } else {
                mMapView.setImageDrawable(null);
            }
        } else {
            mMapView.setVisibility(View.GONE);
            mLocationBackgroundView.setVisibility(View.GONE);
            mMapView.setImageDrawable(null);
            mLocationView.setVisibility(View.VISIBLE);
        }
        if (mLoadMoreAutomatically) {
            showFollowInfo(true);
            showLocationInfo(true);
            showConversation();
        } else {
            mFollowIndicator.setVisibility(View.GONE);
        }
        updateConversationInfo();
        scrollToStart();
    }

    @Override
    public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
        return null;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.copyUrl: {
                final int start = mTextView.getSelectionStart(), end = mTextView.getSelectionEnd();
                final SpannableString string = SpannableString.valueOf(mTextView.getText());
                final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
                if (spans.length != 1) return true;
                ClipboardUtils.setText(getActivity(), spans[0].getURL());
                mode.finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocale = getResources().getConfiguration().locale;
        setRefreshMode(shouldEnablePullToRefresh() ? RefreshMode.BOTH : RefreshMode.NONE);
        setListShownNoAnimation(true);
        mHandler = new Handler();
        mListView = getListView();
        getListAdapter().setGapDisallowed(true);
        final TwidereApplication application = getApplication();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        mImageLoader = application.getImageLoaderWrapper();
        mTwitterWrapper = getTwitterWrapper();
        mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
        mLoadImagesIndicator.setOnClickListener(this);
        mInReplyToView.setOnClickListener(this);
        mFollowButton.setOnClickListener(this);
        mProfileView.setOnClickListener(this);
        mLocationContainer.setOnClickListener(this);
        mRepliesContainer.setOnClickListener(this);
        mRetweetsContainer.setOnClickListener(this);
        mFavoritesContainer.setOnClickListener(this);
        mMenuBar.inflate(R.menu.menu_status);
        mMenuBar.setIsBottomBar(true);
        mMenuBar.setOnMenuItemClickListener(mMenuItemClickListener);
        getStatus(false);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                if (mStatus == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
                    setUserColor(getActivity(), mStatus.user_id, color);
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    clearUserColor(getActivity(), mStatus.user_id);
                }
                break;
            }
            case REQUEST_SELECT_ACCOUNT: {
                if (mStatus == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final long accountId = data.getLongExtra(EXTRA_ID, -1);
                    openStatus(getActivity(), accountId, mStatus.id);
                }
                break;
            }
        }
    }

    @Override
    public void onClick(final View view) {
        if (mStatus == null) return;
        final ParcelableStatus status = mStatus;
        switch (view.getId()) {
            case R.id.profile: {
                openUserProfile(getActivity(), status.account_id, status.user_id, null);
                break;
            }
            case R.id.follow: {
                mTwitterWrapper.createFriendshipAsync(status.account_id, status.user_id);
                break;
            }
            case R.id.in_reply_to: {
                showConversation();
                break;
            }
            case R.id.replies_container: {
                openStatusReplies(getActivity(), status.account_id, status.id, status.user_screen_name);
                break;
            }
            case R.id.location_container: {
                final ParcelableLocation location = status.location;
                if (!ParcelableLocation.isValidLocation(location)) return;
                openMap(getActivity(), location.latitude, location.longitude);
                break;
            }
            case R.id.load_images: {
                if (status.is_possibly_sensitive) {
                    final LoadSensitiveImageConfirmDialogFragment f = new LoadSensitiveImageConfirmDialogFragment();
                    f.show(getChildFragmentManager(), "load_sensitive_image_confirmation");
                } else {
                    loadPreviewImages();
                }
                // UCD
                ProfilingUtil.profile(getActivity(), status.account_id, "Thumbnail click, " + status.id);
                break;
            }
            case R.id.retweets_container: {
                openStatusRetweeters(getActivity(), status.account_id, status.retweet_id > 0 ? status.retweet_id
                        : status.id);
                break;
            }
            case R.id.favorites_container: {
                // TODO
                final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(),
                        status.account_id);
                if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
                    openStatusFavoriters(getActivity(), status.account_id, status.retweet_id > 0 ? status.retweet_id
                            : status.id);
                }
                break;
            }
        }

    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        final FragmentActivity a = getActivity();
        if (a == null) return false;
        a.getMenuInflater().inflate(R.menu.action_status_text_selection, menu);
        return true;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_details_page, container, false);
        view.findViewById(R.id.menu_bar).setVisibility(View.GONE);
        mMainContent = view.findViewById(R.id.content);
        mDetailsLoadProgress = (ProgressBar) view.findViewById(R.id.details_load_progress);
        mDetailsContainer = (ExtendedFrameLayout) view.findViewById(R.id.details_container);
        mDetailsContainer.addView(super.onCreateView(inflater, mDetailsContainer, savedInstanceState));
        mHeaderView = inflater.inflate(R.layout.header_status, null, false);
        mMenuBar = (TwidereMenuBar) mHeaderView.findViewById(R.id.menu_bar);
        mImagePreviewContainer = mHeaderView.findViewById(R.id.image_preview);
        mLocationContainer = mHeaderView.findViewById(R.id.location_container);
        mLocationView = (TextView) mHeaderView.findViewById(R.id.location_view);
        mLocationBackgroundView = mHeaderView.findViewById(R.id.location_background_view);
        mMapView = (ImageView) mHeaderView.findViewById(R.id.map_view);
        mNameView = (TextView) mHeaderView.findViewById(R.id.name);
        mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
        mTextView = (StatusTextView) mHeaderView.findViewById(R.id.text);
        mProfileImageView = (CircularImageView) mHeaderView.findViewById(R.id.profile_image);
        mProfileTypeView = (ImageView) mHeaderView.findViewById(R.id.profile_type);
        mTimeSourceView = (TextView) mHeaderView.findViewById(R.id.time_source);
        mInReplyToView = (TextView) mHeaderView.findViewById(R.id.in_reply_to);
        mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
        mFollowIndicator = mHeaderView.findViewById(R.id.follow_indicator);
        mFollowInfoProgress = (ProgressBar) mHeaderView.findViewById(R.id.follow_info_progress);
        mProfileView = (ColorLabelRelativeLayout) mHeaderView.findViewById(R.id.profile);
        mImagePreviewGrid = (LinearLayout) mHeaderView.findViewById(R.id.image_grid);
        mRepliesContainer = mHeaderView.findViewById(R.id.replies_container);
        mRetweetsContainer = mHeaderView.findViewById(R.id.retweets_container);
        mFavoritesContainer = mHeaderView.findViewById(R.id.favorites_container);
        mRepliesCountView = (TextView) mHeaderView.findViewById(R.id.replies_count);
        mRetweetsCountView = (TextView) mHeaderView.findViewById(R.id.retweets_count);
        mFavoritesCountView = (TextView) mHeaderView.findViewById(R.id.favorites_count);
        mLoadImagesIndicator = mHeaderView.findViewById(R.id.load_images);
        mRetryButton = (Button) view.findViewById(R.id.retry);
        final View cardView = mHeaderView.findViewById(R.id.card);
        ThemeUtils.applyThemeAlphaToDrawable(cardView.getContext(), cardView.getBackground());
        return view;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
    }

    @Override
    public void onDestroyView() {
        // UCD
        if (mStatus != null) {
            ProfilingUtil.profile(getActivity(), mStatus.account_id, "End, " + mStatus.id);
        }
        mStatus = null;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_STATUS);
        lm.destroyLoader(LOADER_ID_LOCATION);
        lm.destroyLoader(LOADER_ID_FOLLOW);
        if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
            mConversationTask.cancel(true);
        }
        super.onDestroyView();
    }

    @Override
    public void onItemsCleared() {

    }

    @Override
    public void onMediaClick(final View view, final ParcelableMedia media) {
        final ParcelableStatus status = mStatus;
        if (status == null) return;
        // UCD
        ProfilingUtil.profile(getActivity(), mStatus.account_id, "Large image click, " + mStatus.id + ", " + media.url);
        openImageDirectly(getActivity(), status.account_id, media.url);
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        final int start = mTextView.getSelectionStart(), end = mTextView.getSelectionEnd();
        final SpannableString string = SpannableString.valueOf(mTextView.getText());
        final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
        final boolean avail = spans.length == 1 && URLUtil.isValidUrl(spans[0].getURL());
        Utils.setMenuItemAvailability(menu, android.R.id.copyUrl, avail);
        return false;
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                         final int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        mShouldScroll = false;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (mStatus == null || !ParseUtils.parseString(mStatus.user_id).equals(key)) return;
        displayStatus(mStatus);
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_FRIENDSHIP_CHANGED);
        filter.addAction(BROADCAST_FAVORITE_CHANGED);
        filter.addAction(BROADCAST_RETWEET_CHANGED);
        registerReceiver(mStatusReceiver, filter);
        updateUserColor();
        final int text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
        mTextView.setTextSize(text_size * 1.25f);
        mNameView.setTextSize(text_size * 1.25f);
        mScreenNameView.setTextSize(text_size * 0.85f);
        mTimeSourceView.setTextSize(text_size * 0.85f);
        mInReplyToView.setTextSize(text_size * 0.85f);
        mLocationView.setTextSize(text_size * 0.85f);
        // mRetweetView.setTextSize(text_size * 0.85f);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    @Override
    public boolean scrollToStart() {
        if (mListView == null) return false;
        final IStatusesListAdapter<List<ParcelableStatus>> adapter = getListAdapter();
        scrollListToPosition(mListView, adapter.getCount() + mListView.getFooterViewsCount() - 1, 0);
        return true;
    }

    // @Override
    // protected void setItemSelected(final ParcelableStatus status, final int
    // position, final boolean selected) {
    // final MultiSelectManager manager = getMultiSelectManager();
    // final Object only_item = manager.getCount() == 1 ?
    // manager.getSelectedItems().get(0) : null;
    // final boolean only_item_selected = only_item != null &&
    // !only_item.equals(mStatus);
    // mListView.setItemChecked(0, only_item_selected);
    // if (mStatus != null) {
    // if (only_item_selected) {
    // manager.selectItem(mStatus);
    // } else {
    // manager.unselectItem(mStatus);
    // }
    // }
    // super.setItemSelected(status, position, selected);
    // }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        return null;
    }

    protected boolean handleMenuItemClick(final MenuItem item) {
        if (mStatus == null) return false;
        final ParcelableStatus status = mStatus;
        switch (item.getItemId()) {
            case MENU_SHARE: {
                startStatusShareChooser(getActivity(), status);
                break;
            }
            case MENU_COPY: {
                if (ClipboardUtils.setText(getActivity(), status.text_plain)) {
                    showOkMessage(getActivity(), R.string.text_copied, false);
                }
                break;
            }
            case MENU_RETWEET: {
                if (isMyRetweet(status)) {
                    cancelRetweet(mTwitterWrapper, status);
                } else {
                    final long id_to_retweet = status.is_retweet && status.retweet_id > 0 ? status.retweet_id
                            : status.id;
                    mTwitterWrapper.retweetStatus(status.account_id, id_to_retweet);
                }
                break;
            }
            case MENU_QUOTE: {
                final Intent intent = new Intent(INTENT_ACTION_QUOTE);
                intent.putExtra(EXTRA_STATUS, status);
                startActivity(intent);
                break;
            }
            case MENU_REPLY: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.putExtra(EXTRA_STATUS, status);
                startActivity(intent);
                break;
            }
            case MENU_FAVORITE: {
                if (status.is_favorite) {
                    mTwitterWrapper.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    mTwitterWrapper.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
            case MENU_DELETE: {
                DestroyStatusDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case MENU_ADD_TO_FILTER: {
                AddStatusFilterDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case MENU_SET_COLOR: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                final int color = getUserColor(getActivity(), status.user_id, true);
                if (color != 0) {
                    intent.putExtra(EXTRA_COLOR, color);
                }
                intent.putExtra(EXTRA_CLEAR_BUTTON, color != 0);
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                startActivityForResult(intent, REQUEST_SET_COLOR);
                break;
            }
            case MENU_CLEAR_NICKNAME: {
                clearUserNickname(getActivity(), status.user_id);
                displayStatus(status);
                break;
            }
            case MENU_SET_NICKNAME: {
                final String nick = getUserNickname(getActivity(), status.user_id, true);
                SetUserNicknameDialogFragment.show(getFragmentManager(), status.user_id, nick);
                break;
            }
            case MENU_TRANSLATE: {
                final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(),
                        status.account_id);
                if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
                    StatusTranslateDialogFragment.show(getFragmentManager(), status);
                } else {

                }
                break;
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
    protected void onReachedBottom() {

    }

    @Override
    protected void setItemSelected(final ParcelableStatus status, final int position, final boolean selected) {
    }

    @Override
    protected void setListHeaderFooters(final ListView list) {
        if (getActivity() == null || isDetached()) return;
        list.addHeaderView(mHeaderView, null, true);
    }

    @Override
    protected boolean shouldEnablePullToRefresh() {
        return false;
    }

    @Override
    protected boolean shouldShowAccountColor() {
        return false;
    }

    private void addConversationStatus(final ParcelableStatus status) {
        if (getActivity() == null || isDetached()) return;
        final List<ParcelableStatus> data = getData();
        if (data == null) return;
        data.add(status);
        final ParcelableStatusesListAdapter adapter = (ParcelableStatusesListAdapter) getListAdapter();
        adapter.setData(data);
        if (!mLoadMoreAutomatically && mShouldScroll) {
            setSelection(0);
        }
    }

    private void clearPreviewImages() {
        mImagePreviewGrid.removeAllViews();
    }

    private void getStatus(final boolean omit_intent_extra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_STATUS);
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omit_intent_extra);
        if (!mStatusLoaderInitialized) {
            lm.initLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
            mStatusLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
        }
    }

    private long getStatusId() {
        return mStatus != null ? mStatus.id : -1;
    }

    private void hidePreviewImages() {
        mLoadImagesIndicator.setVisibility(View.VISIBLE);
        mImagePreviewGrid.setVisibility(View.GONE);
    }

    private void loadPreviewImages() {
        if (mStatus == null) return;
        mLoadImagesIndicator.setVisibility(View.GONE);
        mImagePreviewGrid.setVisibility(View.VISIBLE);
        mImagePreviewGrid.removeAllViews();
        if (mStatus.media != null) {
            final int maxColumns = getResources().getInteger(R.integer.grid_column_image_preview);
            MediaPreviewUtils.addToLinearLayout(mImagePreviewGrid, mImageLoader, mStatus.media, maxColumns, this);
        }
    }

    private void showConversation() {
        if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
            mConversationTask.cancel(true);
            return;
        }
        final IStatusesListAdapter<List<ParcelableStatus>> adapter = getListAdapter();
        final int count = adapter.getCount();
        final ParcelableStatus status;
        if (count == 0) {
            mShouldScroll = !mLoadMoreAutomatically;
            status = mStatus;
        } else {
            status = adapter.getStatus(adapter.getCount() - 1);
        }
        if (status == null || status.in_reply_to_status_id <= 0) return;
        mConversationTask = new LoadConversationTask(this);
        mConversationTask.execute(status);
    }

    private void showFollowInfo(final boolean force) {
        if (mFollowInfoDisplayed && !force) return;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_FOLLOW);
        if (!mFollowInfoLoaderInitialized) {
            lm.initLoader(LOADER_ID_FOLLOW, null, mFollowInfoLoaderCallbacks);
            mFollowInfoLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_FOLLOW, null, mFollowInfoLoaderCallbacks);
        }
    }

    private void showLocationInfo(final boolean force) {
        if (mLocationInfoDisplayed && !force) return;
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(LOADER_ID_LOCATION);
        if (!mLocationLoaderInitialized) {
            lm.initLoader(LOADER_ID_LOCATION, null, mLocationLoaderCallbacks);
            mLocationLoaderInitialized = true;
        } else {
            lm.restartLoader(LOADER_ID_LOCATION, null, mLocationLoaderCallbacks);
        }
    }

    private void updateConversationInfo() {
        final boolean has_converstion = mStatus != null && mStatus.in_reply_to_status_id > 0;
        final IStatusesListAdapter<List<ParcelableStatus>> adapter = getListAdapter();
        final boolean load_not_finished = adapter.isEmpty()
                || adapter.getStatus(adapter.getCount() - 1).in_reply_to_status_id > 0;
        final boolean enable = has_converstion && load_not_finished;
        mInReplyToView.setVisibility(enable ? View.VISIBLE : View.GONE);
        mInReplyToView.setClickable(enable);
    }

    private void updateUserColor() {
        if (mStatus == null) return;
        mProfileImageView.setBorderColor(getUserColor(getActivity(), mStatus.user_id, true));
    }

    public static final class LoadSensitiveImageConfirmDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Fragment f = getParentFragment();
                    if (f instanceof StatusFragment) {
                        ((StatusFragment) f).loadPreviewImages();
                    }
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(R.string.sensitive_content_warning);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }
    }

    private static class DisplayMapRunnable implements Runnable {
        private final ParcelableLocation mLocation;
        private final ImageLoaderWrapper mLoader;
        private final ImageView mView;

        DisplayMapRunnable(final ParcelableLocation location, final ImageLoaderWrapper loader, final ImageView view) {
            mLocation = location;
            mLoader = loader;
            mView = view;
        }

        @Override
        public void run() {
            final String uri = getMapStaticImageUri(mLocation.latitude, mLocation.longitude, mView);
            mLoader.displayPreviewImage(uri, mView);
        }
    }

    static class FollowInfoLoader extends AsyncTaskLoader<SingleResponse<Boolean>> {

        private final ParcelableStatus status;
        private final Context context;

        public FollowInfoLoader(final Context context, final ParcelableStatus status) {
            super(context);
            this.context = context;
            this.status = status;
        }

        @Override
        public SingleResponse<Boolean> loadInBackground() {
            return isAllFollowing();
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        private SingleResponse<Boolean> isAllFollowing() {
            if (status == null) return SingleResponse.getInstance();
            if (status.user_id == status.account_id) return SingleResponse.getInstance(true);
            final Twitter twitter = getTwitterInstance(context, status.account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Relationship result = twitter.showFriendship(status.account_id, status.user_id);
                if (!result.isSourceFollowingTarget()) {
                    SingleResponse.getInstance(false);
                }
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
            return SingleResponse.getInstance();
        }
    }

    static class LoadConversationTask extends AsyncTask<ParcelableStatus, Void, SingleResponse<Boolean>> {

        final Handler handler;
        final Context context;
        final StatusFragment fragment;

        LoadConversationTask(final StatusFragment fragment) {
            context = fragment.getActivity();
            this.fragment = fragment;
            handler = new Handler();
        }

        @Override
        protected SingleResponse<Boolean> doInBackground(final ParcelableStatus... params) {
            if (params == null || params.length != 1)
                return new SingleResponse<>(false, null);
            try {
                final long account_id = params[0].account_id;
                ParcelableStatus status = params[0];
                while (status != null && status.in_reply_to_status_id > 0 && !isCancelled()) {
                    status = findStatus(context, account_id, status.in_reply_to_status_id);
                    if (status == null) {
                        break;
                    }
                    handler.post(new AddStatusRunnable(status));
                }
            } catch (final TwitterException e) {
                return new SingleResponse<>(false, e);
            }
            return new SingleResponse<>(true, null);
        }

        @Override
        protected void onCancelled() {
            fragment.setProgressBarIndeterminateVisibility(false);
            fragment.updateConversationInfo();
        }

        @Override
        protected void onPostExecute(final SingleResponse<Boolean> data) {
            fragment.setProgressBarIndeterminateVisibility(false);
            fragment.updateConversationInfo();
            if (data.getData() == null || !data.getData()) {
                showErrorMessage(context, context.getString(R.string.action_getting_status), data.getException(), true);
            }
        }

        @Override
        protected void onPreExecute() {
            fragment.setProgressBarIndeterminateVisibility(true);
            fragment.updateConversationInfo();
        }

        class AddStatusRunnable implements Runnable {

            final ParcelableStatus status;

            AddStatusRunnable(final ParcelableStatus status) {
                this.status = status;
            }

            @Override
            public void run() {
                fragment.addConversationStatus(status);
            }
        }
    }

    static class LocationInfoLoader extends AsyncTaskLoader<String> {

        private final Context context;
        private final ParcelableLocation location;

        public LocationInfoLoader(final Context context, final ParcelableLocation location) {
            super(context);
            this.context = context;
            this.location = location;
        }

        @Override
        public String loadInBackground() {
            if (location == null) return null;
            try {
                final Geocoder coder = new Geocoder(context);
                final List<Address> addresses = coder.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.size() == 1) {
                    final Address address = addresses.get(0);
                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0, max_idx = address.getMaxAddressLineIndex(); i < max_idx; i++) {
                        builder.append(address.getAddressLine(i));
                        if (i != max_idx - 1) {
                            builder.append(", ");
                        }
                    }
                    return builder.toString();

                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

    }

    static class ParcelableStatusLoader extends AsyncTaskLoader<SingleResponse<ParcelableStatus>> {

        private final boolean mOmitIntentExtra;
        private final Bundle mExtras;
        private final long mAccountId, mStatusId;

        public ParcelableStatusLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
                                      final long accountId, final long statusId) {
            super(context);
            mOmitIntentExtra = omitIntentExtra;
            mExtras = extras;
            mAccountId = accountId;
            mStatusId = statusId;
        }

        @Override
        public SingleResponse<ParcelableStatus> loadInBackground() {
            if (!mOmitIntentExtra && mExtras != null) {
                final ParcelableStatus cache = mExtras.getParcelable(EXTRA_STATUS);
                if (cache != null) return SingleResponse.getInstance(cache);
            }
            try {
                return SingleResponse.getInstance(findStatus(getContext(), mAccountId, mStatusId));
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
