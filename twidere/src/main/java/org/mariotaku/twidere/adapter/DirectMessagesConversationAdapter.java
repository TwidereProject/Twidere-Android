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
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IDirectMessagesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.DirectMessageConversationViewHolder;

import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.findDirectMessageInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.openImage;

public class DirectMessagesConversationAdapter extends BaseCursorAdapter implements IDirectMessagesAdapter,
        OnClickListener {
    private ScaleType mImagePreviewScaleType;

    private final ImageLoaderWrapper mImageLoader;
    private final Context mContext;
    private final MultiSelectManager mMultiSelectManager;
    private MenuButtonClickListener mListener;
    private final ImageLoadingHandler mImageLoadingHandler;

    private boolean mAnimationEnabled = true;

    private int mMaxAnimationPosition;
    private ParcelableDirectMessage.CursorIndices mIndices;

    public DirectMessagesConversationAdapter(final Context context) {
        super(context, R.layout.card_item_message_conversation, null, new String[0], new int[0], 0);
        mContext = context;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mMultiSelectManager = app.getMultiSelectManager();
        mImageLoader = app.getImageLoaderWrapper();
        mImageLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
        configBaseCardAdapter(context, this);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int position = cursor.getPosition();
        final DirectMessageConversationViewHolder holder = (DirectMessageConversationViewHolder) view.getTag();

        final String firstMedia = cursor.getString(mIndices.first_media);

        final long accountId = cursor.getLong(mIndices.account_id);
        final long timestamp = cursor.getLong(mIndices.message_timestamp);
        final boolean is_outgoing = cursor.getInt(mIndices.is_outgoing) == 1;

        holder.setOutgoing(is_outgoing);
        holder.setTextSize(getTextSize());
        holder.text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
        getLinkify().applyAllLinks(holder.text, accountId, false);
        holder.text.setMovementMethod(null);
        holder.time.setText(formatToLongTimeString(mContext, timestamp));
        if (position > mMaxAnimationPosition) {
            if (mAnimationEnabled) {
                view.startAnimation(holder.item_animation);
            }
            mMaxAnimationPosition = position;
        }

        if (firstMedia == null) {
            mImageLoader.cancelDisplayTask(holder.media_preview);
            holder.media_preview_container.setVisibility(View.GONE);
        } else {
            mImageLoader.cancelDisplayTask(holder.media_preview);
            holder.media_preview_container.setVisibility(View.VISIBLE);
            if (mImagePreviewScaleType != null) {
                holder.media_preview.setScaleType(mImagePreviewScaleType);
            }
            if (!firstMedia.equals(mImageLoadingHandler.getLoadingUri(holder.media_preview))) {
                holder.media_preview.setBackgroundResource(0);
                mImageLoader.displayPreviewImageWithCredentials(holder.media_preview, firstMedia,
                        accountId, mImageLoadingHandler);
            }
            holder.media_preview.setTag(position);
        }
        super.bindView(view, context, cursor);
    }

    @Override
    public ParcelableDirectMessage findItem(final long id) {
        for (int i = 0, count = getCount(); i < count; i++) {
            if (getItemId(i) == id) return getDirectMessage(i);
        }
        return null;
    }

    public ParcelableDirectMessage getDirectMessage(final int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed()) return null;
        c.moveToPosition(position);
        final long account_id = c.getLong(mIndices.account_id);
        final long message_id = c.getLong(mIndices.message_id);
        return findDirectMessageInDatabases(mContext, account_id, message_id);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        final Object tag = view.getTag();
        if (!(tag instanceof DirectMessageConversationViewHolder)) {
            final DirectMessageConversationViewHolder holder = new DirectMessageConversationViewHolder(view);
            holder.media_preview.setOnClickListener(this);
            view.setTag(holder);
        }
        return view;
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
                if (message == null || message.first_media == null) return;
                openImage(mContext, message.account_id, message.first_media, false);
            }
        }
    }

    @Override
    public void setAnimationEnabled(final boolean anim) {
        mAnimationEnabled = anim;
    }

    @Override
    public void setDisplayImagePreview(final boolean display) {
        // Images in DM are always enabled
    }

    @Override
    public void setImagePreviewScaleType(final String scaleTypeString) {
        final ScaleType scaleType = ScaleType.valueOf(scaleTypeString.toUpperCase(Locale.US));
        mImagePreviewScaleType = scaleType;
    }

    @Override
    public void setMaxAnimationPosition(final int position) {
        mMaxAnimationPosition = position;
    }

    @Override
    public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
        mListener = listener;
    }

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        if (cursor != null) {
            mIndices = new ParcelableDirectMessage.CursorIndices(cursor);
        } else {
            mIndices = null;
        }
        return super.swapCursor(cursor);
    }
}
