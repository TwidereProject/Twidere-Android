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

package org.mariotaku.twidere.util;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/4/6.
 */
public class StatusAdapterLinkClickHandler<D> extends OnLinkClickHandler {

    private final IStatusesAdapter<D> adapter;

    public StatusAdapterLinkClickHandler(IStatusesAdapter<D> adapter) {
        super(adapter.getContext(), null);
        this.adapter = adapter;
    }

    @Override
    protected void openMedia(long accountId, long extraId, boolean sensitive, String link, int start, int end) {
        if (extraId == RecyclerView.NO_POSITION) return;
        final ParcelableStatus status = adapter.getStatus((int) extraId);
        final ParcelableMedia current = StatusLinkClickHandler.findByLink(status.media, link);
        //TODO open media animation
        Bundle options = null;
        Utils.openMedia(context, status, current, options);
    }

}
