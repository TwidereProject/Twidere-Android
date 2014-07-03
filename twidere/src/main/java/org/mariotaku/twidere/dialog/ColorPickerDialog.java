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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scvngr.levelup.views.gallery.AdapterView;
import com.scvngr.levelup.views.gallery.AdapterView.OnItemClickListener;
import com.scvngr.levelup.views.gallery.Gallery;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;
import org.mariotaku.twidere.view.ForegroundColorView;

public final class ColorPickerDialog extends AlertDialog implements OnItemClickListener, OnColorChangedListener,
		Constants {

	private final static int[] COLORS = { HOLO_RED_DARK, HOLO_RED_LIGHT, HOLO_ORANGE_DARK, HOLO_ORANGE_LIGHT,
			HOLO_GREEN_LIGHT, HOLO_GREEN_DARK, HOLO_BLUE_LIGHT, HOLO_BLUE_DARK, HOLO_PURPLE_DARK, HOLO_PURPLE_LIGHT,
			Color.WHITE };

	private final ColorsAdapter mColorsAdapter;

	private ColorPickerView mColorPicker;
	private Gallery mColorPresets;

	private final Resources mResources;

	private final Bitmap mTempBitmap;
	private final Canvas mCanvas;

	private final int mIconWidth, mIconHeight;
	private final int mRectrangleSize, mNumRectanglesHorizontal, mNumRectanglesVertical;

	public ColorPickerDialog(final Context context, final int initialColor, final boolean showAlphaSlider) {
		super(context);
		mColorsAdapter = new ColorsAdapter(context);
		mResources = context.getResources();
		final float density = mResources.getDisplayMetrics().density;
		mIconWidth = (int) (32 * density);
		mIconHeight = (int) (32 * density);
		mRectrangleSize = (int) (density * 5);
		mNumRectanglesHorizontal = (int) Math.ceil(mIconWidth / mRectrangleSize);
		mNumRectanglesVertical = (int) Math.ceil(mIconHeight / mRectrangleSize);
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

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final int color = mColorsAdapter.getItem(position);
		if (mColorPicker == null) return;
		mColorPicker.setColor(color, true);
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
		mColorPresets = (Gallery) dialogView.findViewById(R.id.color_presets);

		mColorPicker.setOnColorChangedListener(this);
		mColorPresets.setAdapter(mColorsAdapter);
		mColorPresets.setOnItemClickListener(this);
		mColorPresets.setScrollAfterItemClickEnabled(false);
		mColorPresets.setScrollRightSpacingEnabled(false);

		setColor(color, true);
		setAlphaSliderVisible(showAlphaSlider);

		setView(dialogView);
		setTitle(R.string.pick_color);
	}

	private void initColors() {
		for (final int color : COLORS) {
			mColorsAdapter.add(color);
		}
	}

	private void updateColorPreviewBitmap(final int color) {
		final Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= mNumRectanglesVertical; i++) {

			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= mNumRectanglesHorizontal; j++) {

				r.top = i * mRectrangleSize;
				r.left = j * mRectrangleSize;
				r.bottom = r.top + mRectrangleSize;
				r.right = r.left + mRectrangleSize;
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
		final float[] points = new float[] { 0, 0, mIconWidth, 0, 0, 0, 0, mIconHeight, mIconWidth, 0, mIconWidth,
				mIconHeight, 0, mIconHeight, mIconWidth, mIconHeight };
		mCanvas.drawLines(points, paint);

	}

	public static class ColorsAdapter extends ArrayAdapter<Integer> {

		private int mCurrentColor;

		public ColorsAdapter(final Context context) {
			super(context, R.layout.gallery_item_color_picker_preset);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final ForegroundColorView colorView = (ForegroundColorView) view.findViewById(R.id.color);
			final int color = getItem(position);
			colorView.setColor(color);
			colorView.setActivated(mCurrentColor == color);
			return view;
		}

		public void setCurrentColor(final int color) {
			mCurrentColor = color;
			notifyDataSetChanged();
		}

	}
}
