package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class StatusFavoriteEvent {
    @JsonField(name = "source")
    User source;
    @JsonField(name = "target")
    User target;
    @JsonField(name = "target_object")
    Status targetObject;

    public User getSource() {
        return source;
    }

    public User getTarget() {
        return target;
    }

    public Status getTargetObject() {
        return targetObject;
    }
}
