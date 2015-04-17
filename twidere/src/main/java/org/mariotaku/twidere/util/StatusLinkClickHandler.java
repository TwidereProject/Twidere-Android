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

import android.content.Context;
import android.os.Bundle;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/1/23.
 */
public class StatusLinkClickHandler extends OnLinkClickHandler {

    private ParcelableStatus mStatus;

    @Override
    protected void openMedia(long accountId, long extraId, boolean sensitive, String link, int start, int end) {
        final ParcelableStatus status = mStatus;
        final ParcelableMedia current = findByLink(status.media, link);
        //TODO open media animation
        Bundle options = null;
        Utils.openMedia(context, status, current, options);
    }

    public static ParcelableMedia findByLink(ParcelableMedia[] media, String link) {
        if (link == null || media == null) return null;
        for (ParcelableMedia mediaItem : media) {
            if (link.equals(mediaItem.media_url) || link.equals(mediaItem.page_url))
                return mediaItem;
        }
        return null;
    }

    public void setStatus(ParcelableStatus status) {
        mStatus = status;
    }

    public StatusLinkClickHandler(Context context, MultiSelectManager manager) {
        super(context, manager);
    }
}
