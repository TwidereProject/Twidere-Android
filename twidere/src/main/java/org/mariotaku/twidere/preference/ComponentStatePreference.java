package org.mariotaku.twidere.preference;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;


public class ComponentStatePreference extends SwitchPreferenceCompat {

    private final PackageManager mPackageManager;
    private final ComponentName mComponentName;

    public ComponentStatePreference(Context context) {
        super(context);
        mPackageManager = context.getPackageManager();
        mComponentName = getComponentName(context, null);
        setDefaultValue(isComponentEnabled());
    }

    public ComponentStatePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPackageManager = context.getPackageManager();
        mComponentName = getComponentName(context, attrs);
        setDefaultValue(isComponentEnabled());
    }

    public ComponentStatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPackageManager = context.getPackageManager();
        mComponentName = getComponentName(context, attrs);
        setDefaultValue(isComponentEnabled());
    }

    @Override
    public boolean shouldDisableDependents() {
        return getDisableDependentsState() || !isComponentAvailable();
    }

    @Override
    protected final Boolean onGetDefaultValue(@NonNull final TypedArray a, final int index) {
        return isComponentEnabled();
    }

    @Override
    protected final void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
        setChecked(getPersistedBoolean(true));
    }

    protected ComponentName getComponentName(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.name});
        final String name = a.getString(0);
        a.recycle();
        if (name == null) throw new NullPointerException();
        return new ComponentName(context.getPackageName(), name);
    }

    protected boolean isComponentAvailable() {
        return true;
    }

    @Override
    protected boolean shouldPersist() {
        return true;
    }

    @Override
    protected boolean persistBoolean(final boolean value) {
        final int newState = value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        mPackageManager.setComponentEnabledSetting(mComponentName, newState, PackageManager.DONT_KILL_APP);
        return true;
    }

    @Override
    protected boolean getPersistedBoolean(final boolean defaultReturnValue) {
        return isComponentEnabled();
    }

    @SuppressLint("InlinedApi")
    private boolean isComponentEnabled() {
        try {
            final int state = mPackageManager.getComponentEnabledSetting(mComponentName);
            return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
        } catch (NullPointerException e) {
            // Seems this will thrown on older devices
            return false;
        }
    }


}
