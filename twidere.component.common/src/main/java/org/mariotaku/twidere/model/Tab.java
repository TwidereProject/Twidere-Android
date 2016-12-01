package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

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
@ParcelablePlease(allFields = false)
@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
public class Tab implements Parcelable {
    @CursorField(value = Tabs._ID, excludeWrite = true)
    @JsonField(name = "id")
    @ParcelableThisPlease
    long id;

    @CursorField(Tabs.NAME)
    @JsonField(name = "name")
    @ParcelableThisPlease
    String name;

    @CursorField(Tabs.ICON)
    @JsonField(name = "icon")
    @ParcelableThisPlease
    String icon;

    @CursorField(Tabs.TYPE)
    @JsonField(name = "type")
    @CustomTabType
    @ParcelableThisPlease
    String type;

    @CursorField(Tabs.POSITION)
    @JsonField(name = "position")
    @ParcelableThisPlease
    int position;

    @Nullable
    @CursorField(value = Tabs.ARGUMENTS, converter = TabArgumentsFieldConverter.class)
    TabArguments arguments;

    @Nullable
    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    TabExtras extras;

    @Nullable
    @JsonField(name = "arguments")
    @ParcelableThisPlease
    InternalArguments internalArguments;

    @Nullable
    @JsonField(name = "extras")
    @ParcelableThisPlease
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
        if (arguments == null && internalArguments != null) {
            arguments = internalArguments.getArguments();
        }
        return arguments;
    }

    public void setArguments(@Nullable TabArguments arguments) {
        this.arguments = arguments;
        this.internalArguments = InternalArguments.from(arguments);
    }

    @Nullable
    public TabExtras getExtras() {
        if (extras == null && internalExtras != null) {
            extras = internalExtras.getExtras();
        }
        return extras;
    }

    public void setExtras(@Nullable TabExtras extras) {
        this.extras = extras;
        this.internalExtras = InternalExtras.from(extras);
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

    @CustomTabType
    public static String getTypeAlias(String key) {
        if (key == null) return null;
        switch (key) {
            case "mentions_timeline":
            case "activities_about_me":
                return CustomTabType.NOTIFICATIONS_TIMELINE;
        }
        return key;
    }

    @ParcelablePlease(allFields = false)
    @JsonObject
    static class InternalArguments implements Parcelable {
        @JsonField(name = "base")
        TabArguments base;
        @JsonField(name = "text_query")
        @ParcelableThisPlease
        TextQueryArguments textQuery;
        @JsonField(name = "user")
        @ParcelableThisPlease
        UserArguments user;
        @JsonField(name = "user_list")
        @ParcelableThisPlease
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Tab$InternalArgumentsParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<InternalArguments> CREATOR = new Creator<InternalArguments>() {
            public InternalArguments createFromParcel(Parcel source) {
                InternalArguments target = new InternalArguments();
                Tab$InternalArgumentsParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public InternalArguments[] newArray(int size) {
                return new InternalArguments[size];
            }
        };
    }

    @ParcelablePlease(allFields = false)
    @JsonObject
    static class InternalExtras implements Parcelable {

        @JsonField(name = "base")
        TabExtras base;
        @JsonField(name = "interactions")
        @ParcelableThisPlease
        InteractionsTabExtras interactions;
        @JsonField(name = "home")
        @ParcelableThisPlease
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Tab$InternalExtrasParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<InternalExtras> CREATOR = new Creator<InternalExtras>() {
            public InternalExtras createFromParcel(Parcel source) {
                InternalExtras target = new InternalExtras();
                Tab$InternalExtrasParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public InternalExtras[] newArray(int size) {
                return new InternalExtras[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TabParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        public Tab createFromParcel(Parcel source) {
            Tab target = new Tab();
            TabParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };
}
