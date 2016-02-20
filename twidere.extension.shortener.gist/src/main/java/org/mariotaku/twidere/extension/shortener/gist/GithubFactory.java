package org.mariotaku.twidere.extension.shortener.gist;

import android.support.annotation.Nullable;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.ValueMap;
import org.mariotaku.restfu.okhttp3.OkHttpRestClient;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthEndpoint;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.model.ParcelableCredentials;

import okhttp3.OkHttpClient;

/**
 * Created by mariotaku on 16/2/20.
 */
public class GithubFactory {

    public static Github getInstance(final String apiKey) {
        final RestAPIFactory<GithubException> factory = new RestAPIFactory<>();
        factory.setEndpoint(new Endpoint("https://api.github.com/"));
        factory.setHttpClient(new OkHttpRestClient(new OkHttpClient()));
        factory.setAuthorization(new Authorization() {
            @Override
            public String getHeader(Endpoint endpoint, RestRequest restRequest) {
                return "token " + apiKey;
            }

            @Override
            public boolean hasAuthorization() {
                return apiKey != null;
            }
        });
        factory.setExceptionFactory(new GithubExceptionFactory());
        factory.setRestConverterFactory(new GithubConverterFactory());
        return factory.build(Github.class);
    }

    private static class GithubExceptionFactory implements ExceptionFactory<GithubException> {
        @Override
        public GithubException newException(Throwable throwable, HttpRequest httpRequest, HttpResponse httpResponse) {
            GithubException exception;
            if (throwable != null) {
                exception = new GithubException(throwable);
            } else {
                exception = new GithubException();
            }
            exception.setRequest(httpRequest);
            exception.setResponse(httpResponse);
            return exception;
        }
    }
}
