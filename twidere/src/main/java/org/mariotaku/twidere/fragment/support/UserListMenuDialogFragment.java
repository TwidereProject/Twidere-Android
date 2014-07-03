package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.addIntentToMenu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.MenuDialogFragment;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.Utils;

public class UserListMenuDialogFragment extends MenuDialogFragment {

	@Override
	protected void onCreateMenu(final MenuInflater inflater, final Menu menu) {
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments();
		final ParcelableUserList user = args.getParcelable(EXTRA_USER_LIST);
		inflater.inflate(R.menu.action_user_list, menu);
		onPrepareItemMenu(menu, user);
		final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
		final Bundle extensionsExtras = new Bundle();
		extensionsExtras.putParcelable(EXTRA_USER_LIST, user);
		extensionsIntent.putExtras(extensionsExtras);
		addIntentToMenu(getThemedContext(), menu, extensionsIntent);
		final boolean longclickToOpenMenu = prefs.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false);
		Utils.setMenuItemAvailability(menu, MENU_MULTI_SELECT, longclickToOpenMenu);
	}

	protected void onPrepareItemMenu(final Menu menu, final ParcelableUserList userList) {
		if (userList == null) return;
		final boolean isMyList = userList.user_id == userList.account_id;
		Utils.setMenuItemAvailability(menu, MENU_ADD, isMyList);
		Utils.setMenuItemAvailability(menu, MENU_DELETE, isMyList);
	}

}
