package edu.ucdavis.earlybird;

import static org.mariotaku.twidere.util.Utils.copyStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadTask extends AsyncTask<Void, Void, Void> {

	private static final String LAST_UPLOAD_DATE = "last_upload_time";
	private static final double MILLSECS_HALF_DAY = 1000 * 60 * 60 * 12;

	private final String device_id;
	private final Context context;

	private final HttpClientWrapper client = new HttpClientWrapper();

	private static final String PROFILE_SERVER_URL = "http://weik.metaisle.com/profiles";

	// private static final String PROFILE_SERVER_URL =
	// "http://192.168.0.105:3000/profiles";

	public UploadTask(final Context context) {
		this.context = context;
		device_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public void uploadMultipart(final String url, final File file) {
		final String app_root = file.getParent();
		final File tmp_dir = new File(app_root + "/tmp");
		if (!tmp_dir.exists()) {
			if (!tmp_dir.mkdirs()) {
				ProfilingUtil.log(context, "cannot create tmp, do nothing.");
				return;
			}
		}
		final File tmp = new File(tmp_dir, file.getName());
		file.renameTo(tmp);

		try {
			final HttpParameter param = new HttpParameter("upload", tmp);
			final HttpResponse resp = client.post(url, null, new HttpParameter[] { param });

			// Responses from the server (code and message)
			final int serverResponseCode = resp.getStatusCode();

			ProfilingUtil.log(context, "server response code " + serverResponseCode);

			if (serverResponseCode / 100 == 2) {
				tmp.delete();
			} else {
				putBackProfile(context, tmp, file);
			}

		} catch (final TwitterException e) {
			e.printStackTrace();
			putBackProfile(context, tmp, file);
		}
	}

	@Override
	protected Void doInBackground(final Void... params) {

		final SharedPreferences prefs = context.getSharedPreferences("ucd_data_profiling", Context.MODE_PRIVATE);

		if (prefs.contains(LAST_UPLOAD_DATE)) {
			final long lastUpload = prefs.getLong(LAST_UPLOAD_DATE, System.currentTimeMillis());
			final double deltaDays = (System.currentTimeMillis() - lastUpload) / (MILLSECS_HALF_DAY * 2);
			if (deltaDays < 1) {
				ProfilingUtil.log(context, "Uploaded less than 1 day ago.");
				return null;
			}
		}

		final File root = context.getFilesDir();
		final File[] files = root.listFiles(new CSVFileFilter());

		uploadToNode(files);
		prefs.edit().putLong(LAST_UPLOAD_DATE, System.currentTimeMillis()).commit();
		return null;
	}

	private boolean uploadToNode(final File... files) {
		for (final File file : files) {
			if (file.isDirectory()) {
				continue;
			}
			final String url = PROFILE_SERVER_URL + "/" + device_id + "/"
					+ file.getName().replaceFirst("[.][^.]+$", "");
			ProfilingUtil.log(context, url);
			uploadMultipart(url, file);
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
				ProfilingUtil.log(context, "put profile back success");
			} else {
				ProfilingUtil.log(context, "put profile back failed");
			}
		} else {
			if (tmp.renameTo(profile)) {
				ProfilingUtil.log(context, "put profile back success");
			} else {
				ProfilingUtil.log(context, "put profile back failed");
			}
		}
	}
}
