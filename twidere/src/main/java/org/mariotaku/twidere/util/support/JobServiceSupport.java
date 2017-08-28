/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */
package org.mariotaku.twidere.util.support;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A fix to https://code.google.com/p/android/issues/detail?id=104302
 * Created by mariotaku on 2017/2/2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceSupport {
    @SuppressLint("PrivateApi")
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
            final Class<?> callbackCls = Class.forName("android.app.job.IJobCallback");
            final Method acknowledgeStopMessageMethod = callbackCls.getMethod("acknowledgeStopMessage",
                    int.class, boolean.class);
            acknowledgeStopMessageMethod.setAccessible(true);
            // Once method returned true successfully, remove it's callback.
            // Due to Android's Binder implementation, IJobCallback.Stub.asInterface(null) will
            // return null rather than crash
            try {
                acknowledgeStopMessageMethod.invoke(callback, params.getJobId(), reschedule);
                return true;
            } catch (NullPointerException npe) {
                // Treat as handled
                return true;
            }
        } catch (NoSuchMethodException e) {
            // Framework version mismatch, skip
            return false;
        } catch (ClassNotFoundException e) {
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
