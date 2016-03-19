package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.tagprocessors.TagProcessor;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 16/3/19.
 */
public class FontFamilyTagProcessor extends TagProcessor {
    public static final String TAG = "font_family";
    private String mFontFamily;

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return view instanceof TextView;
    }

    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view,
                        @NonNull String suffix) {
        TextView textView = (TextView) view;
        final Typeface defTypeface = textView.getTypeface();
        Typeface typeface = ThemeUtils.getUserTypeface(context, mFontFamily, defTypeface);
        if (defTypeface != typeface) {
            textView.setTypeface(typeface);
        }
    }

    public void setFontFamily(String fontFamily) {
        mFontFamily = fontFamily;
    }
}
