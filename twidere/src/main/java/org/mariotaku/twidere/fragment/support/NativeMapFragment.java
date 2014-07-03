/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.fragment.iface.IMapFragment;

public class NativeMapFragment extends SupportMapFragment implements Constants, IMapFragment {

	private GoogleMap mMapView;

	@Override
	public void center() {
		center(true);
	}

	public void center(final boolean animate) {
		final Bundle args = getArguments();
		if (mMapView == null || args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
			return;
		final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
		final CameraUpdate c = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12);
		if (animate) {
			mMapView.animateCamera(c);
		} else {
			mMapView.moveCamera(c);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE)) return;
		final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
		mMapView = getMap();
		final MarkerOptions marker = new MarkerOptions();
		marker.position(new LatLng(lat, lng));
		mMapView.addMarker(marker);
		center(false);
	}

}
