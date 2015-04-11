package edu.tsinghua.spice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

/**
 * Created by Denny C. Ng on 2/20/15.
 * <p/>
 * Request location ONCE per WAKE_PERIOD_IN_MILLI.
 */
public class SpiceService extends Service {

    private AlarmManager mAlarmManager;

    @Override
    public IBinder onBind(final Intent intent) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SpiceProfilingUtil.log(this, "onCreate");
        mAlarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);

        // Upload Service
        final Intent uploadIntent = new Intent(SpiceUploadReceiver.ACTION_UPLOAD_PROFILE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                12 * 60 * 60 * 1000, PendingIntent.getBroadcast(this, 0, uploadIntent, 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
