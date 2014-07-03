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

import static org.mariotaku.twidere.util.Utils.getDefaultTwitterInstance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

public class TrendsLocationPreference extends Preference implements Constants, OnClickListener {

	private SharedPreferences mPreferences;

	private int mCheckedWoeId = 1;

	private GetAvailableTrendsTask mGetAvailableTrendsTask;

	private final AvailableTrendsAdapter mAdapter;

	private AlertDialog mDialog;

	public TrendsLocationPreference(final Context context) {
		this(context, null);
	}

	public TrendsLocationPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public TrendsLocationPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new AvailableTrendsAdapter(context);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final Location item = mAdapter.getItem(which);
		if (item != null) {
			editor.putInt(KEY_LOCAL_TRENDS_WOEID, item.getWoeid());
			editor.commit();
		}
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	@Override
	protected void onClick() {
		mPreferences = getSharedPreferences();
		if (mPreferences == null) return;
		mCheckedWoeId = mPreferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1);
		if (mGetAvailableTrendsTask != null) {
			mGetAvailableTrendsTask.cancel(false);
		}
		mGetAvailableTrendsTask = new GetAvailableTrendsTask(getContext());
		mGetAvailableTrendsTask.execute();
	}

	private static class AvailableTrendsAdapter extends ArrayAdapter<Location> {

		private final Context mContext;

		public AvailableTrendsAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_single_choice);
			mContext = context;
		}

		public int findItemPosition(final int woeid) {
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				final Location item = getItem(i);
				if (item.getWoeid() == woeid) return i;
			}
			return -1;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final Location item = getItem(position);
			if (item != null && text != null) {
				text.setSingleLine();
				text.setText(item.getName());
			}
			return view;
		}

		public void setData(final List<Location> data) {
			clear();
			if (data != null) {
				addAll(data);
			}
			sort(new LocationComparator(mContext));
		}

	}

	private static class LocationComparator implements Comparator<Location> {
		private final Collator mCollator;

		LocationComparator(final Context context) {
			mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
		}

		@Override
		public int compare(final Location object1, final Location object2) {
			if (object1.getWoeid() == 1) return Integer.MIN_VALUE;
			if (object2.getWoeid() == 1) return Integer.MAX_VALUE;
			return mCollator.compare(object1.getName(), object2.getName());
		}

	}

	class GetAvailableTrendsTask extends AsyncTask<Void, Void, ResponseList<Location>> implements OnCancelListener {

		private final ProgressDialog mProgress;

		public GetAvailableTrendsTask(final Context context) {
			mProgress = new ProgressDialog(context);
		}

		@Override
		public void onCancel(final DialogInterface dialog) {
			cancel(true);
		}

		@Override
		protected ResponseList<Location> doInBackground(final Void... args) {
			final Twitter twitter = getDefaultTwitterInstance(getContext(), false);
			if (twitter == null) return null;
			try {
				return twitter.getAvailableTrends();
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ResponseList<Location> result) {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mAdapter.setData(result);
			if (result == null) return;
			final AlertDialog.Builder selectorBuilder = new AlertDialog.Builder(getContext());
			selectorBuilder.setTitle(getTitle());
			selectorBuilder.setSingleChoiceItems(mAdapter, mAdapter.findItemPosition(mCheckedWoeId),
					TrendsLocationPreference.this);
			selectorBuilder.setNegativeButton(android.R.string.cancel, null);
			mDialog = selectorBuilder.create();
			final ListView lv = mDialog.getListView();
			if (lv != null) {
				lv.setFastScrollEnabled(true);
			}
			mDialog.show();
		}

		@Override
		protected void onPreExecute() {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mProgress.setMessage(getContext().getString(R.string.please_wait));
			mProgress.setOnCancelListener(this);
			mProgress.show();
		}

	}
}
