package org.mariotaku.twidere.adapter.iface;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.view.holder.StatusViewHolder.StatusClickListener;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter<Data> extends IContentCardAdapter, StatusClickListener {

    ParcelableStatus getStatus(int position);

    int getStatusCount();

    long getStatusId(int position);

    boolean isMediaPreviewEnabled();

    boolean isNameFirst();

    boolean isNicknameOnly();

    void setData(Data data);

    boolean shouldShowAccountsColor();
}
