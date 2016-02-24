package org.mariotaku.twidere.task.util;

import android.os.AsyncTask;

import org.mariotaku.twidere.task.AbstractTask;

/**
 * Created by mariotaku on 16/2/24.
 */
public class TaskStarter {

    public static <Params, Result, Callback> void execute(AbstractTask<Params, Result, Callback> task) {
        final AsyncTaskTask<Params, Result, Callback> asyncTaskTask = new AsyncTaskTask<>(task);
        asyncTaskTask.execute();
    }

    static class AsyncTaskTask<P, R, C> extends AsyncTask<Object, Object, R> {

        private final AbstractTask<P, R, C> task;

        public AsyncTaskTask(AbstractTask<P, R, C> task) {
            this.task = task;
        }

        @Override
        protected void onPreExecute() {
            task.invokeBeforeExecute();
        }

        @Override
        protected R doInBackground(Object[] params) {
            return task.invokeExecute();
        }

        @Override
        protected void onPostExecute(R r) {
            task.invokeAfterExecute(r);
        }
    }

}
