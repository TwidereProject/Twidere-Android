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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.mariotaku.twidere.Constants.EXTRA_LATITUDE
import org.mariotaku.twidere.Constants.EXTRA_LONGITUDE
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.iface.IMapFragment
import org.mariotaku.twidere.util.webkit.DefaultWebViewClient

class WebMapFragment : BaseSupportWebViewFragment(), IMapFragment {

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    override fun center() {
        webView?.loadUrl("javascript:center();")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.center -> {
                center()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_google_maps_viewer, menu)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        getLocation()
        setupWebView()
    }

    /**
     * The Location Manager manages location providers. This code searches for
     * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
     * mechanism) and finds the last known location.
     */
    private fun getLocation() {
        val bundle = arguments
        if (bundle != null) {
            latitude = bundle.getDouble(EXTRA_LATITUDE, 0.0)
            longitude = bundle.getDouble(EXTRA_LONGITUDE, 0.0)
        }
    }

    /**
     * Sets up the WebView object and loads the URL of the page *
     */
    private fun setupWebView() {

        val webView = webView!!
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.setWebViewClient(MapWebViewClient(activity))
        webView.loadUrl(MAPVIEW_URI)

        val settings = webView.settings
        settings.builtInZoomControls = false

        /** Allows JavaScript calls to access application resources  */
        webView.addJavascriptInterface(MapJavaScriptInterface(this), "android")

    }

    /**
     * Sets up the interface for getting access to Latitude and Longitude data
     * from device
     */
    internal class MapJavaScriptInterface(val fragment: WebMapFragment) {

        @JavascriptInterface
        fun getLatitude(): Double {
            return fragment.latitude
        }

        @JavascriptInterface
        fun getLongitude(): Double {
            return fragment.longitude
        }

    }

    internal inner class MapWebViewClient(activity: Activity) : DefaultWebViewClient(activity) {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri = Uri.parse(url)
            if (uri.scheme == Uri.parse(MAPVIEW_URI).scheme) return false
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            return true
        }
    }

    companion object {

        private val MAPVIEW_URI = "file:///android_asset/mapview.html"
    }

}
