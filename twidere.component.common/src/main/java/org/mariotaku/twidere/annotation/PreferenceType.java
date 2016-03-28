package org.mariotaku.twidere.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/1/8.
 */
@IntDef({PreferenceType.BOOLEAN, PreferenceType.INT, PreferenceType.LONG, PreferenceType.FLOAT,
        PreferenceType.STRING, PreferenceType.NULL, PreferenceType.INVALID})
@Retention(RetentionPolicy.SOURCE)
public @interface PreferenceType {
    int BOOLEAN = 1, INT = 2, LONG = 3, FLOAT = 4, STRING = 5, NULL = 0, INVALID = -1;
}
