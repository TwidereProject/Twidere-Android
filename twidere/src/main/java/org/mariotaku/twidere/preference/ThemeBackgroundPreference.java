package org.mariotaku.twidere.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SeekBar;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.extension.DialogExtensionsKt;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

/**
 * Created by mariotaku on 14/11/8.
 */
public class ThemeBackgroundPreference extends DialogPreference implements Constants,
        IDialogPreference {

    public final static int MAX_ALPHA = 0xFF;
    public final static int MIN_ALPHA = 0x40;

    private final String[] mBackgroundEntries, mBackgroundValues;
    private String mValue;
    private String mDefaultValue;


    public ThemeBackgroundPreference(Context context) {
        this(context, null);
    }

    public ThemeBackgroundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setKey(KEY_THEME_BACKGROUND);
        final Resources resources = context.getResources();
        mBackgroundEntries = resources.getStringArray(R.array.entries_theme_background);
        mBackgroundValues = resources.getStringArray(R.array.values_theme_background);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        mDefaultValue = (String) defaultValue;
        setValue(restorePersistedValue ? getPersistedString(null) : mDefaultValue);
        updateSummary();
    }

    private void updateSummary() {
        final int valueIndex = getValueIndex();
        setSummary(valueIndex != -1 ? mBackgroundEntries[valueIndex] : null);
    }

    private void persistValue(String value) {
        // Always persist/notify the first time.
        if (!TextUtils.equals(getPersistedString(null), value)) {
            persistString(value);
            callChangeListener(value);
            notifyChanged();
        }
        updateSummary();
    }

    public String getValue() {
        return mValue;
    }

    private void setValue(String value) {
        mValue = value;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public int findIndexOfValue(String value) {
        if (value != null && mBackgroundValues != null) {
            for (int i = mBackgroundValues.length - 1; i >= 0; i--) {
                if (mBackgroundValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void displayDialog(@NonNull PreferenceFragmentCompat fragment) {
        InternalDialogFragment df = InternalDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getParentFragmentManager(), getKey());
    }

    private void saveValue() {
        persistValue(mValue);
    }

    private void setSelectedOption(int which) {
        if (which < 0) {
            setValue(mDefaultValue);
        } else {
            setValue(mBackgroundValues[which]);
        }
    }

    public static class InternalDialogFragment extends PreferenceDialogFragmentCompat {

        private View mAlphaContainer;
        private SeekBar mAlphaSlider;

        public static InternalDialogFragment newInstance(String key) {
            final InternalDialogFragment df = new InternalDialogFragment();
            final Bundle args = new Bundle();
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        private int getSliderAlpha() {
            return mAlphaSlider.getProgress() + MIN_ALPHA;
        }

        @Override
        public void onDialogClosed(boolean positive) {
            if (!positive) return;
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final SharedPreferences preferences = preference.getSharedPreferences();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_THEME_BACKGROUND_ALPHA, getSliderAlpha());
            editor.apply();
            preference.saveValue();
        }

        private void updateAlphaVisibility() {
            if (mAlphaContainer == null) return;
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final boolean isTransparent = VALUE_THEME_BACKGROUND_TRANSPARENT.equals(preference.getValue());
            mAlphaContainer.setVisibility(isTransparent ? View.VISIBLE : View.GONE);
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final ThemeBackgroundPreference preference = (ThemeBackgroundPreference) getPreference();
            final SharedPreferences preferences = preference.getSharedPreferences();
            preference.setValue(preference.getPersistedString(null));
            builder.setTitle(preference.getDialogTitle());
            builder.setSingleChoiceItems(preference.mBackgroundEntries, preference.getValueIndex(), (dialog, which) -> {
                preference.setSelectedOption(which);
                updateAlphaVisibility();
            });
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final Dialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> {
                final AlertDialog alertDialog = (AlertDialog) dialog1;
                DialogExtensionsKt.applyTheme(alertDialog);
                if (preferences != null) {
                    final LayoutInflater inflater = alertDialog.getLayoutInflater();
                    final ListView listView = alertDialog.getListView();
                    assert listView != null;
                    final ViewGroup listViewParent = (ViewGroup) listView.getParent();
                    listViewParent.removeView(listView);
                    final View view = inflater.inflate(R.layout.dialog_theme_background_preference, listViewParent);
                    ((ViewGroup) view.findViewById(R.id.list_container)).addView(listView);
                    mAlphaContainer = view.findViewById(R.id.alpha_container);
                    mAlphaSlider = view.findViewById(R.id.alpha_slider);
                    mAlphaSlider.setMax(MAX_ALPHA - MIN_ALPHA);
                    mAlphaSlider.setProgress(preferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA) - MIN_ALPHA);
                    listView.setOnItemClickListener((parent, view1, position, id) -> {
                        preference.setSelectedOption(position);
                        updateAlphaVisibility();
                    });
                    updateAlphaVisibility();
                }
            });
            return dialog;
        }
    }
}
