package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/1/28.
 */
@StringDef({
        ReadPositionTag.ACTIVITIES_ABOUT_ME,
        ReadPositionTag.HOME_TIMELINE,
        ReadPositionTag.DIRECT_MESSAGES,
        ReadPositionTag.CUSTOM_TIMELINE,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ReadPositionTag {
    String HOME_TIMELINE = "home_timeline";
    String ACTIVITIES_ABOUT_ME = "activities_about_me";
    String DIRECT_MESSAGES = "direct_messages";
    String CUSTOM_TIMELINE = "custom_timeline";
}
