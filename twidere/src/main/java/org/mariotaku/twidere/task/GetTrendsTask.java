package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Trends;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 16/2/24.
 */
public abstract class GetTrendsTask extends AbstractTask<Object, Object, Object> {

    private final Context mContext;
    private final long mAccountId;

    public GetTrendsTask(Context context, final long accountId) {
        this.mContext = context;
        this.mAccountId = accountId;
    }

    public abstract List<Trends> getTrends(@NonNull Twitter twitter) throws TwitterException;

    @Override
    public Object doLongOperation(final Object param) {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, accountHost, false);
        if (twitter == null) return null;
        try {
            final List<Trends> trends = getTrends(twitter);
            storeTrends(mContext.getContentResolver(), getContentUri(), trends);
            return null;
        } catch (final TwitterException e) {
            return null;
        }
    }

    protected abstract Uri getContentUri();

    private static void storeTrends(ContentResolver cr, Uri uri, List<Trends> trendsList) {
        final ArrayList<String> hashtags = new ArrayList<>();
        final ArrayList<ContentValues> hashtagValues = new ArrayList<>();
        if (trendsList != null && trendsList.size() > 0) {
            final ContentValues[] valuesArray = ContentValuesCreator.createTrends(trendsList);
            for (final ContentValues values : valuesArray) {
                final String hashtag = values.getAsString(TwidereDataStore.CachedTrends.NAME).replaceFirst("#", "");
                if (hashtags.contains(hashtag)) {
                    continue;
                }
                hashtags.add(hashtag);
                final ContentValues hashtagValue = new ContentValues();
                hashtagValue.put(TwidereDataStore.CachedHashtags.NAME, hashtag);
                hashtagValues.add(hashtagValue);
            }
            cr.delete(uri, null, null);
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray);
            ContentResolverUtils.bulkDelete(cr, TwidereDataStore.CachedHashtags.CONTENT_URI, TwidereDataStore.CachedHashtags.NAME, hashtags, null, true);
            ContentResolverUtils.bulkInsert(cr, TwidereDataStore.CachedHashtags.CONTENT_URI,
                    hashtagValues.toArray(new ContentValues[hashtagValues.size()]));
        }
    }
}
