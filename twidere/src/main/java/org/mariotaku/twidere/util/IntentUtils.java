package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.activity.support.MediaViewerActivity;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.fragment.support.SensitiveContentWarningDialogFragment;
import org.mariotaku.twidere.fragment.support.UserFragment;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 16/1/2.
 */
public class IntentUtils implements Constants {
    public static String getStatusShareText(@NonNull final Context context, @NonNull final ParcelableStatus status) {
        final Uri link = LinkCreator.getTwitterStatusLink(status);
        return context.getString(R.string.status_share_text_format_with_link,
                status.text_plain, link.toString());
    }

    public static String getStatusShareSubject(@NonNull final Context context, @NonNull final ParcelableStatus status) {
        final String timeString = Utils.formatToLongTimeString(context, status.timestamp);
        return context.getString(R.string.status_share_subject_format_with_time,
                status.user_name, status.user_screen_name, timeString);
    }

    public static void openUserProfile(final Context context, final ParcelableUser user,
                                       final Bundle activityOptions, final boolean newDocument,
                                       @UserFragment.Referral final String referral) {
        if (context == null || user == null) return;
        final Bundle extras = new Bundle();
        extras.putParcelable(EXTRA_USER, user);
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwidereConstants.SCHEME_TWIDERE);
        builder.authority(TwidereConstants.AUTHORITY_USER);
        builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
        if (user.id > 0) {
            builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_USER_ID, String.valueOf(user.id));
        }
        if (user.screen_name != null) {
            builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_SCREEN_NAME, user.screen_name);
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        intent.setExtrasClassLoader(context.getClassLoader());
        intent.putExtras(extras);
        intent.putExtra(EXTRA_REFERRAL, referral);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, activityOptions);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openUserProfile(final Context context, @Nullable final AccountKey accountId,
                                       final long userId, final String screenName,
                                       final Bundle activityOptions, final boolean newDocument,
                                       @UserFragment.Referral final String referral) {
        if (context == null || userId <= 0 && isEmpty(screenName)) return;
        final Uri uri = LinkCreator.getTwidereUserLink(accountId, userId, screenName);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(EXTRA_REFERRAL, referral);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, activityOptions);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openUsers(final Context context, final List<ParcelableUser> users) {
        if (context == null || users == null) return;
        final Bundle extras = new Bundle();
        extras.putParcelableArrayList(EXTRA_USERS, new ArrayList<>(users));
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwidereConstants.SCHEME_TWIDERE);
        builder.authority(TwidereConstants.AUTHORITY_USERS);
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void openUserMentions(final Context context, final long accountId, final String screenName) {
        if (context == null) return;
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwidereConstants.SCHEME_TWIDERE);
        builder.authority(TwidereConstants.AUTHORITY_USER_MENTIONS);
        builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_ACCOUNT_ID, String.valueOf(accountId));
        if (screenName != null) {
            builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_SCREEN_NAME, screenName);
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        context.startActivity(intent);
    }

    public static void openMedia(final Context context, final ParcelableDirectMessage message,
                                 final ParcelableMedia current, @Nullable final Bundle options,
                                 final boolean newDocument) {
        openMedia(context, new AccountKey(message.account_id, message.account_host), false, null,
                message, current, message.media, options, newDocument);
    }

    public static void openMedia(final Context context, final ParcelableStatus status,
                                 final ParcelableMedia current, final Bundle options,
                                 final boolean newDocument) {
        openMedia(context, new AccountKey(status.account_id, status.account_host),
                status.is_possibly_sensitive, status, null, current, getPrimaryMedia(status),
                options, newDocument);
    }

    public static void openMedia(final Context context, final AccountKey accountKey, final boolean isPossiblySensitive,
                                 final ParcelableMedia current, final ParcelableMedia[] media,
                                 final Bundle options, final boolean newDocument) {
        openMedia(context, accountKey, isPossiblySensitive, null, null, current, media, options, newDocument);
    }

    public static void openMedia(final Context context, final AccountKey accountKey, final boolean isPossiblySensitive,
                                 final ParcelableStatus status, final ParcelableDirectMessage message,
                                 final ParcelableMedia current, final ParcelableMedia[] media,
                                 final Bundle options, final boolean newDocument) {
        if (context == null || media == null) return;
        final SharedPreferences prefs = context.getSharedPreferences(TwidereConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (context instanceof FragmentActivity && isPossiblySensitive
                && !prefs.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
            final FragmentActivity activity = (FragmentActivity) context;
            final FragmentManager fm = activity.getSupportFragmentManager();
            final DialogFragment fragment = new SensitiveContentWarningDialogFragment();
            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey);
            args.putParcelable(EXTRA_CURRENT_MEDIA, current);
            if (status != null) {
                args.putParcelable(EXTRA_STATUS, status);
            }
            if (message != null) {
                args.putParcelable(EXTRA_MESSAGE, message);
            }
            args.putParcelableArray(EXTRA_MEDIA, media);
            args.putBundle(EXTRA_ACTIVITY_OPTIONS, options);
            args.putBundle(EXTRA_ACTIVITY_OPTIONS, options);
            args.putBoolean(EXTRA_NEW_DOCUMENT, newDocument);
            fragment.setArguments(args);
            fragment.show(fm, "sensitive_content_warning");
        } else {
            openMediaDirectly(context, accountKey, status, message, current, media, options,
                    newDocument);
        }
    }

    public static void openMediaDirectly(final Context context, final AccountKey accountKey,
                                         final ParcelableStatus status, final ParcelableMedia current,
                                         final Bundle options, final boolean newDocument) {
        openMediaDirectly(context, accountKey, status, null, current, getPrimaryMedia(status),
                options, newDocument);
    }

    public static ParcelableMedia[] getPrimaryMedia(ParcelableStatus status) {
        if (status.is_quote && ArrayUtils.isEmpty(status.media)) {
            return status.quoted_media;
        } else {
            return status.media;
        }
    }

    public static void openMediaDirectly(final Context context, final AccountKey accountKey,
                                         final ParcelableDirectMessage message, final ParcelableMedia current,
                                         final ParcelableMedia[] media, final Bundle options,
                                         final boolean newDocument) {
        openMediaDirectly(context, accountKey, null, message, current, media, options, newDocument);
    }

    public static void openMediaDirectly(final Context context, final AccountKey accountKey,
                                         final ParcelableStatus status, final ParcelableDirectMessage message,
                                         final ParcelableMedia current, final ParcelableMedia[] media,
                                         final Bundle options, final boolean newDocument) {
        if (context == null || media == null) return;
        final Intent intent = new Intent(context, MediaViewerActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_ID, accountKey);
        intent.putExtra(EXTRA_CURRENT_MEDIA, current);
        intent.putExtra(EXTRA_MEDIA, media);
        if (status != null) {
            intent.putExtra(EXTRA_STATUS, status);
            intent.setData(getMediaViewerUri("status", status.id, accountKey));
        }
        if (message != null) {
            intent.putExtra(EXTRA_MESSAGE, message);
            intent.setData(getMediaViewerUri("message", message.id, accountKey));
        }
        if (newDocument && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, options);
        } else {
            context.startActivity(intent);
        }
    }

    public static Uri getMediaViewerUri(String type, long id, AccountKey accountKey) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_TWIDERE);
        builder.authority("media");
        builder.appendPath(type);
        builder.appendPath(String.valueOf(id));
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString());
        }
        return builder.build();
    }
}
