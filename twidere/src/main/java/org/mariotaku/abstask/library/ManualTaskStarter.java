package org.mariotaku.abstask.library;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public class ManualTaskStarter {
    @UiThread
    public static void invokeBeforeExecute(AbstractTask<?, ?, ?> task) {
    }

    @UiThread
    public static <Result> void invokeAfterExecute(AbstractTask<?, Result, ?> task, Result result) {
    }

    @WorkerThread
    public static <Result> Result invokeExecute(AbstractTask<?, Result, ?> task) {
        return null;
    }

}
