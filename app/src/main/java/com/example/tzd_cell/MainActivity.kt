package com.example.tzd_cell

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.tzd_cell.repository.LoadRepository
import com.example.tzd_cell.ui.MainViewModel
import com.example.tzd_cell.ui.RecipientAdapter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "TZD_DEBUG"
        const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        const val DATA = "data"
    }

    private lateinit var db: AppDatabase
    private lateinit var receiver: MyBroadcastReceiver
    private lateinit var repository: LoadRepository
    private lateinit var adapter: RecipientAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate: Activity створено")
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recipientsRecyclerView)
        val progressText = findViewById<android.widget.TextView>(R.id.progressText)
        val finishButton = findViewById<android.widget.Button>(R.id.finishButton)
        adapter = RecipientAdapter(emptyList(), emptyList(), emptyMap()) { recipientId ->
            showEditNameDialog(recipientId)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "tzd_cell_db"
        ).build()
        viewModel = object : ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(db) as T
            }
        }) {}.get(MainViewModel::class.java)
        lifecycleScope.launch {
            viewModel.expected.collect { updateAdapter() }
        }
        lifecycleScope.launch {
            viewModel.scanned.collect { updateAdapter() }
        }
        lifecycleScope.launch {
            viewModel.recipientNames.collect { updateAdapter() }
        }
        lifecycleScope.launch {
            viewModel.progress.collect { (scanned, total) ->
                progressText.text = "Прогрес: $scanned з $total"
            }
        }
        finishButton.setOnClickListener {
            viewModel.clearAll()
        }
        viewModel.loadAll()
        repository = LoadRepository(db)

        receiver = MyBroadcastReceiver().apply {
            onBarcodeScanned = { code ->
                android.util.Log.d(TAG, "onBarcodeScanned: $code")
                handleBarcode(code)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = android.content.IntentFilter(ACTION_DATA_CODE_RECEIVED)
        registerReceiver(receiver, filter)
        android.util.Log.d(TAG, "onResume: BroadcastReceiver зареєстровано (registerReceiver)")
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(receiver)
            android.util.Log.d(TAG, "onPause: BroadcastReceiver відмінено")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "onPause: unregisterReceiver exception: ${e.message}")
        }
    }

    private fun handleBarcode(barcode: String) {
        android.util.Log.d(TAG, "handleBarcode: отримано $barcode")
        lifecycleScope.launch {
            try {
                val result = repository.processBarcode(barcode, System.currentTimeMillis() / 1000)
                when (result) {
                    is LoadRepository.ProcessResult.Success -> {
                        val parsed = result.parsed
                        android.util.Log.d(TAG, "handleBarcode: Успіх, накладна ${parsed.invoiceNumber}, місце ${parsed.currentPlaceInInvoice + 1} з ${parsed.totalPlacesInInvoice}")
                        Toast.makeText(
                            this@MainActivity,
                            "Відскановано: Накладна №${parsed.invoiceNumber}, Місце ${parsed.currentPlaceInInvoice} з ${parsed.totalPlacesInInvoice}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.loadAll()
                        // Після оновлення даних встановлюємо фокус на просканованого отримувача
                        // Затримка, щоб дочекатися оновлення адаптера
                        recyclerView.postDelayed({
                            adapter.setLastScannedRecipient(parsed.recipientId)
                            val pos = adapter.getPositionForRecipient(parsed.recipientId)
                            if (pos >= 0) recyclerView.scrollToPosition(pos)
                        }, 200)
                    }
                    is LoadRepository.ProcessResult.AlreadyScanned -> {
                        android.util.Log.d(TAG, "handleBarcode: Уже відскановано")
                        Toast.makeText(this@MainActivity, "Уже відскановано", Toast.LENGTH_SHORT).show()
                        // TODO: Підсвітити у UI
                    }
                    is LoadRepository.ProcessResult.Error -> {
                        android.util.Log.d(TAG, "handleBarcode: Помилка: ${result.message}")
                        // Якщо повідомлення про застарілий штрихкод, додаємо кількість днів
                        val expiredPrefix = "Штрихкод застарілий"
                        if (result.message.startsWith(expiredPrefix)) {
                            val regex = Regex("(\\d+)")
                            val match = regex.find(barcode)
                            val now = System.currentTimeMillis() / 1000
                            val parsed = try { com.example.tzd_cell.util.Code36Utils.parseBarcode(barcode) } catch (_: Exception) { null }
                            val days = if (parsed != null) ((now - parsed.scanTimestamp) / 86400).toInt() else null
                            val msg = if (days != null && days > 0) "${result.message} на $days днів" else result.message
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "handleBarcode: Exception: ${e.message}")
                Toast.makeText(this@MainActivity, "Штрихкод не вірний", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAdapter() {
        adapter.updateData(
            viewModel.expected.value,
            viewModel.scanned.value,
            viewModel.recipientNames.value
        )
    }

    private fun showEditNameDialog(recipientId: String) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle("Редагувати ім'я отримувача")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.updateRecipientName(recipientId, name)
                }
            }
            .setNegativeButton("Відміна", null)
            .show()
    }
} 