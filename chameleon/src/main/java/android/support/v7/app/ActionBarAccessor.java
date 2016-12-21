package android.support.v7.app;

import android.support.v7.widget.DecorToolbar;

/**
 * Created by mariotaku on 2016/12/21.
 */

public class ActionBarAccessor {
    public static DecorToolbar getDecorToolbar(ActionBar actionBar) {
        if (actionBar instanceof WindowDecorActionBar) {
            return ((WindowDecorActionBar) actionBar).mDecorToolbar;
        } else if (actionBar instanceof ToolbarActionBar) {
            return ((ToolbarActionBar) actionBar).mDecorToolbar;
        }
        return null;
    }
}
