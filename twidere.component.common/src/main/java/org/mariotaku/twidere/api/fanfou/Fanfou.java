package org.mariotaku.twidere.api.fanfou;

import org.mariotaku.twidere.api.fanfou.api.BlocksResources;
import org.mariotaku.twidere.api.fanfou.api.FavoritesResources;
import org.mariotaku.twidere.api.fanfou.api.FriendshipsResources;
import org.mariotaku.twidere.api.fanfou.api.PhotosResources;
import org.mariotaku.twidere.api.fanfou.api.SearchResources;
import org.mariotaku.twidere.api.fanfou.api.StatusesResources;
import org.mariotaku.twidere.api.fanfou.api.UsersResources;
import org.mariotaku.twidere.api.fanfou.api.DirectMessagesResources;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface Fanfou extends StatusesResources, SearchResources, UsersResources, PhotosResources,
        FriendshipsResources, BlocksResources, FavoritesResources, DirectMessagesResources {
}
