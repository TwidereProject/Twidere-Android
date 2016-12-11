package org.mariotaku.twidere.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;

import java.util.List;

/**
 * Created by mariotaku on 16/3/28.
 */
@JsonObject
public class FiltersData {

    @JsonField(name = "users")
    List<UserItem> users;
    @JsonField(name = "keywords")
    List<BaseItem> keywords;
    @JsonField(name = "sources")
    List<BaseItem> sources;
    @JsonField(name = "links")
    List<BaseItem> links;

    public List<UserItem> getUsers() {
        return users;
    }

    public List<BaseItem> getKeywords() {
        return keywords;
    }

    public List<BaseItem> getSources() {
        return sources;
    }

    public List<BaseItem> getLinks() {
        return links;
    }

    public void setUsers(List<UserItem> users) {
        this.users = users;
    }

    public void setKeywords(List<BaseItem> keywords) {
        this.keywords = keywords;
    }

    public void setSources(List<BaseItem> sources) {
        this.sources = sources;
    }

    public void setLinks(List<BaseItem> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "FiltersData{" +
                "users=" + users +
                ", keywords=" + keywords +
                ", sources=" + sources +
                ", links=" + links +
                '}';
    }

    @JsonObject
    @CursorObject(valuesCreator = true)
    public static class UserItem {
        @CursorField(value = Filters.Users.USER_KEY, converter = UserKeyCursorFieldConverter.class)
        @JsonField(name = "user_key", typeConverter = UserKeyConverter.class)
        UserKey userKey;
        @CursorField(Filters.Users.NAME)
        @JsonField(name = "name")
        String name;
        @CursorField(Filters.Users.SCREEN_NAME)
        @JsonField(name = "screen_name")
        String screenName;

        public UserKey getUserKey() {
            return userKey;
        }

        public String getName() {
            return name;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public void setUserKey(UserKey userKey) {
            this.userKey = userKey;
        }

        @Override
        public String toString() {
            return "UserItem{" +
                    "userKey='" + userKey + '\'' +
                    ", name='" + name + '\'' +
                    ", screenName='" + screenName + '\'' +
                    '}';
        }
    }

    @JsonObject
    @CursorObject(valuesCreator = true)
    public static class BaseItem {
        @CursorField(Filters.VALUE)
        @JsonField(name = "value")
        String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "BaseItem{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
