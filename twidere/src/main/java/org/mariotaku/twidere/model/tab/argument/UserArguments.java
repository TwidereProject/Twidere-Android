package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class UserArguments extends TabArguments {
    @JsonField(name = "user_id")
    String userId;
    @JsonField(name = "user_key")
    UserKey userKey;

    public void setUserKey(UserKey userKey) {
        this.userKey = userKey;
    }

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        if (userKey == null) {
            bundle.putParcelable(EXTRA_USER_KEY, UserKey.valueOf(userId));
        } else {
            bundle.putParcelable(EXTRA_USER_KEY, userKey);
        }
    }

    @Override
    public String toString() {
        return "UserArguments{" +
                "userId='" + userId + '\'' +
                ", userKey=" + userKey +
                "} " + super.toString();
    }
}
