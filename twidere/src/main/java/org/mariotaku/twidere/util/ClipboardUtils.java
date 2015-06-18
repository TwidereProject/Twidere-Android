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

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.text.style.ImageSpan;

public final class ClipboardUtils {

    public static boolean setText(final Context context, final CharSequence text) {
        if (context == null) return false;
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(text, text));
        return true;
    }

    @Nullable
    public static String getImageUrl(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return null;
        return ClipboardUtilsAPI16.getImageUrl(context);
    }

    private static class ClipboardUtilsAPI16 {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static String getImageUrl(final Context context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return null;
            final ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            final ClipData primaryClip = cm.getPrimaryClip();
            if (primaryClip.getItemCount() > 0) {
                final ClipData.Item item = primaryClip.getItemAt(0);
                final CharSequence styledText = item.coerceToStyledText(context);
                if (styledText instanceof Spanned) {
                    final Spanned spanned = (Spanned) styledText;
                    final ImageSpan[] imageSpans = spanned.getSpans(0, spanned.length(), ImageSpan.class);
                    if (imageSpans.length == 1) return imageSpans[0].getSource();
                }
            }
            return null;
        }
    }
}
