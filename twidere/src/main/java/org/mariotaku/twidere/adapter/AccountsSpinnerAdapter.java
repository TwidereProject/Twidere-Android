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
import org.mariotaku.twidere.fragment.support.MessagesConversationFragment;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;

import java.util.Collection;

import javax.inject.Inject;

public class AccountsSpinnerAdapter extends ArrayAdapter<ParcelableCredentials> {

    @Inject
    MediaLoaderWrapper mImageLoader;
    private final boolean mDisplayProfileImage;
    private final Context mContext;
    private String mDummyItemText;

    public AccountsSpinnerAdapter(final Context context) {
        this(context, R.layout.list_item_user);
    }

    public AccountsSpinnerAdapter(final Context context, int itemViewResource) {
        super(context, itemViewResource);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(context)).build().inject(this);
        mContext = context;
        mDisplayProfileImage = context.getSharedPreferences(MessagesConversationFragment.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getBoolean(MessagesConversationFragment.KEY_DISPLAY_PROFILE_IMAGE, true);
    }

    public AccountsSpinnerAdapter(final Context context, final Collection<ParcelableCredentials> accounts) {
        this(context);
        addAll(accounts);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).account_id;
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

    private void bindView(final View view, final ParcelableCredentials item) {
        final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
        if (!item.is_dummy) {
            if (text1 != null) {
                text1.setVisibility(View.VISIBLE);
                text1.setText(item.name);
            }
            if (text2 != null) {
                text2.setVisibility(View.VISIBLE);
                text2.setText(String.format("@%s", item.screen_name));
            }
            if (icon != null) {
                icon.setVisibility(View.VISIBLE);
                if (mDisplayProfileImage) {
                    mImageLoader.displayProfileImage(icon, item.profile_image_url);
                } else {
                    mImageLoader.cancelDisplayTask(icon);
//                    icon.setImageResource(R.drawable.ic_profile_image_default);
                }
            }
        } else {
            if (text1 != null) {
                text1.setVisibility(View.VISIBLE);
                text1.setText(mDummyItemText);
            }
            if (text2 != null) {
                text2.setVisibility(View.GONE);
            }
            if (icon != null) {
                icon.setVisibility(View.GONE);
            }
        }
    }


    public void setDummyItemText(int textRes) {
        setDummyItemText(mContext.getString(textRes));
    }

    public void setDummyItemText(String text) {
        mDummyItemText = text;
        notifyDataSetChanged();
    }

}
