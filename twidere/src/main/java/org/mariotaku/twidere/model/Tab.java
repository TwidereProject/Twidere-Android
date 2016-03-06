package org.mariotaku.twidere.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.tab.Arguments;
import org.mariotaku.twidere.model.tab.Extras;
import org.mariotaku.twidere.model.util.TabArgumentsFieldConverter;
import org.mariotaku.twidere.model.util.TabExtrasFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

/**
 * Created by mariotaku on 16/3/6.
 */
@CursorObject
public class Tab {
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
    Arguments arguments;

    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    Extras extras;

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

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }
}
