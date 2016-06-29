/*
 * Twidere - Twitter client for Android
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.twitter.Validator;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DummyItemAdapter;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.service.BackgroundOperationService;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.EditTextEnterHandler;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.TwidereBugReporter;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.view.ComposeEditText;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import static org.mariotaku.twidere.util.Utils.isMyRetweet;

public class RetweetQuoteDialogFragment extends BaseDialogFragment {

    public static final String FRAGMENT_TAG = "retweet_quote";
    private static final boolean SHOW_PROTECTED_CONFIRM = Boolean.parseBoolean("false");
    private PopupMenu mPopupMenu;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final Context context = builder.getContext();
        final ParcelableStatus status = getStatus();
        assert status != null;
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(getContext(),
                status.account_key);
        assert credentials != null;

        builder.setView(R.layout.dialog_status_quote_retweet);
        builder.setTitle(R.string.retweet_quote_confirm_title);
        builder.setPositiveButton(R.string.retweet, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.quote, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(INTENT_ACTION_QUOTE);
                final Menu menu = mPopupMenu.getMenu();
                final MenuItem quoteOriginalStatus = menu.findItem(R.id.quote_original_status);
                intent.putExtra(EXTRA_STATUS, status);
                intent.putExtra(EXTRA_QUOTE_ORIGINAL_STATUS, quoteOriginalStatus.isChecked());
                startActivity(intent);
            }
        });

        final Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final AlertDialog dialog = (AlertDialog) dialogInterface;


                final View itemContent = dialog.findViewById(R.id.item_content);
                final StatusTextCountView textCountView = (StatusTextCountView) dialog.findViewById(R.id.comment_text_count);
                final View itemMenu = dialog.findViewById(R.id.itemMenu);
                final View actionButtons = dialog.findViewById(R.id.actionButtons);
                final View commentContainer = dialog.findViewById(R.id.comment_container);
                final ComposeEditText editComment = (ComposeEditText) dialog.findViewById(R.id.edit_comment);
                final View commentMenu = dialog.findViewById(R.id.comment_menu);
                assert itemContent != null && textCountView != null && itemMenu != null
                        && actionButtons != null && commentContainer != null && editComment != null
                        && commentMenu != null;

                final DummyItemAdapter adapter = new DummyItemAdapter(context);
                adapter.setShouldShowAccountsColor(true);
                final IStatusViewHolder holder = new StatusViewHolder(adapter, itemContent);
                holder.displayStatus(status, false, true);

                textCountView.setMaxLength(TwidereValidator.getTextLimit(credentials));

                itemMenu.setVisibility(View.GONE);
                actionButtons.setVisibility(View.GONE);
                itemContent.setFocusable(false);
                final boolean useQuote = useQuote(!status.user_is_protected, credentials);

                commentContainer.setVisibility(useQuote ? View.VISIBLE : View.GONE);
                editComment.setAccountKey(status.account_key);

                final boolean sendByEnter = mPreferences.getBoolean(KEY_QUICK_SEND);
                final EditTextEnterHandler enterHandler = EditTextEnterHandler.attach(editComment, new EditTextEnterHandler.EnterListener() {
                    @Override
                    public boolean shouldCallListener() {
                        return true;
                    }

                    @Override
                    public boolean onHitEnter() {
                        final ParcelableStatus status = getStatus();
                        if (status == null) return false;
                        if (retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM)) {
                            dismiss();
                            return true;
                        }
                        return false;
                    }
                }, sendByEnter);
                enterHandler.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateTextCount(getDialog(), s, status, credentials);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                mPopupMenu = new PopupMenu(context, commentMenu, Gravity.NO_GRAVITY,
                        R.attr.actionOverflowMenuStyle, 0);
                commentMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupMenu.show();
                    }
                });
                commentMenu.setOnTouchListener(mPopupMenu.getDragToOpenListener());
                mPopupMenu.inflate(R.menu.menu_dialog_comment);
                final Menu menu = mPopupMenu.getMenu();
                MenuUtils.setMenuItemAvailability(menu, R.id.quote_original_status,
                        status.retweet_id != null || status.quoted_id != null);
                mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.isCheckable()) {
                            item.setChecked(!item.isChecked());
                            return true;
                        }
                        return false;
                    }
                });

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean dismissDialog = false;
                        if (editComment.length() > 0) {
                            dismissDialog = retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM);
                        } else if (isMyRetweet(status)) {
                            mTwitterWrapper.cancelRetweetAsync(status.account_key, status.id, status.my_retweet_id);
                            dismissDialog = true;
                        } else if (useQuote(!status.user_is_protected, credentials)) {
                            dismissDialog = retweetOrQuote(credentials, status, SHOW_PROTECTED_CONFIRM);
                        } else {
                            TwidereBugReporter.logException(new IllegalStateException(status.toString()));
                        }
                        if (dismissDialog) {
                            dismiss();
                        }
                    }
                });

                updateTextCount(dialog, editComment.getText(), status, credentials);
            }
        });
        return dialog;
    }

    private void updateTextCount(DialogInterface dialog, CharSequence s, ParcelableStatus status, ParcelableCredentials credentials) {
        if (!(dialog instanceof AlertDialog)) return;
        final AlertDialog alertDialog = (AlertDialog) dialog;
        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton == null) return;
        if (s.length() > 0) {
            positiveButton.setText(R.string.comment);
            positiveButton.setEnabled(true);
        } else if (isMyRetweet(status)) {
            positiveButton.setText(R.string.cancel_retweet);
            positiveButton.setEnabled(true);
        } else if (useQuote(false, credentials)) {
            positiveButton.setText(R.string.retweet);
            positiveButton.setEnabled(true);
        } else {
            positiveButton.setText(R.string.retweet);
            positiveButton.setEnabled(!status.user_is_protected);
        }
        final StatusTextCountView textCountView = (StatusTextCountView) alertDialog.findViewById(R.id.comment_text_count);
        assert textCountView != null;
        textCountView.setTextCount(mValidator.getTweetLength(s.toString()));
    }

    private ParcelableStatus getStatus() {
        final Bundle args = getArguments();
        if (!args.containsKey(EXTRA_STATUS)) return null;
        return args.getParcelable(EXTRA_STATUS);
    }

    @CheckResult
    private boolean retweetOrQuote(ParcelableAccount account, ParcelableStatus status,
                                   boolean showProtectedConfirmation) {
        AsyncTwitterWrapper twitter = mTwitterWrapper;
        final Dialog dialog = getDialog();
        if (dialog == null || twitter == null) return false;
        final EditText editComment = (EditText) dialog.findViewById(R.id.edit_comment);
        if (useQuote(editComment.length() > 0, account)) {
            final Menu menu = mPopupMenu.getMenu();
            final MenuItem itemQuoteOriginalStatus = menu.findItem(R.id.quote_original_status);
            final Uri statusLink;
            final boolean quoteOriginalStatus = itemQuoteOriginalStatus.isChecked();

            String commentText;
            final ParcelableStatusUpdate update = new ParcelableStatusUpdate();
            update.accounts = new ParcelableAccount[]{account};
            final String editingComment = String.valueOf(editComment.getText());
            switch (ParcelableAccountUtils.getAccountType(account)) {
                case ParcelableAccount.Type.FANFOU: {
                    if (!status.is_quote || !quoteOriginalStatus) {
                        if (status.user_is_protected && showProtectedConfirmation) {
                            QuoteProtectedStatusWarnFragment.show(this, account, status);
                            return false;
                        }
                        update.repost_status_id = status.id;
                        commentText = getString(R.string.fanfou_repost_format, editingComment,
                                status.user_screen_name, status.text_plain);
                    } else {
                        if (status.quoted_user_is_protected && showProtectedConfirmation) {
                            return false;
                        }
                        commentText = getString(R.string.fanfou_repost_format, editingComment,
                                status.quoted_user_screen_name, status.quoted_text_plain);
                        update.repost_status_id = status.quoted_id;
                    }
                    if (commentText.length() > Validator.MAX_TWEET_LENGTH) {
                        commentText = commentText.substring(0, Math.max(Validator.MAX_TWEET_LENGTH,
                                editingComment.length()));
                    }
                    break;
                }
                default: {
                    if (!status.is_quote || !quoteOriginalStatus) {
                        statusLink = LinkCreator.getStatusWebLink(status);
                    } else {
                        statusLink = LinkCreator.getQuotedStatusWebLink(status);
                    }
                    update.attachment_url = statusLink.toString();
                    commentText = editingComment;
                    break;
                }
            }
            update.text = commentText;
            update.is_possibly_sensitive = status.is_possibly_sensitive;
            BackgroundOperationService.updateStatusesAsync(getContext(), Draft.Action.QUOTE, update);
        } else {
            twitter.retweetStatusAsync(status.account_key, status.id);
        }
        return true;
    }

    private boolean useQuote(boolean preCondition, ParcelableAccount account) {
        return preCondition || ParcelableAccount.Type.FANFOU.equals(account.account_type);
    }

    public static RetweetQuoteDialogFragment show(final FragmentManager fm, final ParcelableStatus status) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        final RetweetQuoteDialogFragment f = new RetweetQuoteDialogFragment();
        f.setArguments(args);
        f.show(fm, FRAGMENT_TAG);
        return f;
    }


    public static class QuoteProtectedStatusWarnFragment extends BaseDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final RetweetQuoteDialogFragment fragment = (RetweetQuoteDialogFragment) getParentFragment();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Bundle args = getArguments();
                    ParcelableAccount account = args.getParcelable(EXTRA_ACCOUNT);
                    ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
                    if (fragment.retweetOrQuote(account, status, false)) {
                        fragment.dismiss();
                    }
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.quote_protected_status_warning_message);
            builder.setPositiveButton(R.string.send_anyway, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        public static QuoteProtectedStatusWarnFragment show(RetweetQuoteDialogFragment pf,
                                                            ParcelableAccount account,
                                                            ParcelableStatus status) {
            QuoteProtectedStatusWarnFragment f = new QuoteProtectedStatusWarnFragment();
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_ACCOUNT, account);
            args.putParcelable(EXTRA_STATUS, status);
            f.setArguments(args);
            f.show(pf.getChildFragmentManager(), "quote_protected_status_warning");
            return f;
        }
    }
}
