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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 14/12/17.
 */
public class CardMediaContainer extends ViewGroup implements Constants {

    private static final float WIDTH_HEIGHT_RATIO = 0.5f;

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
                             @NonNull final MediaLoaderWrapper loader,
                             final long accountId,
                             final OnMediaClickListener mediaClickListener,
                             final MediaLoadingHandler loadingHandler) {
        displayMedia(mediaArray, loader, accountId, false, mediaClickListener, loadingHandler);
    }

    public void displayMedia(@Nullable final ParcelableMedia[] mediaArray,
                             @NonNull final MediaLoaderWrapper loader,
                             final long accountId, boolean withCredentials,
                             final OnMediaClickListener mediaClickListener,
                             final MediaLoadingHandler loadingHandler) {
        if (mediaArray == null || mMediaPreviewStyle == VALUE_MEDIA_PREVIEW_STYLE_CODE_NONE) {
            for (int i = 0, j = getChildCount(); i < j; i++) {
                final View child = getChildAt(i);
                child.setTag(null);
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
                    imageView.setScaleType(ScaleType.FIT_CENTER);
                    break;
                }
            }
            if (i < k) {
                final ParcelableMedia media = mediaArray[i];
                final String url = TextUtils.isEmpty(media.preview_url) ? media.media_url : media.preview_url;
                if (withCredentials) {
                    loader.displayPreviewImageWithCredentials(imageView, url, accountId, loadingHandler);
                } else {
                    loader.displayPreviewImage(imageView, url, loadingHandler);
                }
                child.setTag(media);
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
            if (childCount == 1) {
                layout1Media(childIndices);
            } else if (childCount == 3) {
                layout3Media(mHorizontalSpacing, mVerticalSpacing, childIndices);
            } else {
                layoutGridMedia(childCount, 2, mHorizontalSpacing, mVerticalSpacing, childIndices);
            }
        }
    }

    private void measure1Media(int contentWidth, int[] childIndices) {
        final View child = getChildAt(childIndices[0]);
        final int childHeight = Math.round(contentWidth * WIDTH_HEIGHT_RATIO);
        final int widthSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY);
        final int heightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    private void layout1Media(int[] childIndices) {
        final View child = getChildAt(childIndices[0]);
        final int left = getPaddingLeft(), top = getPaddingTop();
        final int right = left + child.getMeasuredWidth(), bottom = top + child.getMeasuredHeight();
        child.layout(left, top, right, bottom);
    }

    private int measureGridMedia(int childCount, int columnCount, int contentWidth,
                                 float widthHeightRatio, int horizontalSpacing, int verticalSpacing,
                                 int[] childIndices) {
        final int childWidth = (contentWidth - horizontalSpacing * (columnCount - 1)) / columnCount;
        final int childHeight = Math.round(childWidth * widthHeightRatio);
        final int widthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        final int heightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        for (int i = 0; i < childCount; i++) {
            getChildAt(childIndices[i]).measure(widthSpec, heightSpec);
        }
        final int rowsCount = (int) Math.ceil(childCount / (double) columnCount);
        return rowsCount * childHeight + (rowsCount - 1) * verticalSpacing;
    }

    private void layoutGridMedia(int childCount, int columnCount, int horizontalSpacing,
                                 int verticalSpacing, int[] childIndices) {
        final int initialLeft = getPaddingLeft();
        int left = initialLeft, top = getPaddingTop();
        for (int i = 0; i < childCount; i++) {
            final int colIdx = i % columnCount;
            final View child = getChildAt(childIndices[i]);
            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            if (colIdx == columnCount - 1) {
                // Last item in this row, set top of next row to last view bottom + verticalSpacing
                top = child.getBottom() + verticalSpacing;
                // And reset left to initial left
                left = initialLeft;
            } else {
                // The left of next item is right + horizontalSpacing of previous item
                left = child.getRight() + horizontalSpacing;
            }
        }
    }

    private void measure3Media(int contentWidth, int horizontalSpacing, int[] childIndices) {
        final View child0 = getChildAt(childIndices[0]), child1 = getChildAt(childIndices[1]),
                child2 = getChildAt(childIndices[2]);
        final int childWidth = (contentWidth - horizontalSpacing) / 2;
        final int sizeSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        child0.measure(sizeSpec, sizeSpec);
        final int childRightHeight = Math.round(childWidth - horizontalSpacing) / 2;
        final int heightSpec = MeasureSpec.makeMeasureSpec(childRightHeight, MeasureSpec.EXACTLY);
        child1.measure(sizeSpec, heightSpec);
        child2.measure(sizeSpec, heightSpec);
    }

    private void layout3Media(int horizontalSpacing, int verticalSpacing, int[] childIndices) {
        final int left = getPaddingLeft(), top = getPaddingTop();
        final View child0 = getChildAt(childIndices[0]), child1 = getChildAt(childIndices[1]),
                child2 = getChildAt(childIndices[2]);
        child0.layout(left, top, left + child0.getMeasuredWidth(), top + child0.getMeasuredHeight());
        final int rightColLeft = child0.getRight() + horizontalSpacing;
        child1.layout(rightColLeft, top, rightColLeft + child1.getMeasuredWidth(),
                top + child1.getMeasuredHeight());
        final int child2Top = child1.getBottom() + verticalSpacing;
        child2.layout(rightColLeft, child2Top, rightColLeft + child2.getMeasuredWidth(),
                child2Top + child2.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int contentWidth = measuredWidth - getPaddingLeft() - getPaddingRight();
        final int[] childIndices = createChildIndices();
        final int childCount = getChildIndicesInLayout(this, childIndices);
        int heightSum = 0;
        if (childCount > 0) {
            if (childCount == 1) {
                measure1Media(contentWidth, childIndices);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO);
            } else if (childCount == 2) {
                measureGridMedia(childCount, 2, contentWidth, 1, mHorizontalSpacing, mVerticalSpacing,
                        childIndices);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO);
            } else if (childCount == 3) {
                measure3Media(contentWidth, mHorizontalSpacing, childIndices);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO);
            } else {
                heightSum = measureGridMedia(childCount, 2, contentWidth, WIDTH_HEIGHT_RATIO,
                        mHorizontalSpacing, mVerticalSpacing, childIndices);
            }
        }
        final int height = heightSum + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
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

    public interface OnMediaClickListener {
        void onMediaClick(View view, ParcelableMedia media, long accountId);
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

    @IntDef({VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE, VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PreviewStyle {

    }
}
