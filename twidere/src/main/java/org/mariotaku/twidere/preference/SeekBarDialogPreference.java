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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.mariotaku.twidere.R;

/**
 * A {@link DialogPreference} that provides a user with the means to select an
 * integer from a {@link SeekBar}, and persist it.
 * 
 * @author lukehorvat
 */
public class SeekBarDialogPreference extends DialogPreference {
	private static final int DEFAULT_MIN_PROGRESS = 0;
	private static final int DEFAULT_MAX_PROGRESS = 100;
	private static final int DEFAULT_PROGRESS = 0;

	private int mMinProgress;
	private int mMaxProgress;
	private int mProgress;
	private CharSequence mProgressTextSuffix;
	private TextView mProgressText;
	private SeekBar mSeekBar;

	public SeekBarDialogPreference(final Context context) {
		this(context, null);
	}

	public SeekBarDialogPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public SeekBarDialogPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		// get attributes specified in XML
		final TypedArray a = context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.SeekBarDialogPreference, 0, 0);
		try {
			setMinProgress(a.getInteger(R.styleable.SeekBarDialogPreference_min, DEFAULT_MIN_PROGRESS));
			setMaxProgress(a.getInteger(R.styleable.SeekBarDialogPreference_max, DEFAULT_MAX_PROGRESS));
			setProgressTextSuffix(a.getString(R.styleable.SeekBarDialogPreference_progressTextSuffix));
		} finally {
			a.recycle();
		}

		// set layout
		setDialogLayoutResource(R.layout.preference_seek_bar_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	public int getMaxProgress() {
		return mMaxProgress;
	}

	public int getMinProgress() {
		return mMinProgress;
	}

	public int getProgress() {
		return mProgress;
	}

	public CharSequence getProgressTextSuffix() {
		return mProgressTextSuffix;
	}

	public void setMaxProgress(final int maxProgress) {
		mMaxProgress = maxProgress;
		setProgress(Math.min(mProgress, mMaxProgress));
	}

	public void setMinProgress(final int minProgress) {
		mMinProgress = minProgress;
		setProgress(Math.max(mProgress, mMinProgress));
	}

	public void setProgress(int progress) {
		progress = Math.max(Math.min(progress, mMaxProgress), mMinProgress);

		if (progress != mProgress) {
			mProgress = progress;
			persistInt(progress);
			notifyChanged();
		}
	}

	public void setProgressTextSuffix(final CharSequence progressTextSuffix) {
		mProgressTextSuffix = progressTextSuffix;
	}

	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);

		final CharSequence message = getDialogMessage();
		final TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
		dialogMessageText.setText(message);
		dialogMessageText.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);

		mProgressText = (TextView) view.findViewById(R.id.text_progress);

		mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				// update text that displays the current SeekBar progress value
				// note: this does not persist the progress value. that is only
				// ever done in setProgress()
				final String progressStr = String.valueOf(progress + mMinProgress);
				mProgressText.setText(mProgressTextSuffix == null ? progressStr : progressStr
						.concat(mProgressTextSuffix.toString()));
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
			}
		});
		mSeekBar.setMax(mMaxProgress - mMinProgress);
		mSeekBar.setProgress(mProgress - mMinProgress);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		// when the user selects "OK", persist the new value
		if (positiveResult) {
			final int seekBarProgress = mSeekBar.getProgress() + mMinProgress;
			if (callChangeListener(seekBarProgress)) {
				setProgress(seekBarProgress);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, DEFAULT_PROGRESS);
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		// check whether we saved the state in onSaveInstanceState()
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// restore the state
		final SavedState myState = (SavedState) state;
		setMinProgress(myState.minProgress);
		setMaxProgress(myState.maxProgress);
		setProgress(myState.progress);

		super.onRestoreInstanceState(myState.getSuperState());
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		// save the instance state so that it will survive screen orientation
		// changes and other events that may temporarily destroy it
		final Parcelable superState = super.onSaveInstanceState();

		// set the state's value with the class member that holds current
		// setting value
		final SavedState myState = new SavedState(superState);
		myState.minProgress = getMinProgress();
		myState.maxProgress = getMaxProgress();
		myState.progress = getProgress();

		return myState;
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		setProgress(restore ? getPersistedInt(DEFAULT_PROGRESS) : (Integer) defaultValue);
	}

	private static class SavedState extends BaseSavedState {
		int minProgress;
		int maxProgress;
		int progress;

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		public SavedState(final Parcel source) {
			super(source);

			minProgress = source.readInt();
			maxProgress = source.readInt();
			progress = source.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);

			dest.writeInt(minProgress);
			dest.writeInt(maxProgress);
			dest.writeInt(progress);
		}
	}
}
