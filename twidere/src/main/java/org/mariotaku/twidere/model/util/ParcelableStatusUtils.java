package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 16/1/3.
 */
public class ParcelableStatusUtils {

    public static void makeOriginalStatus(@NonNull ParcelableStatus status) {
        if (!status.is_retweet) return;
        status.id = status.retweet_id;
        status.retweeted_by_user_id = -1;
        status.retweeted_by_user_name = null;
        status.retweeted_by_user_screen_name = null;
        status.retweeted_by_user_profile_image = null;
        status.retweet_timestamp = -1;
        status.retweet_id = -1;
    }

}
