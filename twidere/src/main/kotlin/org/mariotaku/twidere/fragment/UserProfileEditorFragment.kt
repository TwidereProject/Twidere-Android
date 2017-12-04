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
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.TextUtils.isEmpty
import android.view.*
import android.view.View.OnClickListener
import android.widget.ImageView
import com.twitter.Validator
import kotlinx.android.synthetic.main.fragment_user_profile_editor.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import org.mariotaku.ktextension.string
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.data.impl.UserLiveData
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.urlPreferred
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.promise.UserProfilePromises
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.TwitterValidatorMETLengthChecker
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener

class UserProfileEditorFragment : BaseFragment(), OnSizeChangedListener,
        OnClickListener, KeyboardShortcutsHandler.TakeAllKeyboardShortcut {

    private val accountKey: UserKey
        get() = arguments!!.accountKey!!

    private var runningPromise: Promise<Any, Exception>? = null

    private lateinit var liveUser: UserLiveData

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        linkHandlerTitle = getString(R.string.title_edit_profile)
        if (!Utils.isMyAccount(activity!!, accountKey)) {
            activity!!.finish()
            return
        }
        liveUser = UserLiveData(activity!!, accountKey, accountKey, null)

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


        liveUser.observe(this, success = { (account, user) ->
            progressContainer.visibility = View.GONE
            editProfileContent.visibility = View.VISIBLE
            displayUser(user, account)
        }, fail = { activity?.finish() })

        val savedUser = savedInstanceState?.user
        val savedAccount = savedInstanceState?.account
        if (savedInstanceState != null && savedUser != null && savedAccount != null) {
            displayUser(savedUser, savedAccount)
            editName.setText(savedInstanceState.getString(EXTRA_NAME, savedUser.name))
            editLocation.setText(savedInstanceState.getString(EXTRA_LOCATION, savedUser.location))
            editDescription.setText(savedInstanceState.getString(EXTRA_DESCRIPTION,
                    ParcelableUserUtils.getExpandedDescription(savedUser)))
            editUrl.setText(savedInstanceState.getString(EXTRA_URL, savedUser.url_expanded))
        } else {
            getUserInfo(false)
        }
    }

    override fun onClick(view: View) {
        val user = liveUser.user ?: return
        val account = liveUser.account ?: return
        val task = runningPromise
        if (task != null && !task.isDone()) return
        when (view.id) {
            R.id.editProfileImage -> {
                val intent = ThemedMediaPickerActivity.withThemed(activity!!)
                        .aspectRatio(1, 1)
                        .maximumSize(512, 512)
                        .containsVideo(false)
                        .build()
                startActivityForResult(intent, REQUEST_UPLOAD_PROFILE_IMAGE)
            }
            R.id.editProfileBanner -> {
                val builder = ThemedMediaPickerActivity.withThemed(activity!!)
                        .aspectRatio(3, 1)
                        .maximumSize(1500, 500)
                        .containsVideo(false)
                if (account.type == AccountType.TWITTER) {
                    builder.addEntry(getString(R.string.remove), "remove_banner", RESULT_REMOVE_BANNER)
                }
                startActivityForResult(builder.build(), REQUEST_UPLOAD_PROFILE_BANNER_IMAGE)
            }
            R.id.editProfileBackground -> {
                val intent = ThemedMediaPickerActivity.withThemed(activity!!)
                        .containsVideo(false)
                        .build()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_editor, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                val user = liveUser.user ?: return true
                if (!profileChanged(user)) return true
                val update = UserProfilePromises.ProfileUpdate(editName.string.orEmpty(),
                        editUrl.string.orEmpty(), editLocation.string.orEmpty(),
                        editDescription.string.orEmpty(), linkColor.color, backgroundColor.color)
                runningPromise = showProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
                        .and(UserProfilePromises.get(context!!).updateProfile(accountKey, update))
                        .and(dismissProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_NAME, editName.string)
        outState.putString(EXTRA_DESCRIPTION, editDescription.string)
        outState.putString(EXTRA_LOCATION, editLocation.string)
        outState.putString(EXTRA_URL, editUrl.string)
    }

    override fun onSizeChanged(view: View, w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_profile_editor, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == FragmentActivity.RESULT_CANCELED || data == null) return
        val promise = runningPromise
        if (promise != null && !promise.isDone()) return
        when (requestCode) {
            REQUEST_UPLOAD_PROFILE_BANNER_IMAGE -> {
                runningPromise = if (resultCode == RESULT_REMOVE_BANNER) {
                    showProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
                            .and(UserProfilePromises.get(context!!).removeBanner(accountKey))
                            .and(dismissProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG))
                } else {
                    showProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
                            .and(UserProfilePromises.get(context!!).updateBanner(accountKey, data.data, true))
                            .and(dismissProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG))
                }
            }
            REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE -> {
                runningPromise = showProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
                        .and(UserProfilePromises.get(context!!).updateBackground(accountKey, data.data, false, true))
                        .and(dismissProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG))
            }
            REQUEST_UPLOAD_PROFILE_IMAGE -> {
                runningPromise = showProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG)
                        .and(UserProfilePromises.get(context!!).updateProfileImage(accountKey, data.data, true))
                        .and(dismissProgressDialog(UPDATE_PROFILE_DIALOG_FRAGMENT_TAG))
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
    }

    private fun getUserInfo(omitIntentExtra: Boolean) {
        if (liveUser.user != null || omitIntentExtra) {
            liveUser.extraUser = null
        } else {
            liveUser.extraUser = arguments?.user
        }
        liveUser.load()
        if (liveUser.user == null) {
            progressContainer.visibility = View.VISIBLE
            editProfileContent.visibility = View.GONE
        }
    }

    private fun displayUser(user: ParcelableUser, account: AccountDetails) {
        val activity = this.activity ?: return
        if (isDetached || activity.isFinishing) return
        progressContainer.visibility = View.GONE
        editProfileContent.visibility = View.VISIBLE
        editName.setText(user.name)
        editDescription.setText(ParcelableUserUtils.getExpandedDescription(user))
        editLocation.setText(user.location)
        editUrl.setText(if (isEmpty(user.url_expanded)) user.url else user.url_expanded)

        requestManager.loadProfileImage(activity, user,
                ImageShapeStyle.SHAPE_RECTANGLE).into(profileImage)
        requestManager.loadProfileBanner(activity, user, resources.displayMetrics.widthPixels)
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
                canEditBackground = false
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
    }

    private fun profileChanged(user: ParcelableUser): Boolean {
        if (user.link_color != linkColor.color) return true
        if (user.background_color != backgroundColor.color) return true
        if (user.name != editName.string) return true
        if (ParcelableUserUtils.getExpandedDescription(user) != editDescription.string) return true
        if (user.location != editLocation.string) return true
        if (user.urlPreferred != editUrl.string) return true
        return false
    }

    companion object {

        private val REQUEST_UPLOAD_PROFILE_IMAGE = 1
        private val REQUEST_UPLOAD_PROFILE_BANNER_IMAGE = 2
        private val REQUEST_UPLOAD_PROFILE_BACKGROUND_IMAGE = 3
        private val REQUEST_PICK_LINK_COLOR = 11
        private val REQUEST_PICK_BACKGROUND_COLOR = 12

        private val RESULT_REMOVE_BANNER = 101
        private val UPDATE_PROFILE_DIALOG_FRAGMENT_TAG = "update_profile"

        private var ImageView.color: Int
            get() = (drawable as? ColorDrawable)?.color ?: Color.TRANSPARENT
            set(value) = setImageDrawable(ColorDrawable(value))

    }
}
