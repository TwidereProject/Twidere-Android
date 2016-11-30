package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.TrendsSuggestionsFragment;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.argument.TextQueryArguments;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_QUERY;

/**
 * Created by mariotaku on 2016/11/27.
 */

public class SearchTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.search);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.SEARCH;
    }

    @AccountRequirement
    @Override
    public int getAccountRequirement() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new StringExtraConfiguration(EXTRA_QUERY, null).maxLines(1).title(R.string.search_statuses).headerTitle(R.string.query)
        };
    }

    @Override
    public void applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TextQueryArguments arguments = (TextQueryArguments) tab.getArguments();
        assert arguments != null;
        switch (extraConf.getKey()) {
            case EXTRA_QUERY: {
                arguments.setQuery(((StringExtraConfiguration) extraConf).getValue());
                break;
            }
        }
    }

    @Override
    public void readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TextQueryArguments arguments = (TextQueryArguments) tab.getArguments();
        if (arguments == null) return;
        switch (extraConf.getKey()) {
            case EXTRA_QUERY: {
                ((StringExtraConfiguration) extraConf).setValue(arguments.getQuery());
                break;
            }
        }
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return TrendsSuggestionsFragment.class;
    }
}
