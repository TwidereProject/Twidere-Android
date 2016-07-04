package org.mariotaku.twidere.util;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.restfu.RestAPIFactory;

/**
 * Created by mariotaku on 16/5/27.
 */
public class MicroBlogBuilder {

    final RestAPIFactory<MicroBlogException> factory;

    public MicroBlogBuilder() {
        factory = new RestAPIFactory<>();
    }


    public <T> T build(Class<T> cls) {
        return factory.build(cls);
    }

}
