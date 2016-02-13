package org.mariotaku.twidere.model.util;

import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.Date;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

/**
 * Created by mariotaku on 16/2/13.
 */
public class ParcelableDirectMessageUtils {
    public static ParcelableDirectMessage fromDirectMessage(DirectMessage message, long accountId, boolean isOutgoing) {
        ParcelableDirectMessage result = new ParcelableDirectMessage();
        result.account_id = accountId;
        result.is_outgoing = isOutgoing;
        final User sender = message.getSender(), recipient = message.getRecipient();
        assert sender != null && recipient != null;
        final String sender_profile_image_url = TwitterContentUtils.getProfileImageUrl(sender);
        final String recipient_profile_image_url = TwitterContentUtils.getProfileImageUrl(recipient);
        result.id = message.getId();
        result.timestamp = getTime(message.getCreatedAt());
        result.sender_id = sender.getId();
        result.recipient_id = recipient.getId();
        result.text_html = TwitterContentUtils.formatDirectMessageText(message);
        result.text_plain = message.getText();
        result.sender_name = sender.getName();
        result.recipient_name = recipient.getName();
        result.sender_screen_name = sender.getScreenName();
        result.recipient_screen_name = recipient.getScreenName();
        result.sender_profile_image_url = sender_profile_image_url;
        result.recipient_profile_image_url = recipient_profile_image_url;
        result.text_unescaped = toPlainText(result.text_html);
        result.media = ParcelableMediaUtils.fromEntities(message);
        return result;
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

}
