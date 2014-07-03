package org.mariotaku.twidere.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.negusoft.holoaccent.AccentResources;

import org.mariotaku.twidere.content.res.iface.IThemedResources;

public class TwidereAccentResources extends AccentResources implements IThemedResources {

	private final Helper mHelper;

	public TwidereAccentResources(final Context context, final Resources res, final int overrideThemeRes,
			final int accentColor) {
		super(context, res, accentColor);
		mHelper = new Helper(this, context, overrideThemeRes);
	}

	@Override
	public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
		mHelper.addDrawableInterceptor(interceptor);
	}

	@Override
	public Drawable getDrawable(final int id) throws NotFoundException {
		final Drawable d = mHelper.getDrawable(id);
		if (d != null) return d;
		return super.getDrawable(id);
	}

}
