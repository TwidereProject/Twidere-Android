package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;

import java.util.Locale;

/**
 * Created by mariotaku on 16/4/4.
 */
public class IncompatibleAlertActivity extends Activity {
    private TextView mInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_incompatible);

        mInfoText.append(String.format(Locale.US, "Twidere version %s (%d)\n",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        mInfoText.append(String.format(Locale.US, "Classpath %s\n", ClassLoader.getSystemClassLoader()));
        mInfoText.append(String.format(Locale.US, "Brand %s\n", Build.BRAND));
        mInfoText.append(String.format(Locale.US, "Device %s\n", Build.DEVICE));
        mInfoText.append(String.format(Locale.US, "Display %s\n", Build.DISPLAY));
        mInfoText.append(String.format(Locale.US, "Hardware %s\n", Build.HARDWARE));
        mInfoText.append(String.format(Locale.US, "Manufacturer %s\n", Build.MANUFACTURER));
        mInfoText.append(String.format(Locale.US, "Model %s\n", Build.MODEL));
        mInfoText.append(String.format(Locale.US, "Product %s\n", Build.PRODUCT));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mInfoText = (TextView) findViewById(R.id.info_text);
    }
}
