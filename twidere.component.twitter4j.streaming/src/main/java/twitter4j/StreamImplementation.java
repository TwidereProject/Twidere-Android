package twitter4j;

import java.io.IOException;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
interface StreamImplementation {
	void close() throws IOException;

	void next(StreamListener[] listeners, RawStreamListener[] rawStreamListeners) throws TwitterException;

	void onException(Exception ex, StreamListener[] listeners, RawStreamListener[] rawStreamListeners);
}
