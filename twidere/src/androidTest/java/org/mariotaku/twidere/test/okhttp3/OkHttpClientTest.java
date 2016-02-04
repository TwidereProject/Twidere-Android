package org.mariotaku.twidere.test.okhttp3;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static org.junit.Assert.assertTrue;

/**
 * Created by mariotaku on 16/2/5.
 */
public class OkHttpClientTest {

    @Test
    public void testSocksFunctionality() throws Exception {
        final Proxy proxy = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 1080));
        final OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        final Request request = new Request.Builder()
                .url("https://www.google.com/")
                .build();
        assertTrue(client.newCall(request).execute().isSuccessful());
    }

}
