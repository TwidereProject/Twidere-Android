package org.mariotaku.twidere.test;

import com.bluelinelabs.logansquare.Constants;

import org.junit.Test;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.HashtagEntity;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.Trend;
import org.mariotaku.twidere.api.twitter.model.Trends;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.util.TwidereTypeUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DeadLockTest {

    private static final Executor sWatchDogExecutor = Executors.newCachedThreadPool();

    private static Class[] TEST_CLASSES = {Status.class, DirectMessage.class, User.class,
            UserList.class, Trend.class, Trends.class, SavedSearch.class, IDs.class,
            MediaEntity.class, UrlEntity.class, HashtagEntity.class, UserMentionEntity.class};

    @Test
    public void testWithoutSynchronization() throws Exception {
        for (Class testClass : TEST_CLASSES) {
            new Thread(new RunnableWithoutSynchronization(testClass)).start();
        }
    }

    @Test
    public void testWithSynchronization() {
//        for (Class testClass : TEST_CLASSES) {
//            new Thread(new RunnableWithSynchronization(testClass)).start();
//        }
    }

    static class RunnableWithSynchronization implements Runnable {

        private final Class<?> cls;

        RunnableWithSynchronization(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public void run() {
            final WatchdogRunnable runnable = new WatchdogRunnable(cls);
            sWatchDogExecutor.execute(runnable);
            try {
                synchronized (RunnableWithSynchronization.class) {
                    Class.forName(cls.getName() + Constants.MAPPER_CLASS_SUFFIX);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                runnable.finished();
            }
        }
    }

    static class RunnableWithoutSynchronization implements Runnable {

        private final Class<?> cls;

        RunnableWithoutSynchronization(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public void run() {
            final WatchdogRunnable runnable = new WatchdogRunnable(cls);
            sWatchDogExecutor.execute(runnable);
            try {
                Class.forName(cls.getName() + Constants.MAPPER_CLASS_SUFFIX);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                runnable.finished();
            }
        }
    }

    private static class WatchdogRunnable implements Runnable {
        private final Class<?> cls;
        private boolean finished;

        public WatchdogRunnable(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public void run() {
            // Crash if take more than 100ms
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                //
            }
            if (!finished) {
                throw new RuntimeException("Too long waiting: " + TwidereTypeUtils.toSimpleName(cls));
            }
        }

        public synchronized void finished() {
            this.finished = true;
        }
    }

}
