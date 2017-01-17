package org.mariotaku.twidere.model.tab.impl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.extension.model.AccountDetailsExtensionsKt;
import org.mariotaku.twidere.fragment.InteractionsTimelineFragment;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.BooleanHolder;
import org.mariotaku.twidere.model.tab.DrawableHolder;
import org.mariotaku.twidere.model.tab.StringHolder;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.model.tab.conf.BooleanExtraConfiguration;
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras;
import org.mariotaku.twidere.model.util.AccountUtils;

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
                new MentionsOnlyExtraConfiguration(EXTRA_MENTIONS_ONLY).title(R.string.mentions_only).mutable(true),
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

    private static class MentionsOnlyExtraConfiguration extends BooleanExtraConfiguration {

        private boolean valueBackup;

        MentionsOnlyExtraConfiguration(@NotNull String key) {
            super(key, new HasOfficialBooleanHolder());
        }

        @Override
        public void onAccountSelectionChanged(@Nullable AccountDetails account) {
            final boolean hasOfficial;
            if (account == null || account.dummy) {
                hasOfficial = AccountUtils.hasOfficialKeyAccount(getContext());
            } else {
                hasOfficial = AccountDetailsExtensionsKt.isOfficial(account, getContext());
            }
            ((HasOfficialBooleanHolder) getDefaultValue()).hasOfficial = hasOfficial;
            final View view = getView();
            final CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            final TextView titleView = (TextView) view.findViewById(android.R.id.title);
            final TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
            view.setEnabled(hasOfficial);
            titleView.setEnabled(hasOfficial);
            summaryView.setEnabled(hasOfficial);
            checkBox.setEnabled(hasOfficial);
            if (hasOfficial) {
                checkBox.setChecked(valueBackup);
                summaryView.setVisibility(View.GONE);
            } else {
                valueBackup = checkBox.isChecked();
                checkBox.setChecked(true);
                summaryView.setText(R.string.summary_interactions_not_available);
                summaryView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean getValue() {
            if (((HasOfficialBooleanHolder) getDefaultValue()).hasOfficial) {
                return super.getValue();
            }
            return valueBackup;
        }

        private static class HasOfficialBooleanHolder extends BooleanHolder implements Parcelable {

            private boolean hasOfficial;

            @Override
            public boolean createBoolean(Context context) {
                return hasOfficial;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {
            }

            public static final Creator<HasOfficialBooleanHolder> CREATOR = new Creator<HasOfficialBooleanHolder>() {
                @Override
                public HasOfficialBooleanHolder createFromParcel(Parcel in) {
                    return new HasOfficialBooleanHolder();
                }

                @Override
                public HasOfficialBooleanHolder[] newArray(int size) {
                    return new HasOfficialBooleanHolder[size];
                }
            };
        }
    }

}
