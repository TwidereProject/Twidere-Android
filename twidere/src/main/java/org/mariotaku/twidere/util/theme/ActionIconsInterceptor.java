package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.iface.ITwidereContextWrapper;
import org.mariotaku.twidere.content.res.iface.IThemedResources.DrawableInterceptor;
import org.mariotaku.twidere.graphic.icon.TwidereIcon;
import org.mariotaku.twidere.util.ThemeUtils;

public class ActionIconsInterceptor implements DrawableInterceptor {

	private static final SparseArray<Icon> sIconMap = new SparseArray<Icon>();

	static {
		sIconMap.put(R.drawable.ic_iconic_action_twidere, TwidereIcon.TWIDERE);
		sIconMap.put(R.drawable.ic_iconic_action_twidere_square, TwidereIcon.TWIDERE_SQUARE);
		sIconMap.put(R.drawable.ic_iconic_action_web, TwidereIcon.WEB);
		sIconMap.put(R.drawable.ic_iconic_action_compose, TwidereIcon.COMPOSE);
		sIconMap.put(R.drawable.ic_iconic_action_color_palette, TwidereIcon.COLOR_PALETTE);
		sIconMap.put(R.drawable.ic_iconic_action_camera, TwidereIcon.CAMERA);
		sIconMap.put(R.drawable.ic_iconic_action_new_message, TwidereIcon.NEW_MESSAGE);
		sIconMap.put(R.drawable.ic_iconic_action_server, TwidereIcon.SERVER);
		sIconMap.put(R.drawable.ic_iconic_action_gallery, TwidereIcon.GALLERY);
		sIconMap.put(R.drawable.ic_iconic_action_save, TwidereIcon.SAVE);
		sIconMap.put(R.drawable.ic_iconic_action_star, TwidereIcon.STAR);
		sIconMap.put(R.drawable.ic_iconic_action_search, TwidereIcon.SEARCH);
		sIconMap.put(R.drawable.ic_iconic_action_retweet, TwidereIcon.RETWEET);
		sIconMap.put(R.drawable.ic_iconic_action_reply, TwidereIcon.REPLY);
		sIconMap.put(R.drawable.ic_iconic_action_delete, TwidereIcon.DELETE);
		sIconMap.put(R.drawable.ic_iconic_action_add, TwidereIcon.ADD);
		sIconMap.put(R.drawable.ic_iconic_action_share, TwidereIcon.SHARE);
		sIconMap.put(R.drawable.ic_iconic_action_inbox, TwidereIcon.INBOX);
		sIconMap.put(R.drawable.ic_iconic_action_outbox, TwidereIcon.OUTBOX);
		sIconMap.put(R.drawable.ic_iconic_action_copy, TwidereIcon.COPY);
		sIconMap.put(R.drawable.ic_iconic_action_select_all, TwidereIcon.SELECT_ALL);
		sIconMap.put(R.drawable.ic_iconic_action_translate, TwidereIcon.TRANSLATE);
		sIconMap.put(R.drawable.ic_iconic_action_user, TwidereIcon.USER);
		sIconMap.put(R.drawable.ic_iconic_action_accounts, TwidereIcon.USER_GROUP);
		sIconMap.put(R.drawable.ic_iconic_action_send, TwidereIcon.SEND);
		sIconMap.put(R.drawable.ic_iconic_action_edit, TwidereIcon.EDIT);
		sIconMap.put(R.drawable.ic_iconic_action_ok, TwidereIcon.OK);
		sIconMap.put(R.drawable.ic_iconic_action_cancel, TwidereIcon.CANCEL);
		sIconMap.put(R.drawable.ic_iconic_action_preferences, TwidereIcon.PREFERENCES);
		sIconMap.put(R.drawable.ic_iconic_action_mylocation, TwidereIcon.LOCATION_FOUND);
		sIconMap.put(R.drawable.ic_iconic_action_speaker_muted, TwidereIcon.SPEAKER_MUTED);
		sIconMap.put(R.drawable.ic_iconic_action_quote, TwidereIcon.QUOTE);
		sIconMap.put(R.drawable.ic_iconic_action_message, TwidereIcon.MESSAGE);
		sIconMap.put(R.drawable.ic_iconic_action_twitter, TwidereIcon.TWITTER);
		sIconMap.put(R.drawable.ic_iconic_action_home, TwidereIcon.HOME);
		sIconMap.put(R.drawable.ic_iconic_action_mention, TwidereIcon.AT);
		sIconMap.put(R.drawable.ic_iconic_action_hashtag, TwidereIcon.HASHTAG);
		sIconMap.put(R.drawable.ic_iconic_action_trends, TwidereIcon.TRENDS);
		sIconMap.put(R.drawable.ic_iconic_action_list, TwidereIcon.LIST);
		sIconMap.put(R.drawable.ic_iconic_action_staggered, TwidereIcon.STAGGERED);
		sIconMap.put(R.drawable.ic_iconic_action_tab, TwidereIcon.TAB);
		sIconMap.put(R.drawable.ic_iconic_action_extension, TwidereIcon.EXTENSION);
		sIconMap.put(R.drawable.ic_iconic_action_card, TwidereIcon.CARD);
		sIconMap.put(R.drawable.ic_iconic_action_refresh, TwidereIcon.REFRESH);
		sIconMap.put(R.drawable.ic_iconic_action_grid, TwidereIcon.GRID);
		sIconMap.put(R.drawable.ic_iconic_action_about, TwidereIcon.INFO);
		sIconMap.put(R.drawable.ic_iconic_action_more, TwidereIcon.MORE);
		sIconMap.put(R.drawable.ic_iconic_action_open_source, TwidereIcon.OPEN_SOURCE);
		sIconMap.put(R.drawable.ic_iconic_action_notification, TwidereIcon.NOTIFICATION);
		sIconMap.put(R.drawable.ic_iconic_action_interface, TwidereIcon.INTERFACE);
		sIconMap.put(R.drawable.ic_iconic_action_block, TwidereIcon.BLOCK);
		sIconMap.put(R.drawable.ic_iconic_action_warning, TwidereIcon.WARNING);
		sIconMap.put(R.drawable.ic_iconic_action_heart, TwidereIcon.HEART);
		sIconMap.put(R.drawable.ic_iconic_action_checked, TwidereIcon.CHECKED);
		sIconMap.put(R.drawable.ic_iconic_action_drafts, TwidereIcon.DRAFTS);
		sIconMap.put(R.drawable.ic_iconic_action_import, TwidereIcon.IMPORT);
		sIconMap.put(R.drawable.ic_iconic_action_export, TwidereIcon.EXPORT);
		sIconMap.put(R.drawable.ic_iconic_action_storage, TwidereIcon.STORAGE);
	}

