package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/1/28.
 */
@StringDef({
        NotificationType.INTERACTIONS,
        NotificationType.HOME_TIMELINE,
        NotificationType.DIRECT_MESSAGES
})
@Retention(RetentionPolicy.SOURCE)
public @interface NotificationType {
    @NotificationType
    String INTERACTIONS = "interactions";
    @NotificationType
    String HOME_TIMELINE = "home_timeline";
    @NotificationType
    String DIRECT_MESSAGES = "direct_messages";
}
