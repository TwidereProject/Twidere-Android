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

    private final Context context;
    private final Emojidex emojidex;

    private final Collection<Integer> downloadHandles = new LinkedHashSet<Integer>();

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
        }

        return hasHandle;
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkExecUpdate()
    {
        return      emojidex.getUpdateInfo().isNeedUpdate()
                ||  checkUpdateTime()
                ;
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkUpdateTime()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdateTime = emojidex.getUpdateInfo().getLastUpdateTime();
        final long currentTime = System.currentTimeMillis();
        final long updateInterval = 1000 * 60 * 60 * 24 * 7;    // 7 days
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, boolean result)
        {
            finishMethod(handle, "End update.");
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            finishMethod(handle, "Cancel update.");
        }

        private void finishMethod(int handle, String msg)
        {
            if(downloadHandles.remove(handle))
            {
                // End update.
                if(downloadHandles.isEmpty())
                {
                    // Remove listener.
                    emojidex.removeDownloadListener(this);

                    Log.d(TAG, msg);
                }
            }
        }
    }
}
