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

package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DraftsAdapter;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

public class DraftsFragment extends BaseSupportFragment implements Constants, LoaderCallbacks<Cursor>,
        OnItemClickListener, MultiChoiceModeListener {

    private ContentResolver mResolver;
    private SharedPreferences mPreferences;

    private DraftsAdapter mAdapter;

    private ListView mListView;
    private View mEmptyView;
    private TextView mEmptyText;
    private ImageView mEmptyIcon;
    private View mListContainer, mProgressContainer;

    private float mTextSize;

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_multi_select_drafts, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        updateTitle(mode);
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                final DeleteDraftsConfirmDialogFragment f = new DeleteDraftsConfirmDialogFragment();
                final Bundle args = new Bundle();
                args.putLongArray(EXTRA_IDS, mListView.getCheckedItemIds());
                f.setArguments(args);
                f.show(getChildFragmentManager(), "delete_drafts_confirm");
                break;
            }
            case R.id.send: {
                final Cursor c = mAdapter.getCursor();
                if (c == null || c.isClosed()) return false;
                final SparseBooleanArray checked = mListView.getCheckedItemPositions();
                final List<DraftItem> list = new ArrayList<>();
                final DraftItem.CursorIndices indices = new DraftItem.CursorIndices(c);
                for (int i = 0, j = checked.size(); i < j; i++) {
                    if (checked.valueAt(i) && c.moveToPosition(checked.keyAt(i))) {
                        list.add(new DraftItem(c, indices));
                    }
                }
                if (sendDrafts(list)) {
                    final Expression where = Expression.in(new Column(Drafts._ID),
                            new RawItemArray(mListView.getCheckedItemIds()));
                    mResolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
                }
                break;
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

    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = Drafts.CONTENT_URI_UNSENT;
        final String[] cols = Drafts.COLUMNS;
        final String orderBy = Drafts.TIMESTAMP + " DESC";
        return new CursorLoader(getActivity(), uri, cols, null, null, orderBy);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.swapCursor(cursor);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        updateTitle(mode);
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final Cursor c = mAdapter.getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return;
        final DraftItem item = new DraftItem(c, new DraftItem.CursorIndices(c));
        if (item.action_type == Drafts.ACTION_UPDATE_STATUS || item.action_type <= 0) {
            editDraft(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drafts, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mResolver = getContentResolver();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
        mAdapter = new DraftsAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        mEmptyIcon.setImageResource(R.drawable.ic_info_draft);
        mEmptyText.setText(R.string.drafts_hint_messages);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public void onStart() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter != null) {
            twitter.clearNotificationAsync(NOTIFICATION_ID_DRAFTS);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        final float text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
        mAdapter.setTextSize(text_size);
        if (mTextSize != text_size) {
            mTextSize = text_size;
            mListView.invalidateViews();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mEmptyText = (TextView) view.findViewById(R.id.empty_text);
        mEmptyIcon = (ImageView) view.findViewById(R.id.empty_icon);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mListContainer = view.findViewById(R.id.list_container);
    }

    public void setListShown(boolean listShown) {
        mListContainer.setVisibility(listShown ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(listShown ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(listShown && mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void editDraft(final DraftItem draft) {
        final Intent intent = new Intent(INTENT_ACTION_EDIT_DRAFT);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DRAFT, draft);
        intent.putExtras(bundle);
        mResolver.delete(Drafts.CONTENT_URI, Expression.equals(Drafts._ID, draft._id).getSQL(), null);
        startActivityForResult(intent, REQUEST_COMPOSE);
    }

    private boolean sendDrafts(final List<DraftItem> list) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter == null) return false;
        for (final DraftItem item : list) {
            if (item.action_type == Drafts.ACTION_UPDATE_STATUS || item.action_type <= 0) {
                twitter.updateStatusesAsync(new ParcelableStatusUpdate(getActivity(), item));
            } else if (item.action_type == Drafts.ACTION_SEND_DIRECT_MESSAGE) {
                final long recipientId = item.action_extras.optLong(EXTRA_RECIPIENT_ID);
                if (item.account_ids == null || item.account_ids.length <= 0 || recipientId <= 0) {
                    continue;
                }
                final long accountId = item.account_ids[0];
                final String imageUri = item.media != null && item.media.length > 0 ? item.media[0].uri : null;
                twitter.sendDirectMessageAsync(accountId, recipientId, item.text, imageUri);
            }
        }
        return true;
    }

    private void updateTitle(final ActionMode mode) {
        if (mListView == null || mode == null) return;
        final int count = mListView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
    }

    public static class DeleteDraftsConfirmDialogFragment extends BaseSupportDialogFragment implements OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Bundle args = getArguments();
                    if (args == null) return;
                    AsyncTaskUtils.executeTask(new DeleteDraftsTask(getActivity(), args.getLongArray(EXTRA_IDS)));
                    break;
                }
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context context = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_drafts_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

    }

    private static class DeleteDraftsTask extends AsyncTask<Object, Object, Integer> {

        private static final String FRAGMENT_TAG_DELETING_DRAFTS = "deleting_drafts";
        private final FragmentActivity mActivity;
        private final long[] mIds;
        private final NotificationManager mNotificationManager;
        private Handler mHandler;

        private DeleteDraftsTask(final FragmentActivity activity, final long[] ids) {
            mActivity = activity;
            mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            mIds = ids;
            mHandler = new Handler(activity.getMainLooper());
        }

        @Override
        protected Integer doInBackground(final Object... params) {
            final ContentResolver resolver = mActivity.getContentResolver();
            final Expression where = Expression.in(new Column(Drafts._ID), new RawItemArray(mIds));
            final String[] projection = {Drafts.MEDIA};
            final Cursor c = resolver.query(Drafts.CONTENT_URI, projection, where.getSQL(), null, null);
            final int idxMedia = c.getColumnIndex(Drafts.MEDIA);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final ParcelableMediaUpdate[] mediaArray = ParcelableMediaUpdate.fromJSONString(c.getString(idxMedia));
                if (mediaArray != null) {
                    for (final ParcelableMediaUpdate media : mediaArray) {
                        final Uri uri = Uri.parse(media.uri);
                        if ("file".equals(uri.getScheme())) {
                            final File file = new File(uri.getPath());
                            if (!file.delete()) {
                                Log.w(LOGTAG, String.format("Unable to delete %s", file));
                            }
                        }
                    }
                }
                c.moveToNext();
            }
            c.close();
            return resolver.delete(Drafts.CONTENT_URI, where.getSQL(), null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final SupportProgressDialogFragment f = SupportProgressDialogFragment.show(mActivity,
                    FRAGMENT_TAG_DELETING_DRAFTS);
            f.setCancelable(false);
        }

        @Override
        protected void onPostExecute(final Integer result) {
            super.onPostExecute(result);
            final Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DELETING_DRAFTS);
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
            for (long id : mIds) {
                final String tag = Uri.withAppendedPath(Drafts.CONTENT_URI, String.valueOf(id)).toString();
                mNotificationManager.cancel(tag, NOTIFICATION_ID_DRAFTS);
            }
        }
    }
}
