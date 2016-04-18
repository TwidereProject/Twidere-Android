package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.SimpleArrayMap;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.SignInActivity;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Outbox;
import org.mariotaku.twidere.provider.TwidereDataStore.Mentions;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.TwidereCollectionUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.collection.CompactHashSet;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by mariotaku on 14/10/26.
 */
public class AccountsManagerFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>,
        DropListener, OnSharedPreferenceChangeListener, AdapterView.OnItemClickListener, AccountsAdapter.OnAccountToggleListener {

    private static final String FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion";

    private AccountsAdapter mAdapter;
    private ParcelableAccount mSelectedAccount;
    private SimpleArrayMap<UserKey, Boolean> mActivatedState = new SimpleArrayMap<>();

    private DragSortListView mListView;
    private View mEmptyView;
    private View mListContainer, mProgressContainer;
    private TextView mEmptyText;
    private ImageView mEmptyIcon;


    private void setListShown(boolean shown) {
        mListContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_account: {
                final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
                intent.setClass(getActivity(), SignInActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                if (resultCode != Activity.RESULT_OK || data == null || mSelectedAccount == null)
                    return;
                final ContentValues values = new ContentValues();
                values.put(Accounts.COLOR, data.getIntExtra(EXTRA_COLOR, Color.WHITE));
                final Expression where = Expression.equalsArgs(Accounts.ACCOUNT_KEY);
                final String[] whereArgs = {mSelectedAccount.account_key.toString()};
                final ContentResolver cr = getContentResolver();
                cr.update(Accounts.CONTENT_URI, values, where.getSQL(), whereArgs);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_accounts_manager, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final ContextMenuInfo menuInfo = item.getMenuInfo();
        if (!(menuInfo instanceof AdapterContextMenuInfo)) return false;
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ParcelableAccount account = mAdapter.getAccount(info.position);
        mSelectedAccount = account;
        if (account == null) return false;
        switch (item.getItemId()) {
            case R.id.set_color: {
                final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
                intent.putExtra(EXTRA_COLOR, account.color);
                intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                startActivityForResult(intent, REQUEST_SET_COLOR);
                break;
            }
            case R.id.delete: {
                final AccountDeletionDialogFragment f = new AccountDeletionDialogFragment();
                final Bundle args = new Bundle();
                args.putLong(EXTRA_ID, account.id);
                f.setArguments(args);
                f.show(getChildFragmentManager(), FRAGMENT_TAG_ACCOUNT_DELETION);
                break;
            }
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Context context = getContext();
        if (context == null) return;
        final ParcelableAccount account = mAdapter.getAccount(position);
        if (account.account_user != null) {
            IntentUtils.openUserProfile(context, account.account_user, null,
                    mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                    UserFragment.Referral.SELF_PROFILE);
        } else {
            IntentUtils.openUserProfile(context, account.account_key, account.account_key,
                    account.screen_name, null, mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                    UserFragment.Referral.SELF_PROFILE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveActivatedState();
    }

    private void saveActivatedState() {
        final Set<UserKey> trueIds = new CompactHashSet<>(), falseIds = new CompactHashSet<>();
        for (int i = 0, j = mActivatedState.size(); i < j; i++) {
            if (mActivatedState.valueAt(i)) {
                trueIds.add(mActivatedState.keyAt(i));
            } else {
                falseIds.add(mActivatedState.keyAt(i));
            }
        }
        final ContentResolver cr = getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(Accounts.IS_ACTIVATED, true);
        Expression where = Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY), new ArgsArray(trueIds.size()));
        String[] whereArgs = TwidereCollectionUtils.toStringArray(trueIds);
        cr.update(Accounts.CONTENT_URI, values, where.getSQL(), whereArgs);
        values.put(Accounts.IS_ACTIVATED, false);
        where = Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY), new ArgsArray(falseIds.size()));
        whereArgs = TwidereCollectionUtils.toStringArray(falseIds);
        cr.update(Accounts.CONTENT_URI, values, where.getSQL(), whereArgs);
    }

    @Override
    public void onAccountToggle(UserKey accountId, boolean state) {
        mActivatedState.put(accountId, state);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (DragSortListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mEmptyIcon = (ImageView) view.findViewById(R.id.empty_icon);
        mEmptyText = (TextView) view.findViewById(R.id.empty_text);
        mListContainer = view.findViewById(R.id.list_container);
        mProgressContainer = view.findViewById(R.id.progress_container);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (!(menuInfo instanceof AdapterContextMenuInfo)) return;
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final ParcelableAccount account = mAdapter.getAccount(info.position);
        menu.setHeaderTitle(account.name);
        final MenuInflater inflater = new MenuInflater(v.getContext());
        inflater.inflate(R.menu.action_manager_account, menu);
    }

    @Override
    public void onDestroyView() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final FragmentActivity activity = getActivity();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mAdapter = new AccountsAdapter(activity);
        Utils.configBaseAdapter(activity, mAdapter);
        mAdapter.setSortEnabled(true);
        mAdapter.setSwitchEnabled(true);
        mAdapter.setOnAccountToggleListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setDragEnabled(true);
        mListView.setDropListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setEmptyView(mEmptyView);
        mEmptyText.setText(R.string.no_account);
        mEmptyIcon.setImageResource(R.drawable.ic_info_error_generic);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = Accounts.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setListShown(true);
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void drop(int from, int to) {
        mAdapter.drop(from, to);
        if (mListView.getChoiceMode() != AbsListView.CHOICE_MODE_NONE) {
            mListView.moveCheckState(from, to);
        }
        saveAccountPositions();
    }

    private void saveAccountPositions() {
        final ContentResolver cr = getContentResolver();
        final ArrayList<Integer> positions = mAdapter.getCursorPositions();
        final Cursor c = mAdapter.getCursor();
        if (positions != null && c != null && !c.isClosed()) {
            final int idIdx = c.getColumnIndex(Accounts._ID);
            for (int i = 0, j = positions.size(); i < j; i++) {
                c.moveToPosition(positions.get(i));
                final long id = c.getLong(idIdx);
                final ContentValues values = new ContentValues();
                values.put(Accounts.SORT_POSITION, i);
                final Expression where = Expression.equals(Accounts._ID, id);
                cr.update(Accounts.CONTENT_URI, values, where.getSQL(), null);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_DEFAULT_ACCOUNT_KEY.equals(key)) {
            updateDefaultAccount();
        }
    }

    private void updateDefaultAccount() {
        mAdapter.notifyDataSetChanged();
    }

    public static final class AccountDeletionDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Bundle args = getArguments();
            final long id = args.getLong(EXTRA_ID);
            final ContentResolver resolver = getContentResolver();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final String where = Expression.equalsArgs(Accounts._ID)
                            .getSQL();
                    final String[] whereArgs = {String.valueOf(id)};
                    resolver.delete(Accounts.CONTENT_URI, where, whereArgs);
                    // Also delete tweets related to the account we previously
                    // deleted.
                    resolver.delete(Statuses.CONTENT_URI, where, whereArgs);
                    resolver.delete(Mentions.CONTENT_URI, where, whereArgs);
                    resolver.delete(Inbox.CONTENT_URI, where, whereArgs);
                    resolver.delete(Outbox.CONTENT_URI, where, whereArgs);
                    break;
                }
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setTitle(R.string.account_delete_confirm_title);
            builder.setMessage(R.string.account_delete_confirm_message);
            return builder.create();
        }

    }
}
