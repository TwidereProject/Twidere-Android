package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.microblog.library.twitter.model.Relationship;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;

/**
 * Created by mariotaku on 16/2/1.
 */
@CursorObject(valuesCreator = true, tableInfo = true)
public class CachedRelationship {

    @CursorField(value = CachedRelationships.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(value = CachedRelationships.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey user_key;

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

    @CursorField(value = CachedRelationships._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;

    public CachedRelationship() {

    }

    public CachedRelationship(@Nullable Relationship relationship, @NonNull UserKey accountKey, @NonNull UserKey userKey) {
        account_key = accountKey;
        user_key = userKey;
        if (relationship != null) {
            following = relationship.isSourceFollowingTarget();
            followed_by = relationship.isSourceFollowedByTarget();
            blocking = relationship.isSourceBlockingTarget();
            blocked_by = relationship.isSourceBlockedByTarget();
            muting = relationship.isSourceMutingTarget();
            retweet_enabled = relationship.isSourceWantRetweetsFromTarget();
        }
    }
}
