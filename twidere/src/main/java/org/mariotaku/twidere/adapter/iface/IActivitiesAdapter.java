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

package org.mariotaku.twidere.adapter.iface;

import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;

/**
 * Created by mariotaku on 14/11/18.
 */
public interface IActivitiesAdapter<Data> extends IContentCardAdapter, IGapSupportedAdapter {

    ParcelableActivity getActivity(int position);

    int getActivityCount();

    void setData(Data data);

    @PreviewStyle
    int getMediaPreviewStyle();

    @NonNull
    @Override
    MediaLoaderWrapper getMediaLoader();

    MediaLoadingHandler getMediaLoadingHandler();
}
