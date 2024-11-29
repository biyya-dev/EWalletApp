package com.example.relational.database.sample.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.relational.database.sample.DataItemAdapter
import com.example.relational.database.sample.MainActivity
import com.example.relational.database.sample.R
import com.example.relational.database.sample.database.AppDatabase
import com.example.relational.database.sample.database.CategoryDao
import com.example.relational.database.sample.database.DataItemDao
import com.example.relational.database.sample.databinding.ActivityDisplayImagesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class DisplayImages : AppCompatActivity() {

    private lateinit var binding:ActivityDisplayImagesBinding
    private var itemId: Long = 0
    private var itemName: String = ""
    private var itemText: String = ""
    private lateinit var dataItemDao: DataItemDao
    private lateinit var categoryDao: CategoryDao
    private var firstImageBitmap: Bitmap?  = null
    private var secondImageBitmap: Bitmap?  = null

    companion object{

        var imageBitmap: Bitmap?  = null


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        // get instance of the database & DAO
        dataItemDao = AppDatabase.getDatabase(applicationContext).dataItemDao()
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()


        // Retrieve the category ID from the intent extra
        itemId = intent.getLongExtra("itemId", 0)
        itemName = intent.getStringExtra("itemName").toString()
        itemText = intent.getStringExtra("itemText").toString()


        // Update UI with images once the database queries are complete
        lifecycleScope.launch {

            firstImageBitmap = DataItemAdapter.byteArrayOne?.let { convertByteArrayToBitmap(it) }
            secondImageBitmap = DataItemAdapter.byteArrayTwo?.let { convertByteArrayToBitmap(it) }

            binding.nameEditText.text = itemName
            binding.textEditText.text = itemText
            binding.tvTitle.text = itemName
            binding.innerContainerOne.visibility = View.GONE
            binding.innerContainerTwo.visibility = View.GONE
            binding.ivOne.setImageBitmap(firstImageBitmap)
            binding.ivTwo.setImageBitmap(secondImageBitmap)
        }



        binding.ivOne.setOnClickListener(){

            imageBitmap = firstImageBitmap
            val intent = Intent(this@DisplayImages, ShowImage::class.java)
            startActivity(intent)

        }

        binding.ivTwo.setOnClickListener(){

            imageBitmap = secondImageBitmap
            val intent = Intent(this@DisplayImages, ShowImage::class.java)
            startActivity(intent)

        }

        binding.backButton.setOnClickListener(){
           onBackPressed()
        }


        binding.btnDelete.setOnClickListener(){

            deleteItemDialog(this)


        }

        binding.btnShare.setOnClickListener(){

            Toast.makeText(this,"Wait, getting data ready for sharing", Toast.LENGTH_SHORT).show()

            GlobalScope.launch(Dispatchers.IO) {
                shareMyData()

            }

        }

    }

    private fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            // If an exception occurs during decoding, return null or handle the error as needed
            null
        }
    }

    private fun deleteItemDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.delete_category_dialog, null)
        val cancelTV = view.findViewById<TextView>(R.id.tvCancell)
        val deleteTV = view.findViewById<TextView>(R.id.tvActionn)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        nameTV.text = itemName

        val alertDialog = AlertDialog.Builder(context, R.style.CustomDialogTwo)
            .setView(view)
            .create()

        cancelTV.setOnClickListener() {
            alertDialog.dismiss()

        }

        deleteTV.setOnClickListener() {
            if (itemId != -1L) {
                lifecycleScope.launch {
                    dataItemDao.deleteDataItemById(itemId)

                    // decrement the counter
                    val category = categoryDao.getCategoryById(DataRecords.categoryIdParent)
                    category?.let {
                        // Decrement the itemCounter for the category
                        it.itemCounter--
                        Log.d(ContentValues.TAG, "Item Counter: ${it.itemCounter}")

                        // Update the itemCounter in the database
                        categoryDao.updateItemCounter(DataRecords.categoryIdParent, it.itemCounter)

                        // refresh the views in Main activity
                        MainActivity.flagViewRefresh = true

                        onBackPressed()

                    }
                }
                alertDialog.dismiss()

            } else {
                // Handle the case when the new name is empty
                Toast.makeText(context, "Failed to Delete item", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()

            }
        }

        alertDialog.show()


    }

    private fun shareMyData() {
        // Convert Bitmaps to Uris
        val imageUris = ArrayList<Uri>()
        firstImageBitmap?.let { bitmapToUri(it)?.let { uri -> imageUris.add(uri) } }
        secondImageBitmap?.let { bitmapToUri(it)?.let { uri -> imageUris.add(uri) } }

        // Create the share intent
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = "image/*"

        // Add the Name and Description to the intent
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, itemName)
        shareIntent.putExtra(Intent.EXTRA_TEXT, itemText)


        // Add the Images to the intent
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)

        // Start the share intent with the chooser
        val chooserIntent = Intent.createChooser(shareIntent, "Share Data")
        startActivity(chooserIntent)
    }

    private fun bitmapToUri(bitmap: Bitmap): Uri? {
        return try {
            // Convert Bitmap to Uri
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                contentResolver, bitmap, "imageTitle", null
            )
            path?.let { Uri.parse(it) } // Check if the path is not null before parsing the URI
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




    override fun onDestroy() {
        super.onDestroy()

        imageBitmap = null

    }

}