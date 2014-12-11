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

package org.mariotaku.twidere.util;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.mariotaku.twidere.R;

import java.util.HashMap;
import java.util.Map;

public class ImageLoadingHandler implements ImageLoadingListener, ImageLoadingProgressListener {

	private final Map<View, String> mLoadingUris = new HashMap<>();
	private final int[] mProgressBarIds;

	public ImageLoadingHandler() {
		this(R.id.media_preview_progress);
	}

	public ImageLoadingHandler(final int... progressBarIds) {
		mProgressBarIds = progressBarIds;
	}

	public String getLoadingUri(final View view) {
		return mLoadingUris.get(view);
	}

	@Override
	public void onLoadingCancelled(final String imageUri, final View view) {
		if (view == null || imageUri == null || imageUri.equals(mLoadingUris.get(view))) return;
		mLoadingUris.remove(view);
		final ProgressBar progress = findProgressBar(view.getParent());
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingComplete(final String imageUri, final View view, final Bitmap bitmap) {
		if (view == null) return;
		mLoadingUris.remove(view);
		final ProgressBar progress = findProgressBar(view.getParent());
		if (progress != null) {
			progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingFailed(final String imageUri, final View view, final FailReason reason) {
		if (view == null) return;
		if (view instanceof ImageView) {
			((ImageView) view).setImageDrawable(null);
			view.setBackgroundResource(R.drawable.image_preview_refresh);
		}
		mLoadingUris.remove(view);
        final ProgressBar progress = findProgressBar(view.getParent());
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
	}

	@Override
	public void onLoadingStarted(final String imageUri, final View view) {
		if (view == null || imageUri == null || imageUri.equals(mLoadingUris.get(view))) return;
		mLoadingUris.put(view, imageUri);
		final ProgressBar progress = findProgressBar(view.getParent());
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
			progress.setIndeterminate(true);
			progress.setMax(100);
		}
	}

	@Override
	public void onProgressUpdate(final String imageUri, final View view, final int current, final int total) {
		if (total == 0 || view == null) return;
		final ProgressBar progress = findProgressBar(view.getParent());
		if (progress != null) {
			progress.setIndeterminate(false);
			progress.setProgress(100 * current / total);
		}
	}

	private ProgressBar findProgressBar(final ViewParent viewParent) {
		if (mProgressBarIds == null || !(viewParent instanceof View)) return null;
		final View parent = (View) viewParent;
		for (final int id : mProgressBarIds) {
			final View progress = parent.findViewById(id);
			if (progress instanceof ProgressBar) return (ProgressBar) progress;
		}
		return null;
	}
}
