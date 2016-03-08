package org.mariotaku.twidere.model.tab.extra;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/3/6.
 */
@ParcelablePlease
@JsonObject
public class InteractionsTabExtras extends TabExtras implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "my_following_only")
    boolean myFollowingOnly;

    @ParcelableThisPlease
    @JsonField(name = "mentions_only")
    boolean mentionsOnly;

    public boolean isMyFollowingOnly() {
        return myFollowingOnly;
    }

    public void setMyFollowingOnly(boolean myFollowingOnly) {
        this.myFollowingOnly = myFollowingOnly;
    }

    public boolean isMentionsOnly() {
        return mentionsOnly;
    }

    public void setMentionsOnly(boolean mentionsOnly) {
        this.mentionsOnly = mentionsOnly;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        InteractionsTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<InteractionsTabExtras> CREATOR = new Creator<InteractionsTabExtras>() {
        public InteractionsTabExtras createFromParcel(Parcel source) {
            InteractionsTabExtras target = new InteractionsTabExtras();
            InteractionsTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public InteractionsTabExtras[] newArray(int size) {
            return new InteractionsTabExtras[size];
        }
    };
}
