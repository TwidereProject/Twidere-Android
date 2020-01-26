package org.mariotaku.abstask.library;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

/**
 * Created by mariotaku on 16/5/25.
 */
public class ManualTaskStarter {
    @UiThread
    public static void invokeBeforeExecute(AbstractTask<?, ?, ?> task) {
        task.invokeBeforeExecute();
    }

    @UiThread
    public static <Result> void invokeAfterExecute(AbstractTask<?, Result, ?> task, Result result) {
        task.invokeAfterExecute(result);
    }

    @WorkerThread
    public static <Result> Result invokeExecute(AbstractTask<?, Result, ?> task) {
        return task.invokeExecute();
    }

}
