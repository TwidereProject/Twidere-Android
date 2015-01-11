/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getDate;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;
import static twitter4j.internal.util.InternalParseUtil.getUnescapedString;

/**
 * A data class representing sent/received direct message.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class DirectMessageJSONImpl extends TwitterResponseImpl implements DirectMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8809144846145143089L;
	private long id;
	private String text;
	private String rawText;
	private long senderId;
	private long recipientId;
	private Date createdAt;
	private String senderScreenName;
	private String recipientScreenName;

	private UserMentionEntity[] userMentionEntities;
	private URLEntity[] urlEntities;
	private HashtagEntity[] hashtagEntities;
	private MediaEntity[] mediaEntities;

	private User sender;

	private User recipient;

	/* package */DirectMessageJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */DirectMessageJSONImpl(final JSONObject json) throws TwitterException {
		init(json);
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof DirectMessage && ((DirectMessage) obj).getId() == id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		return id;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	@Override
	public String getRawText() {
		return rawText;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getRecipient() {
		return recipient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRecipientId() {
		return recipientId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRecipientScreenName() {
		return recipientScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getSender() {
		return sender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSenderId() {
		return senderId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSenderScreenName() {
		return senderScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return text;
	}

	@Override
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

    @Override
    public String toString() {
        return "DirectMessageJSONImpl{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", rawText='" + rawText + '\'' +
                ", senderId=" + senderId +
                ", recipientId=" + recipientId +
                ", createdAt=" + createdAt +
                ", senderScreenName='" + senderScreenName + '\'' +
                ", recipientScreenName='" + recipientScreenName + '\'' +
                ", userMentionEntities=" + Arrays.toString(userMentionEntities) +
                ", urlEntities=" + Arrays.toString(urlEntities) +
                ", hashtagEntities=" + Arrays.toString(hashtagEntities) +
                ", mediaEntities=" + Arrays.toString(mediaEntities) +
                ", sender=" + sender +
                ", recipient=" + recipient +
                '}';
    }

    private void init(final JSONObject json) throws TwitterException {
		id = getLong("id", json);
		text = getUnescapedString("text", json);
		rawText = getRawString("text", json);
		senderId = getLong("sender_id", json);
		recipientId = getLong("recipient_id", json);
		createdAt = getDate("created_at", json);
		senderScreenName = getRawString("sender_screen_name", json);
		recipientScreenName = getRawString("recipient_screen_name", json);
		if (!json.isNull("entities")) {
			try {
				final JSONObject entities = json.getJSONObject("entities");
				int len;
				if (!entities.isNull("user_mentions")) {
					final JSONArray userMentionsArray = entities.getJSONArray("user_mentions");
					len = userMentionsArray.length();
					userMentionEntities = new UserMentionEntity[len];
					for (int i = 0; i < len; i++) {
						userMentionEntities[i] = new UserMentionEntityJSONImpl(userMentionsArray.getJSONObject(i));
					}
				}
				if (!entities.isNull("urls")) {
					final JSONArray urlsArray = entities.getJSONArray("urls");
					len = urlsArray.length();
					urlEntities = new URLEntity[len];
					for (int i = 0; i < len; i++) {
						urlEntities[i] = new URLEntityJSONImpl(urlsArray.getJSONObject(i));
					}
				}
				if (!entities.isNull("hashtags")) {
					final JSONArray hashtagsArray = entities.getJSONArray("hashtags");
					len = hashtagsArray.length();
					hashtagEntities = new HashtagEntity[len];
					for (int i = 0; i < len; i++) {
						hashtagEntities[i] = new HashtagEntityJSONImpl(hashtagsArray.getJSONObject(i));
					}
				}
				if (!entities.isNull("media")) {
					final JSONArray mediaArray = entities.getJSONArray("media");
					len = mediaArray.length();
					mediaEntities = new MediaEntity[len];
					for (int i = 0; i < len; i++) {
						mediaEntities[i] = new MediaEntityJSONImpl(mediaArray.getJSONObject(i));
					}
				}
			} catch (final JSONException jsone) {
				throw new TwitterException(jsone);
			}
		}
		try {
			sender = new UserJSONImpl(json.getJSONObject("sender"));
			recipient = new UserJSONImpl(json.getJSONObject("recipient"));
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	/* package */
	static ResponseList<DirectMessage> createDirectMessageList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<DirectMessage> directMessages = new ResponseListImpl<DirectMessage>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final DirectMessage directMessage = new DirectMessageJSONImpl(json);
				directMessages.add(directMessage);
			}
			return directMessages;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
