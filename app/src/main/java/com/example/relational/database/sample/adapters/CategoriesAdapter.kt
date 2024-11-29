package com.example.relational.database.sample.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.relational.database.sample.R
import com.example.relational.database.sample.activities.DataItemActivity
import com.example.relational.database.sample.activities.DataRecords
import com.example.relational.database.sample.database.AppDatabase
import com.example.relational.database.sample.database.Category
import com.example.relational.database.sample.database.CategoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryAdapter :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var nameCategory = ""
    private val categoryColorsMap = HashMap<Long, Drawable>()
    private lateinit var categoryDao: CategoryDao


    companion object {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.categories_layout, parent, false)

        //get instance of the databases
        categoryDao = AppDatabase.getDatabase(parent.context).categoryDao()


        return CategoryViewHolder(itemView)


    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)

        // Set the long click listener on the itemView
        holder.itemView.setOnLongClickListener {
            nameCategory = category.name.toString()
            showOptionsDialog(holder.itemView, position)
            true // Return true to consume the event

        }
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val tvItemCounter: TextView = itemView.findViewById(R.id.tvCounter)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
        private val bgLayoutConstraint: ConstraintLayout =
            itemView.findViewById(R.id.bgLayoutConstrnt)


        fun bind(category: Category) {

            // Access the itemCounter for the category
            val itemCounter = category.itemCounter
            tvItemCounter.text = itemCounter.toString()

            categoryNameTextView.text = category.name
            // Set the icon resource
            val resourceId = itemView.context.resources.getIdentifier(
                category.icon.toString(),
                "drawable",
                itemView.context.packageName
            )
            if (resourceId != 0) {
                iconImageView.setImageResource(resourceId)
            }

            val randomGradientDrawable = getRandomGradientDrawable(category.id)
            bgLayoutConstraint.background = randomGradientDrawable


            // Access the RecyclerView adapter from the itemView
//            val recyclerView = itemView.parent as? RecyclerView
//            val totalItemCount = recyclerView?.adapter?.itemCount ?: 0
//
//            // Loop until the last item is reached
//            for (position in 0 until totalItemCount) {
//
//
//            }


            // Check if data exists for the clicked category
            CoroutineScope(Dispatchers.Main).launch {
                val dataExists = checkDataExistsForCategory(itemView.context, category)

                itemView.setOnClickListener {
                    if (dataExists) {
                        val intent = Intent(itemView.context, DataRecords::class.java)
                        intent.putExtra("categoryId", category.id)
                        intent.putExtra("categoryName", category.name)
                        itemView.context.startActivity(intent)
                    }
                    if (!dataExists) {
                        val intent = Intent(itemView.context, DataItemActivity::class.java)
                        intent.putExtra("categoryId", category.id)
                        intent.putExtra("categoryName", category.name)
                        itemView.context.startActivity(intent)
                    }

                }


            }


        }
    }


    private fun getRandomGradientDrawable(categoryId: Long): Drawable {
        if (categoryColorsMap.containsKey(categoryId)) {
            return categoryColorsMap[categoryId]!!
        }

        val gradientColors = arrayOf(
            intArrayOf(Color.parseColor("#F15584"), Color.parseColor("#FD8EC6")), // Gradient 1
            intArrayOf(Color.parseColor("#8F4AC7"), Color.parseColor("#F065B6")), // Gradient 2
            intArrayOf(Color.parseColor("#33CAE4"), Color.parseColor("#22E4D0")), // Gradient 3
            intArrayOf(Color.parseColor("#08CA3E"), Color.parseColor("#4CDC74")), // Gradient 4
            intArrayOf(Color.parseColor("#F97624"), Color.parseColor("#F8A95A")), // Gradient 5
            intArrayOf(Color.parseColor("#9935CB"), Color.parseColor("#996ED9")), // Gradient 6
            intArrayOf(Color.parseColor("#CD1C39"), Color.parseColor("#F23943")), // Gradient 7
            intArrayOf(Color.parseColor("#393ABD"), Color.parseColor("#3874E2")), // Gradient 8
            intArrayOf(Color.parseColor("#E1393B"), Color.parseColor("#FE7849")), // Gradient 9
            intArrayOf(Color.parseColor("#E78B36"), Color.parseColor("#E3C109")) // Gradient 10
            // Add more gradient color combinations as needed
        )

        val randomIndex = (0 until gradientColors.size).random()
        val colors = gradientColors[randomIndex]

        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors)

        val cornerRadiusTopLeft = 5.dpToPx()
        val cornerRadiusTopRight = 5.dpToPx()
        val cornerRadiusBottomLeft = 20.dpToPx()
        val cornerRadiusBottomRight = 20.dpToPx()

        gradientDrawable.cornerRadii = floatArrayOf(
            cornerRadiusTopLeft,
            cornerRadiusTopLeft,
            cornerRadiusTopRight,
            cornerRadiusTopRight,
            cornerRadiusBottomRight,
            cornerRadiusBottomRight,
            cornerRadiusBottomLeft,
            cornerRadiusBottomLeft
        )

        categoryColorsMap[categoryId] = gradientDrawable

        return gradientDrawable
    }

    private fun Int.dpToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }


    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

    private suspend fun checkDataExistsForCategory(context: Context, category: Category): Boolean {

        // Initialize database and DAO
        val database =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "my-database")
                .build()
        val dataItemDao = database.dataItemDao()

        // Check if any data item exists for the given category ID
        val dataItems = dataItemDao.getDataItemsByCategoryId(category.id)
        return dataItems.isNotEmpty()

        Log.d(TAG, "checkDataExistsForCategory: $dataItems ")
    }


    private fun showOptionsDialog(itemView: View, position: Int) {

        val inflater = LayoutInflater.from(itemView.context)
        val view = inflater.inflate(R.layout.long_press_dialog, null)

        // Find your custom views from the inflated layout
        val textViewOption1: TextView = view.findViewById(R.id.tv1)
        val textViewOption2: TextView = view.findViewById(R.id.tv2)
        val textViewOption3: TextView = view.findViewById(R.id.tv3)

        textViewOption1.text = nameCategory

        val alertDialog = AlertDialog.Builder(itemView.context, R.style.CustomDialogTwo)
            .setView(view)
            .create()

        textViewOption2.setOnClickListener {
            // Rename
            renameCategoryDialog(itemView.context, getItem(position))
            alertDialog.dismiss()

        }

        textViewOption3.setOnClickListener {
            // Delete

            deleteCategoryDialog(itemView.context, getItem(position))
            alertDialog.dismiss()
        }

        alertDialog.show()

    }

    private fun deleteCategoryDialog(context: Context, category: Category) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.delete_category_dialog, null)
        val cancelTV = view.findViewById<TextView>(R.id.tvCancell)
        val deleteTV = view.findViewById<TextView>(R.id.tvActionn)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        nameTV.text = category.name

        val alertDialog = AlertDialog.Builder(context, R.style.CustomDialogTwo)
            .setView(view)
            .create()

        cancelTV.setOnClickListener() {
            alertDialog.dismiss()

        }

        deleteTV.setOnClickListener() {
            if (category.id != -1L) {
                GlobalScope.launch {
                    categoryDao.deleteCategoryById(category.id)
                    // Check if the category is still in the list
                    // Send a broadcast indicating that the category was renamed
                    val intent = Intent("dataDelete")
                    context.sendBroadcast(intent)

                }
                alertDialog.dismiss()

            } else {
                // Handle the case when the new name is empty
                Toast.makeText(context, "Failed to delete Category", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()

            }
        }

        alertDialog.show()


    }

    private fun renameCategoryDialog(context: Context, category: Category) {
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
                    categoryDao.renameCategory(category.id, newName)
                    // Send a broadcast indicating that the category was renamed
                    val intent = Intent("dataRename")
                    context.sendBroadcast(intent)

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


//private fun getRandomGradientDrawable(): Drawable {
//    val gradientColors = arrayOf(
//        intArrayOf(Color.parseColor("#3F51B5"), Color.parseColor("#FF4081")), // Gradient 1
//        intArrayOf(Color.parseColor("#FF4081"), Color.parseColor("#3F51B5")), // Gradient 2
//        intArrayOf(Color.parseColor("#FF9800"), Color.parseColor("#FFEB3B")), // Gradient 3
//        // Add more gradient color combinations as needed
//    )
//
//    val randomIndex = (0 until gradientColors.size).random()
//    val colors = gradientColors[randomIndex]
//    return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
//}
