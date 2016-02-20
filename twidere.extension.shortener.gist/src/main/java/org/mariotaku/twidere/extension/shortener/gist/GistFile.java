package org.mariotaku.twidere.extension.shortener.gist;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/20.
 */
@JsonObject
public class GistFile {
    @JsonField(name = "content")
    String content;

    GistFile() {
    }

    public GistFile(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
