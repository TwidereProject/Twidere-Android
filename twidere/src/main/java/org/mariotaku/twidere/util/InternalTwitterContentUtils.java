package org.mariotaku.twidere.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import android.text.TextUtils;

import org.mariotaku.microblog.library.twitter.model.DMResponse;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.MediaEntity;
import org.mariotaku.microblog.library.twitter.model.UrlEntity;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.extension.model.api.StatusExtensionsKt;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.SpanItem;

import java.nio.charset.Charset;
import java.util.zip.CRC32;

import kotlin.Pair;

/**
 * Created by mariotaku on 16/2/24.
 */
public class InternalTwitterContentUtils {

    private InternalTwitterContentUtils() {
    }

    @NonNull
    public static String getBestBannerUrl(@NonNull final String baseUrl, final int width, final int height) {
        final String type;
        if (width <= 0) {
            type = "1500x500";
        } else {
            type = getBestBannerType(width, height);
        }
        final String authority = UriUtils.getAuthority(baseUrl);
        return authority != null && authority.endsWith(".twimg.com") ? baseUrl + "/" + type : baseUrl;
    }

    @NonNull
    public static String getBestBannerType(final int width, int height) {
        if (height > 0 && width / height >= 3) {
            if (width <= 300) return "300x100";
            else if (width <= 600) return "600x200";
            else return "1500x500";
        }
        if (width <= 320) return "mobile";
        else if (width <= 520) return "web";
        else if (width <= 626) return "ipad";
        else if (width <= 640) return "mobile_retina";
        else if (width <= 1040) return "web_retina";
        else return "ipad_retina";
    }

    @Nullable
    public static Pair<String, SpanItem[]> formatUserDescription(@NonNull final User user) {
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, false);
        final UrlEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final UrlEntity url : urls) {
                final String expandedUrl = url.getExpandedUrl();
                if (expandedUrl != null) {
                    builder.addLink(expandedUrl, url.getDisplayUrl(), url.getStart(), url.getEnd(),
                            false);
                }
            }
        }
        return builder.buildWithIndices();
    }

    @NonNull
    public static Pair<String, SpanItem[]> formatDirectMessageText(@NonNull final DirectMessage message) {
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, false);
        StatusExtensionsKt.addEntities(builder, message);
        return builder.buildWithIndices();
    }

    @NonNull
    public static Pair<String, SpanItem[]> formatDirectMessageText(@NonNull final DMResponse.Entry.Message.Data message) {
        String text = message.getText();
        if (text == null) {
            text = "";
        }
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, false);
        StatusExtensionsKt.addEntities(builder, message);
        return builder.buildWithIndices();
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    public static boolean getStartEndForEntity(UrlEntity entity, @NonNull int[] out) {
        out[0] = entity.getStart();
        out[1] = entity.getEnd();
        return true;
    }


    public static boolean isOfficialKey(final Context context, final String consumerKey,
            final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) return false;
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (final String keySecret : keySecrets) {
            if (Long.parseLong(keySecret, 16) == value) return true;
        }
        return false;
    }

    public static String getOfficialKeyName(final Context context, final String consumerKey,
            final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) return null;
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final String[] keyNames = context.getResources().getStringArray(R.array.names_official_consumer_secret);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (int i = 0, j = keySecrets.length; i < j; i++) {
            if (Long.parseLong(keySecrets[i], 16) == value) return keyNames[i];
        }
        return null;
    }

    @NonNull
    public static ConsumerKeyType getOfficialKeyType(final Context context, final String consumerKey,
            final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) {
            return ConsumerKeyType.UNKNOWN;
        }
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final String[] keyNames = context.getResources().getStringArray(R.array.types_official_consumer_secret);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (int i = 0, j = keySecrets.length; i < j; i++) {
            if (Long.parseLong(keySecrets[i], 16) == value) {
                return ConsumerKeyType.parse(keyNames[i]);
            }
        }
        return ConsumerKeyType.UNKNOWN;
    }
}
