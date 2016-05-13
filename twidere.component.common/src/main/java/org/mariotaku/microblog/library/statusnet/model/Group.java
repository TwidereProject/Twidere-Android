package org.mariotaku.microblog.library.statusnet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 16/3/4.
 */
@ParcelablePlease
@JsonObject
public class Group implements Parcelable {

    @JsonField(name = "modified", typeConverter = TwitterDateConverter.class)
    Date modified;
    @JsonField(name = "nickname")
    String nickname;
    @JsonField(name = "admin_count")
    long adminCount;
    @JsonField(name = "created", typeConverter = TwitterDateConverter.class)
    Date created;
    @JsonField(name = "id")
    String id;
    @JsonField(name = "homepage")
    String homepage;
    @JsonField(name = "fullname")
    String fullname;
    @JsonField(name = "homepage_logo")
    String homepageLogo;
    @JsonField(name = "mini_logo")
    String miniLogo;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "member_count")
    long memberCount;
    @JsonField(name = "blocked")
    boolean blocked;
    @JsonField(name = "stream_logo")
    String streamLogo;
    @JsonField(name = "member")
    boolean member;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "original_logo")
    String originalLogo;
    @JsonField(name = "location")
    String location;

    public Date getModified() {
        return modified;
    }

    public String getNickname() {
        return nickname;
    }

    public long getAdminCount() {
        return adminCount;
    }

    public Date getCreated() {
        return created;
    }

    public String getId() {
        return id;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getFullname() {
        return fullname;
    }

    public String getHomepageLogo() {
        return homepageLogo;
    }

    public String getMiniLogo() {
        return miniLogo;
    }

    public String getUrl() {
        return url;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getStreamLogo() {
        return streamLogo;
    }

    public boolean isMember() {
        return member;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalLogo() {
        return originalLogo;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Group{" +
                "modified=" + modified +
                ", nickname='" + nickname + '\'' +
                ", adminCount=" + adminCount +
                ", created=" + created +
                ", id=" + id +
                ", homepage='" + homepage + '\'' +
                ", fullname='" + fullname + '\'' +
                ", homepageLogo='" + homepageLogo + '\'' +
                ", miniLogo='" + miniLogo + '\'' +
                ", url='" + url + '\'' +
                ", memberCount=" + memberCount +
                ", blocked=" + blocked +
                ", streamLogo='" + streamLogo + '\'' +
                ", member=" + member +
                ", description='" + description + '\'' +
                ", originalLogo='" + originalLogo + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return id.equals(group.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GroupParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            Group target = new Group();
            GroupParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}