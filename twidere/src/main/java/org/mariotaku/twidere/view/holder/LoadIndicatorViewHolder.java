package org.mariotaku.twidere.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14/11/19.
 */
public class LoadIndicatorViewHolder extends RecyclerView.ViewHolder {
    private final View loadProgress;

    public LoadIndicatorViewHolder(View view) {
        super(view);
        loadProgress = view.findViewById(R.id.load_progress);
    }

    public void setLoadProgressVisible(boolean visible) {
        loadProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
