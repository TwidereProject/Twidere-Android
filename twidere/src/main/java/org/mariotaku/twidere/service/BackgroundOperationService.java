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
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.twitter.Extractor;

import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.mime.FileTypedData;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.activity.MainHondaJOJOActivity;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterUpload;
import org.mariotaku.twidere.api.twitter.model.MediaUploadResponse;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusUpdate;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.BitmapUtils;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.MediaUploaderInterface;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.StatusCodeMessageUtils;
import org.mariotaku.twidere.util.StatusShortenerInterface;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.TimelineType;
import edu.tsinghua.hotmobi.model.TweetEvent;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ContentValuesCreator.createMessageDraft;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;

public class BackgroundOperationService extends IntentService implements Constants {

    private TwidereValidator mValidator;
    private final Extractor extractor = new Extractor();

    private Handler mHandler;
    private SharedPreferences mPreferences;
    private ContentResolver mResolver;
    private NotificationManager mNotificationManager;
    @Inject
    AsyncTwitterWrapper mTwitter;

    private MediaUploaderInterface mUploader;
    private StatusShortenerInterface mShortener;

    private boolean mUseUploader, mUseShortener;

    public BackgroundOperationService() {
        super("background_operation");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(this)).build().inject(this);
        final TwidereApplication app = TwidereApplication.getInstance(this);
        mHandler = new Handler();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mValidator = new TwidereValidator(this);
        mResolver = getContentResolver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
        final long draftId = ParseUtils.parseLong(uri.getLastPathSegment(), -1);
        if (draftId == -1) return;
        final Expression where = Expression.equals(Drafts._ID, draftId);
        final ContentResolver cr = getContentResolver();
        final Cursor c = cr.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where.getSQL(), null, null);
        if (c == null) return;
        final DraftItem.CursorIndices i = new DraftItem.CursorIndices(c);
        final DraftItem item;
        try {
            if (!c.moveToFirst()) return;
            item = new DraftItem(c, i);
        } finally {
            c.close();
        }
        cr.delete(Drafts.CONTENT_URI, where.getSQL(), null);
        if (item.action_type == Drafts.ACTION_UPDATE_STATUS || item.action_type <= 0) {
            updateStatuses(new ParcelableStatusUpdate(this, item));
        } else if (item.action_type == Drafts.ACTION_SEND_DIRECT_MESSAGE) {
            final long recipientId = item.action_extras.optLong(EXTRA_RECIPIENT_ID);
            if (item.account_ids == null || item.account_ids.length <= 0 || recipientId <= 0) {
                return;
            }
            final long accountId = item.account_ids[0];
            final String imageUri = item.media != null && item.media.length > 0 ? item.media[0].uri : null;
            sendMessage(accountId, recipientId, item.text, imageUri);
        }
    }

    private void handleDiscardDraftIntent(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) return;
        mNotificationManager.cancel(data.toString(), NOTIFICATION_ID_DRAFTS);
        final ContentResolver cr = getContentResolver();
        final long id = ParseUtils.parseLong(data.getLastPathSegment(), -1);
        final Expression where = Expression.equals(Drafts._ID, id);
        cr.delete(Drafts.CONTENT_URI, where.getSQL(), null);
    }

    private void handleSendDirectMessageIntent(final Intent intent) {
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        final long recipientId = intent.getLongExtra(EXTRA_RECIPIENT_ID, -1);
        final String imageUri = intent.getStringExtra(EXTRA_IMAGE_URI);
        final String text = intent.getStringExtra(EXTRA_TEXT);
        sendMessage(accountId, recipientId, text, imageUri);
    }

    private void sendMessage(long accountId, long recipientId, String text, String imageUri) {
        if (accountId <= 0 || recipientId <= 0 || isEmpty(text)) return;
        final String title = getString(R.string.sending_direct_message);
        final Builder builder = new Builder(this);
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
            final ContentValues values = ContentValuesCreator.createDirectMessage(result.getData());
            final String delete_where = DirectMessages.ACCOUNT_ID + " = " + accountId + " AND "
                    + DirectMessages.MESSAGE_ID + " = " + result.getData().id;
            mResolver.delete(DirectMessages.Outbox.CONTENT_URI, delete_where, null);
            mResolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
            showOkMessage(R.string.direct_message_sent, false);
        } else {
            final ContentValues values = createMessageDraft(accountId, recipientId, text, imageUri);
            mResolver.insert(Drafts.CONTENT_URI, values);
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
        updateStatuses(statuses);
    }

    private void updateStatuses(ParcelableStatusUpdate... statuses) {
        final Builder builder = new Builder(this);
        startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(this, builder, 0, null));
        for (final ParcelableStatusUpdate item : statuses) {
            mNotificationManager.notify(NOTIFICATION_ID_UPDATE_STATUS,
                    updateUpdateStatusNotification(this, builder, 0, item));
            final ContentValues draftValues = ContentValuesCreator.createStatusDraft(item,
                    ParcelableAccount.getAccountIds(item.accounts));
            final Uri draftUri = mResolver.insert(Drafts.CONTENT_URI, draftValues);
            final long draftId = draftUri != null ? ParseUtils.parseLong(draftUri.getLastPathSegment(), -1) : -1;
            mTwitter.addSendingDraftId(draftId);
            final List<SingleResponse<ParcelableStatus>> result = updateStatus(builder, item);
            boolean failed = false;
            Exception exception = null;
            final Expression where = Expression.equals(Drafts._ID, draftId);
            final List<Long> failedAccountIds = ListUtils.fromArray(ParcelableAccount.getAccountIds(item.accounts));

            for (final SingleResponse<ParcelableStatus> response : result) {
                final ParcelableStatus data = response.getData();
                if (data == null) {
                    failed = true;
                    if (exception == null) {
                        exception = response.getException();
                    }
                } else if (data.account_id > 0) {
                    failedAccountIds.remove(data.account_id);
                    // BEGIN HotMobi
                    final TweetEvent event = TweetEvent.create(this, data, TimelineType.OTHER);
                    event.setAction(TweetEvent.Action.TWEET);
                    HotMobiLogger.getInstance(this).log(data.account_id, event);
                    // END HotMobi
                }
            }

            if (result.isEmpty()) {
                showErrorMessage(R.string.action_updating_status, getString(R.string.no_account_selected), false);
            } else if (failed) {
                // If the status is a duplicate, there's no need to save it to
                // drafts.
                if (exception instanceof TwitterException
                        && ((TwitterException) exception).getErrorCode() == StatusCodeMessageUtils.STATUS_IS_DUPLICATE) {
                    showErrorMessage(getString(R.string.status_is_duplicate), false);
                } else {
                    final ContentValues accountIdsValues = new ContentValues();
                    accountIdsValues.put(Drafts.ACCOUNT_IDS, ListUtils.toString(failedAccountIds, ',', false));
                    mResolver.update(Drafts.CONTENT_URI, accountIdsValues, where.getSQL(), null);
                    showErrorMessage(R.string.action_updating_status, exception, true);
                    final ContentValues notifValues = new ContentValues();
                    notifValues.put(BaseColumns._ID, draftId);
                    mResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, notifValues);
                }
            } else {
                showOkMessage(R.string.status_updated, false);
                mResolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
                if (item.media != null) {
                    for (final ParcelableMediaUpdate media : item.media) {
                        final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                        if (path != null) {
                            if (!new File(path).delete()) {
                                Log.d(LOGTAG, String.format("unable to delete %s", path));
                            }
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
                                                                      final long accountId, final long recipientId,
                                                                      final String text, final String imageUri) {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(this, accountId, true, true);
        final TwitterUpload twitterUpload = TwitterAPIFactory.getTwitterInstance(this, accountId, true, true, TwitterUpload.class);
        if (twitter == null || twitterUpload == null) return SingleResponse.getInstance();
        try {
            final ParcelableDirectMessage directMessage;
            if (imageUri != null) {
                final String path = getImagePathFromUri(this, Uri.parse(imageUri));
                if (path == null) throw new FileNotFoundException();
                final BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, o);
                final File file = new File(path);
                BitmapUtils.downscaleImageIfNeeded(file, 100);
                final ContentLengthInputStream is = new ContentLengthInputStream(file);
                is.setReadListener(new MessageMediaUploadListener(this, mNotificationManager, builder, text));
//                final MediaUploadResponse uploadResp = twitter.uploadMedia(file.getName(), is, o.outMimeType);
                final MediaUploadResponse uploadResp = twitterUpload.uploadMedia(file);
                directMessage = new ParcelableDirectMessage(twitter.sendDirectMessage(recipientId, text,
                        uploadResp.getId()), accountId, true);
                if (!file.delete()) {
                    Log.d(LOGTAG, String.format("unable to delete %s", path));
                }
            } else {
                directMessage = new ParcelableDirectMessage(twitter.sendDirectMessage(recipientId, text), accountId,
                        true);
            }
            Utils.setLastSeen(this, recipientId, System.currentTimeMillis());


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
        final ArrayList<ContentValues> hashTagValues = new ArrayList<>();
        final Collection<String> hashTags = extractor.extractHashtags(statusUpdate.text);
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
        mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
                hashTagValues.toArray(new ContentValues[hashTagValues.size()]));

        final List<SingleResponse<ParcelableStatus>> results = new ArrayList<>();

        if (statusUpdate.accounts.length == 0) return Collections.emptyList();

        try {
            if (mUseUploader && mUploader == null) throw new UploaderNotFoundException(this);
            if (mUseShortener && mShortener == null) throw new ShortenerNotFoundException(this);

            final boolean hasMedia = statusUpdate.media != null && statusUpdate.media.length > 0;

            final String overrideStatusText;
            if (mUseUploader && mUploader != null && hasMedia) {
                final MediaUploadResult uploadResult;
                try {
                    mUploader.waitForService();
                    uploadResult = mUploader.upload(statusUpdate,
                            UploaderMediaItem.getFromStatusUpdate(this, statusUpdate));
                } catch (final Exception e) {
                    throw new UploadException(this);
                } finally {
                    mUploader.unbindService();
                }
                if (mUseUploader && hasMedia && uploadResult == null)
                    throw new UploadException(this);
                if (uploadResult.error_code != 0)
                    throw new UploadException(uploadResult.error_message);
                overrideStatusText = getImageUploadStatus(this, uploadResult.media_uris, statusUpdate.text);
            } else {
                overrideStatusText = null;
            }

            final String unShortenedText = isEmpty(overrideStatusText) ? statusUpdate.text : overrideStatusText;

            final boolean shouldShorten = mValidator.getTweetLength(unShortenedText) > mValidator.getMaxTweetLength();
            final String shortenedText;
            if (shouldShorten) {
                if (mUseShortener) {
                    final StatusShortenResult shortenedResult;
                    mShortener.waitForService();
                    try {
                        shortenedResult = mShortener.shorten(statusUpdate, unShortenedText);
                    } catch (final Exception e) {
                        throw new ShortenException(this);
                    } finally {
                        mShortener.unbindService();
                    }
                    if (shortenedResult == null || shortenedResult.shortened == null)
                        throw new ShortenException(this);
                    shortenedText = shortenedResult.shortened;
                } else
                    throw new StatusTooLongException(this);
            } else {
                shortenedText = unShortenedText;
            }
            if (statusUpdate.media != null) {
                for (final ParcelableMediaUpdate media : statusUpdate.media) {
                    final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                    final File file = path != null ? new File(path) : null;
                    if (!mUseUploader && file != null && file.exists()) {
                        BitmapUtils.downscaleImageIfNeeded(file, 95);
                    }
                }
            }
            for (final ParcelableAccount account : statusUpdate.accounts) {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(this, account.account_id, true, true);
                final TwitterUpload upload = TwitterAPIFactory.getTwitterInstance(this, account.account_id, true, true, TwitterUpload.class);
                final StatusUpdate status = new StatusUpdate(shortenedText);
                status.inReplyToStatusId(statusUpdate.in_reply_to_status_id);
                if (statusUpdate.location != null) {
                    status.location(ParcelableLocation.toGeoLocation(statusUpdate.location));
                }
                if (!mUseUploader && hasMedia) {
                    final BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    final long[] mediaIds = new long[statusUpdate.media.length];
                    ContentLengthInputStream is = null;
                    try {
                        for (int i = 0, j = mediaIds.length; i < j; i++) {
                            final ParcelableMediaUpdate media = statusUpdate.media[i];
                            final String path = getImagePathFromUri(this, Uri.parse(media.uri));
                            if (path == null) throw new FileNotFoundException();
                            BitmapFactory.decodeFile(path, o);
                            final File file = new File(path);
                            is = new ContentLengthInputStream(file);
                            is.setReadListener(new StatusMediaUploadListener(this, mNotificationManager, builder,
                                    statusUpdate));
                            final ContentType contentType;
                            if (TextUtils.isEmpty(o.outMimeType)) {
                                contentType = ContentType.parse("image/*");
                            } else {
                                contentType = ContentType.parse(o.outMimeType);
                            }
                            final MediaUploadResponse uploadResp = upload.uploadMedia(new FileTypedData(is,
                                    file.getName(), file.length(), contentType));
                            mediaIds[i] = uploadResp.getId();
                        }
                    } catch (final FileNotFoundException e) {
                        Log.w(LOGTAG, e);
                    } catch (final TwitterException e) {
                        Log.w(LOGTAG, e);
                        final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
                        results.add(response);
                        continue;
                    } finally {
                        IoUtils.closeSilently(is);
                    }
                    status.mediaIds(mediaIds);
                }
                status.possiblySensitive(statusUpdate.is_possibly_sensitive);

                if (twitter == null) {
                    results.add(SingleResponse.<ParcelableStatus>getInstance(new NullPointerException()));
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
                        Utils.setLastSeen(this, entities, System.currentTimeMillis());
                    }
                    if (!notReplyToOther) {
                        final long inReplyToUserId = resultStatus.getInReplyToUserId();
                        if (inReplyToUserId <= 0 || inReplyToUserId == HONDAJOJO_ID) {
                            notReplyToOther = true;
                        }
                    }
                    final ParcelableStatus result = new ParcelableStatus(resultStatus, account.account_id, false);
                    results.add(SingleResponse.getInstance(result));
                } catch (final TwitterException e) {
                    Log.w(LOGTAG, e);
                    final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(e);
                    results.add(response);
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

    private static Notification updateUpdateStatusNotification(final Context context,
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
                        updateUpdateStatusNotification(context, builder, percent, statusUpdate));
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
