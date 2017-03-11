package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class StatusTargetObjectEvent extends StreamEvent {
    @JsonField(name = "target_object")
    Status targetObject;

    public Status getTargetObject() {
        return targetObject;
    }

    @Override
    public String toString() {
        return "StatusTargetObjectEvent{" +
                "targetObject=" + targetObject +
                "} " + super.toString();
    }
}
