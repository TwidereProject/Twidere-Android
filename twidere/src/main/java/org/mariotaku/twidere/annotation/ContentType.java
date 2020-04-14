package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/15.
 */

@StringDef({ContentType.STATUS, ContentType.USER})
@Retention(RetentionPolicy.SOURCE)
public @interface ContentType {
    String STATUS = "status";
    String USER = "user";
}
