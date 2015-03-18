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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 15/3/17.
 */
public class ViewStatusDialogFragment extends BaseSupportDialogFragment {

    private StatusViewHolder mHolder;
    private View mStatusContainer;

    public ViewStatusDialogFragment() {
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_scrollable_status, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStatusContainer = view.findViewById(R.id.status_container);
        mHolder = new StatusViewHolder(view);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
            dismiss();
            return;
        }
        final TwidereApplication application = getApplication();
        final FragmentActivity activity = getActivity();
        final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
        final ImageLoadingHandler handler = new ImageLoadingHandler(R.id.media_preview_progress);
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final SharedPreferencesWrapper preferences = SharedPreferencesWrapper.getInstance(activity,
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
        final int profileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        final int mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
        final boolean nicknameOnly = preferences.getBoolean(KEY_NICKNAME_ONLY, false);
        final boolean displayExtraType = args.getBoolean(EXTRA_SHOW_EXTRA_TYPE, true);
        final boolean displayMediaPreview;
        if (args.containsKey(EXTRA_SHOW_MEDIA_PREVIEW)) {
            displayMediaPreview = args.getBoolean(EXTRA_SHOW_MEDIA_PREVIEW);
        } else {
            displayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
        }
        mHolder.displayStatus(activity, loader, handler, twitter, displayMediaPreview, true,
                true, nameFirst, nicknameOnly, profileImageStyle, mediaPreviewStyle, status, null,
                displayExtraType);
        mStatusContainer.findViewById(R.id.item_menu).setVisibility(View.GONE);
        mStatusContainer.findViewById(R.id.action_buttons).setVisibility(View.GONE);
    }


}
