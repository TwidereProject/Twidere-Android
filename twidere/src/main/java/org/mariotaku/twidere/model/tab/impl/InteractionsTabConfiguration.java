package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.InteractionsTimelineFragment;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_MENTIONS_ONLY;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_MY_FOLLOWING_ONLY;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class InteractionsTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.interactions);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.NOTIFICATIONS;
    }

    @AccountFlags
    @Override
    public int getAccountFlags() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_MULTIPLE | FLAG_ACCOUNT_MUTABLE;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new BooleanExtraConfiguration(EXTRA_MY_FOLLOWING_ONLY, false).title(R.string.following_only).mutable(true),
                new BooleanExtraConfiguration(EXTRA_MENTIONS_ONLY, false).title(R.string.mentions_only).mutable(true),
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final InteractionsTabExtras extras = (InteractionsTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_MY_FOLLOWING_ONLY: {
                extras.setMyFollowingOnly(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_MENTIONS_ONLY: {
                extras.setMentionsOnly(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
        }
        return true;
    }

    @Override
    public boolean readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final InteractionsTabExtras extras = (InteractionsTabExtras) tab.getExtras();
        if (extras == null) return false;
        switch (extraConf.getKey()) {
            case EXTRA_MY_FOLLOWING_ONLY: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isMyFollowingOnly());
                break;
            }
            case EXTRA_MENTIONS_ONLY: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isMentionsOnly());
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return InteractionsTimelineFragment.class;
    }
}
