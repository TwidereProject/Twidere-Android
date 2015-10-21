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

package org.mariotaku.twidere.api.twitter.model.impl;

import android.support.annotation.NonNull;

import org.mariotaku.library.logansquare.extension.annotation.Mapper;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

@Mapper(ActivityImplMapper.class)
public class ActivityImpl extends TwitterResponseImpl implements Activity {

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    Action action;

    Date createdAt;

    User[] sources;
    User[] targetUsers;
    User[] targetObjectUsers;
    Status[] targetObjectStatuses, targetStatuses;
    UserList[] targetUserLists, targetObjectUserLists;
    long maxPosition, minPosition;
    int targetObjectsSize, targetsSize, sourcesSize;

    ActivityImpl() {
    }

    @Override
    public User[] getTargetObjectUsers() {
        return targetObjectUsers;
    }

    @Override
    public int compareTo(@NonNull final Activity another) {
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

}