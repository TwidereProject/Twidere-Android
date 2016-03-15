package org.mariotaku.twidere.preference;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.fragment.ThemedEditTextPreferenceDialogFragmentCompat;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

/**
 * Created by mariotaku on 16/3/15.
 */
public class ThemedEditTextPreference extends EditTextPreference implements IDialogPreference {
    public ThemedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThemedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        ThemedEditTextPreferenceDialogFragmentCompat df = ThemedEditTextPreferenceDialogFragmentCompat.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }
}
