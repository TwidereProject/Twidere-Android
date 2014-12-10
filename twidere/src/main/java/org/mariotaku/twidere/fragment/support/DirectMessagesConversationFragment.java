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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.ImagePickerActivity;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.DirectMessagesConversationAdapter;
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.support.UserSearchLoader;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.buildDirectMessageConversationUri;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.showOkMessage;

public class DirectMessagesConversationFragment extends BaseSupportFragment implements
        LoaderCallbacks<Cursor>, OnMenuItemClickListener, TextWatcher, OnClickListener,
        OnItemSelectedListener, OnEditorActionListener, MenuButtonClickListener {

    private static final int LOADER_ID_SEARCH_USERS = 1;

    private TwidereValidator mValidator;
    private AsyncTwitterWrapper mTwitterWrapper;
    private SharedPreferences mPreferences;

    private ListView mMessagesListView, mUsersSearchList;
    private EditText mEditText;
    private StatusTextCountView mTextCountView;
    private View mSendButton;
    private ImageView mAddImageButton;
    private View mConversationContainer, mRecipientSelectorContainer;
    private Spinner mAccountSpinner;
    private ImageView mSenderProfileImageView, mRecipientProfileImageView;
    private EditText mUserQuery;
    private View mUsersSearchProgress;
    private View mQueryButton;

    private PopupMenu mPopupMenu;

    private ParcelableDirectMessage mSelectedDirectMessage;
    private long mAccountId, mRecipientId;
    private boolean mLoaderInitialized;
    private boolean mLoadMoreAutomatically;
    private String mImageUri;

    private Locale mLocale;

    private DirectMessagesConversationAdapter mAdapter;
    private SimpleParcelableUsersAdapter mUsersSearchAdapter;

    private ParcelableAccount mSender;
    private ParcelableUser mRecipient;

    private ImageLoaderWrapper mImageLoader;
    private IColorLabelView mProfileImageContainer;

    private LoaderCallbacks<List<ParcelableUser>> mSearchLoadersCallback = new LoaderCallbacks<List<ParcelableUser>>() {
        @Override
        public Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
            mUsersSearchList.setVisibility(View.GONE);
            mUsersSearchProgress.setVisibility(View.VISIBLE);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID);
            final String query = args.getString(EXTRA_QUERY);
            return new UserSearchLoader(getActivity(), accountId, query, 0, null);
        }

        @Override
        public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
            mUsersSearchList.setVisibility(View.VISIBLE);
            mUsersSearchProgress.setVisibility(View.GONE);
            mUsersSearchAdapter.setData(data, true);
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
        setHasOptionsMenu(true);
        final FragmentActivity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_custom_view_message_user_picker);
            final View actionBarView = actionBar.getCustomView();
            mAccountSpinner = (Spinner) actionBarView.findViewById(R.id.account_spinner);
            mUserQuery = (EditText) actionBarView.findViewById(R.id.user_query);
            mQueryButton = actionBarView.findViewById(R.id.query_button);
            final List<ParcelableAccount> accounts = ParcelableAccount.getAccountsList(activity, false);
            final AccountsSpinnerAdapter adapter = new AccountsSpinnerAdapter(actionBar.getThemedContext(), R.layout.spinner_item_account_icon);
            adapter.setDropDownViewResource(R.layout.list_item_user);
            adapter.addAll(accounts);
            mAccountSpinner.setAdapter(adapter);
            mAccountSpinner.setOnItemSelectedListener(this);
            mQueryButton.setOnClickListener(this);
        }
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mImageLoader = TwidereApplication.getInstance(getActivity()).getImageLoaderWrapper();
        mTwitterWrapper = getTwitterWrapper();
        mValidator = new TwidereValidator(getActivity());
        mLocale = getResources().getConfiguration().locale;
        mAdapter = new DirectMessagesConversationAdapter(getActivity());
        mMessagesListView.setAdapter(mAdapter);
        mAdapter.setMenuButtonClickListener(this);
        mMessagesListView.setDivider(null);
        mMessagesListView.setSelector(android.R.color.transparent);
        mMessagesListView.setFastScrollEnabled(mPreferences.getBoolean(KEY_FAST_SCROLL_THUMB, false));
        mMessagesListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mMessagesListView.setStackFromBottom(true);

        mUsersSearchAdapter = new SimpleParcelableUsersAdapter(activity);
        mUsersSearchList.setAdapter(mUsersSearchAdapter);

        if (mPreferences.getBoolean(KEY_QUICK_SEND, false)) {
            mEditText.setOnEditorActionListener(this);
        }
        mEditText.addTextChangedListener(this);


        mSendButton.setOnClickListener(this);
        mAddImageButton.setOnClickListener(this);
        mSendButton.setEnabled(false);
        if (savedInstanceState != null) {
            final long accountId = savedInstanceState.getLong(EXTRA_ACCOUNT_ID, -1);
            final long recipientId = savedInstanceState.getLong(EXTRA_RECIPIENT_ID, -1);
            showConversation(accountId, recipientId);
            mEditText.setText(savedInstanceState.getString(EXTRA_TEXT));
            mImageUri = savedInstanceState.getString(EXTRA_IMAGE_URI);
        } else {
            final Bundle args = getArguments();
            final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
            final long recipientId = args != null ? args.getLong(EXTRA_RECIPIENT_ID, -1) : -1;
            showConversation(accountId, recipientId);
        }
        final boolean isValid = mAccountId > 0 && mRecipientId > 0;
        mConversationContainer.setVisibility(isValid ? View.VISIBLE : View.GONE);
        mRecipientSelectorContainer.setVisibility(isValid ? View.GONE : View.VISIBLE);

        mUsersSearchList.setVisibility(View.GONE);
        mUsersSearchProgress.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_USER: {
                if (resultCode != Activity.RESULT_OK || !data.hasExtra(EXTRA_USER)) {
                    break;
                }
                final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
                if (user != null && mAccountId > 0) {
                    mRecipientId = user.id;
                    mRecipient = user;
                    showConversation(mAccountId, mRecipientId);
                    updateProfileImage();
                }
                break;
            }
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
        if (mProfileImageContainer == null || mRecipientProfileImageView == null
                || mSenderProfileImageView == null) {
            return;
        }
        mProfileImageContainer.setVisibility(mRecipient != null ? View.VISIBLE : View.GONE);
        if (mSender != null && mRecipient != null) {
            mImageLoader.displayProfileImage(mSenderProfileImageView, mSender.profile_image_url);
            mImageLoader.displayProfileImage(mRecipientProfileImageView, mRecipient.profile_image_url);
            mProfileImageContainer.drawEnd(mSender.color);
        } else {
            mImageLoader.cancelDisplayTask(mSenderProfileImageView);
            mImageLoader.cancelDisplayTask(mRecipientProfileImageView);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_direct_messages_conversation, menu);
        final View profileImageItemView = menu.findItem(R.id.item_profile_image).getActionView();
        profileImageItemView.setOnClickListener(this);
        mProfileImageContainer = (IColorLabelView) profileImageItemView;
        mRecipientProfileImageView = (ImageView) profileImageItemView.findViewById(R.id.recipient_profile_image);
        mSenderProfileImageView = (ImageView) profileImageItemView.findViewById(R.id.sender_profile_image);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.send: {
                sendDirectMessage();
                break;
            }
