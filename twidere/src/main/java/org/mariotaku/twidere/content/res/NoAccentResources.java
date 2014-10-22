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


    public NoAccentResources(Context c, Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
    }



}
