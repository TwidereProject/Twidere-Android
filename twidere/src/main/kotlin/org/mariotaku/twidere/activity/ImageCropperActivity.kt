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

package org.mariotaku.twidere.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageOptions
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_crop_image.*
import org.mariotaku.twidere.R

/**
 * Built-in activity for image cropping.
 * Use [CropImage.activity] to create a builder to start this activity.
 */
class ImageCropperActivity : BaseActivity(), CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {

    /**
     * Persist URI image to crop URI if specific permissions are required
     */
    private val cropImageUri: Uri? get() = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_SOURCE)

    /**
     * the options that were set for the crop image
     */
    private val options: CropImageOptions? get() = intent.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_OPTIONS)

    /**
     * Get Android uri to save the cropped image into.<br></br>
     * Use the given in options or create a temp file.
     */
    private val outputUri: Uri? get() = options?.outputUri

    @SuppressLint("NewApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            // no permissions required or already grunted, can start crop image activity
            cropImageView.setImageUriAsync(cropImageUri)
        }

    }

    override fun onStart() {
        super.onStart()
        cropImageView.setOnSetImageUriCompleteListener(this)
        cropImageView.setOnCropImageCompleteListener(this)
    }

    override fun onStop() {
        super.onStop()
        cropImageView.setOnSetImageUriCompleteListener(null)
        cropImageView.setOnCropImageCompleteListener(null)
    }

    override fun onBackPressed() {
        setResultCancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResultCancel()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_crop_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.perform_crop -> {
                cropImage()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            options?.let { options ->
                if (options.initialCropWindowRectangle != null) {
                    cropImageView.cropRect = options.initialCropWindowRectangle
                }
                if (options.initialRotation > -1) {
                    cropImageView.rotatedDegrees = options.initialRotation
                }
            }
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        setResult(result.uri, result.error, result.sampleSize)
    }

    //region: Private methods

    /**
     * Execute crop image and save the result to output uri.
     */
    private fun cropImage() {
        options?.let { options ->
            if (options.noOutputImage) {
                setResult(null, null, 1)
            } else {
                val outputUri = outputUri
                cropImageView.saveCroppedImageAsync(outputUri,
                        options.outputCompressFormat,
                        options.outputCompressQuality,
                        options.outputRequestWidth,
                        options.outputRequestHeight,
                        options.outputRequestSizeOptions)
            }
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    protected fun rotateImage(degrees: Int) {
        cropImageView.rotateImage(degrees)
    }

    /**
     * Result with cropped image data or error if failed.
     */
    private fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        val resultCode = if (error == null) Activity.RESULT_OK else CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
        setResult(resultCode, getResultIntent(uri, error, sampleSize))
        finish()
    }

    /**
     * Cancel of cropping activity.
     */
    private fun setResultCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    private fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(cropImageView.imageUri, uri, error,
                cropImageView.cropPoints, cropImageView.cropRect, cropImageView.rotatedDegrees,
                cropImageView.wholeImageRect, sampleSize)
        val intent = Intent()
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

    //endregion
}

