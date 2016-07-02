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

package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

public class MessageEntryViewHolder extends ViewHolder implements OnClickListener {

    public final ImageView profileImage;
    public final NameView nameView;
    public final TextView textView;
    public final ShortTimeView timeView;
    private final MessageEntriesAdapter adapter;
    private final IColorLabelView content;

    public MessageEntryViewHolder(final MessageEntriesAdapter adapter, final View itemView) {
        super(itemView);
        this.adapter = adapter;
        content = (IColorLabelView) itemView.findViewById(R.id.content);
        profileImage = (ImageView) itemView.findViewById(R.id.profileImage);
        nameView = (NameView) itemView.findViewById(R.id.name);
        textView = (TextView) itemView.findViewById(R.id.text);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        setTextSize(adapter.getTextSize());
        itemView.setOnClickListener(this);
        profileImage.setOnClickListener(this);
    }

    @UiThread
    public void displayMessage(Cursor cursor, boolean isUnread) {
        final Context context = adapter.getContext();
        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final UserColorNameManager manager = adapter.getUserColorNameManager();

        final UserKey accountKey = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_ACCOUNT_KEY));
        final UserKey conversationId = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_CONVERSATION_ID));
        final long timestamp = cursor.getLong(ConversationEntries.IDX_MESSAGE_TIMESTAMP);
        final boolean isOutgoing = cursor.getInt(ConversationEntries.IDX_IS_OUTGOING) == 1;

        final String name = cursor.getString(ConversationEntries.IDX_NAME);
        final String screenName = cursor.getString(ConversationEntries.IDX_SCREEN_NAME);

        nameView.setName(manager.getUserNickname(conversationId, name));
        nameView.setScreenName("@" + screenName);
        nameView.updateText(adapter.getBidiFormatter());
        textView.setText(toPlainText(cursor.getString(ConversationEntries.IDX_TEXT_UNESCAPED)));
        timeView.setTime(timestamp);
        if (isOutgoing) {
            timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_sent, 0);
        } else {
            timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        nameView.setTypeface(null, isUnread && !isOutgoing ? Typeface.BOLD : Typeface.NORMAL);
        textView.setTypeface(null, isUnread && !isOutgoing ? Typeface.BOLD : Typeface.NORMAL);
        if (adapter.shouldShowAccountsColor()) {
            // FIXME draw account color
        } else {
            content.drawEnd();
        }
        content.drawStart(manager.getUserColor(conversationId));

        final String profileImage = cursor.getString(ConversationEntries.IDX_PROFILE_IMAGE_URL);
        loader.displayProfileImage(this.profileImage, profileImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileImage: {
                adapter.onUserProfileClick(getLayoutPosition());
                break;
            }
            default: {
                if (v == itemView) {
                    adapter.onMessageClick(getLayoutPosition());
                }
                break;
            }
        }
    }

    public void setTextSize(final float textSize) {
        nameView.setPrimaryTextSize(textSize * 1.1f);
        nameView.setSecondaryTextSize(textSize);
        textView.setTextSize(textSize);
        timeView.setTextSize(textSize * 0.85f);
    }

}
