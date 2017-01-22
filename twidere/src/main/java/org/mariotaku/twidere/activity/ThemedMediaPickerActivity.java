package org.mariotaku.twidere.activity;

import android.content.Context;

import org.mariotaku.pickncrop.library.MediaPickerActivity;
import org.mariotaku.twidere.util.RestFuNetworkStreamDownloader;

public class ThemedMediaPickerActivity extends MediaPickerActivity {

    public static IntentBuilder withThemed(Context context) {
        final IntentBuilder builder = new IntentBuilder(context, ThemedMediaPickerActivity.class);
        builder.cropImageActivityClass(ImageCropperActivity.class);
        builder.streamDownloaderClass(RestFuNetworkStreamDownloader.class);
        return builder;
    }

}
