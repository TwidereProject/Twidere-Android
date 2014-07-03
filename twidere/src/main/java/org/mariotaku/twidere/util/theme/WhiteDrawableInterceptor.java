package org.mariotaku.twidere.util.theme;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.negusoft.holoaccent.AccentPalette;
import com.negusoft.holoaccent.drawable.RectDrawable;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.res.iface.IThemedResources.DrawableInterceptor;

public class WhiteDrawableInterceptor implements DrawableInterceptor {

	private static final int PRESSED_ALPHA = 0xAA;
	private static final int FOCUSED_ALPHA = 0x55;

	private final AccentPalette mAccentPalette;
	private final Resources mResources;

	public WhiteDrawableInterceptor(final Resources res) {
		mResources = res;
		mAccentPalette = new AccentPalette(Color.WHITE);
	}

	@Override
	public Drawable getDrawable(final Resources res, final int resId) {
		if (resId == R.drawable.solid_pressed_white_intercepted)
			return new ColorDrawable(mAccentPalette.getAccentColor(PRESSED_ALPHA));
		if (resId == R.drawable.solid_focused_white_intercepted)
			return new ColorDrawable(mAccentPalette.getAccentColor(FOCUSED_ALPHA));
		if (resId == R.drawable.rect_focused_background_white_intercepted) {
			final int backColor = mAccentPalette.getAccentColor(0x55);
			final int borderColor = mAccentPalette.getAccentColor(0xAA);
			return new RectDrawable(mResources, backColor, 2f, borderColor);
		}
		return null;
	}

}
