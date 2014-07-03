package twitter4j;

public interface MediaUploadResponse extends TwitterResponse {

	public long getId();

	public Image getImage();

	public long getSize();

	public interface Image {

		public int getHeight();

		public String getImageType();

		public int getWidth();
	}
}
