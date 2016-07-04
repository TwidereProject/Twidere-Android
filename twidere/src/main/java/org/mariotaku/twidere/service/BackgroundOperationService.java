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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.twitter.Extractor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.abstask.library.ManualTaskStarter;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.TwitterUpload;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.ErrorInfo;
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse;
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse.ProcessingInfo;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.DraftCursorIndices;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtra;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableDirectMessageUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUpdateUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.task.twitter.UpdateStatusTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.NotificationManagerWrapper;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.TimelineType;
import edu.tsinghua.hotmobi.model.TweetEvent;

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
    private static final long BULK_SIZE = 128 * 1024; // 128KiB


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
                if (ArrayUtils.isEmpty(item.account_keys) || recipientId == null) {
                    return;
                }
                final UserKey accountKey = item.account_keys[0];
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
            final ContentValues values = ContentValuesCreator.createMessageDraft(accountId, recipientId, text, imageUri);
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
        final BackgroundOperationService context = this;
        final Builder builder = new Builder(context);
        startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                builder, 0, null));
        for (final ParcelableStatusUpdate item : statuses) {
            final UpdateStatusTask task = new UpdateStatusTask(context, new UpdateStatusTask.StateCallback() {

                @WorkerThread
                @Override
                public void onStartUploadingMedia() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item));
                }

                @WorkerThread
                @Override
                public void onUploadingProgressChanged(int index, long current, long total) {
                    int progress = (int) (current * 100 / total);
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, progress, item));
                }

                @WorkerThread
                @Override
                public void onShorteningStatus() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item));
                }

                @WorkerThread
                @Override
                public void onUpdatingStatus() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item));
                }

                @Override
                public void afterExecute(Context handler, UpdateStatusTask.UpdateStatusResult result) {
                    boolean failed = false;
                    if (result.exception != null) {
                        Toast.makeText(context, result.exception.getMessage(), Toast.LENGTH_SHORT).show();
                        failed = true;
                    } else for (MicroBlogException e : result.exceptions) {
                        if (e != null) {
                            // Show error
                            Toast.makeText(context, R.string.status_not_updated, Toast.LENGTH_SHORT).show();
                            failed = true;
                            break;
                        }
                    }
                    if (failed) {
                        // TODO show draft notification
                    } else {
                        Toast.makeText(context, R.string.status_updated, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void beforeExecute() {

                }
            });
            task.setCallback(this);
            task.setParams(Pair.create(actionType, item));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ManualTaskStarter.invokeBeforeExecute(task);
                }
            });

            final UpdateStatusTask.UpdateStatusResult result = ManualTaskStarter.invokeExecute(task);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ManualTaskStarter.invokeAfterExecute(task, result);
                }
            });

            if (result.exception != null) {
                Log.w(LOGTAG, result.exception);
            } else for (ParcelableStatus status : result.statuses) {
                if (status == null) continue;
                final TweetEvent event = TweetEvent.create(context, status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.TWEET);
                HotMobiLogger.getInstance(context).log(status.account_key, event);
            }
        }
        if (mPreferences.getBoolean(KEY_REFRESH_AFTER_TWEET)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTwitter.refreshAll();
                }
            });
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
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(this, credentials, true, true);
        final TwitterUpload twitterUpload = MicroBlogAPIFactory.getInstance(this, credentials,
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
                        final Uri mediaUri = Uri.parse(imageUri);
                        FileBody body = null;
                        try {
                            body = UpdateStatusTask.getBodyFromMedia(getContentResolver(), mediaUri,
                                    new MessageMediaUploadListener(this, mNotificationManager,
                                            builder, text));
                            final MediaUploadResponse uploadResp = uploadMedia(twitterUpload, body);
                            final DirectMessage response = twitter.sendDirectMessage(recipientId,
                                    text, uploadResp.getId());
                            directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                    accountKey, true);
                        } finally {
                            Utils.closeSilently(body);
                        }
                        final String path = Utils.getImagePathFromUri(this, mediaUri);
                        if (path != null) {
                            final File file = new File(path);
                            if (!file.delete()) {
                                Log.d(LOGTAG, String.format("unable to delete %s", path));
                            }
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


    private MediaUploadResponse uploadMedia(final TwitterUpload upload, Body body) throws IOException, MicroBlogException {
        final String mediaType = body.contentType().getContentType();
        final long length = body.length();
        final InputStream stream = body.stream();
        MediaUploadResponse response = upload.initUploadMedia(mediaType, length, null);
        final int segments = length == 0 ? 0 : (int) (length / BULK_SIZE + 1);
        for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
            final int currentBulkSize = (int) Math.min(BULK_SIZE, length - segmentIndex * BULK_SIZE);
            final SimpleBody bulk = new SimpleBody(ContentType.OCTET_STREAM, null, currentBulkSize,
                    stream);
            upload.appendUploadMedia(response.getId(), segmentIndex, bulk);
        }
        response = upload.finalizeUploadMedia(response.getId());
        for (ProcessingInfo info = response.getProcessingInfo(); shouldWaitForProcess(info); info = response.getProcessingInfo()) {
            final long checkAfterSecs = info.getCheckAfterSecs();
            if (checkAfterSecs <= 0) {
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(checkAfterSecs));
            } catch (InterruptedException e) {
                break;
            }
            response = upload.getUploadMediaStatus(response.getId());
        }
        ProcessingInfo info = response.getProcessingInfo();
        if (info != null && ProcessingInfo.State.FAILED.equals(info.getState())) {
            final MicroBlogException exception = new MicroBlogException();
            ErrorInfo errorInfo = info.getError();
            if (errorInfo != null) {
                exception.setErrors(new ErrorInfo[]{errorInfo});
            }
            throw exception;
        }
        return response;
    }

    private boolean shouldWaitForProcess(ProcessingInfo info) {
        if (info == null) return false;
        switch (info.getState()) {
            case ProcessingInfo.State.PENDING:
            case ProcessingInfo.State.IN_PROGRESS:
                return true;
            default:
                return false;
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

}
