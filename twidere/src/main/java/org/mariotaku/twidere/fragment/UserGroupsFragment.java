package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.UserGroupsLoader;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

/**
 * Created by mariotaku on 16/3/9.
 */
public class UserGroupsFragment extends ParcelableGroupsFragment {
    @Override
    protected Loader<List<ParcelableGroup>> onCreateUserListsLoader(Context context, Bundle args, boolean fromUser) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String userId = args.getString(EXTRA_USER_ID);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        return new UserGroupsLoader(context, accountKey, userId, screenName, getData());
    }

}
