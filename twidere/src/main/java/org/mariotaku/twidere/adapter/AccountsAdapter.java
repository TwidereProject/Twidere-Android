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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccount.Indices;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.AccountViewHolder;

public class AccountsAdapter extends SimpleDragSortCursorAdapter implements Constants, IBaseAdapter {

    private final ImageLoaderWrapper mImageLoader;
    private final SharedPreferences mPreferences;

    private long mDefaultAccountId;

    private boolean mDisplayProfileImage;
    private int mChoiceMode;
    private boolean mSortEnabled;
    private Indices mIndices;

    public AccountsAdapter(final Context context) {
        super(context, R.layout.list_item_account, null, new String[]{Accounts.NAME},
                new int[]{android.R.id.text1}, 0);
        final TwidereApplication application = TwidereApplication.getInstance(context);
        mImageLoader = application.getImageLoaderWrapper();
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public ParcelableAccount getAccount(int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return null;
        return new ParcelableAccount(c, mIndices);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int color = cursor.getInt(mIndices.color);
        final AccountViewHolder holder = (AccountViewHolder) view.getTag();
        holder.screen_name.setText("@" + cursor.getString(mIndices.screen_name));
        holder.setAccountColor(color);
        holder.setIsDefault(mDefaultAccountId != -1 && mDefaultAccountId == cursor.getLong(mIndices.account_id));
        if (mDisplayProfileImage) {
            mImageLoader.displayProfileImage(holder.profile_image, cursor.getString(mIndices.profile_image_url));
        } else {
            mImageLoader.cancelDisplayTask(holder.profile_image);
            holder.profile_image.setImageResource(R.drawable.ic_profile_image_default);
        }
        final boolean isMultipleChoice = mChoiceMode == ListView.CHOICE_MODE_MULTIPLE
                || mChoiceMode == ListView.CHOICE_MODE_MULTIPLE_MODAL;
        holder.checkbox.setVisibility(isMultipleChoice ? View.VISIBLE : View.GONE);
        holder.setSortEnabled(mSortEnabled);
        super.bindView(view, context, cursor);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        final AccountViewHolder holder = new AccountViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public ImageLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public int getLinkHighlightOption() {
        return 0;
    }

    @Override
    public float getTextSize() {
        return 0;
    }

    @Override
    public boolean isDisplayNameFirst() {
        return false;
    }

    @Override
    public boolean isDisplayProfileImage() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean isNicknameOnly() {
        return false;
    }

    @Override
    public boolean isShowAccountColor() {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        mDefaultAccountId = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
        super.notifyDataSetChanged();
    }

    @Override
    public void setDisplayNameFirst(boolean nameFirst) {

    }

    public void setChoiceMode(final int mode) {
        if (mChoiceMode == mode) return;
        mChoiceMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public void setDisplayProfileImage(final boolean display) {
        mDisplayProfileImage = display;
        notifyDataSetChanged();
    }

    @Override
    public void setLinkHighlightOption(String option) {

    }

    @Override
    public void setNicknameOnly(boolean nicknameOnly) {

    }

    @Override
    public void setShowAccountColor(boolean show) {

    }

    @Override
    public void setTextSize(float textSize) {

    }

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        if (cursor != null) {
            mIndices = new Indices(cursor);
        }
        return super.swapCursor(cursor);
    }

    public void setSortEnabled(boolean sortEnabled) {
        if (mSortEnabled == sortEnabled) return;
        mSortEnabled = sortEnabled;
        notifyDataSetChanged();
    }
}
