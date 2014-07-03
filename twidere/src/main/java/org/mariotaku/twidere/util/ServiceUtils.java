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

package org.mariotaku.twidere.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

import org.mariotaku.twidere.Constants;

import java.util.HashMap;

public final class ServiceUtils implements Constants {

	private static HashMap<Context, ServiceUtils.ServiceBinder> sConnectionMap = new HashMap<Context, ServiceUtils.ServiceBinder>();

	public static ServiceToken bindToService(final Context context, final Intent intent) {

		return bindToService(context, intent, null);
	}

	public static ServiceToken bindToService(final Context context, final Intent intent,
			final ServiceConnection callback) {

		final ContextWrapper cw = new ContextWrapper(context);
		final ComponentName cn = cw.startService(intent);
		if (cn != null) {
			final ServiceUtils.ServiceBinder sb = new ServiceBinder(callback);
			if (cw.bindService(intent, sb, 0)) {
				sConnectionMap.put(cw, sb);
				return new ServiceToken(cw);
			}
		}
		Log.e(LOGTAG, "Failed to bind to service");
		return null;
	}

	public static class ServiceToken {

		ContextWrapper wrapped_context;

		ServiceToken(final ContextWrapper context) {

			wrapped_context = context;
		}
	}

	static class ServiceBinder implements ServiceConnection {

		private final ServiceConnection mCallback;

		public ServiceBinder(final ServiceConnection callback) {

			mCallback = callback;
		}

		@Override
		public void onServiceConnected(final ComponentName className, final android.os.IBinder service) {

			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
		}
	}
}
