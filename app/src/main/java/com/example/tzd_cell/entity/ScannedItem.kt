package com.example.tzd_cell.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ScannedItems")
data class ScannedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipient_id: String,
    val invoice_number: String,
    val total_places_in_invoice: Int,
    val current_place_in_invoice: Int,
    val scan_timestamp: Long
) 