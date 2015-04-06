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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseActionBarActivity;
import org.mariotaku.twidere.activity.support.ImagePickerActivity;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.MessageConversationAdapter;
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.support.UserSearchLoader;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUser.CachedIndices;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.view.StatusComposeEditText;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.buildDirectMessageConversationUri;
import static org.mariotaku.twidere.util.Utils.showOkMessage;

public class MessagesConversationFragment extends BaseSupportFragment implements
        LoaderCallbacks<Cursor>, TextWatcher, OnClickListener, OnItemSelectedListener,
        OnEditorActionListener, MenuButtonClickListener, PopupMenu.OnMenuItemClickListener {

    private static final int LOADER_ID_SEARCH_USERS = 1;

    private static final String EXTRA_FROM_CACHE = "from_cache";

    private TwidereValidator mValidator;
    private AsyncTwitterWrapper mTwitterWrapper;
    private SharedPreferences mPreferences;
    private SharedPreferences mMessageDrafts;
    private ReadStateManager mReadStateManager;

    private RecyclerView mMessagesListView;
    private ListView mUsersSearchList;
    private StatusComposeEditText mEditText;
    private StatusTextCountView mTextCountView;
    private View mSendButton;
    private ImageView mAddImageButton;
    private View mConversationContainer, mRecipientSelectorContainer;
    private Spinner mAccountSpinner;
    private ImageView mRecipientProfileImageView;
    private EditText mUserQuery;
    private View mUsersSearchProgress;
    private View mQueryButton;
    private View mUsersSearchEmpty;
    private TextView mUsersSearchEmptyText;

    private PopupMenu mPopupMenu;

    private ParcelableDirectMessage mSelectedDirectMessage;
    private boolean mLoaderInitialized;
    private boolean mLoadMoreAutomatically;
    private String mImageUri;


    private MessageConversationAdapter mAdapter;
    private SimpleParcelableUsersAdapter mUsersSearchAdapter;

    private ParcelableAccount mAccount;
    private ParcelableUser mRecipient;

    private MediaLoaderWrapper mImageLoader;
    private IColorLabelView mProfileImageContainer;

    private LoaderCallbacks<List<ParcelableUser>> mSearchLoadersCallback = new LoaderCallbacks<List<ParcelableUser>>() {
        @Override
        public Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
            mUsersSearchList.setVisibility(View.GONE);
            mUsersSearchProgress.setVisibility(View.VISIBLE);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID);
            final String query = args.getString(EXTRA_QUERY);
            final boolean fromCache = args.getBoolean(EXTRA_FROM_CACHE);
            return new CacheUserSearchLoader(getActivity(), accountId, query, fromCache);
        }

        @Override
        public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
            mUsersSearchList.setVisibility(View.VISIBLE);
            mUsersSearchProgress.setVisibility(View.GONE);
            mUsersSearchAdapter.setData(data, true);
            updateEmptyText();
        }

        @Override
        public void onLoaderReset(Loader<List<ParcelableUser>> loader) {

        }
    };


    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
    }

    @Override
    public void afterTextChanged(final Editable s) {

    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final BaseActionBarActivity activity = (BaseActionBarActivity) getActivity();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mMessageDrafts = getSharedPreferences(MESSAGE_DRAFTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mImageLoader = TwidereApplication.getInstance(activity).getMediaLoaderWrapper();
        mReadStateManager = getReadStateManager();
        mTwitterWrapper = getTwitterWrapper();
        mValidator = new TwidereValidator(activity);

        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context viewContext = view.getContext();
        setHasOptionsMenu(true);
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) throw new NullPointerException();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_custom_view_message_user_picker);
        final View actionBarView = actionBar.getCustomView();
        mAccountSpinner = (Spinner) actionBarView.findViewById(R.id.account_spinner);
        mUserQuery = (EditText) actionBarView.findViewById(R.id.user_query);
        mQueryButton = actionBarView.findViewById(R.id.query_button);
        final List<ParcelableAccount> accounts = ParcelableAccount.getAccountsList(activity, false);
        final AccountsSpinnerAdapter accountsSpinnerAdapter = new AccountsSpinnerAdapter(
                actionBar.getThemedContext(), R.layout.spinner_item_account_icon);
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_user);
        accountsSpinnerAdapter.addAll(accounts);
        mAccountSpinner.setAdapter(accountsSpinnerAdapter);
        mAccountSpinner.setOnItemSelectedListener(this);
        mUserQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final ParcelableAccount account = (ParcelableAccount) mAccountSpinner.getSelectedItem();
                mEditText.setAccountId(account.account_id);
                searchUsers(account.account_id, ParseUtils.parseString(s), true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mQueryButton.setOnClickListener(this);
        mAdapter = new MessageConversationAdapter(activity);
        final LinearLayoutManager layoutManager = new FixedLinearLayoutManager(viewContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        mMessagesListView.setLayoutManager(layoutManager);
        mMessagesListView.setAdapter(mAdapter);

        mUsersSearchAdapter = new SimpleParcelableUsersAdapter(activity);
        mUsersSearchList.setAdapter(mUsersSearchAdapter);
        mUsersSearchList.setEmptyView(mUsersSearchEmpty);
        mUsersSearchList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ParcelableAccount account = (ParcelableAccount) mAccountSpinner.getSelectedItem();
                showConversation(account, mUsersSearchAdapter.getItem(position));
                updateProfileImage();
            }
        });

        if (mPreferences.getBoolean(KEY_QUICK_SEND, false)) {
            mEditText.setOnEditorActionListener(this);
        }
        mEditText.addTextChangedListener(this);


        mSendButton.setOnClickListener(this);
        mAddImageButton.setOnClickListener(this);
        mSendButton.setEnabled(false);
        if (savedInstanceState != null) {
            final ParcelableAccount account = savedInstanceState.getParcelable(EXTRA_ACCOUNT);
            final ParcelableUser recipient = savedInstanceState.getParcelable(EXTRA_USER);
            showConversation(account, recipient);
            mEditText.setText(savedInstanceState.getString(EXTRA_TEXT));
            mImageUri = savedInstanceState.getString(EXTRA_IMAGE_URI);
        } else {
            final Bundle args = getArguments();
            final ParcelableAccount account;
            final ParcelableUser recipient;
            if (args != null) {
                if (args.containsKey(EXTRA_ACCOUNT)) {
                    account = args.getParcelable(EXTRA_ACCOUNT);
                    recipient = args.getParcelable(EXTRA_USER);
                } else if (args.containsKey(EXTRA_ACCOUNT_ID)) {
                    final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
                    final long userId = args.getLong(EXTRA_RECIPIENT_ID, -1);
                    final int accountPos = accountsSpinnerAdapter.findItemPosition(accountId);
                    account = accountPos < 0 ? ParcelableAccount.getCredentials(activity, accountId)
                            : accountsSpinnerAdapter.getItem(accountPos);
                    recipient = Utils.getUserForConversation(activity, accountId, userId);
                } else {
                    account = null;
                    recipient = null;
                }
                showConversation(account, recipient);
                if (account != null && recipient != null) {
                    final String key = getDraftsTextKey(account.account_id, recipient.id);
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

    }

    private String getDraftsTextKey(long accountId, long userId) {
        return String.format(Locale.ROOT, "text_%d_to_%d", accountId, userId);
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateProfileImage();
    }

    private void updateProfileImage() {
        if (mProfileImageContainer == null || mRecipientProfileImageView == null) {
            return;
        }
        mProfileImageContainer.setVisibility(mRecipient != null ? View.VISIBLE : View.GONE);
        if (mAccount != null && mRecipient != null) {
            mImageLoader.displayProfileImage(mRecipientProfileImageView, mRecipient.profile_image_url);
            mProfileImageContainer.drawEnd(mAccount.color);
        } else {
            mImageLoader.cancelDisplayTask(mRecipientProfileImageView);
        }
        final FragmentActivity activity = getActivity();
        if (mRecipient != null) {
            activity.setTitle(UserColorNameUtils.getDisplayName(activity, mRecipient));
        } else {
            activity.setTitle(R.string.direct_messages);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_direct_messages_conversation, menu);
        final View profileImageItemView = MenuItemCompat.getActionView(menu.findItem(R.id.item_profile_image));
        profileImageItemView.setOnClickListener(this);
        mProfileImageContainer = (IColorLabelView) profileImageItemView;
        mRecipientProfileImageView = (ImageView) profileImageItemView.findViewById(R.id.recipient_profile_image);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.send: {
                sendDirectMessage();
                break;
            }
            case R.id.add_image: {
                final Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
                break;
            }
            case R.id.item_profile_image: {
                final ParcelableUser recipient = mRecipient;
                if (recipient == null) return;
                final Bundle options = Utils.makeSceneTransitionOption(getActivity(),
                        new Pair<View, String>(mRecipientProfileImageView,
                                UserFragment.TRANSITION_NAME_PROFILE_IMAGE));
                Utils.openUserProfile(getActivity(), recipient.account_id, recipient.id,
                        recipient.screen_name, options);
                break;
            }
            case R.id.query_button: {
                final ParcelableAccount account = (ParcelableAccount) mAccountSpinner.getSelectedItem();
                searchUsers(account.account_id, ParseUtils.parseString(mUserQuery.getText()), false);
                break;
            }
        }
    }

    private boolean mSearchUsersLoaderInitialized;

    private void searchUsers(long accountId, String query, boolean fromCache) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
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

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
        final long recipientId = args != null ? args.getLong(EXTRA_RECIPIENT_ID, -1) : -1;
        final String[] cols = DirectMessages.COLUMNS;
        final boolean isValid = accountId > 0 && recipientId > 0;
        mConversationContainer.setVisibility(isValid ? View.VISIBLE : View.GONE);
        mRecipientSelectorContainer.setVisibility(isValid ? View.GONE : View.VISIBLE);
        if (!isValid)
            return new CursorLoader(getActivity(), TwidereDataStore.CONTENT_URI_NULL, cols, null, null, null);
        final Uri uri = buildDirectMessageConversationUri(accountId, recipientId, null);
        return new CursorLoader(getActivity(), uri, cols, null, null, Conversation.DEFAULT_SORT_ORDER);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view == null) return;
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER: {
                sendDirectMessage();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int pos, final long id) {
        final ParcelableAccount account = (ParcelableAccount) mAccountSpinner.getSelectedItem();
        if (account != null) {
            mAccount = account;
            updateProfileImage();
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.setCursor(cursor);
    }

    @Override
    public void onMenuButtonClick(final View button, final int position, final long id) {
        mSelectedDirectMessage = mAdapter.findItem(id);
        showMenu(button, mSelectedDirectMessage);
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        if (mSelectedDirectMessage != null) {
            final long message_id = mSelectedDirectMessage.id;
            final long account_id = mSelectedDirectMessage.account_id;
            switch (item.getItemId()) {
                case MENU_DELETE: {
                    mTwitterWrapper.destroyDirectMessageAsync(account_id, message_id);
                    break;
                }
                case MENU_COPY: {
                    if (ClipboardUtils.setText(getActivity(), mSelectedDirectMessage.text_plain)) {
                        showOkMessage(getActivity(), R.string.text_copied, false);
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
    public void onNothingSelected(final AdapterView<?> view) {

    }

//    @Override
//    public void onRefreshFromEnd() {
//        new TwidereAsyncTask<Void, Void, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Void... params) {
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

    @Override
    public void onResume() {
        super.onResume();
        final String previewScaleType = Utils.getNonEmptyString(mPreferences, KEY_MEDIA_PREVIEW_STYLE,
                ScaleType.CENTER_CROP.name());
        mAdapter.setImagePreviewScaleType(previewScaleType);
        mAdapter.notifyDataSetChanged();
        mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
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
    public void onStart() {
        super.onStart();
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
        updateTextCount();
        updateEmptyText();
    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }

        final ParcelableAccount account = mAccount;
        final ParcelableUser recipient = mRecipient;
        if (account != null && recipient != null) {
            final String key = getDraftsTextKey(account.account_id, recipient.id);
            final SharedPreferences.Editor editor = mMessageDrafts.edit();
            editor.putString(key, ParseUtils.parseString(mEditText.getText()));
            editor.apply();
        }
        super.onStop();
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        updateTextCount();
        if (mSendButton == null || s == null) return;
        mSendButton.setEnabled(mValidator.isValidTweet(s.toString()));
    }

    private void updateEmptyText() {
        final boolean noQuery = mUserQuery.length() <= 0;
        if (noQuery) {
            mUsersSearchEmptyText.setText(R.string.type_name_to_search);
        } else {
            mUsersSearchEmptyText.setText(R.string.no_user_found);
        }
    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mUsersSearchProgress = view.findViewById(R.id.users_search_progress);
        mUsersSearchList = (ListView) view.findViewById(R.id.users_search_list);
        mUsersSearchEmpty = view.findViewById(R.id.users_search_empty);
        mUsersSearchEmptyText = (TextView) view.findViewById(R.id.users_search_empty_text);
        mMessagesListView = (RecyclerView) view.findViewById(R.id.recycler_view);
        final View inputSendContainer = view.findViewById(R.id.input_send_container);
        mConversationContainer = view.findViewById(R.id.conversation_container);
        mRecipientSelectorContainer = view.findViewById(R.id.recipient_selector_container);
        mEditText = (StatusComposeEditText) inputSendContainer.findViewById(R.id.edit_text);
        mTextCountView = (StatusTextCountView) inputSendContainer.findViewById(R.id.text_count);
        mSendButton = inputSendContainer.findViewById(R.id.send);
        mAddImageButton = (ImageView) inputSendContainer.findViewById(R.id.add_image);
        mUsersSearchList = (ListView) view.findViewById(R.id.users_search_list);
    }

//    @Override
//    public boolean scrollToStart() {
//        if (mAdapter == null || mAdapter.isEmpty()) return false;
//        setSelection(mAdapter.getCount() - 1);
//        return true;
//    }

    public void showConversation(final ParcelableAccount account, final ParcelableUser recipient) {
        mAccount = account;
        mRecipient = recipient;
        if (account == null || recipient == null) return;
        final LoaderManager lm = getLoaderManager();
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, account.account_id);
        args.putLong(EXTRA_RECIPIENT_ID, recipient.id);
        if (mLoaderInitialized) {
            lm.restartLoader(0, args, this);
        } else {
            mLoaderInitialized = true;
            lm.initLoader(0, args, this);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
        new SetReadStateTask(getActivity(), account, recipient).execute();
        updateActionBar();
        updateProfileImage();
    }

    private void updateActionBar() {
        final BaseActionBarActivity activity = (BaseActionBarActivity) getActivity();
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayOptions(mRecipient != null ? ActionBar.DISPLAY_SHOW_TITLE : ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
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
//        setProgressBarIndeterminateVisibility(refreshing);
//        setRefreshing(refreshing);
    }

//    private void loadMoreMessages() {
//        if (isRefreshing()) return;
//        new TwidereAsyncTask<Void, Void, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Void... params) {
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

    private void sendDirectMessage() {
        final ParcelableAccount account = mAccount;
        final ParcelableUser recipient = mRecipient;
        if (mAccount == null || mRecipient == null) return;
        final String message = mEditText.getText().toString();
        if (mValidator.isValidTweet(message)) {
            mTwitterWrapper.sendDirectMessageAsync(account.account_id, recipient.id, message, mImageUri);
            mEditText.setText(null);
            mImageUri = null;
            updateAddImageButton();
        }
    }

    private void showMenu(final View view, final ParcelableDirectMessage dm) {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        final Context context = getActivity();
        mPopupMenu = new PopupMenu(context, view);
        mPopupMenu.inflate(R.menu.action_direct_message);
        final Menu menu = mPopupMenu.getMenu();
        final MenuItem view_profile_item = menu.findItem(MENU_VIEW_PROFILE);
        if (view_profile_item != null && dm != null) {
            view_profile_item.setVisible(dm.account_id != dm.sender_id);
        }
        mPopupMenu.setOnMenuItemClickListener(this);
        mPopupMenu.show();
    }

    private void updateAddImageButton() {
        mAddImageButton.setActivated(mImageUri != null);
    }

    private void updateTextCount() {
        if (mTextCountView == null || mEditText == null) return;
        final int count = mValidator.getTweetLength(ParseUtils.parseString(mEditText.getText()));
        mTextCountView.setTextCount(count);
    }

    public static class CacheUserSearchLoader extends UserSearchLoader {
        private final boolean mFromCache;

        public CacheUserSearchLoader(Context context, long accountId, String query, boolean fromCache) {
            super(context, accountId, query, 0, null);
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
                    final SharedPreferences nicknamePrefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
                    final long[] nicknameIds = Utils.getMatchedNicknameIds(ParseUtils.parseString(query), nicknamePrefs);
                    selection = Expression.or(Expression.likeRaw(new Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                            Expression.likeRaw(new Column(CachedUsers.NAME), "?||'%'", "^"),
                            Expression.in(new Column(CachedUsers.USER_ID), new RawItemArray(nicknameIds)));
                    selectionArgs = new String[]{queryEscaped, queryEscaped};
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
                final CachedIndices i = new CachedIndices(c);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    cachedList.add(new ParcelableUser(c, i, -1));
                    c.moveToNext();
                }
                c.close();
                return cachedList;
            }
            return super.loadInBackground();
        }
    }

    private static class SetReadStateTask extends AsyncTask<Void, Void, Cursor> {
        private final Context mContext;
        private final ReadStateManager mReadStateManager;
        private final ParcelableAccount mAccount;
        private final ParcelableUser mRecipient;

        public SetReadStateTask(Context context, ParcelableAccount account, ParcelableUser recipient) {
            mContext = context;
            mReadStateManager = TwidereApplication.getInstance(context).getReadStateManager();
            mAccount = account;
            mRecipient = recipient;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor.moveToFirst()) {
                final int messageIdIdx = cursor.getColumnIndex(ConversationEntries.MESSAGE_ID);
                final String key = mAccount.account_id + "-" + mRecipient.id;
                mReadStateManager.setPosition(TAB_TYPE_DIRECT_MESSAGES, key, cursor.getLong(messageIdIdx), false);
            }
            Log.d(LOGTAG, Arrays.toString(mReadStateManager.getPositionPairs(TAB_TYPE_DIRECT_MESSAGES)));
            cursor.close();
        }

        @Override
        protected Cursor doInBackground(Void... params) {
            final ContentResolver resolver = mContext.getContentResolver();
            final String[] projection = {ConversationEntries.MESSAGE_ID};
            final String selection = Expression.and(
                    Expression.equals(ConversationEntries.ACCOUNT_ID, mAccount.account_id),
                    Expression.equals(ConversationEntries.CONVERSATION_ID, mRecipient.id)
            ).getSQL();
            final String orderBy = new OrderBy(ConversationEntries.MESSAGE_ID, false).getSQL();
            return resolver.query(ConversationEntries.CONTENT_URI, projection, selection, null, orderBy);
        }
    }
}
