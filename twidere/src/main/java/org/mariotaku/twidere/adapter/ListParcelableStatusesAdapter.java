package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public class ListParcelableStatusesAdapter extends ParcelableStatusesAdapter {

    public ListParcelableStatusesAdapter(Context context, boolean compact) {
        super(context, compact);
    }

    @Override
    protected int[] getProgressViewIds() {
        return new int[]{R.id.media_preview_progress};
    }

    @NonNull
    @Override
    protected IStatusViewHolder onCreateStatusViewHolder(ViewGroup parent, boolean compact) {
        return createStatusViewHolder(this, getInflater(), parent, compact,
                getCardBackgroundColor());
    }

    public static StatusViewHolder createStatusViewHolder(IStatusesAdapter<?> adapter,
                                                          LayoutInflater inflater, ViewGroup parent,
                                                          boolean compact, int cardBackgroundColor) {
        final View view;
        if (compact) {
            view = inflater.inflate(R.layout.card_item_status_compact, parent, false);
            final View itemContent = view.findViewById(R.id.item_content);
            itemContent.setBackgroundColor(cardBackgroundColor);
        } else {
            view = inflater.inflate(R.layout.card_item_status, parent, false);
            final CardView cardView = (CardView) view.findViewById(R.id.card);
            cardView.setCardBackgroundColor(cardBackgroundColor);
        }
        final StatusViewHolder holder = new StatusViewHolder(adapter, view);
        holder.setOnClickListeners();
        holder.setupViewOptions();
        return holder;
    }
}
