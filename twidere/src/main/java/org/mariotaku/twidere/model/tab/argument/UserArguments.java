package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class UserArguments extends TabArguments {
    @JsonField(name = "user_id")
    String userId;

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putString(EXTRA_USER_ID, userId);
    }

    @Override
    public String toString() {
        return "UserArguments{" +
                "userId=" + userId +
                "} " + super.toString();
    }
}
