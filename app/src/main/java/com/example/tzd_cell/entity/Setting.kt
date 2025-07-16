package com.example.tzd_cell.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: Long
) 