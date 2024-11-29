package com.example.relational.database.sample.activities

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.relational.database.sample.BuildConfig
import com.example.relational.database.sample.MainActivity
import com.example.relational.database.sample.R
import com.example.relational.database.sample.activities.ImageCropActivity
import com.example.relational.database.sample.database.*
import com.example.relational.database.sample.databinding.ActivityDataItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class DataItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataItemBinding
    private var categoryId: Long = 0
    private lateinit var categoryDao: CategoryDao
    private var categoryName: String = ""
    private lateinit var dataItemDao: DataItemDao

    private var firstPhotoPath: String = ""
    private var secondPhotoPath: String = ""


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
        private const val REQUEST_STORAGE_PERMISSION = 123
        private var byteArrayCaptured1: ByteArray? = null
        private var byteArrayCaptured2: ByteArray? = null
        var imagePreviewBitmap: Bitmap? = null
        var firstImageBitmap: Bitmap? = null
        var secondImageBitmap: Bitmap? = null
        var flagFirst = false
        var flagSecond = false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataItemBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Retrieve the category ID from the intent extra
        categoryId = intent.getLongExtra("categoryId", 0)
        categoryName = intent.getStringExtra("categoryName").toString()

        // set title bar title
        binding.tvTitle.text = categoryName


        // get instance of the database & DAO
        dataItemDao = AppDatabase.getDatabase(applicationContext).dataItemDao()
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()


        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        // request camera permission and storage permission
        requestCameraPermission()


        // Set up the button click listener to save the data item

        binding.saveButton.setOnClickListener {

            val name = binding.nameEditText.text.toString()
            val text = binding.textEditText.text.toString()

            if (name.isNotEmpty() && text.isNotEmpty()) {
                val dataItem = DataItem(
                    categoryId = categoryId,
                    name = name,
                    text = text,
                    imageResource1 = byteArrayCaptured1,
                    imageResource2 = byteArrayCaptured2
                )
                insertDataItem(dataItem)


                // update the item counter
                GlobalScope.launch {
                    val category = categoryDao.getCategoryById(categoryId)
                    category?.let {
                        // Increment the itemCounter for the category
                        it.itemCounter++
                        Log.d(TAG, "Item Counter: ${it.itemCounter}")

                        // Update the itemCounter in the database
                        categoryDao.updateItemCounter(categoryId, it.itemCounter)
                    }
                }
            } else {
                Toast.makeText(this, "Please enter name and text", Toast.LENGTH_SHORT).show()
            }

            MainActivity.flagViewRefresh = true
        }


        binding.LayoutCameraOne.setOnClickListener() {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                flagFirst = true
                firstOpenCamera()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                showPermissionDeniedDialog(this)

            }
        }

        binding.LayoutCameraTwo.setOnClickListener() {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                flagSecond = true
                secondOpenCamera()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                showPermissionDeniedDialog(this)
            }
        }


        binding.backButton.setOnClickListener() {

            onBackPressed()

        }


    }

    private fun insertDataItem(dataItem: DataItem) {
        GlobalScope.launch {
            val dataItemId = withContext(Dispatchers.IO) {
                dataItemDao.insertDataItem(dataItem)
            }

            if (dataItemId > 0) {
                runOnUiThread {
                    //Toast.makeText(this@DataItemActivity, "Data item inserted successfully", Toast.LENGTH_SHORT).show()
                    binding.nameEditText.text = null
                    binding.textEditText.text = null
                    onBackPressed()

                }
            }
        }
    }


    private fun firstOpenCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.let {
                val photoUri: Uri = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            firstPhotoPath = absolutePath
        }
    }

    private fun secondOpenCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createSecondImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.let {
                val photoUri: Uri = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }


    @Throws(IOException::class)
    private fun createSecondImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            secondPhotoPath = absolutePath
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            if (flagFirst) {
                // Load the captured image from file path into the capturedImageBitmap variable
                firstImageBitmap = BitmapFactory.decodeFile(firstPhotoPath)
                imagePreviewBitmap = BitmapFactory.decodeFile(firstPhotoPath)

                // Convert the firstImageBitmap to a byte array
                byteArrayCaptured1 = firstImageBitmap?.let { convertBitmapToByteArray(it) }


                Log.d(TAG, "byteArray1: $byteArrayCaptured1")
                // Do something with the captured image (e.g., display it in an ImageView)

                // Send the captured image to another activity
                val intent = Intent(this, ImageCropActivity::class.java)
                startActivity(intent)

                binding.innerContainerOne.visibility = View.GONE
                binding.ivOne.setImageBitmap(firstImageBitmap)
            }

            if (flagSecond) {
                // Load the captured image from file path into the capturedImageBitmap variable
                secondImageBitmap = BitmapFactory.decodeFile(secondPhotoPath)
                imagePreviewBitmap = BitmapFactory.decodeFile(secondPhotoPath)




                Log.d(TAG, "byteArray2: $byteArrayCaptured2")
                // Do something with the captured image (e.g., display it in an ImageView)

                // Send the captured image to another activity
                val intent = Intent(this, ImageCropActivity::class.java)
                startActivity(intent)


                binding.innerContainerTwo.visibility = View.GONE
                binding.ivTwo.setImageBitmap(secondImageBitmap)
            }

        }
    }


    // convert Bitmap to byte array
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)
        return stream.toByteArray()
    }


    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) { ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_STORAGE_PERMISSION)

        } else {
            // Camera permission already granted

        }
    }

    fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                //openCamera()
            } else {
                // Camera permission denied
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showPermissionDeniedDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.dialog_permission_denied, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.show()

        val settingsButton: Button = dialogView.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            navigateToAppSettings(context)
            dialog.dismiss()
        }
    }

    private fun navigateToAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        binding.ivOne.setImageBitmap(firstImageBitmap)

        binding.ivTwo.setImageBitmap(secondImageBitmap)

        if (firstImageBitmap != null) {
            // Convert the firstImageBitmap to a byte array
            byteArrayCaptured1 = firstImageBitmap?.let { convertBitmapToByteArray(it) }
        }

        if (secondImageBitmap != null) {
            // Convert the firstImageBitmap to a byte array
            byteArrayCaptured2 = secondImageBitmap?.let { convertBitmapToByteArray(it) }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        firstImageBitmap = null
        secondImageBitmap = null
        imagePreviewBitmap = null
        flagFirst = false
        flagSecond = false

    }

}
