package org.mariotaku.twidere.util.support;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mariotaku on 2017/2/2.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceSupport {
    public static boolean handleStopJob(JobParameters params, boolean reschedule) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Seems fixed after Nougat, ignore!
            return false;
        }
        try {
            final Method getCallbackMethod = JobParameters.class.getDeclaredMethod("getCallback");
            getCallbackMethod.setAccessible(true);
            final Object callback = getCallbackMethod.invoke(params);
            if (callback == null) return false;
            final Class<?> callbackCls = callback.getClass();
            final Method acknowledgeStopMessageMethod = callbackCls.getDeclaredMethod("acknowledgeStopMessage",
                    int.class, boolean.class);
            acknowledgeStopMessageMethod.setAccessible(true);
            // Once method returned true successfully, remove it's callback.
            // Due to Android's Binder implementation, IJobCallback.Stub.asInterface(null) will
            // return null rather than crash
            try {
                acknowledgeStopMessageMethod.invoke(callbackCls, params.getJobId(), reschedule);
                return true;
            } catch (NullPointerException npe) {
                // https://code.google.com/p/android/issues/detail?id=104302
                // Treat as handled
                return true;
            }
        } catch (NoSuchMethodException e) {
            // Framework version mismatch, skip
            return false;
        } catch (IllegalAccessException e) {
            // This shouldn't happen, skip
            return false;
        } catch (InvocationTargetException e) {
            // Internal error, skip
            return false;
        }
    }

    public static boolean removeCallback(JobParameters params) {
        try {
            // Find `callback` field
            final Field callbackField = JobParameters.class.getDeclaredField("callback");
            callbackField.setAccessible(true);
            callbackField.set(params, null);
            return true;
        } catch (NoSuchFieldException e) {
            // Framework version mismatch, skip
            return false;
        } catch (IllegalAccessException e) {
            // This shouldn't happen, skip
            return false;
        }
    }
}
