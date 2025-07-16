package com.example.tzd_cell

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tzd_cell.entity.*
import com.example.tzd_cell.dao.*

@Database(
    entities = [ExpectedLoad::class, ScannedItem::class, RecipientName::class, Setting::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expectedLoadDao(): ExpectedLoadDao
    abstract fun scannedItemDao(): ScannedItemDao
    abstract fun recipientNameDao(): RecipientNameDao
    abstract fun settingDao(): SettingDao
} 