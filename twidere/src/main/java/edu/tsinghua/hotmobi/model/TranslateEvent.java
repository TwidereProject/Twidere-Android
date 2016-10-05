package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 2016/10/3.
 */

@ParcelablePlease
@JsonObject
public class TranslateEvent extends BaseEvent implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "id")
    String id;
    @ParcelableThisPlease
    @JsonField(name = "account_id")
    String accountId;
    @ParcelableThisPlease
    @JsonField(name = "account_host")
    String accountHost;
    @ParcelableThisPlease
    @JsonField(name = "tweet_lang")
    String tweetLang;
    @ParcelableThisPlease
    @JsonField(name = "translated_lang")
    String translatedLang;

    public static TranslateEvent create(Context context, ParcelableStatus status, String translatedLang) {
        TranslateEvent event = new TranslateEvent();
        event.markStart(context);
        event.setId(status.id);
        event.setAccountId(status.account_key.getId());
        event.setAccountHost(status.account_key.getHost());
        event.setTweetLang(status.lang);
        event.setTranslatedLang(translatedLang);
        return event;
    }

    public String getAccountHost() {
        return accountHost;
    }

    public void setAccountHost(String accountHost) {
        this.accountHost = accountHost;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTweetLang() {
        return tweetLang;
    }

    public void setTweetLang(String tweetLang) {
        this.tweetLang = tweetLang;
    }

    public String getTranslatedLang() {
        return translatedLang;
    }

    public void setTranslatedLang(String translatedLang) {
        this.translatedLang = translatedLang;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TranslateEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TranslateEvent> CREATOR = new Creator<TranslateEvent>() {
        public TranslateEvent createFromParcel(Parcel source) {
            TranslateEvent target = new TranslateEvent();
            TranslateEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TranslateEvent[] newArray(int size) {
            return new TranslateEvent[size];
        }
    };

    @NonNull
    @Override
    public String getLogFileName() {
        return "translate";
    }
}
