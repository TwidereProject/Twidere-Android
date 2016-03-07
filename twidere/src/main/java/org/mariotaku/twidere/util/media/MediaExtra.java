package org.mariotaku.twidere.util.media;

import org.mariotaku.twidere.model.AccountKey;

/**
 * Created by mariotaku on 16/1/28.
 */
public class MediaExtra {
    AccountKey accountKey;
    boolean useThumbor = true;
    String fallbackUrl;
    boolean skipUrlReplacing;

    public AccountKey getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(AccountKey accountKey) {
        this.accountKey = accountKey;
    }

    public boolean isUseThumbor() {
        return useThumbor;
    }

    public void setUseThumbor(boolean useThumbor) {
        this.useThumbor = useThumbor;
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    public boolean isSkipUrlReplacing() {
        return skipUrlReplacing;
    }

    public void setSkipUrlReplacing(boolean skipUrlReplacing) {
        this.skipUrlReplacing = skipUrlReplacing;
    }
}
