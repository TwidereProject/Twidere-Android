package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;

import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public abstract class TabExtras implements Parcelable {
    @CallSuper
    public void copyToBundle(Bundle bundle) {

    }
}
