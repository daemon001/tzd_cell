package com.example.tzd_cell.repository

import com.example.tzd_cell.AppDatabase
import com.example.tzd_cell.entity.*
import com.example.tzd_cell.util.Code36Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadRepository(private val db: AppDatabase) {
    companion object {
        private const val LAST_LOAD_TIMESTAMP_KEY = "last_load_timestamp"
        private const val ONE_WEEK_SECONDS = 604800L
    }

    suspend fun getLastLoadTimestamp(): Long =
        db.settingDao().getByKey(LAST_LOAD_TIMESTAMP_KEY)?.value ?: 0L

    suspend fun setLastLoadTimestamp(value: Long) {
        db.settingDao().insert(Setting(LAST_LOAD_TIMESTAMP_KEY, value))
    }

    suspend fun clearAll() {
        db.expectedLoadDao().clear()
        db.scannedItemDao().clear()
        setLastLoadTimestamp(0L)
    }

    suspend fun processBarcode(barcode: String, currentTime: Long): ProcessResult = withContext(Dispatchers.IO) {
        val parsed = Code36Utils.parseBarcode(barcode)
        val barcodeTimestamp = parsed.scanTimestamp
        val lastLoadTimestamp = getLastLoadTimestamp()
        // Перевірка на застарілий штрихкод
        if (currentTime - barcodeTimestamp > ONE_WEEK_SECONDS) {
            return@withContext ProcessResult.Error("Штрихкод застарілий")
        }
        // Якщо штрихкод новіший за last_load_timestamp — оновлюємо ExpectedLoad
        if (barcodeTimestamp > lastLoadTimestamp) {
            db.expectedLoadDao().clear()
            val expected = parsed.recipients.map {
                ExpectedLoad(recipient_id = it.recipientId, expected_total_places_for_recipient = it.totalPlaces)
            }
            db.expectedLoadDao().insertAll(expected)
            setLastLoadTimestamp(barcodeTimestamp)
        }
        // Додаємо/оновлюємо відскановане місце
        val scannedItem = ScannedItem(
            recipient_id = parsed.recipientId,
            invoice_number = parsed.invoiceNumber.toString(),
            total_places_in_invoice = parsed.totalPlacesInInvoice,
            current_place_in_invoice = parsed.currentPlaceInInvoice,
            scan_timestamp = barcodeTimestamp
        )
        val alreadyScanned = db.scannedItemDao().getByUnique(
            parsed.recipientId,
            parsed.invoiceNumber.toString(),
            parsed.currentPlaceInInvoice
        )
        if (alreadyScanned != null) {
            return@withContext ProcessResult.AlreadyScanned
        }
        db.scannedItemDao().insert(scannedItem)
        return@withContext ProcessResult.Success(parsed)
    }

    sealed class ProcessResult {
        data class Success(val parsed: Code36Utils.ParsedBarcode) : ProcessResult()
        object AlreadyScanned : ProcessResult()
        data class Error(val message: String) : ProcessResult()
    }
} 