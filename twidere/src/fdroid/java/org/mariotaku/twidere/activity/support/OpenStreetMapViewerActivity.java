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

package org.mariotaku.twidere.activity.support;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
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

public class OpenStreetMapViewerActivity extends BaseSupportActivity implements Constants {

    private MapView mMapView;
    private double mLatitude, mLongitude;

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getViewerThemeResource(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_osm_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                onBackPressed();
                break;
            }
            case MENU_CENTER: {
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

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mMapView = (MapView) findViewById(R.id.map_view);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Uri uri = getIntent().getData();
        if (uri == null || !AUTHORITY_MAP.equals(uri.getAuthority())) {
            finish();
            return;
        }
        final double latitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LAT), Double.NaN);
        final double longitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LNG), Double.NaN);
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            finish();
            return;
        }
        mLatitude = latitude;
        mLongitude = longitude;
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_osm_viewer);
        mMapView.setMultiTouchControls(true);
        mMapView.setBuiltInZoomControls(true);
        final List<Overlay> overlays = mMapView.getOverlays();
        final GeoPoint gp = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
        final Drawable d = getResources().getDrawable(R.drawable.ic_map_marker);
        final Itemization markers = new Itemization(d, mMapView.getResourceProxy());
        final OverlayItem overlayitem = new OverlayItem("", "", gp);
        markers.addOverlay(overlayitem);
        overlays.add(markers);
        final IMapController mc = mMapView.getController();
        mc.setZoom(12);
        mc.setCenter(gp);
    }


    static class Itemization extends ItemizedOverlay<OverlayItem> {

        private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

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
