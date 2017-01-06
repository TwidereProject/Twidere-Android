package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.otto.Bus
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.GetMessagesTaskEvent
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 16/2/14.
 */
abstract class GetDirectMessagesTask(
        protected val context: Context
) : AbstractTask<RefreshTaskParam, List<TwitterWrapper.MessageListResponse>, (Boolean) -> Unit>() {
    @Inject
    protected lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    protected lateinit var preferences: SharedPreferencesWrapper
    @Inject
    protected lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(MicroBlogException::class)
    abstract fun getDirectMessages(twitter: MicroBlog, paging: Paging): ResponseList<DirectMessage>

    protected abstract val databaseUri: Uri

    protected abstract val isOutgoing: Boolean

    public override fun doLongOperation(param: RefreshTaskParam): List<TwitterWrapper.MessageListResponse> {
        val accountKeys = param.accountKeys
        val sinceIds = param.sinceIds
        val maxIds = param.maxIds
        val result = ArrayList<TwitterWrapper.MessageListResponse>()
        var idx = 0
        val loadItemLimit = preferences[loadItemLimitKey]
        for (accountKey in accountKeys) {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: continue
            try {
                val paging = Paging()
                paging.setCount(loadItemLimit)
                var maxId: String? = null
                var sinceId: String? = null
                if (maxIds != null && maxIds[idx] != null) {
                    maxId = maxIds[idx]
                    paging.setMaxId(maxId)
                }
                if (sinceIds != null && sinceIds[idx] != null) {
                    sinceId = sinceIds[idx]
                    val sinceIdLong = NumberUtils.toLong(sinceId, -1)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }
                    if (maxIds == null || sinceIds[idx] == null) {
                        paging.setLatestResults(true)
                    }
                }
                val messages = getDirectMessages(twitter, paging)
                result.add(TwitterWrapper.MessageListResponse(accountKey, maxId, sinceId, messages))
                storeMessages(accountKey, messages, isOutgoing, true)
                errorInfoStore.remove(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey)
            } catch (e: MicroBlogException) {
                if (e.errorCode == ErrorInfo.NO_DIRECT_MESSAGE_PERMISSION) {
                    errorInfoStore[ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey] = ErrorInfoStore.CODE_NO_DM_PERMISSION
                } else if (e.isCausedByNetworkIssue) {
                    errorInfoStore[ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey] = ErrorInfoStore.CODE_NETWORK_ERROR
                }
                if (BuildConfig.DEBUG) {
                    Log.w(TwidereConstants.LOGTAG, e)
                }
                result.add(TwitterWrapper.MessageListResponse(accountKey, e))
            }

            idx++
        }
        return result

    }

    private fun storeMessages(accountKey: UserKey, messages: List<DirectMessage>?, isOutgoing: Boolean, notify: Boolean): Boolean {
        if (messages == null) return true
        val uri = databaseUri
        val valuesArray = messages.map { ContentValuesCreator.createDirectMessage(it, accountKey, isOutgoing) }

        // Delete all rows conflicting before new data inserted.
        //            final Expression deleteWhere = Expression.and(Expression.equals(DirectMessages.ACCOUNT_ID, accountKey),
        //                    Expression.in(new Column(DirectMessages.MESSAGE_ID), new RawItemArray(messageIds)));
        //            final Uri deleteUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, false);
        //            mResolver.delete(deleteUri, deleteWhere.getSQL(), null);


        // Insert previously fetched items.
        val insertUri = UriUtils.appendQueryParameters(uri, TwidereConstants.QUERY_PARAM_NOTIFY, notify)
        ContentResolverUtils.bulkInsert(context.contentResolver, insertUri, valuesArray)
        return false
    }

    override fun beforeExecute() {
        bus.post(GetMessagesTaskEvent(databaseUri, true, null))
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, result: List<TwitterWrapper.MessageListResponse>?) {
        bus.post(GetMessagesTaskEvent(databaseUri, false, AsyncTwitterWrapper.getException(result)))
        handler?.invoke(true)
    }
}
