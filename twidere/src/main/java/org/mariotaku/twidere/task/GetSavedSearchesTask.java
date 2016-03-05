package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;
import org.mariotaku.twidere.model.AccountId;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

/**
 * Created by mariotaku on 16/2/13.
 */
public class GetSavedSearchesTask extends AbstractTask<AccountId[], SingleResponse<Object>, Object>
        implements Constants {

    private final Context mContext;

    public GetSavedSearchesTask(Context context) {
        this.mContext = context;
    }

    @Override
    public SingleResponse<Object> doLongOperation(AccountId[] params) {
        final ContentResolver cr = mContext.getContentResolver();
        for (AccountId accountId : params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId.getId(),
                    accountId.getHost(), true);
            if (twitter == null) continue;
            try {
                final ResponseList<SavedSearch> searches = twitter.getSavedSearches();
                final ContentValues[] values = ContentValuesCreator.createSavedSearches(searches,
                        accountId.getId(), accountId.getHost());
                final Expression where = Expression.and(Expression.equalsArgs(SavedSearches.ACCOUNT_ID),
                        Expression.equalsArgs(SavedSearches.ACCOUNT_HOST));
                final String[] whereArgs = {String.valueOf(accountId.getId()), accountId.getHost()};
                cr.delete(SavedSearches.CONTENT_URI, where.getSQL(), whereArgs);
                ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values);
            } catch (TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
            }
        }
        return SingleResponse.getInstance();
    }
}
