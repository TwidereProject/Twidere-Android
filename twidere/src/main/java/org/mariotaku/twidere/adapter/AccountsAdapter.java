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
import android.widget.CompoundButton;

import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccount.Indices;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;
import org.mariotaku.twidere.view.holder.AccountViewHolder;

import javax.inject.Inject;

public class AccountsAdapter extends SimpleDragSortCursorAdapter implements Constants, IBaseAdapter {

    @Inject
    MediaLoaderWrapper mImageLoader;
    private final SharedPreferences mPreferences;

    private boolean mDisplayProfileImage;
    private boolean mSortEnabled;
    private Indices mIndices;
    private boolean mSwitchEnabled;
    private OnAccountToggleListener mOnAccountToggleListener;

    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final Object tag = buttonView.getTag();
            if (!(tag instanceof Long) || mOnAccountToggleListener == null) return;
            final long accountId = (Long) tag;
            mOnAccountToggleListener.onAccountToggle(accountId, isChecked);
        }
    };

    public AccountsAdapter(final Context context) {
        super(context, R.layout.list_item_account, null, new String[]{Accounts.NAME},
                new int[]{android.R.id.text1}, 0);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(context)).build().inject(this);
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
        holder.screenName.setText("@" + cursor.getString(mIndices.screen_name));
        holder.setAccountColor(color);
        if (mDisplayProfileImage) {
            mImageLoader.displayProfileImage(holder.profileImage, cursor.getString(mIndices.profile_image_url));
        } else {
            mImageLoader.cancelDisplayTask(holder.profileImage);
        }
        holder.toggle.setChecked(cursor.getShort(mIndices.is_activated) == 1);
        holder.toggle.setOnCheckedChangeListener(mCheckedChangeListener);
        holder.toggle.setTag(cursor.getLong(mIndices.account_id));
        holder.toggleContainer.setVisibility(mSwitchEnabled ? View.VISIBLE : View.GONE);
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
    public MediaLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public int getLinkHighlightOption() {
        return 0;
    }

    @Override
    public void setLinkHighlightOption(String option) {

    }

    @Override
    public float getTextSize() {
        return 0;
    }

    @Override
    public void setTextSize(float textSize) {

    }

    @Override
    public boolean isDisplayNameFirst() {
        return false;
    }

    @Override
    public void setDisplayNameFirst(boolean nameFirst) {

    }

    @Override
    public boolean isProfileImageDisplayed() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean isShowAccountColor() {
        return false;
    }

    @Override
    public void setShowAccountColor(boolean show) {

    }

    public void setSwitchEnabled(final boolean enabled) {
        if (mSwitchEnabled == enabled) return;
        mSwitchEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public void setDisplayProfileImage(final boolean display) {
        mDisplayProfileImage = display;
        notifyDataSetChanged();
    }

    public void setOnAccountToggleListener(OnAccountToggleListener listener) {
        mOnAccountToggleListener = listener;
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

    public interface OnAccountToggleListener {
        void onAccountToggle(long accountId, boolean state);
    }
}
