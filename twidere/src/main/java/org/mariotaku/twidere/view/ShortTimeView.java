/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.view.themed.ThemedTextView;

public class ShortTimeView extends ThemedTextView implements Constants, OnSharedPreferenceChangeListener {

	private static final long TICKER_DURATION = 5000L;

	private final Runnable mTicker;

	private boolean mShowAbsoluteTime;
	private long mTime;

	private final SharedPreferences mPreferences;

	public ShortTimeView(final Context context) {
		this(context, null);
	}

	public ShortTimeView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public ShortTimeView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mTicker = new TickerRunnable(this);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences != null) {
			mPreferences.registerOnSharedPreferenceChangeListener(this);
		}
		updateTimeDisplayOption();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (KEY_SHOW_ABSOLUTE_TIME.equals(key)) {
			updateTimeDisplayOption();
			invalidateTime();
		}
	}

	public void setTime(final long time) {
		mTime = time;
		invalidateTime();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		post(mTicker);
	}

	@Override
	protected void onDetachedFromWindow() {
		removeCallbacks(mTicker);
		super.onDetachedFromWindow();
	}

	private void invalidateTime() {
		if (mShowAbsoluteTime) {
			setText(formatSameDayTime(getContext(), mTime));
		} else {
			setText(getRelativeTimeSpanString(mTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL));
		}
	}

	private void updateTimeDisplayOption() {
		if (mPreferences == null) return;
		mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false);
	}

	private static class TickerRunnable implements Runnable {

		private final ShortTimeView mTextView;

		private TickerRunnable(final ShortTimeView view) {
			mTextView = view;
		}

		@Override
		public void run() {
			final Handler handler = mTextView.getHandler();
			if (handler == null) return;
			mTextView.invalidateTime();
			final long now = SystemClock.uptimeMillis();
			final long next = now + TICKER_DURATION - now % TICKER_DURATION;
			handler.postAtTime(this, next);
		}
	}

}
