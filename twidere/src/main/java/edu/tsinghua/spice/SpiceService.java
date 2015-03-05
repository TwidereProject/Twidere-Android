package edu.tsinghua.spice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import edu.tsinghua.spice.Utilies.NetworkStateUtil;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

/**
 * Created by Denny C. Ng on 2/20/15.
 *
 * Request location ONCE per WAKE_PERIOD_IN_MILLI.
 */
public class SpiceService extends Service {

    public static final long LOCATION_PERIOD_IN_MILLI = 15 * 60 * 1000;
    public static final String ACTION_GET_LOCATION = "edu.tsinghua.spice.GET_LOCATION";
    private LocationManager mLocationManager;
    private AlarmManager mAlarmManager;
    private LocationUpdateReceiver mAlarmReceiver;
    private PendingIntent locationIntent;
    private PendingIntent uploadIntent;

    @Override
    public IBinder onBind(final Intent intent) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SpiceProfilingUtil.log(this, "onCreate");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAlarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);

        mAlarmReceiver = new LocationUpdateReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GET_LOCATION);
        registerReceiver(mAlarmReceiver, filter);

        final Intent intent = new Intent(ACTION_GET_LOCATION);
        locationIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), LOCATION_PERIOD_IN_MILLI,
                locationIntent);

        // Upload Service
        final Intent i = new Intent(SpiceUploadReceiver.ACTION_UPLOAD_PROFILE);
        uploadIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 12 * 60 * 60 * 1000,
                uploadIntent);
    }

    @Override
    public void onDestroy() {
        mAlarmManager.cancel(locationIntent);
        unregisterReceiver(mAlarmReceiver);
        super.onDestroy();
    }

    private final class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mLocationManager == null) return;
            SpiceProfilingUtil.log(context, "AlarmReceiver");
            final String provider = LocationManager.NETWORK_PROVIDER;
            if (mLocationManager.isProviderEnabled(provider)) {
                final Location location = mLocationManager.getLastKnownLocation(provider);
                if (location != null) {
                    SpiceProfilingUtil.profile(SpiceService.this, SpiceProfilingUtil.FILE_NAME_LOCATION, location.getTime() + ","
                            + location.getLatitude() + "," + location.getLongitude() + "," + location.getProvider());
                    SpiceProfilingUtil.log(context,
                            location.getTime() + "," + location.getLatitude() + "," + location.getLongitude() + ","
                                    + location.getProvider());
                    SpiceProfilingUtil.profile(SpiceService.this, SpiceProfilingUtil.FILE_NAME_NETWORK, NetworkStateUtil.getConnectedType(SpiceService.this));
                }
            }
        }
    }
}
