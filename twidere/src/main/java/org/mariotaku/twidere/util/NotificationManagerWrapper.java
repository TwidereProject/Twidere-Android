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

package org.mariotaku.twidere.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by mariotaku on 15/11/13.
 */
public class NotificationManagerWrapper {
    private final NotificationManager notificationManager;
    private final List<PostedNotification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManagerWrapper(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notify(String tag, int id, Notification notification) {
        notificationManager.notify(tag, id, notification);
        notifications.add(new PostedNotification(tag, id));
    }

    public void notify(int id, Notification notification) {
        notificationManager.notify(id, notification);
        notifications.add(new PostedNotification(null, id));
    }

    public void cancel(String tag, int id) {
        notificationManager.cancel(tag, id);
        notifications.removeAll(find(tag, id));
    }

    private List<PostedNotification> find(String tag, int id) {
        final ArrayList<PostedNotification> result = new ArrayList<>();
        for (PostedNotification notification : notifications) {
            if (notification.equals(tag, id)) {
                result.add(notification);
            }
        }
        return result;
    }

    private List<PostedNotification> findByTag(String tag) {
        final ArrayList<PostedNotification> result = new ArrayList<>();
        for (PostedNotification notification : notifications) {
            if ((tag != null ? tag.equals(notification.tag) : null == notification.tag)) {
                result.add(notification);
            }
        }
        return result;
    }

    private List<PostedNotification> findById(int id) {
        final ArrayList<PostedNotification> result = new ArrayList<>();
        for (PostedNotification notification : notifications) {
            if (id == notification.id) {
                result.add(notification);
            }
        }
        return result;
    }

    public void cancel(int id) {
        notificationManager.cancel(id);
        notifications.removeAll(find(null, id));
    }

    public void cancelById(int id) {
        final List<PostedNotification> collection = findById(id);
        for (PostedNotification notification : collection) {
            notificationManager.cancel(notification.tag, notification.id);
        }
        notificationManager.cancel(id);
        notifications.removeAll(collection);
    }

    public void cancelByTag(String tag) {
        final List<PostedNotification> collection = findByTag(tag);
        for (PostedNotification notification : collection) {
            notificationManager.cancel(notification.tag, notification.id);
        }
        notifications.removeAll(collection);
    }

    public void cancelAll() {
        notificationManager.cancelAll();
    }

    public void cancelByTag() {

    }

    private static class PostedNotification {
        private final String tag;
        private final int id;

        public PostedNotification(String tag, int id) {
            this.tag = tag;
            this.id = id;
        }

        public boolean equals(String tag, int id) {
            return id == this.id && (tag != null ? tag.equals(this.tag) : this.tag == null);
        }

        @Override
        public int hashCode() {
            int result = tag != null ? tag.hashCode() : 0;
            result = 31 * result + id;
            return result;
        }
    }
}
