package org.mariotaku.twidere.activity.support;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;

import com.negusoft.holoaccent.AccentHelper;
import com.negusoft.holoaccent.AccentResources;

/**
 * A copy of AccentActivity that extends FragmentActivity in stead of Activity.
 * <br/><br/>
 * You can copy this file into your project and make your activities extend it if you
 * are using the support library.
 */
public class AccentFragmentActivity extends FragmentActivity {

    private AccentHelper mAccentHelper;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mAccentHelper = new AccentHelper(getOverrideAccentColor(),
                getOverrideAccentColorDark(), getOverrideAccentColorActionBar(), new MyInitListener());
    }

    @Override
    public Resources getResources() {
        if (mAccentHelper == null) {
            return super.getResources();
        }
        return mAccentHelper.getResources(this, super.getResources());
    }

    public final Resources getDefaultResources() {
        return super.getResources();
    }

    /**
     * Override this method to set the accent color programmatically.
     *
     * @return The color to override. If the color is equals to 0, the
     * accent color will be taken from the theme.
     */
    public int getOverrideAccentColor() {
        return 0;
    }

    /**
     * Override this method to set the dark variant of the accent color programmatically.
     *
     * @return The color to override. If the color is equals to 0, the dark version will be
     * taken from the theme. If it is specified in the theme either, it will be calculated
     * based on the accent color.
     */
    public int getOverrideAccentColorDark() {
        return 0;
    }

    /**
     * Override this method to set the action bar variant of the accent color programmatically.
     *
     * @return The color to override. If the color is equals to 0, the action bar version will
     * be taken from the theme. If it is specified in the theme either, it will the same as the
     * accent color.
     */
    public int getOverrideAccentColorActionBar() {
        return 0;
    }

    /**
     * Getter for the AccentHelper instance.
     */
    public AccentHelper getAccentHelper() {
        return mAccentHelper;
    }

    /**
     * Override this function to modify the AccentResources instance. You can add your own logic
     * to the default HoloAccent behaviour.
     */
    public void onInitAccentResources(AccentResources resources) {
        // To be overriden in child classes.
    }

    private class MyInitListener implements AccentHelper.OnInitListener {
        @Override
        public void onInitResources(AccentResources resources) {
            onInitAccentResources(resources);
        }
    }

}
