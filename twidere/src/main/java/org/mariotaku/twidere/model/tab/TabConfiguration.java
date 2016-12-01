package org.mariotaku.twidere.model.tab;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.impl.DMTabConfiguration;
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
            case CustomTabType.DIRECT_MESSAGES_NEXT:
                return new DMTabConfiguration();
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
        private StringHolder headerTitle;
        private int position;
        private boolean mutable;
        private Context context;

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

        public ExtraConfiguration title(StringHolder title) {
            setTitle(title);
            return this;
        }

        public ExtraConfiguration title(@StringRes int titleRes) {
            setTitle(StringHolder.resource(titleRes));
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

        public void onViewCreated(@NonNull Context context, @NonNull View view, @NonNull TabEditorDialogFragment fragment) {

        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }
    }

    public static class BooleanExtraConfiguration extends ExtraConfiguration {

        private final boolean def;
        private CheckBox checkBox;

        public BooleanExtraConfiguration(String key, boolean def) {
            super(key);
            this.def = def;
        }

        @NonNull
        @Override
        public View onCreateView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false);
        }

        @Override
        public void onViewCreated(@NonNull Context context, @NonNull View view, @NonNull TabEditorDialogFragment fragment) {
            final TextView titleView = (TextView) view.findViewById(android.R.id.title);
            titleView.setText(getTitle().createString(context));

            checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(def);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox.toggle();
                }
            });
        }

        public void setValue(boolean value) {
            checkBox.setChecked(value);
        }

        public boolean getValue() {
            return checkBox.isChecked();
        }
    }

    public static class StringExtraConfiguration extends ExtraConfiguration {

        private final String def;
        private int maxLines;

        private EditText editText;

        public StringExtraConfiguration(String key, String def) {
            super(key);
            this.def = def;
        }

        @NonNull
        @Override
        public View onCreateView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_text, parent, false);
        }

        @Override
        public void onViewCreated(@NonNull Context context, @NonNull View view, @NonNull final TabEditorDialogFragment fragment) {
            editText = (EditText) view.findViewById(R.id.editText);
            editText.setHint(getTitle().createString(context));
            editText.setText(def);
        }

        public StringExtraConfiguration maxLines(int maxLines) {
            setMaxLines(maxLines);
            return this;
        }

        public void setMaxLines(int maxLines) {
            this.maxLines = maxLines;
        }

        public int getMaxLines() {
            return maxLines;
        }

        public String getValue() {
            return editText.getText().toString();
        }

        public void setValue(String value) {
            editText.setText(value);
        }
    }

}
