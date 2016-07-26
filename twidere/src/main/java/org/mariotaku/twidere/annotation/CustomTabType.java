package org.mariotaku.twidere.annotation;

import android.support.annotation.StringDef;

/**
 * Created by mariotaku on 16/1/28.
 */
@StringDef({
        CustomTabType.HOME_TIMELINE,
        CustomTabType.NOTIFICATIONS_TIMELINE,
        CustomTabType.TRENDS_SUGGESTIONS,
        CustomTabType.DIRECT_MESSAGES,
        CustomTabType.FAVORITES,
        CustomTabType.USER_TIMELINE,
        CustomTabType.SEARCH_STATUSES,
        CustomTabType.LIST_TIMELINE,
})
public @interface CustomTabType {
    String HOME_TIMELINE = "home_timeline";
    String NOTIFICATIONS_TIMELINE = "notifications_timeline";
    String TRENDS_SUGGESTIONS = "trends_suggestions";
    String DIRECT_MESSAGES = "direct_messages";
    String DIRECT_MESSAGES_NEXT = "direct_messages_next";
    String FAVORITES = "favorites";
    String USER_TIMELINE = "user_timeline";
    String SEARCH_STATUSES = "search_statuses";
    String LIST_TIMELINE = "list_timeline";
}
