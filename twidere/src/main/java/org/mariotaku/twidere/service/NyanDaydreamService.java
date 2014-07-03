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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.service.dreams.DreamService;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.NyanDaydreamView;

public class NyanDaydreamService extends DreamService implements Constants, OnSharedPreferenceChangeListener,
		OnSystemUiVisibilityChangeListener {

	private NyanDaydreamView mNyanDaydreamView;
	private SharedPreferences mPreferences;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setContentView(R.layout.nyan_daydream);
		mNyanDaydreamView.setOnSystemUiVisibilityChangeListener(this);
		updateView();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mNyanDaydreamView = (NyanDaydreamView) findViewById(R.id.nyan);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		setInteractive(false);
		setFullscreen(true);
		setScreenBright(false);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
			updateView();
		}
	}

	@Override
	public void onSystemUiVisibilityChange(final int visibility) {
		if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			finish();
		}
	}

	private void updateView() {
		if (mPreferences == null) return;
		final Resources res = getResources();
		final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
		mNyanDaydreamView.setScale(mPreferences.getInt(KEY_LIVE_WALLPAPER_SCALE, def));
	}

}
