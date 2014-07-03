package twitter4j;

import java.io.Serializable;
import java.util.Date;

public interface Activity extends TwitterResponse, Comparable<Activity>, Serializable {

	public Action getAction();

	public Date getCreatedAt();

	public long getMaxPosition();

	public long getMinPosition();

	public User[] getSources();

	public int getSourcesSize();

	public int getTargetObjectsSize();

	public Status[] getTargetObjectStatuses();

	public UserList[] getTargetObjectUserLists();

	public int getTargetsSize();

	public Status[] getTargetStatuses();

	public UserList[] getTargetUserLists();

	public User[] getTargetUsers();

	public static enum Action implements Serializable {
		FAVORITE(0x1), FOLLOW(0x2), MENTION(0x3), REPLY(0x4), RETWEET(0x5), LIST_MEMBER_ADDED(0x06), LIST_CREATED(0x07);

		public final static int ACTION_FAVORITE = 0x01;
		public final static int ACTION_FOLLOW = 0x02;
		public final static int ACTION_MENTION = 0x03;
		public final static int ACTION_REPLY = 0x04;
		public final static int ACTION_RETWEET = 0x05;
		public final static int ACTION_LIST_MEMBER_ADDED = 0x06;
		public final static int ACTION_LIST_CREATED = 0x07;

		private final int actionId;

		private Action(final int action) {
			actionId = action;
		}

		public int getActionId() {
			return actionId;
		}

		public static Action fromString(final String string) {
			if ("favorite".equalsIgnoreCase(string)) return FAVORITE;
			if ("follow".equalsIgnoreCase(string)) return FOLLOW;
			if ("mention".equalsIgnoreCase(string)) return MENTION;
			if ("reply".equalsIgnoreCase(string)) return REPLY;
			if ("retweet".equalsIgnoreCase(string)) return RETWEET;
			if ("list_member_added".equalsIgnoreCase(string)) return LIST_MEMBER_ADDED;
			if ("list_created".equalsIgnoreCase(string)) return LIST_CREATED;
			throw new IllegalArgumentException("Unknown action " + string);
		}
	}
}
