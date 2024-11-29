package com.example.relational.database.sample.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Category.kt
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: Int,
    var itemCounter:Int = 0
)

// DataItem.kt
@Entity(
    tableName = "data_items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DataItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val text: String,
    val imageResource1: ByteArray?,
    val imageResource2: ByteArray?
)


