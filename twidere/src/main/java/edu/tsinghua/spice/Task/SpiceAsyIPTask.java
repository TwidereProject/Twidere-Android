package edu.tsinghua.spice.Task;
import android.content.Context;
import android.os.AsyncTask;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;


/**
 * Created by Denny C. Ng on 6/14/15.
 */

public class SpiceAsyIPTask extends AsyncTask<Object, Object, Object> {

    private final Context context;

    public SpiceAsyIPTask(final Context context) {
        this.context = context;

    }

    @Override
    protected Object doInBackground(final Object... params) {
        URL infoUrl = null;
        InputStream inStream = null;
        String ipLine = "";
        HttpURLConnection httpConnection = null;
        try {
            infoUrl = new URL("http://www.cmyip.com/");
            URLConnection connection = infoUrl.openConnection();
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");

                Pattern pattern = Pattern
                        .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                Matcher matcher = pattern.matcher(strber.toString());
                if (matcher.find()) {
                    ipLine = matcher.group();
                }
                SpiceProfilingUtil.profile(context, SpiceProfilingUtil.FILE_NAME_IP, ipLine);
                SpiceProfilingUtil.log( "IP address change: "  +  ipLine);
                return "ok";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        } finally {
            try {
                inStream.close();
                httpConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
