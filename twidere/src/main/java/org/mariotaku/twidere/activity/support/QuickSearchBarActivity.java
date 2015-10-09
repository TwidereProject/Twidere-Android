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
import android.support.v4.content.AsyncTaskLoader;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.QuickSearchBarActivity.SuggestionItem;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUser.CachedIndices;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory;
import org.mariotaku.twidere.util.EditTextEnterHandler;
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SwipeDismissListViewTouchListener;
import org.mariotaku.twidere.util.SwipeDismissListViewTouchListener.DismissCallbacks;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.view.ExtendedRelativeLayout;
import org.mariotaku.twidere.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 15/1/6.
 */
public class QuickSearchBarActivity extends ThemedFragmentActivity implements OnClickListener,
        LoaderCallbacks<List<SuggestionItem>>, OnItemSelectedListener, OnItemClickListener,
        DismissCallbacks, OnFitSystemWindowsListener {

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
        return mUsersSearchAdapter.canDismiss(position);
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        final long[] ids = new long[reverseSortedPositions.length];
        for (int i = 0, j = reverseSortedPositions.length; i < j; i++) {
            final int position = reverseSortedPositions[i];
            final SearchHistoryItem item = (SearchHistoryItem) mUsersSearchAdapter.getItem(position);
            mUsersSearchAdapter.removeItemAt(position);
            ids[i] = item.getCursorId();
        }
        final ContentResolver cr = getContentResolver();
        final Long[] idsObject = ArrayUtils.toObject(ids);
        ContentResolverUtils.bulkDelete(cr, SearchHistory.CONTENT_URI, SearchHistory._ID, idsObject,
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
    public Loader<List<SuggestionItem>> onCreateLoader(int id, Bundle args) {
        return new SuggestionsLoader(this, mAccountSpinner.getSelectedItemId(), mSearchQuery.getText().toString());
    }

    @Override
    public void onLoadFinished(Loader<List<SuggestionItem>> loader, List<SuggestionItem> data) {
        mUsersSearchAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<SuggestionItem>> loader) {
        mUsersSearchAdapter.setData(null);
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        mSystemWindowsInsets.set(insets);
        updateWindowAttributes();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SuggestionItem item = mUsersSearchAdapter.getItem(position);
        item.onItemClick(this, position);
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
                if (Utils.removeLineBreaks(s)) {
                    doSearch();
                } else {
                    getSupportLoaderManager().restartLoader(0, null, QuickSearchBarActivity.this);
                }
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

    private static int getHistorySize(CharSequence query) {
        return TextUtils.isEmpty(query) ? 3 : 2;
    }

    private void updateWindowAttributes() {
        final Window window = getWindow();
        final WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        attributes.y = mSystemWindowsInsets.top;
        window.setAttributes(attributes);
    }

    interface SuggestionItem {

        void bindView(SuggestionsAdapter adapter, View view, int position);

        int getItemLayoutResource();

        int getItemViewType();

        boolean isEnabled();

        void onItemClick(QuickSearchBarActivity activity, int position);

    }

    static abstract class BaseClickableItem implements SuggestionItem {
        @Override
        public final boolean isEnabled() {
            return true;
        }

    }

    static class SavedSearchItem extends BaseClickableItem {

        static final int ITEM_VIEW_TYPE = 1;
        private final String mQuery;

        public SavedSearchItem(String query) {
            mQuery = query;
        }

        @Override
        public void bindView(SuggestionsAdapter adapter, View view, int position) {
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            text1.setText(mQuery);
            icon.setImageResource(R.drawable.ic_action_save);
            final View editQuery = view.findViewById(R.id.edit_query);
            editQuery.setTag(position);
            editQuery.setOnClickListener(adapter);
        }

        @Override
        public final int getItemLayoutResource() {
            return R.layout.list_item_suggestion_search;
        }

        @Override
        public int getItemViewType() {
            return ITEM_VIEW_TYPE;
        }

        public String getQuery() {
            return mQuery;
        }

        @Override
        public void onItemClick(QuickSearchBarActivity activity, int position) {
            Utils.openSearch(activity, activity.getAccountId(), mQuery);
            activity.finish();
        }
    }

    static class SearchHistoryItem extends BaseClickableItem {

        static final int ITEM_VIEW_TYPE = 0;
        private final long mCursorId;
        private final String mQuery;

        public SearchHistoryItem(long cursorId, String query) {
            mCursorId = cursorId;
            mQuery = query;
        }

        public long getCursorId() {
            return mCursorId;
        }

        public String getQuery() {
            return mQuery;
        }

        @Override
        public void bindView(SuggestionsAdapter adapter, View view, int position) {
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            text1.setText(mQuery);
            icon.setImageResource(R.drawable.ic_action_history);
            final View editQuery = view.findViewById(R.id.edit_query);
            editQuery.setTag(position);
            editQuery.setOnClickListener(adapter);
        }

        @Override
        public final int getItemLayoutResource() {
            return R.layout.list_item_suggestion_search;
        }

        @Override
        public int getItemViewType() {
            return ITEM_VIEW_TYPE;
        }

        @Override
        public void onItemClick(QuickSearchBarActivity activity, int position) {
            Utils.openSearch(activity, activity.getAccountId(), mQuery);
            activity.finish();
        }


    }

    private void setSearchQuery(String query) {
        mSearchQuery.setText(query);
        if (query == null) return;
        mSearchQuery.setSelection(query.length());
    }

    public static class SuggestionsAdapter extends BaseAdapter implements OnClickListener {

        private final QuickSearchBarActivity mActivity;
        private final LayoutInflater mInflater;
        private final MediaLoaderWrapper mImageLoader;
        private final UserColorNameManager mUserColorNameManager;
        private List<SuggestionItem> mData;

        SuggestionsAdapter(QuickSearchBarActivity activity) {
            mActivity = activity;
            mImageLoader = activity.mImageLoader;
            mUserColorNameManager = activity.mUserColorNameManager;
            mInflater = LayoutInflater.from(activity);
        }

        public boolean canDismiss(int position) {
            return getItemViewType(position) == SearchHistoryItem.ITEM_VIEW_TYPE;
        }

        public Context getContext() {
            return mActivity;
        }

        @Override
        public int getCount() {
            if (mData == null) return 0;
            return mData.size();
        }

        @Override
        public SuggestionItem getItem(int position) {
            if (mData == null) return null;
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            final SuggestionItem item = getItem(position);
            if (convertView == null) {
                view = mInflater.inflate(item.getItemLayoutResource(), parent, false);
            } else {
                view = convertView;
            }
            item.bindView(this, view, position);
            return view;
        }

        public MediaLoaderWrapper getImageLoader() {
            return mImageLoader;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public int getItemViewType(int position) {
            if (mData == null) return IGNORE_ITEM_VIEW_TYPE;
            return mData.get(position).getItemViewType();
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        public void removeItemAt(int position) {
            if (mData == null) return;
            mData.remove(position);
            notifyDataSetChanged();
        }

        public void setData(List<SuggestionItem> data) {
            mData = data;
            notifyDataSetChanged();
        }

        public UserColorNameManager getUserColorNameManager() {
            return mUserColorNameManager;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit_query: {
                    final SuggestionItem item = getItem((Integer) v.getTag());
                    if (item instanceof SearchHistoryItem) {
                        mActivity.setSearchQuery(((SearchHistoryItem) item).getQuery());
                    } else if (item instanceof SavedSearchItem) {
                        mActivity.setSearchQuery(((SavedSearchItem) item).getQuery());
                    }
                    break;
                }
            }
        }
    }

    public static class SuggestionsLoader extends AsyncTaskLoader<List<SuggestionItem>> {

        private static final Pattern PATTERN_SCREEN_NAME = Pattern.compile("(?i)[@\uFF20]?([a-z0-9_]{1,20})");

        private final UserColorNameManager mUserColorNameManager;
        private final long mAccountId;
        private final String mQuery;

        public SuggestionsLoader(QuickSearchBarActivity context, long accountId, String query) {
            super(context);
            mUserColorNameManager = context.mUserColorNameManager;
            mAccountId = accountId;
            mQuery = query;
        }

        @Override
        public List<SuggestionItem> loadInBackground() {
            final boolean emptyQuery = TextUtils.isEmpty(mQuery);
            final Context context = getContext();
            final ContentResolver resolver = context.getContentResolver();
            final List<SuggestionItem> result = new ArrayList<>();
            final String[] historyProjection = {SearchHistory._ID, SearchHistory.QUERY};
            final Cursor historyCursor = resolver.query(SearchHistory.CONTENT_URI,
                    historyProjection, null, null, SearchHistory.DEFAULT_SORT_ORDER);
            for (int i = 0, j = Math.min(getHistorySize(mQuery), historyCursor.getCount()); i < j; i++) {
                historyCursor.moveToPosition(i);
                result.add(new SearchHistoryItem(historyCursor.getLong(0), historyCursor.getString(1)));
            }
            historyCursor.close();
            if (!emptyQuery) {
                final String queryEscaped = mQuery.replace("_", "^_");
                final long[] nicknameIds = Utils.getMatchedNicknameIds(mQuery, mUserColorNameManager);
                final Expression selection = Expression.or(
                        Expression.likeRaw(new Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                        Expression.likeRaw(new Column(CachedUsers.NAME), "?||'%'", "^"),
                        Expression.in(new Column(CachedUsers.USER_ID), new RawItemArray(nicknameIds)));
                final String[] selectionArgs = new String[]{queryEscaped, queryEscaped};
                final String[] order = {CachedUsers.LAST_SEEN, "score", CachedUsers.SCREEN_NAME, CachedUsers.NAME};
                final boolean[] ascending = {false, false, true, true};
                final OrderBy orderBy = new OrderBy(order, ascending);
                final Uri uri = Uri.withAppendedPath(CachedUsers.CONTENT_URI_WITH_SCORE, String.valueOf(mAccountId));
                final Cursor usersCursor = context.getContentResolver().query(uri, CachedUsers.COLUMNS,
                        selection.getSQL(), selectionArgs, orderBy.getSQL());
                final CachedIndices usersIndices = new CachedIndices(usersCursor);
                final int screenNamePos = result.size();
                boolean hasName = false;
                for (int i = 0, j = Math.min(5, usersCursor.getCount()); i < j; i++) {
                    usersCursor.moveToPosition(i);
                    final UserSuggestionItem userSuggestionItem = new UserSuggestionItem(usersCursor, usersIndices, mAccountId);
                    final ParcelableUser user = userSuggestionItem.getUser();
                    result.add(userSuggestionItem);
                    if (user.screen_name.equalsIgnoreCase(mQuery)) {
                        hasName = true;
                    }
                }
                if (!hasName) {
                    final Matcher m = PATTERN_SCREEN_NAME.matcher(mQuery);
                    if (m.matches()) {
                        result.add(screenNamePos, new UserScreenNameItem(m.group(1), mAccountId));
                    }
                }
                usersCursor.close();
            } else {
                final String[] savedSearchesProjection = {SavedSearches.QUERY};
                final Expression savedSearchesWhere = Expression.equals(SavedSearches.ACCOUNT_ID, mAccountId);
                final Cursor savedSearchesCursor = resolver.query(SavedSearches.CONTENT_URI,
                        savedSearchesProjection, savedSearchesWhere.getSQL(), null,
                        SavedSearches.DEFAULT_SORT_ORDER);
                savedSearchesCursor.moveToFirst();
                while (!savedSearchesCursor.isAfterLast()) {
                    result.add(new SavedSearchItem(savedSearchesCursor.getString(0)));
                    savedSearchesCursor.moveToNext();
                }
                savedSearchesCursor.close();
            }
            return result;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    static class UserScreenNameItem extends BaseClickableItem {

        static final int ITEM_VIEW_TYPE = 3;
        private final String mScreenName;
        private final long mAccountId;

        public UserScreenNameItem(String screenName, long accountId) {
            mScreenName = screenName;
            mAccountId = accountId;
        }

        @Override
        public int getItemViewType() {
            return ITEM_VIEW_TYPE;
        }

        @Override
        public void onItemClick(QuickSearchBarActivity activity, int position) {
            Utils.openUserProfile(activity, mAccountId, -1, mScreenName, null);
            activity.finish();
        }

        @Override
        public final int getItemLayoutResource() {
            return R.layout.list_item_suggestion_user;
        }

        @Override
        public void bindView(SuggestionsAdapter adapter, View view, int position) {
            final MediaLoaderWrapper loader = adapter.getImageLoader();
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text1.setText('@' + mScreenName);
            text2.setVisibility(View.GONE);
            icon.setColorFilter(text1.getCurrentTextColor(), Mode.SRC_ATOP);
            loader.cancelDisplayTask(icon);
            icon.setImageResource(R.drawable.ic_action_user);
        }
    }

    static class UserSuggestionItem extends BaseClickableItem {

        static final int ITEM_VIEW_TYPE = 2;
        private final ParcelableUser mUser;

        public UserSuggestionItem(Cursor c, CachedIndices i, long accountId) {
            mUser = new ParcelableUser(c, i, accountId);
        }

        public ParcelableUser getUser() {
            return mUser;
        }

        @Override
        public int getItemViewType() {
            return ITEM_VIEW_TYPE;
        }


        @Override
        public void onItemClick(QuickSearchBarActivity activity, int position) {
            Utils.openUserProfile(activity, mUser, null);
            activity.finish();
        }

        @Override
        public final int getItemLayoutResource() {
            return R.layout.list_item_suggestion_user;
        }

        @Override
        public void bindView(SuggestionsAdapter adapter, View view, int position) {
            final ParcelableUser user = mUser;
            final MediaLoaderWrapper loader = adapter.getImageLoader();
            final UserColorNameManager manager = adapter.getUserColorNameManager();
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);

            text1.setText(manager.getUserNickname(user.id, user.name, false));
            text2.setVisibility(View.VISIBLE);
            text2.setText("@" + user.screen_name);
            icon.clearColorFilter();
            loader.displayProfileImage(icon, user.profile_image_url);
        }
    }

}
