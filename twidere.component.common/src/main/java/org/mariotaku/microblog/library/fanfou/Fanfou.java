package org.mariotaku.microblog.library.fanfou;

import org.mariotaku.microblog.library.fanfou.api.BlocksResources;
import org.mariotaku.microblog.library.fanfou.api.FavoritesResources;
import org.mariotaku.microblog.library.fanfou.api.FriendshipsResources;
import org.mariotaku.microblog.library.fanfou.api.PhotosResources;
import org.mariotaku.microblog.library.fanfou.api.SearchResources;
import org.mariotaku.microblog.library.fanfou.api.StatusesResources;
import org.mariotaku.microblog.library.fanfou.api.UsersResources;
import org.mariotaku.microblog.library.fanfou.api.DirectMessagesResources;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface Fanfou extends StatusesResources, SearchResources, UsersResources, PhotosResources,
        FriendshipsResources, BlocksResources, FavoritesResources, DirectMessagesResources {
}
