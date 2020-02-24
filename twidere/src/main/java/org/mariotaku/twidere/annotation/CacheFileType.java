package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2017/2/4.
 */

@StringDef({CacheFileType.IMAGE, CacheFileType.VIDEO, CacheFileType.JSON})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheFileType {
    String IMAGE = "image";
    String VIDEO = "video";
    String JSON = "json";
}
