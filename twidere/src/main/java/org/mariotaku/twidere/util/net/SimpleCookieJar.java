package org.mariotaku.twidere.util.net;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public final class SimpleCookieJar implements CookieJar {
    private final List<Cookie> allCookies = new ArrayList<>();

    @Override
    public synchronized void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        allCookies.addAll(cookies);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        for (Cookie cookie : allCookies) {
            if (cookie.matches(url)) {
                result.add(cookie);
            }
        }
        return result;
    }
}