package twitter4j.conf;

public interface StreamConfiguration extends Configuration {

	int getAsyncNumThreads();

	String getDispatcherImpl();

	int getHttpStreamingReadTimeout();

	String getSiteStreamBaseURL();

	String getStreamBaseURL();

	String getUserStreamBaseURL();

	boolean isJSONStoreEnabled();

	boolean isStallWarningsEnabled();

	boolean isUserStreamRepliesAllEnabled();

}
