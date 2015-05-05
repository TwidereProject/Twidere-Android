package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.RestHttpClient;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.RestRequest;
import org.mariotaku.simplerestapi.http.RestResponse;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by mariotaku on 15/2/6.
 */
public class RestAPIFactory {

    private Endpoint endpoint;
    private Authorization authorization;
    private Converter converter;
    private RestHttpClient restClient;

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClient(RestHttpClient restClient) {
        this.restClient = restClient;
    }

    public RestAPIFactory() {

    }

    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> cls) {
        final ClassLoader classLoader = cls.getClassLoader();
        final Class[] interfaces = new Class[]{cls};
        return (T) Proxy.newProxyInstance(classLoader, interfaces, new RestInvocationHandler(endpoint,
                authorization, restClient, converter));
    }

    private static class RestInvocationHandler implements InvocationHandler, org.mariotaku.simplerestapi.RestClient {
        private final Endpoint endpoint;
        private final Authorization authorization;
        private final Converter converter;

        @Override
        public Endpoint getEndpoint() {
            return endpoint;
        }

        @Override
        public RestHttpClient getRestClient() {
            return restClient;
        }

        @Override
        public Converter getConverter() {
            return converter;
        }

        @Override
        public Authorization getAuthorization() {
            return authorization;
        }

        private final RestHttpClient restClient;

        public RestInvocationHandler(Endpoint endpoint, Authorization authorization, RestHttpClient restClient, Converter converter) {
            this.endpoint = endpoint;
            this.authorization = authorization;
            this.restClient = restClient;
            this.converter = converter;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            final RestMethodInfo methodInfo = RestMethodInfo.get(method, args);
            final RestMethod restMethod = methodInfo.getMethod();
            final String url = Endpoint.constructUrl(endpoint.getUrl(), methodInfo);
            final RestRequest restRequest = new RestRequest(restMethod.value(), url, methodInfo.getHeaders(), methodInfo.getBody(),
                    endpoint, methodInfo, authorization);
            final Class<?>[] parameterTypes = method.getParameterTypes();
            RestResponse response = null;
            try {
                response = restClient.execute(restRequest);
                if (parameterTypes.length > 0) {
                    final Class<?> lastParameterType = parameterTypes[parameterTypes.length - 1];
                    if (RestCallback.class.isAssignableFrom(lastParameterType)) {
                        final Method callbackMethod = lastParameterType.getMethod("result", Object.class);
                        final RestCallback<?> callback = (RestCallback<?>) args[args.length - 1];
                        final Object object = converter.convert(response, method.getGenericReturnType());
                        if (callback != null) {
                            callbackMethod.invoke(callback, object);
                        }
                        return null;
                    } else if (RawCallback.class.isAssignableFrom(lastParameterType)) {
                        final RawCallback callback = (RawCallback) args[args.length - 1];
                        callback.result(response);
                        return null;
                    }
                }
                return converter.convert(response, method.getGenericReturnType());
            } catch (IOException e) {
                final RestException re = new RestException(e);
                re.setResponse(response);
                if (parameterTypes.length > 0) {
                    final Class<?> lastParameterType = parameterTypes[parameterTypes.length - 1];
                    if (ErrorCallback.class.isAssignableFrom(lastParameterType)) {
                        final ErrorCallback callback = (ErrorCallback) args[args.length - 1];
                        if (callback != null) {
                            callback.error(re);
                            return null;
                        }
                    }
                }
                throw re;
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                Utils.closeSilently(response);
            }
        }
    }

}
