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
import org.mariotaku.twidere.model.tab.conf.PlaceExtraConfiguration;
import org.mariotaku.twidere.model.tab.extra.TrendsTabExtras;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_PLACE;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_WOEID;

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
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED | FLAG_ACCOUNT_MUTABLE;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new PlaceExtraConfiguration(EXTRA_WOEID).title(R.string.trends_location).mutable(true),
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TrendsTabExtras extras = (TrendsTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_PLACE: {
                PlaceExtraConfiguration conf = (PlaceExtraConfiguration) extraConf;
                PlaceExtraConfiguration.Place place = conf.getValue();
                if (place != null) {
                    extras.setWoeId(place.getWoeId());
                    extras.setPlaceName(place.getName());
                } else {
                    extras.setWoeId(0);
                    extras.setPlaceName(null);
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TrendsTabExtras extras = (TrendsTabExtras) tab.getExtras();
        if (extras == null) return false;
        switch (extraConf.getKey()) {
            case EXTRA_PLACE: {
                final int woeId = extras.getWoeId();
                final String name = extras.getPlaceName();
                if (name != null) {
                    PlaceExtraConfiguration.Place place = new PlaceExtraConfiguration.Place(woeId, name);
                    ((PlaceExtraConfiguration) extraConf).setValue(place);
                } else {
                    ((PlaceExtraConfiguration) extraConf).setValue(null);
                }
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return TrendsSuggestionsFragment.class;
    }
}
