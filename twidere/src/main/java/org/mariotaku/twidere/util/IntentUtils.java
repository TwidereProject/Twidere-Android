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
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.activity.support.MediaViewerActivity;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.fragment.support.SensitiveContentWarningDialogFragment;
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
public class IntentUtils {
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
                                       final Bundle activityOptions, final boolean newDocument) {
        if (context == null || user == null) return;
        final Bundle extras = new Bundle();
        extras.putParcelable(IntentConstants.EXTRA_USER, user);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, activityOptions);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openUserProfile(final Context context, final long accountId, final long userId,
                                       final String screenName, final Bundle activityOptions,
                                       final boolean newDocument) {
        if (context == null || accountId <= 0 || userId <= 0 && isEmpty(screenName)) return;
        final Uri uri = LinkCreator.getTwidereUserLink(accountId, userId, screenName);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
        extras.putParcelableArrayList(IntentConstants.EXTRA_USERS, new ArrayList<>(users));
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
        openMedia(context, message.account_id, false, null, message, current, message.media, options, newDocument);
    }

    public static void openMedia(final Context context, final ParcelableStatus status,
                                 final ParcelableMedia current, final Bundle options,
                                 final boolean newDocument) {
        openMedia(context, status.account_id, status.is_possibly_sensitive, status, null, current,
                getPrimaryMedia(status), options, newDocument);
    }

    public static void openMedia(final Context context, final long accountId, final boolean isPossiblySensitive,
                                 final ParcelableMedia current, final ParcelableMedia[] media,
                                 final Bundle options, final boolean newDocument) {
        openMedia(context, accountId, isPossiblySensitive, null, null, current, media, options, newDocument);
    }

    public static void openMedia(final Context context, final long accountId, final boolean isPossiblySensitive,
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
            args.putLong(IntentConstants.EXTRA_ACCOUNT_ID, accountId);
            args.putParcelable(IntentConstants.EXTRA_CURRENT_MEDIA, current);
            if (status != null) {
                args.putParcelable(IntentConstants.EXTRA_STATUS, status);
            }
            if (message != null) {
                args.putParcelable(IntentConstants.EXTRA_MESSAGE, message);
            }
            args.putParcelableArray(IntentConstants.EXTRA_MEDIA, media);
            args.putBundle(IntentConstants.EXTRA_ACTIVITY_OPTIONS, options);
            args.putBundle(IntentConstants.EXTRA_ACTIVITY_OPTIONS, options);
            args.putBoolean(IntentConstants.EXTRA_NEW_DOCUMENT, newDocument);
            fragment.setArguments(args);
            fragment.show(fm, "sensitive_content_warning");
        } else {
            openMediaDirectly(context, accountId, status, message, current, media, options,
                    newDocument);
        }
    }

    public static void openMediaDirectly(final Context context, final long accountId,
                                         final ParcelableStatus status, final ParcelableMedia current,
                                         final Bundle options, final boolean newDocument) {
        openMediaDirectly(context, accountId, status, null, current, getPrimaryMedia(status),
                options, newDocument);
    }

    public static ParcelableMedia[] getPrimaryMedia(ParcelableStatus status) {
        if (status.is_quote && ArrayUtils.isEmpty(status.media)) {
            return status.quoted_media;
        } else {
            return status.media;
        }
    }

    public static void openMediaDirectly(final Context context, final long accountId,
                                         final ParcelableDirectMessage message, final ParcelableMedia current,
                                         final ParcelableMedia[] media, final Bundle options,
                                         final boolean newDocument) {
        openMediaDirectly(context, accountId, null, message, current, media, options, newDocument);
    }

    public static void openMediaDirectly(final Context context, final long accountId,
                                         final ParcelableStatus status, final ParcelableDirectMessage message,
                                         final ParcelableMedia current, final ParcelableMedia[] media,
                                         final Bundle options, final boolean newDocument) {
        if (context == null || media == null) return;
        final Intent intent = new Intent(IntentConstants.INTENT_ACTION_VIEW_MEDIA);
        intent.putExtra(IntentConstants.EXTRA_ACCOUNT_ID, accountId);
        intent.putExtra(IntentConstants.EXTRA_CURRENT_MEDIA, current);
        intent.putExtra(IntentConstants.EXTRA_MEDIA, media);
        if (status != null) {
            intent.putExtra(IntentConstants.EXTRA_STATUS, status);
        }
        if (message != null) {
            intent.putExtra(IntentConstants.EXTRA_MESSAGE, message);
        }
        intent.setClass(context, MediaViewerActivity.class);
        if (newDocument && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, options);
        } else {
            context.startActivity(intent);
        }
    }
}
