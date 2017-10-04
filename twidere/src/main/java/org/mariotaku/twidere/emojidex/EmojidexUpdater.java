package org.mariotaku.twidere.emojidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.downloader.DownloadListener;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by kou on 16/07/13.
 */
public class EmojidexUpdater {
    static final String TAG = "emojidex-Twidere::EmojidexUpdater";
    static final String PREFERENCE_KEY = "lastEmojidexUpdateTime";

    private final Context context;
    private final Emojidex emojidex;

    private final Collection<Integer> downloadHandles = new LinkedHashSet<Integer>();

    private boolean succeeded;

    /**
     * Construct object.
     * @param context
     */
    public EmojidexUpdater(Context context)
    {
        this.context = context;
        emojidex = Emojidex.getInstance();
    }

    /**
     * Start update thread.
     * @return  false when not start update.
     */
    public boolean startUpdateThread()
    {
        return startUpdateThread(false);
    }

    /**
     * Start update thread.
     * @param forceFlag     Force update flag.
     * @return              false when not start update.
     */
    public boolean startUpdateThread(boolean forceFlag)
    {
        if(     !downloadHandles.isEmpty()
                ||  (!checkExecUpdate() && !forceFlag) )
        {
            Log.d(TAG, "Skip update.");
            return false;
        }

        Log.d(TAG, "Start update.");

        downloadHandles.addAll(
                emojidex.update()
        );

        final boolean hasHandle = !downloadHandles.isEmpty();
        if(hasHandle)
        {
            emojidex.addDownloadListener(new CustomDownloadListener());
            succeeded = true;
        }

        return hasHandle;
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkExecUpdate()
    {
        return checkUpdateTime();
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkUpdateTime()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdateTime = pref.getLong(PREFERENCE_KEY, 0);
        final long currentTime = System.currentTimeMillis();
        final long updateInterval = 1000 * 60 * 60 * 24 * 7;    // 7 days
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, boolean result)
        {
            finishMethod(handle, result, "End update.");
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            finishMethod(handle, result, "Cancel update.");
        }

        private void finishMethod(int handle, boolean result, String msg)
        {
            if(downloadHandles.remove(handle))
            {
                succeeded = (succeeded && result);

                // End update.
                if(downloadHandles.isEmpty())
                {
                    // If emoji download failed, execute force update next time.
                    final long updateTime = succeeded ? System.currentTimeMillis() : 0;
                    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                    final SharedPreferences.Editor prefEditor = pref.edit();
                    prefEditor.putLong(PREFERENCE_KEY, updateTime);
                    prefEditor.commit();

                    // Remove listener.
                    emojidex.removeDownloadListener(this);

                    Log.d(TAG, msg);
                }
            }
        }
    }
}
