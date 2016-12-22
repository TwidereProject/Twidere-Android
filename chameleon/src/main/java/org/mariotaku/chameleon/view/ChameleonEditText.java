package org.mariotaku.chameleon.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;
import org.mariotaku.chameleon.internal.ChameleonTypedArray;

import java.lang.reflect.Field;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonEditText extends AppCompatEditText implements ChameleonView {
    public ChameleonEditText(Context context) {
        super(context);
    }

    public ChameleonEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        return Appearance.create(context, attributeSet, theme);
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final Appearance a = (Appearance) appearance;
        Appearance.apply(this, a);
    }

    public static class Appearance extends ChameleonTextView.Appearance {
        private int backgroundColor;

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public static void apply(TextView view, Appearance appearance) {
            view.setLinkTextColor(appearance.getLinkTextColor());
            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(appearance.getBackgroundColor()));
            setCursorTint(view, appearance.getBackgroundColor());
        }

        public static Appearance create(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
            Appearance appearance = new Appearance();
            ChameleonTypedArray a = ChameleonTypedArray.obtain(context, attributeSet,
                    R.styleable.ChameleonEditText, theme);
            appearance.setLinkTextColor(a.getColor(R.styleable.ChameleonEditText_android_textColorLink, theme.getTextColorLink()));
            appearance.setBackgroundColor(a.getColor(R.styleable.ChameleonEditText_backgroundTint, theme.getColorAccent()));
            a.recycle();
            return appearance;
        }


        public static void setCursorTint(@NonNull TextView textView, @ColorInt int color) {
            try {
                Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                fCursorDrawableRes.setAccessible(true);
                int mCursorDrawableRes = fCursorDrawableRes.getInt(textView);
                Field fEditor = TextView.class.getDeclaredField("mEditor");
                fEditor.setAccessible(true);
                Object editor = fEditor.get(textView);
                Class<?> clazz = editor.getClass();
                Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
                fCursorDrawable.setAccessible(true);
                Drawable[] drawables = new Drawable[2];
                drawables[0] = ContextCompat.getDrawable(textView.getContext(), mCursorDrawableRes);
                drawables[0] = ChameleonUtils.createTintedDrawable(drawables[0], color);
                drawables[1] = ContextCompat.getDrawable(textView.getContext(), mCursorDrawableRes);
                drawables[1] = ChameleonUtils.createTintedDrawable(drawables[1], color);
                fCursorDrawable.set(editor, drawables);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
