package org.mariotaku.twidere.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterUserStream;
import org.mariotaku.twidere.api.twitter.UserStreamCallback;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusDeletionNotice;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.Warning;
import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Mentions;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class StreamingService extends Service implements Constants {

    private static final int NOTIFICATION_SERVICE_STARTED = 1;

    private final LongSparseArray<UserStreamCallback> mCallbacks = new LongSparseArray<>();
    private ContentResolver mResolver;

    private SharedPreferences mPreferences;
    private NotificationManager mNotificationManager;

    private long[] mAccountIds;

    private static final Uri[] STATUSES_URIS = new Uri[]{Statuses.CONTENT_URI, Mentions.CONTENT_URI};
    private static final Uri[] MESSAGES_URIS = new Uri[]{DirectMessages.Inbox.CONTENT_URI,
            DirectMessages.Outbox.CONTENT_URI};

    private final ContentObserver mAccountChangeObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(final boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            if (!TwidereArrayUtils.contentMatch(mAccountIds, Utils.getActivatedAccountIds(StreamingService.this))) {
                initStreaming();
            }
        }

    };

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mResolver = getContentResolver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Stream service started.");
        }
        initStreaming();
        mResolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
    }

    @Override
    public void onDestroy() {
        clearTwitterInstances();
        mResolver.unregisterContentObserver(mAccountChangeObserver);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Stream service stopped.");
        }
        super.onDestroy();
    }

    private void clearTwitterInstances() {
        for (int i = 0, j = mCallbacks.size(); i < j; i++) {
            new Thread(new ShutdownStreamTwitterRunnable(mCallbacks.valueAt(i))).start();
        }
        mCallbacks.clear();
        mNotificationManager.cancel(NOTIFICATION_SERVICE_STARTED);
    }

    @SuppressWarnings("deprecation")
    private void initStreaming() {
        if (!BuildConfig.DEBUG) return;
        setTwitterInstances();
        updateStreamState();
    }

    private boolean setTwitterInstances() {
        final List<ParcelableCredentials> accountsList = ParcelableAccount.getCredentialsList(this, true);
        final long[] accountIds = new long[accountsList.size()];
        for (int i = 0, j = accountIds.length; i < j; i++) {
            accountIds[i] = accountsList.get(i).account_id;
        }
        final AccountPreferences[] activitedPreferences = AccountPreferences.getAccountPreferences(this, accountIds);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Setting up twitter stream instances");
        }
        mAccountIds = accountIds;
        clearTwitterInstances();
        boolean result = false;
        for (int i = 0, j = accountsList.size(); i < j; i++) {
            final AccountPreferences preferences = activitedPreferences[i];
            if (!preferences.isStreamingEnabled()) continue;
            final ParcelableCredentials account = accountsList.get(i);
            final Endpoint endpoint = TwitterAPIFactory.getEndpoint(account, TwitterUserStream.class);
            final Authorization authorization = TwitterAPIFactory.getAuthorization(account);
            final TwitterUserStream twitter = TwitterAPIFactory.getInstance(this, endpoint, authorization, TwitterUserStream.class);
            final TwidereUserStreamCallback callback = new TwidereUserStreamCallback(this, account);
            mCallbacks.put(account.account_id, callback);
            new Thread() {
                @Override
                public void run() {
                    twitter.getUserStream(callback);
                    Log.d(Constants.LOGTAG, String.format("Stream %d disconnected", account.account_id));
                    mCallbacks.remove(account.account_id);
                    updateStreamState();
                }
            }.start();
            result |= true;
        }
        return result;
    }

    private void updateStreamState() {
        if (mCallbacks.size() > 0) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final CharSequence contentTitle = getString(R.string.app_name);
            final CharSequence contentText = getString(R.string.timeline_streaming_running);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setOngoing(true);
            builder.setSmallIcon(R.drawable.ic_stat_refresh);
            builder.setContentTitle(contentTitle);
            builder.setContentText(contentText);
            builder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_SERVICE_STARTED, builder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_SERVICE_STARTED);
        }
    }

    static class ShutdownStreamTwitterRunnable implements Runnable {
        private final UserStreamCallback callback;

        ShutdownStreamTwitterRunnable(final UserStreamCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            if (callback == null) return;
            Log.d(Constants.LOGTAG, "Disconnecting stream");
            callback.disconnect();
        }

    }

    static class TwidereUserStreamCallback extends UserStreamCallback {

        private final Context context;
        private final ParcelableAccount account;
        private final ContentResolver resolver;

        private boolean statusStreamStarted, mentionsStreamStarted;

        public TwidereUserStreamCallback(final Context context, final ParcelableAccount account) {
            this.context = context;
            this.account = account;
            resolver = context.getContentResolver();
        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onBlock(final User source, final User blockedUser) {
            final String message = String.format("%s blocked %s", source.getScreenName(), blockedUser.getScreenName());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onDeletionNotice(final long directMessageId, final long userId) {
            final String where = DirectMessages.MESSAGE_ID + " = " + directMessageId;
            for (final Uri uri : MESSAGES_URIS) {
                resolver.delete(uri, where, null);
            }
        }

        @Override
        public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
            final long status_id = statusDeletionNotice.getStatusId();
            final String where = Statuses.STATUS_ID + " = " + status_id;
            for (final Uri uri : STATUSES_URIS) {
                resolver.delete(uri, where, null);
            }
        }

        @Override
        public void onDirectMessage(final DirectMessage directMessage) {
            if (directMessage == null || directMessage.getId() <= 0) return;
            for (final Uri uri : MESSAGES_URIS) {
                final String where = DirectMessages.ACCOUNT_ID + " = " + account.account_id + " AND "
                        + DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
                resolver.delete(uri, where, null);
            }
            final User sender = directMessage.getSender(), recipient = directMessage.getRecipient();
            if (sender.getId() == account.account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.account_id, true);
                if (values != null) {
                    resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
                }
            }
            if (recipient.getId() == account.account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.account_id, false);
                final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
                builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
                if (values != null) {
                    resolver.insert(builder.build(), values);
                }
            }

        }

        @Override
        public void onException(final Throwable ex) {
            if (ex instanceof TwitterException) {
                Log.w(LOGTAG, String.format("Error %d", ((TwitterException) ex).getStatusCode()), ex);
                final RestHttpResponse response = ((TwitterException) ex).getHttpResponse();
                if (response != null) {
                    try {
                        final TypedData body = response.getBody();
                        if (body != null) {
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            body.writeTo(os);
                            final String charsetName;
                            final ContentType contentType = body.contentType();
                            if (contentType != null) {
                                final Charset charset = contentType.getCharset();
                                if (charset != null) {
                                    charsetName = charset.name();
                                } else {
                                    charsetName = Charset.defaultCharset().name();
                                }
                            } else {
                                charsetName = Charset.defaultCharset().name();
                            }
                            Log.w(LOGTAG, os.toString(charsetName));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.w(Constants.LOGTAG, ex);
            }
        }

        @Override
        public void onFavorite(final User source, final User target, final Status favoritedStatus) {
            final String message = String.format("%s favorited %s's tweet: %s", source.getScreenName(),
                    target.getScreenName(), favoritedStatus.getText());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onFollow(final User source, final User followedUser) {
            final String message = String
                    .format("%s followed %s", source.getScreenName(), followedUser.getScreenName());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onFriendList(final long[] friendIds) {

        }

        @Override
        public void onScrubGeo(final long userId, final long upToStatusId) {
            final String where = Statuses.USER_ID + " = " + userId + " AND " + Statuses.STATUS_ID + " >= "
                    + upToStatusId;
            final ContentValues values = new ContentValues();
            values.putNull(Statuses.LOCATION);
            for (final Uri uri : STATUSES_URIS) {
                resolver.update(uri, values, where, null);
            }
        }

        @Override
        public void onStallWarning(final Warning warn) {

        }

        @Override
        public void onStatus(final Status status) {
            final ContentValues values = ContentValuesCreator.createStatus(status, account.account_id);
            if (!statusStreamStarted) {
                statusStreamStarted = true;
                values.put(Statuses.IS_GAP, true);
            }
            final String where = Statuses.ACCOUNT_ID + " = " + account.account_id + " AND " + Statuses.STATUS_ID + " = "
                    + status.getId();
            resolver.delete(Statuses.CONTENT_URI, where, null);
            resolver.delete(Mentions.CONTENT_URI, where, null);
            resolver.insert(Statuses.CONTENT_URI, values);
            final Status rt = status.getRetweetedStatus();
            if (rt != null && rt.getText().contains("@" + account.screen_name) || rt == null
                    && status.getText().contains("@" + account.screen_name)) {
                resolver.insert(Mentions.CONTENT_URI, values);
            }
        }

        @Override
        public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {

        }

        @Override
        public void onUnblock(final User source, final User unblockedUser) {
            final String message = String.format("%s unblocked %s", source.getScreenName(),
                    unblockedUser.getScreenName());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
            final String message = String.format("%s unfavorited %s's tweet: %s", source.getScreenName(),
                    target.getScreenName(), unfavoritedStatus.getText());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onUserListCreation(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListDeletion(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListMemberAddition(final User addedMember, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListMemberDeletion(final User deletedMember, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListSubscription(final User subscriber, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListUnsubscription(final User subscriber, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListUpdate(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserProfileUpdate(final User updatedUser) {

        }
    }

}
