package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/17.
 */

@StringDef({AutoRefreshType.HOME_TIMELINE, AutoRefreshType.INTERACTIONS_TIMELINE,
        AutoRefreshType.DIRECT_MESSAGES})
@Retention(RetentionPolicy.SOURCE)
public @interface AutoRefreshType {

    String HOME_TIMELINE = "home_timeline";
    String INTERACTIONS_TIMELINE = "interactions_timeline";
    String DIRECT_MESSAGES = "direct_messages";

    String[] ALL = {HOME_TIMELINE, INTERACTIONS_TIMELINE, DIRECT_MESSAGES};
}
