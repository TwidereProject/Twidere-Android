package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages

/**
 * Created by mariotaku on 2016/12/14.
 */
class GetSentDirectMessagesTask(context: Context) : GetDirectMessagesTask(context) {

    @Throws(MicroBlogException::class)
    override fun getDirectMessages(twitter: MicroBlog, paging: Paging): ResponseList<DirectMessage> {
        return twitter.getSentDirectMessages(paging)
    }

    override val isOutgoing: Boolean = true

    override val databaseUri: Uri = DirectMessages.Outbox.CONTENT_URI

}
