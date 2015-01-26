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
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IDirectMessagesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableDirectMessage.CursorIndices;
import org.mariotaku.twidere.util.DirectMessageOnLinkClickHandler;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.MessageConversationViewHolder;

import static org.mariotaku.twidere.util.Utils.findDirectMessageInDatabases;
import static org.mariotaku.twidere.util.Utils.openMedia;

public class MessageConversationAdapter extends Adapter<ViewHolder>
        implements Constants, IDirectMessagesAdapter, OnClickListener {

    private static final int ITEM_VIEW_TYPE_MESSAGE_OUTGOING = 1;
    private static final int ITEM_VIEW_TYPE_MESSAGE_INCOMING = 2;
    private final int mOutgoingMessageColor;
    private final int mIncomingMessageColor;

    private ScaleType mImagePreviewScaleType;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ImageLoaderWrapper mImageLoader;
    private final MultiSelectManager mMultiSelectManager;
    private final ImageLoadingHandler mImageLoadingHandler;

    private Cursor mCursor;
    private CursorIndices mIndices;
    private TwidereLinkify mLinkify;

    public MessageConversationAdapter(final Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mLinkify = new TwidereLinkify(new DirectMessageOnLinkClickHandler(context, null));
        mMultiSelectManager = app.getMultiSelectManager();
        mImageLoader = app.getImageLoaderWrapper();
        mImageLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
        mIncomingMessageColor = ThemeUtils.getUserAccentColor(context);
        mOutgoingMessageColor = ThemeUtils.getCardBackgroundColor(context);
    }


    public Context getContext() {
        return mContext;
    }

    public ImageLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    public TwidereLinkify getLinkify() {
        return mLinkify;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_MESSAGE_INCOMING: {
                final View view = mInflater.inflate(R.layout.card_item_message_conversation_incoming, parent, false);
                final MessageConversationViewHolder holder = new MessageConversationViewHolder(this, view);
                holder.setMessageColor(mIncomingMessageColor);
                return holder;
            }
            case ITEM_VIEW_TYPE_MESSAGE_OUTGOING: {
                final View view = mInflater.inflate(R.layout.card_item_message_conversation_outgoing, parent, false);
                final MessageConversationViewHolder holder = new MessageConversationViewHolder(this, view);
                holder.setMessageColor(mOutgoingMessageColor);
                return holder;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_VIEW_TYPE_MESSAGE_INCOMING:
            case ITEM_VIEW_TYPE_MESSAGE_OUTGOING: {
                final Cursor c = mCursor;
                c.moveToPosition(getCursorPosition(position));
                ((MessageConversationViewHolder) holder).displayMessage(c, mIndices);
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

    public ParcelableDirectMessage getDirectMessage(final int position) {
        final Cursor c = mCursor;
        if (c == null || c.isClosed()) return null;
        c.moveToPosition(position);
        final long account_id = c.getLong(mIndices.account_id);
        final long message_id = c.getLong(mIndices.message_id);
        return findDirectMessageInDatabases(mContext, account_id, message_id);
    }

    @Override
    public void onClick(final View view) {
        if (mMultiSelectManager.isActive()) return;
        final Object tag = view.getTag();
        final int position = tag instanceof Integer ? (Integer) tag : -1;
        if (position == -1) return;
        switch (view.getId()) {
            case R.id.media_preview: {
                final ParcelableDirectMessage message = getDirectMessage(position);
                if (message == null || message.media == null) return;
                openMedia(mContext, message.account_id, false, null, message.media);
            }
        }
    }

    @Override
    public void setDisplayImagePreview(final boolean display) {
        // Images in DM are always enabled
    }

    @Override
    public void setImagePreviewScaleType(final String scaleTypeString) {
        final ScaleType scaleType;
        switch (Utils.getMediaPreviewStyle(scaleTypeString)) {
            case VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP: {
                scaleType = ScaleType.CENTER_CROP;
                break;
            }
            case VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE: {
                scaleType = ScaleType.CENTER_INSIDE;
                break;
            }
            default: {
                return;
            }
        }
        mImagePreviewScaleType = scaleType;
    }

    public void setCursor(final Cursor cursor) {
        if (cursor != null) {
            mIndices = new CursorIndices(cursor);
        } else {
            mIndices = null;
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
