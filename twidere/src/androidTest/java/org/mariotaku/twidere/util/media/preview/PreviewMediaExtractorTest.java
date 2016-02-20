package org.mariotaku.twidere.util.media.preview;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by mariotaku on 16/2/9.
 */
public class PreviewMediaExtractorTest {

    @Test
    public void testGetAuthority() throws Exception {
        assertEquals("www.google.com", PreviewMediaExtractor.getAuthority("http://www.google.com/"));
        assertEquals("twitter.com", PreviewMediaExtractor.getAuthority("https://twitter.com"));
        assertNull(PreviewMediaExtractor.getAuthority("www.google.com/"));
    }

    @Test
    public void testGetPath() throws Exception {
        assertEquals("/", PreviewMediaExtractor.getPath("http://www.example.com/"));
        assertEquals("", PreviewMediaExtractor.getPath("http://www.example.com"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path?with=query"));
        assertEquals("/test/path/", PreviewMediaExtractor.getPath("https://example.com/test/path/?with=query"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path?with=query#fragment"));
        assertEquals("/test/path/", PreviewMediaExtractor.getPath("https://example.com/test/path/?with=query#fragment"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path#fragment"));
        assertEquals("/test/path/", PreviewMediaExtractor.getPath("https://example.com/test/path/#fragment"));
    }
}