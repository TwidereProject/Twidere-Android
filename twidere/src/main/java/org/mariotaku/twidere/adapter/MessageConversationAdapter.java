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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IDirectMessagesAdapter;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableDirectMessageCursorIndices;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.DirectMessageOnLinkClickHandler;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.holder.IncomingMessageViewHolder;
import org.mariotaku.twidere.view.holder.MessageViewHolder;

import java.lang.ref.WeakReference;

public class MessageConversationAdapter extends BaseRecyclerViewAdapter<ViewHolder> implements
        Constants, IDirectMessagesAdapter {

    private static final int ITEM_VIEW_TYPE_MESSAGE_OUTGOING = 1;
    private static final int ITEM_VIEW_TYPE_MESSAGE_INCOMING = 2;
    private final int mOutgoingMessageColor;
    private final int mIncomingMessageColor;
    private final boolean mDisplayProfileImage;

    @ShapedImageView.ShapeStyle
    private final int mProfileImageStyle;
    private final int mMediaPreviewStyle;

    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mMediaLoadingHandler;
    private final int mTextSize;

    private Cursor mCursor;
    private ParcelableDirectMessageCursorIndices mIndices;
    private TwidereLinkify mLinkify;
    private CardMediaContainer.OnMediaClickListener mEventListener;

    public MessageConversationAdapter(final Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLinkify = new TwidereLinkify(new DirectMessageOnLinkClickHandler(context, null, preferences));
        mTextSize = preferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mDisplayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mMediaLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mIncomingMessageColor = ThemeUtils.getUserAccentColor(context);
        mOutgoingMessageColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context));
        mEventListener = new EventListener(this);
    }

    public MediaLoadingHandler getMediaLoadingHandler() {
        return mMediaLoadingHandler;
    }

    public TwidereLinkify getLinkify() {
        return mLinkify;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_MESSAGE_INCOMING: {
                final View view = mInflater.inflate(R.layout.card_item_message_conversation_incoming, parent, false);
                final MessageViewHolder holder = new IncomingMessageViewHolder(this, view);
                holder.setMessageColor(mIncomingMessageColor);
                holder.setTextSize(getTextSize());
                return holder;
            }
            case ITEM_VIEW_TYPE_MESSAGE_OUTGOING: {
                final View view = mInflater.inflate(R.layout.card_item_message_conversation_outgoing, parent, false);
                final MessageViewHolder holder = new MessageViewHolder(this, view);
                holder.setMessageColor(mOutgoingMessageColor);
                holder.setTextSize(getTextSize());
                return holder;
            }
        }
        throw new UnsupportedOperationException("Unknown viewType " + viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_MESSAGE_INCOMING:
            case ITEM_VIEW_TYPE_MESSAGE_OUTGOING: {
                final Cursor c = mCursor;
                c.moveToPosition(getCursorPosition(position));
                ((MessageViewHolder) holder).displayMessage(c, mIndices);
            }
        }
    }

    private int getCursorPosition(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        final Cursor c = mCursor;
        c.moveToPosition(position);
        if (c.getInt(mIndices.is_outgoing) == 1) {
            return ITEM_VIEW_TYPE_MESSAGE_OUTGOING;
        } else {
            return ITEM_VIEW_TYPE_MESSAGE_INCOMING;
        }
    }

    @Override
    public int getItemCount() {
        final Cursor c = mCursor;
        if (c == null) return 0;
        return c.getCount();
    }

    @Override
    public ParcelableDirectMessage findItem(final long id) {
        for (int i = 0, count = getItemCount(); i < count; i++) {
            if (getItemId(i) == id) return getDirectMessage(i);
        }
        return null;
    }

    @Override
    public final int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public boolean getProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    public ParcelableDirectMessage getDirectMessage(final int position) {
        final Cursor c = mCursor;
        if (c == null || c.isClosed()) return null;
        c.moveToPosition(position);
        final UserKey accountKey = UserKey.valueOf(c.getString(mIndices.account_key));
        final long messageId = c.getLong(mIndices.id);
        return Utils.findDirectMessageInDatabases(getContext(), accountKey, messageId);
    }

    @Override
    public final int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    public void setCursor(final Cursor cursor) {
        if (cursor != null) {
            mIndices = new ParcelableDirectMessageCursorIndices(cursor);
        } else {
            mIndices = null;
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public CardMediaContainer.OnMediaClickListener getOnMediaClickListener() {
        return mEventListener;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    static class EventListener implements CardMediaContainer.OnMediaClickListener {

        private final WeakReference<MessageConversationAdapter> adapterRef;

        public EventListener(MessageConversationAdapter adapter) {
            this.adapterRef = new WeakReference<>(adapter);
        }

        @Override
        public void onMediaClick(View view, ParcelableMedia media, UserKey accountKey, long extraId) {
            final MessageConversationAdapter adapter = adapterRef.get();
            IntentUtils.openMedia(adapter.getContext(), adapter.getDirectMessage((int) extraId), media,
                    null, adapter.preferences.getBoolean(KEY_NEW_DOCUMENT_API));
        }

    }
}
