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

package org.mariotaku.twidere.api.twitter;

import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.simple.tree.SimpleTreeCodec;

import org.mariotaku.restfu.callback.RawCallback;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusDeletionNotice;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.Warning;
import org.mariotaku.twidere.api.twitter.util.CRLFLineReader;
import org.mariotaku.twidere.api.twitter.util.JSONObjectType;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mariotaku on 15/5/26.
 */
public abstract class UserStreamCallback implements RawCallback {

    private boolean connected;

    private boolean disconnected;

    @Override
    public final void result(final RestHttpResponse response) throws IOException {
        if (!response.isSuccessful()) {
            final TwitterException cause = new TwitterException();
            cause.setResponse(response);
            onException(cause);
            return;
        }
        final SimpleTreeCodec mapper = new SimpleTreeCodec();
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), "UTF-8"));
        try {
            for (String line; (line = reader.readLine()) != null && !disconnected; ) {
                if (!connected) {
                    onConnected();
                    connected = true;
                }
                if (line.isEmpty()) continue;
                TreeNode rootNode = mapper.readTree(LoganSquare.JSON_FACTORY.createParser(line));
                switch (JSONObjectType.determine(rootNode)) {
                    case SENDER: {
                        break;
                    }
                    case STATUS: {
                        onStatus(LoganSquare.mapperFor(Status.class).parse(rootNode.traverse()));
                        break;
                    }
                    case DIRECT_MESSAGE: {
                        onDirectMessage(LoganSquare.mapperFor(DirectMessage.class).parse(rootNode.traverse()));
                        break;
                    }
                    case DELETE: {
                        break;
                    }
                    case LIMIT:
                        break;
                    case STALL_WARNING:
                        break;
                    case SCRUB_GEO:
                        break;
                    case FRIENDS:
                        break;
                    case FAVORITE: {
                        onFavorite(parse(User.class, rootNode.get("source")),
                                parse(User.class, rootNode.get("target")),
                                parse(Status.class, rootNode.get("target_object")));
                        break;
                    }
                    case UNFAVORITE: {
                        onUnfavorite(parse(User.class, rootNode.get("source")),
                                parse(User.class, rootNode.get("target")),
                                parse(Status.class, rootNode.get("target_object")));
                        break;
                    }
                    case FOLLOW:
                        break;
                    case UNFOLLOW:
                        break;
                    case USER_LIST_MEMBER_ADDED:
                        break;
                    case USER_LIST_MEMBER_DELETED:
                        break;
                    case USER_LIST_SUBSCRIBED:
                        break;
                    case USER_LIST_UNSUBSCRIBED:
                        break;
                    case USER_LIST_CREATED:
                        break;
                    case USER_LIST_UPDATED:
                        break;
                    case USER_LIST_DESTROYED:
                        break;
                    case USER_UPDATE:
                        break;
                    case USER_DELETE:
                        break;
                    case USER_SUSPEND:
                        break;
                    case BLOCK:
                        break;
                    case UNBLOCK:
                        break;
                    case DISCONNECTION:
                        break;
                    case UNKNOWN:
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
        return LoganSquare.mapperFor(cls).parse(json.traverse());
    }

    @Override
    public final void error(final Throwable cause) {
        onException(cause);
    }

    public void disconnect() {
        disconnected = true;
    }

    public abstract void onConnected();

    public abstract void onBlock(User source, User blockedUser);

    public abstract void onDeletionNotice(long directMessageId, long userId);

    public abstract void onDeletionNotice(StatusDeletionNotice statusDeletionNotice);

    public abstract void onDirectMessage(DirectMessage directMessage);

    public abstract void onException(Throwable ex);

    public abstract void onFavorite(User source, User target, Status favoritedStatus);

    public abstract void onFollow(User source, User followedUser);

    public abstract void onFriendList(long[] friendIds);

    public abstract void onScrubGeo(long userId, long upToStatusId);

    public abstract void onStallWarning(Warning warn);

    public abstract void onStatus(Status status);

    public abstract void onTrackLimitationNotice(int numberOfLimitedStatuses);

    public abstract void onUnblock(User source, User unblockedUser);

    public abstract void onUnfavorite(User source, User target, Status unfavoritedStatus);

    public abstract void onUserListCreation(User listOwner, UserList list);

    public abstract void onUserListDeletion(User listOwner, UserList list);

    public abstract void onUserListMemberAddition(User addedMember, User listOwner, UserList list);

    public abstract void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list);

    public abstract void onUserListSubscription(User subscriber, User listOwner, UserList list);

    public abstract void onUserListUnsubscription(User subscriber, User listOwner, UserList list);

    public abstract void onUserListUpdate(User listOwner, UserList list);

    public abstract void onUserProfileUpdate(User updatedUser);
}
