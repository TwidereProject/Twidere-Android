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
class GetReceivedDirectMessagesTask(context: Context) : GetDirectMessagesTask(context) {

    @Throws(MicroBlogException::class)
    override fun getDirectMessages(twitter: MicroBlog, paging: Paging): ResponseList<DirectMessage> {
        return twitter.getDirectMessages(paging)
    }

    override val isOutgoing: Boolean = false


    override val databaseUri: Uri = DirectMessages.Inbox.CONTENT_URI

}
