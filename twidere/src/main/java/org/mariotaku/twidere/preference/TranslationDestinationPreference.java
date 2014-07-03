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
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.api.HelpResources.Language;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

public class TranslationDestinationPreference extends Preference implements Constants, OnClickListener {

	private SharedPreferences mPreferences;

	private String mSelectedLanguageCode = "en";

	private GetLanguagesTask mGetAvailableTrendsTask;

	private final LanguagesAdapter mAdapter;

	private AlertDialog mDialog;

	public TranslationDestinationPreference(final Context context) {
		this(context, null);
	}

	public TranslationDestinationPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public TranslationDestinationPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new LanguagesAdapter(context);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final Language item = mAdapter.getItem(which);
		if (item != null) {
			editor.putString(KEY_TRANSLATION_DESTINATION, item.getCode());
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
		if (mGetAvailableTrendsTask != null) {
			mGetAvailableTrendsTask.cancel(false);
		}
		mGetAvailableTrendsTask = new GetLanguagesTask(getContext());
		mGetAvailableTrendsTask.execute();
	}

	private static class LanguageComparator implements Comparator<Language> {
		private final Collator mCollator;

		LanguageComparator(final Context context) {
			mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
		}

		@Override
		public int compare(final Language object1, final Language object2) {
			return mCollator.compare(object1.getName(), object2.getName());
		}

	}

	private static class LanguagesAdapter extends ArrayAdapter<Language> {

		private final Context mContext;

		public LanguagesAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_single_choice);
			mContext = context;
		}

		public int findItemPosition(final String code) {
			if (TextUtils.isEmpty(code)) return -1;
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				final Language item = getItem(i);
				if (code.equalsIgnoreCase(item.getCode())) return i;
			}
			return -1;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final Language item = getItem(position);
			if (item != null && text != null) {
				text.setSingleLine();
				text.setText(item.getName());
			}
			return view;
		}

		public void setData(final List<Language> data) {
			clear();
			if (data != null) {
				addAll(data);
			}
			sort(new LanguageComparator(mContext));
		}

	}

	class GetLanguagesTask extends AsyncTask<Void, Void, ResponseList<Language>> implements OnCancelListener {

		private final ProgressDialog mProgress;

		public GetLanguagesTask(final Context context) {
			mProgress = new ProgressDialog(context);
		}

		@Override
		public void onCancel(final DialogInterface dialog) {
			cancel(true);
		}

		@Override
		protected ResponseList<Language> doInBackground(final Void... args) {
			final Twitter twitter = getDefaultTwitterInstance(getContext(), false);
			final String pref = mPreferences.getString(KEY_TRANSLATION_DESTINATION, null);
			if (twitter == null) return null;
			try {
				if (pref == null) {
					mSelectedLanguageCode = twitter.getAccountSettings().getLanguage();
					final Editor editor = mPreferences.edit();
					editor.putString(KEY_TRANSLATION_DESTINATION, mSelectedLanguageCode);
					editor.apply();
				} else {
					mSelectedLanguageCode = pref;
				}
				return twitter.getLanguages();
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ResponseList<Language> result) {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mAdapter.setData(result);
			if (result == null) return;
			final AlertDialog.Builder selectorBuilder = new AlertDialog.Builder(getContext());
			selectorBuilder.setTitle(getTitle());
			selectorBuilder.setSingleChoiceItems(mAdapter, mAdapter.findItemPosition(mSelectedLanguageCode),
					TranslationDestinationPreference.this);
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
