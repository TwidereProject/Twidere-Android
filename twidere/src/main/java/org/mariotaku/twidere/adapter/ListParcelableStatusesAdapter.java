package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
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

    public ListParcelableStatusesAdapter(Context context) {
        super(context);
    }

    @Override
    protected int[] getProgressViewIds() {
        return new int[]{R.id.media_preview_progress};
    }

    @NonNull
    @Override
    protected IStatusViewHolder onCreateStatusViewHolder(ViewGroup parent) {
        return createStatusViewHolder(this, getInflater(), parent, getCardBackgroundColor());
    }

    public static StatusViewHolder createStatusViewHolder(IStatusesAdapter<?> adapter,
                                                          LayoutInflater inflater, ViewGroup parent,
                                                          int cardBackgroundColor) {
        final View view = inflater.inflate(R.layout.card_item_status_compact, parent, false);
        final View itemContent = view.findViewById(R.id.item_content);
        itemContent.setBackgroundColor(cardBackgroundColor);
        final StatusViewHolder holder = new StatusViewHolder(adapter, view);
        holder.setOnClickListeners();
        holder.setupViewOptions();
        return holder;
    }
}
