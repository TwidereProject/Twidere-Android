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

package org.mariotaku.twidere.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrSegment
import kotlinx.android.synthetic.main.fragment_user_qr.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.extension.loadOriginalProfileImage
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.TwidereColorUtils
import org.mariotaku.twidere.util.glide.DeferredTarget
import org.mariotaku.twidere.util.qr.QrCodeData
import org.mariotaku.uniqr.AndroidPlatform
import org.mariotaku.uniqr.UniqR
import java.lang.ref.WeakReference

/**
 * Display QR code to user
 * Created by mariotaku on 2017/4/3.
 */
class UserQrDialogFragment : BaseDialogFragment() {

    private val user: ParcelableUser get() = arguments.getParcelable(EXTRA_USER)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weakThis = WeakReference(this)
        val deferred = Glide.with(context.applicationContext).loadOriginalProfileImage(context,
                user, 0).into(DeferredTarget())
        promiseOnUi {
            val fragment = weakThis.get() ?: return@promiseOnUi
            fragment.qrView.visibility = View.INVISIBLE
            fragment.qrProgress.visibility = View.VISIBLE
        } and deferred.promise.then { drawable ->
            val fragment = weakThis.get() ?: throw InterruptedException()
            val background = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(background)
            drawable.setBounds(0, 0, background.width, background.height)
            drawable.draw(canvas)

            val palette = Palette.from(background).generate()

            val qrData = run {
                val segments = QrSegment.makeSegments(LinkCreator.getUserWebLink(fragment.user).toString())
                return@run QrCodeData(QrCode.encodeSegments(segments, QrCode.Ecc.HIGH, 5, 40, -1, true))
            }
            val uniqr = UniqR(AndroidPlatform(), background, qrData)
            uniqr.scale = 3
            uniqr.qrPatternColor = palette.patternColor
            val result = uniqr.build().produceResult()
            background.recycle()
            return@then result
        }.successUi { bitmap ->
            val fragment = weakThis.get() ?: return@successUi
            fragment.qrView.visibility = View.VISIBLE
            fragment.qrProgress.visibility = View.GONE
            fragment.qrView.setImageDrawable(BitmapDrawable(fragment.resources, bitmap).apply {
                this.setAntiAlias(false)
                this.isFilterBitmap = false
            })
        }.failUi {
            val fragment = weakThis.get() ?: return@failUi
            Toast.makeText(fragment.context, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
            fragment.dismiss()
        }
    }

    companion object {
        private fun getOptimalPatternColor(color: Int): Int {
            val yiq = IntArray(3)
            TwidereColorUtils.colorToYIQ(color, yiq)
            if (yiq[0] > 72) {
                yiq[0] = 72
                return TwidereColorUtils.YIQToColor(Color.alpha(color), yiq)
            }
            return color
        }

        private val Palette.patternColor: Int get() {
            var color = getDarkVibrantColor(0)
            if (color == 0) {
                color = getDominantColor(0)
            }
            if (color == 0) {
                color = getDarkMutedColor(0)
            }
            if (color == 0) {
                return Color.BLACK
            }
            return getOptimalPatternColor(color)
        }
    }
}
