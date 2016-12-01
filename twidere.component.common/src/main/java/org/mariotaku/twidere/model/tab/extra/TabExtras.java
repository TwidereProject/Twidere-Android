package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.twidere.annotation.CustomTabType;

import java.io.IOException;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public abstract class TabExtras implements Parcelable {
    @CallSuper
    public void copyToBundle(Bundle bundle) {

    }

    @Nullable
    public static TabExtras parse(@NonNull @CustomTabType String type, String json) throws IOException {
        switch (type) {
            case CustomTabType.NOTIFICATIONS_TIMELINE: {
                return LoganSquareMapperFinder.mapperFor(InteractionsTabExtras.class).parse(json);
            }
            case CustomTabType.HOME_TIMELINE: {
                return LoganSquareMapperFinder.mapperFor(HomeTabExtras.class).parse(json);
            }
        }
        return null;
    }
}
