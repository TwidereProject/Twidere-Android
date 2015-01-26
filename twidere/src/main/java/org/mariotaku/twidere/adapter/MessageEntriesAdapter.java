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

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.view.holder.MessageEntryViewHolder;

import static org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME;
import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NICKNAME_ONLY;

public class MessageEntriesAdapter extends Adapter<ViewHolder> implements OnClickListener {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ImageLoaderWrapper mImageLoader;
    private final MultiSelectManager mMultiSelectManager;
    private final boolean mNicknameOnly;
    private Cursor mCursor;
    private MessageEntriesAdapterListener mListener;

    public Context getContext() {
        return mContext;
    }

    public ImageLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    public boolean isNicknameOnly() {
        return mNicknameOnly;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.list_item_message_entry, parent, false);
        return new MessageEntryViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Cursor c = mCursor;
        c.moveToPosition(position);
        ((MessageEntryViewHolder) holder).displayMessage(c);
    }

    public void onMessageClick(int position) {
        if (mListener == null) return;
        mListener.onEntryClick(position, getEntry(position));
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        final Cursor c = mCursor;
        if (c == null) return 0;
        return c.getCount();
    }

    public MessageEntriesAdapter(final Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mMultiSelectManager = app.getMultiSelectManager();
        mImageLoader = app.getImageLoaderWrapper();
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNicknameOnly = prefs.getBoolean(KEY_NICKNAME_ONLY, false);
    }

    public static class DirectMessageEntry {

        public final long account_id, conversation_id;
        public final String screen_name, name;

        DirectMessageEntry(Cursor cursor) {
            account_id = cursor.getLong(ConversationEntries.IDX_ACCOUNT_ID);
            conversation_id = cursor.getLong(ConversationEntries.IDX_CONVERSATION_ID);
            screen_name = cursor.getString(ConversationEntries.IDX_SCREEN_NAME);
            name = cursor.getString(ConversationEntries.IDX_NAME);
        }

    }

    public DirectMessageEntry getEntry(final int position) {
        final Cursor c = mCursor;
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return null;
        return new DirectMessageEntry(c);
    }

    @Override
    public void onClick(final View view) {
//        if (mMultiSelectManager.isActive()) return;
//        final Object tag = view.getTag();
//        final int position = tag instanceof Integer ? (Integer) tag : -1;
//        if (position == -1) return;
//        switch (view.getId()) {
//            case R.id.profile_image: {
//                if (mContext instanceof Activity) {
//                    final long account_id = getAccountId(position);
//                    final long user_id = getConversationId(position);
//                    final String screen_name = getScreenName(position);
//                    openUserProfile(mContext, account_id, user_id, screen_name, null);
//                }
//                break;
//            }
//        }
    }

    public void setListener(MessageEntriesAdapterListener listener) {
        mListener = listener;
    }

    public interface MessageEntriesAdapterListener {
        public void onEntryClick(int position, DirectMessageEntry entry);
    }

}
