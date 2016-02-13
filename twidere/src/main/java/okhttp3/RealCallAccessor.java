package okhttp3;

import android.support.annotation.Nullable;

import okhttp3.internal.http.HttpEngine;

/**
 * Created by mariotaku on 16/2/13.
 */
public class RealCallAccessor {

    @Nullable
    public static HttpEngine getHttpEngine(Call call) {
        if (call instanceof RealCall) {
            return ((RealCall) call).engine;
        }
        return null;
    }

}
