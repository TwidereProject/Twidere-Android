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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.twitter.Validator;

import org.mariotaku.twidere.Constants;
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
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.view.ComposeEditText;
import org.mariotaku.twidere.view.StatusTextCountView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import static org.mariotaku.twidere.util.Utils.isMyRetweet;

public class RetweetQuoteDialogFragment extends BaseSupportDialogFragment implements Constants {

    public static final String FRAGMENT_TAG = "retweet_quote";
    private PopupMenu mPopupMenu;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
        final Context context = builder.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.dialog_status_quote_retweet, null);
        final DummyItemAdapter adapter = new DummyItemAdapter(context);
        adapter.setShouldShowAccountsColor(true);
        final IStatusViewHolder holder = new StatusViewHolder(adapter, view.findViewById(R.id.item_content));
        final ParcelableStatus status = getStatus();
        assert status != null;
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(wrapped, status.account_key);
        assert credentials != null;

        builder.setView(view);
        builder.setTitle(R.string.retweet_quote_confirm_title);
        if (isMyRetweet(status)) {
            builder.setPositiveButton(R.string.cancel_retweet, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTwitterWrapper.cancelRetweetAsync(status.account_key, status.id, status.my_retweet_id);
                }
            });
        } else if (!status.user_is_protected) {
            builder.setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    retweetOrQuote(mTwitterWrapper, credentials, status);
                }
            });
        }
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
        builder.setNegativeButton(android.R.string.cancel, null);

        holder.displayStatus(status, false, true);


        final StatusTextCountView textCountView = (StatusTextCountView) view.findViewById(R.id.comment_text_count);

        textCountView.setMaxLength(TwidereValidator.getTextLimit(credentials));

        view.findViewById(R.id.item_menu).setVisibility(View.GONE);
        view.findViewById(R.id.action_buttons).setVisibility(View.GONE);
        view.findViewById(R.id.item_content).setFocusable(false);
        view.findViewById(R.id.comment_container).setVisibility(status.user_is_protected ? View.GONE : View.VISIBLE);
        final ComposeEditText editComment = (ComposeEditText) view.findViewById(R.id.edit_comment);
        editComment.setAccountKey(status.account_key);

        final boolean sendByEnter = mPreferences.getBoolean(KEY_QUICK_SEND);
        final EditTextEnterHandler enterHandler = EditTextEnterHandler.attach(editComment, new EditTextEnterHandler.EnterListener() {
            @Override
            public boolean shouldCallListener() {
                return true;
            }

            @Override
            public boolean onHitEnter() {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                final ParcelableStatus status = getStatus();
                if (twitter == null || status == null) return false;
                retweetOrQuote(twitter, credentials, status);
                dismiss();
                return true;
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
        final View commentMenu = view.findViewById(R.id.comment_menu);

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

        final Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                updateTextCount(dialog, editComment.getText(), status, credentials);
            }
        });
        return dialog;
    }

    private void updateTextCount(DialogInterface dialog, CharSequence s, ParcelableStatus status,
                                 ParcelableAccount account) {
        if (!(dialog instanceof AlertDialog)) return;
        final AlertDialog alertDialog = (AlertDialog) dialog;
        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton == null) return;
        if (s.length() > 0) {
            positiveButton.setText(R.string.comment);
        } else {
            positiveButton.setText(isMyRetweet(status) ? R.string.cancel_retweet : R.string.retweet);
        }
        final String statusLink = LinkCreator.getStatusWebLink(status).toString();
        final StatusTextCountView textCountView = (StatusTextCountView) alertDialog.findViewById(R.id.comment_text_count);
        assert textCountView != null;
        textCountView.setTextCount(mValidator.getTweetLength(s + " " + statusLink));
    }

    private ParcelableStatus getStatus() {
        final Bundle args = getArguments();
        if (!args.containsKey(EXTRA_STATUS)) return null;
        return args.getParcelable(EXTRA_STATUS);
    }

    private void retweetOrQuote(AsyncTwitterWrapper twitter, ParcelableAccount account, ParcelableStatus status) {
        final Dialog dialog = getDialog();
        if (dialog == null) return;
        final EditText editComment = ((EditText) dialog.findViewById(R.id.edit_comment));
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
                        commentText = getString(R.string.fanfou_repost_format, editingComment,
                                status.user_screen_name, status.text_plain);
                        update.repost_status_id = status.id;
                    } else {
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
                        statusLink = LinkCreator.getTwitterStatusLink(status.user_screen_name,
                                status.id);
                    } else {
                        statusLink = LinkCreator.getTwitterStatusLink(status.quoted_user_screen_name,
                                status.quoted_id);
                    }
                    commentText = editingComment + " " + statusLink;
                    break;
                }
            }
            update.text = commentText;
            update.is_possibly_sensitive = status.is_possibly_sensitive;
            BackgroundOperationService.updateStatusesAsync(getContext(), Draft.Action.QUOTE, update);
        } else {
            twitter.retweetStatusAsync(status.account_key, status.id);
        }
    }

    private boolean useQuote(boolean hasComment, ParcelableAccount account) {
        return hasComment || ParcelableAccount.Type.FANFOU.equals(account.account_type);
    }

    public static RetweetQuoteDialogFragment show(final FragmentManager fm, final ParcelableStatus status) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        final RetweetQuoteDialogFragment f = new RetweetQuoteDialogFragment();
        f.setArguments(args);
        f.show(fm, FRAGMENT_TAG);
        return f;
    }
}
