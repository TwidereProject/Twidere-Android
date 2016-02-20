package org.mariotaku.twidere.extension.twitlonger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.service.StatusShortenerService;

/**
 * Tweet shortener example
 *
 * @author mariotaku
 */
public class TwitLongerStatusShortenerService extends StatusShortenerService implements Constants {

    private static final int NOTIFICATION_ID_REQUEST_PERMISSION = 1;


    /**
     * @return Shortened tweet.
     */
    @Override
    protected StatusShortenResult shorten(final ParcelableStatusUpdate status,
                                          final ParcelableAccount currentAccount,
                                          final String overrideStatusText) {
        final int granted = Twidere.isPermissionGranted(this);
        if (granted == Twidere.Permission.DENIED) {
            return StatusShortenResult.error(-1, getString(R.string.permission_not_granted));
        } else if (granted != Twidere.Permission.GRANTED) {
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.ic_stat_warning);
            builder.setTicker(getString(R.string.permission_request));
            builder.setContentTitle(getString(R.string.permission_is_required_to_shorten_status));
            builder.setContentText(getString(R.string.app_name));
            builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                    RequestPermissionActivity.class), PendingIntent.FLAG_ONE_SHOT));
            builder.setAutoCancel(true);
            nm.notify(NOTIFICATION_ID_REQUEST_PERMISSION, builder.build());
            return StatusShortenResult.error(-1, getString(R.string.permission_not_granted));
        }
        final ParcelableCredentials credentials;
        try {
            credentials = getOAuthCredentials(currentAccount);
        } catch (SecurityException e) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
            return StatusShortenResult.error(-1, getString(R.string.permission_not_granted));
        }
        if (credentials == null) {
            return StatusShortenResult.error(-1, "No valid Twitter account found");
        }
        final TwitLonger tl = TwitLongerFactory.getInstance(TWITLONGER_API_KEY, credentials);
        try {
            final String text;
            if (overrideStatusText != null) {
                text = overrideStatusText;
            } else {
                text = status.text;
            }
            NewPost newPost = new NewPost(text);
            if (status.in_reply_to_status != null) {
                newPost.setInReplyTo(status.in_reply_to_status.id, status.in_reply_to_status.user_screen_name);
            }
            final Post response = tl.createPost(newPost);
            if (response != null) return StatusShortenResult.shortened(response.content);
        } catch (final TwitLongerException e) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
            return StatusShortenResult.error(-1, e.getMessage());
        }
        return StatusShortenResult.error(-1, "Unknown error");
    }

    @Override
    protected boolean callback(StatusShortenResult result, ParcelableStatus status) {
        return false;
    }

    @Nullable
    private ParcelableCredentials getOAuthCredentials(ParcelableAccount account) {
        ParcelableCredentials credentials = Twidere.getCredentials(this, account.account_id);
        if (credentials == null) return null;
        switch (credentials.auth_type) {
            case ParcelableCredentials.AUTH_TYPE_OAUTH:
            case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                return credentials;
            }
        }
        return null;
    }

}
