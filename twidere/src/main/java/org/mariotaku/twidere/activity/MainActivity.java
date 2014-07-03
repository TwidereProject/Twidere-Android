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
import android.content.Intent;
import android.os.Bundle;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public class MainActivity extends Activity implements Constants {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
			StrictModeUtils.detectAllThreadPolicy();
		}
		super.onCreate(savedInstanceState);
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final int themeResource = ThemeUtils.getThemeResource(this);
		final int accentColor = ThemeUtils.isColoredActionBar(themeResource) ? ThemeUtils.getUserThemeColor(this) : 0;
		final int backgroundAlpha = ThemeUtils.isTransparentBackground(themeResource) ? ThemeUtils
				.getUserThemeBackgroundAlpha(this) : 0xFF;
		ThemeUtils.notifyStatusBarColorChanged(this, themeResource, accentColor, backgroundAlpha);
	}

}
