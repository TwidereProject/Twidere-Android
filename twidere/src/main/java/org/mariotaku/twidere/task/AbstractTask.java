package org.mariotaku.twidere.task;

import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

/**
 * Abstract Task class can be used with different implementations
 * Created by mariotaku on 16/2/24.
 */
public abstract class AbstractTask<Params, Result, Callback> {

    private Params mParams;
    private Callback mCallback;

    @WorkerThread
    protected abstract Result doLongOperation(Params params);

    @MainThread
    protected void beforeExecute() {

    }

    @MainThread
    protected void afterExecute(Result result) {

    }

    @MainThread
    protected void afterExecute(Callback callback, Result result) {

    }

    public void setParams(Params params) {
        mParams = params;
    }

    public AbstractTask<Params, Result, Callback> setResultHandler(Callback callback) {
        mCallback = callback;
        return this;
    }

    @MainThread
    public void invokeAfterExecute(Result result) {
        if (mCallback != null) {
            afterExecute(mCallback, result);
        } else {
            afterExecute(result);
        }
    }

    public Result invokeExecute() {
        return doLongOperation(mParams);
    }

    public void invokeBeforeExecute() {
        beforeExecute();
    }
}
