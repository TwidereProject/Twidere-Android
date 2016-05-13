package org.mariotaku.microblog.library.twitter.model;

import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 16/3/1.
 */
public class NewDm extends SimpleValueMap {

    public void setText(String text) {
        put("text", text);
    }

    public void setConversationId(String conversationId) {
        put("conversation_id", conversationId);
    }

    public void setMediaId(long mediaId) {
        put("media_id", mediaId);
    }

}
