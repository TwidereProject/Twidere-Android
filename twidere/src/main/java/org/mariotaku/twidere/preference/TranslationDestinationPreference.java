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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Language;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

public class TranslationDestinationPreference extends Preference implements OnClickListener {

    private String mSelectedLanguageCode = "en";

    private GetLanguagesTask mGetAvailableTrendsTask;

    private final LanguagesAdapter mAdapter;

    private AlertDialog mDialog;

    public TranslationDestinationPreference(final Context context) {
        this(context, null);
    }

    public TranslationDestinationPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public TranslationDestinationPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mAdapter = new LanguagesAdapter(context);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final Language item = mAdapter.getItem(which);
        if (item != null) {
            persistString(item.getCode());
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onClick() {
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

    class GetLanguagesTask extends AsyncTask<Object, Object, ResponseList<Language>> implements OnCancelListener {

        private final ProgressDialog mProgress;

        public GetLanguagesTask(final Context context) {
            mProgress = new ProgressDialog(context);
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            cancel(true);
        }

        @Override
        protected ResponseList<Language> doInBackground(final Object... args) {
            final MicroBlog twitter = MicroBlogAPIFactory.getDefaultTwitterInstance(getContext(), false);
            if (twitter == null) return null;
            try {
                mSelectedLanguageCode = twitter.getAccountSettings().getLanguage();
                return twitter.getLanguages();
            } catch (final MicroBlogException e) {
                Log.w(LOGTAG, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ResponseList<Language> result) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
            mAdapter.setData(result);
            if (result == null) return;
            final AlertDialog.Builder selectorBuilder = new AlertDialog.Builder(getContext());
            selectorBuilder.setTitle(getTitle());
            final String value = getPersistedString(mSelectedLanguageCode);
            selectorBuilder.setSingleChoiceItems(mAdapter, mAdapter.findItemPosition(value),
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
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
            mProgress.setMessage(getContext().getString(R.string.please_wait));
            mProgress.setOnCancelListener(this);
            mProgress.show();
        }

    }
}
