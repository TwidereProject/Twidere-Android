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
import android.widget.SimpleCursorAdapter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.holder.AccountViewHolder;

public class AccountsAdapter extends SimpleCursorAdapter implements Constants {

	private final ImageLoaderWrapper mImageLoader;
	private final SharedPreferences mPreferences;

	private int mUserColorIdx, mProfileImageIdx, mScreenNameIdx, mAccountIdIdx;
	private long mDefaultAccountId;

	private boolean mDisplayProfileImage;
	private int mChoiceMode;

	public AccountsAdapter(final Context context) {
		super(context, R.layout.list_item_account, null, new String[] { Accounts.NAME },
				new int[] { android.R.id.text1 }, 0);
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mImageLoader = application.getImageLoaderWrapper();
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int color = cursor.getInt(mUserColorIdx);
		final AccountViewHolder holder = (AccountViewHolder) view.getTag();
		holder.screen_name.setText("@" + cursor.getString(mScreenNameIdx));
		holder.setAccountColor(color);
		holder.setIsDefault(mDefaultAccountId != -1 && mDefaultAccountId == cursor.getLong(mAccountIdIdx));
		if (mDisplayProfileImage) {
			mImageLoader.displayProfileImage(holder.profile_image, cursor.getString(mProfileImageIdx));
		} else {
			holder.profile_image.setImageResource(R.drawable.ic_profile_image_default);
		}
		final boolean isMultipleChoice = mChoiceMode == ListView.CHOICE_MODE_MULTIPLE
				|| mChoiceMode == ListView.CHOICE_MODE_MULTIPLE_MODAL;
		holder.checkbox.setVisibility(isMultipleChoice ? View.VISIBLE : View.GONE);
		super.bindView(view, context, cursor);
	}

	@Override
	public long getItemId(final int position) {
		final Cursor c = getCursor();
		if (c == null || c.isClosed()) return -1;
		c.moveToPosition(position);
		return c.getLong(mAccountIdIdx);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {

		final View view = super.newView(context, cursor, parent);
		final AccountViewHolder viewholder = new AccountViewHolder(view);
		view.setTag(viewholder);
		return view;
	}

	@Override
	public void notifyDataSetChanged() {
		mDefaultAccountId = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
		super.notifyDataSetChanged();
	}

	public void setChoiceMode(final int mode) {
		if (mChoiceMode == mode) return;
		mChoiceMode = mode;
		notifyDataSetChanged();
	}

	public void setDisplayProfileImage(final boolean display) {
		mDisplayProfileImage = display;
		notifyDataSetChanged();
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mAccountIdIdx = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
			mUserColorIdx = cursor.getColumnIndex(Accounts.COLOR);
			mProfileImageIdx = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndex(Accounts.SCREEN_NAME);
		}
		return super.swapCursor(cursor);
	}
}
