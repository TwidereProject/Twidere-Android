package edu.tsinghua.spice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.mariotaku.twidere.util.Utils;

import edu.tsinghua.spice.Task.SpiceAsyUploadTask;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;


/**
 * Created by Denny C. Ng on 2/20/15.
 */
public class SpiceUploadReceiver extends BroadcastReceiver {

    public static final String ACTION_UPLOAD_PROFILE = "edu.tsinghua.spice.UPLOAD_PROFILE";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final boolean isWifi = Utils.isOnWifi(context.getApplicationContext());
        final boolean isCharging = SpiceProfilingUtil.isCharging(context.getApplicationContext());

        if (isWifi && isCharging) {
            new SpiceAsyUploadTask(context).execute();
        }
    }
}
