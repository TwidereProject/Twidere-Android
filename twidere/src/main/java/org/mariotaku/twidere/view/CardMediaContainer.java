/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.Utils.OnMediaClickListener;

/**
 * Created by mariotaku on 14/12/17.
 */
public class CardMediaContainer extends ViewGroup implements Constants {

    private final int mMaxColumns;
    private final int mHorizontalSpacing, mVerticalSpacing;
    private int[] mTempIndices;
    private int mMediaPreviewStyle;

    public CardMediaContainer(Context context) {
        this(context, null);
    }

    public CardMediaContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardMediaContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaxColumns = 3;
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.horizontalSpacing, android.R.attr.verticalSpacing});
        mHorizontalSpacing = a.getDimensionPixelSize(0, 0);
        mVerticalSpacing = a.getDimensionPixelSize(1, 0);
        a.recycle();
    }


    public void displayMedia(@NonNull final int... imageRes) {
        for (int i = 0, j = getChildCount(), k = imageRes.length; i < j; i++) {
            final View child = getChildAt(i);
            final ImageView imageView = (ImageView) child.findViewById(R.id.media_preview);
            final View progress = child.findViewById(R.id.media_preview_progress);
            progress.setVisibility(GONE);
            if (i < k) {
                imageView.setImageResource(imageRes[i]);
            } else {
                imageView.setImageDrawable(null);
                child.setVisibility(GONE);
            }
        }
    }

    public void displayMedia(@Nullable final ParcelableMedia[] mediaArray,
                             @NonNull final ImageLoaderWrapper loader,
                             final long accountId,
                             final OnMediaClickListener mediaClickListener,
                             final ImageLoadingHandler loadingHandler) {
        if (mediaArray == null || mMediaPreviewStyle == VALUE_MEDIA_PREVIEW_STYLE_CODE_NONE) {
            for (int i = 0, j = getChildCount(); i < j; i++) {
                final View child = getChildAt(i);
                child.setVisibility(GONE);
            }
            return;
        }
        final View.OnClickListener clickListener = new ImageGridClickListener(mediaClickListener, accountId);
        for (int i = 0, j = getChildCount(), k = mediaArray.length; i < j; i++) {
            final View child = getChildAt(i);
            child.setOnClickListener(clickListener);
            final ImageView imageView = (ImageView) child.findViewById(R.id.media_preview);
            switch (mMediaPreviewStyle) {
                case VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP: {
                    imageView.setScaleType(ScaleType.CENTER_CROP);
                    break;
                }
                case VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE: {
                    imageView.setScaleType(ScaleType.CENTER_INSIDE);
                    break;
                }
            }
            if (i < k) {
                final ParcelableMedia media = mediaArray[i];
                loader.displayPreviewImage(imageView, media.page_url, loadingHandler);
                child.setVisibility(VISIBLE);
                if (i == j - 1) {
                    final TextView moreIndicator = (TextView) child.findViewById(R.id.more_media);
                    moreIndicator.setVisibility(j < k ? VISIBLE : GONE);
                    if (k > j) {
                        final int extraMediaCount = k - j;
                        moreIndicator.setText(getResources().getQuantityString(R.plurals.N_media,
                                extraMediaCount, extraMediaCount));
                    } else {
                        moreIndicator.setText(null);
                    }
                }
            } else {
                loader.cancelDisplayTask(imageView);
                child.setVisibility(GONE);
            }
        }
    }

    public void setStyle(int style) {
        mMediaPreviewStyle = style;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int[] childIndices = createChildIndices();
        final int childCount = getChildIndicesInLayout(this, childIndices);
        if (childCount > 0) {
            final double childSqrt = Math.sqrt(childCount);
            final int columnCount = (int) (childSqrt % 1 == 0 ? Math.ceil(childSqrt) : Math.min(childCount, mMaxColumns));
            final int rowCount = (int) Math.ceil(childCount / (double) columnCount);
            final int firstRowColumnCount = childCount - (columnCount * (rowCount - 1));
            for (int i = 0; i < rowCount; i++) {
                final int currColumnCount = i == 0 ? firstRowColumnCount : columnCount;
                final int childT;
                if (i == 0) {
                    childT = getPaddingTop();
                } else if (i == 1) {
                    childT = getChildAt(childIndices[0]).getBottom() + mVerticalSpacing;
                } else {
                    childT = getChildAt(childIndices[firstRowColumnCount + columnCount * (i - 1)]).getBottom() + mVerticalSpacing;
                }
                for (int j = 0; j < currColumnCount; j++) {
                    final int childIdx = i == 0 ? j : firstRowColumnCount + columnCount * (i - 1) + j;
                    final View child = getChildAt(childIndices[childIdx]);
                    final int childL = j == 0 ? getPaddingLeft() : (getChildAt(childIndices[childIdx - 1]).getRight() + mHorizontalSpacing);
                    child.layout(childL, childT, childL + child.getMeasuredWidth(), childT + child.getMeasuredHeight());
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int contentWidth = measuredWidth - getPaddingLeft() - getPaddingRight();
        final int[] childIndices = createChildIndices();
        final int childCount = getChildIndicesInLayout(this, childIndices);
        int heightSum = 0;
        if (childCount > 0) {
            final double childSqrt = Math.sqrt(childCount);
            final int columnCount = (int) (childSqrt % 1 == 0 ? Math.ceil(childSqrt) : Math.min(childCount, mMaxColumns));
            final int rowCount = (int) Math.ceil(childCount / (double) columnCount);
            final int firstRowColumnCount = childCount - (columnCount * (rowCount - 1));
            for (int i = 0; i < rowCount; i++) {
                final int currColumnCount = i == 0 ? firstRowColumnCount : columnCount;
                final int columnWidth = (contentWidth - (mHorizontalSpacing * (currColumnCount - 1))) / currColumnCount;
                final int columnHeight = columnWidth;
                heightSum = heightSum + columnHeight;
                final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY);
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(columnHeight, MeasureSpec.EXACTLY);
                for (int j = 0; j < currColumnCount; j++) {
                    final int childIdx = i == 0 ? j : firstRowColumnCount + columnCount * (i - 1) + j;
                    getChildAt(childIndices[childIdx]).measure(childWidthMeasureSpec, childHeightMeasureSpec);
                }
            }
            heightSum = heightSum + (mVerticalSpacing * rowCount - 1);
        }
        heightSum = heightSum + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSum, MeasureSpec.EXACTLY));
    }

    private int[] createChildIndices() {
        if (mTempIndices == null || mTempIndices.length < getChildCount()) {
            return mTempIndices = new int[getChildCount()];
        }
        return mTempIndices;
    }

    private static int getChildIndicesInLayout(ViewGroup viewGroup, int[] indices) {
        final int childCount = viewGroup.getChildCount();
        int indicesCount = 0;
        for (int i = 0; i < childCount; i++) {
            if (viewGroup.getChildAt(i).getVisibility() != GONE) {
                indices[indicesCount++] = i;
            }
        }
        return indicesCount;
    }

    private static class ImageGridClickListener implements View.OnClickListener {
        private final OnMediaClickListener mListener;
        private final long mAccountId;

        ImageGridClickListener(final OnMediaClickListener listener, final long accountId) {
            mListener = listener;
            mAccountId = accountId;
        }

        @Override
        public void onClick(final View v) {
            if (mListener == null) return;
            mListener.onMediaClick(v, (ParcelableMedia) v.getTag(), mAccountId);
        }

    }
}
