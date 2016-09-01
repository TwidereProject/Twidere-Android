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

import org.apache.commons.lang3.ObjectUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableMediaUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Dynamic layout for media preview
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
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardMediaContainer);
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.CardMediaContainer_android_horizontalSpacing, 0);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.CardMediaContainer_android_verticalSpacing, 0);
        a.recycle();
    }


    public void displayMedia(@NonNull final int... imageRes) {
        for (int i = 0, j = getChildCount(), k = imageRes.length; i < j; i++) {
            final View child = getChildAt(i);
            final ImageView imageView = (ImageView) child.findViewById(R.id.mediaPreview);
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
                             final UserKey accountId, final long extraId,
                             @Nullable final OnMediaClickListener mediaClickListener,
                             @Nullable final MediaLoadingHandler loadingHandler) {
        displayMedia(loader, mediaArray, accountId, mediaClickListener, loadingHandler, extraId, false);
    }

    public void displayMedia(@NonNull final MediaLoaderWrapper loader,
                             @Nullable final ParcelableMedia[] mediaArray, final UserKey accountId,
                             @Nullable final OnMediaClickListener mediaClickListener,
                             @Nullable final MediaLoadingHandler loadingHandler,
                             final long extraId, boolean withCredentials) {
        if (mediaArray == null || mMediaPreviewStyle == VALUE_MEDIA_PREVIEW_STYLE_CODE_NONE) {
            for (int i = 0, j = getChildCount(); i < j; i++) {
                final View child = getChildAt(i);
                child.setTag(null);
                child.setVisibility(GONE);
            }
            return;
        }
        final View.OnClickListener clickListener = new ImageGridClickListener(mediaClickListener,
                accountId, extraId);
        for (int i = 0, j = getChildCount(), k = mediaArray.length; i < j; i++) {
            final View child = getChildAt(i);
            if (mediaClickListener != null) {
                child.setOnClickListener(clickListener);
            }
            final ImageView imageView = (ImageView) child.findViewById(R.id.mediaPreview);
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
                if (ObjectUtils.notEqual(url, imageView.getTag()) || imageView.getDrawable() == null) {
                    if (withCredentials) {
                        loader.displayPreviewImageWithCredentials(imageView, url, accountId, loadingHandler);
                    } else {
                        loader.displayPreviewImage(imageView, url, loadingHandler);
                    }
                }
                imageView.setTag(url);
                if (imageView instanceof MediaPreviewImageView) {
                    ((MediaPreviewImageView) imageView).setHasPlayIcon(ParcelableMediaUtils.hasPlayIcon(media.type));
                }
                if (TextUtils.isEmpty(media.alt_text)) {
                    child.setContentDescription(getContext().getString(R.string.media));
                } else {
                    child.setContentDescription(media.alt_text);
                }
                child.setTag(media);
                child.setVisibility(VISIBLE);
            } else {
                loader.cancelDisplayTask(imageView);
                imageView.setTag(null);
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

    private void measure1Media(int contentWidth, int[] childIndices, float ratioMultiplier) {
        final View child = getChildAt(childIndices[0]);
        final int childHeight = Math.round(contentWidth * WIDTH_HEIGHT_RATIO * ratioMultiplier);
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

    private void measure3Media(int contentWidth, int horizontalSpacing, int[] childIndices, float ratioMultiplier) {
        final View child0 = getChildAt(childIndices[0]), child1 = getChildAt(childIndices[1]),
                child2 = getChildAt(childIndices[2]);
        final int childWidth = (contentWidth - horizontalSpacing) / 2;
        final int childLeftHeightSpec = MeasureSpec.makeMeasureSpec(Math.round(childWidth * ratioMultiplier), MeasureSpec.EXACTLY);
        final int widthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        child0.measure(widthSpec, childLeftHeightSpec);
        final int childRightHeight = Math.round((childWidth - horizontalSpacing) / 2 * ratioMultiplier);
        final int childRightHeightSpec = MeasureSpec.makeMeasureSpec(childRightHeight, MeasureSpec.EXACTLY);
        child1.measure(widthSpec, childRightHeightSpec);
        child2.measure(widthSpec, childRightHeightSpec);
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
        float ratioMultiplier = 1;
        int contentHeight = -1;
        if (getLayoutParams().height != LayoutParams.WRAP_CONTENT) {
            final int measuredHeight = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            ratioMultiplier = contentWidth > 0 ? measuredHeight / (contentWidth * WIDTH_HEIGHT_RATIO) : 1;
            contentHeight = contentWidth;
        }
        final int[] childIndices = createChildIndices();
        final int childCount = getChildIndicesInLayout(this, childIndices);
        int heightSum = 0;
        if (childCount > 0) {
            if (childCount == 1) {
                measure1Media(contentWidth, childIndices, ratioMultiplier);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO * ratioMultiplier);
            } else if (childCount == 2) {
                measureGridMedia(childCount, 2, contentWidth, ratioMultiplier, mHorizontalSpacing,
                        mVerticalSpacing, childIndices);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO * ratioMultiplier);
            } else if (childCount == 3) {
                measure3Media(contentWidth, mHorizontalSpacing, childIndices, ratioMultiplier);
                heightSum = Math.round(contentWidth * WIDTH_HEIGHT_RATIO * ratioMultiplier);
            } else {
                heightSum = measureGridMedia(childCount, 2, contentWidth,
                        WIDTH_HEIGHT_RATIO * ratioMultiplier, mHorizontalSpacing, mVerticalSpacing, childIndices);
            }
            if (contentHeight > 0) {
                heightSum = contentHeight;
            }
        } else if (contentHeight > 0) {
            heightSum = contentHeight;
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
        void onMediaClick(View view, ParcelableMedia media, UserKey accountKey, long id);
    }

    private static class ImageGridClickListener implements View.OnClickListener {
        private final WeakReference<OnMediaClickListener> mListenerRef;
        private final UserKey mAccountKey;
        private final long mExtraId;

        ImageGridClickListener(@Nullable final OnMediaClickListener listener, final UserKey accountKey,
                               final long extraId) {
            mListenerRef = new WeakReference<>(listener);
            mAccountKey = accountKey;
            mExtraId = extraId;
        }

        @Override
        public void onClick(final View v) {
            final OnMediaClickListener listener = mListenerRef.get();
            if (listener == null) return;
            listener.onMediaClick(v, (ParcelableMedia) v.getTag(), mAccountKey, mExtraId);
        }

    }

    @IntDef({VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE, VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PreviewStyle {

    }
}
