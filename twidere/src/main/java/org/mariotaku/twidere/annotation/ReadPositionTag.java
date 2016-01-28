package org.mariotaku.twidere.annotation;

import android.support.annotation.StringDef;

/**
 * Created by mariotaku on 16/1/28.
 */
@StringDef({
        ReadPositionTag.ACTIVITIES_ABOUT_ME,
        ReadPositionTag.HOME_TIMELINE,
        ReadPositionTag.DIRECT_MESSAGES,
})
public @interface ReadPositionTag {
    String HOME_TIMELINE = "home_timeline";
    String ACTIVITIES_ABOUT_ME = "activities_about_me";
    String DIRECT_MESSAGES = "direct_messages";
}
