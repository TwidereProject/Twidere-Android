package edu.tsinghua.spice.Task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.tsinghua.spice.Utilies.SpiceHttpUtil;
import edu.tsinghua.spice.Utilies.SpiceIOUtil;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

import static org.mariotaku.twidere.util.Utils.copyStream;

/**
 * Created by Denny C. Ng on 2/20/15.
 */

public class SpiceAsyUploadTask extends AsyncTask<Void, Void, Void> {

    private static final String PROFILE_SERVER_URL = "http://twidere-spice.mariotaku.org:18080/spice/usage";

    private static final String LAST_UPLOAD_DATE = "last_upload_time";
    private static final double MILLSECS_HALF_DAY = 1000 * 60 * 60 * 12;

    //private final String device_id;
    private final Context context;

    private SpiceHttpUtil uploadClient;

    public SpiceAsyUploadTask(final Context context) {
        this.context = context;
        // device_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public void uploadMultipart(SpiceHttpUtil client, final File file) {

        String fileName = file.getName();
        final String app_root = file.getParent();
        final File tmp_dir = new File(app_root + "/spice");
        if (!tmp_dir.exists()) {
            if (!tmp_dir.mkdirs()) {
                SpiceProfilingUtil.log(context, "cannot create folder spice, do nothing.");
                return;
            }
        }

        final File tmp = new File(tmp_dir, file.getName());
        file.renameTo(tmp);

        try {
            client.connectForMultipart();
            client.addFilePart("file", fileName, SpiceIOUtil.readFile(tmp));
            client.finishMultipart();
            String serverResponseCode = client.getResponse();


            if (serverResponseCode.indexOf("00") > -1) {
                SpiceProfilingUtil.log(context, "server has already received file " + tmp.getName());
                tmp.delete();
            } else {
                SpiceProfilingUtil.log(context, "server does not receive file " + tmp.getName());
                putBackProfile(context, tmp, file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            putBackProfile(context, tmp, file);
        }


    }

    @Override
    protected Void doInBackground(final Void... params) {

        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);

        if (prefs.contains(LAST_UPLOAD_DATE)) {
            final long lastUpload = prefs.getLong(LAST_UPLOAD_DATE, System.currentTimeMillis());
            final double deltaDays = (System.currentTimeMillis() - lastUpload) / (MILLSECS_HALF_DAY * 2);
            if (deltaDays < 1) {
                SpiceProfilingUtil.log(context, "Last uploaded was conducted in 1 day ago.");
                return null;
            }
        }

        final File root = context.getFilesDir();
        final File[] spiceFiles = root.listFiles(new SpiceFileFilter());
        uploadToServer(spiceFiles);
        prefs.edit().putLong(LAST_UPLOAD_DATE, System.currentTimeMillis()).commit();
        return null;
    }


    private boolean uploadToServer(final File... files) {
        for (final File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            final String url = PROFILE_SERVER_URL;
            SpiceProfilingUtil.log(context, url);
            uploadClient = new SpiceHttpUtil(url);
            uploadMultipart(uploadClient, file);
        }
        return false;
    }

    public static void putBackProfile(final Context context, final File tmp, final File profile) {
        boolean success;
        if (profile.exists()) {
            try {
                final FileOutputStream os = new FileOutputStream(tmp, true);
                final FileInputStream is = new FileInputStream(profile);
                copyStream(is, os);
                is.close();
                os.close();
            } catch (final IOException e) {
                e.printStackTrace();
                success = false;
            }
            success = true;

            if (success && tmp.renameTo(profile) && tmp.delete()) {
                SpiceProfilingUtil.log(context, "put profile back success");
            } else {
                SpiceProfilingUtil.log(context, "put profile back failed");
            }
        } else {
            if (tmp.renameTo(profile)) {
                SpiceProfilingUtil.log(context, "put profile back success");
            } else {
                SpiceProfilingUtil.log(context, "put profile back failed");
            }
        }
    }
}
