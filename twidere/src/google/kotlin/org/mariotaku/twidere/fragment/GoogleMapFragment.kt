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

package org.mariotaku.twidere.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_LATITUDE
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_LONGITUDE
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.fragment.iface.IMapFragment

class GoogleMapFragment : SupportMapFragment(), Constants, IMapFragment, IBaseFragment {

    private val actionHelper = IBaseFragment.ActionHelper(this)

    private var activeMap: GoogleMap? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val args = arguments
        if (args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
            return
        val lat = args.getDouble(EXTRA_LATITUDE, 0.0)
        val lng = args.getDouble(EXTRA_LONGITUDE, 0.0)
        getMapAsync { googleMap ->
            val marker = MarkerOptions()
            marker.position(LatLng(lat, lng))
            googleMap.addMarker(marker)
            center(false)
            activeMap = googleMap
        }
    }

    override fun onPause() {
        actionHelper.dispatchOnPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        actionHelper.dispatchOnResumeFragments()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_google_maps_viewer, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestFitSystemWindows()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.center -> {
                center()
            }
        }
        return true
    }


    override fun center() {
        center(true)
    }

    fun center(animate: Boolean) {
        val googleMap = activeMap ?: return
        val args = arguments ?: return
        if (!args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
            return
        val lat = args.getDouble(EXTRA_LATITUDE, 0.0)
        val lng = args.getDouble(EXTRA_LONGITUDE, 0.0)
        val c = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 12f)
        if (animate) {
            googleMap.animateCamera(c)
        } else {
            googleMap.moveCamera(c)
        }
    }


    override val extraConfiguration: Bundle? = null
    override val tabPosition: Int = -1
    override val tabId: Long = -1L

    override fun requestFitSystemWindows() {
        val activity = activity
        val parentFragment = parentFragment
        val callback: IBaseFragment.SystemWindowsInsetsCallback
        if (parentFragment is IBaseFragment.SystemWindowsInsetsCallback) {
            callback = parentFragment
        } else if (activity is IBaseFragment.SystemWindowsInsetsCallback) {
            callback = activity
        } else {
            return
        }
        val insets = Rect()
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets)
        }
    }

    override fun executeAfterFragmentResumed(action: (IBaseFragment) -> Unit) {
        actionHelper.executeAfterFragmentResumed(action)
    }

    protected fun fitSystemWindows(insets: Rect) {
        val view = view
        view?.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }
}
