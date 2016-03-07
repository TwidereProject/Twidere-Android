package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;

/**
 * Created by mariotaku on 16/2/1.
 */
@CursorObject(valuesCreator = true)
public class CachedRelationship {

    @CursorField(CachedRelationships.ACCOUNT_KEY)
    public long account_id;

    @CursorField(CachedRelationships.USER_ID)
    public long user_id;

    @CursorField(CachedRelationships.FOLLOWING)
    public boolean following;

    @CursorField(CachedRelationships.FOLLOWED_BY)
    public boolean followed_by;

    @CursorField(CachedRelationships.BLOCKING)
    public boolean blocking;

    @CursorField(CachedRelationships.BLOCKED_BY)
    public boolean blocked_by;

    @CursorField(CachedRelationships.MUTING)
    public boolean muting;

    @CursorField(CachedRelationships.RETWEET_ENABLED)
    public boolean retweet_enabled;

    public CachedRelationship() {

    }

    public CachedRelationship(long accountId, long userId, @NonNull Relationship relationship) {
        account_id = accountId;
        user_id = userId;
        following = relationship.isSourceFollowingTarget();
        following = relationship.isSourceFollowedByTarget();
        blocking = relationship.isSourceBlockingTarget();
        blocked_by = relationship.isSourceBlockedByTarget();
        muting = relationship.isSourceMutingTarget();
        retweet_enabled = relationship.isSourceWantRetweetsFromTarget();
    }
}
