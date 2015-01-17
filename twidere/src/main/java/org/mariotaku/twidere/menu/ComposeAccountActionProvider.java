/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.BadgeView;
import org.mariotaku.twidere.view.ShapedImageView;

/**
 * Created by mariotaku on 15/1/17.
 */
public class ComposeAccountActionProvider extends ActionProvider implements OnClickListener {

    private final ImageLoaderWrapper mImageLoader;
    private ShapedImageView mProfileImageView;
    private BadgeView mCountView;
    private InvokedListener mInvokedListener;

    @Override
    public void onClick(View v) {
        if (mInvokedListener == null) return;
        mInvokedListener.onInvoked();
    }

    @Override
    public boolean onPerformDefaultAction() {
        if (mInvokedListener == null) return false;
        mInvokedListener.onInvoked();
        return true;
    }

    @Override
    public boolean hasSubMenu() {
        return false;
    }

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public ComposeAccountActionProvider(Context context) {
        super(context);
        mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
    }

    @Override
    public View onCreateActionView() {
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.action_item_account_selector, null);
        view.setOnClickListener(this);
        mProfileImageView = (ShapedImageView) view.findViewById(R.id.account_profile_image);
        mCountView = (BadgeView) view.findViewById(R.id.accounts_count);
        return view;
    }

    public void setInvokedListener(InvokedListener listener) {
        mInvokedListener = listener;
    }

    public void setSelectedAccounts(ParcelableAccount... accounts) {
        if (accounts.length == 1) {
            mCountView.setText(null);
            final ParcelableAccount account = accounts[0];
            mImageLoader.displayProfileImage(mProfileImageView, account.profile_image_url);
            mProfileImageView.setBorderColor(account.color);
        } else {
            mCountView.setText(String.valueOf(accounts.length));
            mImageLoader.cancelDisplayTask(mProfileImageView);
            mProfileImageView.setImageDrawable(null);
            mProfileImageView.setBorderColors(Utils.getAccountColors(accounts));
        }
    }

    public static interface InvokedListener {

        void onInvoked();

    }
}
