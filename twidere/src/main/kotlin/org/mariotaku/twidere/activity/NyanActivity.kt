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

package org.mariotaku.twidere.activity

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_surface_view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_LIVE_WALLPAPER_SCALE
import org.mariotaku.twidere.nyan.NyanDaydreamService
import org.mariotaku.twidere.nyan.NyanSurfaceHelper
import org.mariotaku.twidere.nyan.NyanWallpaperService

class NyanActivity : BaseActivity(), OnLongClickListener, OnSharedPreferenceChangeListener {

    private var helper: NyanSurfaceHelper? = null

    override fun onLongClick(v: View): Boolean {
        Toast.makeText(this, R.string.nyan_sakamoto, Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (KEY_LIVE_WALLPAPER_SCALE == key) {
            updateSurface()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_surface_view)
        surfaceView.setOnLongClickListener(this)
        val holder = surfaceView.holder
        helper = NyanSurfaceHelper(this)
        holder.addCallback(helper)
        updateSurface()
        enableWallpaperDaydream()
    }

    override fun onStart() {
        super.onStart()
        helper?.start()
    }

    override fun onStop() {
        helper?.stop()
        super.onStop()
    }

    private fun enableWallpaperDaydream() {
        val pm = packageManager
        val wallpaperComponent = ComponentName(this, NyanWallpaperService::class.java)
        val wallpaperState = pm.getComponentEnabledSetting(wallpaperComponent)
        var showToast = false
        if (wallpaperState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(wallpaperComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
            showToast = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val daydreamComponent = ComponentName(this, NyanDaydreamService::class.java)
            val daydreamState = pm.getComponentEnabledSetting(daydreamComponent)
            if (daydreamState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(daydreamComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP)
                showToast = true
            }
        }
        if (showToast) {
            Toast.makeText(this, R.string.livewp_daydream_enabled_message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSurface() {
        val def = resources.getInteger(R.integer.default_live_wallpaper_scale)
        helper!!.setScale(preferences.getInt(KEY_LIVE_WALLPAPER_SCALE, def).toFloat())
    }

}
