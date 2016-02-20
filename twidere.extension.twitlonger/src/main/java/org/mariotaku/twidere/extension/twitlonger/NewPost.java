package org.mariotaku.twidere.extension.twitlonger;

import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 16/2/20.
 */
public class NewPost extends SimpleValueMap {

    public NewPost(String content) {
        put("content", content);
    }

    public void setInReplyTo(long inReplyToId, String inReplyToScreenName) {
        put("reply_to_id", inReplyToId);
        put("reply_to_screen_name", inReplyToScreenName);
    }

}
