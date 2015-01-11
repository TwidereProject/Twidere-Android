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

package twitter4j.http;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dan Checkoway - dcheckoway at gmail.com
 */
public final class RequestMethod {
	private final String name;
	private static final Map<String, RequestMethod> instances = new HashMap<String, RequestMethod>(5);

	public static final RequestMethod GET = new RequestMethod("GET");
	public static final RequestMethod POST = new RequestMethod("POST");
	public static final RequestMethod DELETE = new RequestMethod("DELETE");
	public static final RequestMethod HEAD = new RequestMethod("HEAD");
	public static final RequestMethod PUT = new RequestMethod("PUT");

	private RequestMethod(final String name) {
		this.name = name;
		instances.put(name, this);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof RequestMethod)) return false;

		final RequestMethod that = (RequestMethod) o;

		if (!name.equals(that.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public final String name() {
		return name;
	}

	@Override
	public String toString() {
		return "RequestMethod{" + "name='" + name + '\'' + '}';
	}

	private Object readResolve() throws ObjectStreamException {
		return getInstance(name);
	}

	private static RequestMethod getInstance(final String name) {
		return instances.get(name);
	}
}
