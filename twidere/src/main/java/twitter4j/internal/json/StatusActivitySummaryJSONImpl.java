package twitter4j.internal.json;

import static twitter4j.internal.util.InternalParseUtil.getLong;

import org.json.JSONObject;

import twitter4j.IDs;
import twitter4j.StatusActivitySummary;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

public class StatusActivitySummaryJSONImpl extends TwitterResponseImpl implements StatusActivitySummary {

	private static final long serialVersionUID = -8036116716122700832L;

	private IDs favoriters;
	private IDs repliers;

	private IDs retweeters;
	private long favoritersCount;
	private long repliersCount;
	private long retweetersCount;
	private long descendentReplyCount;

	/* package */StatusActivitySummaryJSONImpl(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */StatusActivitySummaryJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof StatusActivitySummaryJSONImpl)) return false;
		final StatusActivitySummaryJSONImpl other = (StatusActivitySummaryJSONImpl) obj;
		if (descendentReplyCount != other.descendentReplyCount) return false;
		if (favoriters == null) {
			if (other.favoriters != null) return false;
		} else if (!favoriters.equals(other.favoriters)) return false;
		if (favoritersCount != other.favoritersCount) return false;
		if (repliers == null) {
			if (other.repliers != null) return false;
		} else if (!repliers.equals(other.repliers)) return false;
		if (repliersCount != other.repliersCount) return false;
		if (retweeters == null) {
			if (other.retweeters != null) return false;
		} else if (!retweeters.equals(other.retweeters)) return false;
		if (retweetersCount != other.retweetersCount) return false;
		return true;
	}

	@Override
	public long getDescendentReplyCount() {
		return descendentReplyCount;
	}

	@Override
	public IDs getFavoriters() {
		return favoriters;
	}

	@Override
	public long getFavoritersCount() {
		return favoritersCount;
	}

	@Override
	public IDs getRepliers() {
		return repliers;
	}

	@Override
	public long getRepliersCount() {
		return repliersCount;
	}

	@Override
	public IDs getRetweeters() {
		return retweeters;
	}

	@Override
	public long getRetweetersCount() {
		return retweetersCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (descendentReplyCount ^ descendentReplyCount >>> 32);
		result = prime * result + (favoriters == null ? 0 : favoriters.hashCode());
		result = prime * result + (int) (favoritersCount ^ favoritersCount >>> 32);
		result = prime * result + (repliers == null ? 0 : repliers.hashCode());
		result = prime * result + (int) (repliersCount ^ repliersCount >>> 32);
		result = prime * result + (retweeters == null ? 0 : retweeters.hashCode());
		result = prime * result + (int) (retweetersCount ^ retweetersCount >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "StatusActivitySummaryJSONImpl{favoriters=" + favoriters + ", repliers=" + repliers + ", retweeters="
				+ retweeters + ", favoritersCount=" + favoritersCount + ", repliersCount=" + repliersCount
				+ ", retweetersCount=" + retweetersCount + ", descendentReplyCount=" + descendentReplyCount + "}";
	}

	private void init(final JSONObject json) throws TwitterException {
		favoriters = new IDsJSONImpl(json.optString("favoriters"));
		repliers = new IDsJSONImpl(json.optString("repliers"));
		retweeters = new IDsJSONImpl(json.optString("retweeters"));
		favoritersCount = getLong("favoriters_count", json);
		repliersCount = getLong("repliers_count", json);
		retweetersCount = getLong("retweeters_count", json);
		descendentReplyCount = getLong("descendent_reply_count", json);
	}
}
