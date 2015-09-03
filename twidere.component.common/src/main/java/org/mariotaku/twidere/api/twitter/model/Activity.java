/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.api.twitter.model;

import java.util.Date;

public interface Activity extends TwitterResponse, Comparable<Activity> {

    int ACTION_FAVORITE = 0x01;
    int ACTION_FOLLOW = 0x02;
    int ACTION_MENTION = 0x03;
    int ACTION_REPLY = 0x04;
    int ACTION_RETWEET = 0x05;
    int ACTION_LIST_MEMBER_ADDED = 0x06;
    int ACTION_LIST_CREATED = 0x07;
    int ACTION_FAVORITED_RETWEET = 0x08;
    int ACTION_RETWEETED_RETWEET = 0x09;
    int ACTION_QUOTE = 0x0A;
    int ACTION_RETWEETED_MENTION = 0x0B;
    int ACTION_FAVORITED_MENTION = 0x0C;
    int ACTION_JOINED_TWITTER = 0x0D;

    Action getAction();

    Date getCreatedAt();

    long getMaxPosition();

    long getMinPosition();

    User[] getSources();

    int getSourcesSize();

    int getTargetObjectsSize();

    Status[] getTargetObjectStatuses();

    UserList[] getTargetObjectUserLists();

    int getTargetsSize();

    Status[] getTargetStatuses();

    UserList[] getTargetUserLists();

    User[] getTargetUsers();

    User[] getTargetObjectUsers();


    enum Action {
        FAVORITE(ACTION_FAVORITE), FOLLOW(ACTION_FOLLOW), MENTION(ACTION_MENTION), REPLY(ACTION_REPLY),
        RETWEET(ACTION_RETWEET), LIST_MEMBER_ADDED(ACTION_LIST_MEMBER_ADDED), LIST_CREATED(ACTION_LIST_CREATED),
        FAVORITED_RETWEET(ACTION_FAVORITED_RETWEET), RETWEETED_RETWEET(ACTION_RETWEETED_RETWEET),
        QUOTE(ACTION_QUOTE), RETWEETED_MENTION(ACTION_RETWEETED_MENTION),
        FAVORITED_MENTION(ACTION_FAVORITED_MENTION), JOINED_TWITTER(ACTION_JOINED_TWITTER);

        private final int actionId;

        Action(final int action) {
            actionId = action;
        }

        public static Action parse(final String string) {
            if ("favorite".equalsIgnoreCase(string)) return FAVORITE;
            if ("follow".equalsIgnoreCase(string)) return FOLLOW;
            if ("mention".equalsIgnoreCase(string)) return MENTION;
            if ("reply".equalsIgnoreCase(string)) return REPLY;
            if ("retweet".equalsIgnoreCase(string)) return RETWEET;
            if ("list_member_added".equalsIgnoreCase(string)) return LIST_MEMBER_ADDED;
            if ("list_created".equalsIgnoreCase(string)) return LIST_CREATED;
            if ("favorited_retweet".equalsIgnoreCase(string)) return FAVORITED_RETWEET;
            if ("retweeted_retweet".equalsIgnoreCase(string)) return RETWEETED_RETWEET;
            if ("quote".equalsIgnoreCase(string)) return QUOTE;
            if ("retweeted_mention".equalsIgnoreCase(string)) return RETWEETED_MENTION;
            if ("favorited_mention".equalsIgnoreCase(string)) return FAVORITED_MENTION;
            if ("joined_twitter".equalsIgnoreCase(string)) return JOINED_TWITTER;
            throw new IllegalArgumentException("Unknown action " + string);
        }

        public int getActionId() {
            return actionId;
        }
    }
}
