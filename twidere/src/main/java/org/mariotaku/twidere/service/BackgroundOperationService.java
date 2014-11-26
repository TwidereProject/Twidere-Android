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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;

import com.twitter.Extractor;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.activity.MainHondaJOJOActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.MediaUploaderInterface;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.StatusCodeMessageUtils;
import org.mariotaku.twidere.util.StatusShortenerInterface;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twitter4j.MediaUploadResponse;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeDirectMessageDraftContentValues;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

public class BackgroundOperationService extends IntentService implements Constants {

    private TwidereValidator mValidator;
    private final Extractor extractor = new Extractor();

    private Handler mHandler;
    private SharedPreferences mPreferences;
    private ContentResolver mResolver;
    private NotificationManager mNotificationManager;
    private AsyncTwitterWrapper mTwitter;
    private MessagesManager mMessagesManager;

    private MediaUploaderInterface mUploader;
    private StatusShortenerInterface mShortener;

    private boolean mUseUploader, mUseShortener;

    public BackgroundOperationService() {
        super("background_operation");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final TwidereApplication app = TwidereApplication.getInstance(this);
        mHandler = new Handler();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mValidator = new TwidereValidator(this);
        mResolver = getContentResolver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mTwitter = app.getTwitterWrapper();
        mMessagesManager = app.getMessagesManager();
        final String uploaderComponent = mPreferences.getString(KEY_MEDIA_UPLOADER, null);
        final String shortenerComponent = mPreferences.getString(KEY_STATUS_SHORTENER, null);
        mUseUploader = !ServicePickerPreference.isNoneValue(uploaderComponent);
        mUseShortener = !ServicePickerPreference.isNoneValue(shortenerComponent);
        mUploader = mUseUploader ? MediaUploaderInterface.getInstance(app, uploaderComponent) : null;
        mShortener = mUseShortener ? StatusShortenerInterface.getInstance(app, shortenerComponent) : null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void showErrorMessage(final CharSequence message, final boolean long_message) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mMessagesManager.showErrorMessage(message, long_message);
            }
        });
    }

    public void showErrorMessage(final int action_res, final Exception e, final boolean long_message) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mMessagesManager.showErrorMessage(action_res, e, long_message);
            }
        });
    }

    public void showErrorMessage(final int action_res, final String message, final boolean long_message) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mMessagesManager.showErrorMessage(action_res, message, long_message);
            }
        });
    }

    public void showOkMessage(final int message_res, final boolean long_message) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mMessagesManager.showOkMessage(message_res, long_message);
            }
        });
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        if (INTENT_ACTION_UPDATE_STATUS.equals(action)) {
            handleUpdateStatusIntent(intent);
        } else if (INTENT_ACTION_SEND_DIRECT_MESSAGE.equals(action)) {
            handleSendDirectMessageIntent(intent);
        }
    }

    private Notification buildNotification(final String title, final String message, final int icon,
                                           final Intent content_intent, final Intent delete_intent) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(message);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(icon);
        if (delete_intent != null) {
            builder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, delete_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }
        if (content_intent != null) {
            content_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, content_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }
        // final Uri defRingtone =
        // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // final String path =
        // mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, "");
        // builder.setSound(isEmpty(path) ? defRingtone : Uri.parse(path),
        // Notification.STREAM_DEFAULT);
        // builder.setLights(HOLO_BLUE_LIGHT, 1000, 2000);
        // builder.setDefaults(Notification.DEFAULT_VIBRATE);
        return builder.build();
    }

    private void handleSendDirectMessageIntent(final Intent intent) {
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        final long recipientId = intent.getLongExtra(EXTRA_RECIPIENT_ID, -1);
        final String imageUri = intent.getStringExtra(EXTRA_IMAGE_URI);
        final String text = intent.getStringExtra(EXTRA_TEXT);
        if (accountId <= 0 || recipientId <= 0 || isEmpty(text)) return;
        final String title = getString(R.string.sending_direct_message);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, 0, true);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setOngoing(true);
        final Notification notification = builder.build();
        startForeground(NOTIFICATION_ID_SEND_DIRECT_MESSAGE, notification);
        final SingleResponse<ParcelableDirectMessage> result = sendDirectMessage(builder, accountId, recipientId, text,
                imageUri);
        if (result.getData() != null && result.getData().id > 0) {
            final ContentValues values = makeDirectMessageContentValues(result.getData());
            final String delete_where = DirectMessages.ACCOUNT_ID + " = " + accountId + " AND "
                    + DirectMessages.MESSAGE_ID + " = " + result.getData().id;
            mResolver.delete(DirectMessages.Outbox.CONTENT_URI, delete_where, null);
            mResolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
            showOkMessage(R.string.direct_message_sent, false);
        } else {
            final ContentValues values = makeDirectMessageDraftContentValues(accountId, recipientId, text, imageUri);
            mResolver.insert(Drafts.CONTENT_URI, values);
            showErrorMessage(R.string.action_sending_direct_message, result.getException(), true);
        }
        stopForeground(false);
        mNotificationManager.cancel(NOTIFICATION_ID_SEND_DIRECT_MESSAGE);
    }

    private void handleUpdateStatusIntent(final Intent intent) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
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
        startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotificaion(this, builder, 0, null));
        for (final ParcelableStatusUpdate item : statuses) {
            mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS,
                    updateUpdateStatusNotificaion(this, builder, 0, item));
            final List<SingleResponse<ParcelableStatus>> result = updateStatus(builder, item);
            boolean failed = false;
            Exception exception = null;
            final List<Long> failed_account_ids = ListUtils.fromArray(Account.getAccountIds(item.accounts));

            for (final SingleResponse<ParcelableStatus> response : result) {
                if (response.getData() == null) {
                    failed = true;
                    if (exception == null) {
                        exception = response.getException();
                    }
                } else if (response.getData().account_id > 0) {
                    failed_account_ids.remove(response.getData().account_id);
                }
            }
            if (result.isEmpty()) {
                saveDrafts(item, failed_account_ids);
                showErrorMessage(R.string.action_updating_status, getString(R.string.no_account_selected), false);
            } else if (failed) {
                // If the status is a duplicate, there's no need to save it to
                // drafts.
                if (exception instanceof TwitterException
                        && ((TwitterException) exception).getErrorCode() == StatusCodeMessageUtils.STATUS_IS_DUPLICATE) {
                    showErrorMessage(getString(R.string.status_is_duplicate), false);
                } else {
                    saveDrafts(item, failed_account_ids);
                    showErrorMessage(R.string.action_updating_status, exception, true);
                }
            } else {
                showOkMessage(R.string.status_updated, false);
                if (item.media != null) {
                    for (final ParcelableMediaUpdate media : item.media) {
                        final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                        if (path != null) {
                            new File(path).delete();
                        }
                    }
                }
            }
            if (mPreferences.getBoolean(KEY_REFRESH_AFTER_TWEET, false)) {
                mTwitter.refreshAll();
            }
        }
        stopForeground(false);
        mNotificationManager.cancel(NOTIFICATION_ID_UPDATE_STATUS);
    }

    private void saveDrafts(final ParcelableStatusUpdate status, final List<Long> account_ids) {
        final ContentValues values = ContentValuesCreator.makeStatusDraftContentValues(status,
                ArrayUtils.fromList(account_ids));
        mResolver.insert(Drafts.CONTENT_URI, values);
        final String title = getString(R.string.status_not_updated);
        final String message = getString(R.string.status_not_updated_summary);
        final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
        final Notification notification = buildNotification(title, message, R.drawable.ic_stat_twitter, intent, null);
        mNotificationManager.notify(NOTIFICATION_ID_DRAFTS, notification);
    }

    private SingleResponse<ParcelableDirectMessage> sendDirectMessage(final NotificationCompat.Builder builder,
                                                                      final long accountId, final long recipientId, final String text, final String imageUri) {
        final Twitter twitter = getTwitterInstance(this, accountId, true, true);
        try {
            final ParcelableDirectMessage directMessage;
            if (imageUri != null) {
                final String path = getImagePathFromUri(this, Uri.parse(imageUri));
                if (path == null) throw new FileNotFoundException();
                final BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, o);
                final File file = new File(path);
                Utils.downscaleImageIfNeeded(file, 100);
                final ContentLengthInputStream is = new ContentLengthInputStream(file);
                is.setReadListener(new MessageMediaUploadListener(this, mNotificationManager, builder, text));
                final MediaUploadResponse uploadResp = twitter.uploadMedia(file.getName(), is, o.outMimeType);
                directMessage = new ParcelableDirectMessage(twitter.sendDirectMessage(recipientId, text,
                        uploadResp.getId()), accountId, true);
                file.delete();
            } else {
                directMessage = new ParcelableDirectMessage(twitter.sendDirectMessage(recipientId, text), accountId,
                        true);
            }
            return SingleResponse.getInstance(directMessage);
        } catch (final IOException e) {
            return SingleResponse.getInstance(e);
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(e);
        }
    }

    private void showToast(final int resId, final int duration) {
        mHandler.post(new ToastRunnable(this, resId, duration));
    }

    private List<SingleResponse<ParcelableStatus>> updateStatus(final Builder builder,
                                                                final ParcelableStatusUpdate statusUpdate) {
        final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
        final Collection<String> hashtags = extractor.extractHashtags(statusUpdate.text);
        for (final String hashtag : hashtags) {
            final ContentValues values = new ContentValues();
            values.put(CachedHashtags.NAME, hashtag);
            hashtag_values.add(values);
        }
        final boolean hasEasterEggTriggerText = statusUpdate.text.contains(EASTER_EGG_TRIGGER_TEXT);
        final boolean hasEasterEggRestoreText = statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART1)
                && statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART2)
                && statusUpdate.text.contains(EASTER_EGG_RESTORE_TEXT_PART3);
        boolean mentionedHondaJOJO = false, notReplyToOther = false;
        mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
                hashtag_values.toArray(new ContentValues[hashtag_values.size()]));

        final List<SingleResponse<ParcelableStatus>> results = new ArrayList<SingleResponse<ParcelableStatus>>();

        if (statusUpdate.accounts.length == 0) return Collections.emptyList();

        try {
            if (mUseUploader && mUploader == null) throw new UploaderNotFoundException(this);
            if (mUseShortener && mShortener == null) throw new ShortenerNotFoundException(this);

            final boolean hasMedia = statusUpdate.media != null && statusUpdate.media.length > 0;

            final String overrideStatusText;
            if (mUseUploader && hasMedia) {
                final MediaUploadResult uploadResult;
                try {
                    if (mUploader != null) {
                        mUploader.waitForService();
                    }
                    uploadResult = mUploader.upload(statusUpdate,
                            UploaderMediaItem.getFromStatusUpdate(this, statusUpdate));
                } catch (final Exception e) {
                    throw new UploadException(this);
                }
                if (mUseUploader && hasMedia && uploadResult == null)
                    throw new UploadException(this);
                if (uploadResult.error_code != 0)
                    throw new UploadException(uploadResult.error_message);
                overrideStatusText = getImageUploadStatus(this, uploadResult.media_uris, statusUpdate.text);
            } else {
                overrideStatusText = null;
            }

            final String unshortenedText = isEmpty(overrideStatusText) ? statusUpdate.text : overrideStatusText;

            final boolean shouldShorten = mValidator.getTweetLength(unshortenedText) > mValidator.getMaxTweetLength();
            final String shortenedText;
            if (shouldShorten) {
                if (mUseShortener) {
                    final StatusShortenResult shortenedResult;
                    mShortener.waitForService();
                    try {
                        shortenedResult = mShortener.shorten(statusUpdate, unshortenedText);
                    } catch (final Exception e) {
                        throw new ShortenException(this);
                    }
                    if (shortenedResult == null || shortenedResult.shortened == null)
                        throw new ShortenException(this);
                    shortenedText = shortenedResult.shortened;
                } else
                    throw new StatusTooLongException(this);
            } else {
                shortenedText = unshortenedText;
            }
            if (statusUpdate.media != null) {
                for (final ParcelableMediaUpdate media : statusUpdate.media) {
                    final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                    final File file = path != null ? new File(path) : null;
                    if (!mUseUploader && file != null && file.exists()) {
                        Utils.downscaleImageIfNeeded(file, 95);
                    }
                }
            }
            for (final Account account : statusUpdate.accounts) {
                final Twitter twitter = getTwitterInstance(this, account.account_id, true, true);
                final StatusUpdate status = new StatusUpdate(shortenedText);
                status.setInReplyToStatusId(statusUpdate.in_reply_to_status_id);
                if (statusUpdate.location != null) {
                    status.setLocation(ParcelableLocation.toGeoLocation(statusUpdate.location));
                }
                if (!mUseUploader && hasMedia) {
                    final BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    final long[] mediaIds = new long[statusUpdate.media.length];
                    try {
                        for (int i = 0, j = mediaIds.length; i < j; i++) {
                            final ParcelableMediaUpdate media = statusUpdate.media[i];
                            final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                            if (path == null) throw new FileNotFoundException();
                            BitmapFactory.decodeFile(path, o);
                            final File file = new File(path);
                            final ContentLengthInputStream is = new ContentLengthInputStream(file);
                            is.setReadListener(new StatusMediaUploadListener(this, mNotificationManager, builder,
                                    statusUpdate));
                            final MediaUploadResponse uploadResp = twitter.uploadMedia(file.getName(), is,
                                    o.outMimeType);
                            mediaIds[i] = uploadResp.getId();
                        }
                    } catch (final FileNotFoundException e) {

                    } catch (final TwitterException e) {
                        final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
                        results.add(response);
                        continue;
                    }
                    status.mediaIds(mediaIds);
                }
                status.setPossiblySensitive(statusUpdate.is_possibly_sensitive);

                if (twitter == null) {
                    results.add(new SingleResponse<ParcelableStatus>(null, new NullPointerException()));
                    continue;
                }
                try {
                    final Status resultStatus = twitter.updateStatus(status);
                    if (!mentionedHondaJOJO) {
                        final UserMentionEntity[] entities = resultStatus.getUserMentionEntities();
                        if (entities == null || entities.length == 0) {
                            mentionedHondaJOJO = statusUpdate.text.contains("@" + HONDAJOJO_SCREEN_NAME);
                        } else if (entities.length == 1 && entities[0].getId() == HONDAJOJO_ID) {
                            mentionedHondaJOJO = true;
                        }
                    }
                    if (!notReplyToOther) {
                        final long inReplyToUserId = resultStatus.getInReplyToUserId();
                        if (inReplyToUserId <= 0 || inReplyToUserId == HONDAJOJO_ID) {
                            notReplyToOther = true;
                        }
                    }
                    final ParcelableStatus result = new ParcelableStatus(resultStatus, account.account_id, false);
                    results.add(new SingleResponse<ParcelableStatus>(result, null));
                } catch (final TwitterException e) {
                    final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
                    results.add(response);
                }
            }
        } catch (final UpdateStatusException e) {
            final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
            results.add(response);
        }
        if (mentionedHondaJOJO) {
            final PackageManager pm = getPackageManager();
            final ComponentName main = new ComponentName(this, MainActivity.class);
            final ComponentName main2 = new ComponentName(this, MainHondaJOJOActivity.class);
            if (hasEasterEggTriggerText) {
                if (notReplyToOther) {
                    pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                    showToast(R.string.easter_egg_triggered_message, Toast.LENGTH_SHORT);
                }
            } else if (hasEasterEggRestoreText) {
                pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                showToast(R.string.icon_restored_message, Toast.LENGTH_SHORT);
            }
        }
        return results;
    }

    private static Notification updateSendDirectMessageNotificaion(final Context context,
                                                                   final NotificationCompat.Builder builder, final int progress, final String message) {
        builder.setContentTitle(context.getString(R.string.sending_direct_message));
        if (message != null) {
            builder.setContentText(message);
        }
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, progress, progress >= 100 || progress <= 0);
        builder.setOngoing(true);
        return builder.build();
    }

    private static Notification updateUpdateStatusNotificaion(final Context context,
                                                              final NotificationCompat.Builder builder, final int progress, final ParcelableStatusUpdate status) {
        builder.setContentTitle(context.getString(R.string.updating_status_notification));
        if (status != null) {
            builder.setContentText(status.text);
        }
        builder.setSmallIcon(R.drawable.ic_stat_send);
        builder.setProgress(100, progress, progress >= 100 || progress <= 0);
        builder.setOngoing(true);
        return builder.build();
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
        private final NotificationManager manager;

        int percent;

        private final Builder builder;
        private final String message;

        MessageMediaUploadListener(final Context context, final NotificationManager manager,
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
                        updateSendDirectMessageNotificaion(context, builder, percent, message));
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
        private static final long serialVersionUID = 3075877185536740034L;

        public ShortenException(final Context context) {
            super(context.getString(R.string.error_message_tweet_shorten_failed));
        }
    }

    static class StatusMediaUploadListener implements ReadListener {
        private final Context context;
        private final NotificationManager manager;

        int percent;

        private final Builder builder;
        private final ParcelableStatusUpdate statusUpdate;

        StatusMediaUploadListener(final Context context, final NotificationManager manager,
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
                        updateUpdateStatusNotificaion(context, builder, percent, statusUpdate));
            }
            this.percent = percent;
        }
    }

    static class StatusTooLongException extends UpdateStatusException {
        private static final long serialVersionUID = -6469920130856384219L;

        public StatusTooLongException(final Context context) {
            super(context.getString(R.string.error_message_status_too_long));
        }
    }

    static class UpdateStatusException extends Exception {
        private static final long serialVersionUID = -1267218921727097910L;

        public UpdateStatusException(final String message) {
            super(message);
        }
    }

    static class UploaderNotFoundException extends UpdateStatusException {
        private static final long serialVersionUID = 1041685850011544106L;

        public UploaderNotFoundException(final Context context) {
            super(context.getString(R.string.error_message_image_uploader_not_found));
        }
    }

    static class UploadException extends UpdateStatusException {
        private static final long serialVersionUID = 8596614696393917525L;

        public UploadException(final Context context) {
            super(context.getString(R.string.error_message_image_upload_failed));
        }

        public UploadException(final String message) {
            super(message);
        }
    }
}
