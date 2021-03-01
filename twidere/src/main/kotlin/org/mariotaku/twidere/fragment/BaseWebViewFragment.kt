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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import org.mariotaku.twidere.util.support.WebSettingsSupport
import org.mariotaku.twidere.util.webkit.DefaultWebViewClient

@SuppressLint("SetJavaScriptEnabled")
open class BaseWebViewFragment : BaseFragment() {

    private var internalWebView: WebView? = null
    private var webViewAvailable: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val view = webView!!
        view.webViewClient = createWebViewClient()
        view.settings.apply {
            builtInZoomControls = true
            javaScriptEnabled = true
            WebSettingsSupport.setAllowUniversalAccessFromFileURLs(this, true)
        }
    }


    protected fun createWebViewClient(): WebViewClient {
        return DefaultWebViewClient(requireActivity())
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        internalWebView?.destroy()
        internalWebView = activity?.let { WebView(it) }
        webViewAvailable = true
        return internalWebView
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    override fun onPause() {
        super.onPause()
        internalWebView!!.onPause()
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    override fun onResume() {
        internalWebView!!.onResume()
        super.onResume()
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    override fun onDestroyView() {
        webViewAvailable = false
        super.onDestroyView()
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    override fun onDestroy() {
        internalWebView?.destroy()
        internalWebView = null
        super.onDestroy()
    }

    /**
     * Gets the WebView.
     */
    val webView: WebView?
        get() = if (webViewAvailable) internalWebView else null
}