	private static int MENU_ICON_SIZE_DP = 32;
	private final Context mContext;
	private final int mIconSize;
	private final int mIconColor;
	private final float mDensity;

	public ActionIconsInterceptor(final Context context, final DisplayMetrics dm, final int overrideThemeRes) {
		mContext = context;
		if (overrideThemeRes != 0) {
			mIconColor = ThemeUtils.getActionIconColor(overrideThemeRes);
		} else if (context instanceof ITwidereContextWrapper) {
			final int resId = ((ITwidereContextWrapper) context).getThemeResourceId();
			mIconColor = ThemeUtils.getActionIconColor(resId);
		} else {
			mIconColor = ThemeUtils.getActionIconColor(context);
		}
		mDensity = dm.density;
		mIconSize = Math.round(mDensity * MENU_ICON_SIZE_DP);
	}

	@Override
	public Drawable getDrawable(final Resources res, final int resId) {
		final Icon icon = sIconMap.get(resId, null);
		if (icon == null) return null;
		final IconicFontDrawable drawable = new IconicFontDrawable(mContext, icon);
		drawable.setIntrinsicWidth(mIconSize);
		drawable.setIntrinsicHeight(mIconSize);
		drawable.setIconColor(mIconColor);
		drawable.setBounds(0, 0, mIconSize, mIconSize);
		return drawable;
	}

}