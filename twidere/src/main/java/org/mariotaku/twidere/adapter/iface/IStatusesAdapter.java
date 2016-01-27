package org.mariotaku.twidere.adapter.iface;

import android.support.annotation.Nullable;
import android.view.View;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IStatusesAdapter<Data> extends IContentCardAdapter, IGapSupportedAdapter {

    int getLinkHighlightingStyle();

    @PreviewStyle
    int getMediaPreviewStyle();

    ParcelableStatus getStatus(int position);

    long getStatusId(int position);

    long getAccountId(int position);

    @Nullable
    ParcelableStatus findStatusById(long accountId, long statusId);

    int getStatusesCount();

    TwidereLinkify getTwidereLinkify();

    boolean isCardActionsHidden();

    boolean isMediaPreviewEnabled();

    boolean isNameFirst();

    boolean isSensitiveContentEnabled();

    void setData(Data data);

    boolean shouldShowAccountsColor();

    boolean shouldUseStarsForLikes();

    MediaLoadingHandler getMediaLoadingHandler();

    @Nullable
    IStatusViewHolder.StatusClickListener getStatusClickListener();

    @Nullable
    StatusAdapterListener getStatusAdapterListener();

    interface StatusAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition);

        void onStatusActionClick(IStatusViewHolder holder, int id, int position);

        void onStatusClick(IStatusViewHolder holder, int position);

        boolean onStatusLongClick(IStatusViewHolder holder, int position);

        void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position);

        void onUserProfileClick(IStatusViewHolder holder, ParcelableStatus status, int position);
    }
}
