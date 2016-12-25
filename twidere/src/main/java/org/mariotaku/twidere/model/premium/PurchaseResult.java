package org.mariotaku.twidere.model.premium;

import android.content.Context;
import android.os.Parcelable;

/**
 * Created by mariotaku on 2016/12/25.
 */

public interface PurchaseResult extends Parcelable {
    boolean save(Context context);

    boolean load(Context context);

    boolean isValid(Context context);
}
