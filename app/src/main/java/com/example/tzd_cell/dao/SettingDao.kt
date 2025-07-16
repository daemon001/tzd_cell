package com.example.tzd_cell.dao

import androidx.room.*
import com.example.tzd_cell.entity.Setting

@Dao
interface SettingDao {
    @Query("SELECT * FROM Settings WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Query("DELETE FROM Settings WHERE key = :key")
    suspend fun deleteByKey(key: String)
} 