package org.mariotaku.twidere.activity.support;

import android.app.ActionBar;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.support.AccountsManagerFragment;

/**
 * Created by mariotaku on 14/10/26.
 */
public class AccountsManagerActivity extends BaseSupportActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_content_fragment);
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_content, new AccountsManagerFragment());
        ft.commit();
    }

    @Override
    public void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.findFragmentById(R.id.main_content);
        if (f instanceof IBaseFragment) {
            ((IBaseFragment) f).requestFitSystemWindows();
        }
    }
}
