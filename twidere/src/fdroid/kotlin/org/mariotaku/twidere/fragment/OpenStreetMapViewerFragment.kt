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

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import android.view.*
import kotlinx.android.synthetic.fdroid.activity_osm_viewer.*
import org.mariotaku.ktextension.preferExternalCacheDir
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_LATITUDE
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_LONGITUDE
import org.mariotaku.twidere.util.DebugLog
import org.osmdroid.api.IMapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.io.File
import java.util.*

class OpenStreetMapViewerFragment : BaseFragment(), Constants {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val args = arguments ?: return
        val latitude = args.getDouble(EXTRA_LATITUDE, Double.NaN)
        val longitude = args.getDouble(EXTRA_LONGITUDE, Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) {
            activity?.finish()
            return
        }
        this.latitude = latitude
        this.longitude = longitude
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)
        mapView.isTilesScaledToDpi = true
        val gp = GeoPoint(latitude, longitude)
        val d = ResourcesCompat.getDrawable(resources, R.drawable.ic_map_marker, null)!!
        val markers = Itemization(d)
        val overlayItem = OverlayItem("", "", gp)
        markers.addOverlay(overlayItem)
        mapView.overlays.add(markers)
        val mc = mapView.controller
        mc.setZoom(12)
        mc.setCenter(gp)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_osm_viewer, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Configuration.getInstance().apply {
            try {
                osmdroidBasePath = File(context.preferExternalCacheDir, "osmdroid")
                osmdroidTileCache = File(osmdroidBasePath, "tiles")

                osmdroidBasePath.mkdirs()
                osmdroidTileCache.mkdirs()
            } catch (e: Exception) {
                DebugLog.w(tr = e)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_osm_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.center -> {
                moveToCenter(latitude, longitude)
            }
        }
        return true
    }

    private fun moveToCenter(lat: Double, lng: Double) {
        val gp = GeoPoint(lat, lng)
        val mc = mapView.controller
        mc.animateTo(gp)
    }

    internal class Itemization(defaultMarker: Drawable) : ItemizedOverlay<OverlayItem>(defaultMarker.apply {
        setBounds(-intrinsicWidth / 2, -intrinsicHeight, intrinsicWidth / 2, 0)
    }) {

        private val overlays = ArrayList<OverlayItem>()

        fun addOverlay(overlay: OverlayItem) {
            overlays.add(overlay)
            populate()
        }

        override fun onSnapToItem(x: Int, y: Int, snapPoint: Point, mapView: IMapView): Boolean {
            return false
        }

        override fun size(): Int {
            return overlays.size
        }

        override fun createItem(i: Int): OverlayItem {
            return overlays[i]
        }

    }
}
