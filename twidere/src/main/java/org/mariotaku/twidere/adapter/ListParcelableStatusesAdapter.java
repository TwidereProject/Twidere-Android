package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public class ListParcelableStatusesAdapter extends AbsParcelableStatusesAdapter {

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
        final View view;
        final int backgroundColor = getCardBackgroundColor();
        final LayoutInflater inflater = getInflater();
        if (compact) {
            view = inflater.inflate(R.layout.card_item_status_compact, parent, false);
            final View itemContent = view.findViewById(R.id.item_content);
            itemContent.setBackgroundColor(backgroundColor);
        } else {
            view = inflater.inflate(R.layout.card_item_status, parent, false);
            final CardView cardView = (CardView) view.findViewById(R.id.card);
            cardView.setCardBackgroundColor(backgroundColor);
        }
        final StatusViewHolder holder = new StatusViewHolder(this, view);
        holder.setOnClickListeners();
        holder.setupViewOptions();
        return holder;
    }
}
