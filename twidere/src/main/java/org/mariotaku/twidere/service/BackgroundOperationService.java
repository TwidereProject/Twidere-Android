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

package org.mariotaku.twidere.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.twitter.Extractor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.activity.MainHondaJOJOActivity;
import org.mariotaku.microblog.library.fanfou.model.PhotoStatusUpdate;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.TwitterUpload;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.ErrorInfo;
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse;
import org.mariotaku.microblog.library.twitter.model.NewMediaMetadata;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.StatusUpdate;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.DraftCursorIndices;
import org.mariotaku.twidere.model.DraftValuesCreator;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtra;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableDirectMessageUtils;
import org.mariotaku.twidere.model.util.ParcelableLocationUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUpdateUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.util.AbsServiceInterface;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.BitmapUtils;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.MediaUploaderInterface;
import org.mariotaku.twidere.util.NotificationManagerWrapper;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.StatusShortenerInterface;
import org.mariotaku.twidere.util.TwidereListUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.TimelineType;
import edu.tsinghua.hotmobi.model.TweetEvent;

import static org.mariotaku.twidere.util.ContentValuesCreator.createMessageDraft;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;

public class BackgroundOperationService extends IntentService implements Constants {


    private Handler mHandler;
    @Inject
    SharedPreferencesWrapper mPreferences;
    @Inject
    AsyncTwitterWrapper mTwitter;
    @Inject
    NotificationManagerWrapper mNotificationManager;
    @Inject
    TwidereValidator mValidator;
    @Inject
    Extractor mExtractor;


