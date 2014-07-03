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

package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.service.NyanDaydreamService;
import org.mariotaku.twidere.service.NyanWallpaperService;
import org.mariotaku.twidere.util.NyanSurfaceHelper;

public class NyanActivity extends Activity implements Constants, OnLongClickListener, OnSharedPreferenceChangeListener {

	private SurfaceView mSurfaceView;
	private SharedPreferences mPreferences;
	private NyanSurfaceHelper mHelper;

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
	}

	@Override
	public boolean onLongClick(final View v) {
		Toast.makeText(this, R.string.nyan_sakamoto, Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
			updateSurface();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		setContentView(R.layout.surface_view);
		mSurfaceView.setOnLongClickListener(this);
		final SurfaceHolder holder = mSurfaceView.getHolder();
		mHelper = new NyanSurfaceHelper(this);
		holder.addCallback(mHelper);
		updateSurface();
		enableWallpaperDaydream();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mHelper != null) {
			mHelper.start();
		}
	}

	@Override
	protected void onStop() {
		if (mHelper != null) {
			mHelper.stop();
		}
		super.onStop();
	}

	private void enableWallpaperDaydream() {
		final PackageManager pm = getPackageManager();
		final ComponentName wallpaperComponent = new ComponentName(this, NyanWallpaperService.class);
		final int wallpaperState = pm.getComponentEnabledSetting(wallpaperComponent);
		boolean showToast = false;
		if (wallpaperState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			pm.setComponentEnabledSetting(wallpaperComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
			showToast = true;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			final ComponentName daydreamComponent = new ComponentName(this, NyanDaydreamService.class);
			final int daydreamState = pm.getComponentEnabledSetting(daydreamComponent);
			if (daydreamState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
				pm.setComponentEnabledSetting(daydreamComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP);
				showToast = true;
			}
		}
		if (showToast) {
			Toast.makeText(this, R.string.livewp_daydream_enabled_message, Toast.LENGTH_SHORT).show();
		}
	}

	private void updateSurface() {
		if (mPreferences == null) return;
		final Resources res = getResources();
		final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
		mHelper.setScale(mPreferences.getInt(KEY_LIVE_WALLPAPER_SCALE, def));
	}

}
