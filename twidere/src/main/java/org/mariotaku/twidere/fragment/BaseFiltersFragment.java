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

package org.mariotaku.twidere.fragment;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.UserListSelectorActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter;
import org.mariotaku.twidere.adapter.SourceAutoCompleteAdapter;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

import static org.mariotaku.twidere.util.Utils.getDefaultAccountKey;

public abstract class BaseFiltersFragment extends AbsContentListViewFragment<SimpleCursorAdapter>
        implements LoaderManager.LoaderCallbacks<Cursor>, MultiChoiceModeListener {

    private static final String EXTRA_AUTO_COMPLETE_TYPE = "auto_complete_type";
    private static final int AUTO_COMPLETE_TYPE_SOURCES = 2;
    private ContentResolver mResolver;
    private ActionMode mActionMode;

    public abstract Uri getContentUri();

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mResolver = getContentResolver();
        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        getLoaderManager().initLoader(0, null, this);
        setRefreshEnabled(false);
        showProgress();
    }

//    @Override
//    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//        final View view = super.onCreateView(inflater, container, savedInstanceState);
//        assert view != null;
//        final ListView listView = (ListView) view.findViewById(R.id.list_view);
//        final Resources res = getResources();
//        final float density = res.getDisplayMetrics().density;
//        final int padding = (int) density * 16;
//        listView.setPadding(padding, 0, padding, 0);
//        return view;
//    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void setControlVisible(boolean visible) {
        super.setControlVisible(visible || !isQuickReturnEnabled());
    }

    private boolean isQuickReturnEnabled() {
        return mActionMode == null;
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mActionMode = mode;
        setControlVisible(true);
        mode.getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        updateTitle(mode);
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final ListView listView = getListView();
        switch (item.getItemId()) {
            case R.id.delete: {
                final Expression where = Expression.in(new Column(Filters._ID),
                        new RawItemArray(listView.getCheckedItemIds()));
                mResolver.delete(getContentUri(), where.getSQL(), null);
                break;
            }
            case R.id.inverse_selection: {
                final SparseBooleanArray positions = listView.getCheckedItemPositions();
                for (int i = 0, j = listView.getCount(); i < j; i++) {
                    listView.setItemChecked(i, !positions.get(i));
                }
                return true;
            }
            default: {
                return false;
            }
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        mActionMode = null;
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        updateTitle(mode);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (ViewCompat.isLaidOut(view)) {
            int childCount = view.getChildCount();
            if (childCount > 0) {
                final View firstChild = view.getChildAt(0);
                final FragmentActivity activity = getActivity();
                int controlBarHeight = 0;
                if (activity instanceof IControlBarActivity) {
                    controlBarHeight = ((IControlBarActivity) activity).getControlBarHeight();
                }
                final boolean visible = firstChild.getTop() > controlBarHeight;
                setControlVisible(visible);
            } else {
                setControlVisible(true);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getActivity(), getContentUri(), getContentColumns(), null, null, null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final SimpleCursorAdapter adapter = getAdapter();
        adapter.swapCursor(data);
        if (data != null && data.getCount() > 0) {
            showContent();
        } else {
            showEmpty(R.drawable.ic_info_volume_off, getString(R.string.no_rule));
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        final SimpleCursorAdapter adapter = getAdapter();
        adapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filters, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add: {
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_URI, getContentUri());
                final AddItemFragment dialog = new AddItemFragment();
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "add_rule");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean isRefreshing() {
        return false;
    }

    @NonNull
    @Override
    protected SimpleCursorAdapter onCreateAdapter(Context context) {
        return new FilterListAdapter(context);
    }

    protected abstract String[] getContentColumns();

    private void updateTitle(final ActionMode mode) {
        final ListView listView = getListView();
        if (listView == null || mode == null || getActivity() == null) return;
        final int count = listView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
    }

    public static final class AddItemFragment extends BaseDialogFragment implements OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    final String text = getText();
                    if (TextUtils.isEmpty(text)) return;
                    final ContentValues values = new ContentValues();
                    values.put(Filters.VALUE, text);
                    final Bundle args = getArguments();
                    final Uri uri = args.getParcelable(EXTRA_URI);
                    assert uri != null;
                    getContentResolver().insert(uri, values);
                    break;
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final FragmentActivity activity = getActivity();
            final Context context = activity;
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(R.layout.dialog_auto_complete_textview);

            builder.setTitle(R.string.add_rule);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    final AutoCompleteTextView editText = (AutoCompleteTextView) alertDialog.findViewById(R.id.edit_text);
                    assert editText != null;
                    final Bundle args = getArguments();
                    final int autoCompleteType;
                    autoCompleteType = args.getInt(EXTRA_AUTO_COMPLETE_TYPE, 0);
                    if (autoCompleteType != 0) {
                        SimpleCursorAdapter mUserAutoCompleteAdapter;
                        if (autoCompleteType == AUTO_COMPLETE_TYPE_SOURCES) {
                            mUserAutoCompleteAdapter = new SourceAutoCompleteAdapter(activity);
                        } else {
                            final ComposeAutoCompleteAdapter adapter = new ComposeAutoCompleteAdapter(activity);
                            adapter.setAccountKey(Utils.getDefaultAccountKey(activity));
                            mUserAutoCompleteAdapter = adapter;
                        }
                        editText.setAdapter(mUserAutoCompleteAdapter);
                        editText.setThreshold(1);
                    }
                }
            });
            return dialog;
        }

        protected String getText() {
            AlertDialog alertDialog = (AlertDialog) getDialog();
            final AutoCompleteTextView editText = (AutoCompleteTextView) alertDialog.findViewById(R.id.edit_text);
            assert editText != null;
            return ParseUtils.parseString(editText.getText());
        }

    }

    private static final class FilterListAdapter extends SimpleCursorAdapter {

        private static final String[] from = new String[]{Filters.VALUE};

        private static final int[] to = new int[]{android.R.id.text1};

        public FilterListAdapter(final Context context) {
            super(context, android.R.layout.simple_list_item_activated_1, null, from, to, 0);
        }

    }

    public static final class FilteredKeywordsFragment extends BaseFiltersFragment {

        @Override
        public Uri getContentUri() {
            return Filters.Keywords.CONTENT_URI;
        }

        @Override
        public String[] getContentColumns() {
            return Filters.Keywords.COLUMNS;
        }


    }

    public static final class FilteredLinksFragment extends BaseFiltersFragment {

        @Override
        public String[] getContentColumns() {
            return Filters.Links.COLUMNS;
        }

        @Override
        public Uri getContentUri() {
            return Filters.Links.CONTENT_URI;
        }

    }

    public static final class FilteredSourcesFragment extends BaseFiltersFragment {

        @Override
        public String[] getContentColumns() {
            return Filters.Sources.COLUMNS;
        }

        @Override
        public Uri getContentUri() {
            return Filters.Sources.CONTENT_URI;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.add: {
                    final Bundle args = new Bundle();
                    args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_SOURCES);
                    args.putParcelable(EXTRA_URI, getContentUri());
                    final AddItemFragment dialog = new AddItemFragment();
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "add_rule");
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

    }

    public static final class FilteredUsersFragment extends BaseFiltersFragment {


        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            switch (requestCode) {
                case REQUEST_SELECT_USER: {
                    if (resultCode != FragmentActivity.RESULT_OK || !data.hasExtra(EXTRA_USER))
                        return;
                    final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
                    final ContentValues values = ContentValuesCreator.createFilteredUser(user);
                    final ContentResolver resolver = getContentResolver();
                    final String where = Expression.equalsArgs(Filters.Users.USER_KEY).getSQL();
                    final String[] whereArgs = {user.key.toString()};
                    resolver.delete(Filters.Users.CONTENT_URI, where, whereArgs);
                    resolver.insert(Filters.Users.CONTENT_URI, values);
                    break;
                }
            }
        }

        @Override
        public String[] getContentColumns() {
            return Filters.Users.COLUMNS;
        }

        @Override
        public Uri getContentUri() {
            return Filters.Users.CONTENT_URI;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.add: {
                    final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                    intent.setClass(getContext(), UserListSelectorActivity.class);
                    intent.putExtra(EXTRA_ACCOUNT_KEY, getDefaultAccountKey(getActivity()));
                    startActivityForResult(intent, REQUEST_SELECT_USER);
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        @NonNull
        @Override
        protected SimpleCursorAdapter onCreateAdapter(Context context) {
            return new FilterUsersListAdapter(context);
        }

        public static final class FilterUsersListAdapter extends SimpleCursorAdapter {

            private final boolean mNameFirst;
            @Inject
            UserColorNameManager mUserColorNameManager;
            @Inject
            SharedPreferencesWrapper mPreferences;
            private int mUserIdIdx, mNameIdx, mScreenNameIdx;

            FilterUsersListAdapter(final Context context) {
                super(context, android.R.layout.simple_list_item_activated_2, null, new String[0], new int[0], 0);
                GeneralComponentHelper.build(context).inject(this);
                mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
            }

            @Override
            public void bindView(@NonNull final View view, final Context context, @NonNull final Cursor cursor) {
                super.bindView(view, context, cursor);
                final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                final UserKey userId = UserKey.valueOf(cursor.getString(mUserIdIdx));
                assert userId != null;
                final String name = cursor.getString(mNameIdx);
                final String screenName = cursor.getString(mScreenNameIdx);
                final String displayName = mUserColorNameManager.getDisplayName(userId, name, screenName,
                        mNameFirst);
                text1.setText(displayName);
                text2.setText(userId.getHost());
            }

            @Override
            public Cursor swapCursor(final Cursor c) {
                final Cursor old = super.swapCursor(c);
                if (c != null) {
                    mUserIdIdx = c.getColumnIndex(Filters.Users.USER_KEY);
                    mNameIdx = c.getColumnIndex(Filters.Users.NAME);
                    mScreenNameIdx = c.getColumnIndex(Filters.Users.SCREEN_NAME);
                }
                return old;
            }

        }

    }
}
