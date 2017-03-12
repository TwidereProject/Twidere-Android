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

package org.mariotaku.microblog.library.twitter.callback;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.DeletionEvent;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.StatusTargetObjectEvent;
import org.mariotaku.microblog.library.twitter.model.StreamEvent;
import org.mariotaku.microblog.library.twitter.model.TwitterStreamObject;
import org.mariotaku.microblog.library.twitter.model.TwitterStreamObject.Type;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.microblog.library.twitter.model.UserListTargetObjectEvent;
import org.mariotaku.microblog.library.twitter.model.Warning;
import org.mariotaku.microblog.library.util.CRLFLineReader;
import org.mariotaku.restfu.callback.RawCallback;
import org.mariotaku.restfu.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/26.
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class UserStreamCallback implements RawCallback<MicroBlogException> {

    private boolean connected;

    private boolean disconnected;

    @Override
    public final void result(@NonNull final HttpResponse response) throws MicroBlogException, IOException {
        if (!response.isSuccessful()) {
            final MicroBlogException cause = new MicroBlogException();
            cause.setHttpResponse(response);
            onException(cause);
            return;
        }
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), "UTF-8"));
        try {
            for (String line; (line = reader.readLine()) != null && !disconnected; ) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (!connected) {
                    onConnected();
                    connected = true;
                }
                if (TextUtils.isEmpty(line)) continue;
                final TwitterStreamObject object = LoganSquare.parse(line, TwitterStreamObject.class);
                if (!handleEvent(object, line)) {
                    onUnhandledEvent(object, line);
                }
            }
        } catch (IOException e) {
            onException(e);
        } finally {
            reader.close();
        }
    }

    @Override
    public final void error(@NonNull final MicroBlogException cause) {
        onException(cause);
    }

    public final void disconnect() {
        disconnected = true;
    }

    private boolean handleEvent(final TwitterStreamObject object, final String json) throws IOException {
        switch (object.determine()) {
            case Type.FRIENDS: {
                return onFriendList(object.getFriends());
            }
            case Type.STATUS: {
                return onStatus(LoganSquare.parse(json, Status.class));
            }
            case Type.DIRECT_MESSAGE: {
                return onDirectMessage(object.getDirectMessage());
            }
            case Type.DELETE: {
                final TwitterStreamObject.Delete delete = object.getDelete();
                if (delete.getStatus() != null) {
                    return onStatusDeleted(delete.getStatus());
                } else if (delete.getDirectMessage() != null) {
                    return onDirectMessageDeleted(delete.getDirectMessage());
                }
                break;
            }
            case Type.LIMIT: {
                return onTrackLimitationNotice(object.getLimit().getTrack());
            }
            case Type.STALL_WARNING: {
                return onStallWarning(object.getWarning());
            }
            case Type.SCRUB_GEO: {
                TwitterStreamObject.ScrubGeo scrubGeo = object.getScrubGeo();
                return onScrubGeo(scrubGeo.getUserId(), scrubGeo.getUpToStatusId());
            }
            case Type.FAVORITE: {
                StatusTargetObjectEvent event = LoganSquare.parse(json, StatusTargetObjectEvent.class);
                return onFavorite(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.UNFAVORITE: {
                StatusTargetObjectEvent event = LoganSquare.parse(json, StatusTargetObjectEvent.class);
                return onUnfavorite(event.getSource(), event.getTarget(), event.getTargetObject());
            }
            case Type.QUOTED_TWEET: {
                StatusTargetObjectEvent event = LoganSquare.parse(json, StatusTargetObjectEvent.class);
                return onQuotedTweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.RETWEETED_RETWEET: {
                StatusTargetObjectEvent event = LoganSquare.parse(json, StatusTargetObjectEvent.class);
                return onRetweetedRetweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.FAVORITED_RETWEET: {
                StatusTargetObjectEvent event = LoganSquare.parse(json, StatusTargetObjectEvent.class);
                return onFavoritedRetweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.FOLLOW: {
                StreamEvent event = LoganSquare.parse(json, StreamEvent.class);
                return onFollow(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.UNFOLLOW: {
                StreamEvent event = LoganSquare.parse(json, StreamEvent.class);
                return onUnfollow(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.USER_LIST_MEMBER_ADDED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListMemberAddition(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_MEMBER_DELETED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListMemberDeletion(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_SUBSCRIBED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListSubscription(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_UNSUBSCRIBED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListUnsubscription(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_CREATED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListCreation(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_LIST_UPDATED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListUpdate(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_LIST_DESTROYED: {
                UserListTargetObjectEvent event = LoganSquare.parse(json, UserListTargetObjectEvent.class);
                return onUserListDeletion(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_UPDATE: {
                StreamEvent event = LoganSquare.parse(json, StreamEvent.class);
                return onUserProfileUpdate(event.getCreatedAt(), event.getSource());
            }
            case Type.BLOCK: {
                StreamEvent event = LoganSquare.parse(json, StreamEvent.class);
                return onBlock(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.UNBLOCK: {
                StreamEvent event = LoganSquare.parse(json, StreamEvent.class);
                return onUnblock(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.DISCONNECTION:
                TwitterStreamObject.Disconnect disconnect = object.getDisconnect();
                return onDisconnect(disconnect.getCode(), disconnect.getReason());
        }
        return false;
    }

    protected abstract boolean onConnected();

    protected abstract boolean onDisconnect(int code, String reason);

    protected abstract boolean onException(@NonNull Throwable ex);

    protected abstract boolean onStatus(@NonNull Status status);

    protected abstract boolean onDirectMessage(@NonNull DirectMessage directMessage);

    protected abstract boolean onBlock(Date createdAt, User source, User blockedUser);

    protected abstract boolean onDirectMessageDeleted(@NonNull DeletionEvent event);

    protected abstract boolean onStatusDeleted(@NonNull DeletionEvent event);

    protected abstract boolean onFavorite(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull Status targetObject);

    protected abstract boolean onFollow(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target);

    protected abstract boolean onUnfollow(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target);

    protected abstract boolean onFriendList(@NonNull String[] friendIds);

    protected abstract boolean onScrubGeo(String userId, String upToStatusId);

    protected abstract boolean onStallWarning(Warning warn);

    protected abstract boolean onTrackLimitationNotice(int numberOfLimitedStatuses);

    protected abstract boolean onUnblock(@NonNull Date createdAt, @NonNull User source,
            @NonNull User unblockedUser);

    protected abstract boolean onUnfavorite(@NonNull User source, @NonNull User target,
            @NonNull Status targetStatus);

    protected abstract boolean onUserListCreation(@NonNull Date createdAt, @NonNull User source,
            @NonNull UserList targetObject);

    protected abstract boolean onUserListDeletion(@NonNull Date createdAt, @NonNull User source,
            @NonNull UserList targetObject);

    protected abstract boolean onUserListMemberAddition(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull UserList targetObject);

    protected abstract boolean onUserListMemberDeletion(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull UserList targetObject);

    protected abstract boolean onUserListSubscription(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull UserList targetObject);

    protected abstract boolean onUserListUnsubscription(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull UserList targetObject);

    protected abstract boolean onUserListUpdate(@NonNull Date createdAt, @NonNull User source,
            @NonNull UserList targetObject);

    protected abstract boolean onUserProfileUpdate(@NonNull Date createdAt,
            @NonNull User updatedUser);

    protected abstract boolean onQuotedTweet(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull Status targetObject);

    protected abstract boolean onFavoritedRetweet(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull Status targetObject);

    protected abstract boolean onRetweetedRetweet(@NonNull Date createdAt, @NonNull User source,
            @NonNull User target, @NonNull Status targetObject);

    protected abstract void onUnhandledEvent(@NonNull TwitterStreamObject obj, @NonNull String json)
            throws IOException;
}
