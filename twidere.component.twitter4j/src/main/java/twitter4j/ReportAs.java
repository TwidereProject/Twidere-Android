package twitter4j;

public enum ReportAs {
	ABUSE("abuse"), COMPROMISED("compromised"), SPAM("spam");

	private String type;

	ReportAs(final String type) {
		this.type = type;
	}

	public String value() {
		return type;
	}

	public static ReportAs parse(final String string) {
		try {
			return ReportAs.valueOf(string.toUpperCase());
		} catch (final Exception e) {
			return SPAM;
		}
	}
}
