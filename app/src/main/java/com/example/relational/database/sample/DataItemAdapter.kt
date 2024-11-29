package com.example.relational.database.sample

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.relational.database.sample.activities.DataRecords
import com.example.relational.database.sample.activities.DisplayImages
import com.example.relational.database.sample.database.AppDatabase
import com.example.relational.database.sample.database.CategoryDao
import com.example.relational.database.sample.database.DataItem
import com.example.relational.database.sample.database.DataItemDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataItemAdapter :
    ListAdapter<DataItem, DataItemAdapter.DataItemViewHolder>(DataItemDiffCallback()) {

    private var nameItem = ""
    private lateinit var itemDao: DataItemDao
    private lateinit var categoryDao: CategoryDao


    private val categoryColorsMap = HashMap<Long, Drawable>()

    companion object {
        var byteArrayOne: ByteArray? = null
        var byteArrayTwo: ByteArray? = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.data_item_layout, parent, false)

        itemDao = AppDatabase.getDatabase(parent.context).dataItemDao()
        categoryDao = AppDatabase.getDatabase(parent.context).categoryDao()



        return DataItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DataItemViewHolder, position: Int) {
        val dataItem = getItem(position)
        holder.bind(dataItem)

        // Set the long click listener on the itemView
        holder.itemView.setOnLongClickListener {
            nameItem = dataItem.name.toString()
            showOptionsDialog(holder.itemView, position)
            true // Return true to consume the event

        }
    }

    inner class DataItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val bgLayoutConstraint: ConstraintLayout =
            itemView.findViewById(R.id.bgLayoutConstrntDI)


        fun bind(dataItem: DataItem) {
            // Bind the data item to the views in the layout
            // Example:
            val nameTextView = itemView.findViewById<TextView>(R.id.nameTViewDI)
//            val layoutOne: ImageView = itemView.findViewById(R.id.imageLinearOne)
//            val layoutTwo: ImageView = itemView.findViewById(R.id.imageLinearTwo)
//
//
//            var byteArrayOneInner: ByteArray? = dataItem.imageResource1
//            var byteArrayTwoInner: ByteArray? = dataItem.imageResource2
//
//
//            var firstImageBitmap: Bitmap?  = byteArrayOneInner?.let { convertByteArrayToBitmap(it) }
//            var secondImageBitmap: Bitmap? = byteArrayTwoInner?.let { convertByteArrayToBitmap(it) }
//


            nameTextView.text = dataItem.name
//            layoutOne.setImageBitmap(firstImageBitmap)
//            layoutTwo.setImageBitmap(secondImageBitmap)


            itemView.setOnClickListener {

                val intent = Intent(itemView.context, DisplayImages::class.java)
                intent.putExtra("itemId", dataItem.id)
                intent.putExtra("itemName", dataItem.name)
                intent.putExtra("itemText", dataItem.text)
//                    intent.putExtra("itemImage1", dataItem.imageResource1)
//                    intent.putExtra("itemImage2", dataItem.imageResource2)
                byteArrayOne = dataItem.imageResource1
                byteArrayTwo = dataItem.imageResource2
                itemView.context.startActivity(intent)

            }

            val randomGradientDrawable = getRandomGradientDrawable(dataItem.id)
            bgLayoutConstraint.background = randomGradientDrawable
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

    private fun showOptionsDialog(itemView: View, position: Int) {

        val inflater = LayoutInflater.from(itemView.context)
        val view = inflater.inflate(R.layout.long_press_dialog, null)

        // Find your custom views from the inflated layout
        val textViewOption1: TextView = view.findViewById(R.id.tv1)
        val textViewOption2: TextView = view.findViewById(R.id.tv2)
        val textViewOption3: TextView = view.findViewById(R.id.tv3)

        textViewOption1.text = nameItem

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

    private fun deleteCategoryDialog(context: Context, dataItem: DataItem) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.delete_category_dialog, null)
        val cancelTV = view.findViewById<TextView>(R.id.tvCancell)
        val deleteTV = view.findViewById<TextView>(R.id.tvActionn)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        nameTV.text = dataItem.name

        val alertDialog = AlertDialog.Builder(context, R.style.CustomDialogTwo)
            .setView(view)
            .create()

        cancelTV.setOnClickListener() {
            alertDialog.dismiss()

        }

        deleteTV.setOnClickListener() {
            if (dataItem.id != -1L) {
                GlobalScope.launch {
                    itemDao.deleteDataItemById(dataItem.id)
                    // Check if the category is still in the list
                    // Send a broadcast indicating that the category was renamed
                    val intent = Intent("dataItemDelete")
                    context.sendBroadcast(intent)


                    // decrement the counter
                    val category = categoryDao.getCategoryById(DataRecords.categoryIdParent)
                    category?.let {
                        // decrement the itemCounter for the category
                        it.itemCounter--
                        Log.d(ContentValues.TAG, "Item Counter: ${it.itemCounter}")

                        // Update the itemCounter in the database
                        categoryDao.updateItemCounter(DataRecords.categoryIdParent, it.itemCounter)

                        // refresh the views in Main activity
                        MainActivity.flagViewRefresh = true
                    }

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

    private fun renameCategoryDialog(context: Context, dataItem: DataItem) {
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
                    itemDao.renameDataItem(dataItem.id, newName)
                    // Send a broadcast indicating that the category was renamed
                    val intent = Intent("dataItemRename")
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


class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}
