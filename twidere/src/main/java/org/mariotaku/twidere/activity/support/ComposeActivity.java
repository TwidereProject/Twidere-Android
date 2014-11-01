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
import android.support.v4.app.DialogFragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.StackView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.twitter.Extractor;

import org.mariotaku.dynamicgridview.DraggableArrayAdapter;
import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.menucomponent.widget.MenuBar.MenuBarListener;
import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.BaseArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import static android.os.Environment.getExternalStorageState;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.ThemeUtils.getActionBarBackground;
import static org.mariotaku.twidere.util.ThemeUtils.getComposeThemeResource;
import static org.mariotaku.twidere.util.ThemeUtils.getWindowContentOverlayForCompose;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getAccountColors;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getCardHighlightColor;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getShareStatus;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showMenuItemToast;

public class ComposeActivity extends BaseSupportDialogActivity implements TextWatcher, LocationListener,
        MenuBarListener, OnClickListener, OnEditorActionListener, OnLongClickListener {

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
    private AsyncTask<Void, Void, ?> mTask;
    private PopupMenu mPopupMenu;
    private TextView mTitleView, mSubtitleView;
    private GridView mMediasPreviewGrid;

    private MenuBar mMenuBar;
    private IColorLabelView mColorIndicator;
    private EditText mEditText;
    private ProgressBar mProgress;
    private StackView mAccountStack;
    private View mSendView, mBottomSendView;
    private StatusTextCountView mSendTextCountView, mBottomSendTextCountView;
    private View mSelectAccount;

    private MediaPreviewAdapter mMediaPreviewAdapter;
    private AccountSelectorAdapter mAccountSelectorAdapter;

    private boolean mIsPossiblySensitive, mShouldSaveAccounts;

    private long[] mAccountIds, mSendAccountIds;

    private Uri mTempPhotoUri;
    private boolean mImageUploaderUsed, mStatusShortenerUsed;
    private ParcelableStatus mInReplyToStatus;

    private ParcelableUser mMentionUser;
    private DraftItem mDraftItem;
    private long mInReplyToStatusId;
    private String mOriginalText;

    private boolean mBottomSendButton;

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
                new DeleteImageTask(this).execute();
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
                            true).execute();
                    mTempPhotoUri = null;
                }
                break;
            }
            case REQUEST_PICK_IMAGE: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri src = intent.getData();
                    mTask = new AddMediaTask(this, src, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false)
                            .execute();
                }
                break;
            }
            case REQUEST_OPEN_DOCUMENT: {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri src = intent.getData();
                    mTask = new AddMediaTask(this, src, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false)
                            .execute();
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
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
        final String option = mPreferences.getString(KEY_COMPOSE_QUIT_ACTION, VALUE_COMPOSE_QUIT_ACTION_ASK);
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final boolean textChanged = text != null && !text.isEmpty() && !text.equals(mOriginalText);
        final boolean isEditingDraft = INTENT_ACTION_EDIT_DRAFT.equals(getIntent().getAction());
        if (VALUE_COMPOSE_QUIT_ACTION_DISCARD.equals(option)) {
            mTask = new DiscardTweetTask(this).execute();
        } else if (textChanged || hasMedia() || isEditingDraft) {
            if (VALUE_COMPOSE_QUIT_ACTION_SAVE.equals(option)) {
                saveToDrafts();
                Toast.makeText(this, R.string.status_saved_to_draft, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                new UnsavedTweetDialogFragment().show(getSupportFragmentManager(), "unsaved_tweet");
            }
        } else {
            mTask = new DiscardTweetTask(this).execute();
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
                Toast.makeText(this, "Select account", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        findViewById(R.id.close).setOnClickListener(this);
        mColorIndicator = (IColorLabelView) findViewById(R.id.accounts_color);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mTitleView = (TextView) findViewById(R.id.actionbar_title);
        mSubtitleView = (TextView) findViewById(R.id.actionbar_subtitle);
        mMediasPreviewGrid = (GridView) findViewById(R.id.medias_thumbnail_preview);
        mMenuBar = (MenuBar) findViewById(R.id.menu_bar);
        mProgress = (ProgressBar) findViewById(R.id.actionbar_progress_indeterminate);
        mAccountStack = (StackView) findViewById(R.id.accounts_stack);
        final View composeActionBar = findViewById(R.id.compose_actionbar);
        final View composeBottomBar = findViewById(R.id.compose_bottombar);
        mSendView = composeActionBar.findViewById(R.id.send);
        mBottomSendView = composeBottomBar.findViewById(R.id.send);
        mSendTextCountView = (StatusTextCountView) mSendView.findViewById(R.id.status_text_count);
        mBottomSendTextCountView = (StatusTextCountView) mBottomSendView.findViewById(R.id.status_text_count);
        mSelectAccount = composeActionBar.findViewById(R.id.select_account);
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

//    @Override
//    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
//        if (isSingleAccount()) return;
//        final boolean selected = !view.isActivated();
//        final Account account = mAccountSelectorAdapter.getItem(position);
//        mAccountSelectorAdapter.setAccountSelected(account.account_id, selected);
//        mSendAccountIds = mAccountSelectorAdapter.getSelectedAccountIds();
//        updateAccountSelection();
//    }
//
//    @Override
//    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
//        final Account account = mAccountSelectorAdapter.getItem(position);
//        final String displayName = getDisplayName(this, account.account_id, account.name, account.screen_name);
//        showMenuItemToast(view, displayName, true);
//        return true;
//    }

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
                showMenuItemToast(v, getString(R.string.send), mBottomSendButton);
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
        outState.putParcelableArrayList(EXTRA_MEDIAS, new ArrayList<Parcelable>(getMediasList()));
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
        updateMediasPreview();
    }

    public void saveToDrafts() {
        final String text = mEditText != null ? ParseUtils.parseString(mEditText.getText()) : null;
        final ParcelableStatusUpdate.Builder builder = new ParcelableStatusUpdate.Builder();
        builder.accounts(Account.getAccounts(this, mSendAccountIds));
        builder.text(text);
        builder.inReplyToStatusId(mInReplyToStatusId);
        builder.location(mRecentLocation);
        builder.isPossiblySensitive(mIsPossiblySensitive);
        if (hasMedia()) {
            builder.medias(getMedias());
        }
        final ContentValues values = ContentValuesCreator.makeStatusDraftContentValues(builder.build());
        mResolver.insert(Drafts.CONTENT_URI, values);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPreferences = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mBottomSendButton = mPreferences.getBoolean(KEY_BOTTOM_SEND_BUTTON, false);
        mTwitterWrapper = getTwidereApplication().getTwitterWrapper();
        mResolver = getContentResolver();
        mValidator = new TwidereValidator(this);
        setContentView(getLayoutInflater().inflate(R.layout.activity_compose, null));
        setProgressBarIndeterminateVisibility(false);
        setFinishOnTouchOutside(false);
        mAccountIds = getAccountIds(this);
        if (mAccountIds.length <= 0) {
            final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
            intent.setClass(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        mMenuBar.setIsBottomBar(true);
        mMenuBar.setMenuBarListener(this);
        mEditText.setOnEditorActionListener(mPreferences.getBoolean(KEY_QUICK_SEND, false) ? this : null);
        mEditText.addTextChangedListener(this);
        mAccountSelectorAdapter = new AccountSelectorAdapter(this);
        mAccountStack.setAdapter(mAccountSelectorAdapter);
        mAccountSelectorAdapter.addAll(Account.getAccountsList(this, false));
        mSelectAccount.setOnClickListener(this);
//        mAccountSelector.setOnItemClickListener(this);
//        mAccountSelector.setOnItemLongClickListener(this);

        mMediaPreviewAdapter = new MediaPreviewAdapter(this);
        mMediasPreviewGrid.setAdapter(mMediaPreviewAdapter);

        final Intent intent = getIntent();

        if (savedInstanceState != null) {
            // Restore from previous saved state
            mSendAccountIds = savedInstanceState.getLongArray(EXTRA_ACCOUNT_IDS);
            mIsPossiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE);
            final ArrayList<ParcelableMediaUpdate> mediasList = savedInstanceState.getParcelableArrayList(EXTRA_MEDIAS);
            if (mediasList != null) {
                addMedias(mediasList);
            }
            mInReplyToStatus = savedInstanceState.getParcelable(EXTRA_STATUS);
            mInReplyToStatusId = savedInstanceState.getLong(EXTRA_STATUS_ID);
            mMentionUser = savedInstanceState.getParcelable(EXTRA_USER);
            mDraftItem = savedInstanceState.getParcelable(EXTRA_DRAFT);
            mShouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS);
            mOriginalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT);
            mTempPhotoUri = savedInstanceState.getParcelable(EXTRA_TEMP_URI);
        } else {
            // The activity was first created
            final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            final long notificationAccount = intent.getLongExtra(EXTRA_NOTIFICATION_ACCOUNT, -1);
            if (notificationId != -1) {
                mTwitterWrapper.clearNotificationAsync(notificationId, notificationAccount);
            }
            if (!handleIntent(intent)) {
                handleDefaultIntent(intent);
            }
            if (mSendAccountIds == null || mSendAccountIds.length == 0) {
                final long[] idsInPrefs = ArrayUtils.parseLongArray(
                        mPreferences.getString(KEY_COMPOSE_ACCOUNTS, null), ',');
                final long[] intersection = ArrayUtils.intersection(idsInPrefs, mAccountIds);
                mSendAccountIds = intersection.length > 0 ? intersection : mAccountIds;
            }
            mOriginalText = ParseUtils.parseString(mEditText.getText());
        }
        if (!setComposeTitle(intent)) {
            setTitle(R.string.compose);
        }

        mMenuBar.inflate(R.menu.menu_compose);
        mSendView.setVisibility(mBottomSendButton ? View.GONE : View.VISIBLE);
        mBottomSendView.setVisibility(mBottomSendButton ? View.VISIBLE : View.GONE);
        mSendView.setOnClickListener(this);
        mBottomSendView.setOnClickListener(this);
        mSendView.setOnLongClickListener(this);
        mBottomSendView.setOnLongClickListener(this);
        final Menu menu = mMenuBar.getMenu();
        final Intent composeExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_COMPOSE);
        addIntentToMenu(this, menu, composeExtensionsIntent, MENU_GROUP_COMPOSE_EXTENSION);
        final Intent imageExtensionsIntent = new Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE);
        final MenuItem mediasMenuItem = menu.findItem(R.id.medias_menu);
        if (mediasMenuItem != null && mediasMenuItem.hasSubMenu()) {
            addIntentToMenu(this, mediasMenuItem.getSubMenu(), imageExtensionsIntent, MENU_GROUP_IMAGE_EXTENSION);
        }
        setMenu();
        updateAccountSelection();
        updateMediasPreview();
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
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
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
        updateMediasPreview();
    }

    private void addMedias(final List<ParcelableMediaUpdate> medias) {
        mMediaPreviewAdapter.addAll(medias);
        updateMediasPreview();
    }

    private void clearMedia() {
        mMediaPreviewAdapter.clear();
        updateMediasPreview();
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
            Crouton.showText(this, R.string.cannot_get_location, CroutonStyle.ALERT);
        }
        return provider != null;
    }

    private ParcelableMediaUpdate[] getMedias() {
        final List<ParcelableMediaUpdate> list = getMediasList();
        return list.toArray(new ParcelableMediaUpdate[list.size()]);
    }

    private List<ParcelableMediaUpdate> getMediasList() {
        return mMediaPreviewAdapter.getAsList();
    }

    private boolean handleDefaultIntent(final Intent intent) {
        if (intent == null) return false;
        final String action = intent.getAction();
        mShouldSaveAccounts = !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action);
        final Uri data = intent.getData();
        final CharSequence extraSubject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT);
        final CharSequence extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        final Uri extraStream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        //TODO handle share_screenshot extra (Bitmap)
        if (extraStream != null) {
            new AddMediaTask(this, extraStream, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false).execute();
        } else if (data != null) {
            new AddMediaTask(this, data, createTempImageUri(), ParcelableMedia.TYPE_IMAGE, false).execute();
        } else if (intent.hasExtra(EXTRA_SHARE_SCREENSHOT)) {
            final Bitmap bitmap = intent.getParcelableExtra(EXTRA_SHARE_SCREENSHOT);
            if (bitmap != null) {
                try {
                    new AddBitmapTask(this, bitmap, createTempImageUri(), ParcelableMedia.TYPE_IMAGE).execute();
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
        if (draft.medias != null) {
            addMedias(Arrays.asList(draft.medias));
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

    private void setCommonMenu(final Menu menu) {
        final boolean hasMedia = hasMedia();
        // final MenuItem itemAddImageSubmenu =
        // menu.findItem(R.id.add_image_submenu);
        // if (itemAddImageSubmenu != null) {
        // final Drawable iconAddImage = itemAddImageSubmenu.getIcon();
        // iconAddImage.mutate();
        // if (hasMedia) {
        // iconAddImage.setColorFilter(activatedColor, Mode.SRC_ATOP);
        // } else {
        // iconAddImage.clearColorFilter();
        // }
        // }

    }

    private boolean setComposeTitle(final Intent intent) {
        final String action = intent.getAction();
        if (INTENT_ACTION_REPLY.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String display_name = getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name);
            setTitle(getString(R.string.reply_to, display_name));
        } else if (INTENT_ACTION_QUOTE.equals(action)) {
            if (mInReplyToStatus == null) return false;
            final String display_name = getDisplayName(this, mInReplyToStatus.user_id, mInReplyToStatus.user_name,
                    mInReplyToStatus.user_screen_name);
            setTitle(getString(R.string.quote_user, display_name));
            mSubtitleView.setVisibility(mInReplyToStatus.user_is_protected
                    && mInReplyToStatus.account_id != mInReplyToStatus.user_id ? View.VISIBLE : View.GONE);
        } else if (INTENT_ACTION_EDIT_DRAFT.equals(action)) {
            if (mDraftItem == null) return false;
            setTitle(R.string.edit_draft);
        } else if (INTENT_ACTION_MENTION.equals(action)) {
            if (mMentionUser == null) return false;
            final String display_name = getDisplayName(this, mMentionUser.id, mMentionUser.name,
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
            if (attachLocation && getLocation()) {
                itemAttachLocation.setChecked(true);
            } else {
                setProgressVisibility(false);
                mPreferences.edit().putBoolean(KEY_ATTACH_LOCATION, false).apply();
                itemAttachLocation.setChecked(false);
            }
        }
        final MenuItem viewItem = menu.findItem(MENU_VIEW);
        if (viewItem != null) {
            viewItem.setVisible(mInReplyToStatus != null);
        }
        final boolean hasMedia = hasMedia(), hasInReplyTo = mInReplyToStatus != null;

        /*
         * No media & Not reply: [Take photo][Add image][Attach location][Drafts]
         * Has media & Not reply: [Take photo][Medias menu][Attach location][Drafts]
         * Is reply: [Medias menu][View status][Attach location][Drafts]
         */
        Utils.setMenuItemAvailability(menu, MENU_TAKE_PHOTO, !hasInReplyTo);
        Utils.setMenuItemAvailability(menu, R.id.take_photo_sub_item, hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_ADD_IMAGE, !hasMedia && !hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_VIEW, hasInReplyTo);
        Utils.setMenuItemAvailability(menu, R.id.medias_menu, hasMedia || hasInReplyTo);
        Utils.setMenuItemAvailability(menu, MENU_TOGGLE_SENSITIVE, hasMedia);
        Utils.setMenuItemAvailability(menu, MENU_EDIT_MEDIAS, hasMedia);

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
            editor.putString(KEY_COMPOSE_ACCOUNTS, ArrayUtils.toString(mSendAccountIds, ',', false));
            editor.apply();
        }
        mAccountSelectorAdapter.clearAccountSelection();
        for (final long accountId : mSendAccountIds) {
            mAccountSelectorAdapter.setAccountSelected(accountId, true);
        }
        mColorIndicator.drawEnd(getAccountColors(this, mSendAccountIds));
    }

    private void updateMediasPreview() {
        final int count = mMediaPreviewAdapter.getCount();
        final Resources res = getResources();
        final int maxColumns = res.getInteger(R.integer.grid_column_image_preview);
        mMediasPreviewGrid.setNumColumns(MathUtils.clamp(count, maxColumns, 1));
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
        mTwitterWrapper.updateStatusAsync(mSendAccountIds, text, statusLocation, getMedias(), inReplyToStatusId,
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
        final StatusTextCountView textCountView = mBottomSendButton ? mBottomSendTextCountView : mSendTextCountView;
        if (textCountView != null && mEditText != null) {
            final String textOrig = parseString(mEditText.getText());
            final String text = hasMedia() && textOrig != null ? mImageUploaderUsed ? getImageUploadStatus(this,
                    new String[]{FAKE_IMAGE_LINK}, textOrig) : textOrig + " " + FAKE_IMAGE_LINK : textOrig;
            final int validatedCount = text != null ? mValidator.getTweetLength(text) : 0;
            textCountView.setTextCount(validatedCount);
        }
    }

    @Override
    public void onPreShowMenu(Menu menu) {

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
                        new DiscardTweetTask((ComposeActivity) activity).execute();
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
            final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
            final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
            mHolder.setShowAsGap(false);
            mHolder.setAccountColorEnabled(true);
            mHolder.setTextSize(prefs.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity())));
            ((View) mHolder.content).setPadding(0, 0, 0, 0);
            mHolder.content.setItemBackground(null);
            mHolder.content.setItemSelector(null);
            mHolder.text.setText(status.text_unescaped);
            mHolder.name.setText(status.user_name);
            mHolder.screen_name.setText("@" + status.user_screen_name);
            mHolder.screen_name.setVisibility(View.VISIBLE);

            final String retweeted_by_name = status.retweeted_by_name;
            final String retweeted_by_screen_name = status.retweeted_by_screen_name;

            final boolean is_my_status = status.account_id == status.user_id;
            final boolean hasMedia = status.medias != null && status.medias.length > 0;
            mHolder.setUserColor(getUserColor(getActivity(), status.user_id, true));
            mHolder.setHighlightColor(getCardHighlightColor(false, status.is_favorite, status.is_retweet));

            mHolder.setIsMyStatus(is_my_status && !prefs.getBoolean(KEY_INDICATE_MY_STATUS, true));

            mHolder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    getUserTypeIconRes(status.user_is_verified, status.user_is_protected), 0);
            mHolder.time.setTime(status.timestamp);
            final int type_icon = getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location), hasMedia,
                    status.is_possibly_sensitive);
            mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, type_icon, 0);
            mHolder.reply_retweet_status
                    .setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
            if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name)
                    && !TextUtils.isEmpty(retweeted_by_screen_name)) {
                if (!prefs.getBoolean(KEY_NAME_FIRST, true)) {
                    mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
                            R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
                            : getString(R.string.retweeted_by, retweeted_by_screen_name));
                } else {
                    mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
                            R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
                            R.string.retweeted_by, retweeted_by_name));
                }
                mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
                        R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
                        R.string.retweeted_by, retweeted_by_name));
                mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet,
                        0, 0, 0);
            } else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
                mHolder.reply_retweet_status.setText(getString(R.string.in_reply_to, status.in_reply_to_screen_name));
                mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_indicator_conversation, 0, 0, 0);
            }
            if (prefs.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
                loader.displayProfileImage(mHolder.my_profile_image, status.user_profile_image_url);
                loader.displayProfileImage(mHolder.profile_image, status.user_profile_image_url);
            } else {
                mHolder.profile_image.setVisibility(View.GONE);
                mHolder.my_profile_image.setVisibility(View.GONE);
            }
            mHolder.image_preview_container.setVisibility(View.GONE);
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
            final ScrollView view = (ScrollView) inflater.inflate(R.layout.dialog_scrollable_status, parent, false);
            mHolder = new StatusViewHolder(view.getChildAt(0));
            return view;
        }

    }

    private static class AccountSelectorAdapter extends BaseArrayAdapter<Account> {

        private final LongSparseArray<Boolean> mAccountSelectStates = new LongSparseArray<>();


        public AccountSelectorAdapter(final Context context) {
            super(context, R.layout.adapter_item_compose_account);
        }

        public void clearAccountSelection() {
            mAccountSelectStates.clear();
            notifyDataSetChanged();
        }

        public void setAccountSelected(final long accountId, final boolean selected) {
            mAccountSelectStates.put(accountId, selected);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final Account account = getItem(position);
            final ImageLoaderWrapper loader = getImageLoader();
            loader.displayProfileImage(icon, account.profile_image_url);
            return view;
        }


    }


    private static class AccountAvatarHolder extends ViewHolder {

        private final ImageView icon;

        public AccountAvatarHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(android.R.id.icon);
        }

        public void setAccount(ImageLoaderWrapper loader, Account account, LongSparseArray<Boolean> states) {
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

    private static class AddMediaTask extends AsyncTask<Void, Void, Boolean> {

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
                Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
            }
        }

        @Override
        protected void onPreExecute() {
            activity.setProgressVisibility(true);
        }
    }

    private static class DeleteImageTask extends AsyncTask<Void, Void, Boolean> {

        final ComposeActivity activity;
        private final ParcelableMediaUpdate[] medias;

        DeleteImageTask(final ComposeActivity activity, final ParcelableMediaUpdate... medias) {
            this.activity = activity;
            this.medias = medias;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            if (medias == null) return false;
            try {
                for (final ParcelableMediaUpdate media : medias) {
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
            activity.setProgressVisibility(false);
            activity.removeAllMedia(Arrays.asList(medias));
            activity.setMenu();
            if (!result) {
                Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
            }
        }

        @Override
        protected void onPreExecute() {
            activity.setProgressVisibility(true);
        }
    }

    private static class DiscardTweetTask extends AsyncTask<Void, Void, Void> {

        final ComposeActivity activity;

        DiscardTweetTask(final ComposeActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            for (final ParcelableMediaUpdate media : activity.getMediasList()) {
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
            activity.setProgressVisibility(false);
            activity.finish();
        }

        @Override
        protected void onPreExecute() {
            activity.setProgressVisibility(true);
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
            mImageLoader.displayPreviewImage(image, media.uri);
            return view;
        }

    }
}
