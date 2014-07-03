package org.mariotaku.twidere.activity.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.internal.menu.MenuAdapter;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.util.ThemeUtils;

public abstract class MenuDialogFragment extends BaseSupportDialogFragment implements OnItemClickListener {

	private Context mThemedContext;

	public Context getThemedContext() {
		if (mThemedContext != null) return mThemedContext;
		final FragmentActivity activity = getActivity();
		final int themeRes, accentColor;
		if (activity instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) activity).getThemeResourceId();
			accentColor = ((IThemedActivity) activity).getThemeColor();
		} else {
			themeRes = ThemeUtils.getSettingsThemeResource(activity);
			accentColor = ThemeUtils.getUserThemeColor(activity);
		}
		return mThemedContext = ThemeUtils.getThemedContextForActionIcons(activity, themeRes, accentColor);
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context context = getThemedContext();
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final MenuAdapter adapter = new MenuAdapter(context);
		final ListView listView = new ListView(context);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		builder.setView(listView);
		final Menu menu = MenuUtils.createMenu(context);
		onCreateMenu(new MenuInflater(context), menu);
		adapter.setMenu(menu);
		return builder.create();
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Fragment parentFragment = getParentFragment();
		final MenuItem item = (MenuItem) parent.getItemAtPosition(position);
		if (item.hasSubMenu()) {

		} else if (parentFragment instanceof OnMenuItemClickListener) {
			((OnMenuItemClickListener) parentFragment).onMenuItemClick(item);
			dismiss();
		}
	}

	protected abstract void onCreateMenu(MenuInflater inflater, Menu menu);

}
