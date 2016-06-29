package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.annotation.Referral;
import org.mariotaku.twidere.fragment.UserFragment;
import org.mariotaku.twidere.model.ParcelableUser;

/**
 * Created by mariotaku on 16/3/4.
 */
@JsonObject
@ParcelablePlease
public class UserEvent extends BaseEvent implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "account_id")
    String accountId;
    @ParcelableThisPlease
    @JsonField(name = "account_host")
    String accountHost;
    @ParcelableThisPlease
    @JsonField(name = "user_id")
    String userId;
    @ParcelableThisPlease
    @JsonField(name = "user_host")
    String userHost;
    @ParcelableThisPlease
    @JsonField(name = "status_count")
    long statusCount;
    @ParcelableThisPlease
    @JsonField(name = "follower_count")
    long followerCount;
    @ParcelableThisPlease
    @JsonField(name = "friend_count")
    long friendCount;
    @ParcelableThisPlease
    @JsonField(name = "referral")
    @Referral
    @Nullable
    String referral;
    public static final Creator<UserEvent> CREATOR = new Creator<UserEvent>() {
        public UserEvent createFromParcel(Parcel source) {
            UserEvent target = new UserEvent();
            UserEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserEvent[] newArray(int size) {
            return new UserEvent[size];
        }
    };

    public static UserEvent create(Context context, @Referral @Nullable String referral) {
        UserEvent event = new UserEvent();
        event.markStart(context);
        event.referral = referral;
        return event;
    }

    public void setUser(@NonNull ParcelableUser user) {
        accountId = user.account_key.getId();
        accountHost = user.account_key.getHost();
        userId = user.key.getId();
        userHost = user.key.toString();
        statusCount = user.statuses_count;
        followerCount = user.followers_count;
        friendCount = user.friends_count;
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "user";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "accountId=" + accountId +
                ", accountHost='" + accountHost + '\'' +
                ", userId=" + userId +
                ", statusCount=" + statusCount +
                ", followerCount=" + followerCount +
                ", friendCount=" + friendCount +
                ", referral='" + referral + '\'' +
                "} " + super.toString();
    }

}
