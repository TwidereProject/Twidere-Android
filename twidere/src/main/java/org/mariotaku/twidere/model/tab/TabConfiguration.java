package org.mariotaku.twidere.model.tab;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.impl.FavoriteTimelineTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.HomeTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.InteractionsTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.MessagesTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.SearchTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.TrendsTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.UserListTimelineTabConfiguration;
import org.mariotaku.twidere.model.tab.impl.UserTimelineTabConfiguration;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

/**
 * Created by mariotaku on 2016/11/27.
 */

public abstract class TabConfiguration {

    public static final int FLAG_HAS_ACCOUNT = 0b0001;
    public static final int FLAG_ACCOUNT_REQUIRED = 0b0010;
    public static final int FLAG_ACCOUNT_MULTIPLE = 0b0100;
    public static final int FLAG_ACCOUNT_MUTABLE = 0b1000;

    @NonNull
    public abstract StringHolder getName();

    @NonNull
    public abstract DrawableHolder getIcon();

    @AccountFlags
    public abstract int getAccountFlags();

    public boolean isSingleTab() {
        return false;
    }

    public int getSortPosition() {
        return 0;
    }

    @Nullable
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return null;
    }

    @NonNull
    public abstract Class<? extends Fragment> getFragmentClass();

    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        return true;
    }

    public boolean readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        return false;
    }

    @IntDef(value = {FLAG_HAS_ACCOUNT, FLAG_ACCOUNT_REQUIRED, FLAG_ACCOUNT_MULTIPLE,
            FLAG_ACCOUNT_MUTABLE}, flag = true)
    protected @interface AccountFlags {

    }

    @NonNull
    public static List<Pair<String, TabConfiguration>> all() {
        List<Pair<String, TabConfiguration>> all = new ArrayList<>();
        for (String type : allTypes()) {
            all.add(new Pair<>(type, ofType(type)));
        }
        return all;
    }

    @NonNull
    public static String[] allTypes() {
        return new String[]{
                CustomTabType.HOME_TIMELINE,
                CustomTabType.NOTIFICATIONS_TIMELINE,
                CustomTabType.TRENDS_SUGGESTIONS,
                CustomTabType.DIRECT_MESSAGES,
                CustomTabType.FAVORITES,
                CustomTabType.USER_TIMELINE,
                CustomTabType.SEARCH_STATUSES,
                CustomTabType.LIST_TIMELINE
        };
    }

    @Nullable
    public static TabConfiguration ofType(@CustomTabType String type) {
        switch (type) {
            case CustomTabType.HOME_TIMELINE:
                return new HomeTabConfiguration();
            case CustomTabType.NOTIFICATIONS_TIMELINE:
                return new InteractionsTabConfiguration();
            case CustomTabType.DIRECT_MESSAGES:
                return new MessagesTabConfiguration();
            case CustomTabType.LIST_TIMELINE:
                return new UserListTimelineTabConfiguration();
            case CustomTabType.FAVORITES:
                return new FavoriteTimelineTabConfiguration();
            case CustomTabType.USER_TIMELINE:
                return new UserTimelineTabConfiguration();
            case CustomTabType.TRENDS_SUGGESTIONS:
                return new TrendsTabConfiguration();
            case CustomTabType.SEARCH_STATUSES:
                return new SearchTabConfiguration();
        }
        return null;
    }

    public static abstract class ExtraConfiguration {
        private final String key;
        private StringHolder title;
        @Nullable
        private StringHolder summary;
        @Nullable
        private StringHolder headerTitle;
        private int position;
        private boolean mutable;

        private Context context;
        private View view;

        protected ExtraConfiguration(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public StringHolder getTitle() {
            return title;
        }

        public void setTitle(StringHolder title) {
            this.title = title;
        }

        @Nullable
        public StringHolder getSummary() {
            return summary;
        }

        public void setSummary(@Nullable StringHolder summary) {
            this.summary = summary;
        }

        public ExtraConfiguration title(StringHolder title) {
            setTitle(title);
            return this;
        }

        public ExtraConfiguration summary(StringHolder summary) {
            setSummary(summary);
            return this;
        }

        public ExtraConfiguration title(@StringRes int titleRes) {
            setTitle(StringHolder.resource(titleRes));
            return this;
        }

        public ExtraConfiguration summary(@StringRes int summaryRes) {
            setSummary(StringHolder.resource(summaryRes));
            return this;
        }

        @Nullable
        public StringHolder getHeaderTitle() {
            return headerTitle;
        }

        public void setHeaderTitle(@Nullable StringHolder headerTitle) {
            this.headerTitle = headerTitle;
        }

        public ExtraConfiguration headerTitle(@Nullable StringHolder title) {
            setHeaderTitle(title);
            return this;
        }

        public ExtraConfiguration headerTitle(@StringRes int titleRes) {
            setHeaderTitle(StringHolder.resource(titleRes));
            return this;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isMutable() {
            return mutable;
        }

        public void setMutable(boolean mutable) {
            this.mutable = mutable;
        }

        public ExtraConfiguration mutable(boolean mutable) {
            setMutable(mutable);
            return this;
        }

        @NonNull
        public abstract View onCreateView(Context context, ViewGroup parent);

        @CallSuper
        public void onCreate(Context context) {
            this.context = context;
        }

        public final Context getContext() {
            return context;
        }

        public View getView() {
            return view;
        }

        @CallSuper
        public void onViewCreated(@NonNull Context context, @NonNull View view, @NonNull TabEditorDialogFragment fragment) {
            this.view = view;
        }

        public void onActivityResult(@NonNull final TabEditorDialogFragment fragment, int requestCode, int resultCode, @Nullable Intent data) {

        }

        public void onAccountSelectionChanged(@Nullable AccountDetails account) {

        }
    }

}
