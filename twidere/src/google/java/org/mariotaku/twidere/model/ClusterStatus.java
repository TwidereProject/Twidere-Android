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

package org.mariotaku.twidere.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by mariotaku on 15/1/13.
 */
public class ClusterStatus implements ClusterItem {

    private final ParcelableStatus status;

    public ClusterStatus(ParcelableStatus status) {
        if (!ParcelableLocation.isValidLocation(status.location)) {
            throw new IllegalArgumentException();
        }
        this.status = status;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(status.location.latitude, status.location.longitude);
    }

}
