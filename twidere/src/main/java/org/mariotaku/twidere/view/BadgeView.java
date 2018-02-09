package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14/11/16.
 */
public class BadgeView extends View {

    private final TextPaint mTextPaint;
    private String mText;
    private float mTextX, mTextY;
    private Rect mTextBounds;

    public BadgeView(Context context) {
        this(context, null);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeView);
        setColor(a.getColor(R.styleable.BadgeView_android_textColor, Color.WHITE));
        setText(a.getString(R.styleable.BadgeView_android_text));
        a.recycle();
        mTextPaint.setTextAlign(Align.CENTER);
        mTextBounds = new Rect();
    }


    public void setColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setText(String text) {
        mText = text;
        updateTextPosition();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        final int hPadding = (int) (Math.round(w * (Math.pow(2, 0.5f) - 1)) / 2);
        final int vPadding = (int) (Math.round(h * (Math.pow(2, 0.5f) - 1)) / 2);
        setPadding(hPadding, vPadding, hPadding, vPadding);
        updateTextPosition();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mTextBounds.isEmpty()) {
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
        }
    }

    private void updateTextPosition() {
        final int width = getWidth(), height = getHeight();
        if (width == 0 || height == 0) return;
        final float contentWidth = width - getPaddingLeft() - getPaddingRight();
        final float contentHeight = height - getPaddingTop() - getPaddingBottom();

        if (mText != null) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            final float scale = Math.min(contentWidth / mTextBounds.width(), contentHeight / mTextBounds.height());
            mTextPaint.setTextSize(Math.min(height / 2, mTextPaint.getTextSize() * scale));
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            mTextX = contentWidth / 2 + getPaddingLeft();
            mTextY = contentHeight / 2 + getPaddingTop() + mTextBounds.height() / 2;
        } else {
            mTextBounds.setEmpty();
        }
    }
}
