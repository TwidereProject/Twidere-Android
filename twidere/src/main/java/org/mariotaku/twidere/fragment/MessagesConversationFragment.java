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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Property;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.activity.ThemedImagePickerActivity;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.MessageConversationAdapter;
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.loader.UserSearchLoader;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserCursorIndices;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.TaskStateChangedEvent;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.EditTextEnterHandler;
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.TakeAllKeyboardShortcut;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.view.ComposeEditText;
import org.mariotaku.twidere.view.ExtendedRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import me.uucky.colorpicker.internal.EffectViewHelper;

import static org.mariotaku.twidere.util.Utils.buildDirectMessageConversationUri;

public class MessagesConversationFragment extends BaseSupportFragment implements
        LoaderCallbacks<Cursor>, OnClickListener, OnItemSelectedListener, MenuButtonClickListener,
        PopupMenu.OnMenuItemClickListener, KeyboardShortcutCallback, TakeAllKeyboardShortcut {

    // Constants
    private static final int LOADER_ID_SEARCH_USERS = 1;
    private static final String EXTRA_FROM_CACHE = "from_cache";


    // Callbacks and listeners
    private LoaderCallbacks<List<ParcelableUser>> mSearchLoadersCallback = new LoaderCallbacks<List<ParcelableUser>>() {
        @Override
        public Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
            mUsersSearchList.setVisibility(View.GONE);
            mUsersSearchEmpty.setVisibility(View.GONE);
            mUsersSearchProgress.setVisibility(View.VISIBLE);
            final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
            final String query = args.getString(EXTRA_QUERY);
            final boolean fromCache = args.getBoolean(EXTRA_FROM_CACHE);
            final boolean fromUser = args.getBoolean(EXTRA_FROM_USER, false);
            return new CacheUserSearchLoader(MessagesConversationFragment.this, accountKey, query,
                    fromCache, fromUser);
        }

        @Override
        public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
            mUsersSearchList.setVisibility(View.VISIBLE);
            mUsersSearchProgress.setVisibility(View.GONE);
            mUsersSearchEmpty.setVisibility(data == null || data.isEmpty() ? View.GONE : View.VISIBLE);
            mUsersSearchAdapter.setData(data, true);
            updateEmptyText();
        }

        @Override
        public void onLoaderReset(Loader<List<ParcelableUser>> loader) {

        }
    };
    private PanelShowHideListener mScrollListener;

    private SharedPreferences mMessageDrafts;
    private EffectViewHelper mEffectHelper;

    // Views
    private RecyclerView mMessagesListView;
    private ListView mUsersSearchList;
    private ComposeEditText mEditText;
    private View mSendButton;
    private ImageView mAddImageButton;
    private View mConversationContainer, mRecipientSelectorContainer;
    private Spinner mAccountSpinner;
    private EditText mEditUserQuery;
    private View mUsersSearchProgress;
    private View mQueryButton;
    private View mUsersSearchEmpty;
    private TextView mUsersSearchEmptyText;
    private PopupMenu mPopupMenu;
    private View mInputPanelShadowCompat;

    // Adapters
    private MessageConversationAdapter mAdapter;
    private SimpleParcelableUsersAdapter mUsersSearchAdapter;

    // Data fields
    private boolean mSearchUsersLoaderInitialized;
    private boolean mNavigateBackPressed;
    private ParcelableDirectMessage mSelectedDirectMessage;
    private boolean mLoaderInitialized;
    private String mImageUri;
    private ParcelableCredentials mAccount;
    private ParcelableUser mRecipient;
    private boolean mTextChanged, mQueryTextChanged;
    private View mInputPanel;

    private final Runnable mBackTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mNavigateBackPressed = false;
        }
    };

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE: {
                if (resultCode != Activity.RESULT_OK || data.getDataString() == null) {
                    break;
                }
                mImageUri = data.getDataString();
                updateAddImageButton();
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final BaseActivity activity = (BaseActivity) getActivity();
        mMessageDrafts = activity.getSharedPreferences(MESSAGE_DRAFTS_PREFERENCES_NAME, Context.MODE_PRIVATE);

        final View view = getView();
        assert view != null;
        final Context viewContext = view.getContext();
        setHasOptionsMenu(true);
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) throw new NullPointerException();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.layout_actionbar_message_user_picker);
        final View actionBarView = actionBar.getCustomView();
        mAccountSpinner = (Spinner) actionBarView.findViewById(R.id.account_spinner);
        mEditUserQuery = (EditText) actionBarView.findViewById(R.id.user_query);
        mQueryButton = actionBarView.findViewById(R.id.query_button);
        final List<ParcelableCredentials> accounts = DataStoreUtils.getCredentialsList(activity, false);
        final AccountsSpinnerAdapter accountsSpinnerAdapter = new AccountsSpinnerAdapter(
                actionBar.getThemedContext(), R.layout.spinner_item_account_icon);
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_user);
        accountsSpinnerAdapter.addAll(accounts);
        mAccountSpinner.setAdapter(accountsSpinnerAdapter);
        mAccountSpinner.setOnItemSelectedListener(this);
        mQueryButton.setOnClickListener(this);
        mAdapter = new MessageConversationAdapter(activity);
        final LinearLayoutManager layoutManager = new FixedLinearLayoutManager(viewContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        mMessagesListView.setLayoutManager(layoutManager);
        mMessagesListView.setAdapter(mAdapter);

        final boolean useOutline = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;

        if (useOutline) {
            final float elevation = getResources().getDimension(R.dimen.element_spacing_normal);
            final PanelElevationProperty property = new PanelElevationProperty(elevation);
            mEffectHelper = new EffectViewHelper(mInputPanel, property, 100);
        } else {
            mEffectHelper = new EffectViewHelper(mInputPanelShadowCompat, View.ALPHA, 100);
        }
        mScrollListener = new PanelShowHideListener(mEffectHelper);

        mInputPanelShadowCompat.setVisibility(useOutline ? View.GONE : View.VISIBLE);
        ViewCompat.setAlpha(mInputPanelShadowCompat, 0);

        mUsersSearchAdapter = new SimpleParcelableUsersAdapter(activity);
        mUsersSearchList.setAdapter(mUsersSearchAdapter);
        mUsersSearchList.setEmptyView(mUsersSearchEmpty);
        mUsersSearchList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ParcelableCredentials account = (ParcelableCredentials) mAccountSpinner.getSelectedItem();
                showConversation(account, mUsersSearchAdapter.getItem(position));
                updateRecipientInfo();
            }
        });

        setupEditQuery();
        setupEditText();

        mSendButton.setOnClickListener(this);
        mAddImageButton.setOnClickListener(this);
        mSendButton.setEnabled(false);
        if (savedInstanceState != null) {
            final ParcelableCredentials account = savedInstanceState.getParcelable(EXTRA_ACCOUNT);
            final ParcelableUser recipient = savedInstanceState.getParcelable(EXTRA_USER);
            showConversation(account, recipient);
            mEditText.setText(savedInstanceState.getString(EXTRA_TEXT));
            mImageUri = savedInstanceState.getString(EXTRA_IMAGE_URI);
        } else {
            final Bundle args = getArguments();
            final ParcelableCredentials account;
            final ParcelableUser recipient;
            if (args != null) {
                if (args.containsKey(EXTRA_ACCOUNT)) {
                    account = args.getParcelable(EXTRA_ACCOUNT);
                    recipient = args.getParcelable(EXTRA_USER);
                } else if (args.containsKey(EXTRA_ACCOUNT_KEY) && args.containsKey(EXTRA_RECIPIENT_ID)) {
                    final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
                    if (accountKey == null) {
                        getActivity().finish();
                        return;
                    }
                    final int accountPos = accountsSpinnerAdapter.findPositionByKey(accountKey);
                    if (accountPos >= 0) {
                        mAccountSpinner.setSelection(accountPos);
                    }
                    final String userId = args.getString(EXTRA_RECIPIENT_ID);
                    if (accountPos >= 0) {
                        account = accountsSpinnerAdapter.getItem(accountPos);
                    } else {
                        account = ParcelableCredentialsUtils.getCredentials(activity, accountKey);
                    }
                    if (userId != null) {
                        recipient = Utils.getUserForConversation(activity, accountKey, userId);
                    } else {
                        recipient = null;
                    }
                } else {
                    account = null;
                    recipient = null;
                }
                showConversation(account, recipient);
                if (account != null && recipient != null) {
                    final String key = getDraftsTextKey(account.account_key, recipient.key);
                    mEditText.setText(mMessageDrafts.getString(key, null));
                }
            }
        }
        mEditText.setSelection(mEditText.length());
        final boolean isValid = mAccount != null && mRecipient != null;
        mConversationContainer.setVisibility(isValid ? View.VISIBLE : View.GONE);
        mRecipientSelectorContainer.setVisibility(isValid ? View.GONE : View.VISIBLE);

        mUsersSearchList.setVisibility(View.GONE);
        mUsersSearchProgress.setVisibility(View.GONE);

        registerForContextMenu(mMessagesListView);

        mQueryTextChanged = false;
        mTextChanged = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        updateEmptyText();
        mMessagesListView.addOnScrollListener(mScrollListener);
        mScrollListener.reset();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAddImageButton();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEditText != null) {
            outState.putCharSequence(EXTRA_TEXT, mEditText.getText());
        }
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
        outState.putParcelable(EXTRA_USER, mRecipient);
        outState.putString(EXTRA_IMAGE_URI, mImageUri);
    }

    @Override
    public void onStop() {
        mMessagesListView.removeOnScrollListener(mScrollListener);
        mBus.unregister(this);
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }

        final ParcelableCredentials account = mAccount;
        final ParcelableUser recipient = mRecipient;
        if (account != null && recipient != null) {
            final String key = getDraftsTextKey(account.account_key, recipient.key);
            final SharedPreferences.Editor editor = mMessageDrafts.edit();
            final String text = ParseUtils.parseString(mEditText.getText());
            if (TextUtils.isEmpty(text)) {
                editor.remove(key);
            } else {
                editor.putString(key, text);
            }
            editor.apply();
        }
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_direct_messages_conversation, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuUtils.setMenuItemAvailability(menu, R.id.delete_all, mRecipient != null
                && Utils.isOfficialCredentials(getActivity(), mAccount));
        updateRecipientInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all: {
                final ParcelableCredentials account = mAccount;
                if (account == null || mRecipient == null) return true;
                Bundle args = new Bundle();
                args.putParcelable(EXTRA_ACCOUNT, account);
                args.putParcelable(EXTRA_USER, mRecipient);
                DialogFragment df = new DeleteConversationConfirmDialogFragment();
                df.setArguments(args);
                df.show(getFragmentManager(), "delete_conversation_confirm");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUsersSearchProgress = view.findViewById(R.id.users_search_progress);
        mUsersSearchList = (ListView) view.findViewById(R.id.users_search_list);
        mUsersSearchEmpty = view.findViewById(R.id.users_search_empty);
        mUsersSearchEmptyText = (TextView) view.findViewById(R.id.users_search_empty_text);
        mMessagesListView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mConversationContainer = view.findViewById(R.id.conversation_container);
        mUsersSearchList = (ListView) view.findViewById(R.id.users_search_list);
        mRecipientSelectorContainer = view.findViewById(R.id.recipient_selector_container);
        mInputPanelShadowCompat = view.findViewById(R.id.input_panel_shadow_compat);
        mInputPanel = view.findViewById(R.id.input_panel);
        mEditText = (ComposeEditText) mInputPanel.findViewById(R.id.edit_text);
        mSendButton = mInputPanel.findViewById(R.id.send);
        mAddImageButton = (ImageView) mInputPanel.findViewById(R.id.add_image);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final UserKey accountId = args != null ? args.<UserKey>getParcelable(EXTRA_ACCOUNT_KEY) : null;
        final String recipientId = args != null ? args.getString(EXTRA_RECIPIENT_ID) : null;
        final String[] cols = DirectMessages.COLUMNS;
        final boolean isValid = accountId != null && recipientId != null;
        mConversationContainer.setVisibility(isValid ? View.VISIBLE : View.GONE);
        mRecipientSelectorContainer.setVisibility(isValid ? View.GONE : View.VISIBLE);
        if (!isValid) {
            return new CursorLoader(getActivity(), TwidereDataStore.CONTENT_URI_NULL, cols, null, null, null);
        }
        final Uri uri = buildDirectMessageConversationUri(accountId, recipientId, null);
        return new CursorLoader(getActivity(), uri, cols, null, null, Conversation.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.send: {
                sendDirectMessage();
                break;
            }
            case R.id.add_image: {
                final Intent intent = ThemedImagePickerActivity.withThemed(getActivity()).build();
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
                break;
            }
            case R.id.query_button: {
                final ParcelableCredentials account = (ParcelableCredentials) mAccountSpinner.getSelectedItem();
                searchUsers(account.account_key, ParseUtils.parseString(mEditUserQuery.getText()), false);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int pos, final long id) {
        final ParcelableCredentials account = (ParcelableCredentials) mAccountSpinner.getSelectedItem();
        if (account != null) {
            mAccount = account;
            updateRecipientInfo();
            updateAccount();
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> view) {

    }

    @Override
    public void onMenuButtonClick(final View button, final int position, final long id) {
        mSelectedDirectMessage = mAdapter.findItem(id);
        showMenu(button, mSelectedDirectMessage);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        final ParcelableDirectMessage message = mSelectedDirectMessage;
        if (message != null) {
            switch (item.getItemId()) {
                case R.id.delete: {
                    mTwitterWrapper.destroyDirectMessageAsync(message.account_key, message.id);
                    break;
                }
                case R.id.copy: {
                    if (ClipboardUtils.setText(getActivity(), message.text_plain)) {
                        Utils.showOkMessage(getActivity(), R.string.text_copied, false);
                    }
                    break;
                }
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.setCursor(cursor);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (menuInfo == null) return;
        final MenuInflater inflater = new MenuInflater(getContext());
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) menuInfo;
        ParcelableDirectMessage message = mAdapter.getDirectMessage(contextMenuInfo.getPosition());
        menu.setHeaderTitle(message.text_unescaped);
        inflater.inflate(R.menu.action_direct_message, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        final ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) menuInfo;
        ParcelableDirectMessage message = mAdapter.getDirectMessage(contextMenuInfo.getPosition());
        if (message == null) return false;
        switch (item.getItemId()) {
            case R.id.copy: {
                ClipboardUtils.setText(getContext(), message.text_plain);
                return true;
            }
            case R.id.delete: {
                Bundle args = new Bundle();
                args.putParcelable(EXTRA_ACCOUNT, mAccount);
                args.putParcelable(EXTRA_MESSAGE, message);
                DeleteMessageConfirmDialogFragment df = new DeleteMessageConfirmDialogFragment();
                df.setArguments(args);
                df.show(getFragmentManager(), "delete_message_confirm");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            final boolean showingConversation = isShowingConversation();
            final EditText editText = showingConversation ? mEditText : mEditUserQuery;
            final boolean textChanged = showingConversation ? mTextChanged : mQueryTextChanged;
            if (editText.length() == 0 && !textChanged) {
                final FragmentActivity activity = getActivity();
                if (!mNavigateBackPressed) {
                    Toast.makeText(activity, R.string.press_again_to_close, Toast.LENGTH_SHORT).show();
                    editText.removeCallbacks(mBackTimeoutRunnable);
                    editText.postDelayed(mBackTimeoutRunnable, 2000);
                    mNavigateBackPressed = true;
                } else {
                    activity.onBackPressed();
                }
            } else {
                mQueryTextChanged = false;
                mTextChanged = false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_BACK.equals(action);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return false;
    }


    public void showConversation(final ParcelableCredentials account, final ParcelableUser recipient) {
        mAccount = account;
        mRecipient = recipient;
        if (account == null || recipient == null) return;
        final LoaderManager lm = getLoaderManager();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, account.account_key);
        args.putString(EXTRA_RECIPIENT_ID, recipient.key.getId());
        if (mLoaderInitialized) {
            lm.restartLoader(0, args, this);
        } else {
            mLoaderInitialized = true;
            lm.initLoader(0, args, this);
        }
        AsyncTaskUtils.executeTask(new SetReadStateTask(getActivity(), account, recipient));
        updateActionBar();
        updateRecipientInfo();
        updateAccount();
        mEditText.requestFocus();
    }

    private void updateAccount() {
        if (mAccount == null) return;
        if (Utils.isOfficialCredentials(getContext(), mAccount)) {
            mAddImageButton.setVisibility(View.VISIBLE);
        } else {
            mAddImageButton.setVisibility(View.GONE);
        }
    }

    public boolean isShowingConversation() {
        return mConversationContainer.getVisibility() == View.VISIBLE;
    }

    private String getDraftsTextKey(UserKey accountKey, UserKey userId) {
        return String.format(Locale.ROOT, "text_%s_to_%s", accountKey, userId);
    }

    private void searchUsers(UserKey accountKey, String query, boolean fromCache) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey);
        args.putString(EXTRA_QUERY, query);
        args.putBoolean(EXTRA_FROM_CACHE, fromCache);
        final LoaderManager lm = getLoaderManager();
        if (mSearchUsersLoaderInitialized) {
            lm.restartLoader(LOADER_ID_SEARCH_USERS, args, mSearchLoadersCallback);
        } else {
            mSearchUsersLoaderInitialized = true;
            lm.initLoader(LOADER_ID_SEARCH_USERS, args, mSearchLoadersCallback);
        }
    }

//    @Override
//    public void onRefreshFromEnd() {
//        new TwidereAsyncTask<Object, Object, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Object... params) {
//                final long[][] result = new long[2][];
//                result[0] = getActivatedAccountIds(getActivity());
//                result[1] = getNewestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(final long[][] result) {
//                final AsyncTwitterWrapper twitter = getTwitterWrapper();
//                if (twitter == null) return;
//                twitter.getReceivedDirectMessagesAsync(result[0], null, result[1]);
//                twitter.getSentDirectMessagesAsync(result[0], null, null);
//            }
//
//        }.executeTask();
//    }
//
//    @Override
//    public void onRefresh() {
//        loadMoreMessages();
//    }

    private void sendDirectMessage() {
        final ParcelableCredentials account = mAccount;
        final ParcelableUser recipient = mRecipient;
        if (mAccount == null || mRecipient == null) return;
        final String message = mEditText.getText().toString();
        if (TextUtils.isEmpty(message)) {
            mEditText.setError(getString(R.string.error_message_no_content));
        } else {
            mTwitterWrapper.sendDirectMessageAsync(account.account_key, recipient.key.getId(),
                    message, mImageUri);
            mEditText.setText(null);
            mImageUri = null;
            updateAddImageButton();
        }
    }

    private void setupEditQuery() {
        final EditTextEnterHandler queryEnterHandler = EditTextEnterHandler.attach(mEditUserQuery, new EnterListener() {
            @Override
            public boolean shouldCallListener() {
                final FragmentActivity activity = getActivity();
                if (!(activity instanceof BaseActivity)) return false;
                return ((BaseActivity) activity).getKeyMetaState() == 0;
            }

            @Override
            public boolean onHitEnter() {
                final FragmentActivity activity = getActivity();
                if (!(activity instanceof BaseActivity)) return false;
                if (((BaseActivity) activity).getKeyMetaState() != 0) return false;
                final ParcelableCredentials account = (ParcelableCredentials) mAccountSpinner.getSelectedItem();
                if (account == null) return false;
                mEditText.setAccountKey(account.account_key);
                searchUsers(account.account_key, ParseUtils.parseString(mEditUserQuery.getText()), false);
                return true;
            }
        }, true);
        queryEnterHandler.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final ParcelableCredentials account = (ParcelableCredentials) mAccountSpinner.getSelectedItem();
                if (account == null) return;
                mEditText.setAccountKey(account.account_key);
                searchUsers(account.account_key, ParseUtils.parseString(s), true);
            }
        });
        mEditUserQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                Utils.removeLineBreaks(s);
                mQueryTextChanged = s.length() == 0;
            }
        });
    }

    private void setupEditText() {
        EditTextEnterHandler.attach(mEditText, new EnterListener() {
            @Override
            public boolean shouldCallListener() {
                return true;
            }

            @Override
            public boolean onHitEnter() {
                sendDirectMessage();
                return true;
            }
        }, mPreferences.getBoolean(KEY_QUICK_SEND, false));
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(final Editable s) {
                mTextChanged = s.length() == 0;
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (mSendButton == null || s == null) return;
                mSendButton.setEnabled(mValidator.isValidDirectMessage(s.toString()));
            }
        });
    }

    private void showMenu(final View view, final ParcelableDirectMessage dm) {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        final Context context = getActivity();
        mPopupMenu = new PopupMenu(context, view);
        mPopupMenu.inflate(R.menu.action_direct_message);
        final Menu menu = mPopupMenu.getMenu();
        final MenuItem view_profile_item = menu.findItem(R.id.view_profile);
        if (view_profile_item != null && dm != null) {
            view_profile_item.setVisible(!TextUtils.equals(dm.account_key.getId(), dm.sender_id));
        }
        mPopupMenu.setOnMenuItemClickListener(this);
        mPopupMenu.show();
    }

    private void updateActionBar() {
        final BaseActivity activity = (BaseActivity) getActivity();
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayOptions(mRecipient != null ? ActionBar.DISPLAY_SHOW_TITLE : ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
    }

    private void updateAddImageButton() {
        mAddImageButton.setActivated(mImageUri != null);
    }

//    @Override
//    public boolean scrollToStart() {
//        if (mAdapter == null || mAdapter.isEmpty()) return false;
//        setSelection(mAdapter.getCount() - 1);
//        return true;
//    }

    private void updateEmptyText() {
        final boolean noQuery = mEditUserQuery.length() <= 0;
        if (noQuery) {
            mUsersSearchEmptyText.setText(R.string.type_name_to_search);
        } else {
            mUsersSearchEmptyText.setText(R.string.no_user_found);
        }
    }

    private void updateRecipientInfo() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        if (mRecipient != null) {
            activity.setTitle(mUserColorNameManager.getDisplayName(mRecipient,
                    mPreferences.getBoolean(KEY_NAME_FIRST)));
        } else {
            activity.setTitle(R.string.direct_messages);
        }
    }