    public BackgroundOperationService() {
        super("background_operation");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        GeneralComponentHelper.build(this).inject(this);
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void showErrorMessage(final CharSequence message, final boolean longMessage) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Utils.showErrorMessage(BackgroundOperationService.this, message, longMessage);
            }
        });
    }

    public void showErrorMessage(final int actionRes, final Exception e, final boolean longMessage) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Utils.showErrorMessage(BackgroundOperationService.this, actionRes, e, longMessage);
            }
        });
    }

    public void showErrorMessage(final int actionRes, final String message, final boolean longMessage) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Utils.showErrorMessage(BackgroundOperationService.this, actionRes, message, longMessage);
            }
        });
    }

    public void showOkMessage(final int messageRes, final boolean longMessage) {
        showToast(getString(messageRes), longMessage);
    }

    private void showToast(final CharSequence message, final boolean longMessage) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BackgroundOperationService.this, message, longMessage ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case INTENT_ACTION_UPDATE_STATUS:
                handleUpdateStatusIntent(intent);
                break;
            case INTENT_ACTION_SEND_DIRECT_MESSAGE:
                handleSendDirectMessageIntent(intent);
                break;
            case INTENT_ACTION_DISCARD_DRAFT:
                handleDiscardDraftIntent(intent);
                break;
            case INTENT_ACTION_SEND_DRAFT: {
                handleSendDraftIntent(intent);
            }
        }
    }

    private void handleSendDraftIntent(Intent intent) {
        final Uri uri = intent.getData();
        if (uri == null) return;
        mNotificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS);
        final long def = -1;
        final long draftId = NumberUtils.toLong(uri.getLastPathSegment(), def);
        if (draftId == -1) return;
        final Expression where = Expression.equals(Drafts._ID, draftId);
        final ContentResolver cr = getContentResolver();
        final Cursor c = cr.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where.getSQL(), null, null);
        if (c == null) return;
        final DraftCursorIndices i = new DraftCursorIndices(c);
        final Draft item;
        try {
            if (!c.moveToFirst()) return;
            item = i.newObject(c);
        } finally {
            c.close();
        }
        cr.delete(Drafts.CONTENT_URI, where.getSQL(), null);
        if (TextUtils.isEmpty(item.action_type)) {
            item.action_type = Draft.Action.UPDATE_STATUS;
        }
        switch (item.action_type) {
            case Draft.Action.UPDATE_STATUS_COMPAT_1:
            case Draft.Action.UPDATE_STATUS_COMPAT_2:
            case Draft.Action.UPDATE_STATUS:
            case Draft.Action.REPLY:
            case Draft.Action.QUOTE: {
                updateStatuses(item.action_type, ParcelableStatusUpdateUtils.fromDraftItem(this, item));
                break;
            }
            case Draft.Action.SEND_DIRECT_MESSAGE_COMPAT:
            case Draft.Action.SEND_DIRECT_MESSAGE: {
                String recipientId = null;
                if (item.action_extras instanceof SendDirectMessageActionExtra) {
                    recipientId = ((SendDirectMessageActionExtra) item.action_extras).getRecipientId();
                }
                if (ArrayUtils.isEmpty(item.account_ids) || recipientId == null) {
                    return;
                }
                final UserKey accountKey = item.account_ids[0];
                final String imageUri = ArrayUtils.isEmpty(item.media) ? null : item.media[0].uri;
                sendMessage(accountKey, recipientId, item.text, imageUri);
                break;
            }
        }
    }

    private void handleDiscardDraftIntent(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) return;
        mNotificationManager.cancel(data.toString(), NOTIFICATION_ID_DRAFTS);
        final ContentResolver cr = getContentResolver();
        final long def = -1;
        final long id = NumberUtils.toLong(data.getLastPathSegment(), def);
        final Expression where = Expression.equals(Drafts._ID, id);
        cr.delete(Drafts.CONTENT_URI, where.getSQL(), null);
    }

    private void handleSendDirectMessageIntent(final Intent intent) {
        final UserKey accountId = intent.getParcelableExtra(EXTRA_ACCOUNT_KEY);
        final String recipientId = intent.getStringExtra(EXTRA_RECIPIENT_ID);
        final String text = intent.getStringExtra(EXTRA_TEXT);
        final String imageUri = intent.getStringExtra(EXTRA_IMAGE_URI);
        if (accountId == null || recipientId == null || text == null) return;
        sendMessage(accountId, recipientId, text, imageUri);
    }

    private void sendMessage(@NonNull UserKey accountId, @NonNull String recipientId,
                             @NonNull String text, @Nullable String imageUri) {
        final String title = getString(R.string.sending_direct_message);
        final Builder builder = new Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, 0, true);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
        builder.setOngoing(true);
        final Notification notification = builder.build();
        startForeground(NOTIFICATION_ID_SEND_DIRECT_MESSAGE, notification);
        final SingleResponse<ParcelableDirectMessage> result = sendDirectMessage(builder, accountId,
                recipientId, text, imageUri);

        final ContentResolver resolver = getContentResolver();
        if (result.hasData()) {
            final ParcelableDirectMessage message = result.getData();
            final ContentValues values = ContentValuesCreator.createDirectMessage(message);
            final String deleteWhere = Expression.and(Expression.equalsArgs(DirectMessages.ACCOUNT_KEY),
                    Expression.equalsArgs(DirectMessages.MESSAGE_ID)).getSQL();
            String[] deleteWhereArgs = {message.account_key.toString(), message.id};
            resolver.delete(DirectMessages.Outbox.CONTENT_URI, deleteWhere, deleteWhereArgs);
            resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
            showOkMessage(R.string.direct_message_sent, false);
        } else {
            final ContentValues values = createMessageDraft(accountId, recipientId, text, imageUri);
            resolver.insert(Drafts.CONTENT_URI, values);
            showErrorMessage(R.string.action_sending_direct_message, result.getException(), true);
        }
        stopForeground(false);
        mNotificationManager.cancel(NOTIFICATION_ID_SEND_DIRECT_MESSAGE);
    }

    private void handleUpdateStatusIntent(final Intent intent) {
        final ParcelableStatusUpdate status = intent.getParcelableExtra(EXTRA_STATUS);
        final Parcelable[] status_parcelables = intent.getParcelableArrayExtra(EXTRA_STATUSES);
        final ParcelableStatusUpdate[] statuses;
        if (status_parcelables != null) {
            statuses = new ParcelableStatusUpdate[status_parcelables.length];
            for (int i = 0, j = status_parcelables.length; i < j; i++) {
                statuses[i] = (ParcelableStatusUpdate) status_parcelables[i];
            }
        } else if (status != null) {
            statuses = new ParcelableStatusUpdate[1];
            statuses[0] = status;
        } else
            return;
        @Draft.Action
        final String actionType = intent.getStringExtra(EXTRA_ACTION);
        updateStatuses(actionType, statuses);
    }

    private void updateStatuses(@Draft.Action final String actionType, final ParcelableStatusUpdate... statuses) {
        final Builder builder = new Builder(this);
        startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(this, builder, 0, null));
        for (final ParcelableStatusUpdate item : statuses) {
            mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS,
                    updateUpdateStatusNotification(this, builder, 0, item));
            final Draft draft = new Draft();
            draft.account_ids = ParcelableAccountUtils.getAccountKeys(item.accounts);
            if (actionType != null) {
                draft.action_type = actionType;
            } else {
                draft.action_type = Draft.Action.UPDATE_STATUS;
            }
            draft.text = item.text;
            draft.location = item.location;
            draft.media = item.media;
            final UpdateStatusActionExtra extra = new UpdateStatusActionExtra();
            extra.setInReplyToStatus(item.in_reply_to_status);
            extra.setIsPossiblySensitive(item.is_possibly_sensitive);
            extra.setRepostStatusId(item.repost_status_id);
            extra.setDisplayCoordinates(item.display_coordinates);
            draft.action_extras = extra;
            final ContentResolver resolver = getContentResolver();
            final Uri draftUri = resolver.insert(Drafts.CONTENT_URI, DraftValuesCreator.create(draft));
            final long def = -1;
            final long draftId = draftUri != null ? NumberUtils.toLong(draftUri.getLastPathSegment(), def) : -1;
            mTwitter.addSendingDraftId(draftId);
            final List<SingleResponse<ParcelableStatus>> result = updateStatus(builder, item);
            boolean failed = false;
            Exception exception = null;
            final Expression where = Expression.equals(Drafts._ID, draftId);
            final List<UserKey> failedAccountIds = new ArrayList<>();
            Collections.addAll(failedAccountIds, ParcelableAccountUtils.getAccountKeys(item.accounts));

            for (final SingleResponse<ParcelableStatus> response : result) {
                final ParcelableStatus data = response.getData();
                if (data == null) {
                    failed = true;
                    if (exception == null) {
                        exception = response.getException();
                    }
                } else {
                    failedAccountIds.remove(data.account_key);
                    // BEGIN HotMobi
                    final TweetEvent event = TweetEvent.create(this, data, TimelineType.OTHER);
                    event.setAction(TweetEvent.Action.TWEET);
                    HotMobiLogger.getInstance(this).log(data.account_key, event);
                    // END HotMobi
                }
            }

            if (result.isEmpty()) {
                showErrorMessage(R.string.action_updating_status, getString(R.string.no_account_selected), false);
            } else if (failed) {
                // If the status is a duplicate, there's no need to save it to
                // drafts.
                if (exception instanceof MicroBlogException
                        && ((MicroBlogException) exception).getErrorCode() == ErrorInfo.STATUS_IS_DUPLICATE) {
                    showErrorMessage(getString(R.string.status_is_duplicate), false);
                } else {
                    final ContentValues accountIdsValues = new ContentValues();
                    accountIdsValues.put(Drafts.ACCOUNT_IDS, TwidereListUtils.toString(failedAccountIds, ',', false));
                    resolver.update(Drafts.CONTENT_URI, accountIdsValues, where.getSQL(), null);
                    showErrorMessage(R.string.action_updating_status, exception, true);
                    final ContentValues notifValues = new ContentValues();
                    notifValues.put(BaseColumns._ID, draftId);
                    resolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, notifValues);
                }
            } else {
                showOkMessage(R.string.status_updated, false);
                resolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
                if (item.media != null) {
                    for (final ParcelableMediaUpdate media : item.media) {
                        final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                        if (path != null && !new File(path).delete()) {
                            Log.d(LOGTAG, String.format("unable to delete %s", path));
                        }
                    }
                }
            }
            mTwitter.removeSendingDraftId(draftId);
            if (mPreferences.getBoolean(KEY_REFRESH_AFTER_TWEET, false)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTwitter.refreshAll();
                    }
                });
            }
        }
        stopForeground(false);
        mNotificationManager.cancel(NOTIFICATION_ID_UPDATE_STATUS);
    }


    private SingleResponse<ParcelableDirectMessage> sendDirectMessage(final NotificationCompat.Builder builder,
                                                                      final UserKey accountKey,
                                                                      final String recipientId,
                                                                      final String text,
                                                                      final String imageUri) {
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(this,
                accountKey);
        if (credentials == null) return SingleResponse.getInstance();
        final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(this, credentials, true, true);
        final TwitterUpload twitterUpload = MicroBlogAPIFactory.getTwitterInstance(this, credentials,
                true, true, TwitterUpload.class);
        if (twitter == null || twitterUpload == null) return SingleResponse.getInstance();
        try {
            final ParcelableDirectMessage directMessage;
            switch (ParcelableAccountUtils.getAccountType(credentials)) {
                case ParcelableAccount.Type.FANFOU: {
                    if (imageUri != null) {
                        throw new MicroBlogException("Can't send image DM on Fanfou");
                    }
                    final DirectMessage dm = twitter.sendFanfouDirectMessage(recipientId, text);
                    directMessage = ParcelableDirectMessageUtils.fromDirectMessage(dm, accountKey, true);
                    break;
                }
                default: {
                    if (imageUri != null) {
                        final String path = getImagePathFromUri(this, Uri.parse(imageUri));
                        if (path == null) throw new FileNotFoundException();
                        final BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, o);
                        final File file = new File(path);
                        BitmapUtils.downscaleImageIfNeeded(file, 100);
                        ContentLengthInputStream is = null;
                        FileBody body = null;
                        try {
                            is = new ContentLengthInputStream(file);
                            is.setReadListener(new MessageMediaUploadListener(this, mNotificationManager,
                                    builder, text));
                            body = new FileBody(is, file.getName(), file.length(),
                                    ContentType.parse(o.outMimeType));
                            final MediaUploadResponse uploadResp = twitterUpload.uploadMedia(body);
                            final DirectMessage response = twitter.sendDirectMessage(recipientId,
                                    text, uploadResp.getId());
                            directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                    accountKey, true);
                        } finally {
                            Utils.closeSilently(is);
                            Utils.closeSilently(body);
                        }
                        if (!file.delete()) {
                            Log.d(LOGTAG, String.format("unable to delete %s", path));
                        }
                    } else {
                        final DirectMessage response = twitter.sendDirectMessage(recipientId, text);
                        directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                accountKey, true);
                    }
                    break;
                }
            }
            Utils.setLastSeen(this, new UserKey(recipientId, accountKey.getHost()),
                    System.currentTimeMillis());

            return SingleResponse.getInstance(directMessage);
        } catch (final IOException e) {
            return SingleResponse.getInstance(e);
        } catch (final MicroBlogException e) {
            return SingleResponse.getInstance(e);
        }
    }

    private void showToast(final int resId, final int duration) {
        mHandler.post(new ToastRunnable(this, resId, duration));
    }

    private List<SingleResponse<ParcelableStatus>> updateStatus(final Builder builder,
                                                                final ParcelableStatusUpdate statusUpdate) {
        final ArrayList<ContentValues> hashTagValues = new ArrayList<>();
        final Collection<String> hashTags = mExtractor.extractHashtags(statusUpdate.text);
        for (final String hashTag : hashTags) {
            final ContentValues values = new ContentValues();
            values.put(CachedHashtags.NAME, hashTag);
            hashTagValues.add(values);
        }
        final boolean hasEasterEggTriggerText = statusUpdate.text.contains(EASTER_EGG_TRIGGER_TEXT);
        final boolean hasEasterEggRestoreText = statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART1)
                && statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART2)
                && statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART3);
        boolean mentionedHondaJOJO = false, notReplyToOther = false;
        final ContentResolver resolver = getContentResolver();
        resolver.bulkInsert(CachedHashtags.CONTENT_URI,
                hashTagValues.toArray(new ContentValues[hashTagValues.size()]));

        final List<SingleResponse<ParcelableStatus>> results = new ArrayList<>();

        if (statusUpdate.accounts.length == 0) return Collections.emptyList();

        try {
            final TwidereApplication app = TwidereApplication.getInstance(this);
            final String uploaderComponent = mPreferences.getString(KEY_MEDIA_UPLOADER, null);
            final String shortenerComponent = mPreferences.getString(KEY_STATUS_SHORTENER, null);

            // Try find uploader and shortener, show error if set but not found
            MediaUploaderInterface uploader = null;
            StatusShortenerInterface shortener = null;
            if (!ServicePickerPreference.isNoneValue(uploaderComponent)) {
                uploader = MediaUploaderInterface.getInstance(app, uploaderComponent);
                if (uploader == null) {
                    throw new UploaderNotFoundException(getString(R.string.error_message_media_uploader_not_found));
                }
                try {
                    uploader.checkService(new AbsServiceInterface.CheckServiceAction() {
                        @Override
                        public void check(@Nullable Bundle metaData) throws AbsServiceInterface.CheckServiceException {
                            if (metaData == null) throw new ExtensionVersionMismatchException();
                            final String extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_MEDIA_UPLOADER);
                            if (!TextUtils.equals(extensionVersion, getString(R.string.media_uploader_service_interface_version))) {
                                throw new ExtensionVersionMismatchException();
                            }
                        }
                    });
                } catch (AbsServiceInterface.CheckServiceException e) {
                    if (e instanceof ExtensionVersionMismatchException) {
                        throw new UploadException(getString(R.string.uploader_version_incompatible));
                    }
                    throw new UploadException(e);
                }
            }
            if (!ServicePickerPreference.isNoneValue(shortenerComponent)) {
                shortener = StatusShortenerInterface.getInstance(app, shortenerComponent);
                if (shortener == null) throw new ShortenerNotFoundException(this);
                try {
                    shortener.checkService(new AbsServiceInterface.CheckServiceAction() {
                        @Override
                        public void check(@Nullable Bundle metaData) throws AbsServiceInterface.CheckServiceException {
                            if (metaData == null) throw new ExtensionVersionMismatchException();
                            final String extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER);
                            if (!TextUtils.equals(extensionVersion, getString(R.string.status_shortener_service_interface_version))) {
                                throw new ExtensionVersionMismatchException();
                            }
                        }
                    });
                } catch (AbsServiceInterface.CheckServiceException e) {
                    if (e instanceof ExtensionVersionMismatchException) {
                        throw new ShortenException(getString(R.string.shortener_version_incompatible));
                    }
                    throw new ShortenException(e);
                }
            }

            final boolean hasMedia = statusUpdate.media != null && statusUpdate.media.length > 0;

            // Uploader handles media scaling, if no uploader present, we will handle them instead.
            if (uploader == null && statusUpdate.media != null) {
                for (final ParcelableMediaUpdate media : statusUpdate.media) {
                    final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                    final File file = path != null ? new File(path) : null;
                    if (file != null && file.exists()) {
                        BitmapUtils.downscaleImageIfNeeded(file, 95);
                    }
                }
            }
            try {
                if (uploader != null && hasMedia) {
                    // Wait for uploader service binding
                    uploader.waitForService();
                }
                if (shortener != null) {
                    // Wait for shortener service binding
                    shortener.waitForService();
                }
                for (final ParcelableAccount account : statusUpdate.accounts) {
                    final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(this,
                            account.account_key);
                    // Get Twitter instance corresponding to account
                    final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(this,
                            account.account_key, true, true);

                    // Shouldn't happen
                    if (twitter == null || credentials == null) {
                        throw new UpdateStatusException("No account found");
                    }

                    String statusText = statusUpdate.text;

                    // Use custom uploader to upload media
                    MediaUploadResult uploadResult = null;
                    if (uploader != null && hasMedia) {
                        try {
                            uploadResult = uploader.upload(statusUpdate,
                                    UploaderMediaItem.getFromStatusUpdate(this, statusUpdate));
                        } catch (final Exception e) {
                            throw new UploadException(getString(R.string.error_message_media_upload_failed));
                        }
                        // Shouldn't return null, but handle that case for shitty extensions.
                        if (uploadResult == null) {
                            throw new UploadException(getString(R.string.error_message_media_upload_failed));
                        }
                        if (uploadResult.error_code != 0)
                            throw new UploadException(uploadResult.error_message);

                        // Replace status text to uploaded
                        statusText = getImageUploadStatus(this, uploadResult.media_uris,
                                statusText);
                    }

                    final boolean shouldShorten = mValidator.getTweetLength(statusText) >
                            TwidereValidator.getTextLimit(credentials);
                    StatusShortenResult shortenedResult = null;
                    if (shouldShorten && shortener != null) {
                        try {
                            shortenedResult = shortener.shorten(statusUpdate, account.account_key,
                                    statusText);
                        } catch (final Exception e) {
                            throw new ShortenException(getString(R.string.error_message_tweet_shorten_failed), e);
                        }
                        // Shouldn't return null, but handle that case for shitty extensions.
                        if (shortenedResult == null)
                            throw new ShortenException(getString(R.string.error_message_tweet_shorten_failed));
                        if (shortenedResult.error_code != 0)
                            throw new ShortenException(shortenedResult.error_message);
                        if (shortenedResult.shortened == null)
                            throw new ShortenException(getString(R.string.error_message_tweet_shorten_failed));
                        statusText = shortenedResult.shortened;
                    }

                    final StatusUpdate status = new StatusUpdate(statusText);
                    if (statusUpdate.in_reply_to_status != null) {
                        status.inReplyToStatusId(statusUpdate.in_reply_to_status.id);
                    }
                    if (statusUpdate.repost_status_id != null) {
                        status.setRepostStatusId(statusUpdate.repost_status_id);
                    }
                    if (statusUpdate.location != null) {
                        status.location(ParcelableLocationUtils.toGeoLocation(statusUpdate.location));
                        status.displayCoordinates(statusUpdate.display_coordinates);
                    }
                    if (uploader == null && hasMedia) {
                        if (uploadOnSocialPlatform(resolver, builder, shortener, uploader,
                                credentials, twitter, statusUpdate, status, statusText,
                                shouldShorten, shortenedResult, uploadResult, results)) {
                            continue;
                        }
                    }
                    status.possiblySensitive(statusUpdate.is_possibly_sensitive);

                    try {
                        final Status resultStatus = twitter.updateStatus(status);
                        final ParcelableStatus result = ParcelableStatusUtils.fromStatus(resultStatus,
                                account.account_key, false);
                        if (!mentionedHondaJOJO) {
                            final ParcelableUserMention[] mentions = result.mentions;
                            if (ArrayUtils.isEmpty(mentions)) {
                                mentionedHondaJOJO = statusUpdate.text.contains("@" + HONDAJOJO_SCREEN_NAME);
                            } else if (mentions.length == 1 && mentions[0].key.equals(HONDAJOJO_ID)) {
                                mentionedHondaJOJO = true;
                            }
                            Utils.setLastSeen(this, mentions, System.currentTimeMillis());
                        }
                        if (!notReplyToOther) {
                            final String inReplyToUserId = resultStatus.getInReplyToUserId();
                            if (inReplyToUserId == null || HONDAJOJO_ID.check(inReplyToUserId, null)) {
                                notReplyToOther = true;
                            }
                        }
                        if (shouldShorten && shortener != null && shortenedResult != null) {
                            shortener.callback(shortenedResult, result);
                        }
                        if (uploader != null && uploadResult != null) {
                            uploader.callback(uploadResult, result);
                        }
                        results.add(SingleResponse.getInstance(result));
                    } catch (final MicroBlogException e) {
                        Log.w(LOGTAG, e);
                        final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
                        results.add(response);
                    }
                }
            } finally {
                // Unbind uploader and shortener
                if (uploader != null) {
                    uploader.unbindService();
                }
                if (shortener != null) {
                    shortener.unbindService();
                }
            }
        } catch (final UpdateStatusException e) {
            Log.w(LOGTAG, e);
            final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
            results.add(response);
        }
        if (mentionedHondaJOJO) {
            triggerEasterEgg(notReplyToOther, hasEasterEggTriggerText, hasEasterEggRestoreText);
        }
        return results;
    }

    private boolean uploadOnSocialPlatform(ContentResolver resolver, Builder builder,
                                           StatusShortenerInterface shortener,
                                           MediaUploaderInterface uploader,
                                           ParcelableCredentials credentials, MicroBlog twitter,
                                           ParcelableStatusUpdate statusUpdate,
                                           StatusUpdate status, String statusText,
                                           boolean shouldShorten, StatusShortenResult shortenedResult,
                                           MediaUploadResult uploadResult,
                                           List<SingleResponse<ParcelableStatus>> results) throws UpdateStatusException {
        try {
            if (ParcelableAccount.Type.FANFOU.equals(credentials.account_type)) {
                if (statusUpdate.media.length > 1) {
                    throw new UpdateStatusException(getString(R.string.error_too_many_photos_fanfou));
                }
                ParcelableMediaUpdate media = statusUpdate.media[0];
                FileBody body = null;
                try {
                    body = getBodyFromMedia(resolver, builder, media, statusUpdate);
                    final PhotoStatusUpdate update = new PhotoStatusUpdate(body, statusText);
                    if (statusUpdate.location != null) {
                        update.setLocation(statusUpdate.location.toString());
                    }
                    final Status newStatus = twitter.uploadPhoto(update);
                    final ParcelableStatus result = ParcelableStatusUtils.fromStatus(newStatus,
                            credentials.account_key, false);
                    if (shouldShorten && shortener != null && shortenedResult != null) {
                        shortener.callback(shortenedResult, result);
                    }
                    if (uploader != null && uploadResult != null) {
                        uploader.callback(uploadResult, result);
                    }
                    results.add(SingleResponse.getInstance(result));
                } finally {
                    Utils.closeSilently(body);
                }
                return true;
            } else {
                final TwitterUpload upload = MicroBlogAPIFactory.getTwitterInstance(this, credentials,
                        true, true, TwitterUpload.class);
                if (upload == null) {
                    throw new UpdateStatusException("Twitter instance is null");
                }
                final String[] mediaIds = new String[statusUpdate.media.length];

                for (int i = 0, j = mediaIds.length; i < j; i++) {
                    final ParcelableMediaUpdate media = statusUpdate.media[i];
                    FileBody body = null;
                    final MediaUploadResponse uploadResp;
                    try {
                        body = getBodyFromMedia(resolver, builder, media, statusUpdate);
                        uploadResp = upload.uploadMedia(body);
                        if (!TextUtils.isEmpty(media.alt_text)) {
                            upload.createMetadata(new NewMediaMetadata(uploadResp.getId(),
                                    media.alt_text));
                        }
                    } finally {
                        Utils.closeSilently(body);
                    }
                    mediaIds[i] = uploadResp.getId();
                }
                status.mediaIds(mediaIds);
            }
        } catch (final IOException e) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
        } catch (final MicroBlogException e) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
            final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
            results.add(response);
            return true;
        }
        return false;
    }

    private FileBody getBodyFromMedia(final ContentResolver resolver, final Builder builder,
                                      final ParcelableMediaUpdate media,
                                      final ParcelableStatusUpdate statusUpdate) throws IOException {
        final Uri mediaUri = Uri.parse(media.uri);
        final String mediaType = resolver.getType(mediaUri);
        final InputStream is = resolver.openInputStream(mediaUri);
        if (is == null) {
            throw new FileNotFoundException(media.uri);
        }
        final long length = is.available();
        final ContentLengthInputStream cis = new ContentLengthInputStream(is, length);
        cis.setReadListener(new StatusMediaUploadListener(this, mNotificationManager, builder,
                statusUpdate));
        final ContentType contentType;
        if (TextUtils.isEmpty(mediaType)) {
            contentType = ContentType.parse("application/octet-stream");
        } else {
            contentType = ContentType.parse(mediaType);
        }
        return new FileBody(cis, "attachment", length, contentType);
    }

    private void triggerEasterEgg(boolean notReplyToOther, boolean hasEasterEggTriggerText, boolean hasEasterEggRestoreText) {
        final PackageManager pm = getPackageManager();
        final ComponentName main = new ComponentName(this, MainActivity.class);
        final ComponentName main2 = new ComponentName(this, MainHondaJOJOActivity.class);
        if (hasEasterEggTriggerText && notReplyToOther) {
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            showToast(R.string.easter_egg_triggered_message, Toast.LENGTH_SHORT);
        } else if (hasEasterEggRestoreText) {
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            showToast(R.string.icon_restored_message, Toast.LENGTH_SHORT);
        }
    }

    private static Notification updateSendDirectMessageNotification(final Context context,
                                                                    final NotificationCompat.Builder builder,
                                                                    final int progress, final String message) {
        builder.setContentTitle(context.getString(R.string.sending_direct_message));
        if (message != null) {
            builder.setContentText(message);
        }
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, progress, progress >= 100 || progress <= 0);
        builder.setOngoing(true);
        return builder.build();
    }

    private static Notification updateUpdateStatusNotification(final Context context,
                                                               final NotificationCompat.Builder builder,
                                                               final int progress,
                                                               final ParcelableStatusUpdate status) {
        builder.setContentTitle(context.getString(R.string.updating_status_notification));
        if (status != null) {
            builder.setContentText(status.text);
        }
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, progress, progress >= 100 || progress <= 0);
        builder.setOngoing(true);
        return builder.build();
    }

    public static void updateStatusesAsync(Context context, @Draft.Action final String action,
                                           final ParcelableStatusUpdate... statuses) {
        final Intent intent = new Intent(context, BackgroundOperationService.class);
        intent.setAction(INTENT_ACTION_UPDATE_STATUS);
        intent.putExtra(EXTRA_STATUSES, statuses);
        intent.putExtra(EXTRA_ACTION, action);
        context.startService(intent);
    }

    private static class ToastRunnable implements Runnable {
        private final Context context;
        private final int resId;
        private final int duration;

        public ToastRunnable(final Context context, final int resId, final int duration) {
            this.context = context;
            this.resId = resId;
            this.duration = duration;
        }

        @Override
        public void run() {
            Toast.makeText(context, resId, duration).show();

        }

    }

    static class MessageMediaUploadListener implements ReadListener {
        private final Context context;
        private final NotificationManagerWrapper manager;

        int percent;

        private final Builder builder;
        private final String message;

        MessageMediaUploadListener(final Context context, final NotificationManagerWrapper manager,
                                   final NotificationCompat.Builder builder, final String message) {
            this.context = context;
            this.manager = manager;
            this.builder = builder;
            this.message = message;
        }

        @Override
        public void onRead(final long length, final long position) {
            final int percent = length > 0 ? (int) (position * 100 / length) : 0;
            if (this.percent != percent) {
                manager.notify(NOTIFICATION_ID_SEND_DIRECT_MESSAGE,
                        updateSendDirectMessageNotification(context, builder, percent, message));
            }
            this.percent = percent;
        }
    }

    static class ShortenerNotFoundException extends UpdateStatusException {
        private static final long serialVersionUID = -7262474256595304566L;

        public ShortenerNotFoundException(final Context context) {
            super(context.getString(R.string.error_message_tweet_shortener_not_found));
        }
    }

    static class ShortenException extends UpdateStatusException {

        public ShortenException() {
            super();
        }

        public ShortenException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ShortenException(Throwable throwable) {
            super(throwable);
        }

        public ShortenException(final String message) {
            super(message);
        }
    }

    static class StatusMediaUploadListener implements ReadListener {
        private final Context context;
        private final NotificationManagerWrapper manager;

        int percent;

        private final Builder builder;
        private final ParcelableStatusUpdate statusUpdate;

        StatusMediaUploadListener(final Context context, final NotificationManagerWrapper manager,
                                  final NotificationCompat.Builder builder, final ParcelableStatusUpdate statusUpdate) {
            this.context = context;
            this.manager = manager;
            this.builder = builder;
            this.statusUpdate = statusUpdate;
        }

        @Override
        public void onRead(final long length, final long position) {
            final int percent = length > 0 ? (int) (position * 100 / length) : 0;
            if (this.percent != percent) {
                manager.notify(NOTIFICATION_ID_UPDATE_STATUS,
                        updateUpdateStatusNotification(context, builder, percent, statusUpdate));
            }
            this.percent = percent;
        }
    }

    static class UpdateStatusException extends Exception {
        public UpdateStatusException() {
            super();
        }

        public UpdateStatusException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UpdateStatusException(Throwable throwable) {
            super(throwable);
        }

        public UpdateStatusException(final String message) {
            super(message);
        }
    }

    static class UploaderNotFoundException extends UpdateStatusException {

        public UploaderNotFoundException() {
            super();
        }

        public UploaderNotFoundException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UploaderNotFoundException(Throwable throwable) {
            super(throwable);
        }

        public UploaderNotFoundException(String message) {
            super(message);
        }
    }

    static class UploadException extends UpdateStatusException {

        public UploadException() {
            super();
        }

        public UploadException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UploadException(Throwable throwable) {
            super(throwable);
        }

        public UploadException(String message) {
            super(message);
        }
    }

    static class ExtensionVersionMismatchException extends AbsServiceInterface.CheckServiceException {

    }
}
