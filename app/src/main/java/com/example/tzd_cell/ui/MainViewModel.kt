package com.example.tzd_cell.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tzd_cell.AppDatabase
import com.example.tzd_cell.entity.ExpectedLoad
import com.example.tzd_cell.entity.RecipientName
import com.example.tzd_cell.entity.ScannedItem
import com.example.tzd_cell.repository.LoadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val db: AppDatabase) : ViewModel() {
    private val repository = LoadRepository(db)

    private val _expected = MutableStateFlow<List<ExpectedLoad>>(emptyList())
    val expected: StateFlow<List<ExpectedLoad>> = _expected.asStateFlow()

    private val _scanned = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scanned: StateFlow<List<ScannedItem>> = _scanned.asStateFlow()

    private val _recipientNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val recipientNames: StateFlow<Map<String, String>> = _recipientNames.asStateFlow()

    private val _progress = MutableStateFlow(Pair(0, 0)) // scanned, total
    val progress: StateFlow<Pair<Int, Int>> = _progress.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            val expectedList = db.expectedLoadDao().getAll()
            val scannedList = db.scannedItemDao().getAll()
            val names = db.recipientNameDao().getAll().associate { it.recipient_id to it.custom_name }
            _expected.value = expectedList
            _scanned.value = scannedList
            _recipientNames.value = names
            val total = expectedList.sumOf { it.expected_total_places_for_recipient }
            val scannedCount = scannedList.count { item ->
                expectedList.any { it.recipient_id == item.recipient_id }
            }
            _progress.value = Pair(scannedCount, total)
        }
    }

    fun updateRecipientName(recipientId: String, name: String) {
        viewModelScope.launch {
            db.recipientNameDao().insert(RecipientName(recipientId, name))
            loadAll()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            loadAll()
        }
    }
} 