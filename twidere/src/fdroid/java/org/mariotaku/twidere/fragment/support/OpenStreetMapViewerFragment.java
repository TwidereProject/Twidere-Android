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

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

public class OpenStreetMapViewerFragment extends BaseSupportFragment implements Constants {

    private MapView mMapView;
    private double mLatitude, mLongitude;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        final double latitude = args.getDouble(EXTRA_LATITUDE, Double.NaN);
        final double longitude = args.getDouble(EXTRA_LONGITUDE, Double.NaN);
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            getActivity().finish();
            return;
        }
        mLatitude = latitude;
        mLongitude = longitude;
        mMapView.setMultiTouchControls(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setTilesScaledToDpi(true);
        final List<Overlay> overlays = mMapView.getOverlays();
        final GeoPoint gp = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
        final Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_map_marker, null);
        final Itemization markers = new Itemization(d, mMapView.getResourceProxy());
        final OverlayItem overlayitem = new OverlayItem("", "", gp);
        markers.addOverlay(overlayitem);
        overlays.add(markers);
        final IMapController mc = mMapView.getController();
        mc.setZoom(12);
        mc.setCenter(gp);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_osm_viewer, container, false);
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mMapView = (MapView) view.findViewById(R.id.map_view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_osm_viewer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.center: {
                moveToCenter(mLatitude, mLongitude);
                break;
            }
        }
        return true;
    }

    private void moveToCenter(double lat, double lng) {
        final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        final IMapController mc = mMapView.getController();
        mc.animateTo(gp);
    }

    static class Itemization extends ItemizedOverlay<OverlayItem> {

        private final ArrayList<OverlayItem> mOverlays = new ArrayList<>();

        public Itemization(final Drawable defaultMarker, final ResourceProxy proxy) {
            super(boundCenterBottom(defaultMarker), proxy);
        }

        public void addOverlay(final OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }

        @Override
        public boolean onSnapToItem(final int x, final int y, final Point snapPoint, final IMapView mapView) {
            return false;
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        protected OverlayItem createItem(final int i) {
            return mOverlays.get(i);
        }

        protected static Drawable boundCenterBottom(final Drawable d) {
            d.setBounds(-d.getIntrinsicWidth() / 2, -d.getIntrinsicHeight(), d.getIntrinsicWidth() / 2, 0);
            return d;
        }

    }
}
