package org.mariotaku.chameleon;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonActivity extends AppCompatActivity implements Chameleon.Themeable {
    private Chameleon mChameleon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mChameleon = Chameleon.getInstance(this, onCreateAppearanceCreator());
        mChameleon.preApply();
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mChameleon.postApply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChameleon.invalidateActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mChameleon.cleanUp();
        }
    }

    @Override
    @CallSuper
    public boolean onCreateOptionsMenu(Menu menu) {
        mChameleon.themeOverflow();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @CallSuper
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean result = super.onPrepareOptionsMenu(menu);
        mChameleon.themeActionMenu(menu);
        return result;
    }

    @Override
    public Chameleon.Theme getOverrideTheme() {
        return null;
    }

    @Nullable
    protected Chameleon.AppearanceCreator onCreateAppearanceCreator() {
        return null;
    }
}
