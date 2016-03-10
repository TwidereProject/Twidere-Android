package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/6.
 */
@JsonObject
public class TextQueryArguments extends TabArguments {
    @JsonField(name = "query")
    String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putString(EXTRA_QUERY, query);
    }

    @Override
    public String toString() {
        return "TextQueryArguments{" +
                "query='" + query + '\'' +
                "} " + super.toString();
    }
}
