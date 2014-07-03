package org.mariotaku.twidere.preference;

import android.content.Context;
import android.util.AttributeSet;

public class SummaryListPreference extends AutoInvalidateListPreference {

	public SummaryListPreference(final Context context) {
		super(context);
	}

	public SummaryListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public CharSequence getSummary() {
		return getEntry();
	}

}
