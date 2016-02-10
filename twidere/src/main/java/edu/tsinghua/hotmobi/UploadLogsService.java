package edu.tsinghua.hotmobi;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 16/2/10.
 */
public class UploadLogsService extends IntentService {
    private static final int NOTIFICATION_ID_UPLOAD_LOG = 301;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadLogsService() {
        super("hotmobi_log_uploader");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setTicker("Uploading logs");
            builder.setContentTitle("HotMobi research project");
            builder.setContentText("Uploading logs");
            builder.setProgress(0, 0, true);
            builder.setSmallIcon(R.drawable.ic_stat_info, 0);
            builder.setOngoing(true);
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID_UPLOAD_LOG, builder.build());
            uploadLogs();
            nm.cancel(NOTIFICATION_ID_UPLOAD_LOG);
        } else {
            uploadLogs();
        }
    }

    private void uploadLogs() {
        final UploadLogsTask task = new UploadLogsTask(this);
        task.run();
    }
}
