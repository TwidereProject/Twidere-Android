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

import java.lang.reflect.InvocationTargetException;

import twitter4j.conf.StreamConfiguration;
import twitter4j.conf.StreamConfigurationContext;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public final class DispatcherFactory {
	private final String dispatcherImpl;
	private final StreamConfiguration conf;

	public DispatcherFactory() {
		this(StreamConfigurationContext.getInstance());
	}

	public DispatcherFactory(final StreamConfiguration conf) {
		dispatcherImpl = conf.getDispatcherImpl();
		this.conf = conf;
	}

	/**
	 * returns a Dispatcher instance.
	 * 
	 * @return dispatcher instance
	 */
	public Dispatcher getInstance() {
		try {
			return (Dispatcher) Class.forName(dispatcherImpl).getConstructor(StreamConfiguration.class)
					.newInstance(conf);
		} catch (final InstantiationException e) {
			throw new AssertionError(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final ClassNotFoundException e) {
			throw new AssertionError(e);
		} catch (final ClassCastException e) {
			throw new AssertionError(e);
		} catch (final NoSuchMethodException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new AssertionError(e);
		}
	}
}
