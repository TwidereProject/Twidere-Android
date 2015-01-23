package com.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * A class to extract usernames, lists, hashtags and URLs from Tweet text.
 */
public class Extractor {
	protected boolean extractURLWithoutProtocol = true;

	/**
	 * Fullwidth at sign: '@'
	 */
	private static final char FULLWIDTH_AT_SIGN = '\uff20';

	/**
	 * Fullwidth number sign: '#'
	 */
	private static final char FULLWIDTH_NUMBER_SIGN = '\uff03';

	/**
	 * Create a new extractor.
	 */
	public Extractor() {
	}

	/**
	 * Extract $cashtag references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract cashtags
	 * @return List of cashtags referenced (without the leading $ sign)
	 */
	public List<String> extractCashtags(final String text) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		final ArrayList<String> extracted = new ArrayList<String>();
		for (final Entity entity : extractCashtagsWithIndices(text)) {
			extracted.add(entity.value);
		}

		return extracted;
	}

	/**
	 * Extract $cashtag references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract cashtags
	 * @return List of cashtags referenced (without the leading $ sign)
	 */
	public List<Entity> extractCashtagsWithIndices(final String text) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		// Performance optimization.
		// If text doesn't contain $, text doesn't contain
		// cashtag, so we can simply return an empty list.
		if (text.indexOf('$') == -1) return Collections.emptyList();

		final ArrayList<Entity> extracted = new ArrayList<Entity>();
		final Matcher matcher = Regex.VALID_CASHTAG.matcher(text);

		while (matcher.find()) {
			extracted.add(new Entity(matcher, Entity.Type.CASHTAG, Regex.VALID_CASHTAG_GROUP_CASHTAG_FULL));
		}

		return extracted;
	}

	/**
	 * Extract URLs, @mentions, lists and #hashtag from a given text/tweet.
	 * 
	 * @param text text of tweet
	 * @return list of extracted entities
	 */
	public List<Entity> extractEntitiesWithIndices(final String text) {
		final ArrayList<Entity> entities = new ArrayList<Entity>();
		entities.addAll(extractURLsWithIndices(text));
		entities.addAll(extractHashtagsWithIndices(text, false));
		entities.addAll(extractMentionsOrListsWithIndices(text));
		entities.addAll(extractCashtagsWithIndices(text));

		removeOverlappingEntities(entities);
		return entities;
	}

	/**
	 * Extract #hashtag references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract hashtags
	 * @return List of hashtags referenced (without the leading # sign)
	 */
	public List<String> extractHashtags(final String text) {
		return extractHashtags(text, true);
	}

	public List<String> extractHashtags(final String text, final boolean exclude_duplicate) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		final ArrayList<String> extracted = new ArrayList<String>();
		for (final Entity entity : extractHashtagsWithIndices(text)) {
			if (!exclude_duplicate || !extracted.contains(entity.value)) {
				extracted.add(entity.value);
			}
		}

		return extracted;
	}

	/**
	 * Extract #hashtag references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract hashtags
	 * @return List of hashtags referenced (without the leading # sign)
	 */
	public List<Entity> extractHashtagsWithIndices(final String text) {
		return extractHashtagsWithIndices(text, true);
	}

	/**
	 * Extract @username references from Tweet text. A mention is an occurance
	 * of @username anywhere in a Tweet.
	 * 
	 * @param text of the tweet from which to extract usernames
	 * @return List of usernames referenced (without the leading @ sign)
	 */
	public Set<String> extractMentionedScreennames(final String text) {
		return extractMentionedScreennames(text, true);
	}

	public Set<String> extractMentionedScreennames(final String text, final boolean exclude_duplicate) {
		if (text == null || text.length() == 0) return Collections.emptySet();

		final Set<String> extracted = new HashSet<String>();
		for (final Entity entity : extractMentionedScreennamesWithIndices(text)) {
			if (!exclude_duplicate || !extracted.contains(entity.value)) {
				extracted.add(entity.value);
			}
		}
		return extracted;
	}

	/**
	 * Extract @username references from Tweet text. A mention is an occurance
	 * of @username anywhere in a Tweet.
	 * 
	 * @param text of the tweet from which to extract usernames
	 * @return List of usernames referenced (without the leading @ sign)
	 */
	public List<Entity> extractMentionedScreennamesWithIndices(final String text) {
		final ArrayList<Entity> extracted = new ArrayList<Entity>();
		for (final Entity entity : extractMentionsOrListsWithIndices(text)) {
			if (entity.listSlug == null) {
				extracted.add(entity);
			}
		}
		return extracted;
	}

	public List<Entity> extractMentionsOrListsWithIndices(final String text) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		// Performance optimization.
		// If text doesn't contain @ at all, the text doesn't
		// contain @mention. So we can simply return an empty list.
		boolean found = false;
		for (final char c : text.toCharArray()) {
			if (c == '@' || c == FULLWIDTH_AT_SIGN) {
				found = true;
				break;
			}
		}
		if (!found) return Collections.emptyList();

		final ArrayList<Entity> extracted = new ArrayList<Entity>();
		final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(text);
		while (matcher.find()) {
			final String after = text.substring(matcher.end());
			if (!Regex.INVALID_MENTION_MATCH_END.matcher(after).find()) {
				if (matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST) == null) {
					extracted.add(new Entity(matcher, Entity.Type.MENTION, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME));
				} else {
					extracted.add(new Entity(matcher.start(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME) - 1, matcher
							.end(Regex.VALID_MENTION_OR_LIST_GROUP_LIST), matcher
							.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME), matcher
							.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST), Entity.Type.MENTION));
				}
			}
		}
		return extracted;
	}

	/**
	 * Extract a @username reference from the beginning of Tweet text. A reply
	 * is an occurance of @username at the beginning of a Tweet, preceded by 0
	 * or more spaces.
	 * 
	 * @param text of the tweet from which to extract the replied to username
	 * @return username referenced, if any (without the leading @ sign). Returns
	 *         null if this is not a reply.
	 */
	public String extractReplyScreenname(final String text) {
		if (text == null) return null;

		final Matcher matcher = Regex.VALID_REPLY.matcher(text);
		if (matcher.find()) {
			final String after = text.substring(matcher.end());
			if (Regex.INVALID_MENTION_MATCH_END.matcher(after).find())
				return null;
			else
				return matcher.group(Regex.VALID_REPLY_GROUP_USERNAME);
		} else
			return null;
	}

	/**
	 * Extract URL references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract URLs
	 * @return List of URLs referenced.
	 */
	public List<String> extractURLs(final String text) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		final ArrayList<String> urls = new ArrayList<String>();
		for (final Entity entity : extractURLsWithIndices(text)) {
			urls.add(entity.value);
		}
		return urls;
	}

	/**
	 * Extract URL references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract URLs
	 * @return List of URLs referenced.
	 */
	public List<Entity> extractURLsWithIndices(final String text) {
		if (text == null || text.length() == 0
				|| (extractURLWithoutProtocol ? text.indexOf('.') : text.indexOf(':')) == -1) // Performance
																								// optimization.
			// If text doesn't contain '.' or ':' at all, text doesn't contain
			// URL,
			// so we can simply return an empty list.
			return Collections.emptyList();

		final ArrayList<Entity> urls = new ArrayList<Entity>();

		final Matcher matcher = Regex.VALID_URL.matcher(text);
		while (matcher.find()) {
			if (matcher.group(Regex.VALID_URL_GROUP_PROTOCOL) == null) {
				// skip if protocol is not present and
				// 'extractURLWithoutProtocol' is false
				// or URL is preceded by invalid character.
				if (!extractURLWithoutProtocol
						|| Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN.matcher(
								matcher.group(Regex.VALID_URL_GROUP_BEFORE)).matches()) {
					continue;
				}
			}
			String url = matcher.group(Regex.VALID_URL_GROUP_URL);
			final int start = matcher.start(Regex.VALID_URL_GROUP_URL);
			int end = matcher.end(Regex.VALID_URL_GROUP_URL);
			final Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
			if (tco_matcher.find()) {
				// In the case of t.co URLs, don't allow additional path
				// characters.
				url = tco_matcher.group();
				end = start + url.length();
			}

			urls.add(new Entity(start, end, url, Entity.Type.URL));
		}

		return urls;
	}

	public boolean isExtractURLWithoutProtocol() {
		return extractURLWithoutProtocol;
	}

	/*
	 * Modify Unicode-based indices of the entities to UTF-16 based indices.
	 * 
	 * In UTF-16 based indices, Unicode supplementary characters are counted as
	 * two characters.
	 * 
	 * This method requires that the list of entities be in ascending order by
	 * start index.
	 * 
	 * @param text original text
	 * 
	 * @param entities entities with Unicode based indices
	 */
	public void modifyIndicesFromUnicodeToUTF16(final String text, final List<Entity> entities) {
		final IndexConverter convert = new IndexConverter(text);

		for (final Entity entity : entities) {
			entity.start = convert.codePointsToCodeUnits(entity.start);
			entity.end = convert.codePointsToCodeUnits(entity.end);
		}
	}

	/*
	 * Modify UTF-16-based indices of the entities to Unicode-based indices.
	 * 
	 * In Unicode-based indices, Unicode supplementary characters are counted as
	 * single characters.
	 * 
	 * This method requires that the list of entities be in ascending order by
	 * start index.
	 * 
	 * @param text original text
	 * 
	 * @param entities entities with UTF-16 based indices
	 */
	public void modifyIndicesFromUTF16ToToUnicode(final String text, final List<Entity> entities) {
		final IndexConverter convert = new IndexConverter(text);

		for (final Entity entity : entities) {
			entity.start = convert.codeUnitsToCodePoints(entity.start);
			entity.end = convert.codeUnitsToCodePoints(entity.end);
		}
	}

	public void setExtractURLWithoutProtocol(final boolean extractURLWithoutProtocol) {
		this.extractURLWithoutProtocol = extractURLWithoutProtocol;
	}

	/**
	 * Extract #hashtag references from Tweet text.
	 * 
	 * @param text of the tweet from which to extract hashtags
	 * @param checkUrlOverlap if true, check if extracted hashtags overlap URLs
	 *            and remove overlapping ones
	 * @return List of hashtags referenced (without the leading # sign)
	 */
	private List<Entity> extractHashtagsWithIndices(final String text, final boolean checkUrlOverlap) {
		if (text == null || text.length() == 0) return Collections.emptyList();

		// Performance optimization.
		// If text doesn't contain # at all, text doesn't contain
		// hashtag, so we can simply return an empty list.
		boolean found = false;
		for (final char c : text.toCharArray()) {
			if (c == '#' || c == FULLWIDTH_NUMBER_SIGN) {
				found = true;
				break;
			}
		}
		if (!found) return Collections.emptyList();

		final ArrayList<Entity> extracted = new ArrayList<Entity>();
		final Matcher matcher = Regex.VALID_HASHTAG.matcher(text);

		while (matcher.find()) {
			final String after = text.substring(matcher.end());
			if (!Regex.INVALID_HASHTAG_MATCH_END.matcher(after).find()) {
				extracted.add(new Entity(matcher, Entity.Type.HASHTAG, Regex.VALID_HASHTAG_GROUP_TAG));
			}
		}

		if (checkUrlOverlap) {
			// extract URLs
			final List<Entity> urls = extractURLsWithIndices(text);
			if (!urls.isEmpty()) {
				extracted.addAll(urls);
				// remove overlap
				removeOverlappingEntities(extracted);
				// remove URL entities
				final Iterator<Entity> it = extracted.iterator();
				while (it.hasNext()) {
					final Entity entity = it.next();
					if (entity.getType() != Entity.Type.HASHTAG) {
						it.remove();
					}
				}
			}
		}

		return extracted;
	}

	private void removeOverlappingEntities(final List<Entity> entities) {
		// sort by index
		Collections.<Entity> sort(entities, new Comparator<Entity>() {
			@Override
			public int compare(final Entity e1, final Entity e2) {
				return e1.start - e2.start;
			}
		});

		// Remove overlapping entities.
		// Two entities overlap only when one is URL and the other is
		// hashtag/mention
		// which is a part of the URL. When it happens, we choose URL over
		// hashtag/mention
		// by selecting the one with smaller start index.
		if (!entities.isEmpty()) {
			final Iterator<Entity> it = entities.iterator();
			Entity prev = it.next();
			while (it.hasNext()) {
				final Entity cur = it.next();
				if (prev.getEnd() > cur.getStart()) {
					it.remove();
				} else {
					prev = cur;
				}
			}
		}
	}

	public static class Entity {
		protected int start;

		protected int end;
		protected final String value;
		// listSlug is used to store the list portion of @mention/list.
		protected final String listSlug;
		protected final Type type;
		protected String displayURL = null;

		protected String expandedURL = null;

		public Entity(final int start, final int end, final String value, final String listSlug, final Type type) {
			this.start = start;
			this.end = end;
			this.value = value;
			this.listSlug = listSlug;
			this.type = type;
		}

		public Entity(final int start, final int end, final String value, final Type type) {
			this(start, end, value, null, type);
		}

		public Entity(final Matcher matcher, final Type type, final int groupNumber) {
			// Offset -1 on start index to include @, # symbols for mentions and
			// hashtags
			this(matcher, type, groupNumber, -1);
		}

		public Entity(final Matcher matcher, final Type type, final int groupNumber, final int startOffset) {
			this(matcher.start(groupNumber) + startOffset, matcher.end(groupNumber), matcher.group(groupNumber), type);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;

			if (!(obj instanceof Entity)) return false;

			final Entity other = (Entity) obj;

			if (type.equals(other.type) && start == other.start && end == other.end && value.equals(other.value))
				return true;
			else
				return false;
		}

		public String getDisplayURL() {
			return displayURL;
		}

		public Integer getEnd() {
			return end;
		}

		public String getExpandedURL() {
			return expandedURL;
		}

		public String getListSlug() {
			return listSlug;
		}

		public Integer getStart() {
			return start;
		}

		public Type getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return type.hashCode() + value.hashCode() + start + end;
		}

		public void setDisplayURL(final String displayURL) {
			this.displayURL = displayURL;
		}

		public void setExpandedURL(final String expandedURL) {
			this.expandedURL = expandedURL;
		}

		@Override
		public String toString() {
			return value + "(" + type + ") [" + start + "," + end + "]";
		}

		public enum Type {
			URL, HASHTAG, MENTION, CASHTAG
		}
	}

	/**
	 * An efficient converter of indices between code points and code units.
	 */
	private static final class IndexConverter {
		protected final String text;

		// Keep track of a single corresponding pair of code unit and code point
		// offsets so that we can re-use counting work if the next requested
		// entity is near the most recent entity.
		protected int codePointIndex = 0;
		protected int charIndex = 0;

		IndexConverter(final String text) {
			this.text = text;
		}

		/**
		 * @param codePointIndex Index into the string measured in code points.
		 * @return the code unit index that corresponds to the specified code
		 *         point index.
		 */
		int codePointsToCodeUnits(final int codePointIndex) {
			// Note that offsetByCodePoints accepts negative indices.
			charIndex = text.offsetByCodePoints(charIndex, codePointIndex - this.codePointIndex);
			this.codePointIndex = codePointIndex;
			return charIndex;
		}

		/**
		 * @param charIndex Index into the string measured in code units.
		 * @return The code point index that corresponds to the specified
		 *         character index.
		 */
		int codeUnitsToCodePoints(final int charIndex) {
			if (charIndex < this.charIndex) {
				codePointIndex -= text.codePointCount(charIndex, this.charIndex);
			} else {
				codePointIndex += text.codePointCount(this.charIndex, charIndex);
			}
			this.charIndex = charIndex;

			// Make sure that charIndex never points to the second code unit of
			// a
			// surrogate pair.
			if (charIndex > 0 && Character.isSupplementaryCodePoint(text.codePointAt(charIndex - 1))) {
				this.charIndex -= 1;
			}
			return codePointIndex;
		}
	}
}
