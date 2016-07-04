package org.mariotaku.twidere.model.util;

import android.support.v4.util.Pair;

import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.Date;

/**
 * Created by mariotaku on 16/2/13.
 */
public class ParcelableDirectMessageUtils {

    private ParcelableDirectMessageUtils() {
    }

    public static ParcelableDirectMessage fromDirectMessage(DirectMessage message, UserKey accountKey, boolean isOutgoing) {
        ParcelableDirectMessage result = new ParcelableDirectMessage();
        result.account_key = accountKey;
        result.is_outgoing = isOutgoing;
        final User sender = message.getSender(), recipient = message.getRecipient();
        assert sender != null && recipient != null;
        final String sender_profile_image_url = TwitterContentUtils.getProfileImageUrl(sender);
        final String recipient_profile_image_url = TwitterContentUtils.getProfileImageUrl(recipient);
        result.id = message.getId();
        result.timestamp = getTime(message.getCreatedAt());
        result.sender_id = sender.getId();
        result.recipient_id = recipient.getId();
        final Pair<String, SpanItem[]> pair = InternalTwitterContentUtils.formatDirectMessageText(message);
        result.text_unescaped = pair.first;
        result.spans = pair.second;
        result.text_plain = message.getText();
        result.sender_name = sender.getName();
        result.recipient_name = recipient.getName();
        result.sender_screen_name = sender.getScreenName();
        result.recipient_screen_name = recipient.getScreenName();
        result.sender_profile_image_url = sender_profile_image_url;
        result.recipient_profile_image_url = recipient_profile_image_url;
        result.media = ParcelableMediaUtils.fromEntities(message);
        if (isOutgoing) {
            result.conversation_id = result.recipient_id;
        } else {
            result.conversation_id = result.sender_id;
        }
        return result;
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

}
