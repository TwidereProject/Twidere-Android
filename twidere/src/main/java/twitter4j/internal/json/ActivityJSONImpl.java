package twitter4j.internal.json;

import static twitter4j.internal.util.InternalParseUtil.getDate;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Activity;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import java.util.Date;

class ActivityJSONImpl extends TwitterResponseImpl implements Activity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8200474717252861878L;

	private Action action;

	private Date createdAt;

	private User[] sources, targetUsers;

	private Status[] targetObjectStatuses, targetStatuses;

	private UserList[] targetUserLists, targetObjectUserLists;

	private long maxPosition, minPosition;

	private int targetObjectsSize, targetsSize, sourcesSize;

	/* package */ActivityJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public int compareTo(final Activity another) {
		if (another == null) return 0;
		final Date this_date = getCreatedAt(), that_date = another.getCreatedAt();
		if (this_date == null || that_date == null) return 0;
		return -this_date.compareTo(that_date);
	}

	@Override
	public Activity.Action getAction() {
		return action;
	}

	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public long getMaxPosition() {
		return maxPosition;
	}

	@Override
	public long getMinPosition() {
		return minPosition;
	}

	@Override
	public User[] getSources() {
		return sources;
	}

	@Override
	public int getSourcesSize() {
		return sourcesSize;
	}

	@Override
	public int getTargetObjectsSize() {
		return targetObjectsSize;
	}

	@Override
	public Status[] getTargetObjectStatuses() {
		return targetObjectStatuses;
	}

	@Override
	public UserList[] getTargetObjectUserLists() {
		return targetObjectUserLists;
	}

	@Override
	public int getTargetsSize() {
		return targetsSize;
	}

	@Override
	public Status[] getTargetStatuses() {
		return targetStatuses;
	}

	@Override
	public UserList[] getTargetUserLists() {
		return targetUserLists;
	}

	@Override
	public User[] getTargetUsers() {
		return targetUsers;
	}

	@Override
	public String toString() {
		return "ActivityJSONImpl{action=" + action + ", createdAt=" + createdAt + ", sources=" + sources
				+ ", targetUsers=" + targetUsers + ", targetObjects=" + targetObjectStatuses + ", targetStatuses="
				+ targetStatuses + ", maxPosition=" + maxPosition + ", minPosition=" + minPosition
				+ ", targetObjectsSize=" + targetObjectsSize + ", targetsSize=" + targetsSize + ", sourcesSize="
				+ sourcesSize + "}";
	}

	final void init(final JSONObject json) throws TwitterException {
		try {
			action = Action.fromString(getRawString("action", json));
			maxPosition = getLong("max_position", json);
			minPosition = getLong("min_position", json);
			createdAt = getDate("created_at", json, "EEE MMM dd HH:mm:ss z yyyy");
			sourcesSize = getInt("sources_size", json);
			targetsSize = getInt("targets_size", json);
			final JSONArray sources_array = json.getJSONArray("sources");
			final JSONArray targets_array = json.getJSONArray("targets");
			final int sources_size = sources_array.length();
			final int targets_size = targets_array.length();
			if (action == Action.LIST_CREATED) {

			} else if (action == Action.FOLLOW || action == Action.MENTION || action == Action.LIST_MEMBER_ADDED) {
				targetUsers = new User[targets_size];
				for (int i = 0; i < targets_size; i++) {
					targetUsers[i] = new UserJSONImpl(targets_array.getJSONObject(i));
				}
			} else {
				targetStatuses = new Status[targets_size];
				for (int i = 0; i < targets_size; i++) {
					targetStatuses[i] = new StatusJSONImpl(targets_array.getJSONObject(i));
				}
			}
			sources = new User[sources_size];
			for (int i = 0; i < sources_size; i++) {
				sources[i] = new UserJSONImpl(sources_array.getJSONObject(i));
			}
			final JSONArray target_objects_array = json.getJSONArray("target_objects");
			final int target_objects_size = target_objects_array.length();
			if (action == Action.LIST_MEMBER_ADDED) {
				targetObjectUserLists = new UserList[target_objects_size];
				for (int i = 0; i < target_objects_size; i++) {
					targetObjectUserLists[i] = new UserListJSONImpl(target_objects_array.getJSONObject(i));
				}
			} else if (action == Action.LIST_CREATED) {
				targetUserLists = new UserList[targets_size];
				for (int i = 0; i < targets_size; i++) {
					targetUserLists[i] = new UserListJSONImpl(targets_array.getJSONObject(i));
				}
			} else {
				targetObjectStatuses = new Status[target_objects_size];
				for (int i = 0; i < target_objects_size; i++) {
					targetObjectStatuses[i] = new StatusJSONImpl(target_objects_array.getJSONObject(i));
				}
			}
			targetObjectsSize = getInt("target_objects_size", json);
		} catch (final TwitterException te) {
			throw te;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	/* package */
	static ResponseList<Activity> createActivityList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		return createActivityList(res.asJSONArray(), res, conf);
	}

	/* package */
	static ResponseList<Activity> createActivityList(final JSONArray list, final HttpResponse res,
			final Configuration conf) throws TwitterException {
		try {
			final int size = list.length();
			final ResponseList<Activity> users = new ResponseListImpl<Activity>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Activity activity = new ActivityJSONImpl(json);
				users.add(activity);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
