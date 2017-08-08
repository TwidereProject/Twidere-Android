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

package org.mariotaku.twidere.activity.shortcut

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.bumptech.glide.Glide
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.glide.DeferredTarget
import java.lang.ref.WeakReference

class CreateQuickAccessShortcutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val df = QuickAccessShortcutTypeDialogFragment()
            df.show(supportFragmentManager, "quick_access_shortcut_type")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val actionType = data.getBundleExtra(EXTRA_EXTRAS)?.getString(EXTRA_TYPE)
                val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY) ?: run {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                when (actionType) {
                    "user", "user_timeline", "user_favorites" -> {
                        val selectUserIntent = Intent(this, UserSelectorActivity::class.java)
                        selectUserIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                        selectUserIntent.putExtra(EXTRA_EXTRAS, Bundle {
                            this[EXTRA_TYPE] = actionType
                            this[EXTRA_ACCOUNT_KEY] = accountKey
                        })
                        startActivityForResult(selectUserIntent, REQUEST_SELECT_USER)
                    }
                    "list", "list_timeline" -> {
                        val selectUserListIntent = Intent(this, UserListSelectorActivity::class.java)
                        selectUserListIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                        selectUserListIntent.putExtra(EXTRA_SHOW_MY_LISTS, true)
                        selectUserListIntent.putExtra(EXTRA_EXTRAS, Bundle {
                            this[EXTRA_TYPE] = actionType
                            this[EXTRA_ACCOUNT_KEY] = accountKey
                        })
                        startActivityForResult(selectUserListIntent, REQUEST_SELECT_USER_LIST)
                    }
                    else -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
            REQUEST_SELECT_USER -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                val extras = data.getBundleExtra(EXTRA_EXTRAS)
                val accountKey = extras.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                val actionType = extras.getString(EXTRA_TYPE)
                addUserRelatedShortcut(actionType, accountKey, user)
            }
            REQUEST_SELECT_USER_LIST -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val list = data.getParcelableExtra<ParcelableUserList>(EXTRA_USER_LIST)
                val extras = data.getBundleExtra(EXTRA_EXTRAS)
                val accountKey = extras.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                val actionType = extras.getString(EXTRA_TYPE)
                addUserListRelatedShortcut(actionType, accountKey, list)
            }
        }
    }

    private fun addUserRelatedShortcut(actionType: String?, accountKey: UserKey?, user: ParcelableUser) {
        when (actionType) {
            "user_timeline" -> {
                val launchIntent = IntentUtils.userTimeline(accountKey, user.key,
                        user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
                val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, userColorNameManager.getDisplayName(user,
                            preferences[nameFirstKey]))
                })
                finish()
            }
            "user_favorites" -> {
                val launchIntent = IntentUtils.userTimeline(accountKey, user.key,
                        user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
                val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, userColorNameManager.getDisplayName(user,
                            preferences[nameFirstKey]))
                })
                finish()
            }
            else -> {
                val displayName = userColorNameManager.getDisplayName(user, preferences[nameFirstKey])
                val deferred = Glide.with(this).loadProfileImage(this, user,
                        shapeStyle = preferences[profileImageStyleKey], cornerRadiusRatio = 0.1f,
                        size = getString(R.string.profile_image_size)).into(DeferredTarget())
                val weakThis = WeakReference(this)
                executeAfterFragmentResumed {
                    ProgressDialogFragment.show(it.supportFragmentManager, TAG_LOAD_ICON_PROGRESS)
                } and deferred.promise.successUi { drawable ->
                    val activity = weakThis.get() ?: return@successUi
                    val launchIntent = IntentUtils.userProfile(accountKey, user.key,
                            user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
                    val icon = Bitmap.createBitmap(drawable.intrinsicWidth,
                            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(icon)
                    drawable.setBounds(0, 0, icon.width, icon.height)
                    drawable.draw(canvas)
                    activity.setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                        putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                        putExtra(Intent.EXTRA_SHORTCUT_NAME, displayName)
                    })
                    activity.finish()
                }.failUi {
                    val activity = weakThis.get() ?: return@failUi
                    activity.setResult(Activity.RESULT_CANCELED)
                    activity.finish()
                }
            }
        }
    }

    private fun addUserListRelatedShortcut(actionType: String?, accountKey: UserKey?, list: ParcelableUserList) {
        when (actionType) {
            "list_timeline" -> {
                val launchIntent = IntentUtils.userListTimeline(accountKey, list.id,
                        list.user_key, list.user_screen_name, list.name)
                val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, list.name)
                })
            }
            else -> {
                val launchIntent = IntentUtils.userListDetails(accountKey, list.id,
                        list.user_key, list.user_screen_name, list.name)
                val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, list.name)
                })
            }
        }
        finish()
    }

    private fun onItemSelected(which: Int) {
        val actionType = resources.getStringArray(R.array.values_quick_access_shortcut_types)[which]
        val selectAccountIntent = Intent(this, AccountSelectorActivity::class.java)
        selectAccountIntent.putExtra(EXTRA_EXTRAS, Bundle {
            this[EXTRA_TYPE] = actionType
        })
        if (actionType == "list") {
            selectAccountIntent.putExtra(EXTRA_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
        }
        startActivityForResult(selectAccountIntent, REQUEST_SELECT_ACCOUNT)
    }

    class QuickAccessShortcutTypeDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setItems(R.array.entries_quick_access_shortcut_types) { _, which ->
                (activity as CreateQuickAccessShortcutActivity).onItemSelected(which)
            }
            return builder.create().apply {
                applyOnShow { applyTheme() }
            }
        }

        override fun onCancel(dialog: DialogInterface?) {
            activity?.finish()
        }
    }

    companion object {
        private const val TAG_LOAD_ICON_PROGRESS = "load_icon_progress"
    }
}
