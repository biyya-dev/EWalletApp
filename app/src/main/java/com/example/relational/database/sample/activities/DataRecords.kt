package com.example.relational.database.sample.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.relational.database.sample.DataItemAdapter
import com.example.relational.database.sample.MainActivity
import com.example.relational.database.sample.R
import com.example.relational.database.sample.databinding.ActivityDataRecordsBinding
import com.example.relational.database.sample.database.AppDatabase
import com.example.relational.database.sample.database.CategoryDao
import com.example.relational.database.sample.database.DataItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataRecords : AppCompatActivity() {

    private lateinit var binding: ActivityDataRecordsBinding
    private var categoryId: Long = 0
    private var categoryName: String = ""
    //private lateinit var category:Category
    private lateinit var dataItemAdapter: DataItemAdapter
    private lateinit var dataItemDao: DataItemDao
    private lateinit var categoryDao: CategoryDao


    companion object {
        var categoryIdParent: Long = 0
    }

    private val itemRenamedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "dataItemRename") {
                loadAndDisplayDataItems()
            }

            if (intent?.action == "dataItemDelete") {
                loadAndDisplayDataItems()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Retrieve the category ID from the intent extra
        categoryId = intent.getLongExtra("categoryId", 0)
        categoryIdParent = categoryId
        categoryName = intent.getStringExtra("categoryName").toString()

        // set title bar title
        binding.tvTitle.text = categoryName



        // get instance of the database & DAO
        dataItemDao = AppDatabase.getDatabase(applicationContext).dataItemDao()
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()




        // Set up the RecyclerView and its adapter
        dataItemAdapter = DataItemAdapter()
        binding.dataItemRecyclerView.layoutManager = GridLayoutManager(this , 2)
        binding.dataItemRecyclerView.adapter = dataItemAdapter




        binding.floatingActionButton.setOnClickListener(){

            val intent = Intent(this, DataItemActivity::class.java)
            intent.putExtra("categoryId", categoryId)
            intent.putExtra("categoryName", categoryName)
            startActivity(intent)
        }


        // Load and display the relevant data items for the selected category
        loadAndDisplayDataItems()


        binding.backButton.setOnClickListener(){
            onBackPressed()
        }

        binding.editButton.setOnClickListener(){

            renameCategoryDialog(this)

        }



        // Register the broadcast receiver
        val filter = IntentFilter().apply {
            addAction("dataItemRename")
            addAction("dataItemDelete")
        }
        registerReceiver(itemRenamedReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        // Update the data items whenever the activity is resumed
        loadAndDisplayDataItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(itemRenamedReceiver)
    }


    private fun loadAndDisplayDataItems() {
        GlobalScope.launch {
            val dataItems = withContext(Dispatchers.IO) {
                dataItemDao.getDataItemsByCategoryId(categoryId)
            }
            runOnUiThread {
                dataItemAdapter.submitList(dataItems)
            }
        }
    }

    private fun renameCategoryDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.add_category_dialog, null)
        val categoryNameEditText = view.findViewById<EditText>(R.id.categoryNameEditText)
        val cancelTV = view.findViewById<TextView>(R.id.tvCancel)
        val renameTV = view.findViewById<TextView>(R.id.tvAction)

        val alertDialog = AlertDialog.Builder(context, R.style.CustomDialogTwo)
            .setView(view)
            .create()


        cancelTV.setOnClickListener() {
            alertDialog.dismiss()

        }

        renameTV.setOnClickListener() {
            val newName = categoryNameEditText.text.toString()
            if (newName.isNotEmpty()) {
                GlobalScope.launch {
                    categoryDao.renameCategory(categoryId, newName)
                    // Send a broadcast indicating that the category was renamed

                    binding.tvTitle.text = newName
                    MainActivity.flagViewRefresh = true
                }
                alertDialog.dismiss()

            } else {
                // Handle the case when the new name is empty
                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()

            }
        }


        alertDialog.show()
    }



}