package org.mariotaku.twidere.annotation;

import android.support.annotation.IntDef;

import org.mariotaku.twidere.model.ParcelableCredentials;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/4.
 */
@IntDef({AuthTypeInt.OAUTH, AuthTypeInt.XAUTH, AuthTypeInt.BASIC, AuthTypeInt.TWIP_O_MODE,
        AuthTypeInt.OAUTH2})
@Retention(RetentionPolicy.SOURCE)
public @interface AuthTypeInt {

    int OAUTH = 0;
    int XAUTH = 1;
    int BASIC = 2;
    int TWIP_O_MODE = 3;
    int OAUTH2 = 4;
}
