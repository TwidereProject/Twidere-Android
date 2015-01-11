/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package twitter4j.internal.async;

import java.util.LinkedList;
import java.util.List;

import twitter4j.conf.StreamConfiguration;
import twitter4j.internal.logging.Logger;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
final class DispatcherImpl implements Dispatcher {
	private final ExecuteThread[] threads;
	private final List<Runnable> q = new LinkedList<Runnable>();

	final Object ticket = new Object();

	private boolean active = true;

	public DispatcherImpl(final StreamConfiguration conf) {
		threads = new ExecuteThread[conf.getAsyncNumThreads()];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ExecuteThread("Twitter4J Async Dispatcher", this, i);
			threads[i].setDaemon(true);
			threads[i].start();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (active) {
					shutdown();
				}
			}
		});
	}

	@Override
	public synchronized void invokeLater(final Runnable task) {
		synchronized (q) {
			q.add(task);
		}
		synchronized (ticket) {
			ticket.notify();
		}
	}

	public Runnable poll() {
		while (active) {
			synchronized (q) {
				if (q.size() > 0) {
					final Runnable task = q.remove(0);
					if (task != null) return task;
				}
			}
			synchronized (ticket) {
				try {
					ticket.wait();
				} catch (final InterruptedException ignore) {
				}
			}
		}
		return null;
	}

	@Override
	public synchronized void shutdown() {
		if (active) {
			active = false;
			for (final ExecuteThread thread : threads) {
				thread.shutdown();
			}
			synchronized (ticket) {
				ticket.notify();
			}
		}
	}
}

class ExecuteThread extends Thread {
	private static Logger logger = Logger.getLogger(DispatcherImpl.class);
	DispatcherImpl q;

	private boolean alive = true;

	ExecuteThread(final String name, final DispatcherImpl q, final int index) {
		super(name + "[" + index + "]");
		this.q = q;
	}

	@Override
	public void run() {
		while (alive) {
			final Runnable task = q.poll();
			if (task != null) {
				try {
					task.run();
				} catch (final Exception ex) {
					logger.error("Got an exception while running a task:", ex);
				}
			}
		}
	}

	public void shutdown() {
		alive = false;
	}
}
