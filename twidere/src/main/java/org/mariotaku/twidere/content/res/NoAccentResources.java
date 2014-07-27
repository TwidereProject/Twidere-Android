/*******************************************************************************
 * Copyright 2013 NEGU Soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mariotaku.twidere.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.negusoft.holoaccent.AccentPalette;
import com.negusoft.holoaccent.AccentResources;
import com.negusoft.holoaccent.util.BitmapUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the default android Resources to replace and modify
 * drawables at runtime and apply the accent color.
 * <br/><br/>
 * "openRawResource()" and "getDrawable()" are called when inflating
 * XML drawable resources. By overriding them, we can replace the
 * components that form the drawables at runtime.
 * <br/><br/>
 * For the OverScroll, the native android drawables are modified
 * directly. We look up their id by name, and then we replace the
 * drawable with a tinted version by applying a ColorFilter.
 */
public class NoAccentResources extends Resources {

    private final List<AccentResources.Interceptor> mInterceptors = new ArrayList<>();
    private final List<AccentResources.ColorInterceptor> mColorInterceptors = new ArrayList<>();

    private static final int[] TINT_DRAWABLE_IDS = {};
    private static final int[] TINT_TRANSFORMATION_DRAWABLE_IDS = {};
    private final Context mContext;

    private List<Integer> mCustomTintDrawableIds;
    private List<Integer> mCustomTransformationDrawableIds;
    private int[] mTintDrawableIds;
    private int[] mTransformationDrawableIds;

    private boolean mInitialized = false;
    private AccentPalette mPalette;

    public NoAccentResources(Context c, Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        mContext = c;
    }

    /**
     * Make sure that the instance is initialized. It will check the 'mInitialized'
     * flag even if it is done within 'initialize()', to avoid getting into the
     * synchronized block every time.
     */
    private void checkInitialized() {
        if (mInitialized)
            return;
        initialize(mContext);
    }

    private synchronized void initialize(Context c) {
        if (mInitialized)
            return;
        mPalette = initPalette(c);
        mTintDrawableIds = appendDrawableIds(TINT_DRAWABLE_IDS, mCustomTintDrawableIds);
        mTransformationDrawableIds = appendDrawableIds(TINT_TRANSFORMATION_DRAWABLE_IDS, mCustomTransformationDrawableIds);
        addInterceptors(c);
        mInitialized = true;
    }

    private int[] appendDrawableIds(int[] defaults, List<Integer> custom) {
        if (custom == null)
            return defaults;

        int customSize = custom.size();
        int[] result = new int[defaults.length + customSize];
        for (int i = 0; i < customSize; i++)
            result[i] = custom.get(i);
        for (int i = 0; i < defaults.length; i++)
            result[customSize + i] = defaults[i];

        return result;
    }

    private AccentPalette initPalette(Context c) {
//        final int[] styleable = {android.R.attr.colorActivatedHighlight};
//        final TypedArray attrs = c.getTheme().obtainStyledAttributes(styleable);
//        final int holoBlue = super.getColor(android.R.color.holo_blue_light);
//        final int color = attrs.getColor(0, holoBlue);
        final int color = 0;
        return new AccentPalette(color, color, color);
    }

    private void addInterceptors(Context c) {
    }

    @Override
    public int getColor(int resId) throws NotFoundException {
        checkInitialized();

        // Give a chance to the interceptors to replace the drawable
        int result;
        for (AccentResources.ColorInterceptor interceptor : mColorInterceptors) {
            result = interceptor.getColor(this, mPalette, resId);
            if (result != 0)
                return result;
        }

        return super.getColor(resId);
    }

    @Override
    public Drawable getDrawable(int resId) throws Resources.NotFoundException {
        checkInitialized();

        // Give a chance to the interceptors to replace the drawable
        Drawable result;
        for (AccentResources.Interceptor interceptor : mInterceptors) {
            result = interceptor.getDrawable(this, mPalette, resId);
            if (result != null)
                return result;
        }

        return super.getDrawable(resId);
    }

    @Override
    public InputStream openRawResource(int resId, TypedValue value)
            throws NotFoundException {
        checkInitialized();

        for (int id : mTintDrawableIds) {
            if (resId == id)
                return getTintendResourceStream(resId, value, mPalette.accentColor);
        }
        for (int id : mTransformationDrawableIds) {
            if (resId == id)
                return getTintTransformationResourceStream(resId, value, mPalette.accentColor);
        }
        return super.openRawResource(resId, value);
    }

    /**
     * Method to access the palette instance
     */
    public AccentPalette getPalette() {
        checkInitialized();
        return mPalette;
    }

    /**
     * Add a drawable interceptor. They are evaluated in the order they are added, and before the
     * default interceptors.
     */
    public void addInterceptor(AccentResources.Interceptor interceptor) {
        mInterceptors.add(0, interceptor);
    }

    /**
     * Add a color interceptor. They are evaluated in the order they are added, and before the
     * default interceptors.
     */
    public void addColorInterceptor(AccentResources.ColorInterceptor interceptor) {
        mColorInterceptors.add(0, interceptor);
    }

    /**
     * Add a drawable resource to which to apply the "tint" technique.
     */
    public void addTintResourceId(int resId) {
        if (mCustomTintDrawableIds == null)
            mCustomTintDrawableIds = new ArrayList<Integer>();
        mCustomTintDrawableIds.add(resId);
    }

    /**
     * Add a drawable resource to which to apply the "tint transformation" technique.
     */
    public void addTintTransformationResourceId(int resId) {
        if (mCustomTransformationDrawableIds == null)
            mCustomTransformationDrawableIds = new ArrayList<Integer>();
        mCustomTransformationDrawableIds.add(resId);
    }

    /**
     * Get a reference to a resource that is equivalent to the one requested,
     * but with the accent color applied to it.
     */
    private InputStream getTintendResourceStream(int id, TypedValue value, int color) {
        checkInitialized();

        Bitmap bitmap = getBitmapFromResource(id, value);
        bitmap = BitmapUtils.applyColor(bitmap, color);
        return getStreamFromBitmap(bitmap);
    }

    /**
     * Get a reference to a resource that is equivalent to the one requested,
     * but changing the tint from the original red to the given color.
     */
    private InputStream getTintTransformationResourceStream(int id, TypedValue value, int color) {
        checkInitialized();

        Bitmap bitmap = getBitmapFromResource(id, value);
        bitmap = BitmapUtils.processTintTransformationMap(bitmap, color);
        return getStreamFromBitmap(bitmap);
    }

    private Bitmap getBitmapFromResource(int resId, TypedValue value) {
        InputStream original = super.openRawResource(resId, value);
        value.density = getDisplayMetrics().densityDpi;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inScaled = false;
        options.inScreenDensity = getDisplayMetrics().densityDpi;
        return BitmapFactory.decodeResourceStream(
                this, value, original,
                new Rect(), options);
    }

    private InputStream getStreamFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) { /* ignore */}

        return new ByteArrayInputStream(bitmapData);
    }


}
