package org.mariotaku.twidere.activity.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.pickncrop.library.ImagePickerActivity;
import org.mariotaku.twidere.activity.ImageCropperActivity;
import org.mariotaku.twidere.util.RestFuNetworkStreamDownloader;
import org.mariotaku.twidere.util.ThemeUtils;

public class ThemedImagePickerActivity extends ImagePickerActivity {

    @Override
    public void setTheme(final int resid) {
        super.setTheme(ThemeUtils.getNoDisplayThemeResource(this));
    }

    public static ThemedIntentBuilder withThemed(Context context) {
        return new ThemedIntentBuilder(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    public static final class ThemedIntentBuilder {
        private final Context context;
        private final IntentBuilder intentBuilder;

        public ThemedIntentBuilder(final Context context) {
            this.context = context;
            this.intentBuilder = new IntentBuilder(context);
            intentBuilder.cropImageActivityClass(ImageCropperActivity.class);
            intentBuilder.streamDownloaderClass(RestFuNetworkStreamDownloader.class);
        }

        public ThemedIntentBuilder takePhoto() {
            intentBuilder.takePhoto();
            return this;
        }

        public ThemedIntentBuilder getImage(@NonNull final Uri uri) {
            intentBuilder.getImage(uri);
            return this;
        }

        public Intent build() {
            final Intent intent = intentBuilder.build();
            intent.setClass(context, ThemedImagePickerActivity.class);
            return intent;
        }

        public ThemedIntentBuilder pickImage() {
            intentBuilder.pickImage();
            return this;
        }

        public ThemedIntentBuilder addEntry(final String name, final String value, final int result) {
            intentBuilder.addEntry(name, value, result);
            return this;
        }

        public ThemedIntentBuilder maximumSize(final int w, final int h) {
            intentBuilder.maximumSize(w, h);
            return this;
        }

        public ThemedIntentBuilder aspectRatio(final int x, final int y) {
            intentBuilder.aspectRatio(x, y);
            return this;
        }
    }


}
