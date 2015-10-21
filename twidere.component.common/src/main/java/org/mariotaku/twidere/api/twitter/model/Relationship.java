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

import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.library.logansquare.extension.annotation.Wrapper;
import org.mariotaku.twidere.api.twitter.model.impl.RelationshipImpl;
import org.mariotaku.twidere.api.twitter.model.impl.RelationshipWrapper;

@Wrapper(RelationshipWrapper.class)
@Implementation(RelationshipImpl.class)
public interface Relationship extends TwitterResponse {
    boolean canSourceDMTarget();

    boolean canSourceMediaTagTarget();

    /**
     * Returns the source user id
     *
     * @return the source user id
     */
    long getSourceUserId();

    /**
     * Returns the source user screen name
     *
     * @return returns the source user screen name
     */
    String getSourceUserScreenName();

    /**
     * Returns the target user id
     *
     * @return target user id
     */
    long getTargetUserId();

    /**
     * Returns the target user screen name
     *
     * @return the target user screen name
     */
    String getTargetUserScreenName();

    /**
     * Returns if the source user is blocking the target user
     *
     * @return if the source is blocking the target
     */
    boolean isSourceBlockingTarget();

    boolean isSourceBlockedByTarget();

    /**
     * Checks if source user is being followed by target user
     *
     * @return true if source user is being followed by target user
     */
    boolean isSourceFollowedByTarget();

    /**
     * Checks if source user is following target user
     *
     * @return true if source user is following target user
     */
    boolean isSourceFollowingTarget();

    boolean isSourceMarkedTargetAsSpam();

    boolean isSourceMutingTarget();

    /**
     * Checks if the source user has enabled notifications for updates of the
     * target user
     *
     * @return true if source user enabled notifications for target user
     */
    boolean isSourceNotificationsEnabled();

    /**
     * Checks if target user is being followed by source user.<br>
     * This method is equivalent to isSourceFollowingTarget().
     *
     * @return true if target user is being followed by source user
     */
    boolean isTargetFollowedBySource();

    /**
     * Checks if target user is following source user.<br>
     * This method is equivalent to isSourceFollowedByTarget().
     *
     * @return true if target user is following source user
     */
    boolean isTargetFollowingSource();

    boolean isSourceRequestedFollowingTarget();

    boolean isTargetRequestedFollowingSource();

    boolean isSourceWantRetweetsFromTarget();

    boolean isSourceNotificationsEnabledForTarget();
}
