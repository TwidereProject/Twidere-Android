package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;

/**
 * Created by mariotaku on 16/2/1.
 */
@ParcelablePlease
@CursorObject(valuesCreator = true, tableInfo = true)
public class ParcelableRelationship implements Parcelable {

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

    public boolean can_dm;

    public boolean filtering;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableRelationshipParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableRelationship> CREATOR = new Creator<ParcelableRelationship>() {
        public ParcelableRelationship createFromParcel(Parcel source) {
            ParcelableRelationship target = new ParcelableRelationship();
            ParcelableRelationshipParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableRelationship[] newArray(int size) {
            return new ParcelableRelationship[size];
        }
    };
}
