package org.mariotaku.microblog.library.twitter.template;

import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Queries;

/**
 * Created by mariotaku on 16/5/27.
 */
@Queries({@KeyValue(key = "include_my_retweet", valueKey = "include_my_retweet"),
        @KeyValue(key = "include_rts", valueKey = "include_entities"),
        @KeyValue(key = "include_entities", valueKey = "include_entities"),
        @KeyValue(key = "include_cards", valueKey = "include_cards"),
        @KeyValue(key = "cards_platform", valueKey = "cards_platform"),
        @KeyValue(key = "include_reply_count", valueKey = "include_reply_count"),
        @KeyValue(key = "include_descendent_reply_count", valueKey = "include_descendent_reply_count"),
        @KeyValue(key = "include_ext_alt_text", valueKey = "include_ext_alt_text"),
        @KeyValue(key = "tweet_mode", valueKey = "tweet_mode"),
        @KeyValue(key = "model_version", valueKey = "model_version"),
        @KeyValue(key = "include_blocking", value = "true"),
        @KeyValue(key = "include_blocked_by", value = "true")
})
public class StatusAnnotationTemplate {
}
