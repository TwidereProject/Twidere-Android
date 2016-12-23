package org.mariotaku.chameleon.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
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

public class ChameleonTextView extends AppCompatTextView implements ChameleonView {
    public ChameleonTextView(Context context) {
        super(context);
    }

    public ChameleonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        return Appearance.create(this, context, attributeSet, theme);
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance.apply(this, (Appearance) appearance);
    }

    public static class Appearance implements ChameleonView.Appearance {
        private int textColor;
        private int linkTextColor;
        private int backgroundColor;


        public int getLinkTextColor() {
            return linkTextColor;
        }

        public void setLinkTextColor(int linkTextColor) {
            this.linkTextColor = linkTextColor;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public static void apply(TextView view, Appearance appearance) {
            view.setLinkTextColor(appearance.getLinkTextColor());
            view.setTextColor(appearance.getTextColor());
            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(appearance.getBackgroundColor()));
            setCursorTint(view, appearance.getBackgroundColor());
            setHandlerTint(view, appearance.getBackgroundColor());
            view.setHighlightColor(ChameleonUtils.adjustAlpha(appearance.getBackgroundColor(), 0.4f));
        }

        public static Appearance create(TextView view, Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
            Appearance appearance = new Appearance();
            ChameleonTypedArray a = ChameleonTypedArray.obtain(context, attributeSet,
                    R.styleable.ChameleonEditText, theme);
            appearance.setTextColor(a.getColor(R.styleable.ChameleonEditText_android_textColor, view.getCurrentTextColor()));
            appearance.setLinkTextColor(a.getColor(R.styleable.ChameleonEditText_android_textColorLink, theme.getTextColorLink()));
            appearance.setBackgroundColor(a.getColor(R.styleable.ChameleonEditText_backgroundTint, theme.getColorAccent()));
            a.recycle();
            return appearance;
        }

        public static void setCursorTint(@NonNull TextView textView, @ColorInt int color) {
            try {
                int mCursorDrawableRes = getIntField(TextView.class, textView, "mCursorDrawableRes");
                Object editor = getField(TextView.class, textView, "mEditor");
                if (editor != null) {
                    Drawable[] drawables = new Drawable[2];
                    drawables[0] = ContextCompat.getDrawable(textView.getContext(), mCursorDrawableRes);
                    drawables[0] = ChameleonUtils.createTintedDrawable(drawables[0], color);
                    drawables[1] = ContextCompat.getDrawable(textView.getContext(), mCursorDrawableRes);
                    drawables[1] = ChameleonUtils.createTintedDrawable(drawables[1], color);
                    setField(Class.forName("android.widget.Editor"), editor, "mCursorDrawable", drawables);
                }
            } catch (Exception e) {
                // Ignore
                e.printStackTrace();
            }
        }

        public static void setHandlerTint(@NonNull TextView textView, @ColorInt int color) {
            try {
                int mTextSelectHandleLeftRes = getIntField(TextView.class, textView, "mTextSelectHandleLeftRes");
                int mTextSelectHandleRightRes = getIntField(TextView.class, textView, "mTextSelectHandleRightRes");
                int mTextSelectHandleRes = getIntField(TextView.class, textView, "mTextSelectHandleRes");
                Object editor = getField(TextView.class, textView, "mEditor");
                if (editor != null) {
                    final Class<?> editorClass = Class.forName("android.widget.Editor");
                    setField(editorClass, editor, "mSelectHandleLeft", ChameleonUtils.createTintedDrawable(ContextCompat.getDrawable(textView.getContext(), mTextSelectHandleLeftRes), color));
                    setField(Class.forName("android.widget.Editor"), editor, "mSelectHandleRight", ChameleonUtils.createTintedDrawable(ContextCompat.getDrawable(textView.getContext(), mTextSelectHandleRightRes), color));
                    setField(Class.forName("android.widget.Editor"), editor, "mSelectHandleCenter", ChameleonUtils.createTintedDrawable(ContextCompat.getDrawable(textView.getContext(), mTextSelectHandleRes), color));
                }
            } catch (Exception e) {
                // Ignore
            }

        }

        private static Object getField(@NonNull Class<?> cls, @NonNull Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        }

        private static int getIntField(@NonNull Class<?> cls, @NonNull Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(object);
        }

        private static void setField(@NonNull Class<?> cls, @NonNull Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        }
    }
}
