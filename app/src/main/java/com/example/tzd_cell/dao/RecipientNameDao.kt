package com.example.tzd_cell.dao

import androidx.room.*
import com.example.tzd_cell.entity.RecipientName

@Dao
interface RecipientNameDao {
    @Query("SELECT * FROM RecipientNames")
    suspend fun getAll(): List<RecipientName>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecipientName)

    @Query("SELECT * FROM RecipientNames WHERE recipient_id = :recipientId LIMIT 1")
    suspend fun getById(recipientId: String): RecipientName?

    @Update
    suspend fun update(item: RecipientName)
} 