package com.example.tzd_cell.util

object Code36Utils {
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val charToValue = ALPHABET.withIndex().associate { it.value to it.index }

    fun decodeChar(c: Char): Int = charToValue[c] ?: throw IllegalArgumentException("Invalid Code36 char: $c")

    fun decodeString(s: String): Int {
        var result = 0
        for (c in s) {
            result = result * 36 + decodeChar(c)
        }
        return result
    }

    fun decodeTimestamp(s: String): Long = decodeString(s).toLong()

    data class ParsedBarcode(
        val recipientId: String,
        val invoiceNumber: Long,
        val totalPlacesInInvoice: Int,
        val currentPlaceInInvoice: Int,
        val scanTimestamp: Long,
        val recipients: List<RecipientInfo>
    ) {
        data class RecipientInfo(val recipientId: String, val totalPlaces: Int)
    }

    fun parseBarcode(barcode: String): ParsedBarcode {
        require(barcode.length >= 12) { "Barcode too short" }
        val recipientId = barcode[0].toString()
        val invoiceNumber = barcode.substring(1, 4).toLong(radix=36)
        val totalPlacesInInvoice = decodeChar(barcode[4])
        val currentPlaceInInvoice = decodeChar(barcode[5])
        val scanTimestamp = decodeTimestamp(barcode.substring(6, 12))
        val recipients = mutableListOf<ParsedBarcode.RecipientInfo>()
        var i = 12
        while (i + 1 < barcode.length) {
            val rId = barcode[i].toString()
            val rPlaces = decodeChar(barcode[i + 1])
            recipients.add(ParsedBarcode.RecipientInfo(rId, rPlaces))
            i += 2
        }
        if (recipients.none { it.recipientId == recipientId }) {
            recipients.add(ParsedBarcode.RecipientInfo(recipientId, totalPlacesInInvoice))
        }
        return ParsedBarcode(
            recipientId = recipientId,
            invoiceNumber = invoiceNumber,
            totalPlacesInInvoice = totalPlacesInInvoice,
            currentPlaceInInvoice = currentPlaceInInvoice,
            scanTimestamp = scanTimestamp,
            recipients = recipients
        )
    }
} 