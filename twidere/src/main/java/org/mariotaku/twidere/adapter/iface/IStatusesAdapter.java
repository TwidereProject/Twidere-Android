package org.mariotaku.twidere.adapter.iface;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter {

    ImageLoaderWrapper getImageLoader();

    Context getContext();

    ImageLoadingHandler getImageLoadingHandler();

    ParcelableStatus getStatus(int position);

    int getStatusCount();
}
