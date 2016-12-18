package org.mariotaku.chameleon;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class Chameleon {
    /**
     * Created by mariotaku on 2016/12/18.
     */

    public static class Theme {
        int primaryColor;
        int accentColor;
        int toolbarColor;
        boolean toolbarColored;

        public int getAccentColor() {
            return accentColor;
        }

        public void setAccentColor(int accentColor) {
            this.accentColor = accentColor;
        }

        public int getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(int primaryColor) {
            this.primaryColor = primaryColor;
        }

        public int getToolbarColor() {
            return toolbarColor;
        }

        public void setToolbarColor(int toolbarColor) {
            this.toolbarColor = toolbarColor;
        }

        public boolean isToolbarColored() {
            return toolbarColored;
        }

        public void setToolbarColored(boolean toolbarColored) {
            this.toolbarColored = toolbarColored;
        }
    }

    /**
     * Created by mariotaku on 2016/12/18.
     */

    public interface Themeable {
        Theme getOverrideTheme();
    }
}
