package org.mariotaku.twidere.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.tab.argument.TabArguments;
import org.mariotaku.twidere.model.tab.extra.TabExtras;
import org.mariotaku.twidere.model.util.TabArgumentsFieldConverter;
import org.mariotaku.twidere.model.util.TabExtrasFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

/**
 * Created by mariotaku on 16/3/6.
 */
@CursorObject(valuesCreator = true)
public class Tab {
    @CursorField(value = Tabs._ID, excludeWrite = true)
    long id;

    @CursorField(Tabs.NAME)
    String name;

    @CursorField(Tabs.ICON)
    String icon;

    @CursorField(Tabs.TYPE)
    @CustomTabType
    String type;

    @CursorField(Tabs.POSITION)
    int position;

    @CursorField(value = Tabs.ARGUMENTS, converter = TabArgumentsFieldConverter.class)
    TabArguments arguments;

    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    TabExtras extras;

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

    public TabArguments getArguments() {
        return arguments;
    }

    public void setArguments(TabArguments arguments) {
        this.arguments = arguments;
    }

    public TabExtras getExtras() {
        return extras;
    }

    public void setExtras(TabExtras extras) {
        this.extras = extras;
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
}
