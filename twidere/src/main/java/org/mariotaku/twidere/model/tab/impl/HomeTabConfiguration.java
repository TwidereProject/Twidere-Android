package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.conf.BooleanExtraConfiguration;
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_HIDE_QUOTES;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_HIDE_REPLIES;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_HIDE_RETWEETS;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class HomeTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.title_home);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.HOME;
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
                new BooleanExtraConfiguration(EXTRA_HIDE_RETWEETS, false).title(R.string.hide_retweets).mutable(true),
                new BooleanExtraConfiguration(EXTRA_HIDE_QUOTES, false).title(R.string.hide_quotes).mutable(true),
                new BooleanExtraConfiguration(EXTRA_HIDE_REPLIES, false).title(R.string.hide_replies).mutable(true),
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final HomeTabExtras extras = (HomeTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_HIDE_RETWEETS: {
                extras.setHideRetweets(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_HIDE_QUOTES: {
                extras.setHideQuotes(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_HIDE_REPLIES: {
                extras.setHideReplies(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
        }
        return true;
    }

    @Override
    public boolean readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final HomeTabExtras extras = (HomeTabExtras) tab.getExtras();
        if (extras == null) return false;
        switch (extraConf.getKey()) {
            case EXTRA_HIDE_RETWEETS: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideRetweets());
                break;
            }
            case EXTRA_HIDE_QUOTES: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideQuotes());
                break;
            }
            case EXTRA_HIDE_REPLIES: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideReplies());
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return HomeTimelineFragment.class;
    }
}
