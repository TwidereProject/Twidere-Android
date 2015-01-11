package twitter4j.conf;

public class StreamConfigurationContext {
	private static final StreamConfigurationFactory factory = new BaseStreamConfigurationFactory();

	public static StreamConfiguration getInstance() {
		return factory.getInstance();
	}
}
