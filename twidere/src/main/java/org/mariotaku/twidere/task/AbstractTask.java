package org.mariotaku.twidere.task;

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.lang.ref.WeakReference;

/**
 * Abstract Task class can be used with different implementations
 * Created by mariotaku on 16/2/24.
 */
public abstract class AbstractTask<Params, Result, Callback> {

    private Params mParams;
    private WeakReference<Callback> mCallbackRef;

    @WorkerThread
    protected abstract Result doLongOperation(Params params);

    @MainThread
    protected void beforeExecute(Params params) {

    }

    @MainThread
    protected void afterExecute(Params params, Result result) {

    }

    @MainThread
    protected void afterExecute(Callback callback, Params params, Result result) {

    }

    public void setParams(Params params) {
        mParams = params;
    }

    public AbstractTask<Params, Result, Callback> setResultHandler(Callback callback) {
        mCallbackRef = new WeakReference<>(callback);
        return this;
    }

    @MainThread
    public void invokeAfterExecute(Result result) {
        Callback callback = getCallback();
        if (callback != null) {
            afterExecute(callback, mParams, result);
        } else {
            afterExecute(mParams, result);
        }
    }

    @Nullable
    protected Callback getCallback() {
        if (mCallbackRef == null) return null;
        return mCallbackRef.get();
    }

    public Result invokeExecute() {
        return doLongOperation(mParams);
    }

    public void invokeBeforeExecute() {
        beforeExecute(mParams);
    }
}
