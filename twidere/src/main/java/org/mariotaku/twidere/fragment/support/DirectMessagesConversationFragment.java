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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.ImagePickerActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.DirectMessagesConversationAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.buildDirectMessageConversationUri;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.showOkMessage;

public class DirectMessagesConversationFragment extends BasePullToRefreshListFragment implements
        LoaderCallbacks<Cursor>, OnMenuItemClickListener, TextWatcher, OnClickListener, Panes.Right,
        OnItemSelectedListener, OnEditorActionListener, MenuButtonClickListener {

    private TwidereValidator mValidator;
    private AsyncTwitterWrapper mTwitterWrapper;
    private SharedPreferences mPreferences;

    private ListView mListView;
    private EditText mEditText;
    private StatusTextCountView mTextCountView;
    private View mSendButton;
    private ImageView mAddImageButton;
    private View mConversationContainer, mRecipientSelectorContainer, mRecipientSelector;
    private Spinner mAccountSpinner;
    private ImageView mSenderProfileImageView, mRecipientProfileImageView;

    private PopupMenu mPopupMenu;

    private ParcelableDirectMessage mSelectedDirectMessage;
    private long mAccountId, mRecipientId;
    private boolean mLoaderInitialized;
    private boolean mLoadMoreAutomatically;
    private String mImageUri;

    private Locale mLocale;

    private DirectMessagesConversationAdapter mAdapter;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
                updateRefreshState();
            }
        }
    };

    private Account mSender;
    private ParcelableUser mRecipient;
    private ImageLoaderWrapper mImageLoader;
    private IColorLabelView mProfileImageContainer;

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
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mImageLoader = TwidereApplication.getInstance(getActivity()).getImageLoaderWrapper();
        mTwitterWrapper = getTwitterWrapper();
        mValidator = new TwidereValidator(getActivity());
        mLocale = getResources().getConfiguration().locale;
        mAdapter = new DirectMessagesConversationAdapter(getActivity());
        setListAdapter(mAdapter);
        mAdapter.setMenuButtonClickListener(this);
        mListView = getListView();
        mListView.setDivider(null);
        mListView.setSelector(android.R.color.transparent);
        mListView.setFastScrollEnabled(mPreferences.getBoolean(KEY_FAST_SCROLL_THUMB, false));
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mListView.setStackFromBottom(true);
        setListShownNoAnimation(false);

        if (mPreferences.getBoolean(KEY_QUICK_SEND, false)) {
            mEditText.setOnEditorActionListener(this);
        }
        mEditText.addTextChangedListener(this);

        final List<Account> accounts = Account.getAccountsList(getActivity(), false);
        mAccountSpinner.setAdapter(new AccountsSpinnerAdapter(getActivity(), accounts));
        mAccountSpinner.setOnItemSelectedListener(this);

        mSendButton.setOnClickListener(this);
        mAddImageButton.setOnClickListener(this);
        mSendButton.setEnabled(false);
        mRecipientSelector.setOnClickListener(this);
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
            case R.id.recipient_selector: {
                if (mAccountId <= 0) return;
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                intent.setClass(getActivity(), UserListSelectorActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_ID, mAccountId);
                startActivityForResult(intent, REQUEST_SELECT_USER);
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
                Utils.openUserProfile(getActivity(), recipient.account_id, recipient.id,
                        recipient.screen_name);
                break;
            }
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
        final View view = inflater.inflate(R.layout.fragment_messages_conversation, null);
        final FrameLayout listContainer = (FrameLayout) view.findViewById(R.id.list_container);
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        listContainer.addView(super.onCreateView(inflater, container, savedInstanceState), lp);
        final ViewGroup inputSendContainer = (ViewGroup) view.findViewById(R.id.input_send_container);
        final FragmentActivity activity = getActivity();
        final int themeRes = ThemeUtils.getThemeResource(activity);
        ViewAccessor.setBackground(inputSendContainer, ThemeUtils.getActionBarSplitBackground(activity, themeRes));
        final Context actionBarContext = ThemeUtils.getActionBarContext(activity);
        View.inflate(actionBarContext, R.layout.fragment_messages_conversation_input_send, inputSendContainer);
        return view;
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
        final Account account = (Account) mAccountSpinner.getSelectedItem();
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
        setListShown(true);
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

    @Override
    public void onRefreshFromEnd() {
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[2][];
                result[0] = getActivatedAccountIds(getActivity());
                result[1] = getNewestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (twitter == null) return;
                twitter.getReceivedDirectMessagesAsync(result[0], null, result[1]);
                twitter.getSentDirectMessagesAsync(result[0], null, null);
            }

        }.execute();
    }

    @Override
    public void onRefreshFromStart() {
        loadMoreMessages();
    }

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
        final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
        registerReceiver(mStatusReceiver, filter);
        updateTextCount();
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
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
        final View inputSendContainer = view.findViewById(R.id.input_send_container);
        mConversationContainer = view.findViewById(R.id.conversation_container);
        mRecipientSelectorContainer = view.findViewById(R.id.recipient_selector_container);
        mRecipientSelector = view.findViewById(R.id.recipient_selector);
        mAccountSpinner = (Spinner) view.findViewById(R.id.account_selector);
        mEditText = (EditText) inputSendContainer.findViewById(R.id.edit_text);
        mTextCountView = (StatusTextCountView) inputSendContainer.findViewById(R.id.text_count);
        mSendButton = inputSendContainer.findViewById(R.id.send);
        mAddImageButton = (ImageView) inputSendContainer.findViewById(R.id.add_image);
        mRecipientSelector = view.findViewById(R.id.recipient_selector);
    }

    @Override
    public boolean scrollToStart() {
        if (mAdapter == null || mAdapter.isEmpty()) return false;
        setSelection(mAdapter.getCount() - 1);
        return true;
    }

    public void showConversation(final long accountId, final long recipientId) {
        mAccountId = accountId;
        mRecipientId = recipientId;
        final Context context = getActivity();
        mSender = Account.getAccount(context, accountId);
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
        updateProfileImage();
    }

    @Override
    protected void onReachedTop() {
        if (!mLoadMoreAutomatically) return;
        loadMoreMessages();
    }

    protected void updateRefreshState() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null || !getUserVisibleHint()) return;
        final boolean refreshing = twitter.isReceivedDirectMessagesRefreshing()
                || twitter.isSentDirectMessagesRefreshing();
        setProgressBarIndeterminateVisibility(refreshing);
        setRefreshing(refreshing);
    }

    private void loadMoreMessages() {
        if (isRefreshing()) return;
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[3][];
                result[0] = getActivatedAccountIds(getActivity());
                result[1] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
                result[2] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (twitter == null) return;
                twitter.getReceivedDirectMessagesAsync(result[0], result[1], null);
                twitter.getSentDirectMessagesAsync(result[0], result[2], null);
            }

        }.execute();
    }

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
        final int color = ThemeUtils.getThemeColor(getActivity());
        if (mImageUri != null) {
            mAddImageButton.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            mAddImageButton.clearColorFilter();
        }
    }

    private void updateTextCount() {
        if (mTextCountView == null || mEditText == null) return;
        final int count = mValidator.getTweetLength(ParseUtils.parseString(mEditText.getText()));
        mTextCountView.setTextCount(count);
    }

}
