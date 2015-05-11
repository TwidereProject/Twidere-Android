package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.RestHttpClient;
import org.mariotaku.simplerestapi.http.RestHttpRequest;
import org.mariotaku.simplerestapi.http.RestHttpResponse;

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
    private RestHttpRequest.Factory requestFactory;
    private ExceptionFactory exceptionFactory;
    private RequestInfo.Factory requestInfoFactory;

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

    public void setRequestFactory(RestHttpRequest.Factory factory) {
        this.requestFactory = factory;
    }

    public void setExceptionFactory(ExceptionFactory factory) {
        this.exceptionFactory = factory;
    }

    public RestAPIFactory() {

    }

    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> cls) {
        final ClassLoader classLoader = cls.getClassLoader();
        final Class[] interfaces = new Class[]{cls};
        return (T) Proxy.newProxyInstance(classLoader, interfaces, new RestInvocationHandler(endpoint,
                authorization, restClient, converter, requestInfoFactory, requestFactory, exceptionFactory));
    }

    public static RestClient getRestClient(Object obj) {
        final InvocationHandler handler = Proxy.getInvocationHandler(obj);
        if (!(handler instanceof RestClient)) throw new IllegalArgumentException();
        return (RestClient) handler;
    }

    public void setRequestInfoFactory(RequestInfo.Factory requestInfoFactory) {
        this.requestInfoFactory = requestInfoFactory;
    }

    private static class RestInvocationHandler implements InvocationHandler, RestClient {
        private final Endpoint endpoint;
        private final Authorization authorization;
        private final Converter converter;
        private final RestHttpRequest.Factory requestFactory;
        private final ExceptionFactory exceptionFactory;
        private final RequestInfo.Factory requestInfoFactory;

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

        public RestInvocationHandler(Endpoint endpoint, Authorization authorization,
                                     RestHttpClient restClient, Converter converter,
                                     RequestInfo.Factory requestInfoFactory, RestHttpRequest.Factory requestFactory, ExceptionFactory exceptionFactory) {
            this.endpoint = endpoint;
            this.authorization = authorization;
            this.restClient = restClient;
            this.converter = converter;
            this.requestInfoFactory = requestInfoFactory;
            this.requestFactory = requestFactory != null ? requestFactory : new RestHttpRequest.DefaultFactory();
            this.exceptionFactory = exceptionFactory != null ? exceptionFactory : new DefaultExceptionFactory();
        }

        @SuppressWarnings("TryWithIdenticalCatches")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            final RestMethodInfo methodInfo = RestMethodInfo.get(method, args);
            final RequestInfo requestInfo;
            if (requestInfoFactory != null) {
                requestInfo = requestInfoFactory.create(methodInfo);
            } else {
                requestInfo = methodInfo.toRequestInfo();
            }
            final RestHttpRequest restHttpRequest = requestFactory.create(endpoint, requestInfo, authorization);
            final Class<?>[] parameterTypes = method.getParameterTypes();
            RestHttpResponse response = null;
            try {
                response = restClient.execute(restHttpRequest);
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
                final Exception re = exceptionFactory.newException(e, response);
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
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                Utils.closeSilently(response);
            }
        }
    }

    public interface ExceptionFactory {

        Exception newException(Throwable cause, RestHttpResponse response);

    }

    public static final class DefaultExceptionFactory implements ExceptionFactory {

        @Override
        public Exception newException(Throwable cause, RestHttpResponse response) {
            final RestException e = new RestException(cause);
            e.setResponse(response);
            return e;
        }
    }
}
