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

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IMapFragment;

public class GoogleMapFragment extends SupportMapFragment implements Constants, IMapFragment, IBaseFragment {

    private GoogleMap mMapView;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
            return;
        final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
        mMapView = getMap();
        final MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(lat, lng));
        mMapView.addMarker(marker);
        center(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_google_maps_viewer, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBaseViewCreated(view, savedInstanceState);
        requestFitSystemWindows();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.center: {
                center();
                break;
            }
        }
        return true;
    }


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
    public Bundle getExtraConfiguration() {
        return null;
    }

    @Override
    public int getTabPosition() {
        return 0;
    }

    @Override
    public void requestFitSystemWindows() {
        final Activity activity = getActivity();
        final Fragment parentFragment = getParentFragment();
        final IBaseFragment.SystemWindowsInsetsCallback callback;
        if (parentFragment instanceof IBaseFragment.SystemWindowsInsetsCallback) {
            callback = (IBaseFragment.SystemWindowsInsetsCallback) parentFragment;
        } else if (activity instanceof IBaseFragment.SystemWindowsInsetsCallback) {
            callback = (IBaseFragment.SystemWindowsInsetsCallback) activity;
        } else {
            return;
        }
        final Rect insets = new Rect();
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets);
        }
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {

    }

    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    }
}
