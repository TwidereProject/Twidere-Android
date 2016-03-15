package org.mariotaku.twidere.preference;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.fragment.ThemedListPreferenceDialogFragmentCompat;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

/**
 * Created by mariotaku on 16/3/15.
 */
public class ThemedListPreference extends ListPreference implements IDialogPreference {
    public ThemedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThemedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedListPreference(Context context) {
        super(context);
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        ThemedListPreferenceDialogFragmentCompat df = ThemedListPreferenceDialogFragmentCompat.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }
}
