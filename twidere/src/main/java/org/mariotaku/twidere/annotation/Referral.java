package org.mariotaku.twidere.annotation;

import android.support.annotation.StringDef;

/**
 * Created by mariotaku on 16/6/29.
 */
@StringDef({Referral.SEARCH_RESULT, Referral.USER_MENTION, Referral.STATUS, Referral.TIMELINE_STATUS,
        Referral.DIRECT, Referral.EXTERNAL, Referral.SELF_PROFILE})
public @interface Referral {

    String SEARCH_RESULT = "search_result";
    String USER_MENTION = "user_mention";
    String STATUS = "status";
    String TIMELINE_STATUS = "timeline_status";
    String DIRECT = "direct";
    String EXTERNAL = "external";
    String SELF_PROFILE = "self_profile";
}