//    @Override
//    protected void onReachedTop() {
//        if (!mLoadMoreAutomatically) return;
//        loadMoreMessages();
//    }

    private void updateRefreshState() {
//        final AsyncTwitterWrapper twitter = getTwitterWrapper();
//        if (twitter == null || !getUserVisibleHint()) return;
//        final boolean refreshing = twitter.isReceivedDirectMessagesRefreshing()
//                || twitter.isSentDirectMessagesRefreshing();
//        setRefreshing(refreshing);
    }

//    private void loadMoreMessages() {
//        if (isRefreshing()) return;
//        new TwidereAsyncTask<Object, Object, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Object... params) {
//                final long[][] result = new long[3][];
//                result[0] = getActivatedAccountIds(getActivity());
//                result[1] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
//                result[2] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(final long[][] result) {
//                final AsyncTwitterWrapper twitter = getTwitterWrapper();
//                if (twitter == null) return;
//                twitter.getReceivedDirectMessagesAsync(result[0], result[1], null);
//                twitter.getSentDirectMessagesAsync(result[0], result[2], null);
//            }
//
//        }.executeTask();
//    }

    public static class CacheUserSearchLoader extends UserSearchLoader {
        private final boolean mFromCache;
        private final UserColorNameManager mUserColorNameManager;

        public CacheUserSearchLoader(MessagesConversationFragment fragment, UserKey accountKey,
                                     String query, boolean fromCache, boolean fromUser) {
            super(fragment.getContext(), accountKey, query, 0, null, fromUser);
            mUserColorNameManager = fragment.mUserColorNameManager;
            mFromCache = fromCache;
        }

        @Override
        public List<ParcelableUser> loadInBackground() {
            final String query = getQuery();
            if (TextUtils.isEmpty(query)) return Collections.emptyList();
            if (mFromCache) {
                final Context context = getContext();
                final ArrayList<ParcelableUser> cachedList = new ArrayList<>();
                final String queryEscaped = query.replace("_", "^_");
                final Expression selection;
                final String[] selectionArgs;
                if (queryEscaped != null) {
                    final String[] nicknameKeys = Utils.getMatchedNicknameKeys(query, mUserColorNameManager);
                    selection = Expression.or(Expression.likeRaw(new Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                            Expression.likeRaw(new Column(CachedUsers.NAME), "?||'%'", "^"),
                            Expression.inArgs(new Column(CachedUsers.USER_KEY), nicknameKeys.length));
                    selectionArgs = new String[nicknameKeys.length + 2];
                    selectionArgs[0] = selectionArgs[1] = queryEscaped;
                    System.arraycopy(nicknameKeys, 0, selectionArgs, 2, nicknameKeys.length);
                } else {
                    selection = null;
                    selectionArgs = null;
                }
                final String[] order = {CachedUsers.LAST_SEEN, CachedUsers.SCREEN_NAME, CachedUsers.NAME};
                final boolean[] ascending = {false, true, true};
                final OrderBy orderBy = new OrderBy(order, ascending);
                final Cursor c = context.getContentResolver().query(CachedUsers.CONTENT_URI,
                        CachedUsers.BASIC_COLUMNS, selection != null ? selection.getSQL() : null,
                        selectionArgs, orderBy.getSQL());
                final ParcelableUserCursorIndices i = new ParcelableUserCursorIndices(c);
                assert c != null;
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    cachedList.add(i.newObject(c));
                    c.moveToNext();
                }
                c.close();
                return cachedList;
            }
            return super.loadInBackground();
        }
    }

    public static class DeleteConversationConfirmDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.delete_conversation_confirm_message);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Bundle args = getArguments();
                    final ParcelableCredentials account = args.getParcelable(EXTRA_ACCOUNT);
                    final ParcelableUser user = args.getParcelable(EXTRA_USER);
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
                    if (account == null || user == null || twitter == null) return;
                    twitter.destroyMessageConversationAsync(account.account_key, user.key.getId());
                    break;
                }
            }
        }
    }


    public static class DeleteMessageConfirmDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.delete_message_confirm_message);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Bundle args = getArguments();
                    final ParcelableCredentials account = args.getParcelable(EXTRA_ACCOUNT);
                    final ParcelableDirectMessage message = args.getParcelable(EXTRA_MESSAGE);
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
                    if (account == null || message == null || twitter == null) return;
                    twitter.destroyDirectMessageAsync(account.account_key, message.id);
                    break;
                }
            }
        }
    }

    public static class SetReadStateTask extends AsyncTask<Object, Object, Cursor> {
        private final Context mContext;
        private final ParcelableCredentials mAccount;
        private final ParcelableUser mRecipient;

        @Inject
        ReadStateManager mReadStateManager;

        public SetReadStateTask(Context context, ParcelableCredentials account, ParcelableUser recipient) {
            GeneralComponentHelper.build(context).inject(this);
            mContext = context;
            mAccount = account;
            mRecipient = recipient;
        }

        @Override
        protected Cursor doInBackground(Object... params) {
            final ContentResolver resolver = mContext.getContentResolver();
            final String[] projection = {ConversationEntries.MESSAGE_ID};
            final String selection = Expression.and(
                    Expression.equalsArgs(ConversationEntries.ACCOUNT_KEY),
                    Expression.equalsArgs(ConversationEntries.CONVERSATION_ID)
            ).getSQL();
            final String[] selectionArgs = {String.valueOf(mAccount.account_key),
                    String.valueOf(mRecipient.key)};
            final String orderBy = new OrderBy(ConversationEntries.MESSAGE_ID, false).getSQL();
            return resolver.query(ConversationEntries.CONTENT_URI, projection, selection,
                    selectionArgs, orderBy);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor.moveToFirst()) {
                final int messageIdIdx = cursor.getColumnIndex(ConversationEntries.MESSAGE_ID);
                final String key = mAccount.account_key + "-" + mRecipient.key;
                mReadStateManager.setPosition(CustomTabType.DIRECT_MESSAGES, key, cursor.getLong(messageIdIdx), false);
            }
            cursor.close();
        }
    }

    private static class PanelShowHideListener extends RecyclerView.OnScrollListener {

        private final EffectViewHelper mEffectHelper;
        private boolean mShowElevation;

        PanelShowHideListener(EffectViewHelper helper) {
            mEffectHelper = helper;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            final boolean showElevation = layoutManager.findLastCompletelyVisibleItemPosition() < layoutManager.getItemCount() - 1;
            if (mShowElevation != showElevation) {
                mEffectHelper.setState(showElevation);
            }
            mShowElevation = showElevation;
        }

        public void reset() {
            mEffectHelper.resetState(mShowElevation);
        }
    }

    private static class PanelElevationProperty extends Property<View, Float> {

        private final float mElevation;

        private PanelElevationProperty(float elevation) {
            super(Float.TYPE, null);
            mElevation = elevation;
        }

        @Override
        public void set(View object, Float value) {
            ViewCompat.setTranslationZ(object, mElevation * value);
        }

        @Override
        public Float get(View object) {
            return ViewCompat.getTranslationZ(object) / mElevation;
        }
    }
}
