/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.promise

import android.app.Application
import android.net.Uri
import nl.komponents.kovenant.Promise
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.model.mastodon.AccountUpdate
import org.mariotaku.microblog.library.model.microblog.ProfileUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.promise.accountTask
import org.mariotaku.twidere.extension.promise.toastOnResult
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder

class UserProfilePromises private constructor(val application: Application) {

    private val profileImageSize = ModelCreationConfig.obtain(application)

    fun updateProfileImage(accountKey: UserKey, imageUri: Uri, deleteImage: Boolean): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        return@accountTask UpdateStatusPromise.getBodyFromMedia(application, imageUri, ParcelableMedia.Type.IMAGE,
                deleteImage, false, null, false, null).use {
            when (account.type) {
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                    return@use mastodon.updateCredentials(AccountUpdate().avatar(it.body))
                            .toParcelable(account)
                }
                else -> {
                    val microBlog = account.newMicroBlogInstance(application, MicroBlog::class.java)
                    microBlog.updateProfileImage(it.body)
                    // Wait for 5 seconds, see
                    // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                    Thread.sleep(5000L)
                    return@use microBlog.verifyCredentials().toParcelable(account,
                            creationConfig = profileImageSize)
                }
            }
        }
    }.toastOnResult(application) {
        return@toastOnResult application.getString(R.string.message_toast_profile_image_updated)
    }

    fun updateBackground(accountKey: UserKey, imageUri: Uri, tile: Boolean, deleteImage: Boolean):
            Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        return@accountTask UpdateStatusPromise.getBodyFromMedia(application, imageUri, ParcelableMedia.Type.IMAGE,
                deleteImage, false, null, false, null).use {
            when (account.type) {
                AccountType.TWITTER, AccountType.FANFOU, AccountType.STATUSNET -> {
                    val microBlog = account.newMicroBlogInstance(application, MicroBlog::class.java)
                    microBlog.updateProfileBackgroundImage(it.body, tile)
                    // Wait for 5 seconds, see
                    // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                    Thread.sleep(5000L)
                    return@use microBlog.verifyCredentials().toParcelable(account,
                            creationConfig = profileImageSize)
                }
                else -> {
                    throw APINotSupportedException("Update background image", account.type)
                }
            }
        }
    }.toastOnResult(application) {
        return@toastOnResult application.getString(R.string.message_toast_profile_background_image_updated)
    }

    fun removeBanner(accountKey: UserKey): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.TWITTER, AccountType.STATUSNET -> {
                val microBlog = account.newMicroBlogInstance(application, MicroBlog::class.java)
                microBlog.removeProfileBannerImage()
                // Wait for 5 seconds, see
                // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                Thread.sleep(5000L)
                return@accountTask microBlog.verifyCredentials().toParcelable(account,
                        creationConfig = profileImageSize)
            }
            else -> {
                throw APINotSupportedException("Remove banner", account.type)
            }
        }
    }.toastOnResult(application) {
        return@toastOnResult application.getString(R.string.message_toast_profile_background_image_updated)
    }

    fun updateBanner(accountKey: UserKey, imageUri: Uri, deleteImage: Boolean): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        return@accountTask UpdateStatusPromise.getBodyFromMedia(application, imageUri, ParcelableMedia.Type.IMAGE,
                deleteImage, false, null, false, null).use {
            when (account.type) {
                AccountType.TWITTER, AccountType.FANFOU, AccountType.STATUSNET -> {
                    val microBlog = account.newMicroBlogInstance(application, MicroBlog::class.java)
                    microBlog.updateProfileBannerImage(it.body)
                    // Wait for 5 seconds, see
                    // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                    Thread.sleep(5000L)
                    return@use microBlog.verifyCredentials().toParcelable(account,
                            creationConfig = profileImageSize)
                }
                else -> {
                    throw APINotSupportedException("Update banner", account.type)
                }
            }
        }
    }.toastOnResult(application) {
        return@toastOnResult application.getString(R.string.message_toast_profile_background_image_updated)
    }


    fun updateProfile(accountKey: UserKey, update: ProfileUpdate): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.MASTODON -> {
                return@accountTask updateMastodonProfile(account, update)
            }
            else -> {
                return@accountTask updateMicroBlogProfile(account, update)
            }
        }
    }

    private fun updateMicroBlogProfile(account: AccountDetails, update: ProfileUpdate): ParcelableUser {
        val microBlog = account.newMicroBlogInstance(context = application, cls = MicroBlog::class.java)
        val profileUpdate = ProfileUpdate()
        profileUpdate.name(HtmlEscapeHelper.escapeBasic(update.name))
        profileUpdate.location(HtmlEscapeHelper.escapeBasic(update.location))
        profileUpdate.description(HtmlEscapeHelper.escapeBasic(update.description))
        profileUpdate.url(update.url)
        profileUpdate.linkColor(update.linkColor)
        profileUpdate.backgroundColor(update.backgroundColor)
        return microBlog.updateProfile(profileUpdate).toParcelable(account,
                creationConfig = profileImageSize)
    }

    private fun updateMastodonProfile(account: AccountDetails, update: ProfileUpdate): ParcelableUser {
        val mastodon = account.newMicroBlogInstance(context = application, cls = Mastodon::class.java)
        val accountUpdate = AccountUpdate()
        accountUpdate.displayName(update.name)
        accountUpdate.note(HtmlEscapeHelper.escapeBasic(update.description))
        return mastodon.updateCredentials(accountUpdate).toParcelable(account)
    }


    data class ProfileUpdate(
            val name: String,
            val url: String,
            val location: String,
            val description: String,
            val linkColor: Int,
            val backgroundColor: Int
    )

    companion object : ApplicationContextSingletonHolder<UserProfilePromises>(::UserProfilePromises)
}
