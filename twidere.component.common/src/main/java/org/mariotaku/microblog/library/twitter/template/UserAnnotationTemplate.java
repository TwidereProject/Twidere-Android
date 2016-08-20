package org.mariotaku.microblog.library.twitter.template;

import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Queries;

/**
 * Created by mariotaku on 16/8/20.
 */
@Queries({@KeyValue(key = "include_entities", valueKey = "include_entities"),
        @KeyValue(key = "include_cards", valueKey = "include_cards"),
        @KeyValue(key = "cards_platform", valueKey = "cards_platform"),
        @KeyValue(key = "include_blocking", value = "true"),
        @KeyValue(key = "include_blocked_by", value = "true")})
public class UserAnnotationTemplate {
}
