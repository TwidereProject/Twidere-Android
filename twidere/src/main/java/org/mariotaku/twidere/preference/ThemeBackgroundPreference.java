package org.mariotaku.twidere.preference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.AlphaPatternDrawable;
import org.mariotaku.twidere.util.support.ViewSupport;

/**
 * Created by mariotaku on 14/11/8.
 */
public class ThemeBackgroundPreference extends DialogPreference implements Constants {

    public final static int MAX_ALPHA = 0xFF;
    public final static int MIN_ALPHA = 0x40;

    private final String[] mBackgroundEntries, mBackgroundValues;
    private String mValue;

    private View mAlphaContainer;
    private SeekBar mAlphaSlider;
    private ImageView mAlphaPreview;

    private OnClickListener mSingleChoiceListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String value = mBackgroundValues[which];
            setValue(value);
        }
    };

    private OnSeekBarChangeListener mAlphaSliderChangedListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateAlphaPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            final SharedPreferences preferences = getSharedPreferences();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_THEME_BACKGROUND_ALPHA, getSliderAlpha());
            editor.apply();
            persistValue(mValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedString(null) : (String) defaultValue);
        updateSummary();
    }

    private void updateSummary() {
        final int valueIndex = getValueIndex();
        setSummary(valueIndex != -1 ? mBackgroundEntries[valueIndex] : null);
    }

    private void setValue(String value) {
        mValue = value;
        updateAlphaVisibility();
    }

    private void persistValue(String value) {
        // Always persist/notify the first time.
        if (!TextUtils.equals(getPersistedString(null), value)) {
            persistString(value);
            notifyChanged();
        }
        updateAlphaVisibility();
        updateSummary();
    }

    private void updateAlphaVisibility() {
        if (mAlphaContainer == null) return;
        final boolean isTransparent = VALUE_THEME_BACKGROUND_TRANSPARENT.equals(mValue);
        mAlphaContainer.setVisibility(isTransparent ? View.VISIBLE : View.GONE);
    }

    public String getValue() {
        return mValue;
    }

    public ThemeBackgroundPreference(Context context) {
        this(context, null);
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public ThemeBackgroundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setKey(KEY_THEME_BACKGROUND);
        final Resources resources = context.getResources();
        mBackgroundEntries = resources.getStringArray(R.array.entries_theme_background);
        mBackgroundValues = resources.getStringArray(R.array.values_theme_background);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setSingleChoiceItems(mBackgroundEntries, getValueIndex(), mSingleChoiceListener);
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
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final Dialog dialog = getDialog();
        final SharedPreferences preferences = getSharedPreferences();
        if (dialog instanceof AlertDialog && preferences != null) {
            mValue = getPersistedString(null);
            final Resources res = dialog.getContext().getResources();
            final LayoutInflater inflater = dialog.getLayoutInflater();
            final ListView listView = ((AlertDialog) dialog).getListView();
            final ViewGroup listViewParent = (ViewGroup) listView.getParent();
            listViewParent.removeView(listView);
            final View view = inflater.inflate(R.layout.dialog_theme_background_preference, listViewParent);
            ((ViewGroup) view.findViewById(R.id.list_container)).addView(listView);
            mAlphaContainer = view.findViewById(R.id.alpha_container);
            mAlphaSlider = (SeekBar) view.findViewById(R.id.alpha_slider);
            mAlphaPreview = (ImageView) view.findViewById(R.id.alpha_preview);
            mAlphaSlider.setMax(MAX_ALPHA - MIN_ALPHA);
            mAlphaSlider.setOnSeekBarChangeListener(mAlphaSliderChangedListener);
            mAlphaSlider.setProgress(preferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA) - MIN_ALPHA);
            final int patternSize = res.getDimensionPixelSize(R.dimen.element_spacing_msmall);
            ViewSupport.setBackground(mAlphaPreview, new AlphaPatternDrawable(patternSize));
            updateAlphaVisibility();
            updateAlphaPreview();

            final int checkedIdx = findIndexOfValue(mValue);
            if (checkedIdx < 0) {
                listView.clearChoices();
            } else {
                listView.setItemChecked(checkedIdx, true);
            }
        }
    }

    private void updateAlphaPreview() {
        if (mAlphaPreview == null || mAlphaSlider == null) return;
        final Drawable drawable = mAlphaPreview.getDrawable();
        if (drawable == null) return;
        drawable.setAlpha(getSliderAlpha());
    }

    private int getSliderAlpha() {
        return mAlphaSlider.getProgress() + MIN_ALPHA;
    }
}
