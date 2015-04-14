package edu.tsinghua.spice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import edu.tsinghua.spice.Utilies.NetworkStateUtil;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

/**
 * Created by Denny C. Ng on 2/20/15.
 * <p/>
 * Request location ONCE per WAKE_PERIOD_IN_MILLI.
 */
public class SpiceService extends Service {

    private AlarmManager mAlarmManager;
    private PendingIntent mUploadIntent;

    @Override
    public IBinder onBind(final Intent intent) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SpiceProfilingUtil.log(this, "onCreate");
        mAlarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, screenFilter);

        // Upload Service
        final Intent uploadIntent = new Intent(SpiceUploadReceiver.ACTION_UPLOAD_PROFILE);
        mUploadIntent = PendingIntent.getBroadcast(this, 0, uploadIntent, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 12 * 60 * 60 * 1000, mUploadIntent);
    }

    @Override
    public void onDestroy() {
        mAlarmManager.cancel(mUploadIntent);
        unregisterReceiver(mScreenReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                SpiceProfilingUtil.profile(context, SpiceProfilingUtil.FILE_NAME_SCREEN, "SCREEN ON" + "," + NetworkStateUtil.getConnectedType(context));
                SpiceProfilingUtil.log(context, "SCREEN ON" + "," + NetworkStateUtil.getConnectedType(context));
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                SpiceProfilingUtil.profile(context, SpiceProfilingUtil.FILE_NAME_SCREEN, "SCREEN OFF" + "," + NetworkStateUtil.getConnectedType(context));
                SpiceProfilingUtil.log(context, "SCREEN OFF" + "," + NetworkStateUtil.getConnectedType(context));
            }
        }

    };

}
