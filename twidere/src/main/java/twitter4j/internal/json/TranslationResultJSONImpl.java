package twitter4j.internal.json;

import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;
import static twitter4j.internal.util.InternalParseUtil.getUnescapedString;

import org.json.JSONObject;

import twitter4j.TranslationResult;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

public class TranslationResultJSONImpl extends TwitterResponseImpl implements TranslationResult {

	private static final long serialVersionUID = 2923323223626332587L;
	private long id;
	private String lang;
	private String translatedLang;
	private String translationType;
	private String text;

	/* package */TranslationResultJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */TranslationResultJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof TranslationResultJSONImpl)) return false;
		final TranslationResultJSONImpl other = (TranslationResultJSONImpl) obj;
		if (id != other.id) return false;
		if (lang == null) {
			if (other.lang != null) return false;
		} else if (!lang.equals(other.lang)) return false;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		if (translatedLang == null) {
			if (other.translatedLang != null) return false;
		} else if (!translatedLang.equals(other.translatedLang)) return false;
		if (translationType == null) {
			if (other.translationType != null) return false;
		} else if (!translationType.equals(other.translationType)) return false;
		return true;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getTranslatedLong() {
		return translatedLang;
	}

	@Override
	public String getTranslationType() {
		return translationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		result = prime * result + (lang == null ? 0 : lang.hashCode());
		result = prime * result + (text == null ? 0 : text.hashCode());
		result = prime * result + (translatedLang == null ? 0 : translatedLang.hashCode());
		result = prime * result + (translationType == null ? 0 : translationType.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "TranslationResultJSONImpl{id=" + id + ", lang=" + lang + ", translatedLang=" + translatedLang
				+ ", translationType=" + translationType + ", text=" + text + "}";
	}

	private void init(final JSONObject json) {
		id = getLong("id", json);
		lang = getRawString("lang", json);
		translatedLang = getRawString("translated_lang", json);
		translationType = getRawString("translation_type", json);
		text = getUnescapedString("text", json);
	}

}
