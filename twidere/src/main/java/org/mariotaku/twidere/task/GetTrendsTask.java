package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Trends;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.TrendsRefreshedEvent;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/2/24.
 */
public abstract class GetTrendsTask extends AbstractTask<Object, Object, Object> {

    private final Context mContext;
    private final UserKey mAccountId;

    @Inject
    protected Bus mBus;

    public GetTrendsTask(Context context, final UserKey accountKey) {
        GeneralComponentHelper.build(context).inject(this);
        this.mContext = context;
        this.mAccountId = accountKey;
    }

    public abstract List<Trends> getTrends(@NonNull MicroBlog twitter) throws MicroBlogException;

    @Override
    public Object doLongOperation(final Object param) {
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mContext, mAccountId, false);
        if (twitter == null) return null;
        try {
            final List<Trends> trends = getTrends(twitter);
            storeTrends(mContext.getContentResolver(), getContentUri(), trends);
            return null;
        } catch (final MicroBlogException e) {
            return null;
        }
    }

    @Override
    protected void afterExecute(Object callback, Object result) {
        mBus.post(new TrendsRefreshedEvent());
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
            ContentResolverUtils.bulkDelete(cr, TwidereDataStore.CachedHashtags.CONTENT_URI, TwidereDataStore.CachedHashtags.NAME, hashtags, null);
            ContentResolverUtils.bulkInsert(cr, TwidereDataStore.CachedHashtags.CONTENT_URI,
                    hashtagValues.toArray(new ContentValues[hashtagValues.size()]));
        }
    }
}
