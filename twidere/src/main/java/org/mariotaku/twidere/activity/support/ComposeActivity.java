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

import android.annotation.TargetApi;
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.twitter.Extractor;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.dynamicgridview.DraggableArrayAdapter;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.menucomponent.internal.widget.IListPopupWindow;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.BaseArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.view.ComposeSelectAccountButton;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.TwidereMenuBar;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

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

import static android.os.Environment.getExternalStorageState;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.ThemeUtils.getActionBarBackground;
import static org.mariotaku.twidere.util.ThemeUtils.getComposeThemeResource;
import static org.mariotaku.twidere.util.ThemeUtils.getWindowContentOverlayForCompose;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

public class ComposeActivity extends BaseSupportDialogActivity implements TextWatcher, LocationListener,
        OnMenuItemClickListener, OnClickListener, OnEditorActionListener, OnLongClickListener, OnItemClickListener {

    private static final String FAKE_IMAGE_LINK = "https://www.example.com/fake_image.jpg";

    private static final String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

    private static final String EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts";

    private static final String EXTRA_ORIGINAL_TEXT = "original_text";

    private static final String EXTRA_TEMP_URI = "temp_uri";
    private static final String EXTRA_SHARE_SCREENSHOT = "share_screenshot";

    private TwidereValidator mValidator;
    private final Extractor mExtractor = new Extractor();

    private AsyncTwitterWrapper mTwitterWrapper;
    private LocationManager mLocationManager;
    private SharedPreferencesWrapper mPreferences;
    private ParcelableLocation mRecentLocation;

    private ContentResolver mResolver;
    private TwidereAsyncTask<Void, Void, ?> mTask;
    private IListPopupWindow mAccountSelectorPopup;
    private TextView mTitleView, mSubtitleView;
    private GridView mMediaPreviewGrid;

    private TwidereMenuBar mMenuBar;
    private EditText mEditText;
    private ProgressBar mProgress;
    private View mSendView;
    private StatusTextCountView mSendTextCountView;
    private ComposeSelectAccountButton mSelectAccountAccounts;

    private MediaPreviewAdapter mMediaPreviewAdapter;

    private boolean mIsPossiblySensitive, mShouldSaveAccounts;

    private long[] mSendAccountIds;

    private Uri mTempPhotoUri;
    private boolean mImageUploaderUsed, mStatusShortenerUsed;
    private ParcelableStatus mInReplyToStatus;

    private ParcelableUser mMentionUser;
    private DraftItem mDraftItem;
    private long mInReplyToStatusId;
    private String mOriginalText;

    private final Rect mWindowDecorHitRect = new Rect();

    @Override
    public void afterTextChanged(final Editable s) {

    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return getComposeThemeResource(this);
    }

    public boolean handleMenuItem(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_TAKE_PHOTO:
            case R.id.take_photo_sub_item: {
                takePhoto();
                break;
            }
            case MENU_ADD_IMAGE:
            case R.id.add_image_sub_item: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || !openDocument()) {
                    pickImage();
                }
                break;
            }
            case MENU_ADD_LOCATION: {
                final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
                if (!attachLocation) {
                    getLocation();
                } else {
                    mLocationManager.removeUpdates(this);
                }
                mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, !attachLocation).apply();
                setMenu();
                updateTextCount();
                break;
            }
            case MENU_DRAFTS: {
                startActivity(new Intent(INTENT_ACTION_DRAFTS));
                break;
            }
            case MENU_DELETE: {
                new DeleteImageTask(this).executeTask();
                break;
            }
            case MENU_TOGGLE_SENSITIVE: {
                if (!hasMedia()) return false;
                mIsPossiblySensitive = !mIsPossiblySensitive;
                setMenu();
                updateTextCount();
                break;
            }
            case MENU_VIEW: {
                if (mInReplyToStatus == null) return false;
                final DialogFragment fragment = new ViewStatusDialogFragment();
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_STATUS, mInReplyToStatus);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "view_status");
                break;
            }
            default: {
                final Intent intent = item.getIntent();
                if (intent != null) {
                    try {
                        final String action = intent.getAction();
                        if (INTENT_ACTION_EXTENSION_COMPOSE.equals(action)) {
                            intent.putExtra(EXTRA_TEXT, ParseUtils.parseString(mEditText.getText()));
                            intent.putExtra(EXTRA_ACCOUNT_IDS, mSendAccountIds);
                            if (mSendAccountIds != null && mSendAccountIds.length > 0) {
                                final long account_id = mSendAccountIds[0];
                                intent.putExtra(EXTRA_NAME, getAccountName(this, account_id));
                                intent.putExtra(EXTRA_SCREEN_NAME, getAccountScreenName(this, account_id));
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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO: {
                if (resultCode == Activity.RESULT_OK) {
                    mTask = new AddMediaTask(this, mTempPhotoUri, createTempImageUri(), ParcelableMedia.TYPE_IMAGE,
                            true).executeTask();
                    mTempPhotoUri = null;
                }
                break;
            }
            case REQUEST_PICK_IMAGE: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri src = intent.getData();
                    mTask = new AddMediaTask(this, src, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false)
                            .executeTask();
                }
                break;
            }
            case REQUEST_OPEN_DOCUMENT: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri src = intent.getData();
                    mTask = new AddMediaTask(this, src, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false)
                            .executeTask();
                }
                break;
            }
            case REQUEST_EDIT_IMAGE: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri uri = intent.getData();
                    if (uri != null) {
                    } else {
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
        if (mTask != null && mTask.getStatus() == TwidereAsyncTask.Status.RUNNING) return;
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final boolean textChanged = text != null && !text.isEmpty() && !text.equals(mOriginalText);
        final boolean isEditingDraft = INTENT_ACTION_EDIT_DRAFT.equals(getIntent().getAction());
        if (textChanged || hasMedia() || isEditingDraft) {
            saveToDrafts();
            Toast.makeText(this, R.string.status_saved_to_draft, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mTask = new DiscardTweetTask(this).executeTask();
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.close: {
                onBackPressed();
                break;
            }
            case R.id.send: {
                if (isQuotingProtectedStatus()) {
                    new RetweetProtectedStatusWarnFragment().show(getSupportFragmentManager(),
                            "retweet_protected_status_warning_message");
                } else {
                    updateStatus();
                }
                break;
            }
            case R.id.select_account: {
                if (!mAccountSelectorPopup.isShowing()) {
                    mAccountSelectorPopup.show();
                }
                final ListView listView = mAccountSelectorPopup.getListView();
                listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                listView.setOnItemClickListener(this);
                for (int i = 0, j = listView.getCount(); i < j; i++) {
                    final long itemId = listView.getItemIdAtPosition(i);
                    listView.setItemChecked(i, ArrayUtils.contains(mSendAccountIds, itemId));
                }
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        findViewById(R.id.close).setOnClickListener(this);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mTitleView = (TextView) findViewById(R.id.actionbar_title);
        mSubtitleView = (TextView) findViewById(R.id.actionbar_subtitle);
        mMediaPreviewGrid = (GridView) findViewById(R.id.media_thumbnail_preview);
        mMenuBar = (TwidereMenuBar) findViewById(R.id.menu_bar);
        mProgress = (ProgressBar) findViewById(R.id.actionbar_progress_indeterminate);
        final View composeActionBar = findViewById(R.id.compose_actionbar);
        final View composeBottomBar = findViewById(R.id.compose_bottombar);
        mSendView = composeBottomBar.findViewById(R.id.send);
        mSendTextCountView = (StatusTextCountView) mSendView.findViewById(R.id.status_text_count);
        mSelectAccountAccounts = (ComposeSelectAccountButton) composeActionBar.findViewById(R.id.select_account);
        ViewAccessor.setBackground(findViewById(R.id.compose_content), getWindowContentOverlayForCompose(this));
        ViewAccessor.setBackground(composeActionBar, getActionBarBackground(this, getCurrentThemeResourceId()));
    }

    @Override
    public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
        if (event == null) return false;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER: {
                updateStatus();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (mRecentLocation == null) {
            mRecentLocation = location != null ? new ParcelableLocation(location) : null;
            setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.send: {
                showMenuItemToast(v, getString(R.string.send), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return handleMenuItem(item);
    }

    @Override
    public void onProviderDisabled(final String provider) {
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putLongArray(EXTRA_ACCOUNT_IDS, mSendAccountIds);
        outState.putParcelableArrayList(EXTRA_MEDIA, new ArrayList<Parcelable>(getMediaList()));
        outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, mIsPossiblySensitive);
        outState.putParcelable(EXTRA_STATUS, mInReplyToStatus);
        outState.putLong(EXTRA_STATUS_ID, mInReplyToStatusId);
        outState.putParcelable(EXTRA_USER, mMentionUser);
        outState.putParcelable(EXTRA_DRAFT, mDraftItem);
        outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, mShouldSaveAccounts);
        outState.putString(EXTRA_ORIGINAL_TEXT, mOriginalText);
        outState.putParcelable(EXTRA_TEMP_URI, mTempPhotoUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        setMenu();
        updateTextCount();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                getWindow().getDecorView().getHitRect(mWindowDecorHitRect);
                if (!mWindowDecorHitRect.contains(Math.round(event.getX()), Math.round(event.getY()))) {
                    onBackPressed();
                    return true;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void removeAllMedia(final List<ParcelableMediaUpdate> list) {
        mMediaPreviewAdapter.removeAll(list);
        updateMediaPreview();
    }

    public void saveToDrafts() {
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final ParcelableStatusUpdate.Builder builder = new ParcelableStatusUpdate.Builder();
        builder.accounts(ParcelableAccount.getAccounts(this, mSendAccountIds));
        builder.text(text);
        builder.inReplyToStatusId(mInReplyToStatusId);
        builder.location(mRecentLocation);
        builder.isPossiblySensitive(mIsPossiblySensitive);
        if (hasMedia()) {
            builder.media(getMedia());
        }
        final ContentValues values = ContentValuesCreator.createStatusDraft(builder.build());
        mResolver.insert(Drafts.CONTENT_URI, values);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPreferences = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mTwitterWrapper = getTwidereApplication().getTwitterWrapper();
        mResolver = getContentResolver();
        mValidator = new TwidereValidator(this);
        setContentView(R.layout.activity_compose);
        setProgressBarIndeterminateVisibility(false);
        setFinishOnTouchOutside(false);
        final long[] defaultAccountIds = getAccountIds(this);
        if (defaultAccountIds.length <= 0) {
            final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
            intent.setClass(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        mMenuBar.setIsBottomBar(true);
        mMenuBar.setOnMenuItemClickListener(this);
        mEditText.setOnEditorActionListener(mPreferences.getBoolean(KEY_QUICK_SEND, false) ? this : null);
        mEditText.addTextChangedListener(this);
        final AccountSelectorAdapter accountAdapter = new AccountSelectorAdapter(mMenuBar.getPopupContext());
        accountAdapter.addAll(ParcelableAccount.getAccountsList(this, false));
        mAccountSelectorPopup = IListPopupWindow.InstanceHelper.getInstance(mMenuBar.getPopupContext());
        mAccountSelectorPopup.setInputMethodMode(IListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        mAccountSelectorPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mAccountSelectorPopup.setModal(true);
        mAccountSelectorPopup.setContentWidth(getResources().getDimensionPixelSize(R.dimen.account_selector_popup_width));
        mAccountSelectorPopup.setAdapter(accountAdapter);
        mAccountSelectorPopup.setAnchorView(mSelectAccountAccounts);
//        mSelectAccountButton.setOnTouchListener(ListPopupWindowCompat.createDragToOpenListener(
//                mAccountSelectorPopup, mSelectAccountButton));

        mSelectAccountAccounts.setOnClickListener(this);
        mSelectAccountAccounts.setOnLongClickListener(this);

        mMediaPreviewAdapter = new MediaPreviewAdapter(this);
        mMediaPreviewGrid.setAdapter(mMediaPreviewAdapter);

        final Intent intent = getIntent();

        if (savedInstanceState != null) {
            // Restore from previous saved state
            mSendAccountIds = savedInstanceState.getLongArray(EXTRA_ACCOUNT_IDS);
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
            mTempPhotoUri = savedInstanceState.getParcelable(EXTRA_TEMP_URI);
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
            if (mSendAccountIds == null || mSendAccountIds.length == 0) {
                final long[] idsInPrefs = TwidereArrayUtils.parseLongArray(
                        mPreferences.getString(KEY_COMPOSE_ACCOUNTS, null), ',');
                final long[] intersection = TwidereArrayUtils.intersection(idsInPrefs, defaultAccountIds);
                mSendAccountIds = intersection.length > 0 ? intersection : defaultAccountIds;
            }
            mOriginalText = ParseUtils.parseString(mEditText.getText());
        }
        if (!setComposeTitle(intent)) {
            setTitle(R.string.compose);
        }

        mMenuBar.inflate(R.menu.menu_compose);
        mSendView.setOnClickListener(this);
        mSendView.setOnLongClickListener(this);
        final Menu menu = mMenuBar.getMenu();
        final Intent composeExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
        addIntentToMenu(this, menu, composeExtensionsIntent, MENU_GROUP_COMPOSE_EXTENSION);
        final Intent imageExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
        final MenuItem mediaMenuItem = menu.findItem(R.id.media_menu);
        if (mediaMenuItem != null && mediaMenuItem.hasSubMenu()) {
            addIntentToMenu(this, mediaMenuItem.getSubMenu(), imageExtensionsIntent, MENU_GROUP_IMAGE_EXTENSION);
        }
        setMenu();
        updateAccountSelection();
        updateMediaPreview();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mImageUploaderUsed = !ServicePickerPreference.isNoneValue(mPreferences.getString(KEY_MEDIA_UPLOADER, null));
        mStatusShortenerUsed = !ServicePickerPreference.isNoneValue(mPreferences.getString(KEY_STATUS_SHORTENER, null));
        setMenu();
        updateTextCount();
        final int text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(this));
        mEditText.setTextSize(text_size * 1.25f);
    }

    @Override
    protected void onStop() {
        if (mAccountSelectorPopup != null && mAccountSelectorPopup.isShowing()) {
            mAccountSelectorPopup.dismiss();
        }
        mLocationManager.removeUpdates(this);
        super.onStop();
    }

    @Override
    protected void onTitleChanged(final CharSequence title, final int color) {
        super.onTitleChanged(title, color);
        mTitleView.setText(title);
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

    /**
     * The Location Manager manages location providers. This code searches for
     * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
     * mechanism) and finds the last known location.
     */
    private boolean getLocation() {
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        final String provider = mLocationManager.getBestProvider(criteria, true);

        if (provider != null) {
            final Location location;
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                location = mLocationManager.getLastKnownLocation(provider);
            }
            if (location == null) {
                mLocationManager.requestLocationUpdates(provider, 0, 0, this);
                setProgressVisibility(true);
            }
            mRecentLocation = location != null ? new ParcelableLocation(location) : null;
        } else {
            Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show();
        }
        return provider != null;
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
            mSendAccountIds = intent.getLongArrayExtra(EXTRA_ACCOUNT_IDS);
            hasAccountIds = true;
        } else if (intent.hasExtra(EXTRA_ACCOUNT_ID)) {
            mSendAccountIds = new long[]{intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)};
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
            new AddMediaTask(this, extraStream, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false).executeTask();
        } else if (data != null) {
            new AddMediaTask(this, data, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false).executeTask();
        } else if (intent.hasExtra(EXTRA_SHARE_SCREENSHOT)) {
            final Bitmap bitmap = intent.getParcelableExtra(EXTRA_SHARE_SCREENSHOT);
            if (bitmap != null) {
                try {
                    new AddBitmapTask(this, bitmap, createTempImageUri(), ParcelableMedia.TYPE_IMAGE).executeTask();
                } catch (IOException e) {
                    // ignore
                    bitmap.recycle();
                }
            }
        }
        mEditText.setText(getShareStatus(this, extraSubject, extraText));
        final int selection_end = mEditText.length();
        mEditText.setSelection(selection_end);
        return true;
    }

    private boolean handleEditDraftIntent(final DraftItem draft) {
        if (draft == null) return false;
        mEditText.setText(draft.text);
        final int selection_end = mEditText.length();
        mEditText.setSelection(selection_end);
        mSendAccountIds = draft.account_ids;
        if (draft.media != null) {
            addMedia(Arrays.asList(draft.media));
        }
        mIsPossiblySensitive = draft.is_possibly_sensitive;
        mInReplyToStatusId = draft.in_reply_to_status_id;
        return true;
    }

    private boolean handleIntent(final Intent intent) {
        final String action = intent.getAction();
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
        final String my_screen_name = getAccountScreenName(this, user.account_id);
        if (isEmpty(my_screen_name)) return false;
        mEditText.setText("@" + user.screen_name + " ");
        final int selection_end = mEditText.length();
        mEditText.setSelection(selection_end);
        mSendAccountIds = new long[]{user.account_id};
        return true;
    }

    private boolean handleQuoteIntent(final ParcelableStatus status) {
        if (status == null || status.id <= 0) return false;
        mEditText.setText(getQuoteStatus(this, status.user_screen_name, status.text_plain));
        mEditText.setSelection(0);
        mSendAccountIds = new long[]{status.account_id};
        return true;
    }

    private boolean handleReplyIntent(final ParcelableStatus status) {
        if (status == null || status.id <= 0) return false;
        final String myScreenName = getAccountScreenName(this, status.account_id);
        if (isEmpty(myScreenName)) return false;
        mEditText.append("@" + status.user_screen_name + " ");
        final int selectionStart = mEditText.length();
        if (!isEmpty(status.retweeted_by_screen_name)) {
            mEditText.append("@" + status.retweeted_by_screen_name + " ");
        }
        final Collection<String> mentions = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        mentions.addAll(mExtractor.extractMentionedScreennames(status.text_plain));
        for (final String mention : mentions) {
            if (mention.equalsIgnoreCase(status.user_screen_name) || mention.equalsIgnoreCase(myScreenName)
                    || mention.equalsIgnoreCase(status.retweeted_by_screen_name)) {
                continue;
            }
            mEditText.append("@" + mention + " ");
        }
        final int selectionEnd = mEditText.length();
        mEditText.setSelection(selectionStart, selectionEnd);
        mSendAccountIds = new long[]{status.account_id};
        return true;
    }

    private boolean handleReplyMultipleIntent(final String[] screenNames, final long accountId,
                                              final long inReplyToStatusId) {
        if (screenNames == null || screenNames.length == 0 || accountId <= 0) return false;
        final String myScreenName = getAccountScreenName(this, accountId);
        if (isEmpty(myScreenName)) return false;
        for (final String screenName : screenNames) {
            if (screenName.equalsIgnoreCase(myScreenName)) {
                continue;
            }
            mEditText.append("@" + screenName + " ");
        }
        mEditText.setSelection(mEditText.length());
        mSendAccountIds = new long[]{accountId};
        mInReplyToStatusId = inReplyToStatusId;
        return true;
    }

    private boolean hasMedia() {
        return !mMediaPreviewAdapter.isEmpty();
    }

    private int getMediaCount() {
        return mMediaPreviewAdapter.getCount();
    }

    private boolean isQuotingProtectedStatus() {
        if (INTENT_ACTION_QUOTE.equals(getIntent().getAction()) && mInReplyToStatus != null)
            return mInReplyToStatus.user_is_protected && mInReplyToStatus.account_id != mInReplyToStatus.user_id;
        return false;
    }

    private boolean noReplyContent(final String text) {
        if (text == null) return true;
        final String action = getIntent().getAction();
        final boolean is_reply = INTENT_ACTION_REPLY.equals(action) || INTENT_ACTION_REPLY_MULTIPLE.equals(action);
        return is_reply && text.equals(mOriginalText);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean openDocument() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        final String[] mimeTypes = {"image/png", "image/jpeg", "image/gif"};
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_OPEN_DOCUMENT);
        } catch (final ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean pickImage() {
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } catch (final ActivityNotFoundException e) {
            showErrorMessage(this, null, e, false);
            return false;
        }
        return true;
    }

    private boolean setComposeTitle(final Intent intent) {
        final String action = intent.getAction();
        if (INTENT_ACTION_REPLY.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String display_name = UserColorNameUtils.getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name);
            setTitle(getString(R.string.reply_to, display_name));
        } else if (INTENT_ACTION_QUOTE.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String display_name = UserColorNameUtils.getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name);
            setTitle(getString(R.string.quote_user, display_name));
            mSubtitleView.setVisibility(mInReplyToStatus.user_is_protected
                    && mInReplyToStatus.account_id != mInReplyToStatus.user_id ? View.VISIBLE : View.GONE);
        } else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
            if (mDraftItem == null) return false;
            setTitle(R.string.edit_draft);
        } else if (INTENT_ACTION_MENTION.equals(action)) {
            if (mMentionUser == null) return false;
            final String display_name = UserColorNameUtils.getDisplayName(this, mMentionUser.id, mMentionUser.name,
                    mMentionUser.screen_name);
            setTitle(getString(R.string.mention_user, display_name));
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
        final MenuItem itemAttachLocation = menu.findItem(MENU_ADD_LOCATION);
        if (itemAttachLocation != null) {
            final boolean attachLocation = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
            final int menuHighlight = ThemeUtils.getUserAccentColor(this);
            if (attachLocation && getLocation()) {
                itemAttachLocation.setChecked(true);
                MenuUtils.setMenuInfo(itemAttachLocation, new TwidereMenuInfo(true, menuHighlight));
            } else {
                setProgressVisibility(false);
                mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, false).apply();
                itemAttachLocation.setChecked(false);
                MenuUtils.setMenuInfo(itemAttachLocation, new TwidereMenuInfo(false, menuHighlight));
            }
        }
        final MenuItem viewItem = menu.findItem(MENU_VIEW);
        if (viewItem != null) {
            viewItem.setVisible(mInReplyToStatus != null);
        }
        final boolean hasMedia = hasMedia(), hasInReplyTo = mInReplyToStatus != null;

        /*
         * No media & Not reply: [Take photo][Add image][Attach location][Drafts]
         * Has media & Not reply: [Take photo][Media menu][Attach location][Drafts]
         * Is reply: [Media menu][View status][Attach location][Drafts]
         */
        Utils.setMenuItemAvailability(menu, MENU_TAKE_PHOTO, !hasInReplyTo);
        Utils.setMenuItemAvailability(menu, R.id.take_photo_sub_item, hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_ADD_IMAGE, !hasMedia && !hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_VIEW, hasInReplyTo);
        Utils.setMenuItemAvailability(menu, R.id.media_menu, hasMedia || hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_TOGGLE_SENSITIVE, hasMedia);
        Utils.setMenuItemAvailability(menu, MENU_EDIT_MEDIA, hasMedia);

        menu.setGroupEnabled(MENU_GROUP_IMAGE_EXTENSION, hasMedia);
        menu.setGroupVisible(MENU_GROUP_IMAGE_EXTENSION, hasMedia);
        final MenuItem itemToggleSensitive = menu.findItem(MENU_TOGGLE_SENSITIVE);
        if (itemToggleSensitive != null) {
            itemToggleSensitive.setChecked(hasMedia && mIsPossiblySensitive);
        }
        mMenuBar.show();
    }

    private void setProgressVisibility(final boolean visible) {
        mProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean takePhoto() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return false;
        final File cache_dir = getExternalCacheDir();
        final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis());
        mTempPhotoUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
        try {
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (final ActivityNotFoundException e) {
            showErrorMessage(this, null, e, false);
            return false;
        }
        return true;
    }

    private void updateAccountSelection() {
        if (mSendAccountIds == null) return;
        if (mShouldSaveAccounts) {
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(KEY_COMPOSE_ACCOUNTS, TwidereArrayUtils.toString(mSendAccountIds, ',', false));
            editor.apply();
        }
        mSelectAccountAccounts.setSelectedAccounts(mSendAccountIds);
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
            final int text_length = mEditText.length();
            mEditText.setSelection(text_length - (tweetLength - maxLength), text_length);
            return;
        } else if (!hasMedia && (isEmpty(text) || noReplyContent(text))) {
            mEditText.setError(getString(R.string.error_message_no_content));
            return;
        }
        final boolean attach_location = mPreferences.getBoolean(KEY_ATTACH_LOCATION, false);
        if (mRecentLocation == null && attach_location) {
            final Location location;
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                location = null;
            }
            mRecentLocation = location != null ? new ParcelableLocation(location) : null;
        }
        final boolean isQuote = INTENT_ACTION_QUOTE.equals(getIntent().getAction());
        final ParcelableLocation statusLocation = attach_location ? mRecentLocation : null;
        final boolean linkToQuotedTweet = mPreferences.getBoolean(KEY_LINK_TO_QUOTED_TWEET, true);
        final long inReplyToStatusId = !isQuote || linkToQuotedTweet ? mInReplyToStatusId : -1;
        final boolean isPossiblySensitive = hasMedia && mIsPossiblySensitive;
        mTwitterWrapper.updateStatusAsync(mSendAccountIds, text, statusLocation, getMedia(), inReplyToStatusId,
                isPossiblySensitive);
        if (mPreferences.getBoolean(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
                && (mInReplyToStatus == null || mInReplyToStatusId <= 0)) {
            mIsPossiblySensitive = false;
            mShouldSaveAccounts = true;
            mTempPhotoUri = null;
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
        final String textOrig = parseString(mEditText.getText());
        final String text = hasMedia() && textOrig != null ? mImageUploaderUsed ? getImageUploadStatus(this,
                new String[]{FAKE_IMAGE_LINK}, textOrig) : textOrig + " " + FAKE_IMAGE_LINK : textOrig;
        final int validatedCount = text != null ? mValidator.getTweetLength(text) : 0;
        mSendTextCountView.setTextCount(validatedCount);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ListView listView = (ListView) parent;
        final SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();
        mSendAccountIds = new long[listView.getCheckedItemCount()];
        for (int i = 0, j = listView.getCount(), k = 0; i < j; i++) {
            if (checkedPositions.get(i)) {
                mSendAccountIds[k++] = listView.getItemIdAtPosition(i);
            }
        }
        updateAccountSelection();
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

    public static class UnsavedTweetDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Activity activity = getActivity();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    if (activity instanceof ComposeActivity) {
                        ((ComposeActivity) activity).saveToDrafts();
                    }
                    activity.finish();
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    if (activity instanceof ComposeActivity) {
                        new DiscardTweetTask((ComposeActivity) activity).executeTask();
                    } else {
                        activity.finish();
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
            builder.setMessage(R.string.unsaved_status);
            builder.setPositiveButton(R.string.save, this);
            builder.setNegativeButton(R.string.discard, this);
            return builder.create();
        }
    }

    public static class ViewStatusDialogFragment extends BaseSupportDialogFragment {

        private StatusViewHolder mHolder;
        private View mStatusContainer;

        public ViewStatusDialogFragment() {
            setStyle(STYLE_NO_TITLE, 0);
        }

        @Override
        public void onActivityCreated(final Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final Bundle args = getArguments();
            if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
                dismiss();
                return;
            }
            final TwidereApplication application = getApplication();
            final FragmentActivity activity = getActivity();
            final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
            final ImageLoadingHandler handler = new ImageLoadingHandler(R.id.media_preview_progress);
            final AsyncTwitterWrapper twitter = getTwitterWrapper();
            final SharedPreferencesWrapper preferences = SharedPreferencesWrapper.getInstance(activity,
                    SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
            final int profileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            final int mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
            mHolder.displayStatus(activity, loader, handler, twitter, profileImageStyle,
                    mediaPreviewStyle, status, null);
            mStatusContainer.findViewById(R.id.item_menu).setVisibility(View.GONE);
            mStatusContainer.findViewById(R.id.action_buttons).setVisibility(View.GONE);
            mStatusContainer.findViewById(R.id.reply_retweet_status).setVisibility(View.GONE);
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_scrollable_status, parent, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mStatusContainer = view.findViewById(R.id.status_container);
            mHolder = new StatusViewHolder(view);
        }
    }

    private static class AccountSelectorAdapter extends BaseArrayAdapter<ParcelableAccount> {

        public AccountSelectorAdapter(final Context context) {
            super(context, android.R.layout.simple_list_item_multiple_choice);
        }

        @Override
        public long getItemId(int position) {
            return super.getItem(position).account_id;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final ParcelableAccount account = getItem(position);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            text1.setText(Utils.getAccountDisplayName(getContext(), account.account_id, isDisplayNameFirst()));
            return view;
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
        protected Boolean doInBackground(Void... params) {
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

    private static class AddMediaTask extends TwidereAsyncTask<Void, Void, Boolean> {

        private final ComposeActivity activity;
        private final int media_type;
        private final Uri src, dst;
        private final boolean delete_src;

        Uri getSrc() {
            return src;
        }

        AddMediaTask(final ComposeActivity activity, final Uri src, final Uri dst, final int media_type,
                     final boolean delete_src) {
            this.activity = activity;
            this.src = src;
            this.dst = dst;
            this.media_type = media_type;
            this.delete_src = delete_src;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                final ContentResolver resolver = activity.getContentResolver();
                final InputStream is = resolver.openInputStream(src);
                final OutputStream os = resolver.openOutputStream(dst);
                copyStream(is, os);
                os.close();
                if (ContentResolver.SCHEME_FILE.equals(src.getScheme()) && delete_src) {
                    final File file = new File(src.getPath());
                    if (!file.delete()) {
                        Log.d(LOGTAG, String.format("Unable to delete %s", file));
                    }
                }
            } catch (final IOException e) {
                Log.w(LOGTAG, e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            activity.setProgressVisibility(false);
            activity.addMedia(new ParcelableMediaUpdate(dst.toString(), media_type));
            activity.setMenu();
            activity.updateTextCount();
            if (!result) {
                Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            activity.setProgressVisibility(true);
        }
    }

    private static class DeleteImageTask extends TwidereAsyncTask<Void, Void, Boolean> {

        final ComposeActivity mActivity;
        private final ParcelableMediaUpdate[] mMedia;

        DeleteImageTask(final ComposeActivity activity, final ParcelableMediaUpdate... media) {
            this.mActivity = activity;
            this.mMedia = media;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
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
            mActivity.setProgressVisibility(false);
            mActivity.removeAllMedia(Arrays.asList(mMedia));
            mActivity.setMenu();
            if (!result) {
                Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            mActivity.setProgressVisibility(true);
        }
    }

    private static class DiscardTweetTask extends TwidereAsyncTask<Void, Void, Void> {

        final ComposeActivity mActivity;

        DiscardTweetTask(final ComposeActivity activity) {
            this.mActivity = activity;
        }

        @Override
        protected Void doInBackground(final Void... params) {
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
        protected void onPostExecute(final Void result) {
            mActivity.setProgressVisibility(false);
            mActivity.finish();
        }

        @Override
        protected void onPreExecute() {
            mActivity.setProgressVisibility(true);
        }
    }

    private static class MediaPreviewAdapter extends DraggableArrayAdapter<ParcelableMediaUpdate> {

        private final ImageLoaderWrapper mImageLoader;

        public MediaPreviewAdapter(final Context context) {
            super(context, R.layout.grid_item_media_editor);
            mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final ParcelableMediaUpdate media = getItem(position);
            final ImageView image = (ImageView) view.findViewById(R.id.image);
            mImageLoader.displayPreviewImage(media.uri, image);
            return view;
        }

        public List<ParcelableMediaUpdate> getAsList() {
            return Collections.unmodifiableList(getObjects());
        }
    }


}
