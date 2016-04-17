package org.mariotaku.twidere.util;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.Twidere_ParameterizedTypeAccessor;

import org.mariotaku.twidere.common.BuildConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by mariotaku on 16/2/19.
 */
public class LoganSquareMapperFinder {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private LoganSquareMapperFinder() {
    }

    public static <T> JsonMapper<T> mapperFor(Class<T> cls) throws ClassLoaderDeadLockException {
        return mapperFor(Twidere_ParameterizedTypeAccessor.<T>create(cls));
    }

    public static <T> JsonMapper<T> mapperFor(Type type) throws ClassLoaderDeadLockException {
        return mapperFor(Twidere_ParameterizedTypeAccessor.<T>create(type));
    }

    public static <T> JsonMapper<T> mapperFor(final ParameterizedType<T> type) throws ClassLoaderDeadLockException {
        final Future<JsonMapper<T>> future = pool.submit(new Callable<JsonMapper<T>>() {
            @Override
            public JsonMapper<T> call() {
                return LoganSquare.mapperFor(type);
            }
        });
        final JsonMapper<T> mapper;
        //noinspection TryWithIdenticalCatches
        try {
            mapper = future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            }
            BugReporter.logException(e);
            throw new ClassLoaderDeadLockException(e);
        }
        return mapper;
    }

    public static class ClassLoaderDeadLockException extends IOException {
        public ClassLoaderDeadLockException() {
            super();
        }

        public ClassLoaderDeadLockException(String detailMessage) {
            super(detailMessage);
        }

        public ClassLoaderDeadLockException(String message, Throwable cause) {
            super(message, cause);
        }

        public ClassLoaderDeadLockException(Throwable cause) {
            super(cause);
        }
    }
}
