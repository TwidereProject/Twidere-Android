package org.mariotaku.twidere.graphic.icon;

import android.content.Context;
import android.graphics.Typeface;

import com.atermenji.android.iconicdroid.icon.Icon;
import com.atermenji.android.iconicdroid.util.TypefaceManager.IconicTypeface;

public enum TwidereIcon implements Icon {
	// Brand icons
	TWIDERE(0xF000), OPEN_SOURCE(0xF001), TWITTER(0xF002), TRANSLATE(0xF003), TWIDERE_SQUARE(0xF010),

	ADD(0xF020), EDIT(0xF021), SAVE(0xF022), DELETE(0xF023), OK(0xF024), CANCEL(0xF025), REFRESH(0xF026), MORE(0xF027), COPY(
			0xF028), CUT(0xF029), PASTE(0xF02A), SELECT_ALL(0xF02B), STAR(0xF02C), SEND(0xF02D), SHARE(0xF02E), NEW_MESSAGE(
			0xF02F), CHECKED(0xF030), IMPORT(0xF034), EXPORT(0xF035),

	PREFERENCES(0xF040), SEARCH(0xF041), INFO(0xF042), HELP(0xF043), COLLAPSE(0xF044), EXPAND(0xF045), PREVIOUS(0xF046), NEXT(
			0xF047), MIC(0xF048), MIC_MUTED(0xF049), SPEAKER(0xF04A), SPEAKER_MUTED(0xF04B), LOCK(0xF04C), UNLOCK(
			0xF04D), WARNING(0xF04E), ERROR(0xF04F),

	NOTIFICATION(0xF050), LOCATION_OFF(0xF058), LOCATION_SEARCHING(0xF059), LOCATION_FOUND(0xF05A), CAMERA(0xF05C), CAMCORDER(
			0xF05D), GALLERY(0xF05E),

	USER(0xF068), USERS(0xF069), USER_GROUP(0xF06A),

	HOME(0xF080), MESSAGE(0xF081), INBOX(0xF082), OUTBOX(0xF083), LIST(0xF084), GRID(0xF085), STACK(0xF086), STAGGERED(
			0xF087), REPLY(0xF088), RETWEET(0xF089), QUOTE(0xF08A), AT(0xF08B), COMPOSE(0xF08C), HEART(0xF08D), COLOR_PALETTE(
			0xF08E), BLOCK(0xF08F), HASHTAG(0xF090), TRENDS(0xF091), DRAFTS(0xF092),

	TAB(0xF0A0), EXTENSION(0xF0A1), CARD(0xF0A2), INTERFACE(0xF0A3), WEB(0xF0A4), SERVER(0xF0A5), STORAGE(0xF0A6);

	private final int mIconUtfValue;

	private TwidereIcon(final int iconUtfValue) {
		mIconUtfValue = iconUtfValue;
	}

	@Override
	public IconicTypeface getIconicTypeface() {
		return TwidereIconTypeface.SINGLETON;
	}

	@Override
	public int getIconUtfValue() {
		return mIconUtfValue;
	}

	private static final class TwidereIconTypeface implements IconicTypeface {

		static final TwidereIconTypeface SINGLETON = new TwidereIconTypeface();

		private Typeface mTypeface;

		@Override
		public Typeface getTypeface(final Context context) {
			if (mTypeface == null) {
				mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/TwidereIconic.ttf");
			}
			return mTypeface;
		}
	}
}
