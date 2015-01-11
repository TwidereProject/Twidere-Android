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

package twitter4j;

import org.json.JSONObject;

import twitter4j.internal.util.InternalParseUtil;

/**
 * StatusDeletionNotice implementation. This class is NOT intended to be
 * extended but left non-final for the ease of mock testing.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
class StatusDeletionNoticeImpl implements StatusDeletionNotice, java.io.Serializable {

	private final long statusId;
	private final long userId;
	private static final long serialVersionUID = 1723338404242596062L;

	/* package */StatusDeletionNoticeImpl(final JSONObject status) {
		statusId = InternalParseUtil.getLong("id", status);
		userId = InternalParseUtil.getLong("user_id", status);
	}

	@Override
	public int compareTo(final StatusDeletionNotice that) {
		final long delta = statusId - that.getStatusId();
		if (delta < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) delta;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final StatusDeletionNoticeImpl that = (StatusDeletionNoticeImpl) o;

		if (statusId != that.statusId) return false;
		if (userId != that.userId) return false;

		return true;
	}

	@Override
	public long getStatusId() {
		return statusId;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		int result = (int) (statusId ^ statusId >>> 32);
		result = 31 * result + (int) (userId ^ userId >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "StatusDeletionNoticeImpl{" + "statusId=" + statusId + ", userId=" + userId + '}';
	}
}
