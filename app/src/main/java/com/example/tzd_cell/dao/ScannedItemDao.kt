package com.example.tzd_cell.dao

import androidx.room.*
import com.example.tzd_cell.entity.ScannedItem

@Dao
interface ScannedItemDao {
    @Query("SELECT * FROM ScannedItems")
    suspend fun getAll(): List<ScannedItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScannedItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScannedItem>)

    @Query("DELETE FROM ScannedItems")
    suspend fun clear()

    @Query("SELECT * FROM ScannedItems WHERE recipient_id = :recipientId AND invoice_number = :invoiceNumber AND current_place_in_invoice = :currentPlace LIMIT 1")
    suspend fun getByUnique(recipientId: String, invoiceNumber: String, currentPlace: Int): ScannedItem?
} 