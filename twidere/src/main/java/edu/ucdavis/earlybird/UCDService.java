package edu.ucdavis.earlybird;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Request location ONCE per WAKE_PERIOD_IN_MILLI.
 */
public class UCDService extends Service {

    public static final String ACTION_GET_LOCATION = "edu.ucdavis.earlybird.GET_LOCATION";
    private AlarmManager mAlarmManager;
    private PendingIntent uploadIntent;

    @Override
    public IBinder onBind(final Intent intent) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ProfilingUtil.log(this, "onCreate");
        mAlarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);

        // Upload Service
        final Intent i = new Intent(UploadReceiver.ACTION_UPLOAD_PROFILE);
        uploadIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 12 * 60 * 60 * 1000, uploadIntent);
    }

    @Override
    public void onDestroy() {
        mAlarmManager.cancel(uploadIntent);
        super.onDestroy();
    }

}
