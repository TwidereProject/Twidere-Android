package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.argument.UserArguments;
import org.mariotaku.twidere.model.tab.conf.UserExtraConfiguration;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class UserTimelineTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.users_statuses);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.USER;
    }

    @AccountFlags
    @Override
    public int getAccountFlags() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new UserExtraConfiguration(EXTRA_USER).title(R.string.title_user).headerTitle(R.string.title_user)
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        UserArguments arguments = (UserArguments) tab.getArguments();
        assert arguments != null;
        switch (extraConf.getKey()) {
            case EXTRA_USER: {
                final ParcelableUser user = ((UserExtraConfiguration) extraConf).getValue();
                if (user == null) return false;
                arguments.setUserKey(user.key);
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return UserTimelineFragment.class;
    }
}
