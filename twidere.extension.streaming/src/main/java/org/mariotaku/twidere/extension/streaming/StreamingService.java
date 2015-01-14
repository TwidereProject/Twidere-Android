package org.mariotaku.twidere.extension.streaming;

import android.app.Notification;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.TwidereSharedPreferences;
import org.mariotaku.twidere.extension.streaming.util.TwidereHostAddressResolverFactory;
import org.mariotaku.twidere.extension.streaming.util.Utils;
import org.mariotaku.twidere.library.twitter4j.streaming.BuildConfig;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccount.ParcelableCredentials;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Mentions;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.StreamConfigurationBuilder;

import static android.text.TextUtils.isEmpty;

public class StreamingService extends Service implements Constants, PrivateConstants {

    private static final int NOTIFITION_SERVICE_STARTED = 1;
    private static final int NOTIFITION_REQUEST_PERMISSION = 2;

    private final List<WeakReference<TwitterStream>> mTwitterInstances = new ArrayList<>();
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
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mResolver = getContentResolver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Stream service started.");
        }
        initStreaming();
        mResolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
    }

    @Override
    public void onDestroy() {
        clearTwitterInstances();
        mResolver.unregisterContentObserver(mAccountChangeObserver);
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Stream service stopped.");
        }
        super.onDestroy();
    }

    private void clearTwitterInstances() {
        for (final WeakReference<TwitterStream> reference : mTwitterInstances) {
            final TwitterStream twitter = reference.get();
            new Thread(new ShutdownStreamTwitterRunnable(twitter)).start();
        }
        mTwitterInstances.clear();
        mNotificationManager.cancel(NOTIFITION_SERVICE_STARTED);
    }

    @SuppressWarnings("deprecation")
    private void initStreaming() {
        if (!mPreferences.getBoolean(PREFERENCE_KEY_ENABLE_STREAMING, true)) return;
        final boolean granted;
        try {
            granted = Twidere.isPermissionGranted(this);
        } catch (final SecurityException e) {
            stopSelf();
            return;
        }
        if (granted) {
            final TwidereSharedPreferences prefs = Twidere.getSharedPreferences(this);
            if (setTwitterInstances(prefs)) {
                final Intent intent = new Intent(this, SettingsActivity.class);
                final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                final CharSequence contentTitle = getString(R.string.app_name);
                final CharSequence contentText = getString(R.string.streaming_service_running);
                final Notification notification = new Notification();
                notification.flags = Notification.FLAG_ONGOING_EVENT;
                notification.icon = R.drawable.ic_stat_twidere;
                notification.tickerText = getString(R.string.streaming_service_running);
                notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
                mNotificationManager.notify(NOTIFITION_SERVICE_STARTED, notification);
            } else {
                mNotificationManager.cancel(NOTIFITION_SERVICE_STARTED);
            }
        } else {
            final Intent intent = new Intent(this, SettingsActivity.class);
            final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final CharSequence contentTitle = getString(R.string.app_name);
            final CharSequence contentText = getString(R.string.request_permission);
            final Notification notification = new Notification();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.icon = R.drawable.ic_stat_login;
            notification.tickerText = getString(R.string.request_permission);
            notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
            mNotificationManager.notify(NOTIFITION_REQUEST_PERMISSION, notification);
        }
    }

    private boolean setTwitterInstances(final TwidereSharedPreferences prefs) {
        if (prefs == null) return false;
        final List<ParcelableCredentials> accountsList = ParcelableAccount.getCredentialsList(this, true);
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Setting up twitter stream instances");
        }
        mAccountIds = new long[accountsList.size()];
        clearTwitterInstances();
        for (int i = 0, j = accountsList.size(); i < j; i++) {
            final ParcelableCredentials account = accountsList.get(i);
            final String token = account.oauth_token;
            final String secret = account.oauth_token_secret;
            final long account_id = account.account_id;
            mAccountIds[i] = account_id;
            final StreamConfigurationBuilder cb = new StreamConfigurationBuilder();
            cb.setGZIPEnabled(prefs.getBoolean(KEY_GZIP_COMPRESSING, true));
            cb.setIncludeEntitiesEnabled(true);
            if (prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false)) {
                cb.setIgnoreSSLError(true);
                cb.setHostAddressResolverFactory(new TwidereHostAddressResolverFactory(this));
            }
            final String default_consumer_key = Utils
                    .getNonEmptyString(prefs, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
            final String default_consumer_secret = Utils.getNonEmptyString(prefs, KEY_CONSUMER_SECRET,
                    TWITTER_CONSUMER_SECRET_2);
            final String consumer_key = account.consumer_key, consumer_secret = account.consumer_secret;
            if (!isEmpty(consumer_key) && !isEmpty(consumer_secret)) {
                cb.setOAuthConsumerKey(consumer_key);
                cb.setOAuthConsumerSecret(consumer_secret);
            } else {
                cb.setOAuthConsumerKey(default_consumer_key);
                cb.setOAuthConsumerSecret(default_consumer_secret);
            }
            final TwitterStream twitter = new TwitterStreamFactory(cb.build()).getInstance(new AccessToken(token,
                    secret));
            twitter.addListener(new UserStreamListenerImpl(this, account_id));
            twitter.user();
            mTwitterInstances.add(new WeakReference<>(twitter));
        }
        return true;
    }

    static class ShutdownStreamTwitterRunnable implements Runnable {
        private final TwitterStream twitter;

        ShutdownStreamTwitterRunnable(final TwitterStream twitter) {
            this.twitter = twitter;
        }

        @Override
        public void run() {
            if (twitter == null) return;
            twitter.shutdown();
        }

    }

    static class UserStreamListenerImpl implements UserStreamListener {

        private final long account_id;
        private final ParcelableAccount account;
        private final ContentResolver resolver;
        private final Context context;

        private boolean statusStreamStarted, mentionsStreamStarted;

        public UserStreamListenerImpl(final Context context, final long account_id) {
            this.context = context;
            this.account_id = account_id;
            resolver = context.getContentResolver();
            account = ParcelableAccount.getAccount(context, account_id);
        }

        @Override
        public void onBlock(final User source, final User blockedUser) {
            final String message = String.format("%s blocked %s", source.getScreenName(), blockedUser.getScreenName());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
                final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND "
                        + DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
                resolver.delete(uri, where, null);
            }
            final User sender = directMessage.getSender(), recipient = directMessage.getRecipient();
            if (sender.getId() == account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account_id, true);
                if (values != null) {
                    resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
                }
            }
            if (recipient.getId() == account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account_id, false);
                final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
                builder.appendQueryParameter(Twidere.QUERY_PARAM_NOTIFY, "true");
                if (values != null) {
                    resolver.insert(builder.build(), values);
                }
            }

        }

        @Override
        public void onException(final Exception ex) {
            Log.w(LOGTAG, ex);
        }

        @Override
        public void onFavorite(final User source, final User target, final Status favoritedStatus) {
            final String message = String.format("%s favorited %s's tweet: %s", source.getScreenName(),
                    target.getScreenName(), favoritedStatus.getText());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFollow(final User source, final User followedUser) {
            final String message = String
                    .format("%s followed %s", source.getScreenName(), followedUser.getScreenName());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
        public void onStallWarning(final StallWarning warn) {

        }

        @Override
        public void onStatus(final Status status) {
            final ContentValues values = ContentValuesCreator.createStatus(status, account_id);
            if (values == null) return;
            if (!statusStreamStarted) {
                statusStreamStarted = true;
                values.put(Statuses.IS_GAP, true);
            }
            final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = "
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
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
            final String message = String.format("%s unfavorited %s's tweet: %s", source.getScreenName(),
                    target.getScreenName(), unfavoritedStatus.getText());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

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
