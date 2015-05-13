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

package org.mariotaku.twidere.fragment.support;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METLengthChecker;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.DummyStatusHolderAdapter;

import static org.mariotaku.twidere.util.Utils.isMyRetweet;

public class RetweetQuoteDialogFragment extends BaseSupportDialogFragment implements
        DialogInterface.OnClickListener {

    public static final String FRAGMENT_TAG = "retweet_quote";
    private MaterialEditText mEditComment;
    private PopupMenu mPopupMenu;
    private View mCommentMenu;
    private TwidereValidator mValidator;

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final ParcelableStatus status = getStatus();
        if (status == null) return;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (twitter == null) return;
                if (mEditComment.length() > 0) {
                    final Menu menu = mPopupMenu.getMenu();
                    final MenuItem quoteOriginalStatus = menu.findItem(R.id.quote_original_status);
                    final MenuItem linkToQuotedStatus = menu.findItem(R.id.link_to_quoted_status);
                    final Uri statusLink;
                    final long inReplyToStatusId;
                    if (!status.is_quote) {
                        inReplyToStatusId = status.id;
                        statusLink = LinkCreator.getTwitterStatusLink(status.quoted_by_user_screen_name, status.id);
                    } else if (quoteOriginalStatus.isChecked()) {
                        inReplyToStatusId = status.quote_id;
                        statusLink = LinkCreator.getTwitterStatusLink(status.user_screen_name, status.quote_id);
                    } else {
                        inReplyToStatusId = status.id;
                        statusLink = LinkCreator.getTwitterStatusLink(status.quoted_by_user_screen_name, status.id);
                    }
                    final String commentText = mEditComment.getText() + " " + statusLink;
                    twitter.updateStatusAsync(new long[]{status.account_id}, commentText, null, null,
                            linkToQuotedStatus.isChecked() ? inReplyToStatusId : -1, status.is_possibly_sensitive);
                } else if (isMyRetweet(status)) {
                    twitter.cancelRetweetAsync(status.account_id, status.id, status.my_retweet_id);
                } else {
                    twitter.retweetStatusAsync(status.account_id, status.id);
                }
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                final Intent intent = new Intent(INTENT_ACTION_QUOTE);
                intent.putExtra(EXTRA_STATUS, status);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
        final Context context = builder.getContext();
        mValidator = new TwidereValidator(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.dialog_status_quote_retweet, null);
        final StatusViewHolder holder = new StatusViewHolder(new DummyStatusHolderAdapter(context), view.findViewById(R.id.item_content));
        final ParcelableStatus status = getStatus();

        assert status != null;

        builder.setView(view);
        builder.setTitle(R.string.retweet_quote_confirm_title);
        builder.setPositiveButton(isMyRetweet(status) ? R.string.cancel_retweet : R.string.retweet, this);
        builder.setNeutralButton(R.string.quote, this);
        builder.setNegativeButton(android.R.string.cancel, null);

        holder.displayStatus(status, null, false, true);

        view.findViewById(R.id.item_menu).setVisibility(View.GONE);
        view.findViewById(R.id.action_buttons).setVisibility(View.GONE);
        view.findViewById(R.id.item_content).setFocusable(false);
        mEditComment = (MaterialEditText) view.findViewById(R.id.edit_comment);
        mEditComment.setLengthChecker(new METLengthChecker() {

            final String statusLink = LinkCreator.getTwitterStatusLink(status.user_screen_name, status.quote_id).toString();

            @Override
            public int getLength(CharSequence text) {
                return mValidator.getTweetLength(text + " " + statusLink);
            }
        });
        mEditComment.setMaxCharacters(mValidator.getMaxTweetLength());
        mCommentMenu = view.findViewById(R.id.comment_menu);

        mPopupMenu = new PopupMenu(context, mCommentMenu, Gravity.NO_GRAVITY,
                R.attr.actionOverflowMenuStyle, 0);
        mCommentMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
        mCommentMenu.setOnTouchListener(mPopupMenu.getDragToOpenListener());
        mPopupMenu.inflate(R.menu.menu_dialog_comment);
        final Menu menu = mPopupMenu.getMenu();
        MenuUtils.setMenuItemAvailability(menu, R.id.quote_original_status,
                status.retweet_id > 0 || status.quote_id > 0);
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

        return builder.create();
    }

    private ParcelableStatus getStatus() {
        final Bundle args = getArguments();
        if (!args.containsKey(EXTRA_STATUS)) return null;
        return args.getParcelable(EXTRA_STATUS);
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
