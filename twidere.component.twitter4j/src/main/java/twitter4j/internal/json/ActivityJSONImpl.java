/*
 * Twidere - Twitter client for Android
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

package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import twitter4j.Activity;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getDate;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

class ActivityJSONImpl extends TwitterResponseImpl implements Activity {

    /**
     *
     */
    private static final long serialVersionUID = -8200474717252861878L;

    private Action action;

    private Date createdAt;

    private User[] sources, targetUsers;

    private Status[] targetObjectStatuses, targetStatuses;

    private UserList[] targetUserLists, targetObjectUserLists;

    private long maxPosition, minPosition;

    private int targetObjectsSize, targetsSize, sourcesSize;

    /* package */ActivityJSONImpl(final JSONObject json) throws TwitterException {
        super();
        init(json);
    }

    @Override
    public int compareTo(final Activity another) {
        if (another == null) return 0;
        final Date thisDate = getCreatedAt(), thatDate = another.getCreatedAt();
        if (thisDate == null || thatDate == null) return 0;
        return thisDate.compareTo(thatDate);
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public long getMaxPosition() {
        return maxPosition;
    }

    @Override
    public long getMinPosition() {
        return minPosition;
    }

    @Override
    public User[] getSources() {
        return sources;
    }

    @Override
    public int getSourcesSize() {
        return sourcesSize;
    }

    @Override
    public int getTargetObjectsSize() {
        return targetObjectsSize;
    }

    @Override
    public Status[] getTargetObjectStatuses() {
        return targetObjectStatuses;
    }

    @Override
    public UserList[] getTargetObjectUserLists() {
        return targetObjectUserLists;
    }

    @Override
    public int getTargetsSize() {
        return targetsSize;
    }

    @Override
    public Status[] getTargetStatuses() {
        return targetStatuses;
    }

    @Override
    public UserList[] getTargetUserLists() {
        return targetUserLists;
    }

    @Override
    public User[] getTargetUsers() {
        return targetUsers;
    }

    @Override
    public String toString() {
        return "ActivityJSONImpl{" +
                "action=" + action +
                ", createdAt=" + createdAt +
                ", sources=" + Arrays.toString(sources) +
                ", targetUsers=" + Arrays.toString(targetUsers) +
                ", targetObjectStatuses=" + Arrays.toString(targetObjectStatuses) +
                ", targetStatuses=" + Arrays.toString(targetStatuses) +
                ", targetUserLists=" + Arrays.toString(targetUserLists) +
                ", targetObjectUserLists=" + Arrays.toString(targetObjectUserLists) +
                ", maxPosition=" + maxPosition +
                ", minPosition=" + minPosition +
                ", targetObjectsSize=" + targetObjectsSize +
                ", targetsSize=" + targetsSize +
                ", sourcesSize=" + sourcesSize +
                '}';
    }

    final void init(final JSONObject json) throws TwitterException {
        try {
            action = Action.fromString(getRawString("action", json));
            maxPosition = getLong("max_position", json);
            minPosition = getLong("min_position", json);
            createdAt = getDate("created_at", json, "EEE MMM dd HH:mm:ss z yyyy");
            sourcesSize = getInt("sources_size", json);
            targetsSize = getInt("targets_size", json);
            final JSONArray sources_array = json.getJSONArray("sources");
            final JSONArray targets_array = json.getJSONArray("targets");
            final int sources_size = sources_array.length();
            final int targets_size = targets_array.length();
            if (action == Action.LIST_CREATED) {

            } else if (action == Action.FOLLOW || action == Action.MENTION || action == Action.LIST_MEMBER_ADDED) {
                targetUsers = new User[targets_size];
                for (int i = 0; i < targets_size; i++) {
                    targetUsers[i] = new UserJSONImpl(targets_array.getJSONObject(i));
                }
            } else {
                targetStatuses = new Status[targets_size];
                for (int i = 0; i < targets_size; i++) {
                    targetStatuses[i] = new StatusJSONImpl(targets_array.getJSONObject(i));
                }
            }
            sources = new User[sources_size];
            for (int i = 0; i < sources_size; i++) {
                sources[i] = new UserJSONImpl(sources_array.getJSONObject(i));
            }
            final JSONArray target_objects_array = json.getJSONArray("target_objects");
            final int target_objects_size = target_objects_array.length();
            if (action == Action.LIST_MEMBER_ADDED) {
                targetObjectUserLists = new UserList[target_objects_size];
                for (int i = 0; i < target_objects_size; i++) {
                    targetObjectUserLists[i] = new UserListJSONImpl(target_objects_array.getJSONObject(i));
                }
            } else if (action == Action.LIST_CREATED) {
                targetUserLists = new UserList[targets_size];
                for (int i = 0; i < targets_size; i++) {
                    targetUserLists[i] = new UserListJSONImpl(targets_array.getJSONObject(i));
                }
            } else {
                targetObjectStatuses = new Status[target_objects_size];
                for (int i = 0; i < target_objects_size; i++) {
                    targetObjectStatuses[i] = new StatusJSONImpl(target_objects_array.getJSONObject(i));
                }
            }
            targetObjectsSize = getInt("target_objects_size", json);
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }

    /* package */
    static ResponseList<Activity> createActivityList(final HttpResponse res, final Configuration conf)
            throws TwitterException {
        return createActivityList(res.asJSONArray(), res, conf);
    }

    /* package */
    static ResponseList<Activity> createActivityList(final JSONArray list, final HttpResponse res,
                                                     final Configuration conf) throws TwitterException {
        try {
            final int size = list.length();
            final ResponseList<Activity> users = new ResponseListImpl<Activity>(size, res);
            for (int i = 0; i < size; i++) {
                final JSONObject json = list.getJSONObject(i);
                final Activity activity = new ActivityJSONImpl(json);
                users.add(activity);
            }
            return users;
        } catch (final JSONException jsone) {
            throw new TwitterException(jsone);
        }
    }
}
