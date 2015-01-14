package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.menucomponent.widget.MenuBar.MenuBarListener;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 14-7-29.
 */
public class TwidereMenuBar extends MenuBar implements MenuBarListener, Constants {
    private final int mItemColor, mPopupItemColor, mHighlightColor;
    private OnMenuItemClickListener mListener;

    public TwidereMenuBar(Context context) {
        this(context, null);
    }

    public TwidereMenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(getItemViewContext());
        final int popupItemBackgroundColor = ThemeUtils.getThemeBackgroundColor(getPopupContext());
        final Resources resources = getResources();
        final int colorDark = resources.getColor(R.color.action_icon_dark);
        final int colorLight = resources.getColor(R.color.action_icon_light);
        mItemColor = Utils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight);
        mPopupItemColor = Utils.getContrastYIQ(popupItemBackgroundColor, colorDark, colorLight);
        mHighlightColor = isInEditMode() ? 0 : ThemeUtils.getUserAccentColor(getContext());
        setMenuBarListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPopupStyleAttribute(android.R.attr.actionOverflowMenuStyle);
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onPreShowMenu(Menu menu) {
        ThemeUtils.applyColorFilterToMenuIcon(menu, mItemColor, mPopupItemColor, Mode.SRC_ATOP,
                MENU_GROUP_STATUS_SHARE, MENU_GROUP_STATUS_EXTENSION);
    }

    @Override
    public void onPostShowMenu(Menu menu) {
        final View overflowItemView = getOverflowItemView();
        if (overflowItemView instanceof ImageView) {
            ((ImageView) overflowItemView).setColorFilter(mItemColor, Mode.SRC_ATOP);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mListener != null && mListener.onMenuItemClick(item);
    }
}
