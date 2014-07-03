package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.content.res.iface.IThemedResources.DrawableInterceptor;
import org.mariotaku.twidere.graphic.icon.TwidereIcon;
import org.mariotaku.twidere.util.ThemeUtils;

public class ActivityIconsInterceptor implements DrawableInterceptor {

	private static final SparseArray<IconSpec> sIconMap = new SparseArray<IconSpec>();

	static {
		sIconMap.put(R.drawable.ic_iconic_twidere, new IconSpec(TwidereIcon.TWIDERE, 48));
		sIconMap.put(R.drawable.ic_iconic_profile_image_default, new IconSpec(TwidereIcon.TWITTER, 128, 0xFF00ABEC));
	}

	private static int MENU_ICON_SIZE_DP = 48;
	private final Context mContext;
	private final int mIconSize;
	private final int mIconColor;
	private final float mDensity;

	public ActivityIconsInterceptor(final Context context, final DisplayMetrics dm, final int overrideThemeRes) {
		mContext = context;
		if (overrideThemeRes != 0) {
			mIconColor = ThemeUtils.getActionIconColor(overrideThemeRes);
		} else if (context instanceof TwidereContextThemeWrapper) {
			final int resId = ((TwidereContextThemeWrapper) context).getThemeResourceId();
			mIconColor = ThemeUtils.getActionIconColor(resId);
		} else {
			mIconColor = ThemeUtils.getActionIconColor(context);
		}
		mDensity = dm.density;
		mIconSize = Math.round(mDensity * MENU_ICON_SIZE_DP);
	}

	@Override
	public Drawable getDrawable(final Resources res, final int resId) {
		final IconSpec spec = sIconMap.get(resId, null);
		if (spec == null) return null;
		final IconicFontDrawable drawable = new IconicFontDrawable(mContext, spec.icon);
		drawable.setIntrinsicWidth(mIconSize);
		drawable.setIntrinsicHeight(mIconSize);
		drawable.setIconColor(spec.color == 0 ? mIconColor : spec.color);
		return drawable;
	}

	private static class IconSpec {
		private final Icon icon;
		private final float size;
		private final int color;

		IconSpec(final Icon icon, final float size) {
			this(icon, size, 0);
		}

		IconSpec(final Icon icon, final float size, final int color) {
			this.icon = icon;
			this.size = size;
			this.color = color;
		}
	}

}