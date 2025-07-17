package com.example.tzd_cell.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tzd_cell.R
import com.example.tzd_cell.entity.ExpectedLoad
import com.example.tzd_cell.entity.ScannedItem
import android.graphics.drawable.GradientDrawable

class RecipientAdapter(
    private var expected: List<ExpectedLoad>,
    private var scanned: List<ScannedItem>,
    private var recipientNames: Map<String, String>,
    private val onEditName: (String) -> Unit
) : RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder>() {

    data class InvoicePlace(val invoice: String, val total: Int, val place: Int, val scanned: Boolean)

    data class RecipientGroup(
        val recipientId: String,
        val customName: String?,
        val invoices: Map<String, List<InvoicePlace>>,
        val allScanned: Boolean,
        val expectedPlaces: Int
    )

    private var groups: List<RecipientGroup> = emptyList()
    private var lastScannedRecipientId: String? = null

    fun updateData(expected: List<ExpectedLoad>, scanned: List<ScannedItem>, recipientNames: Map<String, String>) {
        this.expected = expected
        this.scanned = scanned
        this.recipientNames = recipientNames
        buildGroups()
        notifyDataSetChanged()
    }

    private fun buildGroups() {
        groups = expected.map { exp ->
            val customName = recipientNames[exp.recipient_id]
            val recipientScanned = scanned.filter { it.recipient_id == exp.recipient_id }
            val invoices = recipientScanned.groupBy { it.invoice_number }
            val invoiceMap = mutableMapOf<String, List<InvoicePlace>>()
            for (inv in invoices.keys) {
                val scannedPlaces = recipientScanned.filter { it.invoice_number == inv }
                val total = scannedPlaces.firstOrNull()?.total_places_in_invoice ?: 0
                val places = (1..total).map { place ->
                    InvoicePlace(
                        invoice = inv,
                        total = total,
                        place = place,
                        scanned = scannedPlaces.any { it.current_place_in_invoice == place }
                    )
                }
                invoiceMap[inv] = places
            }
            val allScanned = invoiceMap.isNotEmpty() && invoiceMap.values.flatten().all { it.scanned }
            RecipientGroup(
                recipientId = exp.recipient_id,
                customName = customName,
                invoices = invoiceMap,
                allScanned = allScanned,
                expectedPlaces = exp.expected_total_places_for_recipient
            )
        }
    }

    fun setLastScannedRecipient(recipientId: String?) {
        lastScannedRecipientId = recipientId
        notifyDataSetChanged()
    }

    fun getPositionForRecipient(recipientId: String?): Int {
        return groups.indexOfFirst { it.recipientId == recipientId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipient, parent, false)
        return RecipientViewHolder(view)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: RecipientViewHolder, position: Int) {
        val group = groups[position]
        val isLastScanned = group.recipientId == lastScannedRecipientId
        holder.bind(group, onEditName, isLastScanned)
    }

    class RecipientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.recipientName)
        private val placesCountText: TextView = itemView.findViewById(R.id.placesCountText)
        private val editBtn: ImageButton = itemView.findViewById(R.id.editNameBtn)
        private val invoicesLayout: LinearLayout = itemView.findViewById(R.id.invoicesLayout)
        fun bind(group: RecipientGroup, onEditName: (String) -> Unit, isLastScanned: Boolean = false) {
            nameText.text = group.customName ?: group.recipientId
            placesCountText.text = "${group.expectedPlaces} місць"
            editBtn.setOnClickListener { onEditName(group.recipientId) }
            if (group.allScanned) {
                itemView.setBackgroundResource(R.color.pastel_green)
            } else if (isLastScanned) {
                itemView.setBackgroundResource(R.color.teal_200) // або інший колір для підсвічування
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }
            invoicesLayout.removeAllViews()
            for ((invoice, places) in group.invoices) {
                val invoiceText = TextView(itemView.context)
                invoiceText.text = "Накладна №$invoice"
                invoicesLayout.addView(invoiceText)
                val scroll = android.widget.HorizontalScrollView(itemView.context)
                val placesLayout = LinearLayout(itemView.context)
                placesLayout.orientation = LinearLayout.HORIZONTAL
                for (place in places) {
                    val stick = View(itemView.context)
                    val widthDp = 10 // ширина патички
                    val heightDp = 32 // висота патички
                    val density = itemView.context.resources.displayMetrics.density
                    val widthPx = (widthDp * density).toInt()
                    val heightPx = (heightDp * density).toInt()
                    val params = LinearLayout.LayoutParams(widthPx, heightPx)
                    params.setMargins(4, 8, 4, 8)
                    stick.layoutParams = params
                    val drawable = GradientDrawable()
                    drawable.shape = GradientDrawable.RECTANGLE
                    drawable.cornerRadius = 8 * density // закруглені краї
                    drawable.setColor(
                        if (place.scanned) itemView.context.getColor(R.color.pastel_green) else itemView.context.getColor(android.R.color.darker_gray)
                    )
                    stick.background = drawable
                    placesLayout.addView(stick)
                }
                scroll.addView(placesLayout)
                invoicesLayout.addView(scroll)
            }
        }
    }
} 