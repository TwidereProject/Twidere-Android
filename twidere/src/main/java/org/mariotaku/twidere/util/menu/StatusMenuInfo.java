package org.mariotaku.twidere.util.menu;

import android.view.ContextMenu.ContextMenuInfo;

/**
 * Created by mariotaku on 14/10/27.
 */
public class StatusMenuInfo implements ContextMenuInfo {
    private final boolean highlight;

    public StatusMenuInfo(boolean highlight) {
        this.highlight = highlight;
    }

    public boolean isHighlight() {
        return highlight;
    }
}
