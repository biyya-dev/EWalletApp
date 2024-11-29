package com.example.relational.database.sample

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.relational.database.sample.activities.Settings
import com.example.relational.database.sample.adapters.CategoryAdapter
import com.example.relational.database.sample.adapters.MyIconSpinnerAdapter
import com.example.relational.database.sample.database.AppDatabase
import com.example.relational.database.sample.database.Category
import com.example.relational.database.sample.database.CategoryDao
import com.example.relational.database.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryDao: CategoryDao
    private var flagInsertCategories = true


    companion object {

        var flagViewRefresh = false


    }

    // receive the updates from category adapter to reload the categories when queries are performed
    private val categoryRenamedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "dataRename") {
                loadCategories()
            }

            if (intent?.action == "dataDelete") {
                loadCategories()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        //get instance of the databases
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()


        // Set up RecyclerView and adapter
        categoryAdapter = CategoryAdapter()
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.categoryRecyclerView.adapter = categoryAdapter

        // Load categories from the database or add initial categories if the database is empty
        loadCategories()

        // Set up the "Add Category" button click listener
        binding.addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }


        binding.icSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }


        // Register the broadcast receiver
        val filter = IntentFilter().apply {
            addAction("dataRename")
            addAction("dataDelete")
        }
        registerReceiver(categoryRenamedReceiver, filter)

    }


    override fun onResume() {
        super.onResume()
        // Reload categories whenever the activity is resumed
        //TODO:: possible data duplication due to this, a check added to prevent this when writing pre loaded categories
        if (!flagInsertCategories && flagViewRefresh) {
            loadCategories()
            flagViewRefresh = false
        }
    }

    override fun onPause() {
        super.onPause()
        flagInsertCategories = false
        //Log.d(TAG, "onPause: flag is : $flagInsertCategories")

    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver
        unregisterReceiver(categoryRenamedReceiver)
    }


    private fun loadCategories() {
        GlobalScope.launch {
            val categories = withContext(Dispatchers.IO) {
                categoryDao.getAllCategories()
            }
            if (categories.isEmpty()) {
                // If no categories exist, add initial categories
                addInitialCategories()

            } else {
                runOnUiThread {
                    categoryAdapter.submitList(null) // Clear the adapter's list
                    categoryAdapter.submitList(categories)
                }
            }
        }
    }


    private fun addInitialCategories() {
        val initialCategories = listOf(
            Category(name = "Office ID Card", icon = R.drawable.ic_office_id_card),
            Category(name = "ID Card", icon = R.drawable.ic_id_card),
            Category(name = "Credit Card", icon = R.drawable.ic_credit_card),
            Category(name = "Driving License", icon = R.drawable.ic_driving_licese),
            Category(name = "Insurance Card", icon = R.drawable.ic_insurance_card),
            Category(name = "Business Card", icon = R.drawable.ic_business_card),
            Category(name = "Voting Card", icon = R.drawable.ic_voting_card),
            Category(name = "Passport", icon = R.drawable.ic_passport),
            Category(name = "Shopping Cards", icon = R.drawable.ic_shopping_card),
            Category(name = "Debit Card", icon = R.drawable.ic_debit_card)
            // Add more initial categories as needed
        )

        //TODO:: currently checking for the names rather than IDs before writing data into db, will cause issues when append curd operations wil be performed, modify the dao query to return ids rather than names
        GlobalScope.launch {
            for (category in initialCategories) {
                val existingCategory = categoryDao.getCategoryByName(category.name)
                if (existingCategory == null) {
                    categoryDao.insertCategory(category)
                }
            }
            // Reload categories after inserting initial categories
            loadCategories()
        }
    }


    private fun showAddCategoryDialog() {
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.setContentView(R.layout.bottom_sheet)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set window width to MATCH_PARENT
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        val layoutParams = dialog.window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        dialog.window?.attributes = layoutParams

        // Find views and set click listeners here
        val buttonOption1: TextView = dialog.findViewById(R.id.buttonOption1)
//        val buttonOption2: TextView = dialog.findViewById(R.id.buttonOption2)
//        val buttonOption3: TextView = dialog.findViewById(R.id.buttonOption3)

        buttonOption1.setOnClickListener {
            addCategoryDialog()
            dialog.dismiss()

        }


        dialog.show()

    }

    private fun addCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val categoryNameEditText = dialogView.findViewById<EditText>(R.id.nameNewCategory)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancl)
        val tvAdd = dialogView.findViewById<TextView>(R.id.tvAdd)
        val iconSpinner = dialogView.findViewById<Spinner>(R.id.iconSpinner)


        val iconList = mutableListOf<Int>()
        iconList.add(R.drawable.ic_office_id_card)
        iconList.add(R.drawable.ic_id_card)
        iconList.add(R.drawable.ic_credit_card)
        iconList.add(R.drawable.ic_driving_licese)
        iconList.add(R.drawable.ic_insurance_card)
        iconList.add(R.drawable.ic_business_card)
        iconList.add(R.drawable.ic_voting_card)
        iconList.add(R.drawable.ic_passport)
        iconList.add(R.drawable.ic_shopping_card)
        iconList.add(R.drawable.ic_debit_card)


        val iconAdapter = MyIconSpinnerAdapter(this, iconList)
        iconSpinner.adapter = iconAdapter


        val alertDialog = AlertDialog.Builder(this, R.style.CustomDialogTwo)
            .setView(dialogView)
            .create()

        var mySelectedIcon: Int? = null

        iconSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedIcon = iconList[position]

                mySelectedIcon = selectedIcon

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mySelectedIcon = R.drawable.ic_office_id_card
            }
        }

        tvAdd.setOnClickListener() {
            val categoryName = categoryNameEditText.text.toString()
            if (categoryName.isNotEmpty()) {
                val category = mySelectedIcon?.let { it1 -> Category(name = categoryName, icon = it1) }
                //TODO: this could be performed on a background thread
                if (category != null) {
                    insertCategory(category)
                }
            }
            else {
                Toast.makeText(this, "Name cannot be empty" , Toast.LENGTH_SHORT).show()
            }
            alertDialog.dismiss()
        }

        tvCancel.setOnClickListener() {
            alertDialog.dismiss()
        }


        alertDialog.show()
    }


    private fun insertCategory(category: Category) {
        GlobalScope.launch {
            val categoryId = withContext(Dispatchers.IO) {
                categoryDao.insertCategory(category)
            }
            if (categoryId > 0) {
                loadCategories()
            }
        }
    }


