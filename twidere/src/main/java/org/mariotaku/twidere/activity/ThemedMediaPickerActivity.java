package org.mariotaku.twidere.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.pickncrop.library.MediaPickerActivity;
import org.mariotaku.twidere.util.RestFuNetworkStreamDownloader;

public class ThemedMediaPickerActivity extends MediaPickerActivity {

    public static IntentBuilder withThemed(Context context) {
        final IntentBuilder builder = new IntentBuilder(context);
        builder.cropImageActivityClass(ImageCropperActivity.class);
        builder.streamDownloaderClass(RestFuNetworkStreamDownloader.class);
        return builder;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

}
