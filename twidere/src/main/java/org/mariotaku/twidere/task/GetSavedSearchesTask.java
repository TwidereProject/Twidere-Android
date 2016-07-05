package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SavedSearch;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

/**
 * Created by mariotaku on 16/2/13.
 */
public class GetSavedSearchesTask extends AbstractTask<UserKey[], SingleResponse<Object>, Object> {

    private final Context mContext;

    public GetSavedSearchesTask(Context context) {
        this.mContext = context;
    }

    @Override
    public SingleResponse<Object> doLongOperation(UserKey[] params) {
        final ContentResolver cr = mContext.getContentResolver();
        for (UserKey accountKey : params) {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mContext, accountKey, true);
            if (twitter == null) continue;
            try {
                final ResponseList<SavedSearch> searches = twitter.getSavedSearches();
                final ContentValues[] values = ContentValuesCreator.createSavedSearches(searches,
                        accountKey);
                final Expression where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY);
                final String[] whereArgs = {accountKey.toString()};
                cr.delete(SavedSearches.CONTENT_URI, where.getSQL(), whereArgs);
                ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values);
            } catch (MicroBlogException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
            }
        }
        return SingleResponse.Companion.getInstance();
    }
}
