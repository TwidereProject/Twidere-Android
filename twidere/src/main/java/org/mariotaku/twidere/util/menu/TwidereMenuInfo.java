package org.mariotaku.twidere.util.menu;

import android.view.ContextMenu.ContextMenuInfo;

/**
 * Created by mariotaku on 14/10/27.
 */
public class TwidereMenuInfo implements ContextMenuInfo {
    private final int highlightColor;
    private final boolean isHighlight;


    public TwidereMenuInfo(boolean isHighlight) {
        this(isHighlight, 0);
    }

    public TwidereMenuInfo(boolean isHighlight, int highlightColor) {
        this.isHighlight = isHighlight;
        this.highlightColor = highlightColor;
    }

    public int getHighlightColor(int def) {
        return highlightColor != 0 ? highlightColor : def;
    }

    public boolean isHighlight() {
        return isHighlight;
    }
}
