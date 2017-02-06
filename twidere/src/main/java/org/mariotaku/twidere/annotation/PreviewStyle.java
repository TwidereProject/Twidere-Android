package org.mariotaku.twidere.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2017/2/6.
 */
@IntDef({PreviewStyle.NONE, PreviewStyle.SCALE, PreviewStyle.CROP, PreviewStyle.REAL_SIZE})
@Retention(RetentionPolicy.SOURCE)
public @interface PreviewStyle {

    int NONE = 0;

    int CROP = 1;
    int SCALE = 2;
    int REAL_SIZE = 3;
}
