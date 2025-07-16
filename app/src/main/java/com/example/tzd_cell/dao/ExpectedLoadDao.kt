package com.example.tzd_cell.dao

import androidx.room.*
import com.example.tzd_cell.entity.ExpectedLoad

@Dao
interface ExpectedLoadDao {
    @Query("SELECT * FROM ExpectedLoad")
    suspend fun getAll(): List<ExpectedLoad>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExpectedLoad>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExpectedLoad)

    @Query("DELETE FROM ExpectedLoad")
    suspend fun clear()
} 