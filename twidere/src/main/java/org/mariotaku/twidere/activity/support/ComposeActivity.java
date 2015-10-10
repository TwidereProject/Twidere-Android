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

package org.mariotaku.twidere.activity.support;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.SuperToast.Duration;
import com.github.johnpersano.supertoasts.SuperToast.OnDismissListener;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.twitter.Extractor;

import org.mariotaku.dynamicgridview.DraggableArrayAdapter;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.fragment.support.SupportProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.ViewStatusDialogFragment;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.text.MarkForDeleteSpan;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.EditTextEnterHandler;
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.PermissionUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ActionIconView;
import org.mariotaku.twidere.view.BadgeView;
import org.mariotaku.twidere.view.ComposeEditText;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.StatusTextCountView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ComposeActivity extends ThemedFragmentActivity implements LocationListener,
        OnMenuItemClickListener, OnClickListener, OnLongClickListener, Callback {

    // Constants
    private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";
    private static final String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";
    private static final String EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts";
    private static final String EXTRA_ORIGINAL_TEXT = "original_text";
    private static final String EXTRA_SHARE_SCREENSHOT = "share_screenshot";
    private static final String DISCARD_STATUS_DIALOG_FRAGMENT_TAG = "discard_status";

    // Utility classes
    private final Extractor mExtractor = new Extractor();
    private TwidereValidator mValidator;
    private LocationManager mLocationManager;
    private ContentResolver mResolver;
    private AsyncTask<Object, Object, ?> mTask;
    private SupportMenuInflater mMenuInflater;

    // Views
    private GridView mMediaPreviewGrid;
    private ActionMenuView mMenuBar;
    private ComposeEditText mEditText;
    private View mSendView;
    private StatusTextCountView mSendTextCountView;
    private RecyclerView mAccountSelector;
    private View mAccountSelectorContainer;
    private DraftItem mDraftItem;
    private ShapedImageView mProfileImageView;
    private BadgeView mCountView;
    private View mAccountSelectorButton;
    private View mLocationContainer;
    private ActionIconView mLocationIcon;
    private TextView mLocationText;

    // Adapters
    private MediaPreviewAdapter mMediaPreviewAdapter;
    private AccountIconsAdapter mAccountsAdapter;

    // Data fields
    private ParcelableLocation mRecentLocation;
    private ParcelableStatus mInReplyToStatus;
    private ParcelableUser mMentionUser;
    private String mOriginalText;
    private long mInReplyToStatusId;
    private boolean mIsPossiblySensitive, mShouldSaveAccounts;
    private boolean mImageUploaderUsed, mStatusShortenerUsed;
    private boolean mNavigateBackPressed;
    private boolean mTextChanged;
    private SetProgressVisibleRunnable mSetProgressVisibleRunnable;
    private boolean mFragmentResumed;
    private int mKeyMetaState;

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getComposeThemeResource(this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
            case REQUEST_PICK_IMAGE:
            case REQUEST_OPEN_DOCUMENT: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri src = intent.getData();
                    mTask = AsyncTaskUtils.executeTask(new AddMediaTask(this, src,
                            createTempImageUri(), ParcelableMedia.TYPE_IMAGE, true));
                }
                break;
            }
            case REQUEST_EDIT_IMAGE: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri uri = intent.getData();
                    if (uri == null) {
                        break;
                    }
                    setMenu();
                    updateTextCount();
                }
                break;
            }
            case REQUEST_EXTENSION_COMPOSE: {
                if (resultCode == Activity.RESULT_OK) {
                    final String text = intent.getStringExtra(EXTRA_TEXT);
                    final String append = intent.getStringExtra(EXTRA_APPEND_TEXT);
                    final Uri imageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
                    if (text != null) {
                        mEditText.setText(text);
                    } else if (append != null) {
                        mEditText.append(append);
                    }
                    if (imageUri != null) {
                    }
                    setMenu();
                    updateTextCount();
                }
                break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final boolean textChanged = text != null && !text.isEmpty() && !text.equals(mOriginalText);
        final boolean isEditingDraft = INTENT_ACTION_EDIT_DRAFT.equals(getIntent().getAction());
        if (textChanged || hasMedia() || isEditingDraft) {
            saveToDrafts();
            Toast.makeText(this, R.string.status_saved_to_draft, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mTask = AsyncTaskUtils.executeTask(new DiscardTweetTask(this));
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putLongArray(EXTRA_ACCOUNT_IDS, mAccountsAdapter.getSelectedAccountIds());
        outState.putParcelableArrayList(EXTRA_MEDIA, new ArrayList<Parcelable>(getMediaList()));
        outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
        outState.putParcelable(EXTRA_STATUS, mInReplyToStatus);
        outState.putLong(EXTRA_STATUS_ID, mInReplyToStatusId);
        outState.putParcelable(EXTRA_USER, mMentionUser);
        outState.putParcelable(EXTRA_DRAFT, mDraftItem);
        outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, mShouldSaveAccounts);
        outState.putString(EXTRA_ORIGINAL_TEXT, mOriginalText);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mImageUploaderUsed = !ServicePickerPreference.isNoneValue(mPreferences.getString(KEY_MEDIA_UPLOADER, null));
        mStatusShortenerUsed = !ServicePickerPreference.isNoneValue(mPreferences.getString(KEY_STATUS_SHORTENER, null));
        startLocationUpdateIfEnabled();
        setMenu();
        updateTextCount();
        final int textSize = mPreferences.getInt(KEY_TEXT_SIZE, Utils.getDefaultTextSize(this));
        mEditText.setTextSize(textSize * 1.25f);
    }

    @Override
    protected void onStop() {
        saveAccountSelection();
        try {
            mLocationManager.removeUpdates(this);
        } catch (SecurityException ignore) {
        }
        super.onStop();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.send: {
                confirmAndUpdateStatus();
                break;
            }
            case R.id.account_selector_container: {
                setAccountSelectorVisible(false);
                break;
            }
            case R.id.account_selector_button: {
                final boolean isVisible = mAccountSelectorContainer.getVisibility() == View.VISIBLE;
                mAccountSelectorContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                break;
            }
            case R.id.location_container: {
                toggleLocation();
                break;
            }
        }
    }

    private void confirmAndUpdateStatus() {
        if (isQuotingProtectedStatus()) {
            new RetweetProtectedStatusWarnFragment().show(getSupportFragmentManager(),
                    "retweet_protected_status_warning_message");
        } else {
            updateStatus();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        final Window window = getWindow();
        final Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        final int actionBarHeight = ThemeUtils.getActionBarHeight(this);
        final View contentView = window.findViewById(android.R.id.content);
        final int[] location = new int[2];
        contentView.getLocationOnScreen(location);
        if (location[1] > actionBarHeight) {
            contentView.setPadding(contentView.getPaddingLeft(), 0,
                    contentView.getPaddingRight(), contentView.getPaddingBottom());
            return true;
        }
        final int statusBarHeight = rect.top;
        contentView.getWindowVisibleDisplayFrame(rect);
        final int paddingTop = statusBarHeight + actionBarHeight - rect.top;
        contentView.setPadding(contentView.getPaddingLeft(), paddingTop,
                contentView.getPaddingRight(), contentView.getPaddingBottom());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        final Window window = getWindow();
        final View contentView = window.findViewById(android.R.id.content);
        contentView.setPadding(contentView.getPaddingLeft(), 0,
                contentView.getPaddingRight(), contentView.getPaddingBottom());
    }

    @Override
    public void onLocationChanged(final Location location) {
        setRecentLocation(ParcelableLocation.fromLocation(location));
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.send: {
                Utils.showMenuItemToast(v, getString(R.string.send), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_photo:
            case R.id.take_photo_sub_item: {
                takePhoto();
                break;
            }
            case R.id.add_image:
            case R.id.add_image_sub_item: {
                pickImage();
                break;
            }
            case R.id.add_location: {
                toggleLocation();
                break;
            }
            case R.id.drafts: {
                Utils.openDrafts(this);
                break;
            }
            case R.id.delete: {
                AsyncTaskUtils.executeTask(new DeleteImageTask(this));
                break;
            }
            case R.id.toggle_sensitive: {
                if (!hasMedia()) return false;
                mIsPossiblySensitive = !mIsPossiblySensitive;
                setMenu();
                updateTextCount();
                break;
            }
            case R.id.view: {
                if (mInReplyToStatus == null) return false;
                final DialogFragment fragment = new ViewStatusDialogFragment();
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_STATUS, mInReplyToStatus);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "view_status");
                break;
            }
            case R.id.link_to_quoted_status: {
                final boolean newValue = !item.isChecked();
                item.setChecked(newValue);
                mPreferences.edit().putBoolean(KEY_LINK_TO_QUOTED_TWEET, newValue).apply();
                break;
            }
            default: {
                final Intent intent = item.getIntent();
                if (intent != null) {
                    try {
                        final String action = intent.getAction();
                        if (INTENT_ACTION_EXTENSION_COMPOSE.equals(action)) {
                            final long[] accountIds = mAccountsAdapter.getSelectedAccountIds();
                            intent.putExtra(EXTRA_TEXT, ParseUtils.parseString(mEditText.getText()));
                            intent.putExtra(EXTRA_ACCOUNT_IDS, accountIds);
                            if (accountIds.length > 0) {
                                final long account_id = accountIds[0];
                                intent.putExtra(EXTRA_NAME, Utils.getAccountName(this, account_id));
                                intent.putExtra(EXTRA_SCREEN_NAME, Utils.getAccountScreenName(this, account_id));
                            }
                            if (mInReplyToStatusId > 0) {
                                intent.putExtra(EXTRA_IN_REPLY_TO_ID, mInReplyToStatusId);
                            }
                            if (mInReplyToStatus != null) {
                                intent.putExtra(EXTRA_IN_REPLY_TO_NAME, mInReplyToStatus.user_name);
                                intent.putExtra(EXTRA_IN_REPLY_TO_SCREEN_NAME, mInReplyToStatus.user_screen_name);
                            }
                            startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE);
                        } else if (INTENT_ACTION_EXTENSION_EDIT_IMAGE.equals(action)) {
                            // final ComponentName cmp = intent.getComponent();
                            // if (cmp == null || !hasMedia()) return false;
                            // final String name = new
                            // File(mMediaUri.getPath()).getName();
                            // final Uri data =
                            // Uri.withAppendedPath(CacheFiles.CONTENT_URI,
                            // Uri.encode(name));
                            // intent.setData(data);
                            // grantUriPermission(cmp.getPackageName(), data,
                            // Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            // startActivityForResult(intent,
                            // REQUEST_EDIT_IMAGE);
                        } else {
                            startActivity(intent);
                        }
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
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final Rect rect = new Rect();
                getWindow().getDecorView().getHitRect(rect);
                if (!rect.contains(Math.round(event.getX()), Math.round(event.getY()))) {
                    onBackPressed();
                    return true;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mEditText = (ComposeEditText) findViewById(R.id.edit_text);
        mMediaPreviewGrid = (GridView) findViewById(R.id.media_thumbnail_preview);
        mMenuBar = (ActionMenuView) findViewById(R.id.menu_bar);
        mSendView = findViewById(R.id.send);
        mSendTextCountView = (StatusTextCountView) mSendView.findViewById(R.id.status_text_count);
        mAccountSelector = (RecyclerView) findViewById(R.id.account_selector);
        mAccountSelectorContainer = findViewById(R.id.account_selector_container);
        mProfileImageView = (ShapedImageView) findViewById(R.id.account_profile_image);
        mCountView = (BadgeView) findViewById(R.id.accounts_count);
        mAccountSelectorButton = findViewById(R.id.account_selector_button);
        mLocationContainer = findViewById(R.id.location_container);
        mLocationIcon = (ActionIconView) findViewById(R.id.location_icon);
        mLocationText = (TextView) findViewById(R.id.location_text);
    }

    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(this);
        }
        return mMenuInflater;
    }

    public void removeAllMedia(final List<ParcelableMediaUpdate> list) {
        mMediaPreviewAdapter.removeAll(list);
        updateMediaPreview();
    }

    public void saveToDrafts() {
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final ParcelableStatusUpdate.Builder builder = new ParcelableStatusUpdate.Builder();
        builder.accounts(ParcelableAccount.getAccounts(this, mAccountsAdapter.getSelectedAccountIds()));
        builder.text(text);
        builder.inReplyToStatusId(mInReplyToStatusId);
        builder.location(mRecentLocation);
        builder.isPossiblySensitive(mIsPossiblySensitive);
        if (hasMedia()) {
            builder.media(getMedia());
        }
        final ContentValues values = ContentValuesCreator.createStatusDraft(builder.build());
        final Uri draftUri = mResolver.insert(Drafts.CONTENT_URI, values);
        displayNewDraftNotification(text, draftUri);
    }

    public void setSelectedAccounts(ParcelableAccount... accounts) {
        if (accounts.length == 1) {
            mCountView.setText(null);
            final ParcelableAccount account = accounts[0];
            mImageLoader.displayProfileImage(mProfileImageView, account.profile_image_url);
            mProfileImageView.setBorderColor(account.color);
        } else {
            mCountView.setText(String.valueOf(accounts.length));
            mImageLoader.cancelDisplayTask(mProfileImageView);
            mProfileImageView.setImageDrawable(null);
            mProfileImageView.setBorderColors(Utils.getAccountColors(accounts));
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mResolver = getContentResolver();
        mValidator = new TwidereValidator(this);
        setContentView(R.layout.activity_compose);
        setFinishOnTouchOutside(false);
        final long[] defaultAccountIds = Utils.getAccountIds(this);
        if (defaultAccountIds.length <= 0) {
            final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
            intent.setClass(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        mMenuBar.setOnMenuItemClickListener(this);
        setupEditText();
        mAccountSelectorContainer.setOnClickListener(this);
        mAccountSelectorButton.setOnClickListener(this);
        mLocationContainer.setOnClickListener(this);

        final LinearLayoutManager linearLayoutManager = new FixedLinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        mAccountSelector.setLayoutManager(linearLayoutManager);
        mAccountSelector.addItemDecoration(new SpacingItemDecoration(this));
        mAccountSelector.setItemAnimator(new RecyclerView.ItemAnimator() {
            @Override
            public void runPendingAnimations() {

            }

            @Override
            public boolean animateRemove(ViewHolder holder) {
                return false;
            }

            @Override
            public boolean animateAdd(ViewHolder holder) {
                return false;
            }

            @Override
            public boolean animateMove(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
                return false;
            }

            @Override
            public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
                return false;
            }

            @Override
            public void endAnimation(ViewHolder item) {
            }

            @Override
            public void endAnimations() {

            }

            @Override
            public boolean isRunning() {
                return false;
            }
        });
        mAccountsAdapter = new AccountIconsAdapter(this);
        mAccountSelector.setAdapter(mAccountsAdapter);
        mAccountsAdapter.setAccounts(ParcelableCredentials.getCredentialsArray(this, false, false));

        mMediaPreviewAdapter = new MediaPreviewAdapter(this);
        mMediaPreviewGrid.setAdapter(mMediaPreviewAdapter);

        final Intent intent = getIntent();


        if (savedInstanceState != null) {
            // Restore from previous saved state
            mAccountsAdapter.setSelectedAccountIds(savedInstanceState.getLongArray(EXTRA_ACCOUNT_IDS));
            mIsPossiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE);
            final ArrayList<ParcelableMediaUpdate> mediaList = savedInstanceState.getParcelableArrayList(EXTRA_MEDIA);
            if (mediaList != null) {
                addMedia(mediaList);
            }
            mInReplyToStatus = savedInstanceState.getParcelable(EXTRA_STATUS);
            mInReplyToStatusId = savedInstanceState.getLong(EXTRA_STATUS_ID);
            mMentionUser = savedInstanceState.getParcelable(EXTRA_USER);
            mDraftItem = savedInstanceState.getParcelable(EXTRA_DRAFT);
            mShouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS);
            mOriginalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT);
        } else {
            // The context was first created
            final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            final long notificationAccount = intent.getLongExtra(EXTRA_NOTIFICATION_ACCOUNT, -1);
            if (notificationId != -1) {
                mTwitterWrapper.clearNotificationAsync(notificationId, notificationAccount);
            }
            if (!handleIntent(intent)) {
                handleDefaultIntent(intent);
            }
            final long[] accountIds = mAccountsAdapter.getSelectedAccountIds();
            if (accountIds.length == 0) {
                final long[] idsInPrefs = TwidereArrayUtils.parseLongArray(
                        mPreferences.getString(KEY_COMPOSE_ACCOUNTS, null), ',');
                final long[] intersection = TwidereArrayUtils.intersection(idsInPrefs, defaultAccountIds);
                mAccountsAdapter.setSelectedAccountIds(intersection.length > 0 ? intersection : defaultAccountIds);
            }
            mOriginalText = ParseUtils.parseString(mEditText.getText());
        }
        if (!setComposeTitle(intent)) {
            setTitle(R.string.compose);
        }

        final Menu menu = mMenuBar.getMenu();
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        ThemeUtils.wrapMenuIcon(mMenuBar);

        mSendView.setOnClickListener(this);
        mSendView.setOnLongClickListener(this);
        final Intent composeExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
        Utils.addIntentToMenu(this, menu, composeExtensionsIntent, MENU_GROUP_COMPOSE_EXTENSION);
        final Intent imageExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
        final MenuItem mediaMenuItem = menu.findItem(R.id.media_menu);
        if (mediaMenuItem != null && mediaMenuItem.hasSubMenu()) {
            Utils.addIntentToMenu(this, mediaMenuItem.getSubMenu(), imageExtensionsIntent, MENU_GROUP_IMAGE_EXTENSION);
        }
        setMenu();
        updateLocationState();
        updateMediaPreview();
        notifyAccountSelectionChanged();

        mTextChanged = false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (KeyEvent.isModifierKey(keyCode)) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mKeyMetaState |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mKeyMetaState &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setupEditText() {
        final boolean sendByEnter = mPreferences.getBoolean(KEY_QUICK_SEND);
        EditTextEnterHandler.attach(mEditText, new EnterListener() {
            @Override
            public boolean shouldCallListener() {
                return mKeyMetaState == 0;
            }

            @Override
            public boolean onHitEnter() {
                confirmAndUpdateStatus();
                return true;
            }
        }, sendByEnter);
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                setMenu();
                updateTextCount();
                if (s instanceof Spannable && count == 1 && before == 0) {
                    final ImageSpan[] imageSpans = ((Spannable) s).getSpans(start, start + count, ImageSpan.class);
                    if (imageSpans.length == 1) {
                        final Intent intent = ThemedImagePickerActivity.withThemed(ComposeActivity.this)
                                .getImage(Uri.parse(imageSpans[0].getSource())).build();
                        startActivityForResult(intent, REQUEST_PICK_IMAGE);
                        ((Spannable) s).setSpan(new MarkForDeleteSpan(), start, start + count,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
                mTextChanged = s.length() == 0;
                final MarkForDeleteSpan[] deletes = s.getSpans(0, s.length(), MarkForDeleteSpan.class);
                for (MarkForDeleteSpan delete : deletes) {
                    s.delete(s.getSpanStart(delete), s.getSpanEnd(delete));
                    s.removeSpan(delete);
                }
                for (Object span : s.getSpans(0, s.length(), CharacterStyle.class)) {
                    if (span instanceof URLSpan) {
                        s.removeSpan(span);
                    } else if (span instanceof MetricAffectingSpan) {
                        s.removeSpan(span);
                    }
                }
            }
        });
        mEditText.setCustomSelectionActionModeCallback(this);
    }

    @Override
    protected void onTitleChanged(final CharSequence title, final int color) {
        super.onTitleChanged(title, color);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            if (mEditText.length() == 0 && !mTextChanged) {
                if (!mNavigateBackPressed) {
                    final SuperToast toast = SuperToast.create(this, getString(R.string.press_again_to_close), Duration.SHORT);
                    toast.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(View view) {
                            mNavigateBackPressed = false;
                        }
                    });
                    toast.show();
                } else {
                    onBackPressed();
                }
                mNavigateBackPressed = true;
            } else {
                mTextChanged = false;
            }
            return true;
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    private void addMedia(final ParcelableMediaUpdate media) {
        mMediaPreviewAdapter.add(media);
        updateMediaPreview();
    }

    private void addMedia(final List<ParcelableMediaUpdate> media) {
        mMediaPreviewAdapter.addAll(media);
        updateMediaPreview();
    }

    private void clearMedia() {
        mMediaPreviewAdapter.clear();
        updateMediaPreview();
    }

    private Uri createTempImageUri() {
        final File file = new File(getCacheDir(), "tmp_image_" + System.currentTimeMillis());
        return Uri.fromFile(file);
    }

    private void displayNewDraftNotification(String text, Uri draftUri) {
        final ContentValues values = new ContentValues();
        values.put(BaseColumns._ID, draftUri.getLastPathSegment());
        getContentResolver().insert(Drafts.CONTENT_URI_NOTIFICATIONS, values);
    }

    private ParcelableMediaUpdate[] getMedia() {
        final List<ParcelableMediaUpdate> list = getMediaList();
        return list.toArray(new ParcelableMediaUpdate[list.size()]);
    }

    private List<ParcelableMediaUpdate> getMediaList() {
        return mMediaPreviewAdapter.getAsList();
    }

    private boolean handleDefaultIntent(final Intent intent) {
        if (intent == null) return false;
        final String action = intent.getAction();
        final boolean hasAccountIds;
        if (intent.hasExtra(EXTRA_ACCOUNT_IDS)) {
            mAccountsAdapter.setSelectedAccountIds(intent.getLongArrayExtra(EXTRA_ACCOUNT_IDS));
            hasAccountIds = true;
        } else if (intent.hasExtra(EXTRA_ACCOUNT_ID)) {
            mAccountsAdapter.setSelectedAccountIds(intent.getLongExtra(EXTRA_ACCOUNT_ID, -1));
            hasAccountIds = true;
        } else {
            hasAccountIds = false;
        }
        mShouldSaveAccounts = !Intent.ACTION_SEND.equals(action)
                && !Intent.ACTION_SEND_MULTIPLE.equals(action) && !hasAccountIds;
        final Uri data = intent.getData();
        final CharSequence extraSubject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT);
        final CharSequence extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        final Uri extraStream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        //TODO handle share_screenshot extra (Bitmap)
        if (extraStream != null) {
            AsyncTaskUtils.executeTask(new AddMediaTask(this, extraStream, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false));
        } else if (data != null) {
            AsyncTaskUtils.executeTask(new AddMediaTask(this, data, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false));
        } else if (intent.hasExtra(EXTRA_SHARE_SCREENSHOT) && Utils.useShareScreenshot()) {
            final Bitmap bitmap = intent.getParcelableExtra(EXTRA_SHARE_SCREENSHOT);
            if (bitmap != null) {
                try {
                    AsyncTaskUtils.executeTask(new AddBitmapTask(this, bitmap, createTempImageUri(), ParcelableMedia.TYPE_IMAGE));
                } catch (IOException e) {
                    // ignore
                    bitmap.recycle();
                }
            }
        }
        mEditText.setText(Utils.getShareStatus(this, extraSubject, extraText));
        final int selectionEnd = mEditText.length();
        mEditText.setSelection(selectionEnd);
        return true;
    }

    private boolean handleEditDraftIntent(final DraftItem draft) {
        if (draft == null) return false;
        mEditText.setText(draft.text);
        final int selectionEnd = mEditText.length();
        mEditText.setSelection(selectionEnd);
        mAccountsAdapter.setSelectedAccountIds(draft.account_ids);
        if (draft.media != null) {
            addMedia(Arrays.asList(draft.media));
        }
        mIsPossiblySensitive = draft.is_possibly_sensitive;
        mInReplyToStatusId = draft.in_reply_to_status_id;
        return true;
    }

    private boolean handleIntent(final Intent intent) {
        final String action = intent.getAction();
        if (action == null) return false;
        mShouldSaveAccounts = false;
        mMentionUser = intent.getParcelableExtra(EXTRA_USER);
        mInReplyToStatus = intent.getParcelableExtra(EXTRA_STATUS);
        mInReplyToStatusId = mInReplyToStatus != null ? mInReplyToStatus.id : -1;
        switch (action) {
            case INTENT_ACTION_REPLY: {
                return handleReplyIntent(mInReplyToStatus);
            }
            case INTENT_ACTION_QUOTE: {
                return handleQuoteIntent(mInReplyToStatus);
            }
            case INTENT_ACTION_EDIT_DRAFT: {
                mDraftItem = intent.getParcelableExtra(EXTRA_DRAFT);
                return handleEditDraftIntent(mDraftItem);
            }
            case INTENT_ACTION_MENTION: {
                return handleMentionIntent(mMentionUser);
            }
            case INTENT_ACTION_REPLY_MULTIPLE: {
                final String[] screenNames = intent.getStringArrayExtra(EXTRA_SCREEN_NAMES);
                final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
                final long inReplyToUserId = intent.getLongExtra(EXTRA_IN_REPLY_TO_ID, -1);
                return handleReplyMultipleIntent(screenNames, accountId, inReplyToUserId);
            }
            case INTENT_ACTION_COMPOSE_TAKE_PHOTO: {
                return takePhoto();
            }
            case INTENT_ACTION_COMPOSE_PICK_IMAGE: {
                return pickImage();
            }
        }
        // Unknown action or no intent extras
        return false;
    }

    private boolean handleMentionIntent(final ParcelableUser user) {
        if (user == null || user.id <= 0) return false;
        final String my_screen_name = Utils.getAccountScreenName(this, user.account_id);
        if (TextUtils.isEmpty(my_screen_name)) return false;
        mEditText.setText("@" + user.screen_name + " ");
        final int selection_end = mEditText.length();
        mEditText.setSelection(selection_end);
        mAccountsAdapter.setSelectedAccountIds(user.account_id);
        return true;
    }

    private boolean handleQuoteIntent(final ParcelableStatus status) {
        if (status == null || status.id <= 0) return false;
        mEditText.setText(Utils.getQuoteStatus(this, status.id, status.user_screen_name, status.text_plain));
        mEditText.setSelection(0);
        mAccountsAdapter.setSelectedAccountIds(status.account_id);
        return true;
    }

    private boolean handleReplyIntent(final ParcelableStatus status) {
        if (status == null || status.id <= 0) return false;
        final String myScreenName = Utils.getAccountScreenName(this, status.account_id);
        if (TextUtils.isEmpty(myScreenName)) return false;
        int selectionStart = 0;
        mEditText.append("@" + status.user_screen_name + " ");
        selectionStart = mEditText.length();
        if (status.is_retweet) {
            mEditText.append("@" + status.retweeted_by_user_screen_name + " ");
        }
        final Collection<String> mentions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        mentions.addAll(mExtractor.extractMentionedScreennames(status.text_plain));
        for (final String mention : mentions) {
            if (mention.equalsIgnoreCase(status.user_screen_name) || mention.equalsIgnoreCase(myScreenName)
                    || mention.equalsIgnoreCase(status.retweeted_by_user_screen_name)) {
                continue;
            }
            mEditText.append("@" + mention + " ");
        }
        final int selectionEnd = mEditText.length();
        mEditText.setSelection(selectionStart, selectionEnd);
        mAccountsAdapter.setSelectedAccountIds(status.account_id);
        return true;
    }

    private boolean handleReplyMultipleIntent(final String[] screenNames, final long accountId,
                                              final long inReplyToStatusId) {
        if (screenNames == null || screenNames.length == 0 || accountId <= 0) return false;
        final String myScreenName = Utils.getAccountScreenName(this, accountId);
        if (TextUtils.isEmpty(myScreenName)) return false;
        for (final String screenName : screenNames) {
            if (screenName.equalsIgnoreCase(myScreenName)) {
                continue;
            }
            mEditText.append("@" + screenName + " ");
        }
        mEditText.setSelection(mEditText.length());
        mAccountsAdapter.setSelectedAccountIds(accountId);
        mInReplyToStatusId = inReplyToStatusId;
        return true;
    }

    private boolean hasMedia() {
        return !mMediaPreviewAdapter.isEmpty();
    }

    private boolean isQuote() {
        return INTENT_ACTION_QUOTE.equals(getIntent().getAction());
    }

    private boolean isQuotingProtectedStatus() {
        if (!isQuote() || mInReplyToStatus == null) return false;
        return mInReplyToStatus.user_is_protected && mInReplyToStatus.account_id != mInReplyToStatus.user_id;
    }

    private boolean noReplyContent(final String text) {
        if (text == null) return true;
        final String action = getIntent().getAction();
        final boolean is_reply = INTENT_ACTION_REPLY.equals(action) || INTENT_ACTION_REPLY_MULTIPLE.equals(action);
        return is_reply && text.equals(mOriginalText);
    }

    private void notifyAccountSelectionChanged() {
        final ParcelableAccount[] accounts = mAccountsAdapter.getSelectedAccounts();
        setSelectedAccounts(accounts);
        mEditText.setAccountId(accounts.length > 0 ? accounts[0].account_id : Utils.getDefaultAccountId(this));
        setMenu();
//        mAccountActionProvider.setSelectedAccounts(mAccountsAdapter.getSelectedAccounts());
    }

    private boolean pickImage() {
        final Intent intent = ThemedImagePickerActivity.withThemed(this).pickImage().build();
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
        return true;
    }

    private void saveAccountSelection() {
        if (!mShouldSaveAccounts) return;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_COMPOSE_ACCOUNTS, TwidereArrayUtils.toString(mAccountsAdapter.getSelectedAccountIds(), ',', false));
        editor.apply();
    }

    private void setAccountSelectorVisible(boolean visible) {
        mAccountSelectorContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean setComposeTitle(final Intent intent) {
        final String action = intent.getAction();
        final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        if (INTENT_ACTION_REPLY.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String displayName = mUserColorNameManager.getDisplayName(mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name, nameFirst, false);
            setTitle(getString(R.string.reply_to, displayName));
        } else if (INTENT_ACTION_QUOTE.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String displayName = mUserColorNameManager.getDisplayName(mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name, nameFirst, false);
            setTitle(getString(R.string.quote_user, displayName));
        } else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
            if (mDraftItem == null) return false;
            setTitle(R.string.edit_draft);
        } else if (INTENT_ACTION_MENTION.equals(action)) {
            if (mMentionUser == null) return false;
            final String displayName = mUserColorNameManager.getDisplayName(mMentionUser.id, mMentionUser.name,
                    mMentionUser.screen_name, nameFirst, false);
            setTitle(getString(R.string.mention_user, displayName));
        } else if (INTENT_ACTION_REPLY_MULTIPLE.equals(action)) {
            setTitle(R.string.reply);
        } else if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            setTitle(R.string.share);
        } else {
            setTitle(R.string.compose);
        }
        return true;
    }

    private void setMenu() {
        if (mMenuBar == null) return;
        final Menu menu = mMenuBar.getMenu();
        final boolean hasMedia = hasMedia(), hasInReplyTo = mInReplyToStatus != null;

        /*
         * No media & Not reply: [Take photo][Add image][Attach location][Drafts]
         * Has media & Not reply: [Take photo][Media menu][Attach location][Drafts]
         * Is reply: [Media menu][View status][Attach location][Drafts]
         */
        MenuUtils.setMenuItemAvailability(menu, R.id.take_photo, !hasInReplyTo);
        MenuUtils.setMenuItemAvailability(menu, R.id.take_photo_sub_item, hasInReplyTo);
        MenuUtils.setMenuItemAvailability(menu, R.id.add_image, !hasMedia && !hasInReplyTo);
        MenuUtils.setMenuItemAvailability(menu, R.id.view, hasInReplyTo);
        MenuUtils.setMenuItemAvailability(menu, R.id.media_menu, hasMedia || hasInReplyTo);
        MenuUtils.setMenuItemAvailability(menu, R.id.toggle_sensitive, hasMedia);
        MenuUtils.setMenuItemAvailability(menu, R.id.edit_media, hasMedia);
        MenuUtils.setMenuItemAvailability(menu, R.id.link_to_quoted_status, isQuote());
        MenuUtils.setMenuItemAvailability(menu, R.id.schedule, isScheduleSupported());

        menu.setGroupEnabled(MENU_GROUP_IMAGE_EXTENSION, hasMedia);
        menu.setGroupVisible(MENU_GROUP_IMAGE_EXTENSION, hasMedia);
        MenuUtils.setMenuItemChecked(menu, R.id.toggle_sensitive, hasMedia && mIsPossiblySensitive);
        MenuUtils.setMenuItemChecked(menu, R.id.link_to_quoted_status, mPreferences.getBoolean(KEY_LINK_TO_QUOTED_TWEET));
        ThemeUtils.resetCheatSheet(mMenuBar);
//        mMenuBar.show();
    }

    private boolean isScheduleSupported() {
        for (ParcelableCredentials account : mAccountsAdapter.getSelectedAccounts()) {
            if (TwitterContentUtils.getOfficialKeyType(this, account.consumer_key, account.consumer_secret)
                    != ConsumerKeyType.TWEETDECK) {
                return false;
            }
        }
        return true;
    }

    private void setProgressVisible(final boolean visible) {
        mSetProgressVisibleRunnable = new SetProgressVisibleRunnable(this, visible);
        if (mFragmentResumed) {
            runOnUiThread(mSetProgressVisibleRunnable);
            mSetProgressVisibleRunnable = null;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mFragmentResumed = true;
        if (mSetProgressVisibleRunnable != null) {
            runOnUiThread(mSetProgressVisibleRunnable);
            mSetProgressVisibleRunnable = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFragmentResumed = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdateIfEnabled();
        } else {
            //TODO show permission denied message
        }
    }

    private static class SetProgressVisibleRunnable implements Runnable {

        private final ComposeActivity activity;
        private final boolean visible;

        SetProgressVisibleRunnable(ComposeActivity activity, boolean visible) {
            this.activity = activity;
            this.visible = visible;
        }

        @Override
        public void run() {
            final FragmentManager fm = activity.getSupportFragmentManager();
            final Fragment f = fm.findFragmentByTag(DISCARD_STATUS_DIALOG_FRAGMENT_TAG);
            if (!visible && f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            } else if (visible) {
                SupportProgressDialogFragment df = new SupportProgressDialogFragment();
                df.show(fm, DISCARD_STATUS_DIALOG_FRAGMENT_TAG);
                df.setCancelable(false);
            }
        }
    }

    private void setRecentLocation(ParcelableLocation location) {
        if (location != null) {
            mLocationText.setText(location.getHumanReadableString(3));
        } else {
            mLocationText.setText(R.string.unknown_location);
        }
        mRecentLocation = location;
    }

    /**
     * The Location Manager manages location providers. This code searches for
     * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
     * mechanism) and finds the last known location.
     */
    private boolean startLocationUpdateIfEnabled() {
        final LocationManager lm = mLocationManager;
        try {
            lm.removeUpdates(this);
        } catch (SecurityException ignore) {
        }
        final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION);
        if (!attachLocation) {
            return false;
        }
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        final String provider = lm.getBestProvider(criteria, true);
        if (provider != null) {
            try {
                mLocationText.setText(R.string.getting_location);
                lm.requestLocationUpdates(provider, 0, 0, this);
                final Location location = Utils.getCachedLocation(this);
                if (location != null) {
                    onLocationChanged(location);
                }
            } catch (SecurityException e) {
                return false;
            }
        } else {
            Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show();
        }
        return provider != null;
    }

    private boolean takePhoto() {
        final Intent intent = ThemedImagePickerActivity.withThemed(this).takePhoto().build();
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        return true;
    }

    private void toggleLocation() {
        final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
        mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, !attachLocation).apply();
        requestOrUpdateLocation();
        updateLocationState();
        setMenu();
        updateTextCount();
    }

    private void requestOrUpdateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdateIfEnabled();
        } else {
            final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_REQUEST_PERMISSIONS);
        }
    }

    private void updateLocationState() {
        final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
        if (attachLocation) {
            mLocationIcon.setColorFilter(ThemeUtils.getOptimalAccentColor(this, false,
                    getCurrentThemeResourceId()), Mode.SRC_ATOP);
        } else {
            mLocationIcon.setColorFilter(mLocationIcon.getDefaultColor(), Mode.SRC_ATOP);
            mLocationText.setText(R.string.no_location);
        }
    }

    private void updateMediaPreview() {
        final int count = mMediaPreviewAdapter.getCount();
        final Resources res = getResources();
        final int maxColumns = res.getInteger(R.integer.grid_column_image_preview);
        mMediaPreviewGrid.setNumColumns(MathUtils.clamp(count, maxColumns, 1));
    }

    private void updateStatus() {
        if (isFinishing()) return;
        final boolean hasMedia = hasMedia();
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final int tweetLength = mValidator.getTweetLength(text), maxLength = mValidator.getMaxTweetLength();
        if (!mStatusShortenerUsed && tweetLength > maxLength) {
            mEditText.setError(getString(R.string.error_message_status_too_long));
            final int textLength = mEditText.length();
            mEditText.setSelection(textLength - (tweetLength - maxLength), textLength);
            return;
        } else if (!hasMedia && (TextUtils.isEmpty(text) || noReplyContent(text))) {
            mEditText.setError(getString(R.string.error_message_no_content));
            return;
        } else if (mAccountsAdapter.isSelectionEmpty()) {
            mEditText.setError(getString(R.string.no_account_selected));
            return;
        }
        final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
        final long[] accountIds = mAccountsAdapter.getSelectedAccountIds();
        final boolean isQuote = isQuote();
        final ParcelableLocation statusLocation = attachLocation ? mRecentLocation : null;
        final boolean linkToQuotedTweet = mPreferences.getBoolean(KEY_LINK_TO_QUOTED_TWEET, true);
        final long inReplyToStatusId = !isQuote || linkToQuotedTweet ? mInReplyToStatusId : -1;
        final boolean isPossiblySensitive = hasMedia && mIsPossiblySensitive;
        mTwitterWrapper.updateStatusAsync(accountIds, text, statusLocation, getMedia(), inReplyToStatusId,
                isPossiblySensitive);
        if (mPreferences.getBoolean(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
                && (mInReplyToStatus == null || mInReplyToStatusId <= 0)) {
            mIsPossiblySensitive = false;
            mShouldSaveAccounts = true;
            mInReplyToStatus = null;
            mMentionUser = null;
            mDraftItem = null;
            mInReplyToStatusId = -1;
            mOriginalText = null;
            mEditText.setText(null);
            clearMedia();
            final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
            setIntent(intent);
            setComposeTitle(intent);
            handleIntent(intent);
            setMenu();
            updateTextCount();
        } else {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void updateTextCount() {
        if (mSendTextCountView == null || mEditText == null) return;
        final String textOrig = ParseUtils.parseString(mEditText.getText());
        final String text = hasMedia() && textOrig != null ? mImageUploaderUsed ? Utils.getImageUploadStatus(this,
                new String[]{FAKE_IMAGE_LINK}, textOrig) : textOrig + " " + FAKE_IMAGE_LINK : textOrig;
        final int validatedCount = text != null ? mValidator.getTweetLength(text) : 0;
        mSendTextCountView.setTextCount(validatedCount);
    }

    static class AccountIconViewHolder extends ViewHolder implements OnClickListener {

        private final AccountIconsAdapter adapter;
        private final ShapedImageView iconView;
        private final TextView nameView;

        public AccountIconViewHolder(AccountIconsAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            iconView = (ShapedImageView) itemView.findViewById(android.R.id.icon);
            nameView = (TextView) itemView.findViewById(android.R.id.text1);
            itemView.setOnClickListener(this);
        }

        public void showAccount(AccountIconsAdapter adapter, ParcelableAccount account, boolean isSelected) {
            itemView.setAlpha(isSelected ? 1 : 0.33f);
            final MediaLoaderWrapper loader = adapter.getImageLoader();
            loader.displayProfileImage(iconView, account.profile_image_url);
            iconView.setBorderColor(account.color);
            nameView.setText(adapter.isNameFirst() ? account.name : ("@" + account.screen_name));
        }

        @Override
        public void onClick(View v) {
            adapter.toggleSelection(getAdapterPosition());
        }


    }

    private static class AccountIconsAdapter extends BaseRecyclerViewAdapter<AccountIconViewHolder> {

        private final ComposeActivity mActivity;
        private final LayoutInflater mInflater;
        private final LongSparseArray<Boolean> mSelection;
        private final boolean mNameFirst;

        private ParcelableCredentials[] mAccounts;

        public AccountIconsAdapter(ComposeActivity activity) {
            super(activity);
            setHasStableIds(true);
            mActivity = activity;
            mInflater = activity.getLayoutInflater();
            mSelection = new LongSparseArray<>();
            mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
        }

        public MediaLoaderWrapper getImageLoader() {
            return mMediaLoader;
        }

        @NonNull
        public long[] getSelectedAccountIds() {
            if (mAccounts == null) return new long[0];
            final long[] temp = new long[mAccounts.length];
            int selectedCount = 0;
            for (ParcelableAccount account : mAccounts) {
                if (mSelection.get(account.account_id, false)) {
                    temp[selectedCount++] = account.account_id;
                }
            }
            final long[] result = new long[selectedCount];
            System.arraycopy(temp, 0, result, 0, result.length);
            return result;
        }

        public void setSelectedAccountIds(long... accountIds) {
            mSelection.clear();
            if (accountIds != null) {
                for (long accountId : accountIds) {
                    mSelection.put(accountId, true);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        public ParcelableCredentials[] getSelectedAccounts() {
            if (mAccounts == null) return new ParcelableCredentials[0];
            final ParcelableCredentials[] temp = new ParcelableCredentials[mAccounts.length];
            int selectedCount = 0;
            for (ParcelableCredentials account : mAccounts) {
                if (mSelection.get(account.account_id, false)) {
                    temp[selectedCount++] = account;
                }
            }
            final ParcelableCredentials[] result = new ParcelableCredentials[selectedCount];
            System.arraycopy(temp, 0, result, 0, result.length);
            return result;
        }

        public boolean isSelectionEmpty() {
            return getSelectedAccountIds().length == 0;
        }

        @Override
        public AccountIconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_compose_account, parent, false);
            return new AccountIconViewHolder(this, view);
        }

        @Override
        public void onBindViewHolder(AccountIconViewHolder holder, int position) {
            final ParcelableAccount account = mAccounts[position];
            final boolean isSelected = mSelection.get(account.account_id, false);
            holder.showAccount(this, account, isSelected);
        }

        @Override
        public int getItemCount() {
            return mAccounts != null ? mAccounts.length : 0;
        }

        public void setAccounts(ParcelableCredentials[] accounts) {
            mAccounts = accounts;
            notifyDataSetChanged();
        }

        private void toggleSelection(int position) {
            if (mAccounts == null) return;
            final long accountId = mAccounts[position].account_id;
            mSelection.put(accountId, !mSelection.get(accountId, false));
            mActivity.notifyAccountSelectionChanged();
            notifyDataSetChanged();
        }

        public boolean isNameFirst() {
            return mNameFirst;
        }
    }

    private static class AddBitmapTask extends AddMediaTask {

        private final Bitmap mBitmap;

        AddBitmapTask(ComposeActivity activity, Bitmap bitmap, Uri dst, int media_type) throws IOException {
            super(activity, Uri.fromFile(File.createTempFile("tmp_bitmap", null)), dst, media_type,
                    true);
            mBitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            if (mBitmap == null || mBitmap.isRecycled()) return false;
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(getSrc().getPath());
                mBitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                mBitmap.recycle();
            } catch (IOException e) {
                return false;
            } finally {
                IoUtils.closeSilently(os);
            }
            return super.doInBackground(params);
        }

    }

    private static class AddMediaTask extends AsyncTask<Object, Object, Boolean> {

        private final ComposeActivity activity;
        private final int media_type;
        private final Uri src, dst;
        private final boolean delete_src;

        AddMediaTask(final ComposeActivity activity, final Uri src, final Uri dst, final int media_type,
                     final boolean delete_src) {
            this.activity = activity;
            this.src = src;
            this.dst = dst;
            this.media_type = media_type;
            this.delete_src = delete_src;
        }

        @Override
        protected Boolean doInBackground(final Object... params) {
            InputStream is = null;
            OutputStream os = null;
            try {
                final ContentResolver resolver = activity.getContentResolver();
                is = resolver.openInputStream(src);
                os = resolver.openOutputStream(dst);
                Utils.copyStream(is, os);
                if (ContentResolver.SCHEME_FILE.equals(src.getScheme()) && delete_src) {
                    final File file = new File(src.getPath());
                    if (!file.delete()) {
                        Log.d(LOGTAG, String.format("Unable to delete %s", file));
                    }
                }
            } catch (final IOException e) {
                Log.w(LOGTAG, e);
                return false;
            } finally {
                Utils.closeSilently(os);
                Utils.closeSilently(is);
            }
            return true;
        }

        Uri getSrc() {
            return src;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            activity.setProgressVisible(false);
            activity.addMedia(new ParcelableMediaUpdate(dst.toString(), media_type));
            activity.setMenu();
            activity.updateTextCount();
            if (!result) {
                Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.setProgressVisible(true);
        }
    }

    private static class DeleteImageTask extends AsyncTask<Object, Object, Boolean> {

        final ComposeActivity mActivity;
        private final ParcelableMediaUpdate[] mMedia;

        DeleteImageTask(final ComposeActivity activity, final ParcelableMediaUpdate... media) {
            this.mActivity = activity;
            this.mMedia = media;
        }

        @Override
        protected Boolean doInBackground(final Object... params) {
            if (mMedia == null) return false;
            try {
                for (final ParcelableMediaUpdate media : mMedia) {
                    if (media.uri == null) continue;
                    final Uri uri = Uri.parse(media.uri);
                    if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                        final File file = new File(uri.getPath());
                        if (!file.delete()) {
                            Log.d(LOGTAG, String.format("Unable to delete %s", file));
                        }
                    }
                }
            } catch (final Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            mActivity.setProgressVisible(false);
            mActivity.removeAllMedia(Arrays.asList(mMedia));
            mActivity.setMenu();
            if (!result) {
                Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            mActivity.setProgressVisible(true);
        }
    }

    private static class DiscardTweetTask extends AsyncTask<Object, Object, Object> {

        final ComposeActivity mActivity;

        DiscardTweetTask(final ComposeActivity activity) {
            this.mActivity = activity;
        }

        @Override
        protected Object doInBackground(final Object... params) {
            for (final ParcelableMediaUpdate media : mActivity.getMediaList()) {
                if (media.uri == null) continue;
                final Uri uri = Uri.parse(media.uri);
                if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                    final File file = new File(uri.getPath());
                    if (!file.delete()) {
                        Log.d(LOGTAG, String.format("Unable to delete %s", file));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Object result) {
            mActivity.setProgressVisible(false);
            mActivity.finish();
        }

        @Override
        protected void onPreExecute() {
            mActivity.setProgressVisible(true);
        }
    }

    private static class MediaPreviewAdapter extends DraggableArrayAdapter<ParcelableMediaUpdate> {

        private final MediaLoaderWrapper mImageLoader;

        public MediaPreviewAdapter(final ComposeActivity activity) {
            super(activity, R.layout.grid_item_media_editor);
            mImageLoader = activity.mImageLoader;
        }

        public List<ParcelableMediaUpdate> getAsList() {
            return Collections.unmodifiableList(getObjects());
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final ParcelableMediaUpdate media = getItem(position);
            final ImageView image = (ImageView) view.findViewById(R.id.image);
            mImageLoader.displayPreviewImage(media.uri, image);
            return view;
        }


    }

    public static class RetweetProtectedStatusWarnFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Activity activity = getActivity();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    if (activity instanceof ComposeActivity) {
                        ((ComposeActivity) activity).updateStatus();
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
            builder.setMessage(R.string.quote_protected_status_warning_message);
            builder.setPositiveButton(R.string.send_anyway, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }
    }

    private static class SpacingItemDecoration extends ItemDecoration {

        private final int mSpacingSmall, mSpacingNormal;

        SpacingItemDecoration(Context context) {
            final Resources resources = context.getResources();
            mSpacingSmall = resources.getDimensionPixelSize(R.dimen.element_spacing_small);
            mSpacingNormal = resources.getDimensionPixelSize(R.dimen.element_spacing_normal);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            final int pos = parent.getChildAdapterPosition(view);
            if (pos == 0) {
                outRect.set(0, mSpacingNormal, 0, 0);
            } else if (pos == parent.getAdapter().getItemCount() - 1) {
                outRect.set(0, 0, 0, mSpacingNormal);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }
    }

}
