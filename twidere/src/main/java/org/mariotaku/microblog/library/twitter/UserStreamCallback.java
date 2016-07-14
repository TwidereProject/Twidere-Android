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

package org.mariotaku.microblog.library.twitter;

import android.text.TextUtils;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.TreeNode;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.DeletionEvent;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.StatusFavoriteEvent;
import org.mariotaku.microblog.library.twitter.model.TwitterStreamObject;
import org.mariotaku.microblog.library.twitter.model.TwitterStreamObject.Type;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.microblog.library.twitter.model.Warning;
import org.mariotaku.microblog.library.twitter.util.CRLFLineReader;
import org.mariotaku.restfu.callback.RawCallback;
import org.mariotaku.restfu.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mariotaku on 15/5/26.
 */
public abstract class UserStreamCallback implements RawCallback {

    private boolean connected;

    private boolean disconnected;

    @Override
    public final void result(final HttpResponse response) throws IOException {
        if (!response.isSuccessful()) {
            final MicroBlogException cause = new MicroBlogException();
            cause.setHttpResponse(response);
            onException(cause);
            return;
        }
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), "UTF-8"));
        try {
            for (String line; (line = reader.readLine()) != null && !disconnected; ) {
                if (!connected) {
                    onConnected();
                    connected = true;
                }
                if (TextUtils.isEmpty(line)) continue;
                final TwitterStreamObject object = LoganSquare.parse(line, TwitterStreamObject.class);
                switch (object.determine()) {
                    case Type.SENDER: {
                        break;
                    }
                    case Type.STATUS: {
                        onStatus(LoganSquareMapperFinder.mapperFor(Status.class).parse(line));
                        break;
                    }
                    case Type.DIRECT_MESSAGE: {
                        onDirectMessage(object.getDirectMessage());
                        break;
                    }
                    case Type.DELETE: {
                        final TwitterStreamObject.Delete delete = object.getDelete();
                        if (delete.getStatus() != null) {
                            onStatusDeleted(delete.getStatus());
                        } else if (delete.getDirectMessage() != null) {
                            onDirectMessageDeleted(delete.getDirectMessage());
                        }
                        break;
                    }
                    case Type.LIMIT:
                        break;
                    case Type.STALL_WARNING:
                        break;
                    case Type.SCRUB_GEO:
                        break;
                    case Type.FRIENDS:
                        break;
                    case Type.FAVORITE: {
                        StatusFavoriteEvent event = LoganSquareMapperFinder.mapperFor(StatusFavoriteEvent.class).parse(line);
                        onFavorite(event.getSource(), event.getTarget(), event.getTargetObject());
                        break;
                    }
                    case Type.UNFAVORITE: {
                        StatusFavoriteEvent event = LoganSquareMapperFinder.mapperFor(StatusFavoriteEvent.class).parse(line);
                        onUnfavorite(event.getSource(), event.getTarget(), event.getTargetObject());
                        break;
                    }
                    case Type.FOLLOW:
                        break;
                    case Type.UNFOLLOW:
                        break;
                    case Type.USER_LIST_MEMBER_ADDED:
                        break;
                    case Type.USER_LIST_MEMBER_DELETED:
                        break;
                    case Type.USER_LIST_SUBSCRIBED:
                        break;
                    case Type.USER_LIST_UNSUBSCRIBED:
                        break;
                    case Type.USER_LIST_CREATED:
                        break;
                    case Type.USER_LIST_UPDATED:
                        break;
                    case Type.USER_LIST_DESTROYED:
                        break;
                    case Type.USER_UPDATE:
                        break;
                    case Type.USER_DELETE:
                        break;
                    case Type.USER_SUSPEND:
                        break;
                    case Type.BLOCK:
                        break;
                    case Type.UNBLOCK:
                        break;
                    case Type.DISCONNECTION:
                        break;
                    case Type.UNKNOWN:
                        break;
                }
            }
        } catch (IOException e) {
            onException(e);
        } finally {
            Log.d("Twidere.Stream", "Cleaning up...");
            reader.close();
            response.close();
        }
    }


    private static <T> T parse(final Class<T> cls, final TreeNode json) throws IOException {
        return LoganSquareMapperFinder.mapperFor(cls).parse(json.traverse());
    }

    @Override
    public final void error(final Exception cause) {
        onException(cause);
    }

    public void disconnect() {
        disconnected = true;
    }

    public abstract void onConnected();

    public abstract void onStatus(Status status);

    public abstract void onDirectMessage(DirectMessage directMessage);

    public abstract void onBlock(User source, User blockedUser);

    public abstract void onDirectMessageDeleted(DeletionEvent event);

    public abstract void onStatusDeleted(DeletionEvent event);

    public abstract void onException(Throwable ex);

    public abstract void onFavorite(User source, User target, Status targetStatus);

    public abstract void onFollow(User source, User followedUser);

    public abstract void onFriendList(long[] friendIds);

    public abstract void onScrubGeo(long userId, long upToStatusId);

    public abstract void onStallWarning(Warning warn);

    public abstract void onTrackLimitationNotice(int numberOfLimitedStatuses);

    public abstract void onUnblock(User source, User unblockedUser);

    public abstract void onUnfavorite(User source, User target, Status targetStatus);

    public abstract void onUserListCreation(User listOwner, UserList list);

    public abstract void onUserListDeletion(User listOwner, UserList list);

    public abstract void onUserListMemberAddition(User addedMember, User listOwner, UserList list);

    public abstract void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list);

    public abstract void onUserListSubscription(User subscriber, User listOwner, UserList list);

    public abstract void onUserListUnsubscription(User subscriber, User listOwner, UserList list);

    public abstract void onUserListUpdate(User listOwner, UserList list);

    public abstract void onUserProfileUpdate(User updatedUser);
}
