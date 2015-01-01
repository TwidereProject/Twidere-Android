package org.mariotaku.twidere.adapter.iface;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter<Data> extends IGapSupportedAdapter, ICardSupportedAdapter {

    ImageLoaderWrapper getImageLoader();

    Context getContext();

    ImageLoadingHandler getImageLoadingHandler();

    ParcelableStatus getStatus(int position);

    int getStatusCount();

    int getProfileImageStyle();

    int getMediaPreviewStyle();

    void onStatusClick(StatusViewHolder holder, int position);

    void onUserProfileClick(StatusViewHolder holder, int position);

    void setData(Data data);

    AsyncTwitterWrapper getTwitterWrapper();

    float getTextSize();
}
