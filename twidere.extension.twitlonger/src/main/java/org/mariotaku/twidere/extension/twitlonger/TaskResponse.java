package org.mariotaku.twidere.extension.twitlonger;

/**
 * Created by mariotaku on 16/2/20.
 */
public class TaskResponse<O, T extends Throwable> {
    O object;
    T throwable;

    public TaskResponse(T throwable) {
        this.throwable = throwable;
    }

    public TaskResponse(O object) {
        this.object = object;
    }

    public O getObject() {
        return object;
    }

    public T getThrowable() {
        return throwable;
    }

    public static <O, T extends Throwable> TaskResponse<O, T> getInstance(O post) {
        return new TaskResponse<>(post);
    }

    public static <O, T extends Throwable> TaskResponse<O, T> getInstance(T throwable) {
        return new TaskResponse<>(throwable);
    }

    public boolean hasError() {
        return throwable != null;
    }
}
