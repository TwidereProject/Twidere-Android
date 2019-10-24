/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.callback;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), StandardCharsets.UTF_8));
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
                final TwitterStreamObject object = JsonSerializer.parse(line, TwitterStreamObject.class);
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
                return onStatus(JsonSerializer.parse(json, Status.class));
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
                StatusTargetObjectEvent event = JsonSerializer.parse(json, StatusTargetObjectEvent.class);
                return onFavorite(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.UNFAVORITE: {
                StatusTargetObjectEvent event = JsonSerializer.parse(json, StatusTargetObjectEvent.class);
                return onUnfavorite(event.getSource(), event.getTarget(), event.getTargetObject());
            }
            case Type.QUOTED_TWEET: {
                StatusTargetObjectEvent event = JsonSerializer.parse(json, StatusTargetObjectEvent.class);
                return onQuotedTweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.RETWEETED_RETWEET: {
                StatusTargetObjectEvent event = JsonSerializer.parse(json, StatusTargetObjectEvent.class);
                return onRetweetedRetweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.FAVORITED_RETWEET: {
                StatusTargetObjectEvent event = JsonSerializer.parse(json, StatusTargetObjectEvent.class);
                return onFavoritedRetweet(event.getCreatedAt(), event.getSource(), event.getTarget(),
                        event.getTargetObject());
            }
            case Type.FOLLOW: {
                StreamEvent event = JsonSerializer.parse(json, StreamEvent.class);
                return onFollow(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.UNFOLLOW: {
                StreamEvent event = JsonSerializer.parse(json, StreamEvent.class);
                return onUnfollow(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.USER_LIST_MEMBER_ADDED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListMemberAddition(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_MEMBER_DELETED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListMemberDeletion(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_SUBSCRIBED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListSubscription(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_UNSUBSCRIBED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListUnsubscription(event.getCreatedAt(), event.getSource(),
                        event.getTarget(), event.getTargetObject());
            }
            case Type.USER_LIST_CREATED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListCreation(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_LIST_UPDATED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListUpdate(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_LIST_DESTROYED: {
                UserListTargetObjectEvent event = JsonSerializer.parse(json, UserListTargetObjectEvent.class);
                return onUserListDeletion(event.getCreatedAt(), event.getSource(),
                        event.getTargetObject());
            }
            case Type.USER_UPDATE: {
                StreamEvent event = JsonSerializer.parse(json, StreamEvent.class);
                return onUserProfileUpdate(event.getCreatedAt(), event.getSource());
            }
            case Type.BLOCK: {
                StreamEvent event = JsonSerializer.parse(json, StreamEvent.class);
                return onBlock(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.UNBLOCK: {
                StreamEvent event = JsonSerializer.parse(json, StreamEvent.class);
                return onUnblock(event.getCreatedAt(), event.getSource(), event.getTarget());
            }
            case Type.DISCONNECTION:
                TwitterStreamObject.Disconnect disconnect = object.getDisconnect();
                return onDisconnectNotice(disconnect.getCode(), disconnect.getReason());
        }
        return false;
    }

    protected abstract boolean onConnected();

    protected abstract boolean onDisconnectNotice(int code, String reason);

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
