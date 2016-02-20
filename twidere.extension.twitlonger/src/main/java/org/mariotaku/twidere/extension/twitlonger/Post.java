
package org.mariotaku.twidere.extension.twitlonger;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Post {

    @JsonField(name = "post_time")
    public String postTime;
    @JsonField(name = "reply_to_id")
    public long replyToId;
    @JsonField(name = "short_url")
    public String shortUrl;
    @JsonField(name = "screen_name")
    public String screenName;
    @JsonField(name = "full_url")
    public String fullUrl;
    @JsonField(name = "id")
    public String id;
    @JsonField(name = "twitter_status_id")
    public Object twitterStatusId;
    @JsonField(name = "tweet_content")
    public String tweetContent;
    @JsonField(name = "content")
    public String content;

}