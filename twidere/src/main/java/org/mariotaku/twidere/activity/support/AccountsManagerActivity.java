package org.mariotaku.twidere.activity.support;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.support.AccountsManagerFragment;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;

/**
 * Accounts manager
 * Created by mariotaku on 14/10/26.
 */
public class AccountsManagerActivity extends BaseActionBarActivity {

    private TintedStatusFrameLayout mMainContent;

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mMainContent = (TintedStatusFrameLayout) findViewById(R.id.main_content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_content_fragment);
        mMainContent.setOnFitSystemWindowsListener(this);
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_content, new AccountsManagerFragment());
        ft.commit();
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        super.onFitSystemWindows(insets);
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.findFragmentById(R.id.main_content);
        if (f instanceof IBaseFragment) {
            ((IBaseFragment) f).requestFitSystemWindows();
        }
    }
}
