package org.mariotaku.microblog.library.fanfou.model;

import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.restfu.http.mime.Body;

/**
 * Created by mariotaku on 16/3/20.
 */
public class PhotoStatusUpdate extends SimpleValueMap {

    public PhotoStatusUpdate(Body photo, String status) {
        put("photo", photo);
        put("status", status);
    }

    public void setLocation(String location) {
        if (location == null) {
            remove("location");
            return;
        }
        put("location", location);
    }

}
