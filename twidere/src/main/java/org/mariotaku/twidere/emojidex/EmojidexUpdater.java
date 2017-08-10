package org.mariotaku.twidere.emojidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.downloader.DownloadConfig;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;

import org.mariotaku.twidere.R;

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

        final DownloadConfig config =
                new DownloadConfig()
                        .addFormat(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)))
                ;
        final EmojiDownloader downloader = emojidex.getEmojiDownloader();

        boolean result = false;

        // UTF
        int handle = downloader.downloadUTFEmoji(config);
        if(handle != EmojiDownloader.HANDLE_NULL)
        {
            downloadHandles.add(handle);
            result = true;
        }

        // Extended
        handle = downloader.downloadExtendedEmoji(config);
        if(handle != EmojiDownloader.HANDLE_NULL)
        {
            downloadHandles.add(handle);
            result = true;
        }

        if(result)
            emojidex.addDownloadListener(new CustomDownloadListener());

        return result;
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
        public void onFinish(int handle, EmojiDownloader.Result result)
        {
            if(     downloadHandles.remove(handle)
                    &&  downloadHandles.isEmpty()       )
            {
                // If emoji download failed, execute force update next time.
                final long updateTime = result.getFailedCount() > 0 ? 0 : System.currentTimeMillis();
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                final SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putLong(PREFERENCE_KEY, updateTime);
                prefEditor.commit();

                // Remove listener.
                emojidex.removeDownloadListener(this);

                Log.d(TAG, "End update.");
            }
        }

        @Override
        public void onCancelled(int handle, EmojiDownloader.Result result)
        {
            if(     downloadHandles.remove(handle)
                    &&  downloadHandles.isEmpty()       )
            {
                // Remove listener.
                emojidex.removeDownloadListener(this);

                Log.d(TAG, "Cancel update.");
            }
        }
    }
}
