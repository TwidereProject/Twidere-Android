package com.twitter;

/**
 * A class for validating Tweet texts.
 */
public class Validator {
	public static final int MAX_TWEET_LENGTH = 140;

	protected int shortUrlLength = 22;
	protected int shortUrlLengthHttps = 23;

	private final Extractor extractor = new Extractor();

	public int getShortUrlLength() {
		return shortUrlLength;
	}

	public int getShortUrlLengthHttps() {
		return shortUrlLengthHttps;
	}

	public int getTweetLength(final String text) {
		int length = text.codePointCount(0, text.length());

		for (final Extractor.Entity urlEntity : extractor.extractURLsWithIndices(text)) {
			length += urlEntity.start - urlEntity.end;
			length += urlEntity.value.toLowerCase().startsWith("https://") ? shortUrlLengthHttps : shortUrlLength;
		}

		return length;
	}

	public boolean isValidTweet(final String text) {
		if (text == null || text.length() == 0) return false;

		for (final char c : text.toCharArray()) {
			if (c == '\uFFFE' || c == '\uuFEFF' || // BOM
					c == '\uFFFF' || // Special
					c >= '\u202A' && c <= '\u202E') return false;
		}

		return getTweetLength(text) <= MAX_TWEET_LENGTH;
	}

	public void setShortUrlLength(final int shortUrlLength) {
		this.shortUrlLength = shortUrlLength;
	}

	public void setShortUrlLengthHttps(final int shortUrlLengthHttps) {
		this.shortUrlLengthHttps = shortUrlLengthHttps;
	}
}
