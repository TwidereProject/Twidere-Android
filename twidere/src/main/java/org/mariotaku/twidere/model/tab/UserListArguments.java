package org.mariotaku.twidere.model.tab;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class UserListArguments extends Arguments {
    @JsonField(name = "list_id")
    long listId;
}
