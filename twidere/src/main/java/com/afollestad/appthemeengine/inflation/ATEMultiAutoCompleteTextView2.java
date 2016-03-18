package com.afollestad.appthemeengine.inflation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.tagprocessors.ATEDefaultTags;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATEMultiAutoCompleteTextView2 extends AppCompatMultiAutoCompleteTextView implements ViewInterface, PostInflationApplier {

    public ATEMultiAutoCompleteTextView2(Context context) {
        super(context);
        init(context, null);
    }

    public ATEMultiAutoCompleteTextView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public ATEMultiAutoCompleteTextView2(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext, boolean waitForInflate) {
        super(context, attrs);
        mWaitForInflate = waitForInflate;
        init(context, keyContext);
    }

    private boolean mWaitForInflate;
    private ATEActivity mKeyContext;

    private void init(Context context, @Nullable ATEActivity keyContext) {
        ATEDefaultTags.process(this);
        if (mWaitForInflate) {
            mKeyContext = keyContext;
            ATE.addPostInflationView(this);
            return;
        }
        ATEViewUtil.init(keyContext, this, context);
    }

    @Override
    public boolean setsStatusBarColor() {
        return false;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }

    @Override
    public void postApply() {
        if(!mWaitForInflate) return;
        mWaitForInflate = false;
        ATEViewUtil.init(mKeyContext, this, getContext());
        mKeyContext = null;
    }
}
