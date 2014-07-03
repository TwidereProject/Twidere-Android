package twitter4j;

public interface TranslationResult extends TwitterResponse {

	public long getId();

	public String getLang();

	public String getText();

	public String getTranslatedLong();

	public String getTranslationType();

}
