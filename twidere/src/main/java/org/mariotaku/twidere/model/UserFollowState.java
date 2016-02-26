package org.mariotaku.twidere.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/25.
 *
 * This is a subset of {@link ParcelableUser}, which only has two fields, id and is_following,
 * to minimize processing speed.
 */
@JsonObject
public class UserFollowState {

    @JsonField(name = "id")
    public long id;

    @JsonField(name = "is_following")
    public boolean is_following;
}
