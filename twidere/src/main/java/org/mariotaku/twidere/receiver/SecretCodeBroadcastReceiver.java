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

package org.mariotaku.twidere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;

public class SecretCodeBroadcastReceiver extends BroadcastReceiver implements IntentConstants {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final Intent testIntent = new Intent(context, SettingsActivity.class);
		final String cls = SettingsDetailsFragment.class.getName();
		final String title = context.getString(R.string.hidden_settings);
		final Bundle args = new Bundle();
		args.putInt(EXTRA_RESID, R.xml.settings_hidden);
		testIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, cls);
		testIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
		testIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, title);
		testIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_SHORT_TITLE, title);
		testIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(testIntent);
	}

}
