package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.Date;

/**
 * Created by mariotaku on 16/1/3.
 */
public class StatusUtils {

    public static Status fromParcelableStatus(@NonNull ParcelableStatus parcelable) {
        Status status = new Status();
        status.id = parcelable.id;
        status.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.text_plain);
        status.createdAt = new Date(parcelable.timestamp);
        status.inReplyToStatusId = parcelable.in_reply_to_status_id;
        status.inReplyToUserId = parcelable.in_reply_to_user_id;
        status.inReplyToScreenName = parcelable.in_reply_to_screen_name;
        if (parcelable.is_retweet) {
            Status retweet = status.retweetedStatus = new Status();
            retweet.id = parcelable.retweet_id;
            retweet.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.text_plain);
            retweet.createdAt = new Date(parcelable.retweet_timestamp);
            User retweetUser = retweet.user = new User();
            retweetUser.id = parcelable.user_id;
            retweetUser.screenName = parcelable.user_screen_name;
            retweetUser.name = parcelable.user_name;
            retweetUser.profileBackgroundImageUrl = parcelable.user_profile_image_url;

            User user = status.user = new User();
            user.id = parcelable.retweeted_by_user_id;
            user.name = parcelable.retweeted_by_user_name;
            user.screenName = parcelable.retweeted_by_user_screen_name;
            user.profileImageUrl = parcelable.retweeted_by_user_profile_image;
        } else if (parcelable.is_quote) {
            Status quote = status.quotedStatus = new Status();
            quote.id = parcelable.quoted_id;
            quote.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.quoted_text_plain);
            quote.createdAt = new Date(parcelable.quoted_timestamp);
            User quotedUser = quote.user = new User();
            quotedUser.id = parcelable.quoted_user_id;
            quotedUser.name = parcelable.quoted_user_name;
            quotedUser.screenName = parcelable.quoted_user_screen_name;
            quotedUser.profileImageUrl = parcelable.quoted_user_profile_image;

            User user = status.user = new User();
            user.id = parcelable.user_id;
            user.screenName = parcelable.user_screen_name;
            user.name = parcelable.user_name;
            user.profileBackgroundImageUrl = parcelable.user_profile_image_url;
        } else {
            User user = status.user = new User();
            user.id = parcelable.user_id;
            user.screenName = parcelable.user_screen_name;
            user.name = parcelable.user_name;
            user.profileBackgroundImageUrl = parcelable.user_profile_image_url;
        }
        return status;
    }

}
