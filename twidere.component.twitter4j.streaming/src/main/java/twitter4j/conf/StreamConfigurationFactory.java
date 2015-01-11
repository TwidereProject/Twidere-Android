package twitter4j.conf;

public interface StreamConfigurationFactory extends ConfigurationFactory {

	/**
	 * returns the root configuration
	 * 
	 * @return root configuration
	 */
	@Override
	StreamConfiguration getInstance();
}
