package org.mariotaku.twidere.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2017/2/6.
 */
@IntDef({PreviewStyle.NONE, PreviewStyle.SCALE, PreviewStyle.CROP, PreviewStyle.ACTUAL_SIZE})
@Retention(RetentionPolicy.SOURCE)
public @interface PreviewStyle {

    int NONE = 0;

    int CROP = 1;
    int SCALE = 2;
    int ACTUAL_SIZE = 3;
}
