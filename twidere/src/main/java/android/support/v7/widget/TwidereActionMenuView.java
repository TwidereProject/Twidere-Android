package android.support.v7.widget;

import android.content.Context;
import android.support.v7.widget.android.support.v7.view.menu.TwidereActionMenuItemView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mariotaku on 16/3/18.
 */
public class TwidereActionMenuView extends ActionMenuView {
    public TwidereActionMenuView(Context context) {
        super(context);
    }

    public TwidereActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public View createActionMenuView(Context context, AttributeSet attrs) {
        return new TwidereActionMenuItemView(context, attrs);
    }
}
