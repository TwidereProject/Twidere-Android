/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commonsware.cwac.layouts.AspectLockedFrameLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableMediaUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.view.MediaPreviewImageView;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public class StaggeredGridParcelableStatusesAdapter extends ParcelableStatusesAdapter {

    public StaggeredGridParcelableStatusesAdapter(Context context) {
        super(context);
    }

    @Override
    protected int[] getProgressViewIds() {
        return new int[]{R.id.media_image_progress};
    }

    @NonNull
    @Override
    protected IStatusViewHolder onCreateStatusViewHolder(ViewGroup parent) {
        final View view = getInflater().inflate(R.layout.adapter_item_media_status, parent, false);
        final MediaStatusViewHolder holder = new MediaStatusViewHolder(this, view);
        holder.setOnClickListeners();
        holder.setupViewOptions();
        return holder;
    }

    public static class MediaStatusViewHolder extends RecyclerView.ViewHolder
            implements IStatusViewHolder, View.OnClickListener, View.OnLongClickListener {
        private final SimpleAspectRatioSource aspectRatioSource = new SimpleAspectRatioSource();

        private final AspectLockedFrameLayout mediaImageContainer;
        private final MediaPreviewImageView mediaImageView;
        private final ImageView mediaProfileImageView;
        private final TextView mediaTextView;
        private final IStatusesAdapter<?> adapter;
        private StatusClickListener listener;

        public MediaStatusViewHolder(IStatusesAdapter<?> adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            mediaImageContainer = (AspectLockedFrameLayout) itemView.findViewById(R.id.media_image_container);
            mediaImageContainer.setAspectRatioSource(aspectRatioSource);
            mediaImageView = (MediaPreviewImageView) itemView.findViewById(R.id.media_image);
            mediaProfileImageView = (ImageView) itemView.findViewById(R.id.media_profile_image);
            mediaTextView = (TextView) itemView.findViewById(R.id.media_text);
        }


        @Override
        public void displayStatus(@NonNull ParcelableStatus status, boolean displayInReplyTo, boolean shouldDisplayExtraType) {
            final MediaLoaderWrapper loader = adapter.getMediaLoader();
            final ParcelableMedia[] media = status.media;
            if (media == null || media.length < 1) return;
            final ParcelableMedia firstMedia = media[0];
            mediaTextView.setText(status.text_unescaped);
            if (firstMedia.width > 0 && firstMedia.height > 0) {
                aspectRatioSource.setSize(firstMedia.width, firstMedia.height);
            } else {
                aspectRatioSource.setSize(100, 100);
            }
            mediaImageContainer.setTag(firstMedia);
            mediaImageContainer.requestLayout();

            mediaImageView.setHasPlayIcon(ParcelableMediaUtils.hasPlayIcon(firstMedia.type));
            loader.displayProfileImage(mediaProfileImageView, status);
            loader.displayPreviewImageWithCredentials(mediaImageView, firstMedia.preview_url,
                    status.account_key, adapter.getMediaLoadingHandler());
        }

        @Override
        @Nullable
        public ImageView getProfileImageView() {
            return mediaProfileImageView;
        }

        @Override
        @Nullable
        public ImageView getProfileTypeView() {
            return null;
        }

        @Override
        public void onClick(View v) {
            if (listener == null) return;
            switch (v.getId()) {
                case R.id.itemContent: {
                    listener.onStatusClick(this, getLayoutPosition());
                    break;
                }
            }
        }

        public boolean onLongClick(View v) {
            return false;
        }

        @Override
        public void onMediaClick(View view, ParcelableMedia media, UserKey accountKey, long extraId) {
        }

        @Override
        public void setStatusClickListener(StatusClickListener listener) {
            this.listener = listener;
            itemView.findViewById(R.id.itemContent).setOnClickListener(this);
        }

        @Override
        public void setTextSize(float textSize) {

        }

        @Override
        public void playLikeAnimation(LikeAnimationDrawable.OnLikedListener listener) {

        }

        public void setOnClickListeners() {
            setStatusClickListener(adapter.getStatusClickListener());
        }

        public void setupViewOptions() {
            setTextSize(adapter.getTextSize());
        }


        private static class SimpleAspectRatioSource implements AspectLockedFrameLayout.AspectRatioSource {
            private int width, height;

            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            public void setSize(int width, int height) {
                this.width = width;
                this.height = height;
            }

        }
    }
}
