package org.mariotaku.twidere.fragment.iface;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by mariotaku on 16/3/16.
 */
public interface IToolBarSupportFragment {

    Toolbar getToolbar();

    float getControlBarOffset();

    void setControlBarOffset(float offset);

    int getControlBarHeight();

    boolean setupWindow(FragmentActivity activity);
}
