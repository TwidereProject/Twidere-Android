package org.mariotaku.twidere.model.util

import android.text.TextUtils
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.extension.model.api.getProfileImageOfSize
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.UserColorNameManager

/**
 * Processing ParcelableUser
 *
 * Created by mariotaku on 16/2/24.
 */
object ParcelableUserUtils {

    fun fromUser(user: User, accountKey: UserKey?, accountType: String?, position: Long = 0,
            profileImageSize: String = "normal"): ParcelableUser {
        val urlEntities = user.urlEntities
        val obj = ParcelableUser()
        obj.position = position
        obj.account_key = accountKey
        obj.key = UserKeyUtils.fromUser(user)
        obj.created_at = user.createdAt?.time ?: -1
        obj.is_protected = user.isProtected
        obj.is_verified = user.isVerified
        obj.name = user.name
        obj.screen_name = user.screenName
        obj.description_plain = user.description
        val userDescription = InternalTwitterContentUtils.formatUserDescription(user)
        if (userDescription != null) {
            obj.description_unescaped = userDescription.first
            obj.description_spans = userDescription.second
        }
        obj.location = user.location
        obj.profile_image_url = user.getProfileImageOfSize(profileImageSize)
        obj.profile_banner_url = user.profileBannerUrl
        obj.profile_background_url = user.profileBackgroundImageUrlHttps
        if (TextUtils.isEmpty(obj.profile_background_url)) {
            obj.profile_background_url = user.profileBackgroundImageUrl
        }
        obj.url = user.url
        if (obj.url != null && urlEntities.isNotNullOrEmpty()) {
            obj.url_expanded = urlEntities[0].expandedUrl
        }
        obj.is_follow_request_sent = user.isFollowRequestSent == true
        obj.followers_count = user.followersCount
        obj.friends_count = user.friendsCount
        obj.statuses_count = user.statusesCount
        obj.favorites_count = user.favouritesCount
        obj.listed_count = user.listedCount
        obj.media_count = user.mediaCount
        obj.is_following = user.isFollowing == true
        obj.background_color = parseColor(user.profileBackgroundColor)
        obj.link_color = parseColor(user.profileLinkColor)
        obj.text_color = parseColor(user.profileTextColor)
        obj.user_type = accountType
        obj.is_cache = false
        obj.is_basic = false

        val extras = ParcelableUser.Extras()
        extras.ostatus_uri = user.ostatusUri
        extras.blocking = user.isBlocking == true
        extras.blocked_by = user.isBlockedBy == true
        extras.followed_by = user.isFollowedBy == true
        extras.muting = user.isMuting == true
        extras.statusnet_profile_url = user.statusnetProfileUrl
        extras.profile_image_url_original = user.profileImageUrlOriginal
        extras.pinned_status_ids = user.pinnedTweetIds
        extras.groups_count = user.groupsCount
        extras.unique_id = user.uniqueId
        obj.extras = extras
        return obj
    }

    fun fromUsers(users: Array<User>?, accountKey: UserKey?, accountType: String?,
            profileImageSize: String = "normal"): Array<ParcelableUser>? {
        return users?.map {
            fromUser(it, accountKey, accountType, profileImageSize = profileImageSize)
        }?.toTypedArray()
    }

    fun parseColor(colorString: String?): Int {
        if (colorString == null) return 0
        var str: String = colorString
        if (!str.startsWith("#")) {
            str = "#" + str
        }
        return ParseUtils.parseColor(str, 0)
    }

    fun getProfileBannerUrl(user: ParcelableUser): String? {
        if (!TextUtils.isEmpty(user.profile_banner_url)) return user.profile_banner_url
        if (USER_TYPE_FANFOU_COM == user.key.host) {
            return user.profile_background_url
        }
        return null
    }

    fun updateExtraInformation(user: ParcelableUser, account: AccountDetails,
            manager: UserColorNameManager) {
        user.account_color = account.color
        user.color = manager.getUserColor(user.key)
        user.nickname = manager.getUserNickname(user.key)
    }

    fun getExpandedDescription(user: ParcelableUser): String {
        if (TextUtils.isEmpty(user.description_unescaped)) {
            return user.description_plain
        }
        if (user.description_spans != null) {
            // TODO expand description
        }
        return user.description_unescaped
    }
}
