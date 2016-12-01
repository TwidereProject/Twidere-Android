package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.constant.IntentConstants;

/**
 * Created by mariotaku on 16/6/22.
 */
@ParcelablePlease
@JsonObject
public class HomeTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "hide_retweets")
    boolean hideRetweets;
    @JsonField(name = "hide_quotes")
    boolean hideQuotes;
    @JsonField(name = "hide_replies")
    boolean hideReplies;

    public boolean isHideRetweets() {
        return hideRetweets;
    }

    public void setHideRetweets(boolean hideRetweets) {
        this.hideRetweets = hideRetweets;
    }

    public boolean isHideQuotes() {
        return hideQuotes;
    }

    public void setHideQuotes(boolean hideQuotes) {
        this.hideQuotes = hideQuotes;
    }

    public boolean isHideReplies() {
        return hideReplies;
    }

    public void setHideReplies(boolean hideReplies) {
        this.hideReplies = hideReplies;
    }

    @Override
    public void copyToBundle(Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_RETWEETS, hideRetweets);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_QUOTES, hideQuotes);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_REPLIES, hideReplies);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HomeTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "HomeTabExtras{" +
                "hideRetweets=" + hideRetweets +
                ", hideQuotes=" + hideQuotes +
                ", hideReplies=" + hideReplies +
                "} " + super.toString();
    }

    public static final Creator<HomeTabExtras> CREATOR = new Creator<HomeTabExtras>() {
        public HomeTabExtras createFromParcel(Parcel source) {
            HomeTabExtras target = new HomeTabExtras();
            HomeTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public HomeTabExtras[] newArray(int size) {
            return new HomeTabExtras[size];
        }
    };
}
