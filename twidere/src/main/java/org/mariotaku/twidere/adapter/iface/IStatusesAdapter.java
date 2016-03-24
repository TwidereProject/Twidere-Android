package org.mariotaku.twidere.adapter.iface;

import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter<Data> extends IContentCardAdapter, IGapSupportedAdapter {

    int getLinkHighlightingStyle();

    @PreviewStyle
    int getMediaPreviewStyle();

    ParcelableStatus getStatus(int position);

    @Nullable
    String getStatusId(int position);

    long getStatusTimestamp(int adapterPosition);

    long getStatusPositionKey(int adapterPosition);

    @Nullable
    UserKey getAccountKey(int position);

    @Nullable
    ParcelableStatus findStatusById(UserKey accountKey, String statusId);

    int getStatusCount();

    int getRawStatusCount();

    TwidereLinkify getTwidereLinkify();

    boolean isCardActionsShown(int position);

    void showCardActions(int position);

    boolean isMediaPreviewEnabled();

    boolean isNameFirst();

    boolean isSensitiveContentEnabled();

    boolean setData(Data data);

    boolean shouldShowAccountsColor();

    boolean shouldUseStarsForLikes();

    MediaLoadingHandler getMediaLoadingHandler();

    @Nullable
    IStatusViewHolder.StatusClickListener getStatusClickListener();

}
