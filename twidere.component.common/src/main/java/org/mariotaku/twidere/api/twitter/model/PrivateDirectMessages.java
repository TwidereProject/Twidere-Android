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

/**
 * Created by mariotaku on 15/7/5.
 */
public interface PrivateDirectMessages {

    UserEvents getUserEvents();

    interface UserInbox {

        User getUser(long userId);

        Conversation getConversation(String conversationId);

        Message[] getEntries();
    }

    interface UserEvents {
        String getCursor();
        long getLastSeenEventId();
    }

    UserInbox getUserInbox();

    interface Message {
        interface Data extends EntitySupport {

            String getText();

            String getConversationId();

            long getId();

            long getRecipientId();

            long getSenderId();

            long getTime();
        }
    }

    interface Conversation {
        Participant[] getParticipants();

        String getConversationId();

        long getLastReadEventId();

        long getMaxEntryId();

        long getMinEntryId();

        boolean isNotificationsDisabled();


        interface Participant {

            long getUserId();
        }

        enum Type {
            ONE_TO_ONE, GROUP_DM
        }
    }

    enum Status {
        HAS_MORE, AT_END
    }

}
