/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.DummyStatusHolderAdapter;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 15/3/17.
 */
public class ViewStatusDialogFragment extends BaseSupportDialogFragment {

    private DummyStatusHolderAdapter mAdapter;
    private IStatusViewHolder mHolder;
    private View mStatusContainer;

    public ViewStatusDialogFragment() {
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
        if (getShowsDialog()) {
            return inflater.inflate(R.layout.dialog_scrollable_status, parent, false);
        }
        return inflater.inflate(R.layout.fragment_scrollable_status, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStatusContainer = view.findViewById(R.id.status_container);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
            dismiss();
            return;
        }
        final View view = getView();
        if (view == null) throw new AssertionError();
        final FragmentActivity activity = getActivity();
        mAdapter = new DummyStatusHolderAdapter(activity);
        mHolder = new StatusViewHolder(mAdapter, getView());
        final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
        if (args.containsKey(EXTRA_SHOW_MEDIA_PREVIEW)) {
            mAdapter.setMediaPreviewEnabled(args.getBoolean(EXTRA_SHOW_MEDIA_PREVIEW));
        }
        mHolder.displayStatus(status, null, false, true);
        mStatusContainer.findViewById(R.id.item_menu).setVisibility(View.GONE);
        mStatusContainer.findViewById(R.id.action_buttons).setVisibility(View.GONE);
    }


}
