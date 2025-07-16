package com.example.tzd_cell.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ExpectedLoad")
data class ExpectedLoad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipient_id: String,
    val expected_total_places_for_recipient: Int
) 