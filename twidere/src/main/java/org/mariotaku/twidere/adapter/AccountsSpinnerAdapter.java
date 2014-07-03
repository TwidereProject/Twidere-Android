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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.DirectMessagesConversationFragment;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

import java.util.Collection;

public class AccountsSpinnerAdapter extends ArrayAdapter<Account> {

	private final ImageLoaderWrapper mImageLoader;
	private final boolean mDisplayProfileImage;

	public AccountsSpinnerAdapter(final Context context) {
		super(context, R.layout.list_item_two_line_small);
		setDropDownViewResource(R.layout.list_item_two_line_small);
		mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
		mDisplayProfileImage = context.getSharedPreferences(DirectMessagesConversationFragment.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE).getBoolean(DirectMessagesConversationFragment.KEY_DISPLAY_PROFILE_IMAGE, true);
	}

	public AccountsSpinnerAdapter(final Context context, final Collection<Account> accounts) {
		this(context);
		addAll(accounts);
	}

	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getDropDownView(position, convertView, parent);
		bindView(view, getItem(position));
		return view;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		bindView(view, getItem(position));
		return view;
	}

	private void bindView(final View view, final Account item) {
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		text2.setVisibility(item.is_dummy ? View.GONE : View.VISIBLE);
		icon.setVisibility(item.is_dummy ? View.GONE : View.VISIBLE);
		if (!item.is_dummy) {
			text1.setText(item.name);
			text2.setText(String.format("@%s", item.screen_name));
			if (mDisplayProfileImage) {
				mImageLoader.displayProfileImage(icon, item.profile_image_url);
			} else {
				icon.setImageResource(R.drawable.ic_profile_image_default);
			}
		} else {
			text1.setText(R.string.none);
		}
	}

}
