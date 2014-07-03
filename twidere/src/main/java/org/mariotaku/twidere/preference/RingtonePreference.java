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

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ArrayUtils;

public class RingtonePreference extends AutoInvalidateListPreference {

	private Ringtone[] mRingtones;
	private String[] mEntries, mValues;

	private int mSelectedItem;

	public RingtonePreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public int getSelectedItem() {
		return mSelectedItem;
	}

	public Ringtone getSelectedRingtone() {
		return mRingtones[mSelectedItem];
	}

	public void setSelectedItem(final int selected) {
		mSelectedItem = selected >= 0 && selected < mValues.length ? selected : 0;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		final Ringtone ringtone = getSelectedRingtone();
		if (ringtone != null && ringtone.isPlaying()) {
			ringtone.stop();
		}
		if (positiveResult && mSelectedItem >= 0 && mSelectedItem < mValues.length) {
			if (callChangeListener(mValues[mSelectedItem])) {
				persistString(mValues[mSelectedItem]);
			}
		}
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		loadRingtones(getContext());
		setSelectedItem(ArrayUtils.indexOf(mValues, getPersistedString(null)));
		builder.setSingleChoiceItems(getEntries(), getSelectedItem(), new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				setSelectedItem(which);
				final Ringtone ringtone = getSelectedRingtone();
				if (ringtone.isPlaying()) {
					ringtone.stop();
				}
				ringtone.play();
			}
		});
	}

	private void loadRingtones(final Context context) {
		final RingtoneManager manager = new RingtoneManager(context);
		manager.setType(RingtoneManager.TYPE_NOTIFICATION);
		final Cursor cur = manager.getCursor();
		cur.moveToFirst();
		final int count = cur.getCount();
		mRingtones = new Ringtone[count + 1];
		mEntries = new String[count + 1];
		mValues = new String[count + 1];
		final Uri default_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		final Ringtone default_ringtone = RingtoneManager.getRingtone(context, default_uri);
		mRingtones[0] = default_ringtone;
		mEntries[0] = context.getString(R.string.default_ringtone);
		mValues[0] = default_uri.toString();
		for (int i = 0; i < count; i++) {
			final Ringtone ringtone = manager.getRingtone(i);
			mRingtones[i + 1] = ringtone;
			mEntries[i + 1] = ringtone.getTitle(context);
			mValues[i + 1] = manager.getRingtoneUri(i).toString();
		}
		setEntries(mEntries);
		setEntryValues(mValues);
		cur.close();
	}

}
