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

package twitter4j.internal.json;

import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
@SuppressWarnings("rawtypes")
class PagableResponseListImpl<T> extends ResponseListImpl implements PagableResponseList {
	private static final long serialVersionUID = 9098876089678648404L;
	private final long previousCursor;
	private final long nextCursor;

	PagableResponseListImpl(final int size, final JSONObject json, final HttpResponse res) {
		super(size, res);
		this.previousCursor = InternalParseUtil.getLong("previous_cursor", json);
		this.nextCursor = InternalParseUtil.getLong("next_cursor", json);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPreviousCursor() {
		return previousCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return 0 != nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasPrevious() {
		return 0 != previousCursor;
	}

}
