package org.mariotaku.twidere.extension.streaming;

import org.mariotaku.twidere.Twidere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TwidereLaunchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		final Intent service_intent = new Intent(context, StreamingService.class);
		if (Twidere.BROADCAST_HOME_ACTIVITY_ONCREATE.equals(action)) {
			context.startService(service_intent);
		} else if (Twidere.BROADCAST_HOME_ACTIVITY_ONDESTROY.equals(action)) {
			context.stopService(service_intent);
		}
	}

}
