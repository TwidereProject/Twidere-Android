package org.mariotaku.twidere.preference;

import android.content.Context;
import android.util.AttributeSet;

public class SummaryEditTextPreference extends AutoFixEditTextPreference {

	public SummaryEditTextPreference(final Context context) {
		super(context);
	}

	public SummaryEditTextPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SummaryEditTextPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		return getText();
	}

	@Override
	public void setText(final String text) {
		super.setText(text);
		setSummary(text);
	}

}
