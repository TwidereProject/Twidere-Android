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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;

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
            Log.d("Twidere.Stream", "Cleaning up...");
            reader.close();
        }
    }

    private boolean handleEvent(final TwitterStreamObject object, final String json) throws IOException {
        switch (object.determine()) {
            case Type.SENDER: {
                break;
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
            case Type.STALL_WARNING:
                break;
            case Type.SCRUB_GEO: {
                TwitterStreamObject.ScrubGeo scrubGeo = object.getScrubGeo();
                return onScrubGeo(scrubGeo.getUserId(), scrubGeo.getUpToStatusId());
            }
            case Type.FRIENDS: {
                return onFriendList(object.getFriends());
            }
            case Type.FAVORITE: {
                StatusFavoriteEvent event = LoganSquare.parse(json, StatusFavoriteEvent.class);
                return onFavorite(event.getSource(), event.getTarget(), event.getTargetObject());
            }
            case Type.UNFAVORITE: {
                StatusFavoriteEvent event = LoganSquare.parse(json, StatusFavoriteEvent.class);
                return onUnfavorite(event.getSource(), event.getTarget(), event.getTargetObject());
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
                TwitterStreamObject.Disconnect disconnect = object.getDisconnect();
                return onDisconnect(disconnect.getCode(), disconnect.getReason());
            case Type.UNKNOWN:
                break;
        }
        return false;
    }


    @Override
    public final void error(@NonNull final MicroBlogException cause) {
        onException(cause);
    }

    public void disconnect() {
        disconnected = true;
    }

    protected boolean onConnected() {
        return false;
    }

    protected boolean onDisconnect(int code, String reason) {
        return false;
    }

    protected boolean onStatus(@NonNull Status status) {
        return false;
    }

    protected boolean onDirectMessage(@NonNull DirectMessage directMessage) {
        return false;
    }

    protected boolean onBlock(User source, User blockedUser) {
        return false;
    }

    protected boolean onDirectMessageDeleted(@NonNull DeletionEvent event) {
        return false;
    }

    protected boolean onStatusDeleted(@NonNull DeletionEvent event) {
        return false;
    }

    protected boolean onException(@NonNull Throwable ex) {
        return false;
    }

    protected boolean onFavorite(@NonNull User source, @NonNull User target, @NonNull Status targetStatus) {
        return false;
    }

    protected boolean onFollow(User source, User followedUser) {
        return false;
    }

    protected boolean onFriendList(@NonNull String[] friendIds) {
        return false;
    }

    protected boolean onScrubGeo(String userId, String upToStatusId) {
        return false;
    }

    protected boolean onStallWarning(Warning warn) {
        return false;
    }

    protected boolean onTrackLimitationNotice(int numberOfLimitedStatuses) {
        return false;
    }

    protected boolean onUnblock(User source, User unblockedUser) {
        return false;
    }

    protected boolean onUnfavorite(User source, User target, Status targetStatus) {
        return false;
    }

    protected boolean onUserListCreation(User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListDeletion(User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListSubscription(User subscriber, User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserListUpdate(User listOwner, UserList list) {
        return false;
    }

    protected boolean onUserProfileUpdate(User updatedUser) {
        return false;
    }

    protected void onUnhandledEvent(@NonNull final TwitterStreamObject obj, @NonNull final String json) throws IOException {
    }
}
