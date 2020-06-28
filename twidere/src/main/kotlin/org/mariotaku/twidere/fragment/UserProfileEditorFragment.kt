/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import android.view.*
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.loader.app.LoaderManager
import com.twitter.twittertext.Validator
import kotlinx.android.synthetic.main.fragment_user_profile_editor.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.promiseOnUi
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.dismissDialogFragment
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.AccountUpdate
import org.mariotaku.microblog.library.twitter.model.ProfileUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.loadProfileBanner
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.task.*
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener

class UserProfileEditorFragment : BaseFragment(), OnSizeChangedListener,
        OnClickListener, LoaderCallbacks<SingleResponse<ParcelableUser>>,
        KeyboardShortcutsHandler.TakeAllKeyboardShortcut {

    private var currentTask: AbstractTask<*, *, UserProfileEditorFragment>? = null
    private val accountKey: UserKey
        get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!
    private var user: ParcelableUser? = null
    private var account: AccountDetails? = null
    private var userInfoLoaderInitialized: Boolean = false
    private var getUserInfoCalled: Boolean = false

    override fun onClick(view: View) {
        val user = user ?: return
        val account = account ?: return
        val task = currentTask
        if (task != null && !task.isFinished) return
        when (view.id) {
            R.id.editProfileImage -> {
                val intent = activity?.let {
                    ThemedMediaPickerActivity.withThemed(it)
                            .aspectRatio(1, 1)
                            .maximumSize(512, 512)
                            .containsVideo(false)
                            .build()
                }
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE)
            }
            R.id.editProfileBanner -> {
                val builder = activity?.let {
                    ThemedMediaPickerActivity.withThemed(it)
                            .aspectRatio(3, 1)
                            .maximumSize(1500, 500)
                            .containsVideo(false)
                }
                if (account.type == AccountType.TWITTER) {
                    builder?.addEntry(getString(R.string.remove), "remove_banner", RESULT_REMOVE_BANNER)
                }
                startActivityForResult(builder?.build(), REQUEST_UPLOAD_PROFILE_BANNER_IMAGE)
            }
            R.id.editProfileBackground -> {
                val intent = activity?.let {
                    ThemedMediaPickerActivity.withThemed(it)
                            .containsVideo(false)
                            .build()
                }
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE)
            }
            R.id.setLinkColor -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, user.link_color)
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                startActivityForResult(intent, REQUEST_PICK_LINK_COLOR)
            }
            R.id.setBackgroundColor -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, user.background_color)
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                startActivityForResult(intent, REQUEST_PICK_BACKGROUND_COLOR)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<SingleResponse<ParcelableUser>> {
        progressContainer.visibility = View.VISIBLE
        editProfileContent.visibility = View.GONE
        return ParcelableUserLoader(requireActivity(), accountKey, accountKey, null, arguments,
            omitIntentExtra = false,
            loadFromCache = false
        )
    }

    override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableUser>>,
                                data: SingleResponse<ParcelableUser>) {
        val user = data.data ?: this.user ?: run {
            activity?.finish()
            return
        }
        displayUser(user, data.extras.getParcelable(EXTRA_ACCOUNT))
    }

    override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableUser>>) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_editor, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                val name = ParseUtils.parseString(editName.text)
                val url = ParseUtils.parseString(editUrl.text)
                val location = ParseUtils.parseString(editLocation.text)
                val description = ParseUtils.parseString(editDescription.text)
                val linkColor = linkColor.color
                val backgroundColor = backgroundColor.color
                val task = UpdateProfileTaskInternal(this, accountKey, user, name, url, location,
                        description, linkColor, backgroundColor)
                TaskStarter.execute(task)
                this.currentTask = task
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        if (!Utils.isMyAccount(requireActivity(), accountKey)) {
            activity?.finish()
            return
        }

        val lengthChecker = TwitterValidatorMETLengthChecker(Validator())

        editDescription.setLengthChecker(lengthChecker)

        profileImage.setOnClickListener(this)
        profileBanner.setOnClickListener(this)
        profileBackground.setOnClickListener(this)

        editProfileImage.setOnClickListener(this)
        editProfileBanner.setOnClickListener(this)
        editProfileBackground.setOnClickListener(this)

        setLinkColor.setOnClickListener(this)
        setBackgroundColor.setOnClickListener(this)

        val savedUser = savedInstanceState?.getParcelable<ParcelableUser?>(EXTRA_USER)
        val savedAccount = savedInstanceState?.getParcelable<AccountDetails?>(EXTRA_ACCOUNT)
        if (savedInstanceState != null && savedUser != null && savedAccount != null) {
            displayUser(savedUser, savedAccount)
            editName.setText(savedInstanceState.getString(EXTRA_NAME, savedUser.name))
            editLocation.setText(savedInstanceState.getString(EXTRA_LOCATION, savedUser.location))
            editDescription.setText(savedInstanceState.getString(EXTRA_DESCRIPTION,
                    ParcelableUserUtils.getExpandedDescription(savedUser)))
            editUrl.setText(savedInstanceState.getString(EXTRA_URL, savedUser.url_expanded))
        } else {
            getUserInfo()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_USER, user)
        outState.putParcelable(EXTRA_ACCOUNT, account)
        outState.putString(EXTRA_NAME, ParseUtils.parseString(editName.text))
        outState.putString(EXTRA_DESCRIPTION, ParseUtils.parseString(editDescription.text))
        outState.putString(EXTRA_LOCATION, ParseUtils.parseString(editLocation.text))
        outState.putString(EXTRA_URL, ParseUtils.parseString(editUrl.text))
    }

    override fun onSizeChanged(view: View, w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile_editor, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == FragmentActivity.RESULT_CANCELED || data == null) return
        when (requestCode) {
            REQUEST_UPLOAD_PROFILE_BANNER_IMAGE -> {
                val task = currentTask
                if (task != null && !task.isFinished) return
                currentTask = if (resultCode == RESULT_REMOVE_BANNER) {
                    context?.let { RemoveProfileBannerTaskInternal(it, accountKey) }
                } else {
                    UpdateProfileBannerImageTaskInternal(this, accountKey,
                        data.data!!, true)
                }
            }
            REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE -> {
                val task = currentTask
                if (task != null && !task.isFinished) return
                currentTask = UpdateProfileBackgroundImageTaskInternal(this, accountKey,
                        data.data!!, tile = false, deleteImage = true
                )
            }
            REQUEST_UPLOAD_PROFILE_IMAGE -> {
                val task = currentTask
                if (task != null && !task.isFinished) return
                currentTask = UpdateProfileImageTaskInternal(this, accountKey,
                        data.data!!, true)
            }
            REQUEST_PICK_LINK_COLOR -> {
                if (resultCode == Activity.RESULT_OK) {
                    linkColor.color = data.getIntExtra(EXTRA_COLOR, 0)
                }
            }
            REQUEST_PICK_BACKGROUND_COLOR -> {
                if (resultCode == Activity.RESULT_OK) {
                    backgroundColor.color = data.getIntExtra(EXTRA_COLOR, 0)
                }
            }
        }
        executeAfterFragmentResumed {
            val task = currentTask
            if (task != null && !task.isFinished) {
                TaskStarter.execute(task)
            }
        }
    }

    private fun displayUser(user: ParcelableUser?, account: AccountDetails?) {
        val context = context ?: return
        if (!getUserInfoCalled) return
        if (isDetached || (activity?.isFinishing != false)) return
        getUserInfoCalled = false
        this.user = user
        this.account = account
        if (user != null && account != null) {
            progressContainer.visibility = View.GONE
            editProfileContent.visibility = View.VISIBLE
            editName.setText(user.name)
            editDescription.setText(ParcelableUserUtils.getExpandedDescription(user))
            editLocation.setText(user.location)
            editUrl.setText(if (isEmpty(user.url_expanded)) user.url else user.url_expanded)

            requestManager.loadProfileImage(context, user, 0).into(profileImage)
            requestManager.loadProfileBanner(context, user, resources.displayMetrics.widthPixels)
                    .into(profileBanner)
            requestManager.load(user.profile_background_url).into(profileBackground)

            linkColor.color = user.link_color
            backgroundColor.color = user.background_color

            var canEditUrl = false
            var canEditLocation = false
            var canEditBanner = false
            var canEditBackground = false
            var canEditLinkColor = false
            var canEditBackgroundColor = false

            when (account.type) {
                AccountType.TWITTER -> {
                    canEditUrl = true
                    canEditLocation = true
                    canEditBanner = true
                    canEditBackground = true
                    canEditLinkColor = true
                    canEditBackgroundColor = true
                }
                AccountType.MASTODON -> {
                    canEditBanner = true
                }
                AccountType.FANFOU -> {
                    canEditUrl = true
                    canEditLocation = true
                    canEditBackground = true
                    canEditLinkColor = true
                    canEditBackgroundColor = true
                }
            }
            editProfileBanner.visibility = if (canEditBanner) View.VISIBLE else View.GONE
            editProfileBackground.visibility = if (canEditBackground) View.VISIBLE else View.GONE
            editUrl.visibility = if (canEditUrl) View.VISIBLE else View.GONE
            editLocation.visibility = if (canEditLocation) View.VISIBLE else View.GONE
            setLinkColor.visibility = if (canEditLinkColor) View.VISIBLE else View.GONE
            setBackgroundColor.visibility = if (canEditBackgroundColor) View.VISIBLE else View.GONE
        } else {
            progressContainer.visibility = View.GONE
            editProfileContent.visibility = View.GONE
        }
    }

    private fun getUserInfo() {
        if (activity == null || isDetached) return
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(LOADER_ID_USER)
        getUserInfoCalled = true
        if (userInfoLoaderInitialized) {
            lm.restartLoader(LOADER_ID_USER, null, this)
        } else {
            lm.initLoader(LOADER_ID_USER, null, this)
            userInfoLoaderInitialized = true
        }
    }

    private fun dismissDialogFragment(tag: String) {
        executeAfterFragmentResumed {
            val fm = childFragmentManager
            val f = fm.findFragmentByTag(tag)
            if (f is DialogFragment) {
                f.dismiss()
            }
        }
    }

    private fun setUpdateState(start: Boolean) {
        if (!start) {
            dismissDialogFragment(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
            return
        }
        executeAfterFragmentResumed {
            val fm = childFragmentManager
            val df = ProgressDialogFragment()
            df.show(fm, UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
            df.isCancelable = false
        }
    }

    internal class UpdateProfileTaskInternal(
            fragment: UserProfileEditorFragment,
            accountKey: UserKey,
            private val original: ParcelableUser?,
            private val name: String,
            private val url: String,
            private val location: String,
            private val description: String,
            private val linkColor: Int,
            private val backgroundColor: Int
    ) : AbsAccountRequestTask<Any?, Pair<ParcelableUser, AccountDetails>,
            UserProfileEditorFragment>(fragment.requireContext(), accountKey) {

        init {
            this.callback = fragment
        }

        override fun onExecute(account: AccountDetails, params: Any?): Pair<ParcelableUser, AccountDetails> {
            val orig = this.original
            if (orig != null && !orig.isProfileChanged()) {
                return Pair(orig, account)
            }
            return Pair(when (account.type) {
                AccountType.MASTODON -> updateMastodonProfile(account)
                else -> updateMicroBlogProfile(account)
            }, account)
        }


        override fun onSucceed(callback: UserProfileEditorFragment?, result: Pair<ParcelableUser, AccountDetails>) {
            if (callback == null) return
            promiseOnUi {
                val context = this.callback?.context ?: return@promiseOnUi
                val (user, account) = result
                val task = UpdateAccountInfoTask(context)
                task.params = Pair(account, user)
                TaskStarter.execute(task)
            } and callback.executeAfterFragmentResumed { fragment ->
                fragment.childFragmentManager.dismissDialogFragment(DIALOG_FRAGMENT_TAG)
                fragment.activity?.finish()
            }

        }

        override fun beforeExecute() {
            super.beforeExecute()
            callback?.executeAfterFragmentResumed { fragment ->
                val df = ProgressDialogFragment.show(fragment.childFragmentManager, DIALOG_FRAGMENT_TAG)
                df.isCancelable = false
            }
        }

        private fun updateMicroBlogProfile(account: AccountDetails): ParcelableUser {
            val microBlog = account.newMicroBlogInstance(context = context, cls = MicroBlog::class.java)
            val profileUpdate = ProfileUpdate()
            profileUpdate.name(HtmlEscapeHelper.escapeBasic(name))
            profileUpdate.location(HtmlEscapeHelper.escapeBasic(location))
            profileUpdate.description(HtmlEscapeHelper.escapeBasic(description))
            profileUpdate.url(url)
            profileUpdate.linkColor(linkColor)
            profileUpdate.backgroundColor(backgroundColor)
            val profileImageSize = context.getString(R.string.profile_image_size)
            return microBlog.updateProfile(profileUpdate).toParcelable(account,
                    profileImageSize = profileImageSize)
        }

        private fun updateMastodonProfile(account: AccountDetails): ParcelableUser {
            val mastodon = account.newMicroBlogInstance(context = context, cls = Mastodon::class.java)
            val accountUpdate = AccountUpdate()
            accountUpdate.displayName(name)
            accountUpdate.note(HtmlEscapeHelper.escapeBasic(description))
            return mastodon.updateCredentials(accountUpdate).toParcelable(account)
        }

        private fun ParcelableUser.isProfileChanged(): Boolean {
            if (linkColor != link_color) return true
            if (backgroundColor != background_color) return true
            if (!TextUtils.equals(this@UpdateProfileTaskInternal.name, name)) return true
            if (!TextUtils.equals(description, ParcelableUserUtils.getExpandedDescription(this)))
                return true
            if (!TextUtils.equals(this@UpdateProfileTaskInternal.location, location)) return true
            if (!TextUtils.equals(this@UpdateProfileTaskInternal.url, if (isEmpty(url_expanded)) url else url_expanded))
                return true
            return false
        }

        companion object {

            private const val DIALOG_FRAGMENT_TAG = "updating_user_profile"
        }

    }

    internal class RemoveProfileBannerTaskInternal(
            context: Context,
            accountKey: UserKey
    ) : AbsAccountRequestTask<Any?, Boolean, UserProfileEditorFragment>(context, accountKey) {
        override fun onExecute(account: AccountDetails, params: Any?): Boolean {
            return account.newMicroBlogInstance(context, MicroBlog::class.java)
                    .removeProfileBannerImage().isSuccessful
        }

        override fun onSucceed(callback: UserProfileEditorFragment?, result: Boolean) {
            if (callback == null) return
            val context = callback.context
            callback.getUserInfo()
            Toast.makeText(context, R.string.message_toast_profile_banner_image_updated,
                    Toast.LENGTH_SHORT).show()
        }

        override fun beforeExecute() {
            super.beforeExecute()
            callback?.setUpdateState(true)
        }

    }

    private class UpdateProfileBannerImageTaskInternal(
            fragment: UserProfileEditorFragment,
            accountKey: UserKey,
            imageUri: Uri,
            deleteImage: Boolean
    ) : UpdateProfileBannerImageTask<UserProfileEditorFragment>(fragment.requireContext(), accountKey, imageUri, deleteImage) {

        init {
            callback = fragment
        }

        override fun afterExecute(callback: UserProfileEditorFragment?, result: ParcelableUser?, exception: MicroBlogException?) {
            callback?.setUpdateState(false)
            callback?.getUserInfo()
        }

        override fun beforeExecute() {
            callback?.setUpdateState(true)
        }

    }

    private class UpdateProfileBackgroundImageTaskInternal(
            fragment: UserProfileEditorFragment,
            accountKey: UserKey,
            imageUri: Uri,
            tile: Boolean,
            deleteImage: Boolean
    ) : UpdateProfileBackgroundImageTask<UserProfileEditorFragment>(fragment.requireContext(), accountKey, imageUri,
            tile, deleteImage) {

        init {
            callback = fragment
        }

        override fun afterExecute(callback: UserProfileEditorFragment?, result: ParcelableUser?, exception: MicroBlogException?) {
            super.afterExecute(callback, result, exception)
            callback?.setUpdateState(false)
            callback?.getUserInfo()
        }

        override fun beforeExecute() {
            super.beforeExecute()
            callback?.setUpdateState(true)
        }

    }

    private class UpdateProfileImageTaskInternal(
            fragment: UserProfileEditorFragment,
            accountKey: UserKey,
            imageUri: Uri,
            deleteImage: Boolean
    ) : UpdateProfileImageTask<UserProfileEditorFragment>(fragment.requireContext(), accountKey, imageUri, deleteImage) {

        init {
            callback = fragment
        }

        override fun afterExecute(callback: UserProfileEditorFragment?, result: ParcelableUser?, exception: MicroBlogException?) {
            super.afterExecute(callback, result, exception)
            callback?.setUpdateState(false)
            callback?.getUserInfo()
        }

        override fun beforeExecute() {
            super.beforeExecute()
            callback?.setUpdateState(true)
        }

    }

    companion object {

        private const val LOADER_ID_USER = 1

        private const val REQUEST_UPLOAD_PROFILE_IMAGE = 1
        private const val REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2
        private const val REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE = 3
        private const val REQUEST_PICK_LINK_COLOR = 11
        private const val REQUEST_PICK_BACKGROUND_COLOR = 12

        private const val RESULT_REMOVE_BANNER = 101
        private const val UPDATE_PROFILE_DIALOG_FRAGMENT_TAG = "update_profile"

    }
}
