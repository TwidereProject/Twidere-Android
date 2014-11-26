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

package org.mariotaku.twidere.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayRecyclerAdapter;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;
import org.mariotaku.twidere.view.ForegroundColorView;

public final class ColorPickerDialog extends AlertDialog implements Constants, OnColorChangedListener {

    private final static int[] COLORS = {R.color.material_red, R.color.material_pink,
            R.color.material_purple, R.color.material_deep_purple, R.color.material_indigo,
            R.color.material_blue, R.color.material_light_blue, R.color.material_cyan,
            R.color.material_teal, R.color.material_green, R.color.material_light_green,
            R.color.material_lime, R.color.material_yellow, R.color.material_amber,
            R.color.material_orange, R.color.material_deep_orange};

    private final ColorsAdapter mColorsAdapter;

    private ColorPickerView mColorPicker;
    private RecyclerView mColorPresets;

    private final Resources mResources;

    private final Bitmap mTempBitmap;
    private final Canvas mCanvas;

    private final int mIconWidth, mIconHeight;
    private final int mRectangleSize, mNumRectanglesHorizontal, mNumRectanglesVertical;

    public ColorPickerDialog(final Context context, final int initialColor, final boolean showAlphaSlider) {
        super(context);
        mColorsAdapter = new ColorsAdapter(this, context);
        mResources = context.getResources();
        final float density = mResources.getDisplayMetrics().density;
        mIconWidth = (int) (32 * density);
        mIconHeight = (int) (32 * density);
        mRectangleSize = (int) (density * 5);
        mNumRectanglesHorizontal = (int) Math.ceil(mIconWidth / mRectangleSize);
        mNumRectanglesVertical = (int) Math.ceil(mIconHeight / mRectangleSize);
        mTempBitmap = Bitmap.createBitmap(mIconWidth, mIconHeight, Config.ARGB_8888);
        mCanvas = new Canvas(mTempBitmap);
        init(context, initialColor, showAlphaSlider);
        initColors();
        mColorsAdapter.setCurrentColor(initialColor);
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void onColorChanged(final int color) {
        mColorsAdapter.setCurrentColor(color);
        updateColorPreviewBitmap(color);
        setIcon(new BitmapDrawable(mResources, mTempBitmap));
    }

    public final void setAlphaSliderVisible(final boolean visible) {
        mColorPicker.setAlphaSliderVisible(visible);
    }

    public final void setColor(final int color) {
        mColorPicker.setColor(color);
    }

    public final void setColor(final int color, final boolean callback) {
        mColorPicker.setColor(color, callback);
    }

    private void init(final Context context, final int color, final boolean showAlphaSlider) {

        // To fight color branding.
        getWindow().setFormat(PixelFormat.RGBA_8888);

        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View dialogView = inflater.inflate(R.layout.dialog_color_picker, null);

        mColorPicker = (ColorPickerView) dialogView.findViewById(R.id.color_picker);
        mColorPresets = (RecyclerView) dialogView.findViewById(R.id.color_presets);

        mColorPicker.setOnColorChangedListener(this);
        mColorPresets.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mColorPresets.setAdapter(mColorsAdapter);
//		mColorPresets.setOnItemClickListener(this);

        setColor(color, true);
        setAlphaSliderVisible(showAlphaSlider);

        setView(dialogView);
        setTitle(R.string.pick_color);
    }

    private void initColors() {
        final Resources resources = getContext().getResources();
        for (final int colorRes : COLORS) {
            mColorsAdapter.add(resources.getColor(colorRes));
        }
    }

    private void updateColorPreviewBitmap(final int color) {
        final Rect r = new Rect();
        boolean verticalStartWhite = true;
        for (int i = 0; i <= mNumRectanglesVertical; i++) {

            boolean isWhite = verticalStartWhite;
            for (int j = 0; j <= mNumRectanglesHorizontal; j++) {

                r.top = i * mRectangleSize;
                r.left = j * mRectangleSize;
                r.bottom = r.top + mRectangleSize;
                r.right = r.left + mRectangleSize;
                final Paint paint = new Paint();
                paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

                mCanvas.drawRect(r, paint);

                isWhite = !isWhite;
            }

            verticalStartWhite = !verticalStartWhite;

        }
        mCanvas.drawColor(color);
        final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2.0f);
        final float[] points = new float[]{0, 0, mIconWidth, 0, 0, 0, 0, mIconHeight, mIconWidth, 0, mIconWidth,
                mIconHeight, 0, mIconHeight, mIconWidth, mIconHeight};
        mCanvas.drawLines(points, paint);

    }

    private static class ColorsAdapter extends ArrayRecyclerAdapter<Integer, ColorViewHolder> implements View.OnClickListener {

        private final ColorPickerDialog mDialog;
        private final LayoutInflater mInflater;
        private int mCurrentColor;

        public ColorsAdapter(ColorPickerDialog dialog, final Context context) {
            mDialog = dialog;
            mInflater = LayoutInflater.from(context);
        }

        public void setCurrentColor(final int color) {
            mCurrentColor = color;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(ColorViewHolder holder, int position, Integer item) {
            holder.setItem(position, item, mCurrentColor == item);
        }

        @Override
        public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.gallery_item_color_picker_preset, parent, false);
            view.setOnClickListener(this);
            return new ColorViewHolder(view);
        }

        @Override
        public void onClick(View v) {
            final Object tag = v.getTag();
            if (!(tag instanceof Integer)) return;
            mDialog.mColorPicker.setColor(getItem((Integer) tag), true);
        }
    }

    public static class ColorViewHolder extends ViewHolder {

        private final ForegroundColorView colorView;

        public ColorViewHolder(View itemView) {
            super(itemView);
            colorView = (ForegroundColorView) itemView.findViewById(R.id.color);
        }

        public void setItem(int position, int color, boolean activated) {
            itemView.setTag(position);
            colorView.setColor(color);
            colorView.setActivated(activated);
        }
    }
}
