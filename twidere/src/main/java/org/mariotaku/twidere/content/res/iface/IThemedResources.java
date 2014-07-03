package org.mariotaku.twidere.content.res.iface;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import org.mariotaku.twidere.util.theme.ActionIconsInterceptor;
import org.mariotaku.twidere.util.theme.ActivityIconsInterceptor;
import org.mariotaku.twidere.util.theme.WhiteDrawableInterceptor;

import java.util.ArrayList;

public interface IThemedResources {

	public static final String RESOURCES_LOGTAG = "Twidere.Resources";

	public void addDrawableInterceptor(final DrawableInterceptor interceptor);

	public interface DrawableInterceptor {

		public Drawable getDrawable(final Resources res, final int resId);
	}

	public static final class Helper {

		private final ArrayList<DrawableInterceptor> mDrawableInterceptors = new ArrayList<DrawableInterceptor>();
		private final Resources mResources;

		public Helper(final Resources res, final Context context, final int overrideThemeRes) {
			mResources = res;
			final DisplayMetrics dm = res.getDisplayMetrics();
			addDrawableInterceptor(new ActionIconsInterceptor(context, dm, overrideThemeRes));
			addDrawableInterceptor(new ActivityIconsInterceptor(context, dm, overrideThemeRes));
			addDrawableInterceptor(new WhiteDrawableInterceptor(res));
		}

		public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
			mDrawableInterceptors.add(interceptor);
		}

		public Drawable getDrawable(final int resId) throws NotFoundException {
			for (final DrawableInterceptor interceptor : mDrawableInterceptors) {
				final Drawable d = interceptor.getDrawable(mResources, resId);
				if (d != null) return d;
			}
			return null;
		}
	}

}
