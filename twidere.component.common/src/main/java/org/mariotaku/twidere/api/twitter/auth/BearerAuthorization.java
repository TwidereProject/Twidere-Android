package org.mariotaku.twidere.api.twitter.auth;

import org.mariotaku.restfu.RestRequestInfo;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;

/**
 * Created by mariotaku on 16/1/4.
 */
public class BearerAuthorization implements Authorization {
    private final String accessToken;

    public BearerAuthorization(String accessToken) {
        this.accessToken = accessToken;
    }


    @Override
    public String getHeader(Endpoint endpoint, RestRequestInfo info) {
        return "Bearer " + accessToken;
    }

    @Override
    public boolean hasAuthorization() {
        return accessToken != null;
    }
}
