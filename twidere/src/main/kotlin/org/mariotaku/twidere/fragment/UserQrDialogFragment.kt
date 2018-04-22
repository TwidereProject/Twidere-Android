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

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrSegment
import kotlinx.android.synthetic.main.fragment_user_qr.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.constant.qrArtEnabledKey
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SaveFileResult
import org.mariotaku.twidere.provider.ShareProvider
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.LinkCreator
import org.mariotaku.twidere.util.TwidereColorUtils
import org.mariotaku.twidere.util.qr.QrCodeData
import org.mariotaku.twidere.util.sync.mkdirIfNotExists
import org.mariotaku.uniqr.AndroidPlatform
import org.mariotaku.uniqr.UniqR
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException

/**
 * Display QR code to user
 * Created by mariotaku on 2017/4/3.
 */
class UserQrDialogFragment : BaseDialogFragment() {

    private val user: ParcelableUser
        get() = arguments!!.user!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Twidere_Dark_Dialog_NoFrame)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qrShare.setOnClickListener {
            shareQrImage()
        }
        qrSave.setOnClickListener {
            saveQrImage()
        }
        qrContainer.setOnClickListener {
            val artEnabled = !PreferencesSingleton.get(context!!)[qrArtEnabledKey]
            PreferencesSingleton.get(context!!)[qrArtEnabledKey] = artEnabled
            displayQrCode(artEnabled)
        }
        view.setOnClickListener {
            dismiss()
        }
        displayQrCode(PreferencesSingleton.get(context!!)[qrArtEnabledKey])
    }

    private fun displayQrCode(art: Boolean) {
        val weakThis by weak(this)
        promiseOnUi {
            val fragment = weakThis?.takeIf { it.view != null } ?: return@promiseOnUi
            fragment.qrView.visibility = View.INVISIBLE
            fragment.qrProgress.visibility = View.VISIBLE
        } and loadProfileImage().then { drawable ->
            val fragment = weakThis?.takeIf { it.context != null } ?: throw InterruptedException()
            val background = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(background)
            drawable.setBounds(0, 0, background.width, background.height)
            drawable.draw(canvas)

            val palette = Palette.from(background).generate()

            val link = LinkCreator.getUserWebLink(fragment.user)!!
            val segments = QrSegment.makeSegments(link.toString())
            val qrCode = QrCode.encodeSegments(segments, QrCode.Ecc.HIGH, 5, 40, -1, true)
            val uniqr = UniqR(AndroidPlatform(), if (art) background else null, QrCodeData(qrCode))
            uniqr.scale = 3
            uniqr.padding = 16
            uniqr.dotSize = 1
            uniqr.qrPatternColor = palette.patternColor
            val result = uniqr.build().produceResult()
            background.recycle()
            return@then result
        }.successUi { bitmap ->
            val fragment = weakThis?.takeIf { it.context != null && it.view != null } ?: return@successUi
            fragment.qrView.visibility = View.VISIBLE
            fragment.qrProgress.visibility = View.GONE
            fragment.qrView.setImageDrawable(BitmapDrawable(fragment.resources, bitmap).apply {
                this.setAntiAlias(false)
                this.isFilterBitmap = false
            })
        }.failUi {
            val fragment = weakThis?.takeIf { it.dialog != null } ?: return@failUi
            Toast.makeText(fragment.context, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
            fragment.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SHARE_MEDIA -> {
                ShareProvider.clearTempFiles(context!!)
            }
        }
    }

    private fun saveQrImage() {
        val weakThis by weak(this)
        val pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val saveDir = File(pubDir, "Twidere")
        val bitmap = (qrView.drawable as? BitmapDrawable)?.bitmap ?: return
        showProgressDialog("save_qr") and saveQrBitmap(bitmap, saveDir).successUi { (savedFile, _) ->
            val context = weakThis?.context ?: return@successUi
            MediaScannerConnection.scanFile(context, arrayOf(savedFile.absolutePath), arrayOf("image/png"), null)
            Toast.makeText(context, R.string.message_toast_saved_to_gallery, Toast.LENGTH_SHORT).show()
        }.alwaysUi {
            weakThis?.dismissProgressDialog("save_qr")
        }
    }

    private fun shareQrImage() {
        val weakThis by weak(this)
        val saveDir = ShareProvider.getFilesDir(context!!) ?: return
        val bitmap = (qrView.drawable as? BitmapDrawable)?.bitmap ?: return

        showProgressDialog("share_qr") and saveQrBitmap(bitmap, saveDir).successUi { (savedFile, mimeType) ->
            val activity = weakThis?.activity ?: return@successUi

            val fileUri = ShareProvider.getUriForFile(activity, TwidereConstants.AUTHORITY_TWIDERE_SHARE,
                    savedFile)

            val intent = Intent(Intent.ACTION_SEND)
            intent.setDataAndType(fileUri, mimeType)
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            }
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.action_share)),
                    REQUEST_SHARE_MEDIA)
        }.alwaysUi {
            weakThis?.dismissProgressDialog("share_qr")
        }
    }

    private fun saveQrBitmap(bitmap: Bitmap, saveDir: File) = task {
        if (saveDir.mkdirIfNotExists() == null) throw IOException()
        val saveFile = File(saveDir, "qr_${user.screen_name}_${System.currentTimeMillis()}.png")
        saveFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return@task SaveFileResult(saveFile, "image/png")
    }

    private fun loadProfileImage(): Promise<Drawable, Exception> {
        val activity = this.activity ?: return Promise.ofFail(InterruptedException())
        if (isDetached || dialog == null || activity.isFinishing) {
            return Promise.ofFail(InterruptedException())
        }
        val weakActivity by weak(activity)
        val user = this.user

        return task {
            val context = weakActivity ?: throw InterruptedException()
            val requestManager = Glide.with(context)
            val profileImageSize = context.getString(R.string.profile_image_size)
            try {
                return@task requestManager.loadOriginalProfileImage(user, 0)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
            } catch (e: ExecutionException) {
                // Ignore
            }
            // Return fallback profile image
            return@task requestManager.loadProfileImage(user, ImageShapeStyle.SHAPE_NONE, size = profileImageSize).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
        }
    }

    companion object {

        private val REQUEST_SHARE_MEDIA = 201

        private fun getOptimalPatternColor(color: Int): Int {
            val yiq = IntArray(3)
            TwidereColorUtils.colorToYIQ(color, yiq)
            if (yiq[0] > 96) {
                yiq[0] = 96
                return TwidereColorUtils.YIQToColor(Color.alpha(color), yiq)
            }
            return color
        }

        private val Palette.patternColor: Int
            get() {
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
