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
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_COMPOSE

class CreateComposeShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setVisible(true)
        val intent = Intent()
        val launchIntent = Intent(INTENT_ACTION_COMPOSE).apply {
            `package` = BuildConfig.APPLICATION_ID
        }
        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_app_shortcut_compose,
                BitmapFactory.Options().apply { inMutable = true }).apply {
            val appIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher, BitmapFactory.Options().apply {
                inSampleSize = 3
            })
            val canvas = Canvas(this)
            val appIconLeft = (width - appIcon.width).toFloat()
            val appIconTop = (height - appIcon.height).toFloat()
            canvas.drawBitmap(appIcon, appIconLeft, appIconTop, null)
            appIcon.recycle()
        }

        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.action_compose))
        setResult(RESULT_OK, intent)
        finish()
    }
}
