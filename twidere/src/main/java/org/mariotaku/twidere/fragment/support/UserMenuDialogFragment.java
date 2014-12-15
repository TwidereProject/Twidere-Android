package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import org.mariotaku.twidere.activity.support.MenuDialogFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.Utils.addIntentToMenu;

public class UserMenuDialogFragment extends MenuDialogFragment {

    @Override
    protected void onCreateMenu(final MenuInflater inflater, final Menu menu) {
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Bundle args = getArguments();
        final ParcelableUser user = args.getParcelable(EXTRA_USER);
        onPrepareItemMenu(menu, user);
        final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
        final Bundle extensionsExtras = new Bundle();
        extensionsExtras.putParcelable(EXTRA_USER, user);
        extensionsIntent.putExtras(extensionsExtras);
        addIntentToMenu(getThemedContext(), menu, extensionsIntent);
    }

    protected void onPrepareItemMenu(final Menu menu, final ParcelableUser user) {

    }

}
