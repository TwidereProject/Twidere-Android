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
import org.mariotaku.twidere.api.twitter.model.impl.UserMentionEntityImpl;

/**
 * A data interface representing one single user mention entity.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
@Implementation(UserMentionEntityImpl.class)
public interface UserMentionEntity {
    /**
     * Returns the index of the end character of the user mention.
     *
     * @return the index of the end character of the user mention
     */
    int getEnd();

    /**
     * Returns the user id mentioned in the status.
     *
     * @return the user id mentioned in the status
     */
    long getId();

    /**
     * Returns the name mentioned in the status.
     *
     * @return the name mentioned in the status
     */
    String getName();

    /**
     * Returns the screen name mentioned in the status.
     *
     * @return the screen name mentioned in the status
     */
    String getScreenName();

    /**
     * Returns the index of the start character of the user mention.
     *
     * @return the index of the start character of the user mention
     */
    int getStart();
}
