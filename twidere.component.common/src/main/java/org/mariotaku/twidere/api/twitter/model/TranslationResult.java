package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.twidere.api.twitter.model.TwitterResponse;

public interface TranslationResult extends TwitterResponse {

	public long getId();

	public String getLang();

	public String getText();

	public String getTranslatedLang();

	public String getTranslationType();

}
