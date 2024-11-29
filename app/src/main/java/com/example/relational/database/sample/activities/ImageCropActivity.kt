package com.example.relational.database.sample.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.relational.database.sample.R
import com.example.relational.database.sample.databinding.ActivityImageCropBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class ImageCropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCropBinding
    private var rotationAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Create temporary URI for the image
        val sourceUri = DataItemActivity.imagePreviewBitmap?.let { createTempImageUri(it) }
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))

        // Start UCrop Activity
        if (sourceUri != null) {
            startCropActivity(sourceUri, destinationUri)
        } else {
            finish() // Exit if image is not available
        }

        // Cancel button click listener
        binding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Back button click listener
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun startCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // Optional: Adjust aspect ratio
            .withMaxResultSize(1080, 1080) // Optional: Max size for cropped image
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            // Handle cropped image
            val resultUri = UCrop.getOutput(data!!)
            val croppedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
            handleCroppedImage(croppedBitmap)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.printStackTrace()
            finish()
        }
    }

    private fun handleCroppedImage(croppedBitmap: Bitmap) {
        if (DataItemActivity.flagFirst) {
            DataItemActivity.firstImageBitmap = croppedBitmap
            DataItemActivity.flagFirst = false
        } else if (DataItemActivity.flagSecond) {
            DataItemActivity.secondImageBitmap = croppedBitmap
            DataItemActivity.flagSecond = false
        }
        finish()
    }

    private fun createTempImageUri(bitmap: Bitmap): Uri {
        val tempFile = File(cacheDir, "tempImage.jpg")
        tempFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            tempFile
        )
    }
}
