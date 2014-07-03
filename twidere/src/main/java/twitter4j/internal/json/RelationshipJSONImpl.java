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

import static twitter4j.internal.util.InternalParseUtil.getBoolean;
import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * A data class that has detailed information about a relationship between two
 * users
 * 
 * @author Perry Sakkaris - psakkaris at gmail.com
 * @see <a href="https://dev.twitter.com/docs/api/1.1/get/friendships/show">GET
 *      friendships/show | Twitter Developers</a>
 * @since Twitter4J 2.1.0
 */
/* package */class RelationshipJSONImpl extends TwitterResponseImpl implements Relationship {

	private static final long serialVersionUID = 2816753598969317818L;
	private final long targetUserId;
	private final String targetUserScreenName;
	private final boolean sourceBlockingTarget;
	private final boolean sourceNotificationsEnabled;
	private final boolean sourceFollowingTarget;
	private final boolean sourceFollowedByTarget;
	private final long sourceUserId;
	private final String sourceUserScreenName;
	private final boolean sourceCanDM;
	private final boolean sourceCanMediaTag;
	private final boolean sourceMutingTarget;
	private final boolean sourceMarkedTargetAsSpam;

	/* package */RelationshipJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		this(res, res.asJSONObject());
	}

	/* package */RelationshipJSONImpl(final HttpResponse res, final JSONObject json) throws TwitterException {
		super(res);
		try {
			final JSONObject relationship = json.getJSONObject("relationship");
			final JSONObject sourceJson = relationship.getJSONObject("source");
			final JSONObject targetJson = relationship.getJSONObject("target");
			sourceUserId = getLong("id", sourceJson);
			targetUserId = getLong("id", targetJson);
			sourceUserScreenName = getHTMLUnescapedString("screen_name", sourceJson);
			targetUserScreenName = getHTMLUnescapedString("screen_name", targetJson);
			sourceBlockingTarget = getBoolean("blocking", sourceJson);
			sourceFollowingTarget = getBoolean("following", sourceJson);
			sourceFollowedByTarget = getBoolean("followed_by", sourceJson);
			sourceNotificationsEnabled = getBoolean("notifications_enabled", sourceJson);
			sourceCanDM = getBoolean("can_dm", sourceJson);
			sourceCanMediaTag = getBoolean("can_media_tag", sourceJson);
			sourceMutingTarget = getBoolean("muting", sourceJson);
			sourceMarkedTargetAsSpam = getBoolean("marked_spam", sourceJson);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */RelationshipJSONImpl(final JSONObject json) throws TwitterException {
		this(null, json);
	}

	@Override
	public boolean canSourceDMTarget() {
		return sourceCanDM;
	}

	@Override
	public boolean canSourceMediaTagTarget() {
		return sourceCanMediaTag;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof RelationshipJSONImpl)) return false;
		final RelationshipJSONImpl other = (RelationshipJSONImpl) obj;
		if (sourceBlockingTarget != other.sourceBlockingTarget) return false;
		if (sourceCanDM != other.sourceCanDM) return false;
		if (sourceCanMediaTag != other.sourceCanMediaTag) return false;
		if (sourceFollowedByTarget != other.sourceFollowedByTarget) return false;
		if (sourceFollowingTarget != other.sourceFollowingTarget) return false;
		if (sourceMarkedTargetAsSpam != other.sourceMarkedTargetAsSpam) return false;
		if (sourceMutingTarget != other.sourceMutingTarget) return false;
		if (sourceNotificationsEnabled != other.sourceNotificationsEnabled) return false;
		if (sourceUserId != other.sourceUserId) return false;
		if (sourceUserScreenName == null) {
			if (other.sourceUserScreenName != null) return false;
		} else if (!sourceUserScreenName.equals(other.sourceUserScreenName)) return false;
		if (targetUserId != other.targetUserId) return false;
		if (targetUserScreenName == null) {
			if (other.targetUserScreenName != null) return false;
		} else if (!targetUserScreenName.equals(other.targetUserScreenName)) return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSourceUserId() {
		return sourceUserId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSourceUserScreenName() {
		return sourceUserScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTargetUserId() {
		return targetUserId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTargetUserScreenName() {
		return targetUserScreenName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (sourceBlockingTarget ? 1231 : 1237);
		result = prime * result + (sourceCanDM ? 1231 : 1237);
		result = prime * result + (sourceCanMediaTag ? 1231 : 1237);
		result = prime * result + (sourceFollowedByTarget ? 1231 : 1237);
		result = prime * result + (sourceFollowingTarget ? 1231 : 1237);
		result = prime * result + (sourceMarkedTargetAsSpam ? 1231 : 1237);
		result = prime * result + (sourceMutingTarget ? 1231 : 1237);
		result = prime * result + (sourceNotificationsEnabled ? 1231 : 1237);
		result = prime * result + (int) (sourceUserId ^ sourceUserId >>> 32);
		result = prime * result + (sourceUserScreenName == null ? 0 : sourceUserScreenName.hashCode());
		result = prime * result + (int) (targetUserId ^ targetUserId >>> 32);
		result = prime * result + (targetUserScreenName == null ? 0 : targetUserScreenName.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceBlockingTarget() {
		return sourceBlockingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceFollowedByTarget() {
		return sourceFollowedByTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceFollowingTarget() {
		return sourceFollowingTarget;
	}

	@Override
	public boolean isSourceMarkedTargetAsSpam() {
		return sourceMarkedTargetAsSpam;
	}

	@Override
	public boolean isSourceMutingTarget() {
		return sourceMutingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSourceNotificationsEnabled() {
		return sourceNotificationsEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetFollowedBySource() {
		return sourceFollowingTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetFollowingSource() {
		return sourceFollowedByTarget;
	}

	@Override
	public String toString() {
		return "RelationshipJSONImpl{targetUserId=" + targetUserId + ", targetUserScreenName=" + targetUserScreenName
				+ ", sourceBlockingTarget=" + sourceBlockingTarget + ", sourceNotificationsEnabled="
				+ sourceNotificationsEnabled + ", sourceFollowingTarget=" + sourceFollowingTarget
				+ ", sourceFollowedByTarget=" + sourceFollowedByTarget + ", sourceUserId=" + sourceUserId
				+ ", sourceUserScreenName=" + sourceUserScreenName + ", sourceCanDM=" + sourceCanDM
				+ ", sourceCanMediaTag=" + sourceCanMediaTag + ", sourceMutingTarget=" + sourceMutingTarget
				+ ", sourceMarkedTargetAsSpam=" + sourceMarkedTargetAsSpam + "}";
	}

	/* package */
	static ResponseList<Relationship> createRelationshipList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<Relationship> relationships = new ResponseListImpl<Relationship>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Relationship relationship = new RelationshipJSONImpl(json);
				relationships.add(relationship);
			}
			return relationships;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
