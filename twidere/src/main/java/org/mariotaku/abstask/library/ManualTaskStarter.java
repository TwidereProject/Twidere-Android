package org.mariotaku.abstask.library;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public class ManualTaskStarter {
    @UiThread
    public static void invokeBeforeExecute(AbstractTask<?, ?, ?> task) {
        task.mDispatcher.invokeBeforeExecute();
    }

    @UiThread
    public static <Result> void invokeAfterExecute(AbstractTask<?, Result, ?> task, Result result) {
        task.mDispatcher.invokeAfterExecute(result);
    }

    @WorkerThread
    public static <Result> Result invokeExecute(AbstractTask<?, Result, ?> task) {
        return task.mDispatcher.invokeExecute();
    }

}
