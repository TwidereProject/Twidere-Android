package org.mariotaku.twidere.model;

import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.tab.argument.TabArguments;
import org.mariotaku.twidere.model.tab.argument.TextQueryArguments;
import org.mariotaku.twidere.model.tab.argument.UserArguments;
import org.mariotaku.twidere.model.tab.argument.UserListArguments;
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras;
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras;
import org.mariotaku.twidere.model.tab.extra.TabExtras;
import org.mariotaku.twidere.model.util.TabArgumentsFieldConverter;
import org.mariotaku.twidere.model.util.TabExtrasFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

/**
 * Created by mariotaku on 16/3/6.
 */
@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
public class Tab {
    @CursorField(value = Tabs._ID, excludeWrite = true)
    @JsonField(name = "id")
    long id;

    @CursorField(Tabs.NAME)
    @JsonField(name = "name")
    String name;

    @CursorField(Tabs.ICON)
    @JsonField(name = "icon")
    String icon;

    @CursorField(Tabs.TYPE)
    @JsonField(name = "type")
    @CustomTabType
    String type;

    @CursorField(Tabs.POSITION)
    @JsonField(name = "position")
    int position;

    @Nullable
    @CursorField(value = Tabs.ARGUMENTS, converter = TabArgumentsFieldConverter.class)
    TabArguments arguments;

    @Nullable
    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    TabExtras extras;

    @Nullable
    @JsonField(name = "arguments")
    InternalArguments internalArguments;

    @Nullable
    @JsonField(name = "extras")
    InternalExtras internalExtras;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @CustomTabType
    public String getType() {
        return type;
    }

    public void setType(@CustomTabType String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Nullable
    public TabArguments getArguments() {
        return arguments;
    }

    public void setArguments(@Nullable TabArguments arguments) {
        this.arguments = arguments;
    }

    @Nullable
    public TabExtras getExtras() {
        return extras;
    }

    public void setExtras(@Nullable TabExtras extras) {
        this.extras = extras;
    }


    @OnPreJsonSerialize
    void beforeJsonSerialize() {
        internalArguments = InternalArguments.from(arguments);
        internalExtras = InternalExtras.from(extras);
    }

    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (internalArguments != null) {
            arguments = internalArguments.getArguments();
        }
        if (internalExtras != null) {
            extras = internalExtras.getExtras();
        }
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", type='" + type + '\'' +
                ", position=" + position +
                ", arguments=" + arguments +
                ", extras=" + extras +
                '}';
    }

    @JsonObject
    static class InternalArguments {
        @JsonField(name = "base")
        TabArguments base;
        @JsonField(name = "text_query")
        TextQueryArguments textQuery;
        @JsonField(name = "user")
        UserArguments user;
        @JsonField(name = "user_list")
        UserListArguments userList;

        public static InternalArguments from(TabArguments arguments) {
            if (arguments == null) return null;
            InternalArguments result = new InternalArguments();
            if (arguments instanceof TextQueryArguments) {
                result.textQuery = (TextQueryArguments) arguments;
            } else if (arguments instanceof UserArguments) {
                result.user = (UserArguments) arguments;
            } else if (arguments instanceof UserListArguments) {
                result.userList = (UserListArguments) arguments;
            } else {
                result.base = arguments;
            }
            return result;
        }

        public TabArguments getArguments() {
            if (userList != null) {
                return userList;
            } else if (user != null) {
                return user;
            } else if (textQuery != null) {
                return textQuery;
            } else {
                return base;
            }
        }
    }

    @JsonObject
    static class InternalExtras {

        @JsonField(name = "base")
        TabExtras base;
        @JsonField(name = "interactions")
        InteractionsTabExtras interactions;
        @JsonField(name = "home")
        HomeTabExtras home;

        public static InternalExtras from(TabExtras extras) {
            if (extras == null) return null;
            InternalExtras result = new InternalExtras();
            if (extras instanceof InteractionsTabExtras) {
                result.interactions = (InteractionsTabExtras) extras;
            }
            if (extras instanceof HomeTabExtras) {
                result.home = (HomeTabExtras) extras;
            } else {
                result.base = extras;
            }
            return result;
        }

        public TabExtras getExtras() {
            if (interactions != null) {
                return interactions;
            } else if (home != null) {
                return home;
            } else {
                return base;
            }
        }
    }
}
