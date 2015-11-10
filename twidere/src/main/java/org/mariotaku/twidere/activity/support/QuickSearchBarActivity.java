/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.activity.support;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory;
import org.mariotaku.twidere.provider.TwidereDataStore.Suggestions;
import org.mariotaku.twidere.util.EditTextEnterHandler;
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SwipeDismissListViewTouchListener;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.view.ExtendedRelativeLayout;
import org.mariotaku.twidere.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.util.List;

import jopt.csp.util.SortableIntList;

/**
 * Created by mariotaku on 15/1/6.
 */
public class QuickSearchBarActivity extends ThemedFragmentActivity implements OnClickListener,
        LoaderCallbacks<Cursor>, OnItemSelectedListener, OnItemClickListener,
        OnFitSystemWindowsListener, SwipeDismissListViewTouchListener.DismissCallbacks {

    private Spinner mAccountSpinner;
    private EditText mSearchQuery;
    private View mSearchSubmit;
    private ListView mSuggestionsList;
    private SuggestionsAdapter mUsersSearchAdapter;
    private ExtendedRelativeLayout mMainContent;
    private Rect mSystemWindowsInsets = new Rect();
    private boolean mTextChanged;

    @Override
    public boolean canDismiss(int position) {
        return mUsersSearchAdapter.getItemViewType(position) == SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY;
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        final Long[] ids = new Long[reverseSortedPositions.length];
        for (int i = 0, j = reverseSortedPositions.length; i < j; i++) {
            final int position = reverseSortedPositions[i];
            final SuggestionItem item = mUsersSearchAdapter.getSuggestionItem(position);
            ids[i] = item._id;
        }
        mUsersSearchAdapter.addRemovedPositions(reverseSortedPositions);
        final ContentResolver cr = getContentResolver();
        ContentResolverUtils.bulkDelete(cr, SearchHistory.CONTENT_URI, SearchHistory._ID, ids,
                null, false);
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getQuickSearchBarThemeResource(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_submit: {
                doSearch();
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mMainContent = (ExtendedRelativeLayout) findViewById(R.id.main_content);
        mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
        mSearchQuery = (EditText) findViewById(R.id.search_query);
        mSearchSubmit = findViewById(R.id.search_submit);
        mSuggestionsList = (ListView) findViewById(R.id.suggestions_list);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final long accountId = getAccountId();
        final Uri.Builder builder = Suggestions.Search.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(QUERY_PARAM_QUERY, ParseUtils.parseString(mSearchQuery.getText()));
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, ParseUtils.parseString(accountId));
        return new CursorLoader(this, builder.build(), Suggestions.Search.COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mUsersSearchAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUsersSearchAdapter.changeCursor(null);
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        mSystemWindowsInsets.set(insets);
        updateWindowAttributes();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SuggestionItem item = mUsersSearchAdapter.getSuggestionItem(position);
        switch (mUsersSearchAdapter.getItemViewType(position)) {
            case SuggestionsAdapter.VIEW_TYPE_USER_SUGGESTION_ITEM: {
                Utils.openUserProfile(this, getAccountId(), item.extra_id, item.summary, null);
                finish();
                break;
            }
            case SuggestionsAdapter.VIEW_TYPE_USER_SCREEN_NAME: {
                Utils.openUserProfile(this, getAccountId(), -1, item.title, null);
                finish();
                break;
            }
            case SuggestionsAdapter.VIEW_TYPE_SAVED_SEARCH:
            case SuggestionsAdapter.VIEW_TYPE_SEARCH_HISTORY: {
                Utils.openSearch(this, getAccountId(), item.title);
                finish();
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action) && mSearchQuery.length() == 0) {
            if (!mTextChanged) {
                onBackPressed();
            } else {
                mTextChanged = false;
            }
            return true;
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search_bar);
        final List<ParcelableCredentials> accounts = ParcelableAccount.getCredentialsList(this, false);
        final AccountsSpinnerAdapter accountsSpinnerAdapter = new AccountsSpinnerAdapter(this, R.layout.spinner_item_account_icon);
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_user);
        accountsSpinnerAdapter.addAll(accounts);
        mAccountSpinner.setAdapter(accountsSpinnerAdapter);
        mAccountSpinner.setOnItemSelectedListener(this);
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final int index = accountsSpinnerAdapter.findItemPosition(intent.getLongExtra(EXTRA_ACCOUNT_ID, -1));
            if (index != -1) {
                mAccountSpinner.setSelection(index);
            }
        }
        mMainContent.setOnFitSystemWindowsListener(this);
        mUsersSearchAdapter = new SuggestionsAdapter(this);
        mSuggestionsList.setAdapter(mUsersSearchAdapter);
        mSuggestionsList.setOnItemClickListener(this);

        final SwipeDismissListViewTouchListener listener = new SwipeDismissListViewTouchListener(mSuggestionsList, this);
        mSuggestionsList.setOnTouchListener(listener);
        mSuggestionsList.setOnScrollListener(listener.makeScrollListener());
        mSearchSubmit.setOnClickListener(this);

        EditTextEnterHandler.attach(mSearchQuery, new EnterListener() {
            @Override
            public boolean shouldCallListener() {
                return true;
            }

            @Override
            public boolean onHitEnter() {
                doSearch();
                return true;
            }
        }, true);
        mSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTextChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                getSupportLoaderManager().restartLoader(0, null, QuickSearchBarActivity.this);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWindowAttributes();
    }

    private void doSearch() {
        if (isFinishing()) return;
        final String query = ParseUtils.parseString(mSearchQuery.getText());
        if (TextUtils.isEmpty(query)) return;
        final long accountId = mAccountSpinner.getSelectedItemId();
        Utils.openSearch(this, accountId, query);
        finish();
    }

    private long getAccountId() {
        return mAccountSpinner.getSelectedItemId();
    }

    private void updateWindowAttributes() {
        final Window window = getWindow();
        final WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        attributes.y = mSystemWindowsInsets.top;
        window.setAttributes(attributes);
    }

    private void setSearchQuery(String query) {
        mSearchQuery.setText(query);
        if (query == null) return;
        mSearchQuery.setSelection(query.length());
    }

    static class SuggestionItem {


        public final String title, summary;
        public final long _id, extra_id;

        public SuggestionItem(Cursor cursor, SuggestionsAdapter.Indices indices) {
            _id = cursor.getLong(indices._id);
            title = cursor.getString(indices.title);
            summary = cursor.getString(indices.summary);
            extra_id = cursor.getLong(indices.extra_id);
        }
    }

    public static class SuggestionsAdapter extends CursorAdapter implements OnClickListener {

        static final int VIEW_TYPE_SEARCH_HISTORY = 0;
        static final int VIEW_TYPE_SAVED_SEARCH = 1;
        static final int VIEW_TYPE_USER_SUGGESTION_ITEM = 2;
        static final int VIEW_TYPE_USER_SCREEN_NAME = 3;

        private final LayoutInflater mInflater;
        private final MediaLoaderWrapper mImageLoader;
        private final UserColorNameManager mUserColorNameManager;
        private final QuickSearchBarActivity mActivity;
        private final SortableIntList mRemovedPositions;
        private Indices mIndices;

        SuggestionsAdapter(QuickSearchBarActivity activity) {
            super(activity, null, 0);
            mRemovedPositions = new SortableIntList();
            mActivity = activity;
            mImageLoader = activity.mImageLoader;
            mUserColorNameManager = activity.mUserColorNameManager;
            mInflater = LayoutInflater.from(activity);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            switch (getActualItemViewType(cursor.getPosition())) {
                case VIEW_TYPE_SEARCH_HISTORY:
                case VIEW_TYPE_SAVED_SEARCH: {
                    final View view = mInflater.inflate(R.layout.list_item_suggestion_search, parent, false);
                    final SearchViewHolder holder = new SearchViewHolder(view);
                    holder.edit_query.setOnClickListener(this);
                    view.setTag(holder);
                    return view;
                }
                case VIEW_TYPE_USER_SUGGESTION_ITEM:
                case VIEW_TYPE_USER_SCREEN_NAME: {
                    final View view = mInflater.inflate(R.layout.list_item_suggestion_user, parent, false);
                    view.setTag(new UserViewHolder(view));
                    return view;
                }
            }
            throw new UnsupportedOperationException("Unknown viewType");
        }

        public SuggestionItem getSuggestionItem(int position) {
            final Cursor cursor = (Cursor) getItem(position);
            return new SuggestionItem(cursor, mIndices);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            switch (getActualItemViewType(cursor.getPosition())) {
                case VIEW_TYPE_SEARCH_HISTORY: {
                    final SearchViewHolder holder = (SearchViewHolder) view.getTag();
                    final String title = cursor.getString(mIndices.title);
                    holder.edit_query.setTag(title);
                    holder.text1.setText(title);
                    holder.icon.setImageResource(R.drawable.ic_action_history);
                    break;
                }
                case VIEW_TYPE_SAVED_SEARCH: {
                    final SearchViewHolder holder = (SearchViewHolder) view.getTag();
                    final String title = cursor.getString(mIndices.title);
                    holder.edit_query.setTag(title);
                    holder.text1.setText(title);
                    holder.icon.setImageResource(R.drawable.ic_action_save);
                    break;
                }
                case VIEW_TYPE_USER_SUGGESTION_ITEM: {
                    final UserViewHolder holder = (UserViewHolder) view.getTag();
                    holder.text1.setText(mUserColorNameManager.getUserNickname(cursor.getLong(mIndices.extra_id),
                            cursor.getString(mIndices.title), false));
                    holder.text2.setVisibility(View.VISIBLE);
                    holder.text2.setText(String.format("@%s", cursor.getString(mIndices.summary)));
                    holder.icon.clearColorFilter();
                    mImageLoader.displayProfileImage(holder.icon, cursor.getString(mIndices.icon));
                    break;
                }
                case VIEW_TYPE_USER_SCREEN_NAME: {
                    final UserViewHolder holder = (UserViewHolder) view.getTag();
                    holder.text1.setText(String.format("@%s", cursor.getString(mIndices.title)));
                    holder.text2.setVisibility(View.GONE);
                    holder.icon.setColorFilter(holder.text1.getCurrentTextColor(), Mode.SRC_ATOP);
                    mImageLoader.cancelDisplayTask(holder.icon);
                    holder.icon.setImageResource(R.drawable.ic_action_user);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return getActualItemViewType(getActualPosition(position));
        }

        public int getActualItemViewType(int position) {
            final Cursor cursor = (Cursor) super.getItem(position);
            switch (cursor.getString(mIndices.type)) {
                case Suggestions.Search.TYPE_SAVED_SEARCH: {
                    return VIEW_TYPE_SAVED_SEARCH;
                }
                case Suggestions.Search.TYPE_SCREEN_NAME: {
                    return VIEW_TYPE_USER_SCREEN_NAME;
                }
                case Suggestions.Search.TYPE_SEARCH_HISTORY: {
                    return VIEW_TYPE_SEARCH_HISTORY;
                }
                case Suggestions.Search.TYPE_USER: {
                    return VIEW_TYPE_USER_SUGGESTION_ITEM;
                }
            }
            return IGNORE_ITEM_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit_query: {
                    mActivity.setSearchQuery((String) v.getTag());
                    break;
                }
            }
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor != null) {
                mIndices = new Indices(newCursor);
            } else {
                mIndices = null;
            }
            mRemovedPositions.clear();
            return super.swapCursor(newCursor);
        }

        @Override
        public int getCount() {
            if (mRemovedPositions == null) return super.getCount();
            return super.getCount() - mRemovedPositions.size();
        }

        @Override
        public Object getItem(int position) {
            return super.getItem(getActualPosition(position));
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(getActualPosition(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(getActualPosition(position), convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return super.getDropDownView(getActualPosition(position), convertView, parent);
        }

        private int getActualPosition(int position) {
            if (mRemovedPositions == null) return position;
            int skipped = 0;
            for (int i = 0, j = mRemovedPositions.size(); i < j; i++) {
                if (position + skipped >= mRemovedPositions.get(i)) {
                    skipped++;
                }
            }
            return position + skipped;
        }

        public void addRemovedPositions(int[] positions) {
            for (int position : positions) {
                mRemovedPositions.add(getActualPosition(position));
            }
            mRemovedPositions.sort();
            notifyDataSetChanged();
        }

        static class SearchViewHolder {

            private final ImageView icon;
            private final TextView text1;
            private final View edit_query;

            SearchViewHolder(View view) {
                icon = (ImageView) view.findViewById(android.R.id.icon);
                text1 = (TextView) view.findViewById(android.R.id.text1);
                edit_query = view.findViewById(R.id.edit_query);
            }

        }

        static class UserViewHolder {

            private final ImageView icon;
            private final TextView text1;
            private final TextView text2;

            UserViewHolder(View view) {
                icon = (ImageView) view.findViewById(android.R.id.icon);
                text1 = (TextView) view.findViewById(android.R.id.text1);
                text2 = (TextView) view.findViewById(android.R.id.text2);
            }
        }

        private static class Indices {
            private final int _id;
            private final int type;
            private final int title;
            private final int summary;
            private final int icon;
            private final int extra_id;

            public Indices(Cursor cursor) {
                _id = cursor.getColumnIndex(Suggestions._ID);
                type = cursor.getColumnIndex(Suggestions.TYPE);
                title = cursor.getColumnIndex(Suggestions.TITLE);
                summary = cursor.getColumnIndex(Suggestions.SUMMARY);
                icon = cursor.getColumnIndex(Suggestions.ICON);
                extra_id = cursor.getColumnIndex(Suggestions.EXTRA_ID);
            }
        }
    }

}