//            case R.id.recipient_selector: {
//                if (mAccountId <= 0) return;
//                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
//                intent.setClass(getActivity(), UserListSelectorActivity.class);
//                intent.putExtra(EXTRA_ACCOUNT_ID, mAccountId);
//                startActivityForResult(intent, REQUEST_SELECT_USER);
//                break;
//            }
            case R.id.add_image: {
                final Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
                break;
            }
            case R.id.item_profile_image: {
                final ParcelableUser recipient = mRecipient;
                if (recipient == null) return;
                Utils.openUserProfile(getActivity(), recipient.account_id, recipient.id,
                        recipient.screen_name, null);
                break;
            }
            case R.id.query_button: {
                final ParcelableAccount account = (ParcelableAccount) mAccountSpinner.getSelectedItem();
                searchUsers(account.account_id, ParseUtils.parseString(mUserQuery.getText()));
                break;
            }
        }
    }

    private boolean mSearchUsersLoaderInitialized;

    private void searchUsers(long accountId, String query) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
        args.putString(EXTRA_QUERY, query);
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
            return new CursorLoader(getActivity(), TweetStore.CONTENT_URI_NULL, cols, null, null, null);
        final Uri uri = buildDirectMessageConversationUri(accountId, recipientId, null);
        return new CursorLoader(getActivity(), uri, cols, null, null, Conversation.DEFAULT_SORT_ORDER);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
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
            mAccountId = account.account_id;
            mSender = account;
            updateProfileImage();
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.swapCursor(cursor);
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
//    public void onRefreshFromStart() {
//        loadMoreMessages();
//    }

    @Override
    public void onResume() {
        super.onResume();
        configBaseCardAdapter(getActivity(), mAdapter);
        final boolean displayImagePreview = mPreferences.getBoolean(KEY_DISPLAY_IMAGE_PREVIEW, false);
        final String previewScaleType = Utils.getNonEmptyString(mPreferences, KEY_IMAGE_PREVIEW_SCALE_TYPE,
                ScaleType.CENTER_CROP.name());
        mAdapter.setDisplayImagePreview(displayImagePreview);
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
        outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
        outState.putLong(EXTRA_RECIPIENT_ID, mRecipientId);
        outState.putString(EXTRA_IMAGE_URI, mImageUri);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
        updateTextCount();
    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        super.onStop();
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        updateTextCount();
        if (mSendButton == null || s == null) return;
        mSendButton.setEnabled(mValidator.isValidTweet(s.toString()));
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUsersSearchProgress = view.findViewById(R.id.users_search_progress);
        mUsersSearchList = (ListView) view.findViewById(R.id.users_search_list);
        mMessagesListView = (ListView) view.findViewById(android.R.id.list);
        final View inputSendContainer = view.findViewById(R.id.input_send_container);
        mConversationContainer = view.findViewById(R.id.conversation_container);
        mRecipientSelectorContainer = view.findViewById(R.id.recipient_selector_container);
        mEditText = (EditText) inputSendContainer.findViewById(R.id.edit_text);
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

    public void showConversation(final long accountId, final long recipientId) {
        mAccountId = accountId;
        mRecipientId = recipientId;
        final Context context = getActivity();
        mSender = ParcelableAccount.getAccount(context, accountId);
        mRecipient = Utils.getUserForConversation(context, accountId, recipientId);
        final LoaderManager lm = getLoaderManager();
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
        args.putLong(EXTRA_RECIPIENT_ID, recipientId);
        if (mLoaderInitialized) {
            lm.restartLoader(0, args, this);
        } else {
            mLoaderInitialized = true;
            lm.initLoader(0, args, this);
        }
        updateActionBar();
        updateProfileImage();
    }

    private void updateActionBar() {
        final FragmentActivity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
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
        final Editable text = mEditText.getText();
        if (isEmpty(text) || mAccountId <= 0 || mRecipientId <= 0) return;
        final String message = text.toString();
        if (mValidator.isValidTweet(message)) {
            mTwitterWrapper.sendDirectMessageAsync(mAccountId, mRecipientId, message, mImageUri);
            text.clear();
            mImageUri = null;
            updateAddImageButton();
        }
    }

    private void showMenu(final View view, final ParcelableDirectMessage dm) {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
        final Context context = getActivity();
        mPopupMenu = PopupMenu.getInstance(context, view);
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

    private static class UsersSearchAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private Object mUsers;
        private int mScreenNameIdx;
        private long mAccountId;

        public UsersSearchAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setUsers(List<ParcelableUser> users) {
            mUsers = users;
            notifyDataSetChanged();
        }

        public void setUsers(Cursor users) {
            mUsers = users;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mUsers instanceof Cursor) {
                final Cursor c = (Cursor) mUsers;
                mScreenNameIdx = c.getColumnIndex(CachedUsers.SCREEN_NAME);
                return c.getCount();
            } else if (mUsers instanceof List) {
                return ((List) mUsers).size();
            }
            return 0;
        }

        public void setAccountId(long accountId) {
            mAccountId = accountId;
        }

        @Override
        public ParcelableUser getItem(int position) {
            if (mUsers instanceof Cursor) {
                final Cursor c = (Cursor) mUsers;
                return new ParcelableUser(c, mAccountId);
            } else if (mUsers instanceof List) {
                return (ParcelableUser) ((List) mUsers).get(position);
            }
            throw new IllegalStateException();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_user, parent, false);
            }
            if (mUsers instanceof Cursor) {
                final Cursor c = (Cursor) mUsers;
                c.moveToPosition(position);
                bindUser(view, c);
            } else if (mUsers instanceof List) {
                bindUser(view, getItem(position));
            }
            return view;
        }

        private void bindUser(View view, ParcelableUser user) {

        }

        private void bindUser(View view, Cursor cursor) {

        }
    }
}
