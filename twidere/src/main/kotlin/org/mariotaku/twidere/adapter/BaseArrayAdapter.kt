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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.adapter.iface.IBaseAdapter
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

open class BaseArrayAdapter<T> @JvmOverloads constructor(context: Context, layoutRes: Int, collection: Collection<T>? = null) : ArrayAdapter<T>(context, layoutRes, collection), Constants, IBaseAdapter, OnSharedPreferenceChangeListener {

    val linkify: TwidereLinkify
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit override var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper

    private val nicknamePrefs: SharedPreferences
    private val colorPrefs: SharedPreferences

    override var textSize: Float = 0f
    override var linkHighlightOption: Int = 0

    override var isProfileImageDisplayed: Boolean = false
    override var isDisplayNameFirst: Boolean = false
    override var isShowAccountColor: Boolean = false

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as BaseArrayAdapter<Any>)
        linkify = TwidereLinkify(OnLinkClickHandler(context, multiSelectManager, preferences))
        nicknamePrefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
        colorPrefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
        nicknamePrefs.registerOnSharedPreferenceChangeListener(this)
        colorPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_DISPLAY_PROFILE_IMAGE, KEY_MEDIA_PREVIEW_STYLE, KEY_DISPLAY_SENSITIVE_CONTENTS -> {
                notifyDataSetChanged()
            }
        }
    }

    override fun setLinkHighlightOption(option: String) {
        val optionInt = Utils.getLinkHighlightingStyleInt(option)
        linkify.setHighlightOption(optionInt)
        if (optionInt == linkHighlightOption) return
        linkHighlightOption = optionInt
    }

}
