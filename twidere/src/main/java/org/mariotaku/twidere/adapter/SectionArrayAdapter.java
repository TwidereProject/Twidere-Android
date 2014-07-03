package org.mariotaku.twidere.adapter;

import android.content.Context;

import java.util.Collection;

public class SectionArrayAdapter<T> extends ArrayAdapter<T> {

	public SectionArrayAdapter(final Context context, final int layoutRes) {
		super(context, layoutRes);
	}

	public SectionArrayAdapter(final Context context, final int layoutRes, final Collection<? extends T> collection) {
		super(context, layoutRes, collection);
	}

}