//    class MyBottomSheetFragment : BottomSheetDialogFragment() {
//        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//            val bottomSheet =
//                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.setBackgroundResource(android.R.color.transparent)
//            return bottomSheetDialog
//        }
//
//        override fun onCreateView(
//            inflater: LayoutInflater, container: ViewGroup?,
//            savedInstanceState: Bundle?
//        ): View? {
//            val view = inflater.inflate(R.layout.bottom_sheet, container, false)
//
//            val buttonOption1: TextView = view.findViewById(R.id.buttonOption1)
//            val buttonOption2: TextView = view.findViewById(R.id.buttonOption2)
//            val buttonOption3: TextView = view.findViewById(R.id.buttonOption3)
//
//            // Set click listeners for the buttons
//            buttonOption1.setOnClickListener {
//                // Handle Option 1 click
//                dismiss()
//            }
//
//            buttonOption2.setOnClickListener {
//                // Handle Option 2 click
//                dismiss()
//            }
//
//            buttonOption3.setOnClickListener {
//                // Handle Option 3 click
//                dismiss()
//            }
//
//            return view
//        }
//    }


}


//class MainActivity : AppCompatActivity() {
//    private late init var categoryAdapter: CategoryAdapter
//    private late init var categoryDao: CategoryDao
//    private late init var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding= ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//
//        // Change status bar content color to black since status bar color is set to white
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
//
//
//
//
//        // Initialize database and DAO
//        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my-database")
//            .build()
//        categoryDao = database.categoryDao()
//
//        // Set up RecyclerView and adapter
//        categoryAdapter = CategoryAdapter()
//        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this,2)
//        binding.categoryRecyclerView.adapter = categoryAdapter
//
//        // Load categories from the database
//        loadCategories()
//
//        // Set up the "Add Category" button click listener
//        binding.addCategoryButton.setOnClickListener { showAddCategoryDialog() }
//
//
//
//    }
//
//    private fun loadCategories() {
//        GlobalScope.launch {
//            val categories = withContext(Dispatchers.IO) {
//                categoryDao.getAllCategories()
//            }
//            categoryAdapter.submitList(categories)
//        }
//    }
//
//    private fun showAddCategoryDialog() {
//        val dialog = AlertDialog.Builder(this)
//        val dialogView = layoutInflater.inflate(R.layout.add_category_dialog, null)
//        val categoryNameEditText = dialogView.findViewById<EditText>(R.id.categoryNameEditText)
//
//        dialog.setView(dialogView)
//        dialog.setPositiveButton("Add") { _, _ ->
//            val categoryName = categoryNameEditText.text.toString()
//            if (categoryName.isNotEmpty()) {
//                val category = Category(name = categoryName, icon = "")
//                insertCategory(category)
//            }
//        }
//        dialog.setNegativeButton("Cancel", null)
//
//        val alertDialog = dialog.create()
//        alertDialog.show()
//    }
//
//    private fun insertCategory(category: Category) {
//        GlobalScope.launch {
//            val categoryId = withContext(Dispatchers.IO) {
//                categoryDao.insertCategory(category)
//            }
//            if (categoryId > 0) {
//                loadCategories()
//            }
//        }
//    }
//}

//private fun addInitialCategories() {
//    val initialCategories = listOf(
//        Category(name = "Office ID Card", icon = R.drawable.ic_office_id_card),
//        Category(name = "ID Card", icon = R.drawable.ic_id_card),
//        Category(name = "Credit Card", icon = R.drawable.ic_credit_card),
//        Category(name = "Driving License", icon = R.drawable.ic_driving_license),
//        Category(name = "Insurance Card", icon = R.drawable.ic_insurance_card),
//        Category(name = "Business Card", icon = R.drawable.ic_business_card),
//        Category(name = "Voting Card", icon = R.drawable.ic_voting_card),
//        Category(name = "Passport", icon = R.drawable.ic_passport),
//        Category(name = "Shopping Cards", icon = R.drawable.ic_shopping_card),
//        Category(name = "Debit Card", icon = R.drawable.ic_debit_card)
//        // Add more initial categories as needed
//    )
//
//    GlobalScope.launch {
//        for (category in initialCategories) {
//            categoryDao.insertCategory(category)
//        }
//        // Reload categories after inserting initial categories
//        loadCategories()
//    }
//}