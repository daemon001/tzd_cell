package com.example.tzd_cell.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RecipientNames")
data class RecipientName(
    @PrimaryKey val recipient_id: String,
    val custom_name: String
) 