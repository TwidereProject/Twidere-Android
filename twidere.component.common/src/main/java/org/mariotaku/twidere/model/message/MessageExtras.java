package org.mariotaku.twidere.model.message;

import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 2017/2/9.
 */

@JsonObject
public abstract class MessageExtras implements Parcelable {
    public static MessageExtras parse(final String messageType, final String json) {
        return null;
    }
}
