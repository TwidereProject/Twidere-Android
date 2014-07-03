package twitter4j.api;

import twitter4j.MediaUploadResponse;
import twitter4j.TwitterException;

import java.io.File;
import java.io.InputStream;

public interface MediaResources {

	public MediaUploadResponse uploadMedia(File file) throws TwitterException;

	public MediaUploadResponse uploadMedia(String fileName, InputStream fileBody, String fileType)
			throws TwitterException;

}
