package org.mariotaku.twidere.emojidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.emojidex.emojidexandroid.DownloadListener;
import com.emojidex.emojidexandroid.Emoji;
import com.emojidex.emojidexandroid.EmojiDownloader;
import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;

import org.mariotaku.twidere.R;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * Created by kou on 16/07/13.
 */
public class EmojidexUpdater {
    static final String TAG = "Twidere::EmojidexUpdater";
    static final String PREF_KEY = "lastEmojidexUpdateTime";

    private final Context context;
    private final Emojidex emojidex;

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
        if( !checkExecUpdate() && !forceFlag )
        {
            Log.d(TAG, "Skip update.");
            return false;
        }

        Log.d(TAG, "Start update.");

        final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)));
        return emojidex.download(formats.toArray(new EmojiFormat[formats.size()]), new CustomDownloadListener(forceFlag));
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
        final long lastUpdateTime = pref.getLong(PREF_KEY, 0);
        final long currentTime = new Date().getTime();
        final long updateInterval = 1000 * 60 * 60 * 24 * 7;
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    /**
     * Custom download listener.
     */
    class CustomDownloadListener extends DownloadListener
    {
        private final boolean force;

        public CustomDownloadListener(boolean forceFlag)
        {
            force = forceFlag;
        }

        @Override
        public void onPreAllEmojiDownload() {
            emojidex.reload();
        }

        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader)
        {
            super.onPostAllJsonDownload(downloader);
        }

        @Override
        public void onPostOneEmojiDownload(String emojiName) {
            final Emoji emoji = emojidex.getEmoji(emojiName);
            if(emoji != null)
            {
                emoji.reloadImage();
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();

            // Save update time.
            final long updateTime = new Date().getTime();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(PREF_KEY, updateTime);
            prefEditor.commit();

            Log.d(TAG, "End update.");
        }
    }
}
