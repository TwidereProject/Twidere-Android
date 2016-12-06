package org.mariotaku.twidere.model.tab.impl;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.TrendsSuggestionsFragment;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class TrendsTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.trends);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.HASHTAG;
    }

    @AccountFlags
    @Override
    public int getAccountFlags() {
        return FLAG_HAS_ACCOUNT;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return TrendsSuggestionsFragment.class;
    }
}
