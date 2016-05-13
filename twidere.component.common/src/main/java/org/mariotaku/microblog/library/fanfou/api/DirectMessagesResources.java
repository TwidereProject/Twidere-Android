package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;

/**
 * Created by mariotaku on 16/3/31.
 */
public interface DirectMessagesResources {

    @POST("/direct_messages/new.json")
    DirectMessage sendFanfouDirectMessage(@Param("user") String user, @Param("text") String text,
                                          @Param("in_reply_to_id") String inReplyToId)
            throws MicroBlogException;

    @POST("/direct_messages/new.json")
    DirectMessage sendFanfouDirectMessage(@Param("user") String user, @Param("text") String text)
            throws MicroBlogException;

}
