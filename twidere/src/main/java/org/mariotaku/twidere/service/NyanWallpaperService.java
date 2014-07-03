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

package org.mariotaku.twidere.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.PowerManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.NyanSurfaceHelper;

public class NyanWallpaperService extends WallpaperService implements Constants {

	@Override
	public Engine onCreateEngine() {
		return new NyanWallpaperEngine();
	}

	private Context getContext() {
		return this;
	}

	private final class NyanWallpaperEngine extends Engine implements OnSharedPreferenceChangeListener {

		private SharedPreferences mPreferences;
		private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(final Context context, final Intent intent) {
				final String action = intent.getAction();
				if (Intent.ACTION_SCREEN_ON.equals(action)) {
					if (mHelper == null) return;
					mHelper.start();
				} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
					if (mHelper == null) return;
					mHelper.stop();
				}

			}
		};

		private NyanSurfaceHelper mHelper;

		@Override
		public void onCreate(final SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			mPreferences.registerOnSharedPreferenceChangeListener(this);
			mHelper = new NyanSurfaceHelper(getContext());
			final IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			registerReceiver(mScreenReceiver, filter);
		}

		@Override
		public void onDestroy() {
			mPreferences.unregisterOnSharedPreferenceChangeListener(this);
			unregisterReceiver(mScreenReceiver);
			super.onDestroy();
		}

		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			if (KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
				updateSurface();
			}
		}

		@Override
		public void onSurfaceCreated(final SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			holder.addCallback(mHelper);
			updateSurface();
			updateHelperState();
		}

		@Override
		public void onSurfaceDestroyed(final SurfaceHolder holder) {
			mHelper.stop();
			holder.removeCallback(mHelper);
			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onVisibilityChanged(final boolean visible) {
			super.onVisibilityChanged(visible);
			if (mHelper != null) {
				mHelper.setSkipDrawing(!visible);
			}
		}

		private void updateHelperState() {
			final PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			if (pm.isScreenOn()) {
				mHelper.start();
			} else {
				mHelper.stop();
			}
		}

		private void updateSurface() {
			if (mPreferences == null) return;
			final Resources res = getResources();
			final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
			mHelper.setScale(mPreferences.getInt(KEY_LIVE_WALLPAPER_SCALE, def));
			updateHelperState();
		}

	}

}
