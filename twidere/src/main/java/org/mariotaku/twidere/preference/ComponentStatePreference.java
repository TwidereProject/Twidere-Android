package org.mariotaku.twidere.preference;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class ComponentStatePreference extends CheckBoxPreference {

	private final PackageManager mPackageManager;
	private final ComponentName mComponentName;

	public ComponentStatePreference(final Context context) {
		this(context, null);
	}

	public ComponentStatePreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
	}

	public ComponentStatePreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.name });
		final String name = a.getString(0);
		if (name == null) throw new NullPointerException();
		mPackageManager = context.getPackageManager();
		mComponentName = new ComponentName(context.getPackageName(), name);
		setDefaultValue(isComponentEnabled());
	}

	@Override
	public boolean shouldDisableDependents() {
		final boolean disableDependentsState = getDisableDependentsState();
		final boolean value = isComponentEnabled();
		return disableDependentsState ? value : !value;
	}

	@Override
	protected boolean getPersistedBoolean(final boolean defaultReturnValue) {
		return isComponentEnabled();
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return isComponentEnabled();
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		setChecked(getPersistedBoolean(true));
	}

	@Override
	protected boolean persistBoolean(final boolean value) {
		final int newState = value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		mPackageManager.setComponentEnabledSetting(mComponentName, newState, PackageManager.DONT_KILL_APP);
		return true;
	}

	@Override
	protected boolean shouldPersist() {
		return true;
	}

	@SuppressLint("InlinedApi")
	private boolean isComponentEnabled() {
		final int state = mPackageManager.getComponentEnabledSetting(mComponentName);
		return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
				&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
	}

}
