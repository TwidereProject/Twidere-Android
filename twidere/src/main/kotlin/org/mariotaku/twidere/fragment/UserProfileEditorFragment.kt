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
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.text.Editable
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.widget.Toast
import com.twitter.Validator
import kotlinx.android.synthetic.main.fragment_user_profile_editor.*
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.ProfileUpdate
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.ThemedImagePickerActivity
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.task.UpdateAccountInfoTask
import org.mariotaku.twidere.task.UpdateProfileBackgroundImageTask
import org.mariotaku.twidere.task.UpdateProfileBannerImageTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.AsyncTwitterWrapper.UpdateProfileImageTask
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener

class UserProfileEditorFragment : BaseSupportFragment(), OnSizeChangedListener, TextWatcher,
        OnClickListener, LoaderCallbacks<SingleResponse<ParcelableUser>>,
        KeyboardShortcutsHandler.TakeAllKeyboardShortcut {

    private var currentTask: AbstractTask<*, *, UserProfileEditorFragment>? = null
    private val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
    private var user: ParcelableUser? = null
    private var userInfoLoaderInitialized: Boolean = false
    private var getUserInfoCalled: Boolean = false

    override fun beforeTextChanged(s: CharSequence, length: Int, start: Int, end: Int) {
    }

    override fun onTextChanged(s: CharSequence, length: Int, start: Int, end: Int) {
        updateDoneButton()
    }

    override fun afterTextChanged(s: Editable) {
    }

    override fun onClick(view: View) {
        val user = user
        val task = currentTask
        if (user == null || task != null && !task.isFinished)
            return
        when (view.id) {
            R.id.profileImage -> {
            }
            R.id.profileBanner -> {
            }
            R.id.editProfileImage -> {
                val intent = ThemedImagePickerActivity.withThemed(activity).aspectRatio(1, 1).maximumSize(512, 512).build()
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE)
            }
            R.id.editProfileBanner -> {
                val intent = ThemedImagePickerActivity.withThemed(activity).aspectRatio(3, 1).maximumSize(1500, 500).addEntry(getString(R.string.remove), "remove_banner", RESULT_REMOVE_BANNER).build()
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_BANNER_IMAGE)
            }
            R.id.editProfileBackground -> {
                val intent = ThemedImagePickerActivity.withThemed(activity).build()
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
        return ParcelableUserLoader(activity, accountKey, accountKey, null, arguments, false, false)
    }

    override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableUser>>,
                                data: SingleResponse<ParcelableUser>) {
        if (data.data != null && data.data.key != null) {
            displayUser(data.data)
        } else if (user == null) {
        }
    }

    override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableUser>>) {

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_profile_editor, menu)
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
                val task = UpdateProfileTaskInternal(accountKey, user, name, url, location,
                        description, linkColor, backgroundColor)
                task.params = activity
                task.callback = this
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
        if (!Utils.isMyAccount(activity, accountKey)) {
            activity.finish()
            return
        }

        val lengthChecker = TwitterValidatorMETLengthChecker(Validator())
        editName.addTextChangedListener(this)
        editDescription.addTextChangedListener(this)
        editLocation.addTextChangedListener(this)
        editUrl.addTextChangedListener(this)

        editDescription.setLengthChecker(lengthChecker)

        profileImage.setOnClickListener(this)
        profileBanner.setOnClickListener(this)
        profileBackground.setOnClickListener(this)

        editProfileImage.setOnClickListener(this)
        editProfileBanner.setOnClickListener(this)
        editProfileBackground.setOnClickListener(this)

        setLinkColor.setOnClickListener(this)
        setBackgroundColor.setOnClickListener(this)

        if (savedInstanceState != null && savedInstanceState.getParcelable<Parcelable>(EXTRA_USER) != null) {
            val user = savedInstanceState.getParcelable<ParcelableUser>(EXTRA_USER)!!
            displayUser(user)
            editName.setText(savedInstanceState.getString(EXTRA_NAME, user.name))
            editLocation.setText(savedInstanceState.getString(EXTRA_LOCATION, user.location))
            editDescription.setText(savedInstanceState.getString(EXTRA_DESCRIPTION, ParcelableUserUtils.getExpandedDescription(user)))
            editUrl.setText(savedInstanceState.getString(EXTRA_URL, user.url_expanded))
        } else {
            getUserInfo()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_USER, user)
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
                if (resultCode == RESULT_REMOVE_BANNER) {
                    currentTask = RemoveProfileBannerTaskInternal(accountKey)
                } else {
                    currentTask = UpdateProfileBannerImageTaskInternal(activity, accountKey,
                            data.data, true)
                }
            }
            REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE -> {
                val task = currentTask
                if (task != null && !task.isFinished) return
                currentTask = UpdateProfileBackgroundImageTaskInternal(activity, accountKey,
                        data.data, false, true)
            }
            REQUEST_UPLOAD_PROFILE_IMAGE -> {
                val task = currentTask
                if (task != null && !task.isFinished) return
                currentTask = UpdateProfileImageTaskInternal(activity, accountKey,
                        data.data, true)
            }
            REQUEST_PICK_LINK_COLOR -> {
                if (resultCode == Activity.RESULT_OK) {
                    linkColor.color = data.getIntExtra(EXTRA_COLOR, 0)
                    updateDoneButton()
                }
            }
            REQUEST_PICK_BACKGROUND_COLOR -> {
                if (resultCode == Activity.RESULT_OK) {
                    backgroundColor.color = data.getIntExtra(EXTRA_COLOR, 0)
                    updateDoneButton()
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

    private fun displayUser(user: ParcelableUser?) {
        if (!getUserInfoCalled) return
        getUserInfoCalled = false
        this.user = user
        if (user != null) {
            progressContainer.visibility = View.GONE
            editProfileContent.visibility = View.VISIBLE
            editName.setText(user.name)
            editDescription.setText(ParcelableUserUtils.getExpandedDescription(user))
            editLocation.setText(user.location)
            editUrl.setText(if (isEmpty(user.url_expanded)) user.url else user.url_expanded)
            mediaLoader.displayProfileImage(profileImage, user)
            val defWidth = resources.displayMetrics.widthPixels
            mediaLoader.displayProfileBanner(profileBanner, user.profile_banner_url, defWidth)
            mediaLoader.displayImage(profileBackground, user.profile_background_url)
            linkColor.color = user.link_color
            backgroundColor.color = user.background_color
            if (USER_TYPE_FANFOU_COM == user.key.host) {
                editProfileBanner.visibility = View.GONE
            } else {
                editProfileBanner.visibility = View.VISIBLE
            }
        } else {
            progressContainer.visibility = View.GONE
            editProfileContent.visibility = View.GONE
        }
        updateDoneButton()
    }

    private fun getUserInfo() {
        if (activity == null || isDetached) return
        val lm = loaderManager
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

    private fun updateDoneButton() {

    }

    internal class UpdateProfileTaskInternal(
            private val accountKey: UserKey,
            private val original: ParcelableUser?,
            private val name: String,
            private val url: String,
            private val location: String,
            private val description: String,
            private val linkColor: Int,
            private val backgroundColor: Int
    ) : AbstractTask<Context, SingleResponse<ParcelableUser>, UserProfileEditorFragment>() {

        override fun doLongOperation(context: Context): SingleResponse<ParcelableUser> {
            val credentials = ParcelableCredentialsUtils.getCredentials(context, accountKey) ?: return SingleResponse.Companion.getInstance<ParcelableUser>()
            val twitter = MicroBlogAPIFactory.getInstance(context, credentials,
                    true, true) ?: return SingleResponse.Companion.getInstance<ParcelableUser>()
            try {
                var user: User? = null
                if (isProfileChanged) {
                    val profileUpdate = ProfileUpdate()
                    profileUpdate.name(HtmlEscapeHelper.escapeBasic(name))
                    profileUpdate.location(HtmlEscapeHelper.escapeBasic(location))
                    profileUpdate.description(HtmlEscapeHelper.escapeBasic(description))
                    profileUpdate.url(url)
                    profileUpdate.linkColor(linkColor)
                    profileUpdate.backgroundColor(backgroundColor)
                    user = twitter.updateProfile(profileUpdate)
                }
                if (user == null) {
                    // User profile unchanged
                    return SingleResponse.Companion.getInstance<ParcelableUser>()
                }
                val response = SingleResponse.Companion.getInstance(
                        ParcelableUserUtils.fromUser(user, accountKey))
                response.extras.putParcelable(EXTRA_ACCOUNT, credentials)
                return response
            } catch (e: MicroBlogException) {
                return SingleResponse.Companion.getInstance<ParcelableUser>(e)
            }

        }

        private val isProfileChanged: Boolean
            get() {
                val orig = original ?: return true
                if (linkColor != orig.link_color) return true
                if (backgroundColor != orig.background_color) return true
                if (!TextUtils.equals(name, orig.name)) return true
                if (!TextUtils.equals(description, ParcelableUserUtils.getExpandedDescription(orig)))
                    return true
                if (!TextUtils.equals(location, orig.location)) return true
                if (!TextUtils.equals(url, if (isEmpty(orig.url_expanded)) orig.url else orig.url_expanded))
                    return true
                return false
            }

        override fun afterExecute(callback: UserProfileEditorFragment?, result: SingleResponse<ParcelableUser>) {
            if (callback == null) return
            val activity = callback.activity ?: return
            if (result.hasData()) {
                val account = result.extras.getParcelable<ParcelableAccount>(EXTRA_ACCOUNT)
                if (account != null) {
                    val task = UpdateAccountInfoTask(activity)
                    task.params = Pair(account, result.data)
                    TaskStarter.execute(task)
                }
            }
            callback.executeAfterFragmentResumed { fragment ->
                val f = (fragment as UserProfileEditorFragment).fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG)
                if (f is DialogFragment) {
                    f.dismissAllowingStateLoss()
                }
                f.activity.finish()
            }
        }

        override fun beforeExecute() {
            super.beforeExecute()
            callback?.executeAfterFragmentResumed { fragment ->
                val fm = (fragment as UserProfileEditorFragment).activity.supportFragmentManager
                val df = ProgressDialogFragment.show(fm, DIALOG_FRAGMENT_TAG)
                df.isCancelable = false
            }
        }

        companion object {

            private val DIALOG_FRAGMENT_TAG = "updating_user_profile"
        }

    }

    internal inner class RemoveProfileBannerTaskInternal(private val accountKey: UserKey) : AbstractTask<Any, SingleResponse<Boolean>, UserProfileEditorFragment>() {

        override fun doLongOperation(params: Any): SingleResponse<Boolean> {
            return TwitterWrapper.deleteProfileBannerImage(activity, accountKey)
        }

        override fun afterExecute(callback: UserProfileEditorFragment?, result: SingleResponse<Boolean>) {
            super.afterExecute(callback, result)
            if (result.data != null) {
                getUserInfo()
                Toast.makeText(activity, R.string.profile_banner_image_updated, Toast.LENGTH_SHORT).show()
            } else {
                Utils.showErrorMessage(activity, R.string.action_removing_profile_banner_image,
                        result.exception, true)
            }
            setUpdateState(false)
        }

        override fun beforeExecute() {
            super.beforeExecute()
            setUpdateState(true)
        }

    }

    private inner class UpdateProfileBannerImageTaskInternal(context: Context, accountKey: UserKey,
                                                             imageUri: Uri, deleteImage: Boolean) : UpdateProfileBannerImageTask<UserProfileEditorFragment>(context, accountKey, imageUri, deleteImage) {

        override fun afterExecute(callback: UserProfileEditorFragment?, result: SingleResponse<ParcelableUser>?) {
            super.afterExecute(callback, result)
            setUpdateState(false)
            getUserInfo()
        }

        override fun beforeExecute() {
            super.beforeExecute()
            setUpdateState(true)
        }

    }

    private inner class UpdateProfileBackgroundImageTaskInternal(context: Context, accountKey: UserKey,
                                                                 imageUri: Uri, tile: Boolean,
                                                                 deleteImage: Boolean) : UpdateProfileBackgroundImageTask<UserProfileEditorFragment>(context, accountKey, imageUri, tile, deleteImage) {

        override fun afterExecute(callback: UserProfileEditorFragment?, result: SingleResponse<ParcelableUser>?) {
            super.afterExecute(callback, result)
            setUpdateState(false)
            getUserInfo()
        }

        override fun beforeExecute() {
            super.beforeExecute()
            setUpdateState(true)
        }

    }

    private inner class UpdateProfileImageTaskInternal(context: Context, accountKey: UserKey,
                                                       imageUri: Uri, deleteImage: Boolean) : UpdateProfileImageTask<UserProfileEditorFragment>(context, accountKey, imageUri, deleteImage) {

        override fun afterExecute(callback: UserProfileEditorFragment?, result: SingleResponse<ParcelableUser>?) {
            super.afterExecute(callback, result)
            if (result != null && result.data != null) {
                displayUser(result.data)
            }
            setUpdateState(false)
        }

        override fun beforeExecute() {
            super.beforeExecute()
            setUpdateState(true)
        }

    }

    companion object {

        private val LOADER_ID_USER = 1

        private val REQUEST_UPLOAD_PROFILE_IMAGE = 1
        private val REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2
        private val REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE = 3
        private val REQUEST_PICK_LINK_COLOR = 11
        private val REQUEST_PICK_BACKGROUND_COLOR = 12

        private val RESULT_REMOVE_BANNER = 101
        private val UPDATE_PROFILE_DIALOG_FRAGMENT_TAG = "update_profile"

    }
}
