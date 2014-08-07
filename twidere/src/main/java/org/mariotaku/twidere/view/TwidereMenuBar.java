package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuInflater;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.twidere.menu.TwidereMenuInflater;

/**
 * Created by mariotaku on 14-7-29.
 */
public class TwidereMenuBar extends MenuBar {
    public TwidereMenuBar(Context context) {
        super(context);
    }

    public TwidereMenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return new TwidereMenuInflater(getContext());
    }
}
