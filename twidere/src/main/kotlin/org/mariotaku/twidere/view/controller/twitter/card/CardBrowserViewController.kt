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

package org.mariotaku.twidere.view.controller.twitter.card

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import org.mariotaku.twidere.view.ContainerView

/**
 * Created by mariotaku on 15/1/6.
 */
class CardBrowserViewController : ContainerView.ViewController() {

    lateinit var url: String

    override fun onCreateView(parent: ContainerView): View {
        val webView = WebView(context)
        webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        return webView
    }

    override fun onDestroyView(view: View) {
        (view as WebView).destroy()
        super.onDestroyView(view)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()
        val webView = view as WebView
        webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = false
        }
        webView.loadUrl(url)
    }


    companion object {

        fun show(url: String): CardBrowserViewController {
            val vc = CardBrowserViewController()
            vc.url = url
            return vc
        }
    }
}
