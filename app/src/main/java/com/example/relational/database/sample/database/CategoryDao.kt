package com.example.relational.database.sample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// CategoryDao.kt
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Insert
    suspend fun insertCategory(category: Category): Long

    //CRUD operations or queries

    @Query("SELECT * FROM categories WHERE name = :name")
    fun getCategoryByName(name: String): Category?

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    // Query to update the itemCounter variable
    @Query("UPDATE categories SET itemCounter = :newCounter WHERE id = :categoryId")
    suspend fun updateItemCounter(categoryId: Long, newCounter: Int)

    // Query to rename a category
    @Query("UPDATE categories SET name = :newName WHERE id = :categoryId")
    suspend fun renameCategory(categoryId: Long, newName: String)

    // Query to delete a category by ID
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

}

// DataItemDao.kt
@Dao
interface DataItemDao {
    @Query("SELECT * FROM data_items WHERE categoryId = :categoryId")
    suspend fun getDataItemsByCategoryId(categoryId: Long): List<DataItem>

    @Insert
    suspend fun insertDataItem(dataItem: DataItem): Long

    @Query("UPDATE data_items SET imageResource1 = :imageResource1, imageResource2 = :imageResource2 WHERE id = :dataItemId")
    suspend fun updateImageResources(dataItemId: Long, imageResource1: Int, imageResource2: Int)


    //CRUD operations or queries

    @Query("SELECT imageResource1 FROM data_items WHERE categoryId = :categoryId")
    fun getImageOneForCategory(categoryId: Long): ByteArray?

    @Query("SELECT imageResource2 FROM data_items WHERE categoryId = :categoryId")
    fun getImageTwoForCategory(categoryId: Long): ByteArray?


    // Query to rename a data item
    @Query("UPDATE data_items SET name = :newName WHERE id = :dataItemId")
    suspend fun renameDataItem(dataItemId: Long, newName: String)

    // Query to delete a data item by ID
    @Query("DELETE FROM data_items WHERE id = :dataItemId")
    suspend fun deleteDataItemById(dataItemId: Long)


}
