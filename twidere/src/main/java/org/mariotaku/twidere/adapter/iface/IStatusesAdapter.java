package org.mariotaku.twidere.adapter.iface;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter<Data> extends IContentCardAdapter {

    ParcelableStatus getStatus(int position);

    int getStatusCount();

    void setData(Data data);

    void onUserProfileClick(StatusViewHolder holder, int position);

    void onStatusClick(StatusViewHolder holder, int position);
}
