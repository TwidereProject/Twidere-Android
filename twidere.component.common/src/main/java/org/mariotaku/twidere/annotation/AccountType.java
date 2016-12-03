package org.mariotaku.twidere.annotation;

import android.support.annotation.StringDef;

import org.mariotaku.twidere.model.ParcelableAccount;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/3.
 */
@StringDef({AccountType.TWITTER, AccountType.STATUSNET, AccountType.FANFOU})
@Retention(RetentionPolicy.SOURCE)
public @interface AccountType {
    String TWITTER = "twitter";
    String STATUSNET = "statusnet";
    String FANFOU = "fanfou";
}
