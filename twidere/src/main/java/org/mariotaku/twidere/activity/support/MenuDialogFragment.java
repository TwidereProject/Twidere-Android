package org.mariotaku.twidere.activity.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.internal.menu.MenuAdapter;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.menu.TwidereMenuInflater;
import org.mariotaku.twidere.util.ThemeUtils;

public abstract class MenuDialogFragment extends BaseSupportDialogFragment implements OnItemClickListener {

    private MenuAdapter mAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getThemedContext();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mAdapter = new MenuAdapter(context);
        final ListView listView = new ListView(context);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        builder.setView(listView);
        final Menu menu = MenuUtils.createMenu(context);
        onCreateMenu(new TwidereMenuInflater(context), menu);
        final int itemColor = ThemeUtils.getThemeForegroundColor(context);
        final int highlightColor = ThemeUtils.getUserAccentColor(context);
        ThemeUtils.applyColorFilterToMenuIcon(menu, itemColor, highlightColor, Mode.SRC_ATOP);
        mAdapter.setMenu(menu);
        return builder.create();
    }

    public Context getThemedContext() {
        return getActivity();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Fragment parentFragment = getParentFragment();
        final MenuItem item = (MenuItem) parent.getItemAtPosition(position);
        if (item.hasSubMenu()) {
            mAdapter.setMenu(item.getSubMenu());
        } else if (parentFragment instanceof OnMenuItemClickListener) {
            ((OnMenuItemClickListener) parentFragment).onMenuItemClick(item);
            dismiss();
        }
    }

    protected abstract void onCreateMenu(TwidereMenuInflater inflater, Menu menu);

}
