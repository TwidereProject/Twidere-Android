package org.mariotaku.twidere.model.tab;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class TextArguments extends Arguments {
    @JsonField(name = "text")
    String text;
}
