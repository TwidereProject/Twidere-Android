package org.mariotaku.twidere.util.media;

/**
 * Created by mariotaku on 16/1/28.
 */
public class MediaExtra {
    long accountId;
    boolean useThumbor = true;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public boolean isUseThumbor() {
        return useThumbor;
    }

    public void setUseThumbor(boolean useThumbor) {
        this.useThumbor = useThumbor;
    }
}
