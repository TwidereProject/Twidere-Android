package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.argument.UserArguments;
import org.mariotaku.twidere.model.tab.conf.UserExtraConfiguration;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER;
import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class FavoriteTimelineTabConfiguration extends TabConfiguration {
    final static StringHolder TAB_NAME = new StringHolder() {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        @Override
        public String createString(Context context) {
            if (DependencyHolder.Companion.get(context).preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
                return context.getString(R.string.title_favorites);
            }
            return context.getString(R.string.title_likes);
        }
    };

    @NonNull
    @Override
    public StringHolder getName() {
        return TAB_NAME;
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.FAVORITE;
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
        return UserFavoritesFragment.class;
    }
}
