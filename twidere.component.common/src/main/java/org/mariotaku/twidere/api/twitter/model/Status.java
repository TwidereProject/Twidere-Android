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
import org.mariotaku.twidere.api.twitter.model.impl.StatusImpl;

import java.util.Date;

/**
 * A data interface representing one single status of a user.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@Implementation(StatusImpl.class)
public interface Status extends Comparable<Status>, TwitterResponse, ExtendedEntitySupport {

    /**
     * Returns an array of contributors, or null if no contributor is associated
     * with this status.
     *
     * @since Twitter4J 2.2.3
     */
    long[] getContributors();

    long getReplyCount();

    long getDescendentReplyCount();

    /**
     * returns the created_at
     *
     * @return the created_at
     */
    Date getCreatedAt();

    long getCurrentUserRetweet();

    long getFavoriteCount();

    /**
     * Returns The location that this tweet refers to if available.
     *
     * @return returns The location that this tweet refers to if available (can
     * be null)
     */
    GeoLocation getGeoLocation();

    /**
     * returns the status id of the tweet
     *
     * @return the status id
     */
    long getId();

    /**
     * Returns the in_reply_to_screen_name
     *
     * @return the in_in_reply_to_screen_name
     * @since Twitter4J 2.0.4
     */
    String getInReplyToScreenName();

    /**
     * Returns the in_reply_tostatus_id
     *
     * @return the in_reply_tostatus_id
     */
    long getInReplyToStatusId();

    /**
     * Returns the in_reply_user_id
     *
     * @return the in_reply_tostatus_id
     * @since Twitter4J 1.0.4
     */
    long getInReplyToUserId();

    /**
     * Returns the place associated with the Tweet.
     *
     * @return The place associated with the Tweet
     */
    Place getPlace();

    /**
     * Returns the number of times this tweet has been retweeted, or -1 when the
     * tweet was created before this feature was enabled.
     *
     * @return the retweet count.
     */
    long getRetweetCount();

    /**
     * @since Twitter4J 2.1.0
     */
    Status getRetweetedStatus();

    Status getQuotedStatus();

    /**
     * returns the source of the tweet
     *
     * @return the source of the tweet
     */
    String getSource();

    /**
     * returns the text
     *
     * @return the text
     */
    String getText();

    /**
     * Return the user associated with the status.<br>
     * This can be null if the instance if from User.getStatus().
     *
     * @return the user
     */
    User getUser();

    /**
     * Test if the status is favorited
     *
     * @return true if favorited
     * @since Twitter4J 1.0.4
     */
    boolean isFavorited();

    boolean isPossiblySensitive();

    /**
     * @since Twitter4J 2.0.10
     */
    boolean isRetweet();

    boolean isQuote();

    /**
     * Returns true if the authenticating user has retweeted this tweet, or
     * false when the tweet was created before this feature was enabled.
     *
     * @return whether the authenticating user has retweeted this tweet.
     * @since Twitter4J 2.1.4
     */
    boolean isRetweetedByMe();

    /**
     * Test if the status is truncated
     *
     * @return true if truncated
     * @since Twitter4J 1.0.4
     */
    boolean isTruncated();

    CardEntity getCard();

}
